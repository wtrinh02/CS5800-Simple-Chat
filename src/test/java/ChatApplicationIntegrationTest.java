import db.SchemaManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChatApplicationIntegrationTest {

    private static Thread serverThread;

    @BeforeAll
    static void startServerOnce() throws Exception {

        java.nio.file.Files.deleteIfExists(java.nio.file.Paths.get("chat.db"));

        SchemaManager.initialize();

        ChatServer server = new ChatServer();
        serverThread = new Thread(server::start);
        serverThread.setDaemon(true);
        serverThread.start();

        Thread.sleep(1000);
    }
    @AfterAll
    static void stopServer() throws Exception {
        try {
            new Socket("localhost", 8888).close(); // wakes server blocking accept()
        } catch (Exception ignored) {}

        serverThread.interrupt();
    }

    @Test
    void sendingCommandWithoutLoginShouldReturnNotLoggedInError() throws Exception {
        RawClientWithoutAuth client = new RawClientWithoutAuth();
        client.connect();
        client.send("SEND_DM:anyone:hello");
        String error = client.waitForPrefix("ERROR:NOT_LOGGED_IN", 3000);
        boolean receivedError = error != null && error.startsWith("ERROR:NOT_LOGGED_IN");
        assertEquals(true, receivedError);
        client.close();
    }

    @Test
    void registerCommandShouldReturnRegisterOkForNewUser() throws Exception {
        RawClientWithoutAuth client = new RawClientWithoutAuth();
        client.connect();
        client.send("REGISTER:intUser1:IntUserOne:intPass");
        String response = client.waitForPrefix("REGISTER_OK:intUser1:IntUserOne", 3000);
        boolean receivedRegisterOk = response != null && response.startsWith("REGISTER_OK:intUser1:IntUserOne");
        assertEquals(true, receivedRegisterOk);
        client.close();
    }

    @Test
    void loginForNonexistentUserShouldReturnNoSuchUserError() throws Exception {
        RawClientWithoutAuth client = new RawClientWithoutAuth();
        client.connect();
        client.send("LOGIN:doesNotExist:somePass");
        String response = client.waitForPrefix("LOGIN_FAILED:NO_SUCH_USER", 3000);
        boolean receivedNoSuchUser = response != null && response.startsWith("LOGIN_FAILED:NO_SUCH_USER");
        assertEquals(true, receivedNoSuchUser);
        client.close();
    }

    @Test
    void friendRequestAndAcceptShouldResultInFriendAddedMessagesOnBothSides() throws Exception {
        TestClient alice = new TestClient("friendA1", "FriendAlice", "passA");
        TestClient bob = new TestClient("friendB1", "FriendBob", "passB");

        alice.connectAndRegister();
        bob.connectAndRegister();

        alice.send("FRIEND_REQUEST:friendB1");
        String requestAtBob = bob.waitForPrefix("FRIEND_REQUEST:friendA1:FriendAlice", 3000);

        bob.send("ACCEPT_FRIEND:friendA1");
        String addedAtAlice = alice.waitForPrefix("FRIEND_ADDED:friendB1:FriendBob", 3000);
        String addedAtBob = bob.waitForPrefix("FRIEND_ADDED:friendA1:FriendAlice", 3000);

        boolean bothSidesReceivedFriendAdded = requestAtBob != null
                && addedAtAlice != null
                && addedAtBob != null
                && addedAtAlice.startsWith("FRIEND_ADDED:friendB1:FriendBob")
                && addedAtBob.startsWith("FRIEND_ADDED:friendA1:FriendAlice");

        assertEquals(true, bothSidesReceivedFriendAdded);

        alice.close();
        bob.close();
    }

    @Test
    void directMessageAfterFriendshipShouldDeliverDecoratedDmAndDmDelivered() throws Exception {
        TestClient alice = new TestClient("dmA1", "DmAlice", "passA");
        TestClient bob = new TestClient("dmB1", "DmBob", "passB");

        alice.connectAndRegister();
        bob.connectAndRegister();

        alice.send("FRIEND_REQUEST:dmB1");
        bob.waitForPrefix("FRIEND_REQUEST:dmA1:DmAlice", 3000);
        bob.send("ACCEPT_FRIEND:dmA1");
        alice.waitForPrefix("FRIEND_ADDED:dmB1:DmBob", 3000);
        bob.waitForPrefix("FRIEND_ADDED:dmA1:DmAlice", 3000);

        alice.send("SEND_DM:dmB1:hello dm advanced");
        String dmAtBob = bob.waitForPrefix("DM:dmA1:DmAlice:", 3000);
        String deliveredAtAlice = alice.waitForPrefix("DM_DELIVERED:dmB1:DmBob:", 3000);

        boolean dmWasDeliveredWithExpectedPrefixes = dmAtBob != null
                && deliveredAtAlice != null
                && dmAtBob.startsWith("DM:dmA1:DmAlice:")
                && deliveredAtAlice.startsWith("DM_DELIVERED:dmB1:DmBob:");

        assertEquals(true, dmWasDeliveredWithExpectedPrefixes);

        alice.close();
        bob.close();
    }

    static class TestClient {

        private final String userId;
        private final String username;
        private final String password;

        private Socket socket;
        private BufferedReader reader;
        private PrintWriter writer;
        private final BlockingQueue<String> inbox = new LinkedBlockingQueue<>();

        TestClient(String userId, String username, String password) {
            this.userId = userId;
            this.username = username;
            this.password = password;
        }

        void connectAndRegister() throws Exception {
            socket = new Socket("localhost", 8888);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            startReaderThread();
            send("REGISTER:" + userId + ":" + username + ":" + password);
            waitForPrefix("REGISTER_OK:" + userId + ":" + username, 3000);
        }

        void send(String command) {
            writer.println(command);
        }

        String waitForPrefix(String prefix, long timeoutMillis) throws InterruptedException {
            long deadline = System.currentTimeMillis() + timeoutMillis;
            while (System.currentTimeMillis() < deadline) {
                String msg = inbox.poll(200, TimeUnit.MILLISECONDS);
                if (msg != null && msg.startsWith(prefix)) {
                    return msg;
                }
            }
            return null;
        }

        void close() throws IOException {
            socket.close();
        }

        private void startReaderThread() {
            Thread t = new Thread(() -> {
                try {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        inbox.offer(line);
                    }
                } catch (IOException ignored) {
                }
            });
            t.setDaemon(true);
            t.start();
        }
    }

    static class RawClientWithoutAuth {

        private Socket socket;
        private BufferedReader reader;
        private PrintWriter writer;
        private final BlockingQueue<String> inbox = new LinkedBlockingQueue<>();

        void connect() throws Exception {
            socket = new Socket("localhost", 8888);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            startReaderThread();
        }

        void send(String command) {
            writer.println(command);
        }

        String waitForPrefix(String prefix, long timeoutMillis) throws InterruptedException {
            long deadline = System.currentTimeMillis() + timeoutMillis;
            while (System.currentTimeMillis() < deadline) {
                String msg = inbox.poll(200, TimeUnit.MILLISECONDS);
                if (msg != null && msg.startsWith(prefix)) {
                    return msg;
                }
            }
            return null;
        }

        void close() throws IOException {
            socket.close();
        }

        private void startReaderThread() {
            Thread t = new Thread(() -> {
                try {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        inbox.offer(line);
                    }
                } catch (IOException ignored) {
                }
            });
            t.setDaemon(true);
            t.start();
        }
    }
}
