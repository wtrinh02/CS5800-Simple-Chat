import Message.*;
import Message.Decorator.BaseMessage;
import Message.Decorator.MessageComponent;
import Message.Decorator.SenderNameDecorator;
import Message.Decorator.TimestampDecorator;

import User.State.*;
import User.*;
import User.UserBuilder;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import db.dao.UserDAO;
import db.dao.FriendDAO;
import db.dao.BlockedDAO;
import db.dao.DMDAO;
import db.dao.ServerDAO;
import db.dao.ServerMessageDAO;

public class ChatServer {
    private static final int PORT = 8888;

    // --- DAOs (database layer) ---
    private final UserDAO userDAO = new UserDAO();
    private final FriendDAO friendDAO = new FriendDAO();
    private final BlockedDAO blockedDAO = new BlockedDAO();
    private final DMDAO dmDAO = new DMDAO();
    private final ServerDAO serverDAO = new ServerDAO();
    private final ServerMessageDAO serverMessageDAO = new ServerMessageDAO();

    // --- In-memory runtime layer ---
    private final Map<String, User> users;                 // userId -> User
    private final Map<String, ClientHandler> onlineClients;// userId -> connection
    private final Map<String, LocalServer> localServers;   // serverId -> LocalServer
    private final ExecutorService threadPool;
    private ServerSocket serverSocket;
    private MessageFactory messageFactory;

    public ChatServer() {
        this.messageFactory = new MessageFactory();
        this.users = new ConcurrentHashMap<>();
        this.onlineClients = new ConcurrentHashMap<>();
        this.localServers = new ConcurrentHashMap<>();
        this.threadPool = Executors.newCachedThreadPool();

        // Create default "general" server (DB + in-memory)
        if (!serverDAO.exists("general")) {
            serverDAO.createServer("general", "General", "SYSTEM");
        }
        LocalServer generalServer = new LocalServer("general", "General", "SYSTEM");
        localServers.put("general", generalServer);
        System.out.println("Default 'General' server created");
    }

    // -------------------------------------------------------------------------
    // SERVER LIFECYCLE
    // -------------------------------------------------------------------------
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

    public void shutdown() {
        try {
            threadPool.shutdown();
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            System.err.println("Error shutting down server: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // PASSWORD / AUTH HELPERS
    // -------------------------------------------------------------------------
    // Very simple "hash" (for class project). In real life, use BCrypt/Argon2.
    private String hashPassword(String rawPassword) {
        // For now, just store as-is (plain text) to keep it simple.
        // If you want, you can change this to a real hash later.
        return rawPassword;
    }

    public boolean userExists(String userId) {
        return userDAO.exists(userId);
    }

    public boolean isPasswordValid(String userId, String rawPassword) {
        String stored = userDAO.getPasswordHash(userId);
        if (stored == null) return false;
        return stored.equals(hashPassword(rawPassword));
    }

    // -------------------------------------------------------------------------
    // USERS
    // -------------------------------------------------------------------------

    // Legacy overload (used by tests / older code)
    public synchronized void registerUser(String userId, String username, String email) {
        registerUser(userId, username, email, "NO_PASSWORD_SET");
    }

    // New registration method with password
    public synchronized void registerUser(String userId, String username, String email, String rawPassword) {
        if (!userDAO.exists(userId)) {
            userDAO.createUser(userId, username, email, hashPassword(rawPassword));
        }

        if (!users.containsKey(userId)) {
            User user = new UserBuilder()
                    .setUserId(userId)
                    .setUsername(username)
                    .setEmail(email)
                    .setOnline(false)
                    .build();
            users.put(userId, user);
            System.out.println("User registered: " + username);
        }
    }

    public synchronized void connectUser(String userId, ClientHandler handler) {
        User user = users.get(userId);

        // Load from DB if needed
        if (user == null) {
            db.model.DbUser dbUser = userDAO.getUserById(userId);
            if (dbUser == null) {
                handler.sendMessage("ERROR: User not found");
                return;
            }
            user = new UserBuilder()
                    .setUserId(dbUser.id())
                    .setUsername(dbUser.username())
                    .setEmail(dbUser.email())
                    .setOnline(false)
                    .build();
            users.put(userId, user);
        }

        user.setState(new OnlineState());
        user.setOnline(true);
        onlineClients.put(userId, handler);
        userDAO.setOnline(userId, true);

        // System: user online (time msg)
        Message onlineMsg = messageFactory.userOnline(user.getUsername());
        String displayOnline = formatForDisplay(onlineMsg);
        broadcastToServer("general", "SERVER_MSG:general:SYSTEM:SYSTEM:" + displayOnline);

        LocalServer generalServer = localServers.get("general");
        if (generalServer != null) {
            serverDAO.addMember("general", userId);
            generalServer.addMember(userId);
            handler.sendMessage("SERVER_JOINED:general:" + generalServer.getServerName());

            Message joinMessage = messageFactory.serverJoin(user.getUsername(), "general");
            String displayJoin = formatForDisplay(joinMessage);
            broadcastToServer(
                    "general",
                    "SERVER_MSG:general:SYSTEM:SYSTEM:" + displayJoin
            );
        }

        notifyFriendsOnlineStatus(userId, true);
        System.out.println("User connected: " + user.getUsername());
    }

    public synchronized void disconnectUser(String userId) {
        User user = users.get(userId);
        if (user != null) {
            user.setState(new OfflineState());
            user.setOnline(false);
            onlineClients.remove(userId);
            userDAO.setOnline(userId, false);

            Message offlineMsg = messageFactory.userOffline(user.getUsername());
            String displayOffline = formatForDisplay(offlineMsg);
            broadcastToServer(
                    "general",
                    "SERVER_MSG:general:SYSTEM:SYSTEM:" + displayOffline
            );

            LocalServer generalServer = localServers.get("general");
            if (generalServer != null && generalServer.isMember(userId)) {
                generalServer.removeMember(userId);
                serverDAO.removeMember("general", userId);

                Message leaveMessage = messageFactory.serverLeave(user.getUsername(), "general");
                String displayLeave = formatForDisplay(leaveMessage);
                broadcastToServer(
                        "general",
                        "SERVER_MSG:general:SYSTEM:SYSTEM:" + displayLeave
                );
            }

            notifyFriendsOnlineStatus(userId, false);
            System.out.println("User disconnected: " + user.getUsername());
        }
    }

    // -------------------------------------------------------------------------
    // FRIENDS
    // -------------------------------------------------------------------------
    public synchronized void sendFriendRequest(String senderId, String receiverId) {
        User sender = loadUserIfExists(senderId);
        User receiver = loadUserIfExists(receiverId);

        if (sender == null || receiver == null) {
            sendToClient(senderId, "ERROR: User not found");
            return;
        }
        if (blockedDAO.isBlocked(receiverId, senderId)) {
            sendToClient(senderId, "ERROR: Cannot send friend request (you are blocked by this user)");
            return;
        }

        Message friendRequest = messageFactory.friendRequest(senderId, receiverId);
        // Optional: store pending request via DAO

        sendToClient(receiverId, "FRIEND_REQUEST:" + senderId + ":" + sender.getUsername());
        System.out.println("Friend request: " + senderId + " -> " + receiverId);
    }

    public synchronized void acceptFriendRequest(String userId, String friendId) {
        User user = loadUserIfExists(userId);
        User friend = loadUserIfExists(friendId);

        if (user == null || friend == null) {
            sendToClient(userId, "ERROR: User not found");
            return;
        }

        // In-memory
        user.addFriend(friendId);
        friend.addFriend(userId);

        // DB (two-way)
        friendDAO.addFriendship(userId, friendId);
        friendDAO.addFriendship(friendId, userId);

        sendToClient(userId, "FRIEND_ADDED:" + friendId + ":" + friend.getUsername());
        sendToClient(friendId, "FRIEND_ADDED:" + userId + ":" + user.getUsername());

        System.out.println("Friends added: " + userId + " <-> " + friendId);
    }

    public synchronized List<String> getOnlineFriends(String userId) {
        List<String> result = new ArrayList<>();
        List<String> friendIds = friendDAO.getFriends(userId);

        for (String fid : friendIds) {
            User friend = users.get(fid);
            if (friend != null && friend.isOnline()) {
                result.add(fid + ":" + friend.getUsername());
            }
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // DIRECT MESSAGES
    // -------------------------------------------------------------------------
    public synchronized void sendDirectMessage(String senderId, String receiverId, String content) {
        User sender = loadUserIfExists(senderId);
        User receiver = loadUserIfExists(receiverId);

        if (sender == null || receiver == null) {
            sendToClient(senderId, "ERROR: User not found");
            return;
        }

        if (blockedDAO.isBlocked(receiverId, senderId) || blockedDAO.isBlocked(senderId, receiverId)) {
            sendToClient(senderId, "ERROR: Cannot send direct message (user is blocked)");
            return;
        }

        if (!friendDAO.areFriends(senderId, receiverId)) {
            sendToClient(senderId, "ERROR: Not friends with this user");
            return;
        }

        Message message = messageFactory.directMessage(senderId, receiverId, content);

        String conversationId = getConversationId(senderId, receiverId);

        // In-memory cache
        sender.addDirectMessage(conversationId, message);
        receiver.addDirectMessage(conversationId, message);

        // Persist
        dmDAO.saveMessage(
                conversationId,
                senderId,
                receiverId,
                content,
                message.getTimestamp()
        );

        // Use decorators: [time] senderName: msg
        String display = formatForDisplay(message);

        sendToClient(receiverId, "DM:" + senderId + ":" + sender.getUsername() + ":" + display);
        sendToClient(senderId, "DM_DELIVERED:" + receiverId + ":" + receiver.getUsername() + ":" + display);

        System.out.println("DM: " + senderId + " -> " + receiverId + ": " + content);
    }

    public synchronized List<Message> getConversationHistory(String userId, String friendId) {
        List<Message> result = new ArrayList<>();

        // Only DB (to avoid duplicates)
        List<Message> dbMessages = dmDAO.getMessages(userId, friendId);
        for (Message m : dbMessages) {
            if (!result.contains(m)) {
                result.add(m);
            }
        }

        result.sort(Comparator.comparing(Message::getTimestamp));
        return result;
    }

    // -------------------------------------------------------------------------
    // BLOCKING
    // -------------------------------------------------------------------------
    public synchronized void blockUser(String userId, String blockedId) {
        User user = loadUserIfExists(userId);
        User target = loadUserIfExists(blockedId);

        if (user == null || target == null) {
            sendToClient(userId, "ERROR: User not found");
            return;
        }

        user.blockUser(blockedId);
        blockedDAO.blockUser(userId, blockedId);

        sendToClient(userId, "BLOCKED:" + blockedId + ":" + target.getUsername());
    }

    public synchronized void unblockUser(String userId, String blockedId) {
        User user = loadUserIfExists(userId);
        User target = loadUserIfExists(blockedId);

        if (user == null || target == null) {
            sendToClient(userId, "ERROR: User not found");
            return;
        }

        user.unblockUser(blockedId);
        blockedDAO.unblockUser(userId, blockedId);

        sendToClient(userId, "UNBLOCKED:" + blockedId + ":" + target.getUsername());
    }

    public synchronized List<String> getBlockedUsers(String userId) {
        List<String> result = new ArrayList<>();
        List<String> blockedIds = blockedDAO.getBlockedUsers(userId);

        for (String bid : blockedIds) {
            db.model.DbUser dbUser = userDAO.getUserById(bid);
            if (dbUser != null) {
                result.add(bid + ":" + dbUser.username());
            }
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // SERVERS / CHANNELS
    // -------------------------------------------------------------------------
    public synchronized void createLocalServer(String serverId, String serverName, String ownerId) {
        if (!localServers.containsKey(serverId)) {
            serverDAO.createServer(serverId, serverName, ownerId);
            serverDAO.addMember(serverId, ownerId);

            LocalServer server = new LocalServer(serverId, serverName, ownerId);
            server.addMember(ownerId);
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
        User user = loadUserIfExists(userId);

        if (server == null) {
            sendToClient(userId, "ERROR: Server not found");
            return;
        }

        if (user == null) {
            sendToClient(userId, "ERROR: User not found");
            return;
        }

        server.addMember(userId);
        serverDAO.addMember(serverId, userId);

        sendToClient(userId, "SERVER_JOINED:" + serverId + ":" + server.getServerName());

        Message joinMessage = messageFactory.serverJoin(user.getUsername(), serverId);
        String displayJoin = formatForDisplay(joinMessage);

        broadcastToServer(
                serverId,
                "SERVER_MSG:" + serverId + ":SYSTEM:SYSTEM:" + displayJoin
        );

        System.out.println("User " + userId + " joined server: " + serverId);
    }

    public synchronized void leaveLocalServer(String userId, String serverId) {
        LocalServer server = localServers.get(serverId);
        User user = loadUserIfExists(userId);

        if (server == null || user == null) {
            return;
        }

        if (userId.equals(server.getOwnerId())) {
            sendToClient(userId, "ERROR: Server owner cannot leave");
            return;
        }

        server.removeMember(userId);
        serverDAO.removeMember(serverId, userId);
        sendToClient(userId, "SERVER_LEFT:" + serverId);

        Message leaveMessage = messageFactory.serverLeave(user.getUsername(), serverId);
        String displayLeave = formatForDisplay(leaveMessage);

        broadcastToServer(
                serverId,
                "SERVER_MSG:" + serverId + ":SYSTEM:SYSTEM:" + displayLeave
        );

        System.out.println("User " + userId + " left server: " + serverId);
    }

    public synchronized void sendServerMessage(String userId, String serverId, String content) {
        LocalServer server = localServers.get(serverId);
        User user = loadUserIfExists(userId);

        if (server == null) {
            sendToClient(userId, "ERROR: Server not found");
            return;
        }

        if (user == null) {
            sendToClient(userId, "ERROR: User not found");
            return;
        }

        if (!server.isMember(userId)) {
            sendToClient(userId, "ERROR: You are not a member of this server");
            return;
        }

        Message message = messageFactory.serverMessage(userId, serverId, content);

        server.addMessage(message);
        serverMessageDAO.saveMessage(
                serverId,
                userId,
                content,
                message.getTimestamp()
        );

        // [time] senderName: msg
        String display = formatForDisplay(message);

        broadcastToServer(
                serverId,
                "SERVER_MSG:" + serverId + ":" + userId + ":" + user.getUsername() + ":" + display
        );

        System.out.println("Server message in " + serverId + " from " + userId + ": " + content);
    }

    public synchronized List<String> listLocalServers() {
        return serverDAO.listServers();  // e.g. "id:name:memberCount"
    }

    public synchronized List<String> getServerMembers(String serverId) {
        List<String> memberIds = serverDAO.getMembers(serverId);
        List<String> result = new ArrayList<>();

        for (String mid : memberIds) {
            db.model.DbUser dbUser = userDAO.getUserById(mid);
            if (dbUser != null) {
                result.add(mid + ":" + dbUser.username());
            }
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // HELPER METHODS
    // -------------------------------------------------------------------------
    private void broadcastToServer(String serverId, String message) {
        LocalServer server = localServers.get(serverId);
        if (server == null) {
            return;
        }

        for (String memberId : server.getMembers()) {
            sendToClient(memberId, message);
        }
    }

    private void notifyFriendsOnlineStatus(String userId, boolean online) {
        String status = online ? "ONLINE" : "OFFLINE";
        List<String> friends = friendDAO.getFriends(userId);
        User user = users.get(userId);
        String username = (user != null) ? user.getUsername() : userId;

        for (String friendId : friends) {
            sendToClient(friendId, "STATUS:" + userId + ":" + username + ":" + status);
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

    private User loadUserIfExists(String userId) {
        if (userId == null) return null;

        User u = users.get(userId);
        if (u != null) return u;

        db.model.DbUser dbUser = userDAO.getUserById(userId);
        if (dbUser == null) return null;

        u = new UserBuilder()
                .setUserId(dbUser.id())
                .setUsername(dbUser.username())
                .setEmail(dbUser.email())
                .setOnline(false)
                .build();
        users.put(userId, u);
        return u;
    }

    /**
     * Decorator pipeline:
     * - If sender == "SYSTEM": [time] msg
     * - Else (DM + server msg): [time] senderName: msg
     *
     * We rewrite a "display" message whose senderId = username,
     * so decorators don't need ChatServer or DAOs.
     */
    private String formatForDisplay(Message message) {
        String senderId = message.getSenderId();
        boolean isSystem = "SYSTEM".equals(senderId);

        String senderLabel = senderId;
        if (!isSystem) {
            senderLabel = resolveUsername(senderId); // turn "u1" into "Alice"
        }

        // Build a "display" message with senderLabel instead of raw ID
        Message displayMessage = new Message(
                message.getId(),
                senderLabel,
                message.getReceiverId(),
                message.getContent(),
                message.getType(),
                message.getTimestamp()
        );

        MessageComponent component = new BaseMessage(displayMessage);

        boolean includeSender =
                !isSystem &&
                        (message.getType() == Message.MessageType.DIRECT_MESSAGE
                                || message.getType() == Message.MessageType.SERVER_MESSAGE);

        if (includeSender) {
            component = new SenderNameDecorator(component, displayMessage);
        }

        component = new TimestampDecorator(component, displayMessage);

        return component.getContent();
    }

    // Used by history / name lookup
    public String resolveUsername(String userId) {
        if (userId == null) return "UNKNOWN";
        db.model.DbUser dbUser = userDAO.getUserById(userId);
        return (dbUser != null) ? dbUser.username() : userId;
    }

    // -------------------------------------------------------------------------
    // MAIN
    // -------------------------------------------------------------------------
    public static void main(String[] args) {
        db.SchemaManager.initialize();
        ChatServer server = new ChatServer();
        server.start();
    }
}


// ============================================================================
// ClientHandler
// ============================================================================
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
            in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
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

        // Enforce auth: only REGISTER / LOGIN allowed without userId
        if (!action.equals("REGISTER") && !action.equals("LOGIN") && userId == null) {
            sendMessage("ERROR:NOT_LOGGED_IN");
            return;
        }

        switch (action) {

            // -------------------------------------------------------------
            // REGISTRATION: REGISTER:<userId>:<username>:<password>
            // -------------------------------------------------------------
            case "REGISTER":
                if (parts.length >= 4) {
                    String newUserId = parts[1];
                    String username  = parts[2];
                    String password  = parts[3];

                    if (server.userExists(newUserId)) {
                        sendMessage("REGISTER_FAILED:USER_EXISTS");
                    } else {
                        server.registerUser(newUserId, username, "", password);
                        this.userId = newUserId;
                        sendMessage("REGISTER_OK:" + newUserId + ":" + username);
                        server.connectUser(this.userId, this);
                    }
                } else {
                    sendMessage("REGISTER_FAILED:BAD_FORMAT");
                }
                break;

            // -------------------------------------------------------------
            // LOGIN: LOGIN:<userId>:<password>
            // -------------------------------------------------------------
            case "LOGIN":
                if (parts.length >= 3) {
                    String loginId  = parts[1];
                    String password = parts[2];

                    if (!server.userExists(loginId)) {
                        sendMessage("LOGIN_FAILED:NO_SUCH_USER");
                    } else if (!server.isPasswordValid(loginId, password)) {
                        sendMessage("LOGIN_FAILED:BAD_PASSWORD");
                    } else {
                        this.userId = loginId;
                        String username = server.resolveUsername(this.userId);
                        sendMessage("LOGIN_OK:" + this.userId + ":" + username);
                        server.connectUser(this.userId, this);
                    }
                } else {
                    sendMessage("LOGIN_FAILED:BAD_FORMAT");
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
                    String serverId   = parts[1];
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
                    List<Message> history = null;
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
                        String formattedTime = m.getFormattedTimestamp();
                        String senderName    = server.resolveUsername(m.getSenderId());
                        String content       = m.getContent();

                        if (payload.length() > 0) payload.append("|");

                        payload.append(formattedTime)
                                .append("~")
                                .append(senderName)
                                .append("~")
                                .append(content);
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
