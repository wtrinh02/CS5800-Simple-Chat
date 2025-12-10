import Message.Message;
import User.User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("User.User Class Tests")
class UserTest {

    private User testUser;
    private Set<String> testFriendIds;
    private Map<String, List<Message>> testDirectMessages;

    @BeforeEach
    void setUp() {
        testFriendIds = new HashSet<>();
        testDirectMessages = new HashMap<>();
    }

    @AfterEach
    void tearDown() {
        testUser = null;
        testFriendIds = null;
        testDirectMessages = null;
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create user with all valid parameters")
        void testConstructorCreatesUserWithValidParameters() {
            testUser = new User("user1", "Alice", "alice@test.com", testFriendIds, testDirectMessages, true);
            assertThat(testUser).isNotNull();
        }

        @Test
        @DisplayName("Should handle null friendIds by creating empty set")
        void testConstructorHandlesNullFriendIds() {
            testUser = new User("user1", "Alice", "alice@test.com", null, testDirectMessages, false);
            assertThat(testUser.getFriendIds()).isEmpty();
        }

        @Test
        @DisplayName("Should handle null directMessages by creating empty map")
        void testConstructorHandlesNullDirectMessages() {
            testUser = new User("user1", "Alice", "alice@test.com", testFriendIds, null, false);
            assertThat(testUser.getDirectMessages("anyConversation")).isEmpty();
        }
    }

    @Nested
    @DisplayName("Getter Method Tests")
    class GetterTests {

        @Test
        @DisplayName("getUserId() should return correct userId")
        void testGetUserIdReturnsCorrectValue() {
            testUser = new User("user123", "Alice", "alice@test.com", testFriendIds, testDirectMessages, true);
            String actualUserId = testUser.getUserId();
            assertThat(actualUserId).isEqualTo("user123");
        }

        @Test
        @DisplayName("getUsername() should return correct username")
        void testGetUsernameReturnsCorrectValue() {
            testUser = new User("user1", "Alice", "alice@test.com", testFriendIds, testDirectMessages, true);
            String actualUsername = testUser.getUsername();
            assertThat(actualUsername).isEqualTo("Alice");
        }

        @Test
        @DisplayName("getEmail() should return correct email")
        void testGetEmailReturnsCorrectValue() {
            testUser = new User("user1", "Alice", "alice@test.com", testFriendIds, testDirectMessages, true);
            String actualEmail = testUser.getEmail();
            assertThat(actualEmail).isEqualTo("alice@test.com");
        }

        @Test
        @DisplayName("isOnline() should return correct online status")
        void testIsOnlineReturnsCorrectValue() {
            testUser = new User("user1", "Alice", "alice@test.com", testFriendIds, testDirectMessages, true);
            boolean actualOnlineStatus = testUser.isOnline();
            assertThat(actualOnlineStatus).isTrue();
        }

        @Test
        @DisplayName("getFriendIds() should return defensive copy")
        void testGetFriendIdsReturnsDefensiveCopy() {
            testUser = new User("user1", "Alice", "alice@test.com", testFriendIds, testDirectMessages, true);
            Set<String> firstFriendIdsCopy = testUser.getFriendIds();
            firstFriendIdsCopy.add("hacker");
            Set<String> secondFriendIdsCopy = testUser.getFriendIds();
            assertThat(secondFriendIdsCopy).doesNotContain("hacker");
        }
    }

    @Nested
    @DisplayName("setOnline() Method Tests")
    class SetOnlineTests {

        @Test
        @DisplayName("setOnline(true) should set user online")
        void testSetOnlineTrueSetsUserOnline() {
            testUser = new User("user1", "Alice", "alice@test.com", testFriendIds, testDirectMessages, false);
            testUser.setOnline(true);
            assertThat(testUser.isOnline()).isTrue();
        }

        @Test
        @DisplayName("setOnline(false) should set user offline")
        void testSetOnlineFalseSetsUserOffline() {
            testUser = new User("user1", "Alice", "alice@test.com", testFriendIds, testDirectMessages, true);
            testUser.setOnline(false);
            assertThat(testUser.isOnline()).isFalse();
        }
    }

    @Nested
    @DisplayName("addFriend() Method Tests")
    class AddFriendTests {

        @Test
        @DisplayName("addFriend() should add friend to empty list")
        void testAddFriendAddsToEmptyList() {
            testUser = new User("user1", "Alice", "alice@test.com", testFriendIds, testDirectMessages, true);
            testUser.addFriend("friend1");
            assertThat(testUser.getFriendIds()).hasSize(1);
        }

        @Test
        @DisplayName("addFriend() should add friend to existing list")
        void testAddFriendAddsToExistingList() {
            testFriendIds.add("friend1");
            testUser = new User("user1", "Alice", "alice@test.com", testFriendIds, testDirectMessages, true);
            testUser.addFriend("friend2");
            assertThat(testUser.getFriendIds()).hasSize(2);
        }

        @Test
        @DisplayName("addFriend() should not add duplicate friend")
        void testAddFriendDoesNotAddDuplicate() {
            testUser = new User("user1", "Alice", "alice@test.com", testFriendIds, testDirectMessages, true);
            testUser.addFriend("friend1");
            testUser.addFriend("friend1");
            assertThat(testUser.getFriendIds()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("removeFriend() Method Tests")
    class RemoveFriendTests {

        @Test
        @DisplayName("removeFriend() should remove existing friend")
        void testRemoveFriendRemovesExistingFriend() {
            testFriendIds.add("friend1");
            testUser = new User("user1", "Alice", "alice@test.com", testFriendIds, testDirectMessages, true);
            testUser.removeFriend("friend1");
            assertThat(testUser.getFriendIds()).isEmpty();
        }

        @Test
        @DisplayName("removeFriend() should handle non-existent friend gracefully")
        void testRemoveFriendHandlesNonExistentFriendGracefully() {
            testFriendIds.add("friend1");
            testUser = new User("user1", "Alice", "alice@test.com", testFriendIds, testDirectMessages, true);
            testUser.removeFriend("nonExistentFriend");
            assertThat(testUser.getFriendIds()).hasSize(1);
        }

        @Test
        @DisplayName("removeFriend() should not affect other friends")
        void testRemoveFriendDoesNotAffectOtherFriends() {
            testFriendIds.add("friend1");
            testFriendIds.add("friend2");
            testUser = new User("user1", "Alice", "alice@test.com", testFriendIds, testDirectMessages, true);
            testUser.removeFriend("friend1");
            assertThat(testUser.getFriendIds()).contains("friend2");
        }
    }

    @Nested
    @DisplayName("isFriend() Method Tests")
    class IsFriendTests {

        @Test
        @DisplayName("isFriend() should return true for existing friend")
        void testIsFriendReturnsTrueForExistingFriend() {
            testFriendIds.add("friend1");
            testUser = new User("user1", "Alice", "alice@test.com", testFriendIds, testDirectMessages, true);
            boolean isFriendResult = testUser.isFriend("friend1");
            assertThat(isFriendResult).isTrue();
        }

        @Test
        @DisplayName("isFriend() should return false for non-existent friend")
        void testIsFriendReturnsFalseForNonExistentFriend() {
            testUser = new User("user1", "Alice", "alice@test.com", testFriendIds, testDirectMessages, true);
            boolean isFriendResult = testUser.isFriend("stranger");
            assertThat(isFriendResult).isFalse();
        }
    }

    @Nested
    @DisplayName("addDirectMessage() Method Tests")
    class AddDirectMessageTests {

        @Test
        @DisplayName("addDirectMessage() should add message to new conversation")
        void testAddDirectMessageAddsMessageToNewConversation() {
            testUser = new User("user1", "Alice", "alice@test.com", testFriendIds, testDirectMessages, true);
            Message testMessage = new Message("msg1", "user1", "user2", "Hello", Message.MessageType.DIRECT_MESSAGE);
            testUser.addDirectMessage("conv1", testMessage);
            assertThat(testUser.getDirectMessages("conv1")).hasSize(1);
        }

        @Test
        @DisplayName("addDirectMessage() should add message to existing conversation")
        void testAddDirectMessageAddsMessageToExistingConversation() {
            testUser = new User("user1", "Alice", "alice@test.com", testFriendIds, testDirectMessages, true);
            Message firstMessage = new Message("msg1", "user1", "user2", "Hello", Message.MessageType.DIRECT_MESSAGE);
            Message secondMessage = new Message("msg2", "user2", "user1", "Hi", Message.MessageType.DIRECT_MESSAGE);
            testUser.addDirectMessage("conv1", firstMessage);
            testUser.addDirectMessage("conv1", secondMessage);
            assertThat(testUser.getDirectMessages("conv1")).hasSize(2);
        }

        @Test
        @DisplayName("addDirectMessage() should preserve message order")
        void testAddDirectMessagePreservesMessageOrder() {
            testUser = new User("user1", "Alice", "alice@test.com", testFriendIds, testDirectMessages, true);
            Message firstMessage = new Message("msg1", "user1", "user2", "First", Message.MessageType.DIRECT_MESSAGE);
            Message secondMessage = new Message("msg2", "user1", "user2", "Second", Message.MessageType.DIRECT_MESSAGE);
            testUser.addDirectMessage("conv1", firstMessage);
            testUser.addDirectMessage("conv1", secondMessage);
            List<Message> conversationMessages = testUser.getDirectMessages("conv1");
            assertThat(conversationMessages.get(0).getContent()).isEqualTo("First");
        }
    }

    @Nested
    @DisplayName("getDirectMessages() Method Tests")
    class GetDirectMessagesTests {

        @Test
        @DisplayName("getDirectMessages() should return empty list for non-existent conversation")
        void testGetDirectMessagesReturnsEmptyListForNonExistentConversation() {
            testUser = new User("user1", "Alice", "alice@test.com", testFriendIds, testDirectMessages, true);
            List<Message> conversationMessages = testUser.getDirectMessages("nonExistentConversation");
            assertThat(conversationMessages).isEmpty();
        }

        @Test
        @DisplayName("getDirectMessages() should return correct messages for conversation")
        void testGetDirectMessagesReturnsCorrectMessagesForConversation() {
            testUser = new User("user1", "Alice", "alice@test.com", testFriendIds, testDirectMessages, true);
            Message testMessage = new Message("msg1", "user1", "user2", "Hello", Message.MessageType.DIRECT_MESSAGE);
            testUser.addDirectMessage("conv1", testMessage);
            List<Message> conversationMessages = testUser.getDirectMessages("conv1");
            assertThat(conversationMessages).contains(testMessage);
        }

        @Test
        @DisplayName("getDirectMessages() should isolate different conversations")
        void testGetDirectMessagesIsolatesDifferentConversations() {
            testUser = new User("user1", "Alice", "alice@test.com", testFriendIds, testDirectMessages, true);
            Message messageToBob = new Message("msg1", "user1", "user2", "Hello Bob", Message.MessageType.DIRECT_MESSAGE);
            Message messageToCharlie = new Message("msg2", "user1", "user3", "Hello Charlie", Message.MessageType.DIRECT_MESSAGE);
            testUser.addDirectMessage("conv_user2", messageToBob);
            testUser.addDirectMessage("conv_user3", messageToCharlie);
            List<Message> bobConversationMessages = testUser.getDirectMessages("conv_user2");
            assertThat(bobConversationMessages).doesNotContain(messageToCharlie);
        }
    }

    @Nested
    @DisplayName("toString() Method Tests")
    class ToStringTests {

        @Test
        @DisplayName("toString() should return non-null string")
        void testToStringReturnsNonNullString() {
            testUser = new User("user1", "Alice", "alice@test.com", testFriendIds, testDirectMessages, true);
            String toStringResult = testUser.toString();
            assertThat(toStringResult).isNotNull();
        }

        @Test
        @DisplayName("toString() should contain userId")
        void testToStringContainsUserId() {
            testUser = new User("user123", "Alice", "alice@test.com", testFriendIds, testDirectMessages, true);
            String toStringResult = testUser.toString();
            assertThat(toStringResult).contains("user123");
        }

        @Test
        @DisplayName("toString() should contain username")
        void testToStringContainsUsername() {
            testUser = new User("user1", "Alice", "alice@test.com", testFriendIds, testDirectMessages, true);
            String toStringResult = testUser.toString();
            assertThat(toStringResult).contains("Alice");
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle empty userId")
        void testHandlesEmptyUserId() {
            testUser = new User("", "Alice", "alice@test.com", testFriendIds, testDirectMessages, true);
            assertThat(testUser.getUserId()).isEmpty();
        }

        @Test
        @DisplayName("Should handle empty username")
        void testHandlesEmptyUsername() {
            testUser = new User("user1", "", "alice@test.com", testFriendIds, testDirectMessages, true);
            assertThat(testUser.getUsername()).isEmpty();
        }

        @Test
        @DisplayName("Should handle null email")
        void testHandlesNullEmail() {
            testUser = new User("user1", "Alice", null, testFriendIds, testDirectMessages, true);
            assertThat(testUser.getEmail()).isNull();
        }

        @ParameterizedTest
        @ValueSource(strings = {"friend1", "friend2", "friend3"})
        @DisplayName("Should add multiple different friends")
        void testAddsMultipleDifferentFriends(String friendIdToAdd) {
            if (testUser == null) {
                testUser = new User("user1", "Alice", "alice@test.com", testFriendIds, testDirectMessages, true);
            }
            testUser.addFriend(friendIdToAdd);
            assertThat(testUser.isFriend(friendIdToAdd)).isTrue();
        }
    }
}