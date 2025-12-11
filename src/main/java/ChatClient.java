import java.io.*;
import java.net.*;
import java.util.*;

public class ChatClient {
    private static final int SERVER_PORT = 8888;

    private final String serverHost;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String userId;
    private String username;
    private boolean running;
    private String currentServer;
    private Thread listenerThread;
    private String lastPrintedServer = null;

    // NEW: authentication flag
    private boolean authenticated = false;

    // ANSI color codes for nicer CLI output
    private static final String RESET  = "\u001B[0m";
    private static final String RED    = "\u001B[31m";
    private static final String GREEN  = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN   = "\u001B[36m";

    public ChatClient(String userId, String username) {
        this(userId, username, "localhost");
    }

    public ChatClient(String userId, String username, String serverHost) {
        this.userId = userId;
        this.username = username;
        this.running = true;
        this.currentServer = null;
        this.serverHost = serverHost;
    }

    public void connect() {
        try {
            socket = new Socket(serverHost, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // *** NO AUTO-REGISTER HERE ANYMORE ***
            System.out.println(GREEN + "Connected to chat server " + serverHost + ":" + SERVER_PORT + RESET);
            System.out.println(YELLOW + "You must login or register before using chat commands." + RESET);
            System.out.println("Use:");
            System.out.println("  login <password>");
            System.out.println("  register <password>");

            // Listener thread
            listenerThread = new Thread(this::listenForMessages);
            listenerThread.setDaemon(true);
            listenerThread.start();

        } catch (IOException e) {
            System.err.println(RED + "Connection error: " + e.getMessage() + RESET);
        }
    }

    private void printPrompt() {
        String serverLabel = (currentServer != null) ? currentServer : "no-server";
        if (!authenticated) {
            System.out.print(username + "@auth> ");
        } else {
            System.out.print(username + "@" + serverLabel + "> ");
        }
    }

    private void clearCurrentLine() {
        System.out.print("\u001B[1A");
        System.out.print("\r\u001B[2K");
    }

    private void printHelp() {
        System.out.println("\n========= USER COMMANDS =========");
        System.out.println("add <userId>          - Send friend request");
        System.out.println("accept <userId>       - Accept friend request");
        System.out.println("dm <userId> <msg>     - Send direct message");
        System.out.println("friends               - List online friends");
        System.out.println("block <userId>        - Block a user");
        System.out.println("unblock <userId>      - Unblock a user");
        System.out.println("blocked               - Show blocked users");
        System.out.println("history <userId>      - Show DM history");
        System.out.println("\n========= SERVER COMMANDS =======");
        System.out.println("create <serverId> <name> - Create a server");
        System.out.println("servers                 - List all servers");
        System.out.println("join <serverId>         - Join a server");
        System.out.println("leave <serverId>        - Leave a server");
        System.out.println("members <serverId>      - List server members");
        System.out.println("say <message>           - Send message to current server");
        System.out.println("quit / exit             - Exit");
        System.out.println("help                    - Show this help");
        System.out.println("=================================\n");
    }

    private void printAuthHelp() {
        System.out.println("\n========= AUTH COMMANDS =========");
        System.out.println("login <password>       - Log into existing account for this userId");
        System.out.println("register <password>    - Create a new account for this userId & username");
        System.out.println("help                   - Show this auth help");
        System.out.println("=================================\n");
    }

    private void listenForMessages() {
        try {
            String message;
            while (running && (message = in.readLine()) != null) {
                handleServerMessage(message);
            }
        } catch (IOException e) {
            if (running) {
                System.err.println(RED + "Connection lost: " + e.getMessage() + RESET);
            }
        }
    }

    private void handleServerMessage(String message) {
        String[] parts = message.split(":", 2); // Split into type and data
        String type = parts[0];

        // ---------- AUTH RESPONSES FIRST ----------
        if ("REGISTER_OK".equals(type)) {
            // Format: REGISTER_OK:<userId>:<username>
            String[] p = message.split(":");
            if (p.length >= 3) {
                this.userId = p[1];
                this.username = p[2];
            }
            authenticated = true;
            System.out.println(GREEN + "\n[AUTH] Registration successful! Logged in as "
                    + username + " (ID: " + userId + ")" + RESET);
            printHelp();
            printPrompt();
            return;
        }

        if ("REGISTER_FAILED".equals(type)) {
            String reason = (parts.length >= 2) ? parts[1] : "Unknown reason";
            System.out.println(RED + "\n[AUTH] Registration failed: " + reason + RESET);
            printAuthHelp();
            printPrompt();
            return;
        }

        if ("LOGIN_OK".equals(type)) {
            // Format: LOGIN_OK:<userId>:<username>
            String[] p = message.split(":");
            if (p.length >= 3) {
                this.userId = p[1];
                this.username = p[2];
            }
            authenticated = true;
            System.out.println(GREEN + "\n[AUTH] Login successful! Welcome " + username
                    + " (ID: " + userId + ")" + RESET);
            printHelp();
            printPrompt();
            return;
        }

        if ("LOGIN_FAILED".equals(type)) {
            String reason = (parts.length >= 2) ? parts[1] : "Unknown reason";
            System.out.println(RED + "\n[AUTH] Login failed: " + reason + RESET);
            printAuthHelp();
            printPrompt();
            return;
        }

        // If not authenticated yet, ignore other traffic (except maybe generic ERROR)
        if (!authenticated && !"ERROR".equals(type)) {
            System.out.println(YELLOW + "\n[AUTH] You must login or register first." + RESET);
            printAuthHelp();
            printPrompt();
            return;
        }

        // ---------- NORMAL CHAT MESSAGES ----------
        switch (type) {
            case "REGISTERED":
                if (parts.length >= 2) {
                    System.out.println("Successfully registered with ID: " + parts[1]);
                }
                break;

            case "FRIEND_REQUEST": {
                String[] frParts = parts.length >= 2 ? parts[1].split(":", 2) : new String[0];
                if (frParts.length >= 2) {
                    System.out.println("\n[FRIEND REQUEST] " + frParts[1] + " (ID: " + frParts[0] + ") wants to be your friend!");
                    System.out.println("To accept, type: accept " + frParts[0]);
                }
                break;
            }

            case "FRIEND_ADDED": {
                String[] faParts = parts.length >= 2 ? parts[1].split(":", 2) : new String[0];
                if (faParts.length >= 2) {
                    System.out.println("\n[FRIEND ADDED] You are now friends with " + faParts[1] + " (ID: " + faParts[0] + ")");
                    // Automatically show updated friends list
                    getOnlineFriends();
                }
                break;
            }

            case "DM": {
                // Format: DM:<senderId>:<senderName>:<displayString>
                String[] dmParts = parts.length >= 2 ? parts[1].split(":", 3) : new String[0];
                if (dmParts.length >= 3) {
                    String display = dmParts[2]; // already formatted by decorators
                    System.out.println("\n" + GREEN + "[DM] " + display + RESET);
                }
                break;
            }

            case "STATUS": {
                String[] stParts = parts.length >= 2 ? parts[1].split(":", 3) : new String[0];
                if (stParts.length >= 3) {
                    String friendName = stParts[1];
                    String status = stParts[2];
                    System.out.println("\n" + CYAN + "[STATUS] " + friendName + " is now " + status + RESET);
                    if ("ONLINE".equals(status)) {
                        getOnlineFriends();
                    }
                }
                break;
            }

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
                    System.err.println("\n" + RED + "[ERROR] " + parts[1] + RESET);
                }
                break;

            case "SERVER_CREATED": {
                String[] scParts = parts.length >= 2 ? parts[1].split(":", 2) : new String[0];
                if (scParts.length >= 2) {
                    System.out.println("\n[SERVER CREATED] Server '" + scParts[1] + "' (ID: " + scParts[0] + ") created successfully!");
                    currentServer = scParts[0];
                }
                break;
            }

            case "SERVER_JOINED": {
                String[] sjParts = parts.length >= 2 ? parts[1].split(":", 2) : new String[0];
                if (sjParts.length >= 2) {
                    System.out.println("\n[JOINED SERVER] You joined '" + sjParts[1] + "' (ID: " + sjParts[0] + ")");
                    currentServer = sjParts[0];
                    if (sjParts[0].equals("general")) {
                        System.out.println("You can now chat with everyone! Type 'say <message>' to send a message.");
                    }
                }
                break;
            }

            case "SERVER_LEFT":
                if (parts.length >= 2) {
                    String leftServer = parts[1];
                    System.out.println("\n[LEFT SERVER] You left server: " + leftServer);

                    if (leftServer.equals(currentServer)) {
                        currentServer = "general";
                        System.out.println("[AUTO-JOIN] Switched back to general server.");
                        joinLocalServer("general");
                    }
                }
                break;

            case "SERVER_MSG": {
                // Format from server:
                // SERVER_MSG:<serverId>:<senderId>:<senderName>:<displayString>
                String[] smParts = parts.length >= 2 ? parts[1].split(":", 4) : new String[0];

                if (smParts.length >= 4) {
                    String serverId = smParts[0];
                    String senderId = smParts[1];
                    String senderName = smParts[2]; // unused for formatting now
                    String display = smParts[3];    // already decorated string

                    if (!serverId.equals(lastPrintedServer)) {
                        System.out.println("\n====== [" + serverId.toUpperCase() + "] ======");
                        lastPrintedServer = serverId;
                    }

                    if (senderId.equals("SYSTEM")) {
                        if (serverId.equals(currentServer)) {
                            System.out.println(CYAN + "[* " + serverId + " | SERVER] " + display + RESET);
                        } else {
                            System.out.println(CYAN + "[" + serverId + " | SERVER] " + display + RESET);
                        }
                    } else {
                        if (serverId.equals(currentServer)) {
                            System.out.println(CYAN + "[* " + serverId + "] " + display + RESET);
                        } else {
                            System.out.println(CYAN + "[" + serverId + "] " + display + RESET);
                        }
                    }
                }
                break;
            }

            case "SERVERS":
                System.out.println("\n=== Available Servers ===");
                if (parts.length >= 2 && !parts[1].isEmpty()) {
                    String[] servers = parts[1].split(",");
                    for (String server : servers) {
                        String[] serverParts = server.split(":");
                        if (serverParts.length >= 2) {
                            // DB version only returns id:name, so handle 2 parts
                            if (serverParts.length == 2) {
                                System.out.println("  - " + serverParts[1] + " (ID: " + serverParts[0] + ")");
                            } else if (serverParts.length >= 3) {
                                System.out.println("  - " + serverParts[1] + " (ID: " + serverParts[0] + ", Members: " + serverParts[2] + ")");
                            }
                        }
                    }
                } else {
                    System.out.println("No servers available.");
                }
                break;

            case "MEMBERS": {
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
            }

            case "NEW_SERVER": {
                String[] nsParts = parts.length >= 2 ? parts[1].split(":", 3) : new String[0];
                if (nsParts.length >= 3) {
                    String newServerId = nsParts[0];
                    String newServerName = nsParts[1];
                    String creatorName = nsParts[2];
                    System.out.println("\n[NEW SERVER] " + creatorName + " created '" + newServerName + "' (ID: " + newServerId + ")");
                    System.out.println("Type 'join " + newServerId + "' to join this server!");
                }
                break;
            }

            case "DM_DELIVERED": {
                // Format: DM_DELIVERED:<receiverId>:<receiverName>:<displayString>
                String[] ddParts = parts.length >= 2 ? parts[1].split(":", 3) : new String[0];
                if (ddParts.length >= 3) {
                    String receiverName = ddParts[1];
                    String display = ddParts[2]; // already decorated
                    System.out.println("\n" + GREEN + "[DELIVERED] To " + receiverName + ": " + display + RESET);
                }
                break;
            }

            case "BLOCKED": {
                String[] bParts = parts.length >= 2 ? parts[1].split(":", 2) : new String[0];
                if (bParts.length >= 2) {
                    System.out.println("\n[BLOCKED] You blocked " + bParts[1] + " (ID: " + bParts[0] + ")");
                }
                break;
            }

            case "UNBLOCKED": {
                String[] ubParts = parts.length >= 2 ? parts[1].split(":", 2) : new String[0];
                if (ubParts.length >= 2) {
                    System.out.println("\n[UNBLOCKED] You unblocked " + ubParts[1] + " (ID: " + ubParts[0] + ")");
                }
                break;
            }

            case "BLOCKED_LIST":
                System.out.println("\n=== Blocked Users ===");
                if (parts.length >= 2 && !parts[1].isEmpty()) {
                    String[] blockedUsers = parts[1].split(",");
                    for (String blocked : blockedUsers) {
                        String[] buParts = blocked.split(":");
                        if (buParts.length >= 2) {
                            System.out.println("  - " + buParts[1] + " (ID: " + buParts[0] + ")");
                        }
                    }
                } else {
                    System.out.println("You have not blocked any users.");
                }
                break;

            case "HISTORY": {
                String[] hParts = parts.length >= 2 ? parts[1].split(":", 2) : new String[0];
                if (hParts.length >= 1) {
                    String friendId = hParts[0];
                    System.out.println("\n=== DM History with " + friendId + " ===");
                    if (hParts.length == 2 && !hParts[1].isEmpty()) {
                        String[] entries = hParts[1].split("\\|");
                        for (String entry : entries) {
                            String[] eParts = entry.split("~", 3);
                            if (eParts.length == 3) {
                                String timestamp = eParts[0];
                                String sender = eParts[1];
                                String content = eParts[2];
                                System.out.println("[" + timestamp + "] " + sender + ": " + content);
                            }
                        }
                    } else {
                        System.out.println("No previous messages.");
                    }
                }
                break;
            }

            default:
                System.out.println("[SERVER] " + message);
        }

        printPrompt();
    }

    // ----------------- COMMAND WRAPPERS -----------------

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

    public void blockUser(String targetUserId) {
        sendCommand("BLOCK_USER:" + targetUserId);
        System.out.println("Blocking user: " + targetUserId);
    }

    public void unblockUser(String targetUserId) {
        sendCommand("UNBLOCK_USER:" + targetUserId);
        System.out.println("Unblocking user: " + targetUserId);
    }

    public void getBlockedUsers() {
        sendCommand("GET_BLOCKED");
    }

    public void getDirectMessageHistory(String friendId) {
        sendCommand("GET_HISTORY:" + friendId);
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
        if (currentServer == null) {
            System.out.println("You are not in any server. Switching you back to general...");
            joinLocalServer("general");
            currentServer = "general";
        }
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

    // ----------------- CLI LOOP -----------------

    public void startCLI() {
        Scanner scanner = new Scanner(System.in);

        printAuthHelp();

        while (running) {
            printPrompt();
            String rawInput = scanner.nextLine();
            clearCurrentLine();

            String input = rawInput.trim();
            if (input.isEmpty()) continue;

            String[] parts = input.split("\\s+", 3);
            String command = parts[0].toLowerCase();

            // ---------- AUTH PHASE ----------
            if (!authenticated) {
                switch (command) {
                    case "register":
                        if (parts.length >= 2) {
                            String password = parts[1];
                            sendCommand("REGISTER:" + userId + ":" + username + ":" + password);
                            System.out.println("Attempting registration for " + username + " (ID: " + userId + ")...");
                        } else {
                            System.out.println("Usage: register <password>");
                        }
                        continue;

                    case "login":
                        if (parts.length >= 2) {
                            String password = parts[1];
                            sendCommand("LOGIN:" + userId + ":" + password);
                            System.out.println("Attempting login for " + username + " (ID: " + userId + ")...");
                        } else {
                            System.out.println("Usage: login <password>");
                        }
                        continue;

                    case "help":
                        printAuthHelp();
                        continue;

                    case "quit":
                    case "exit":
                        disconnect();
                        System.out.println("Exiting client...");
                        System.exit(0);
                        return;

                    default:
                        System.out.println("You must login or register first.");
                        System.out.println("Use: login <password>  or  register <password>");
                        continue;
                }
            }

            // ---------- NORMAL COMMANDS AFTER AUTH ----------
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
                    if (parts.length >= 2) {
                        String msg = input.substring(4).trim();
                        sendServerMessage(currentServer, msg);
                    } else {
                        System.out.println("Usage: say <message>");
                    }
                    break;

                case "block":
                    if (parts.length >= 2) {
                        blockUser(parts[1]);
                    } else {
                        System.out.println("Usage: block <userId>");
                    }
                    break;

                case "unblock":
                    if (parts.length >= 2) {
                        unblockUser(parts[1]);
                    } else {
                        System.out.println("Usage: unblock <userId>");
                    }
                    break;

                case "blocked":
                    getBlockedUsers();
                    break;

                case "history":
                    if (parts.length >= 2) {
                        getDirectMessageHistory(parts[1]);
                    } else {
                        System.out.println("Usage: history <userId>");
                    }
                    break;

                case "help":
                    printHelp();
                    break;

                case "quit":
                case "exit":
                    disconnect();
                    System.out.println("Exiting client...");
                    System.exit(0);
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
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            System.out.println("Disconnected from server.");
        } catch (IOException e) {
            System.err.println("Error disconnecting: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java ChatClient <userId> <username> [serverHost]");
            System.out.println("Example (local): java ChatClient u1 Alice");
            System.out.println("Example (remote): java ChatClient u1 Alice 192.168.1.105");
            return;
        }

        String userId = args[0];
        String username = args[1];
        String host = args.length >= 3 ? args[2] : "localhost";

        ChatClient client = new ChatClient(userId, username, host);
        client.connect();
        client.startCLI();
    }
}
