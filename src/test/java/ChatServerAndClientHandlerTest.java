import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ChatServerAndClientHandlerTest {

    private ChatServer server;


    static class TestClientHandler extends ClientHandler {

        private final List<String> receivedMessages = new ArrayList<>();

        public TestClientHandler(ChatServer server) throws Exception {
            super(new Socket(), server); // Dummy socket
        }

        @Override
        public void sendMessage(String message) {
            receivedMessages.add(message);
        }

        public String getLastMessage() {
            if (receivedMessages.isEmpty()) {
                return null;
            }
            return receivedMessages.get(receivedMessages.size() - 1);
        }

        public int messageCount() {
            return receivedMessages.size();
        }
    }


    @BeforeEach
    void setup() {
        server = new ChatServer();
    }

    @Test
    void registerUser_createsUser() {
        server.registerUser("u1", "Alice", "a@mail");
        assertNotNull(server.getUser("u1"));
    }

    @Test
    void connectUser_setsUserOnline() throws Exception {
        server.registerUser("u1", "Alice", "a@mail");

        TestClientHandler handler = new TestClientHandler(server);

        server.connectUser("u1", handler);

        assertTrue(server.getUser("u1").isOnline());
    }


    @Test
    void disconnectUser_setsUserOffline() {
        server.registerUser("u1", "Alice", "a@mail");
        server.disconnectUser("u1");
        assertFalse(server.getUser("u1").isOnline());
    }

    @Test
    void sendFriendRequest_sendsToReceiver() throws Exception {
        server.registerUser("u1", "Alice", "a@mail");
        server.registerUser("u2", "Bob", "b@mail");

        TestClientHandler handler = injectHandler("u2");

        server.sendFriendRequest("u1", "u2");

        assertTrue(handler.getLastMessage().startsWith("FRIEND_REQUEST"));
    }

    @Test
    void acceptFriendRequest_createsMutualFriends() {
        server.registerUser("u1", "Alice", "a@mail");
        server.registerUser("u2", "Bob", "b@mail");

        server.acceptFriendRequest("u1", "u2");

        assertTrue(server.getUser("u1").isFriend("u2"));
    }

    @Test
    void sendDirectMessage_sendsDMToReceiver() throws Exception {
        server.registerUser("u1", "Alice", "a@mail");
        server.registerUser("u2", "Bob", "b@mail");

        server.getUser("u1").addFriend("u2");
        server.getUser("u2").addFriend("u1");

        TestClientHandler handler = injectHandler("u2");

        server.sendDirectMessage("u1", "u2", "Hello");

        assertEquals("DM:u1:Alice:Hello", handler.getLastMessage());
    }

    @Test
    void getOnlineFriends_returnsOnlyOnlineFriends() {
        server.registerUser("u1", "Alice", "a@mail");
        server.registerUser("u2", "Bob", "b@mail");

        server.getUser("u1").addFriend("u2");
        server.getUser("u2").setOnline(true);

        assertEquals(1, server.getOnlineFriends("u1").size());
    }

    @Test
    void createLocalServer_addsServer() {
        server.registerUser("u1", "Alice", "a@mail");

        server.createLocalServer("s1", "Games", "u1");

        assertTrue(server.listLocalServers().stream().anyMatch(s -> s.contains("s1")));
    }

    @Test
    void joinLocalServer_addsMember() {
        server.registerUser("u1", "Alice", "a@mail");

        server.joinLocalServer("u1", "general");

        assertTrue(server.getServerMembers("general")
                .stream().anyMatch(m -> m.contains("u1")));
    }

    @Test
    void leaveLocalServer_removesMember() {
        server.registerUser("u1", "Alice", "a@mail");
        server.joinLocalServer("u1", "general");

        server.leaveLocalServer("u1", "general");

        assertTrue(server.getServerMembers("general")
                .stream().noneMatch(m -> m.contains("u1")));
    }

    @Test
    void sendServerMessage_broadcastsMessage() throws Exception {
        server.registerUser("u1", "Alice", "a@mail");

        TestClientHandler handler = injectHandler("u1");

        server.joinLocalServer("u1", "general");
        server.sendServerMessage("u1", "general", "Hello world");

        assertTrue(handler.getLastMessage().contains("Hello world"));
    }

    @Test
    void listLocalServers_returnsDefaultServer() {
        assertTrue(server.listLocalServers().size() >= 1);
    }

    @Test
    void getServerMembers_returnsNonNullList() {
        assertNotNull(server.getServerMembers("general"));
    }

    @Test
    void shutdown_doesNotThrow() {
        server.shutdown();
        assertTrue(true);
    }

    @Test
    void clientHandler_registerCommand_createsUser() throws Exception {
        ChatServer realServer = new ChatServer();
        ClientHandler handler = new ClientHandler(new Socket(), realServer);

        Method method = ClientHandler.class.getDeclaredMethod("processCommand", String.class);
        method.setAccessible(true);

        method.invoke(handler, "REGISTER:u5:Test:test@mail");

        assertNotNull(realServer.getUser("u5"));
    }

    @Test
    void clientHandler_joinServerCommand_addsUserToServer() throws Exception {
        ChatServer realServer = new ChatServer();
        ClientHandler handler = new ClientHandler(new Socket(), realServer);

        Method method = ClientHandler.class.getDeclaredMethod("processCommand", String.class);
        method.setAccessible(true);

        method.invoke(handler, "REGISTER:u3:Dave:d@mail");
        method.invoke(handler, "JOIN_SERVER:general");

        assertTrue(realServer.getServerMembers("general")
                .stream().anyMatch(m -> m.contains("u3")));
    }

    private TestClientHandler injectHandler(String userId) throws Exception {
        TestClientHandler testHandler = new TestClientHandler(server);

        Field onlineClientsField = ChatServer.class.getDeclaredField("onlineClients");
        onlineClientsField.setAccessible(true);

        @SuppressWarnings("unchecked")
        Map<String, ClientHandler> onlineClients =
                (Map<String, ClientHandler>) onlineClientsField.get(server);

        onlineClients.put(userId, testHandler);

        server.getUser(userId).setOnline(true);

        return testHandler;
    }
}
