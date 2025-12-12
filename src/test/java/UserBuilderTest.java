import Message.Message;
import User.User;
import User.UserBuilder;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserBuilderTest {

    @Test
    void buildShouldCreateUserWithProvidedValues() {
        Set<String> friends = new HashSet<>();
        friends.add("friend-1");
        Map<String, List<Message>> directMessages = new HashMap<>();
        directMessages.put("friend-1", Collections.singletonList(
                new Message("m1", "friend-1", "u1", "hello", Message.MessageType.DIRECT_MESSAGE)
        ));

        UserBuilder builder = new UserBuilder()
                .setUserId("u1")
                .setUsername("alice")
                .setEmail("alice@example.com")
                .setFriendIds(friends)
                .setDirectMessages(directMessages)
                .setOnline(true);

        User user = builder.build();
        boolean hasExpectedFields = "u1".equals(user.getUserId())
                && "alice".equals(user.getUsername())
                && "alice@example.com".equals(user.getEmail())
                && user.isOnline()
                && user.getFriendIds().contains("friend-1")
                && !user.getDirectMessages("friend-1").isEmpty();
        assertEquals(true, hasExpectedFields);
    }

    @Test
    void builderWithNoFriendIdsShouldCreateUserWithEmptyFriendSet() {
        UserBuilder builder = new UserBuilder()
                .setUserId("u2")
                .setUsername("bob")
                .setEmail("bob@example.com");
        User user = builder.build();
        boolean hasNoFriends = user.getFriendIds().isEmpty();
        assertEquals(true, hasNoFriends);
    }

    @Test
    void builderWithNoDirectMessagesShouldReturnEmptyConversationHistory() {
        UserBuilder builder = new UserBuilder()
                .setUserId("u3")
                .setUsername("carol")
                .setEmail("carol@example.com");
        User user = builder.build();
        boolean historyIsEmpty = user.getDirectMessages("any").isEmpty();
        assertEquals(true, historyIsEmpty);
    }
}
