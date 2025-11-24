import org.junit.jupiter.api.*;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

public class ChatApplicationIntegrationTest {

    private static ExecutorService serverExecutor;
    private static Thread serverThread;

    @BeforeAll
    static void startServer() {
        ChatServer server = new ChatServer();

        serverThread = new Thread(server::start);
        serverThread.setDaemon(true);
        serverThread.start();

        // Give server time to start
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {}
    }

    @Test
    void serverCreationIsBroadcastToOtherOnlineUsers() throws Exception {

        TestClient userA = new TestClient("srv1", "Alex");
        TestClient userB = new TestClient("srv2", "Blake");

        userA.connect();
        userB.connect();

        userA.send("CREATE_SERVER:test123:My Test Server");

        String message = userB.waitFor("NEW_SERVER:test123:My Test Server:Alex", 3000);

        assertNotNull(message);

        userA.close();
        userB.close();
    }

    @Test
    void userCanJoinAndLeaveLocalServer() throws Exception {

        TestClient user = new TestClient("join1", "Jordan");
        user.connect();

        user.send("JOIN_SERVER:general");

        String joinMsg = user.waitFor("SERVER_JOINED:general", 3000);
        assertNotNull(joinMsg);

        user.send("LEAVE_SERVER:general");

        String leaveMsg = user.waitFor("SERVER_LEFT:general", 3000);
        assertNotNull(leaveMsg);

        user.close();
    }

    @Test
    void messagesAreBroadcastToAllServerMembers() throws Exception {

        TestClient userA = new TestClient("chat1", "Maya");
        TestClient userB = new TestClient("chat2", "Noah");

        userA.connect();
        userB.connect();

        userA.send("JOIN_SERVER:general");
        userB.send("JOIN_SERVER:general");

        Thread.sleep(300);

        userA.send("SERVER_MSG:general:Hello everyone!");

        String message = userB.waitFor("SERVER_MSG:chat1:Maya:Hello everyone!", 3000);

        assertNotNull(message);

        userA.close();
        userB.close();
    }


    @Test
    void twoUsersCanSendAndReceiveDirectMessages() throws Exception {

        TestClient userA = new TestClient("u1", "Alice");
        TestClient userB = new TestClient("u2", "Bob");

        userA.connect();
        userB.connect();


        userA.send("FRIEND_REQUEST:u2");
        Thread.sleep(200);

        userB.send("ACCEPT_FRIEND:u1");
        Thread.sleep(200);


        userA.send("SEND_DM:u2:Hello Bob!");
        String messageAtUserB = userB.waitFor("DM:u1:Alice:Hello Bob!", 3000);

        assertNotNull(messageAtUserB);


        userB.send("SEND_DM:u1:Hey Alice!");
        String messageAtUserA = userA.waitFor("DM:u2:Bob:Hey Alice!", 3000);

        assertNotNull(messageAtUserA);

        userA.close();
        userB.close();
    }



    @Test
    void usersCanSendAndAcceptFriendRequests() throws Exception {

        TestClient userA = new TestClient("u3", "Carol");
        TestClient userB = new TestClient("u4", "Dave");

        userA.connect();
        userB.connect();


        userA.send("FRIEND_REQUEST:u4");

        String request = userB.waitFor("FRIEND_REQUEST:u3:Carol", 3000);
        assertNotNull(request);


        userB.send("ACCEPT_FRIEND:u3");

        String confirmationA = userA.waitFor("FRIEND_ADDED:u4:Dave", 3000);
        String confirmationB = userB.waitFor("FRIEND_ADDED:u3:Carol", 3000);

        assertNotNull(confirmationA);
        assertNotNull(confirmationB);

        userA.close();
        userB.close();
    }

    @Test
    void onlineFriendsListUpdatesWhenFriendComesOnline() throws Exception {

        TestClient userA = new TestClient("friend1", "Evan");
        TestClient userB = new TestClient("friend2", "Zoe");

        userA.connect();
        userB.connect();

        userA.send("FRIEND_REQUEST:friend2");
        Thread.sleep(200);

        userB.send("ACCEPT_FRIEND:friend1");
        Thread.sleep(200);

        userA.send("GET_FRIENDS");

        String friendsList = userA.waitFor("FRIENDS:", 3000);

        assertNotNull(friendsList);
        assertTrue(friendsList.contains("friend2:Zoe"));

        userA.close();
        userB.close();
    }


    static class TestClient {

        private final String userId;
        private final String username;

        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        private final BlockingQueue<String> receivedMessages = new LinkedBlockingQueue<>();

        public TestClient(String userId, String username) {
            this.userId = userId;
            this.username = username;
        }

        public void connect() throws Exception {
            socket = new Socket("localhost", 8888);

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            send("REGISTER:" + userId + ":" + username + ":test@mail");

            startReaderThread();
        }

        private void startReaderThread() {
            new Thread(() -> {
                try {
                    String line;
                    while ((line = in.readLine()) != null) {
                        receivedMessages.offer(line);
                    }
                } catch (IOException ignored) {}
            }).start();
        }

        public void send(String command) {
            out.println(command);
        }

        public String waitFor(String expectedPrefix, long timeoutMillis) throws InterruptedException {

            long endTime = System.currentTimeMillis() + timeoutMillis;

            while (System.currentTimeMillis() < endTime) {
                String msg = receivedMessages.poll(200, TimeUnit.MILLISECONDS);

                if (msg != null && msg.startsWith(expectedPrefix)) {
                    return msg;
                }
            }
            return null;
        }

        public void close() throws IOException {
            socket.close();
        }
    }
}
