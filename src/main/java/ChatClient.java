// ========================================
// FILE 6: ChatClient.java
// ========================================
import java.io.*;
import java.net.*;
import java.util.*;

public class ChatClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8888;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String userId;
    private String username;
    private boolean running;
    private String currentServer; // Track which server user is currently in

    public ChatClient(String userId, String username) {
        this.userId = userId;
        this.username = username;
        this.running = true;
        this.currentServer = null;
    }

    public void connect() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Register with server
            sendCommand("REGISTER:" + userId + ":" + username + ":user@example.com");

            // Start listening thread
            Thread listenerThread = new Thread(this::listenForMessages);
            listenerThread.start();

            System.out.println("Connected to chat server as " + username);

        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
        }
    }

    private void listenForMessages() {
        try {
            String message;
            while (running && (message = in.readLine()) != null) {
                handleServerMessage(message);
            }
        } catch (IOException e) {
            if (running) {
                System.err.println("Connection lost: " + e.getMessage());
            }
        }
    }

    private void handleServerMessage(String message) {
        String[] parts = message.split(":", 2); // Split into type and data
        String type = parts[0];

        switch (type) {
            case "REGISTERED":
                if (parts.length >= 2) {
                    System.out.println("Successfully registered with ID: " + parts[1]);
                }
                break;

            case "FRIEND_REQUEST":
                String[] frParts = parts.length >= 2 ? parts[1].split(":", 2) : new String[0];
                if (frParts.length >= 2) {
                    System.out.println("\n[FRIEND REQUEST] " + frParts[1] + " (ID: " + frParts[0] + ") wants to be your friend!");
                    System.out.println("To accept, type: accept " + frParts[0]);
                }
                break;

            case "FRIEND_ADDED":
                String[] faParts = parts.length >= 2 ? parts[1].split(":", 2) : new String[0];
                if (faParts.length >= 2) {
                    System.out.println("\n[FRIEND ADDED] You are now friends with " + faParts[1] + " (ID: " + faParts[0] + ")");
                    // Automatically show updated friends list
                    getOnlineFriends();
                }
                break;

            case "DM":
                String[] dmParts = parts.length >= 2 ? parts[1].split(":", 3) : new String[0];
                if (dmParts.length >= 3) {
                    String senderId = dmParts[0];
                    String senderName = dmParts[1];
                    String content = dmParts[2];
                    System.out.println("\n[DM from " + senderName + "]: " + content);
                }
                break;

            case "STATUS":
                String[] stParts = parts.length >= 2 ? parts[1].split(":", 3) : new String[0];
                if (stParts.length >= 3) {
                    String friendId = stParts[0];
                    String friendName = stParts[1];
                    String status = stParts[2];
                    System.out.println("\n[STATUS] " + friendName + " is now " + status);
                    // Automatically show updated friends list when a friend comes online
                    if (status.equals("ONLINE")) {
                        getOnlineFriends();
                    }
                }
                break;

            case "FRIENDS":
                System.out.println("\n=== Online Friends ===");
                if (parts.length >= 2 && !parts[1].isEmpty()) {
                    String[] friends = parts[1].split(",");
                    for (String friend : friends) {
                        String[] friendParts = friend.split(":");
                        if (friendParts.length >= 2) {
                            System.out.println("  - " + friendParts[1] + " (ID: " + friendParts[0] + ")");
                        }
                    }
                    System.out.println("Total: " + friends.length + " online friend(s)");
                } else {
                    System.out.println("No friends are currently online.");
                    System.out.println("(Friends must be connected to appear here)");
                }
                break;

            case "ERROR":
                if (parts.length >= 2) {
                    System.err.println("\n[ERROR] " + parts[1]);
                }
                break;

            case "SERVER_CREATED":
                String[] scParts = parts.length >= 2 ? parts[1].split(":", 2) : new String[0];
                if (scParts.length >= 2) {
                    System.out.println("\n[SERVER CREATED] Server '" + scParts[1] + "' (ID: " + scParts[0] + ") created successfully!");
                    currentServer = scParts[0];
                }
                break;

            case "SERVER_JOINED":
                String[] sjParts = parts.length >= 2 ? parts[1].split(":", 2) : new String[0];
                if (sjParts.length >= 2) {
                    System.out.println("\n[JOINED SERVER] You joined '" + sjParts[1] + "' (ID: " + sjParts[0] + ")");
                    currentServer = sjParts[0];
                    if (sjParts[0].equals("general")) {
                        System.out.println("You can now chat with everyone! Type 'say <message>' to send a message.");
                    }
                }
                break;

            case "SERVER_LEFT":
                if (parts.length >= 2) {
                    System.out.println("\n[LEFT SERVER] You left the server (ID: " + parts[1] + ")");
                    if (parts[1].equals(currentServer)) {
                        currentServer = null;
                    }
                }
                break;

            case "SERVER_MSG":
                String[] smParts = parts.length >= 2 ? parts[1].split(":", 3) : new String[0];
                if (smParts.length >= 3) {
                    String senderId = smParts[0];
                    String senderName = smParts[1];
                    String content = smParts[2];
                    if (senderId.equals("SYSTEM")) {
                        System.out.println("\n[SERVER] " + content);
                    } else {
                        System.out.println("\n[" + senderName + "]: " + content);
                    }
                }
                break;

            case "SERVERS":
                System.out.println("\n=== Available Servers ===");
                if (parts.length >= 2 && !parts[1].isEmpty()) {
                    String[] servers = parts[1].split(",");
                    for (String server : servers) {
                        String[] serverParts = server.split(":");
                        if (serverParts.length >= 3) {
                            System.out.println("  - " + serverParts[1] + " (ID: " + serverParts[0] + ", Members: " + serverParts[2] + ")");
                        }
                    }
                } else {
                    System.out.println("No servers available.");
                }
                break;

            case "MEMBERS":
                System.out.println("\n=== Server Members ===");
                String[] memParts = parts.length >= 2 ? parts[1].split(":", 2) : new String[0];
                if (memParts.length >= 2) {
                    if (!memParts[1].isEmpty()) {
                        String[] members = memParts[1].split(",");
                        for (String member : members) {
                            String[] memberParts = member.split(":");
                            if (memberParts.length >= 2) {
                                System.out.println("  - " + memberParts[1] + " (ID: " + memberParts[0] + ")");
                            }
                        }
                    } else {
                        System.out.println("No members in this server.");
                    }
                } else {
                    System.out.println("No member data received.");
                }
                break;

            case "NEW_SERVER":
                String[] nsParts = parts.length >= 2 ? parts[1].split(":", 3) : new String[0];
                if (nsParts.length >= 3) {
                    String newServerId = nsParts[0];
                    String newServerName = nsParts[1];
                    String creatorName = nsParts[2];
                    System.out.println("\n[NEW SERVER] " + creatorName + " created '" + newServerName + "' (ID: " + newServerId + ")");
                    System.out.println("Type 'join " + newServerId + "' to join this server!");
                }
                break;

            default:
                System.out.println("[SERVER] " + message);
        }

        System.out.print("> ");
    }

    public void sendFriendRequest(String friendId) {
        sendCommand("FRIEND_REQUEST:" + friendId);
        System.out.println("Friend request sent to user: " + friendId);
    }

    public void acceptFriendRequest(String friendId) {
        sendCommand("ACCEPT_FRIEND:" + friendId);
        System.out.println("Accepting friend request from: " + friendId);
    }

    public void sendDirectMessage(String receiverId, String content) {
        sendCommand("SEND_DM:" + receiverId + ":" + content);
    }

    public void getOnlineFriends() {
        sendCommand("GET_FRIENDS");
    }

    public void createLocalServer(String serverId, String serverName) {
        sendCommand("CREATE_SERVER:" + serverId + ":" + serverName);
        System.out.println("Creating server: " + serverName);
    }

    public void joinLocalServer(String serverId) {
        sendCommand("JOIN_SERVER:" + serverId);
        System.out.println("Joining server: " + serverId);
    }

    public void leaveLocalServer(String serverId) {
        sendCommand("LEAVE_SERVER:" + serverId);
        System.out.println("Leaving server: " + serverId);
    }

    public void sendServerMessage(String serverId, String content) {
        sendCommand("SERVER_MSG:" + serverId + ":" + content);
    }

    public void listLocalServers() {
        sendCommand("LIST_SERVERS");
    }

    public void getServerMembers(String serverId) {
        sendCommand("SERVER_MEMBERS:" + serverId);
    }

    private void sendCommand(String command) {
        if (out != null) {
            out.println(command);
        }
    }

    public void startCLI() {
        Scanner scanner = new Scanner(System.in);

        System.out.println("\n=== Chat Client Commands ===");
        System.out.println("add <userId>        - Send friend request");
        System.out.println("accept <userId>     - Accept friend request");
        System.out.println("dm <userId> <msg>   - Send direct message");
        System.out.println("friends             - List online friends");
        System.out.println("\n=== Server Commands ===");
        System.out.println("create <serverId> <n> - Create a server");
        System.out.println("servers             - List all servers");
        System.out.println("join <serverId>     - Join a server");
        System.out.println("leave <serverId>    - Leave a server");
        System.out.println("members <serverId>  - List server members");
        System.out.println("say <message>       - Send message to current server");
        System.out.println("quit                - Exit");
        System.out.println("============================\n");

        while (running) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) continue;

            String[] parts = input.split("\\s+", 3);
            String command = parts[0].toLowerCase();

            switch (command) {
                case "add":
                    if (parts.length >= 2) {
                        sendFriendRequest(parts[1]);
                    } else {
                        System.out.println("Usage: add <userId>");
                    }
                    break;

                case "accept":
                    if (parts.length >= 2) {
                        acceptFriendRequest(parts[1]);
                    } else {
                        System.out.println("Usage: accept <userId>");
                    }
                    break;

                case "dm":
                    if (parts.length >= 3) {
                        sendDirectMessage(parts[1], parts[2]);
                    } else {
                        System.out.println("Usage: dm <userId> <message>");
                    }
                    break;

                case "friends":
                    getOnlineFriends();
                    break;

                case "create":
                    if (parts.length >= 3) {
                        String serverId = parts[1];
                        String serverName = input.substring(input.indexOf(parts[1]) + parts[1].length()).trim();
                        createLocalServer(serverId, serverName);
                    } else {
                        System.out.println("Usage: create <serverId> <serverName>");
                    }
                    break;

                case "servers":
                    listLocalServers();
                    break;

                case "join":
                    if (parts.length >= 2) {
                        joinLocalServer(parts[1]);
                    } else {
                        System.out.println("Usage: join <serverId>");
                    }
                    break;

                case "leave":
                    if (parts.length >= 2) {
                        leaveLocalServer(parts[1]);
                    } else {
                        System.out.println("Usage: leave <serverId>");
                    }
                    break;

                case "members":
                    if (parts.length >= 2) {
                        getServerMembers(parts[1]);
                    } else {
                        System.out.println("Usage: members <serverId>");
                    }
                    break;

                case "say":
                    if (currentServer != null && parts.length >= 2) {
                        String message = input.substring(4).trim();
                        sendServerMessage(currentServer, message);
                    } else if (currentServer == null) {
                        System.out.println("You are not in any server. Join a server first.");
                    } else {
                        System.out.println("Usage: say <message>");
                    }
                    break;

                case "quit":
                case "exit":
                    disconnect();
                    return;

                default:
                    System.out.println("Unknown command: " + command);
            }
        }

        scanner.close();
    }

    public void disconnect() {
        running = false;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
            System.out.println("Disconnected from server.");
        } catch (IOException e) {
            System.err.println("Error disconnecting: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java ChatClient <userId> <username>");
            System.out.println("Example: java ChatClient user1 Alice");
            return;
        }

        String userId = args[0];
        String username = args[1];

        ChatClient client = new ChatClient(userId, username);
        client.connect();
        client.startCLI();
    }
}