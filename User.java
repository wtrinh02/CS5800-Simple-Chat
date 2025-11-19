import java.util.*;

public class User {

    private final String userId;
    private final String username;
    private final String email;
    private final Set<String> friendIds;
    // Stores conversations: Key is the conversation ID (or other user's ID), Value is list of messages
    private final Map<String, List<Message>> directMessages;
    private boolean online;

    // Constructor with all parameters
    public User(
        String userId,
        String username,
        String email,
        Set<String> friendIds,
        Map<String, List<Message>> directMessages,
        boolean online
    ) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        // Defensive copying to prevent external modification of the internal set
        this.friendIds = friendIds != null
            ? new HashSet<>(friendIds)
            : new HashSet<>();
        this.directMessages = directMessages != null
            ? new HashMap<>(directMessages)
            : new HashMap<>();
        this.online = online;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    // Returns a copy to protect internal state
    public Set<String> getFriendIds() {
        return new HashSet<>(friendIds);
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public void addFriend(String friendId) {
        friendIds.add(friendId);
    }

    public void removeFriend(String friendId) {
        friendIds.remove(friendId);
    }

    public boolean isFriend(String userId) {
        return friendIds.contains(userId);
    }

    public void addDirectMessage(String conversationId, Message message) {
        directMessages
            .computeIfAbsent(conversationId, k -> new ArrayList<>())
            .add(message);
    }

    public List<Message> getDirectMessages(String conversationId) {
        return directMessages.getOrDefault(conversationId, new ArrayList<>());
    }

    @Override
    public String toString() {
        return (
            "User{" +
            "userId='" +
            userId +
            '\'' +
            ", username='" +
            username +
            '\'' +
            ", email='" +
            email +
            '\'' +
            ", online=" +
            online +
            ", friendsCount=" +
            friendIds.size() +
            '}'
        );
    }
}
