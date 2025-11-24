import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ChatServer {
    private static final int PORT = 8888;
    private final Map<String, User> users;
    private final Map<String, ClientHandler> onlineClients;
    private final Map<String, LocalServer> localServers;
    private final ExecutorService threadPool;
    private ServerSocket serverSocket;

    public ChatServer() {
        this.users = new ConcurrentHashMap<>();
        this.onlineClients = new ConcurrentHashMap<>();
        this.localServers = new ConcurrentHashMap<>();
        this.threadPool = Executors.newCachedThreadPool();


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
            System.out.println("User registered: " + username);
        }
    }

    public synchronized void connectUser(String userId, ClientHandler handler) {
        User user = users.get(userId);
        if (user != null) {
            user.setOnline(true);
            onlineClients.put(userId, handler);


            LocalServer generalServer = localServers.get("general");
            if (generalServer != null) {
                generalServer.addMember(userId);
                handler.sendMessage("SERVER_JOINED:general:" + generalServer.getServerName());


                Message joinMessage = new Message(
                        UUID.randomUUID().toString(),
                        "SYSTEM",
                        "general",
                        user.getUsername() + " joined the server",
                        Message.MessageType.SERVER_JOIN
                );
                broadcastToServer("general", "SERVER_MSG:SYSTEM:" + joinMessage.getContent());
            }

            notifyFriendsOnlineStatus(userId, true);
            System.out.println("User connected: " + user.getUsername());
        }
    }

    public synchronized void disconnectUser(String userId) {
        User user = users.get(userId);
        if (user != null) {
            user.setOnline(false);
            onlineClients.remove(userId);


            LocalServer generalServer = localServers.get("general");
            if (generalServer != null && generalServer.isMember(userId)) {
                generalServer.removeMember(userId);
                Message leaveMessage = new Message(
                        UUID.randomUUID().toString(),
                        "SYSTEM",
                        "general",
                        user.getUsername() + " left the server",
                        Message.MessageType.SERVER_LEAVE
                );
                broadcastToServer("general", "SERVER_MSG:SYSTEM:" + leaveMessage.getContent());
            }

            notifyFriendsOnlineStatus(userId, false);
            System.out.println("User disconnected: " + user.getUsername());
        }
    }

    public synchronized void sendFriendRequest(String senderId, String receiverId) {
        User sender = users.get(senderId);
        User receiver = users.get(receiverId);

        if (sender == null || receiver == null) {
            sendToClient(senderId, "ERROR: User not found");
            return;
        }

        Message friendRequest = new Message(
                UUID.randomUUID().toString(),
                senderId,
                receiverId,
                sender.getUsername() + " wants to be your friend!",
                Message.MessageType.FRIEND_REQUEST
        );

        sendToClient(receiverId, "FRIEND_REQUEST:" + senderId + ":" + sender.getUsername());
        System.out.println("Friend request: " + senderId + " -> " + receiverId);
    }

    public synchronized void acceptFriendRequest(String userId, String friendId) {
        User user = users.get(userId);
        User friend = users.get(friendId);

        if (user == null || friend == null) {
            sendToClient(userId, "ERROR: User not found");
            return;
        }

        user.addFriend(friendId);
        friend.addFriend(userId);

        sendToClient(userId, "FRIEND_ADDED:" + friendId + ":" + friend.getUsername());
        sendToClient(friendId, "FRIEND_ADDED:" + userId + ":" + user.getUsername());

        System.out.println("Friends added: " + userId + " <-> " + friendId);
    }

    public synchronized void sendDirectMessage(String senderId, String receiverId, String content) {
        User sender = users.get(senderId);
        User receiver = users.get(receiverId);

        if (sender == null || receiver == null) {
            sendToClient(senderId, "ERROR: User not found");
            return;
        }

        if (!sender.isFriend(receiverId)) {
            sendToClient(senderId, "ERROR: Not friends with this user");
            return;
        }

        Message message = new Message(
                UUID.randomUUID().toString(),
                senderId,
                receiverId,
                content,
                Message.MessageType.DIRECT_MESSAGE
        );

        String conversationId = getConversationId(senderId, receiverId);
        sender.addDirectMessage(conversationId, message);
        receiver.addDirectMessage(conversationId, message);

        sendToClient(receiverId, "DM:" + senderId + ":" + sender.getUsername() + ":" + content);
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


    public synchronized void createLocalServer(String serverId, String serverName, String ownerId) {
        if (!localServers.containsKey(serverId)) {
            LocalServer server = new LocalServer(serverId, serverName, ownerId);
            localServers.put(serverId, server);
            sendToClient(ownerId, "SERVER_CREATED:" + serverId + ":" + serverName);


            for (String userId : onlineClients.keySet()) {
                if (!userId.equals(ownerId)) {
                    User creator = users.get(ownerId);
                    String creatorName = creator != null ? creator.getUsername() : "Unknown";
                    sendToClient(userId, "NEW_SERVER:" + serverId + ":" + serverName + ":" + creatorName);
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


        Message joinMessage = new Message(
                UUID.randomUUID().toString(),
                "SYSTEM",
                serverId,
                user.getUsername() + " joined the server",
                Message.MessageType.SERVER_JOIN
        );
        broadcastToServer(serverId, "SERVER_MSG:SYSTEM:" + joinMessage.getContent());

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


        Message leaveMessage = new Message(
                UUID.randomUUID().toString(),
                "SYSTEM",
                serverId,
                user.getUsername() + " left the server",
                Message.MessageType.SERVER_LEAVE
        );
        broadcastToServer(serverId, "SERVER_MSG:SYSTEM:" + leaveMessage.getContent());

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

        Message message = new Message(
                UUID.randomUUID().toString(),
                userId,
                serverId,
                content,
                Message.MessageType.SERVER_MESSAGE
        );

        server.addMessage(message);
        broadcastToServer(serverId, "SERVER_MSG:" + userId + ":" + user.getUsername() + ":" + content);

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