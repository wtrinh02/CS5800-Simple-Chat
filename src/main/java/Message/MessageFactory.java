package Message;

import java.util.UUID;

public class MessageFactory {

    public Message directMessage(String senderId, String receiverId, String content) {
        return new Message(
                UUID.randomUUID().toString(),
                senderId,
                receiverId,
                content,
                Message.MessageType.DIRECT_MESSAGE
        );
    }

    public Message serverMessage(String senderId, String serverId, String content) {
        return new Message(
                UUID.randomUUID().toString(),
                senderId,
                serverId,
                content,
                Message.MessageType.SERVER_MESSAGE
        );
    }

    public Message friendRequest(String senderId, String receiverId) {
        return new Message(
                UUID.randomUUID().toString(),
                senderId,
                receiverId,
                "Friend Request",
                Message.MessageType.FRIEND_REQUEST
        );
    }

    public Message userOnline(String username) {
        return new Message(
                UUID.randomUUID().toString(),
                "SYSTEM",
                "ALL",
                username + " is now online",
                Message.MessageType.USER_ONLINE
        );
    }

    public Message userOffline(String username) {
        return new Message(
                UUID.randomUUID().toString(),
                "SYSTEM",
                "ALL",
                username + " is now offline",
                Message.MessageType.USER_OFFLINE
        );
    }

    public Message serverJoin(String username, String serverId) {
        return new Message(
                UUID.randomUUID().toString(),
                "SYSTEM",
                serverId,
                username + " joined the server",
                Message.MessageType.SERVER_JOIN
        );
    }

    public Message serverLeave(String username, String serverId) {
        return new Message(
                UUID.randomUUID().toString(),
                "SYSTEM",
                serverId,
                username + " left the server",
                Message.MessageType.SERVER_LEAVE
        );
    }
}
