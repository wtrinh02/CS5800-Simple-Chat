import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message {
    private final String messageId;
    private final String senderId;
    private final String receiverId;
    private final String content;
    private final LocalDateTime timestamp;
    private final MessageType type;

    public enum MessageType {
        DIRECT_MESSAGE,
        FRIEND_REQUEST,
        FRIEND_ACCEPT,
        SYSTEM_MESSAGE,
        USER_ONLINE,
        USER_OFFLINE,
        SERVER_MESSAGE,
        SERVER_JOIN,
        SERVER_LEAVE
    }

    public Message(String messageId, String senderId, String receiverId,
                   String content, MessageType type) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.timestamp = LocalDateTime.now();
        this.type = type;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public MessageType getType() {
        return type;
    }

    public String getFormattedTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return timestamp.format(formatter);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s -> %s: %s (Type: %s)",
                getFormattedTimestamp(), senderId, receiverId, content, type);
    }

    public String toDisplayString() {
        return String.format("[%s] %s: %s",
                timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                senderId, content);
    }
}