import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChatClientTest {

    private ChatClient createClientWithWriter(TestWriter writer) throws Exception {
        ChatClient client = new ChatClient("user-1", "Alice");
        Field outField = ChatClient.class.getDeclaredField("out");
        outField.setAccessible(true);
        outField.set(client, writer.getWriter());
        return client;
    }

    private ChatClient createClientWithSocket(TestSocket socket, BufferedReader reader, PrintWriter writer) throws Exception {
        ChatClient client = new ChatClient("user-2", "Bob");
        Field socketField = ChatClient.class.getDeclaredField("socket");
        Field inField = ChatClient.class.getDeclaredField("in");
        Field outField = ChatClient.class.getDeclaredField("out");
        socketField.setAccessible(true);
        inField.setAccessible(true);
        outField.setAccessible(true);
        socketField.set(client, socket);
        inField.set(client, reader);
        outField.set(client, writer);
        return client;
    }

    @Test
    void sendFriendRequestShouldSendCorrectFriendRequestCommand() throws Exception {
        TestWriter writer = new TestWriter();
        ChatClient client = createClientWithWriter(writer);
        client.sendFriendRequest("friend-123");
        boolean matchesExpected = writer.getOutput().equals("FRIEND_REQUEST:friend-123");
        assertEquals(true, matchesExpected);
    }

    @Test
    void acceptFriendRequestShouldSendCorrectAcceptFriendCommand() throws Exception {
        TestWriter writer = new TestWriter();
        ChatClient client = createClientWithWriter(writer);
        client.acceptFriendRequest("friend-456");
        boolean matchesExpected = writer.getOutput().equals("ACCEPT_FRIEND:friend-456");
        assertEquals(true, matchesExpected);
    }

    @Test
    void sendDirectMessageShouldSendProperlyFormattedSendDmCommand() throws Exception {
        TestWriter writer = new TestWriter();
        ChatClient client = createClientWithWriter(writer);
        client.sendDirectMessage("receiver-1", "hello world");
        boolean matchesExpected = writer.getOutput().equals("SEND_DM:receiver-1:hello world");
        assertEquals(true, matchesExpected);
    }

    @Test
    void getOnlineFriendsShouldSendGetFriendsCommand() throws Exception {
        TestWriter writer = new TestWriter();
        ChatClient client = createClientWithWriter(writer);
        client.getOnlineFriends();
        boolean matchesExpected = writer.getOutput().equals("GET_FRIENDS");
        assertEquals(true, matchesExpected);
    }

    @Test
    void blockUserShouldSendBlockUserCommand() throws Exception {
        TestWriter writer = new TestWriter();
        ChatClient client = createClientWithWriter(writer);
        client.blockUser("blocked-user");
        boolean matchesExpected = writer.getOutput().equals("BLOCK_USER:blocked-user");
        assertEquals(true, matchesExpected);
    }

    @Test
    void unblockUserShouldSendUnblockUserCommand() throws Exception {
        TestWriter writer = new TestWriter();
        ChatClient client = createClientWithWriter(writer);
        client.unblockUser("blocked-user");
        boolean matchesExpected = writer.getOutput().equals("UNBLOCK_USER:blocked-user");
        assertEquals(true, matchesExpected);
    }

    @Test
    void getBlockedUsersShouldSendGetBlockedCommand() throws Exception {
        TestWriter writer = new TestWriter();
        ChatClient client = createClientWithWriter(writer);
        client.getBlockedUsers();
        boolean matchesExpected = writer.getOutput().equals("GET_BLOCKED");
        assertEquals(true, matchesExpected);
    }

    @Test
    void getDirectMessageHistoryShouldSendGetHistoryCommandWithTargetId() throws Exception {
        TestWriter writer = new TestWriter();
        ChatClient client = createClientWithWriter(writer);
        client.getDirectMessageHistory("friend-123");
        boolean matchesExpected = writer.getOutput().equals("GET_HISTORY:friend-123");
        assertEquals(true, matchesExpected);
    }

    @Test
    void createLocalServerShouldSendCreateServerCommandWithIdAndName() throws Exception {
        TestWriter writer = new TestWriter();
        ChatClient client = createClientWithWriter(writer);
        client.createLocalServer("server-1", "Cool Server");
        boolean matchesExpected = writer.getOutput().equals("CREATE_SERVER:server-1:Cool Server");
        assertEquals(true, matchesExpected);
    }

    @Test
    void joinLocalServerShouldSendJoinServerCommandWithServerId() throws Exception {
        TestWriter writer = new TestWriter();
        ChatClient client = createClientWithWriter(writer);
        client.joinLocalServer("server-2");
        boolean matchesExpected = writer.getOutput().equals("JOIN_SERVER:server-2");
        assertEquals(true, matchesExpected);
    }

    @Test
    void leaveLocalServerShouldSendLeaveServerCommandWithServerId() throws Exception {
        TestWriter writer = new TestWriter();
        ChatClient client = createClientWithWriter(writer);
        client.leaveLocalServer("server-3");
        boolean matchesExpected = writer.getOutput().equals("LEAVE_SERVER:server-3");
        assertEquals(true, matchesExpected);
    }

    @Test
    void sendServerMessageShouldSendServerMsgCommandWithServerIdAndContent() throws Exception {
        TestWriter writer = new TestWriter();
        ChatClient client = createClientWithWriter(writer);

        java.lang.reflect.Field currentServerField = ChatClient.class.getDeclaredField("currentServer");
        currentServerField.setAccessible(true);
        currentServerField.set(client, "general");

        client.sendServerMessage("general", "hello server");

        boolean matchesExpected = writer.getOutput().equals("SERVER_MSG:general:hello server");
        assertEquals(true, matchesExpected);
    }



    @Test
    void listLocalServersShouldSendListServersCommand() throws Exception {
        TestWriter writer = new TestWriter();
        ChatClient client = createClientWithWriter(writer);
        client.listLocalServers();
        boolean matchesExpected = writer.getOutput().equals("LIST_SERVERS");
        assertEquals(true, matchesExpected);
    }

    @Test
    void getServerMembersShouldSendServerMembersCommandWithServerId() throws Exception {
        TestWriter writer = new TestWriter();
        ChatClient client = createClientWithWriter(writer);
        client.getServerMembers("general");
        boolean matchesExpected = writer.getOutput().equals("SERVER_MEMBERS:general");
        assertEquals(true, matchesExpected);
    }

    @Test
    void disconnectShouldCloseUnderlyingSocketWhenPresent() throws Exception {
        TestSocket socket = new TestSocket();
        BufferedReader reader = new BufferedReader(new java.io.StringReader(""));
        PrintWriter writer = new PrintWriter(System.out, true);
        ChatClient client = createClientWithSocket(socket, reader, writer);
        client.disconnect();
        boolean socketClosed = socket.wasClosed();
        assertEquals(true, socketClosed);
    }
}
