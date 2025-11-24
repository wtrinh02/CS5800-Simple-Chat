import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.Socket;
import java.io.BufferedReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ChatClientMockTest {

    private ChatClient createClientWithWriter(TestWriter testWriter) throws Exception {
        ChatClient client = new ChatClient("u1", "Alice");

        Field outField = ChatClient.class.getDeclaredField("out");
        outField.setAccessible(true);
        outField.set(client, testWriter.getWriter());

        return client;
    }

    @Test
    void sendFriendRequest_sendsCorrectCommand() throws Exception {
        TestWriter writer = new TestWriter();
        ChatClient client = createClientWithWriter(writer);

        client.sendFriendRequest("u2");

        assertEquals("FRIEND_REQUEST:u2", writer.getOutput());
    }

    @Test
    void acceptFriendRequest_sendsCorrectCommand() throws Exception {
        TestWriter writer = new TestWriter();
        ChatClient client = createClientWithWriter(writer);

        client.acceptFriendRequest("u2");

        assertEquals("ACCEPT_FRIEND:u2", writer.getOutput());
    }

    @Test
    void sendDirectMessage_sendsCorrectFormat() throws Exception {
        TestWriter writer = new TestWriter();
        ChatClient client = createClientWithWriter(writer);

        client.sendDirectMessage("u2", "Hello");

        assertEquals("SEND_DM:u2:Hello", writer.getOutput());
    }

    @Test
    void createLocalServer_sendsCorrectCommand() throws Exception {
        TestWriter writer = new TestWriter();
        ChatClient client = createClientWithWriter(writer);

        client.createLocalServer("s1", "Game");

        assertEquals("CREATE_SERVER:s1:Game", writer.getOutput());
    }

    @Test
    void joinLocalServer_sendsCorrectCommand() throws Exception {
        TestWriter writer = new TestWriter();
        ChatClient client = createClientWithWriter(writer);

        client.joinLocalServer("general");

        assertEquals("JOIN_SERVER:general", writer.getOutput());
    }

    @Test
    void leaveLocalServer_sendsCorrectCommand() throws Exception {
        TestWriter writer = new TestWriter();
        ChatClient client = createClientWithWriter(writer);

        client.leaveLocalServer("general");

        assertEquals("LEAVE_SERVER:general", writer.getOutput());
    }

    @Test
    void sendServerMessage_sendsCorrectFormat() throws Exception {
        TestWriter writer = new TestWriter();
        ChatClient client = createClientWithWriter(writer);

        client.sendServerMessage("general", "Hello");

        assertEquals("SERVER_MSG:general:Hello", writer.getOutput());
    }

    @Test
    void listLocalServers_sendsCorrectCommand() throws Exception {
        TestWriter writer = new TestWriter();
        ChatClient client = createClientWithWriter(writer);

        client.listLocalServers();

        assertEquals("LIST_SERVERS", writer.getOutput());
    }

    @Test
    void getServerMembers_sendsCorrectCommand() throws Exception {
        TestWriter writer = new TestWriter();
        ChatClient client = createClientWithWriter(writer);

        client.getServerMembers("general");

        assertEquals("SERVER_MEMBERS:general", writer.getOutput());
    }

    @Test
    void handleServerJoined_setsCurrentServer() throws Exception {
        ChatClient client = new ChatClient("u1", "Alice");

        Method method = ChatClient.class.getDeclaredMethod("handleServerMessage", String.class);
        method.setAccessible(true);

        method.invoke(client, "SERVER_JOINED:general:General");

        Field field = ChatClient.class.getDeclaredField("currentServer");
        field.setAccessible(true);

        assertEquals("general", field.get(client));
    }

    @Test
    void handleServerLeft_clearsCurrentServer() throws Exception {
        ChatClient client = new ChatClient("u1", "Alice");

        Method method = ChatClient.class.getDeclaredMethod("handleServerMessage", String.class);
        method.setAccessible(true);

        method.invoke(client, "SERVER_JOINED:general:General");
        method.invoke(client, "SERVER_LEFT:general");

        Field field = ChatClient.class.getDeclaredField("currentServer");
        field.setAccessible(true);

        assertEquals(null, field.get(client));
    }

    @Test
    void disconnect_closesSocketSuccessfully() throws Exception {
        ChatClient client = new ChatClient("u1", "Alice");

        TestSocket testSocket = new TestSocket();

        BufferedReader reader = null;
        PrintWriter writer = null;

        Field socketField = ChatClient.class.getDeclaredField("socket");
        Field inField = ChatClient.class.getDeclaredField("in");
        Field outField = ChatClient.class.getDeclaredField("out");

        socketField.setAccessible(true);
        inField.setAccessible(true);
        outField.setAccessible(true);

        socketField.set(client, testSocket);
        inField.set(client, reader);
        outField.set(client, writer);

        client.disconnect();

        assertTrue(testSocket.wasClosed());
    }


}
