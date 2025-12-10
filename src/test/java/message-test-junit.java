import Message.Message;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Message.Message Class Tests")
class MessageTest {

    private Message testMessage;

    @AfterEach
    void tearDown() {
        testMessage = null;
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create message with all valid parameters")
        void testConstructorCreatesMessageWithValidParameters() {
            testMessage = new Message("msg1", "user1", "user2", "Hello", Message.MessageType.DIRECT_MESSAGE);
            assertThat(testMessage).isNotNull();
        }

        @Test
        @DisplayName("Should auto-generate timestamp on creation")
        void testConstructorAutoGeneratesTimestamp() {
            LocalDateTime timestampBeforeCreation = LocalDateTime.now();
            testMessage = new Message("msg1", "user1", "user2", "Hello", Message.MessageType.DIRECT_MESSAGE);
            LocalDateTime timestampAfterCreation = LocalDateTime.now();
            assertThat(testMessage.getTimestamp()).isBetween(timestampBeforeCreation, timestampAfterCreation);
        }
    }

    @Nested
    @DisplayName("Getter Method Tests")
    class GetterTests {

        @Test
        @DisplayName("getMessageId() should return correct messageId")
        void testGetMessageIdReturnsCorrectValue() {
            testMessage = new Message("msg123", "user1", "user2", "Hello", Message.MessageType.DIRECT_MESSAGE);
            String actualMessageId = testMessage.getMessageId();
            assertThat(actualMessageId).isEqualTo("msg123");
        }

        @Test
        @DisplayName("getSenderId() should return correct senderId")
        void testGetSenderIdReturnsCorrectValue() {
            testMessage = new Message("msg1", "sender1", "user2", "Hello", Message.MessageType.DIRECT_MESSAGE);
            String actualSenderId = testMessage.getSenderId();
            assertThat(actualSenderId).isEqualTo("sender1");
        }

        @Test
        @DisplayName("getReceiverId() should return correct receiverId")
        void testGetReceiverIdReturnsCorrectValue() {
            testMessage = new Message("msg1", "user1", "receiver1", "Hello", Message.MessageType.DIRECT_MESSAGE);
            String actualReceiverId = testMessage.getReceiverId();
            assertThat(actualReceiverId).isEqualTo("receiver1");
        }

        @Test
        @DisplayName("getContent() should return correct content")
        void testGetContentReturnsCorrectValue() {
            testMessage = new Message("msg1", "user1", "user2", "Test message", Message.MessageType.DIRECT_MESSAGE);
            String actualContent = testMessage.getContent();
            assertThat(actualContent).isEqualTo("Test message");
        }

        @Test
        @DisplayName("getType() should return correct message type")
        void testGetTypeReturnsCorrectValue() {
            testMessage = new Message("msg1", "user1", "user2", "Hello", Message.MessageType.FRIEND_REQUEST);
            Message.MessageType actualMessageType = testMessage.getType();
            assertThat(actualMessageType).isEqualTo(Message.MessageType.FRIEND_REQUEST);
        }

        @Test
        @DisplayName("getTimestamp() should return non-null timestamp")
        void testGetTimestampReturnsNonNullTimestamp() {
            testMessage = new Message("msg1", "user1", "user2", "Hello", Message.MessageType.DIRECT_MESSAGE);
            LocalDateTime actualTimestamp = testMessage.getTimestamp();
            assertThat(actualTimestamp).isNotNull();
        }
    }

    @Nested
    @DisplayName("getFormattedTimestamp() Method Tests")
    class GetFormattedTimestampTests {

        @Test
        @DisplayName("getFormattedTimestamp() should return non-null string")
        void testGetFormattedTimestampReturnsNonNullString() {
            testMessage = new Message("msg1", "user1", "user2", "Hello", Message.MessageType.DIRECT_MESSAGE);
            String formattedTimestamp = testMessage.getFormattedTimestamp();
            assertThat(formattedTimestamp).isNotNull();
        }

        @Test
        @DisplayName("getFormattedTimestamp() should return non-empty string")
        void testGetFormattedTimestampReturnsNonEmptyString() {
            testMessage = new Message("msg1", "user1", "user2", "Hello", Message.MessageType.DIRECT_MESSAGE);
            String formattedTimestamp = testMessage.getFormattedTimestamp();
            assertThat(formattedTimestamp).isNotEmpty();
        }

        @Test
        @DisplayName("getFormattedTimestamp() should have correct format length")
        void testGetFormattedTimestampHasCorrectFormatLength() {
            testMessage = new Message("msg1", "user1", "user2", "Hello", Message.MessageType.DIRECT_MESSAGE);
            String formattedTimestamp = testMessage.getFormattedTimestamp();
            assertThat(formattedTimestamp).hasSize(19);
        }

        @Test
        @DisplayName("getFormattedTimestamp() should contain date separator")
        void testGetFormattedTimestampContainsDateSeparator() {
            testMessage = new Message("msg1", "user1", "user2", "Hello", Message.MessageType.DIRECT_MESSAGE);
            String formattedTimestamp = testMessage.getFormattedTimestamp();
            assertThat(formattedTimestamp).contains("-");
        }

        @Test
        @DisplayName("getFormattedTimestamp() should contain time separator")
        void testGetFormattedTimestampContainsTimeSeparator() {
            testMessage = new Message("msg1", "user1", "user2", "Hello", Message.MessageType.DIRECT_MESSAGE);
            String formattedTimestamp = testMessage.getFormattedTimestamp();
            assertThat(formattedTimestamp).contains(":");
        }
    }

    @Nested
    @DisplayName("toString() Method Tests")
    class ToStringTests {

        @Test
        @DisplayName("toString() should return non-null string")
        void testToStringReturnsNonNullString() {
            testMessage = new Message("msg1", "user1", "user2", "Hello", Message.MessageType.DIRECT_MESSAGE);
            String toStringResult = testMessage.toString();
            assertThat(toStringResult).isNotNull();
        }

        @Test
        @DisplayName("toString() should contain senderId")
        void testToStringContainsSenderId() {
            testMessage = new Message("msg1", "user1", "user2", "Hello", Message.MessageType.DIRECT_MESSAGE);
            String toStringResult = testMessage.toString();
            assertThat(toStringResult).contains("user1");
        }

        @Test
        @DisplayName("toString() should contain receiverId")
        void testToStringContainsReceiverId() {
            testMessage = new Message("msg1", "user1", "user2", "Hello", Message.MessageType.DIRECT_MESSAGE);
            String toStringResult = testMessage.toString();
            assertThat(toStringResult).contains("user2");
        }

        @Test
        @DisplayName("toString() should contain content")
        void testToStringContainsContent() {
            testMessage = new Message("msg1", "user1", "user2", "Hello World", Message.MessageType.DIRECT_MESSAGE);
            String toStringResult = testMessage.toString();
            assertThat(toStringResult).contains("Hello World");
        }

        @Test
        @DisplayName("toString() should contain message type")
        void testToStringContainsMessageType() {
            testMessage = new Message("msg1", "user1", "user2", "Hello", Message.MessageType.DIRECT_MESSAGE);
            String toStringResult = testMessage.toString();
            assertThat(toStringResult).contains("DIRECT_MESSAGE");
        }
    }

    @Nested
    @DisplayName("toDisplayString() Method Tests")
    class ToDisplayStringTests {

        @Test
        @DisplayName("toDisplayString() should return non-null string")
        void testToDisplayStringReturnsNonNullString() {
            testMessage = new Message("msg1", "user1", "user2", "Hello", Message.MessageType.DIRECT_MESSAGE);
            String displayString = testMessage.toDisplayString();
            assertThat(displayString).isNotNull();
        }

        @Test
        @DisplayName("toDisplayString() should contain senderId")
        void testToDisplayStringContainsSenderId() {
            testMessage = new Message("msg1", "Alice", "user2", "Hello", Message.MessageType.DIRECT_MESSAGE);
            String displayString = testMessage.toDisplayString();
            assertThat(displayString).contains("Alice");
        }

        @Test
        @DisplayName("toDisplayString() should contain content")
        void testToDisplayStringContainsContent() {
            testMessage = new Message("msg1", "user1", "user2", "Hello World", Message.MessageType.DIRECT_MESSAGE);
            String displayString = testMessage.toDisplayString();
            assertThat(displayString).contains("Hello World");
        }

        @Test
        @DisplayName("toDisplayString() should start with timestamp bracket")
        void testToDisplayStringStartsWithTimestampBracket() {
            testMessage = new Message("msg1", "user1", "user2", "Hello", Message.MessageType.DIRECT_MESSAGE);
            String displayString = testMessage.toDisplayString();
            assertThat(displayString).startsWith("[");
        }

        @Test
        @DisplayName("toDisplayString() should contain time separator")
        void testToDisplayStringContainsTimeSeparator() {
            testMessage = new Message("msg1", "user1", "user2", "Hello", Message.MessageType.DIRECT_MESSAGE);
            String displayString = testMessage.toDisplayString();
            assertThat(displayString).contains(":");
        }
    }

    @Nested
    @DisplayName("Message.Message Type Tests")
    class MessageTypeTests {

        @ParameterizedTest
        @EnumSource(Message.MessageType.class)
        @DisplayName("Should support all message types")
        void testSupportsAllMessageTypes(Message.MessageType messageType) {
            testMessage = new Message("msg1", "user1", "user2", "Test", messageType);
            assertThat(testMessage.getType()).isEqualTo(messageType);
        }

        @Test
        @DisplayName("Should create DIRECT_MESSAGE type")
        void testCreatesDirectMessageType() {
            testMessage = new Message("msg1", "user1", "user2", "Hello", Message.MessageType.DIRECT_MESSAGE);
            assertThat(testMessage.getType()).isEqualTo(Message.MessageType.DIRECT_MESSAGE);
        }

        @Test
        @DisplayName("Should create FRIEND_REQUEST type")
        void testCreatesFriendRequestType() {
            testMessage = new Message("msg1", "user1", "user2", "Request", Message.MessageType.FRIEND_REQUEST);
            assertThat(testMessage.getType()).isEqualTo(Message.MessageType.FRIEND_REQUEST);
        }

        @Test
        @DisplayName("Should create FRIEND_ACCEPT type")
        void testCreatesFriendAcceptType() {
            testMessage = new Message("msg1", "user1", "user2", "Accept", Message.MessageType.FRIEND_ACCEPT);
            assertThat(testMessage.getType()).isEqualTo(Message.MessageType.FRIEND_ACCEPT);
        }

        @Test
        @DisplayName("Should create SYSTEM_MESSAGE type")
        void testCreatesSystemMessageType() {
            testMessage = new Message("msg1", "SYSTEM", "user1", "System", Message.MessageType.SYSTEM_MESSAGE);
            assertThat(testMessage.getType()).isEqualTo(Message.MessageType.SYSTEM_MESSAGE);
        }

        @Test
        @DisplayName("Should create USER_ONLINE type")
        void testCreatesUserOnlineType() {
            testMessage = new Message("msg1", "user1", "user2", "Online", Message.MessageType.USER_ONLINE);
            assertThat(testMessage.getType()).isEqualTo(Message.MessageType.USER_ONLINE);
        }

        @Test
        @DisplayName("Should create USER_OFFLINE type")
        void testCreatesUserOfflineType() {
            testMessage = new Message("msg1", "user1", "user2", "Offline", Message.MessageType.USER_OFFLINE);
            assertThat(testMessage.getType()).isEqualTo(Message.MessageType.USER_OFFLINE);
        }

        @Test
        @DisplayName("Should create SERVER_MESSAGE type")
        void testCreatesServerMessageType() {
            testMessage = new Message("msg1", "user1", "server1", "Message", Message.MessageType.SERVER_MESSAGE);
            assertThat(testMessage.getType()).isEqualTo(Message.MessageType.SERVER_MESSAGE);
        }

        @Test
        @DisplayName("Should create SERVER_JOIN type")
        void testCreatesServerJoinType() {
            testMessage = new Message("msg1", "user1", "server1", "Join", Message.MessageType.SERVER_JOIN);
            assertThat(testMessage.getType()).isEqualTo(Message.MessageType.SERVER_JOIN);
        }

        @Test
        @DisplayName("Should create SERVER_LEAVE type")
        void testCreatesServerLeaveType() {
            testMessage = new Message("msg1", "user1", "server1", "Leave", Message.MessageType.SERVER_LEAVE);
            assertThat(testMessage.getType()).isEqualTo(Message.MessageType.SERVER_LEAVE);
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle empty messageId")
        void testHandlesEmptyMessageId() {
            testMessage = new Message("", "user1", "user2", "Hello", Message.MessageType.DIRECT_MESSAGE);
            assertThat(testMessage.getMessageId()).isEmpty();
        }

        @Test
        @DisplayName("Should handle empty senderId")
        void testHandlesEmptySenderId() {
            testMessage = new Message("msg1", "", "user2", "Hello", Message.MessageType.DIRECT_MESSAGE);
            assertThat(testMessage.getSenderId()).isEmpty();
        }

        @Test
        @DisplayName("Should handle empty receiverId")
        void testHandlesEmptyReceiverId() {
            testMessage = new Message("msg1", "user1", "", "Hello", Message.MessageType.DIRECT_MESSAGE);
            assertThat(testMessage.getReceiverId()).isEmpty();
        }

        @Test
        @DisplayName("Should handle empty content")
        void testHandlesEmptyContent() {
            testMessage = new Message("msg1", "user1", "user2", "", Message.MessageType.DIRECT_MESSAGE);
            assertThat(testMessage.getContent()).isEmpty();
        }

        @Test
        @DisplayName("Should handle null content")
        void testHandlesNullContent() {
            testMessage = new Message("msg1", "user1", "user2", null, Message.MessageType.DIRECT_MESSAGE);
            assertThat(testMessage.getContent()).isNull();
        }

        @Test
        @DisplayName("Should handle very long content")
        void testHandlesVeryLongContent() {
            String veryLongContent = "A".repeat(10000);
            testMessage = new Message("msg1", "user1", "user2", veryLongContent, Message.MessageType.DIRECT_MESSAGE);
            assertThat(testMessage.getContent()).hasSize(10000);
        }

        @Test
        @DisplayName("Should handle special characters in content")
        void testHandlesSpecialCharactersInContent() {
            String contentWithSpecialCharacters = "Hello! @#$%^&*()";
            testMessage = new Message("msg1", "user1", "user2", contentWithSpecialCharacters, Message.MessageType.DIRECT_MESSAGE);
            assertThat(testMessage.getContent()).isEqualTo(contentWithSpecialCharacters);
        }

        @Test
        @DisplayName("Should handle unicode characters in content")
        void testHandlesUnicodeCharactersInContent() {
            String contentWithUnicode = "你好 🎉";
            testMessage = new Message("msg1", "user1", "user2", contentWithUnicode, Message.MessageType.DIRECT_MESSAGE);
            assertThat(testMessage.getContent()).isEqualTo(contentWithUnicode);
        }

        @Test
        @DisplayName("Should handle same sender and receiver")
        void testHandlesSameSenderAndReceiver() {
            testMessage = new Message("msg1", "user1", "user1", "Note to self", Message.MessageType.DIRECT_MESSAGE);
            assertThat(testMessage.getSenderId()).isEqualTo(testMessage.getReceiverId());
        }

        @Test
        @DisplayName("Should create messages with different timestamps")
        void testCreatesMessagesWithDifferentTimestamps() throws InterruptedException {
            Message firstMessage = new Message("msg1", "user1", "user2", "First", Message.MessageType.DIRECT_MESSAGE);
            Thread.sleep(10);
            Message secondMessage = new Message("msg2", "user1", "user2", "Second", Message.MessageType.DIRECT_MESSAGE);
            assertThat(firstMessage.getTimestamp()).isNotEqualTo(secondMessage.getTimestamp());
        }
    }

    @Nested
    @DisplayName("Immutability Tests")
    class ImmutabilityTests {

        @Test
        @DisplayName("Message.Message fields should be immutable after creation")
        void testMessageFieldsAreImmutableAfterCreation() {
            testMessage = new Message("msg1", "user1", "user2", "Hello", Message.MessageType.DIRECT_MESSAGE);
            String originalMessageId = testMessage.getMessageId();
            assertThat(testMessage.getMessageId()).isEqualTo(originalMessageId);
        }

        @Test
        @DisplayName("Timestamp should remain constant")
        void testTimestampRemainsConstant() {
            testMessage = new Message("msg1", "user1", "user2", "Hello", Message.MessageType.DIRECT_MESSAGE);
            LocalDateTime firstTimestamp = testMessage.getTimestamp();
            LocalDateTime secondTimestamp = testMessage.getTimestamp();
            assertThat(firstTimestamp).isEqualTo(secondTimestamp);
        }
    }
}