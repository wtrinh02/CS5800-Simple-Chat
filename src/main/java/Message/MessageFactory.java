package Message;

import java.util.UUID;

public class MessageFactory {

    public static Message directMessage(String senderId, String receiverId, String content) {
        return create(senderId, receiverId, content, Message.MessageType.DIRECT_MESSAGE);
    }

    public static Message friendRequest(String senderId, String receiverId) {
        return create(senderId, receiverId,
                senderId + " sent you a friend request",
                Message.MessageType.FRIEND_REQUEST);
    }

    public static Message friendAccept(String senderId, String receiverId) {
        return create(senderId, receiverId,
                senderId + " accepted your friend request",
                Message.MessageType.FRIEND_ACCEPT);
    }

    public static Message systemMessage(String content) {
        return create("SYSTEM", "ALL", content, Message.MessageType.SYSTEM_MESSAGE);
    }

    public static Message userOnline(String username) {
        return create("SYSTEM", "general",
                username + " is now Online",
                Message.MessageType.USER_ONLINE);
    }

    public static Message userOffline(String username) {
        return create("SYSTEM", "general",
                username + " is now Offline",
                Message.MessageType.USER_OFFLINE);
    }

    public static Message serverJoin(String username, String serverId) {
        return create("SYSTEM", serverId,
                username + " joined " + serverId,
                Message.MessageType.SERVER_JOIN);
    }

    public static Message serverLeave(String username, String serverId) {
        return create("SYSTEM", serverId,
                username + " left " + serverId,
                Message.MessageType.SERVER_LEAVE);
    }

    public static Message serverMessage(String senderId, String serverId, String content) {
        return create(senderId, serverId, content, Message.MessageType.SERVER_MESSAGE);
    }

    private static Message create(String sender, String receiver, String content, Message.MessageType type) {
        return new Message(
                UUID.randomUUID().toString(),
                sender,
                receiver,
                content,
                type
        );
    }
}
