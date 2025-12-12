import Message.Message;
import User.User;
import User.State.AwayState;
import User.State.BusyState;
import User.State.OfflineState;
import User.State.OnlineState;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserTest {

    private User createUser() {
        return new User("u1", "alice", "alice@example.com", new HashSet<>(), new HashMap<>(), false);
    }

    @Test
    void constructorShouldPopulateCoreFields() {
        Set<String> friends = new HashSet<>();
        Map<String, List<Message>> directMessages = new HashMap<>();
        User user = new User("id-1", "bob", "bob@example.com", friends, directMessages, true);
        boolean hasExpectedFields = "id-1".equals(user.getUserId())
                && "bob".equals(user.getUsername())
                && "bob@example.com".equals(user.getEmail())
                && user.isOnline();
        assertEquals(true, hasExpectedFields);
    }

    @Test
    void addFriendShouldAddFriendId() {
        User user = createUser();
        user.addFriend("friend-1");
        boolean containsFriend = user.getFriendIds().contains("friend-1");
        assertEquals(true, containsFriend);
    }

    @Test
    void removeFriendShouldRemoveFriendId() {
        User user = createUser();
        user.addFriend("friend-1");
        user.removeFriend("friend-1");
        boolean doesNotContainFriend = !user.getFriendIds().contains("friend-1");
        assertEquals(true, doesNotContainFriend);
    }

    @Test
    void blockUserShouldAddBlockedUserId() {
        User user = createUser();
        user.blockUser("blocked-1");
        boolean containsBlocked = user.getBlockedUserIds().contains("blocked-1");
        assertEquals(true, containsBlocked);
    }

    @Test
    void unblockUserShouldRemoveBlockedUserId() {
        User user = createUser();
        user.blockUser("blocked-1");
        user.unblockUser("blocked-1");
        boolean doesNotContainBlocked = !user.getBlockedUserIds().contains("blocked-1");
        assertEquals(true, doesNotContainBlocked);
    }

    @Test
    void addDirectMessageShouldAppendMessageToConversation() {
        User user = createUser();
        Message msg = new Message("m1", "friend-1", "u1", "hello", Message.MessageType.DIRECT_MESSAGE);
        user.addDirectMessage("friend-1", msg);
        List<Message> messages = user.getDirectMessages("friend-1");
        boolean containsMessage = messages.size() == 1 && "hello".equals(messages.get(0).getContent());
        assertEquals(true, containsMessage);
    }

    @Test
    void getDirectMessagesShouldReturnEmptyListForUnknownPartner() {
        User user = createUser();
        List<Message> messages = user.getDirectMessages("unknown");
        boolean isEmpty = messages.isEmpty();
        assertEquals(true, isEmpty);
    }

    @Test
    void setOnlineTrueShouldMarkUserOnline() {
        User user = createUser();
        user.setOnline(true);
        boolean isOnline = user.isOnline();
        assertEquals(true, isOnline);
    }

    @Test
    void setOnlineFalseShouldMarkUserOffline() {
        User user = createUser();
        user.setOnline(true);
        user.setOnline(false);
        boolean isOffline = !user.isOnline();
        assertEquals(true, isOffline);
    }

    @Test
    void settingOnlineStateShouldExposeOnlineStateName() {
        User user = createUser();
        user.setState(new OnlineState());
        boolean isOnlineState = "ONLINE".equals(user.getStateName());
        assertEquals(true, isOnlineState);
    }

    @Test
    void settingAwayStateShouldExposeAwayStateName() {
        User user = createUser();
        user.setState(new AwayState());
        boolean isAwayState = "AWAY".equals(user.getStateName());
        assertEquals(true, isAwayState);
    }

    @Test
    void settingBusyStateShouldExposeBusyStateName() {
        User user = createUser();
        user.setState(new BusyState());
        boolean isBusyState = "BUSY".equals(user.getStateName());
        assertEquals(true, isBusyState);
    }

    @Test
    void settingOfflineStateShouldExposeOfflineStateName() {
        User user = createUser();
        user.setState(new OfflineState());
        boolean isOfflineState = "OFFLINE".equals(user.getStateName());
        assertEquals(true, isOfflineState);
    }

    @Test
    void toStringShouldContainUserIdUsernameAndEmail() {
        User user = createUser();
        String repr = user.toString();
        boolean containsCoreFields = repr.contains("u1") && repr.contains("alice") && repr.contains("alice@example.com");
        assertEquals(true, containsCoreFields);
    }
}
