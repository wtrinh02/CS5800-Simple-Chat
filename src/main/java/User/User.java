package User;
import java.util.*;
import Message.*;
import User.State.*;

public class User {

    private final String userId;
    private final String username;
    private final String email;
    private final Set<String> friendIds;
    private final Map<String, List<Message>> directMessages;
    private final Set<String> blockedUserIds;
    private boolean online;
    private UserState state;

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
        this.friendIds = friendIds != null ? new HashSet<>(friendIds) : new HashSet<>();
        this.directMessages = directMessages != null ? new HashMap<>(directMessages) : new HashMap<>();
        this.blockedUserIds = new HashSet<>();
        this.online = online;
        this.state = new OfflineState();
    }

    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }

    public Set<String> getFriendIds() {
        return new HashSet<>(friendIds);
    }

    public boolean isOnline() {
        return state instanceof OnlineState;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public void addFriend(String friendId) { friendIds.add(friendId); }
    public void removeFriend(String friendId) { friendIds.remove(friendId); }

    public boolean isFriend(String userId) {
        return friendIds.contains(userId);
    }

    public void addDirectMessage(String conversationId, Message message) {
        directMessages.computeIfAbsent(conversationId, k -> new ArrayList<>()).add(message);
    }

    public List<Message> getDirectMessages(String conversationId) {
        return directMessages.getOrDefault(conversationId, new ArrayList<>());
    }

    public void blockUser(String targetUserId) {
        if (targetUserId != null && !targetUserId.equals(userId)) {
            blockedUserIds.add(targetUserId);
        }
    }

    public void unblockUser(String targetUserId) {
        blockedUserIds.remove(targetUserId);
    }

    public boolean hasBlocked(String targetUserId) {
        return blockedUserIds.contains(targetUserId);
    }

    public Set<String> getBlockedUserIds() {
        return new HashSet<>(blockedUserIds);
    }

    public void setState(UserState newState) {
        this.state = newState;
    }

    public String getStatus() {
        return state.getStateName();
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", online=" + isOnline() +
                ", friendsCount=" + friendIds.size() +
                '}';
    }
}
