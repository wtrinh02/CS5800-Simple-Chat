import Message.*;
import User.State.*;
import User.*;
import User.UserBuilder;


import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ChatServer {
    private static final int PORT = 8888;

    private static final String DATA_DIR      = "data";
    private static final String USERS_FILE    = DATA_DIR + File.separator + "users.csv";
    private static final String DM_DIR        = DATA_DIR + File.separator + "dm";
    private static final String FRIENDS_FILE  = DATA_DIR + File.separator + "friends.csv";
    private static final String BLOCKED_FILE  = DATA_DIR + File.separator + "blocked.csv";

    private final Map<String, User> users;
    private final Map<String, ClientHandler> onlineClients;
    private final Map<String, LocalServer> localServers;
    private final ExecutorService threadPool;
    private ServerSocket serverSocket;
    private MessageFactory MessageFactory;

    public ChatServer() {
        MessageFactory = new MessageFactory();
        this.users = new ConcurrentHashMap<>();
        this.onlineClients = new ConcurrentHashMap<>();
        this.localServers = new ConcurrentHashMap<>();
        this.threadPool = Executors.newCachedThreadPool();

        loadUsersFromFile();
        loadFriendsFromFile();
        loadBlockedFromFile();

        LocalServer generalServer = new LocalServer("general", "General", "SYSTEM");
        localServers.put("general", generalServer);
        System.out.println("Default 'General' server created");
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Chat Server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket, this);
                threadPool.execute(handler);
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    public synchronized void registerUser(String userId, String username, String email) {
        if (!users.containsKey(userId)) {
            User user = new UserBuilder()
                    .setUserId(userId)
                    .setUsername(username)
                    .setEmail(email)
                    .setOnline(true)
                    .build();
            users.put(userId, user);
            appendUserToFile(user);
            System.out.println("User.User registered: " + username);
        }
    }

    public synchronized void connectUser(String userId, ClientHandler handler) {
        User user = users.get(userId);
        if (user != null) {
            user.setState(new OnlineState());
            user.setOnline(true);
            onlineClients.put(userId, handler);

            Message onlineMsg = MessageFactory.userOnline(user.getUsername());
            broadcastToServer("general", "SERVER_MSG:general:SYSTEM:SYSTEM:" + onlineMsg.getContent());

            LocalServer generalServer = localServers.get("general");
            if (generalServer != null) {
                generalServer.addMember(userId);
                handler.sendMessage("SERVER_JOINED:general:" + generalServer.getServerName());

                Message joinMessage = MessageFactory.serverJoin(user.getUsername(), "general");
                broadcastToServer("general",
                        "SERVER_MSG:general:SYSTEM:SYSTEM:" + joinMessage.getContent()
                );
            }

            notifyFriendsOnlineStatus(userId, true);
            System.out.println("User.User connected: " + user.getUsername());
        }
    }

    public synchronized void disconnectUser(String userId) {
        User user = users.get(userId);
        if (user != null) {
            user.setState(new OfflineState());
            user.setOnline(false);
            onlineClients.remove(userId);

            Message offlineMsg = MessageFactory.userOffline(user.getUsername());
            broadcastToServer("general", "SERVER_MSG:general:SYSTEM:SYSTEM:" + offlineMsg.getContent());

            LocalServer generalServer = localServers.get("general");
            if (generalServer != null && generalServer.isMember(userId)) {
                generalServer.removeMember(userId);

                Message leaveMessage = MessageFactory.serverLeave(user.getUsername(), "general");
                broadcastToServer("general",
                        "SERVER_MSG:general:SYSTEM:SYSTEM:" + leaveMessage.getContent()
                );
            }

            notifyFriendsOnlineStatus(userId, false);
            System.out.println("User.User disconnected: " + user.getUsername());
        }
    }

    public synchronized void sendFriendRequest(String senderId, String receiverId) {
        User sender = users.get(senderId);
        User receiver = users.get(receiverId);

        if (sender == null || receiver == null) {
            sendToClient(senderId, "ERROR: User.User not found");
            return;
        }
        if (receiver.hasBlocked(senderId)) {
            sendToClient(senderId, "ERROR: Cannot send friend request (you are blocked by this user)");
            return;
        }

        Message friendRequest = MessageFactory.friendRequest(senderId, receiverId);

        sendToClient(receiverId, "FRIEND_REQUEST:" + senderId + ":" + sender.getUsername());
        System.out.println("Friend request: " + senderId + " -> " + receiverId);
    }

    public synchronized void acceptFriendRequest(String userId, String friendId) {
        User user = users.get(userId);
        User friend = users.get(friendId);

        if (user == null || friend == null) {
            sendToClient(userId, "ERROR: User.User not found");
            return;
        }

        user.addFriend(friendId);
        friend.addFriend(userId);

        appendFriendshipToFile(userId, friendId);
        appendFriendshipToFile(friendId, userId);

        sendToClient(userId, "FRIEND_ADDED:" + friendId + ":" + friend.getUsername());
        sendToClient(friendId, "FRIEND_ADDED:" + userId + ":" + user.getUsername());

        System.out.println("Friends added: " + userId + " <-> " + friendId);
    }

    public synchronized void sendDirectMessage(String senderId, String receiverId, String content) {
        User sender = users.get(senderId);
        User receiver = users.get(receiverId);

        if (sender == null || receiver == null) {
            sendToClient(senderId, "ERROR: User.User not found");
            return;
        }

        if (receiver.hasBlocked(senderId) || sender.hasBlocked(receiverId)) {
            sendToClient(senderId, "ERROR: Cannot send direct message (user is blocked)");
            return;
        }

        if (!sender.isFriend(receiverId)) {
            sendToClient(senderId, "ERROR: Not friends with this user");
            return;
        }

        Message message = MessageFactory.directMessage(senderId, receiverId, content);

        String conversationId = getConversationId(senderId, receiverId);
        sender.addDirectMessage(conversationId, message);
        receiver.addDirectMessage(conversationId, message);

        appendDmToFile(conversationId, message);

        sendToClient(receiverId, "DM:" + senderId + ":" + sender.getUsername() + ":" + content);
        sendToClient(senderId, "DM_DELIVERED:" + receiverId + ":" + receiver.getUsername() + ":" + content);

        System.out.println("DM: " + senderId + " -> " + receiverId + ": " + content);
    }

    public synchronized List<String> getOnlineFriends(String userId) {
        User user = users.get(userId);
        if (user == null) return new ArrayList<>();

        List<String> onlineFriends = new ArrayList<>();
        for (String friendId : user.getFriendIds()) {
            User friend = users.get(friendId);
            if (friend != null && friend.isOnline()) {
                onlineFriends.add(friendId + ":" + friend.getUsername());
            }
        }
        return onlineFriends;
    }

    public synchronized void blockUser(String userId, String blockedId) {
        User user = users.get(userId);
        User target = users.get(blockedId);

        if (user == null || target == null) {
            sendToClient(userId, "ERROR: User.User not found");
            return;
        }

        user.blockUser(blockedId);
        appendBlockedToFile(userId, blockedId);
        sendToClient(userId, "BLOCKED:" + blockedId + ":" + target.getUsername());
    }

    public synchronized void unblockUser(String userId, String blockedId) {
        User user = users.get(userId);
        User target = users.get(blockedId);

        if (user == null || target == null) {
            sendToClient(userId, "ERROR: User.User not found");
            return;
        }

        user.unblockUser(blockedId);
        rewriteBlockedFileFromMemory();

        sendToClient(userId, "UNBLOCKED:" + blockedId + ":" + target.getUsername());
    }

    public synchronized List<String> getBlockedUsers(String userId) {
        User user = users.get(userId);
        List<String> result = new ArrayList<>();
        if (user == null) {
            return result;
        }
        for (String blockedId : user.getBlockedUserIds()) {
            User blockedUser = users.get(blockedId);
            if (blockedUser != null) {
                result.add(blockedId + ":" + blockedUser.getUsername());
            }
        }
        return result;
    }

    private void notifyFriendsOnlineStatus(String userId, boolean online) {
        User user = users.get(userId);
        if (user == null) return;

        String status = online ? "ONLINE" : "OFFLINE";
        for (String friendId : user.getFriendIds()) {
            sendToClient(friendId, "STATUS:" + userId + ":" + user.getUsername() + ":" + status);
        }
    }

    private void sendToClient(String userId, String message) {
        ClientHandler handler = onlineClients.get(userId);
        if (handler != null) {
            handler.sendMessage(message);
        }
    }

    private String getConversationId(String userId1, String userId2) {
        return userId1.compareTo(userId2) < 0
                ? userId1 + "_" + userId2
                : userId2 + "_" + userId1;
    }

    public User getUser(String userId) {
        return users.get(userId);
    }

    public synchronized List<Message> getConversationHistory(String userId, String friendId) {
        List<Message> result = new ArrayList<>();
        String conversationId = getConversationId(userId, friendId);

        User user = users.get(userId);
        if (user != null) {
            result.addAll(user.getDirectMessages(conversationId));
        }

        if (!result.isEmpty()) {
            return result;
        }

        File dmDir = new File(DM_DIR);
        if (!dmDir.exists()) {
            return result;
        }

        File file = new File(dmDir, conversationId + ".log");
        if (!file.exists()) {
            return result;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|", 5);
                if (parts.length >= 4) {
                    String sender   = parts[1];
                    String receiver = parts[2];
                    String content  = parts[3];

                    Message msg = new Message(
                            UUID.randomUUID().toString(),
                            sender,
                            receiver,
                            content,
                            Message.MessageType.DIRECT_MESSAGE
                    );
                    result.add(msg);
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load DM history: " + e.getMessage());
        }

        return result;
    }

    public synchronized void createLocalServer(String serverId, String serverName, String ownerId) {
        if (!localServers.containsKey(serverId)) {
            LocalServer server = new LocalServer(serverId, serverName, ownerId);
            localServers.put(serverId, server);
            sendToClient(ownerId, "SERVER_CREATED:" + serverId + ":" + serverName);

            for (String uid : onlineClients.keySet()) {
                if (!uid.equals(ownerId)) {
                    User creator = users.get(ownerId);
                    String creatorName = creator != null ? creator.getUsername() : "Unknown";
                    sendToClient(uid, "NEW_SERVER:" + serverId + ":" + serverName + ":" + creatorName);
                }
            }

            System.out.println("Local server created: " + serverName + " by " + ownerId);
        }
    }

    public synchronized void joinLocalServer(String userId, String serverId) {
        LocalServer server = localServers.get(serverId);
        User user = users.get(userId);

        if (server == null) {
            sendToClient(userId, "ERROR: Server not found");
            return;
        }

        if (user == null) {
            return;
        }

        server.addMember(userId);

        sendToClient(userId, "SERVER_JOINED:" + serverId + ":" + server.getServerName());

        Message joinMessage = MessageFactory.serverJoin(user.getUsername(), serverId);

        broadcastToServer(serverId,
                "SERVER_MSG:" + serverId + ":SYSTEM:SYSTEM:" + joinMessage.getContent()
        );

        System.out.println("User " + userId + " joined server: " + serverId);
    }

    public synchronized void leaveLocalServer(String userId, String serverId) {
        LocalServer server = localServers.get(serverId);
        User user = users.get(userId);

        if (server == null || user == null) {
            return;
        }

        if (userId.equals(server.getOwnerId())) {
            sendToClient(userId, "ERROR: Server owner cannot leave");
            return;
        }

        server.removeMember(userId);
        sendToClient(userId, "SERVER_LEFT:" + serverId);

        Message leaveMessage = MessageFactory.serverLeave(user.getUsername(), serverId);

        broadcastToServer(serverId,
                "SERVER_MSG:" + serverId + ":SYSTEM:SYSTEM:" + leaveMessage.getContent()
        );

        System.out.println("User " + userId + " left server: " + serverId);
    }

    public synchronized void sendServerMessage(String userId, String serverId, String content) {
        LocalServer server = localServers.get(serverId);
        User user = users.get(userId);

        if (server == null) {
            sendToClient(userId, "ERROR: Server not found");
            return;
        }

        if (user == null) {
            return;
        }

        if (!server.isMember(userId)) {
            sendToClient(userId, "ERROR: You are not a member of this server");
            return;
        }

        Message message = MessageFactory.serverMessage(userId, serverId, content);

        server.addMessage(message);
        broadcastToServer(serverId,
                "SERVER_MSG:" + serverId + ":" + userId + ":" + user.getUsername() + ":" + content
        );

        System.out.println("Server message in " + serverId + " from " + userId + ": " + content);
    }

    public synchronized List<String> listLocalServers() {
        List<String> serverList = new ArrayList<>();
        for (LocalServer server : localServers.values()) {
            serverList.add(server.getServerId() + ":" + server.getServerName() + ":" + server.getMemberCount());
        }
        return serverList;
    }

    public synchronized List<String> getServerMembers(String serverId) {
        LocalServer server = localServers.get(serverId);
        if (server == null) {
            return new ArrayList<>();
        }

        List<String> memberList = new ArrayList<>();
        for (String memberId : server.getMembers()) {
            User member = users.get(memberId);
            if (member != null) {
                memberList.add(memberId + ":" + member.getUsername());
            }
        }
        return memberList;
    }

    private void broadcastToServer(String serverId, String message) {
        LocalServer server = localServers.get(serverId);
        if (server == null) {
            return;
        }

        for (String memberId : server.getMembers()) {
            sendToClient(memberId, message);
        }
    }

    public void shutdown() {
        try {
            threadPool.shutdown();
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            System.err.println("Error shutting down server: " + e.getMessage());
        }
    }

    private void loadUsersFromFile() {
        File file = new File(USERS_FILE);
        if (!file.exists()) {
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int count = 0;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", 3);
                if (parts.length == 3) {
                    String id    = parts[0];
                    String name  = parts[1];
                    String email = parts[2];

                    User user = new UserBuilder()
                            .setUserId(id)
                            .setUsername(name)
                            .setEmail(email)
                            .setOnline(false)
                            .build();
                    users.put(id, user);
                    count++;
                }
            }
            System.out.println("Loaded " + count + " users from disk.");
        } catch (IOException e) {
            System.err.println("Failed to load users from file: " + e.getMessage());
        }
    }

    private void appendUserToFile(User user) {
        try {
            File dataDir = new File(DATA_DIR);
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }

            try (FileWriter fw = new FileWriter(USERS_FILE, true);
                 BufferedWriter bw = new BufferedWriter(fw);
                 PrintWriter out = new PrintWriter(bw)) {

                out.println(user.getUserId() + "," +
                        user.getUsername() + "," +
                        user.getEmail());
            }
        } catch (IOException e) {
            System.err.println("Failed to persist user: " + e.getMessage());
        }
    }

    private void appendDmToFile(String conversationId, Message message) {
        try {
            File dmDir = new File(DM_DIR);
            if (!dmDir.exists()) {
                dmDir.mkdirs();
            }

            File file = new File(dmDir, conversationId + ".log");
            try (FileWriter fw = new FileWriter(file, true);
                 BufferedWriter bw = new BufferedWriter(fw);
                 PrintWriter out = new PrintWriter(bw)) {

                out.println(message.getFormattedTimestamp() + "|" +
                        message.getSenderId() + "|" +
                        message.getReceiverId() + "|" +
                        message.getContent().replace("\n", " ") + "|" +
                        message.getType().name());
            }
        } catch (IOException e) {
            System.err.println("Failed to persist DM: " + e.getMessage());
        }
    }

    private void appendFriendshipToFile(String userId, String friendId) {
        try {
            File dataDir = new File(DATA_DIR);
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }

            try (FileWriter fw = new FileWriter(FRIENDS_FILE, true);
                 BufferedWriter bw = new BufferedWriter(fw);
                 PrintWriter out = new PrintWriter(bw)) {

                out.println(userId + "," + friendId);
            }
        } catch (IOException e) {
            System.err.println("Failed to persist friendship: " + e.getMessage());
        }
    }

    private void loadFriendsFromFile() {
        File file = new File(FRIENDS_FILE);
        if (!file.exists()) {
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int count = 0;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", 2);
                if (parts.length == 2) {
                    String userId   = parts[0];
                    String friendId = parts[1];

                    User user = users.get(userId);
                    if (user != null) {
                        user.addFriend(friendId);
                        count++;
                    }
                }
            }
            System.out.println("Loaded " + count + " friendship links from disk.");
        } catch (IOException e) {
            System.err.println("Failed to load friendships: " + e.getMessage());
        }
    }

    private void appendBlockedToFile(String userId, String blockedId) {
        try {
            File dataDir = new File(DATA_DIR);
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }

            try (FileWriter fw = new FileWriter(BLOCKED_FILE, true);
                 BufferedWriter bw = new BufferedWriter(fw);
                 PrintWriter out = new PrintWriter(bw)) {

                out.println(userId + "," + blockedId);
            }
        } catch (IOException e) {
            System.err.println("Failed to persist blocked user: " + e.getMessage());
        }
    }

    private void loadBlockedFromFile() {
        File file = new File(BLOCKED_FILE);
        if (!file.exists()) {
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int count = 0;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", 2);
                if (parts.length == 2) {
                    String userId    = parts[0];
                    String blockedId = parts[1];

                    User user = users.get(userId);
                    if (user != null) {
                        user.blockUser(blockedId);
                        count++;
                    }
                }
            }
            System.out.println("Loaded " + count + " block relationships from disk.");
        } catch (IOException e) {
            System.err.println("Failed to load blocked users: " + e.getMessage());
        }
    }

    private void rewriteBlockedFileFromMemory() {
        try {
            File dataDir = new File(DATA_DIR);
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }

            try (FileWriter fw = new FileWriter(BLOCKED_FILE, false);
                 BufferedWriter bw = new BufferedWriter(fw);
                 PrintWriter out = new PrintWriter(bw)) {

                for (User u : users.values()) {
                    for (String blockedId : u.getBlockedUserIds()) {
                        out.println(u.getUserId() + "," + blockedId);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to rewrite blocked file: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        ChatServer server = new ChatServer();
        server.start();
    }
}

class ClientHandler implements Runnable {
    private final Socket socket;
    private final ChatServer server;
    private BufferedReader in;
    private PrintWriter out;
    private String userId;

    public ClientHandler(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                processCommand(inputLine);
            }
        } catch (IOException e) {
            System.err.println("Client handler error: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    private void processCommand(String command) {
        String[] parts = command.split(":", 4);
        String action = parts[0];

        switch (action) {
            case "REGISTER":
                if (parts.length >= 3) {
                    userId = parts[1];
                    String username = parts[2];
                    String email = parts.length > 3 ? parts[3] : "";
                    server.registerUser(userId, username, email);
                    server.connectUser(userId, this);
                    sendMessage("REGISTERED:" + userId);
                }
                break;

            case "FRIEND_REQUEST":
                if (parts.length >= 2) {
                    server.sendFriendRequest(userId, parts[1]);
                }
                break;

            case "ACCEPT_FRIEND":
                if (parts.length >= 2) {
                    server.acceptFriendRequest(userId, parts[1]);
                }
                break;

            case "SEND_DM":
                if (parts.length >= 3) {
                    server.sendDirectMessage(userId, parts[1], parts[2]);
                }
                break;

            case "GET_FRIENDS":
                List<String> friends = server.getOnlineFriends(userId);
                sendMessage("FRIENDS:" + String.join(",", friends));
                break;

            case "CREATE_SERVER":
                if (parts.length >= 3) {
                    String serverId = parts[1];
                    String serverName = parts[2];
                    server.createLocalServer(serverId, serverName, userId);
                }
                break;

            case "JOIN_SERVER":
                if (parts.length >= 2) {
                    server.joinLocalServer(userId, parts[1]);
                }
                break;

            case "LEAVE_SERVER":
                if (parts.length >= 2) {
                    server.leaveLocalServer(userId, parts[1]);
                }
                break;

            case "SERVER_MSG":
                if (parts.length >= 3) {
                    server.sendServerMessage(userId, parts[1], parts[2]);
                }
                break;

            case "LIST_SERVERS":
                List<String> servers = server.listLocalServers();
                sendMessage("SERVERS:" + String.join(",", servers));
                break;

            case "SERVER_MEMBERS":
                if (parts.length >= 2) {
                    List<String> members = server.getServerMembers(parts[1]);
                    sendMessage("MEMBERS:" + parts[1] + ":" + String.join(",", members));
                }
                break;

            case "BLOCK_USER":
                if (parts.length >= 2) {
                    server.blockUser(userId, parts[1]);
                }
                break;

            case "UNBLOCK_USER":
                if (parts.length >= 2) {
                    server.unblockUser(userId, parts[1]);
                }
                break;

            case "GET_BLOCKED":
                List<String> blocked = server.getBlockedUsers(userId);
                sendMessage("BLOCKED_LIST:" + String.join(",", blocked));
                break;

            case "GET_HISTORY":
                if (parts.length >= 2) {
                    String friendId = parts[1];
                    List<Message> history = server.getConversationHistory(userId, friendId);
                    StringBuilder payload = new StringBuilder();
                    for (Message m : history) {
                        if (payload.length() > 0) {
                            payload.append("|");
                        }
                        payload.append(m.getFormattedTimestamp())
                                .append("~")
                                .append(m.getSenderId())
                                .append("~")
                                .append(m.getContent());
                    }
                    sendMessage("HISTORY:" + friendId + ":" + payload);
                }
                break;
        }
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    private void cleanup() {
        try {
            if (userId != null) {
                server.disconnectUser(userId);
            }
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Cleanup error: " + e.getMessage());
        }
    }
}
