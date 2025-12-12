package Message;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class Message {

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

    private final String id;
    private final String senderId;
    private final String receiverId;   // or serverId
    private final String content;
    private final MessageType type;
    private long timestamp;

    // ---- MAIN CONSTRUCTOR (DATABASE + FULL CONTROL) ----
    public Message(String id, String senderId, String receiverId,
                   String content, MessageType type, long timestamp) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.type = type;
        this.timestamp = timestamp;
    }

    // ---- DEFAULT CONSTRUCTOR (auto timestamp) ----
    public Message(String id, String senderId, String receiverId,
                   String content, MessageType type) {
        this(id, senderId, receiverId, content, type, System.currentTimeMillis());
    }

    public String getId() { return id; }
    public String getSenderId() { return senderId; }
    public String getReceiverId() { return receiverId; }
    public String getContent() { return content; }
    public MessageType getType() { return type; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp =  timestamp; }

    public String getFormattedTimestamp() {
        java.text.SimpleDateFormat fmt =
                new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return fmt.format(new java.util.Date(timestamp));
    }


    @Override
    public String toString() {
        return String.format(
                "[%s] %s -> %s: %s (Type: %s)",
                getFormattedTimestamp(),
                senderId,
                receiverId,
                content,
                type
        );
    }


    public String toDisplayString() {
        java.text.SimpleDateFormat fmt =
                new java.text.SimpleDateFormat("HH:mm:ss");

        return String.format(
                "[%s] %s: %s",
                fmt.format(new java.util.Date(timestamp)),
                senderId,
                content
        );
    }

}