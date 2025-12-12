import Message.Message;
import Message.MessageFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MessageFactoryTest {

    private final MessageFactory factory = new MessageFactory();

    @Test
    void directMessageShouldCreateDirectMessageWithExpectedFields() {
        Message message = factory.directMessage("s1", "r1", "hello");
        boolean hasExpectedFields = "s1".equals(message.getSenderId())
                && "r1".equals(message.getReceiverId())
                && "hello".equals(message.getContent())
                && Message.MessageType.DIRECT_MESSAGE.equals(message.getType());
        assertEquals(true, hasExpectedFields);
    }

    @Test
    void serverMessageShouldCreateServerMessageWithExpectedFields() {
        Message message = factory.serverMessage("s2", "server-1", "channel message");
        boolean hasExpectedFields = "s2".equals(message.getSenderId())
                && "server-1".equals(message.getReceiverId())
                && "channel message".equals(message.getContent())
                && Message.MessageType.SERVER_MESSAGE.equals(message.getType());
        assertEquals(true, hasExpectedFields);
    }

    @Test
    void friendRequestShouldCreateFriendRequestMessageWithFixedContent() {
        Message message = factory.friendRequest("s3", "r3");
        boolean hasExpectedFields = "s3".equals(message.getSenderId())
                && "r3".equals(message.getReceiverId())
                && "Friend Request".equals(message.getContent())
                && Message.MessageType.FRIEND_REQUEST.equals(message.getType());
        assertEquals(true, hasExpectedFields);
    }

    @Test
    void userOnlineShouldCreateSystemUserOnlineMessageToAll() {
        Message message = factory.userOnline("Alice");
        boolean hasExpectedFields = "SYSTEM".equals(message.getSenderId())
                && "ALL".equals(message.getReceiverId())
                && message.getContent().contains("Alice")
                && Message.MessageType.USER_ONLINE.equals(message.getType());
        assertEquals(true, hasExpectedFields);
    }

    @Test
    void userOfflineShouldCreateSystemUserOfflineMessageToAll() {
        Message message = factory.userOffline("Bob");
        boolean hasExpectedFields = "SYSTEM".equals(message.getSenderId())
                && "ALL".equals(message.getReceiverId())
                && message.getContent().contains("Bob")
                && Message.MessageType.USER_OFFLINE.equals(message.getType());
        assertEquals(true, hasExpectedFields);
    }

    @Test
    void serverJoinShouldCreateServerJoinMessageWithSystemSender() {
        Message message = factory.serverJoin("alice", "server-1");
        boolean hasExpectedFields = "SYSTEM".equals(message.getSenderId())
                && "server-1".equals(message.getReceiverId())
                && message.getContent().contains("joined the server")
                && Message.MessageType.SERVER_JOIN.equals(message.getType());
        assertEquals(true, hasExpectedFields);
    }

    @Test
    void serverLeaveShouldCreateServerLeaveMessageWithSystemSender() {
        Message message = factory.serverLeave("alice", "server-1");
        boolean hasExpectedFields = "SYSTEM".equals(message.getSenderId())
                && "server-1".equals(message.getReceiverId())
                && message.getContent().contains("left the server")
                && Message.MessageType.SERVER_LEAVE.equals(message.getType());
        assertEquals(true, hasExpectedFields);
    }
}
