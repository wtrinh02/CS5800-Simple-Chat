import Message.Message;
import org.junit.jupiter.api.*;
import static org.assertj.core.api.Assertions.*;

import java.util.*;

@DisplayName("LocalServer Class Tests")
class LocalServerTest {

    private LocalServer testLocalServer;
    private static final String TEST_SERVER_ID = "server1";
    private static final String TEST_SERVER_NAME = "Gaming Server";
    private static final String TEST_OWNER_ID = "owner1";
    private static final String TEST_MEMBER_ID = "member1";

    @AfterEach
    void tearDown() {
        testLocalServer = null;
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create server with valid parameters")
        void testConstructorCreatesServerWithValidParameters() {
            testLocalServer = new LocalServer(TEST_SERVER_ID, TEST_SERVER_NAME, TEST_OWNER_ID);
            assertThat(testLocalServer).isNotNull();
        }

        @Test
        @DisplayName("Should automatically add owner as member")
        void testConstructorAutomaticallyAddsOwnerAsMember() {
            testLocalServer = new LocalServer(TEST_SERVER_ID, TEST_SERVER_NAME, TEST_OWNER_ID);
            assertThat(testLocalServer.isMember(TEST_OWNER_ID)).isTrue();
        }

        @Test
        @DisplayName("Should initialize with owner as only member")
        void testConstructorInitializesWithOwnerAsOnlyMember() {
            testLocalServer = new LocalServer(TEST_SERVER_ID, TEST_SERVER_NAME, TEST_OWNER_ID);
            assertThat(testLocalServer.getMemberCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Getter Method Tests")
    class GetterTests {

        @Test
        @DisplayName("getServerId() should return correct serverId")
        void testGetServerIdReturnsCorrectValue() {
            testLocalServer = new LocalServer(TEST_SERVER_ID, TEST_SERVER_NAME, TEST_OWNER_ID);
            String actualServerId = testLocalServer.getServerId();
            assertThat(actualServerId).isEqualTo(TEST_SERVER_ID);
        }

        @Test
        @DisplayName("getServerName() should return correct serverName")
        void testGetServerNameReturnsCorrectValue() {
            testLocalServer = new LocalServer(TEST_SERVER_ID, TEST_SERVER_NAME, TEST_OWNER_ID);
            String actualServerName = testLocalServer.getServerName();
            assertThat(actualServerName).isEqualTo(TEST_SERVER_NAME);
        }

        @Test
        @DisplayName("getOwnerId() should return correct ownerId")
        void testGetOwnerIdReturnsCorrectValue() {
            testLocalServer = new LocalServer(TEST_SERVER_ID, TEST_SERVER_NAME, TEST_OWNER_ID);
            String actualOwnerId = testLocalServer.getOwnerId();
            assertThat(actualOwnerId).isEqualTo(TEST_OWNER_ID);
        }

        @Test
        @DisplayName("getMembers() should return defensive copy")
        void testGetMembersReturnsDefensiveCopy() {
            testLocalServer = new LocalServer(TEST_SERVER_ID, TEST_SERVER_NAME, TEST_OWNER_ID);
            Set<String> firstMembersCopy = testLocalServer.getMembers();
            firstMembersCopy.add("hacker");
            Set<String> secondMembersCopy = testLocalServer.getMembers();
            assertThat(secondMembersCopy).doesNotContain("hacker");
        }

        @Test
        @DisplayName("getMessages() should return defensive copy")
        void testGetMessagesReturnsDefensiveCopy() {
            testLocalServer = new LocalServer(TEST_SERVER_ID, TEST_SERVER_NAME, TEST_OWNER_ID);
            Message hackerMessage = new Message("hack1", "hacker", TEST_SERVER_ID, "Hack", Message.MessageType.SERVER_MESSAGE);
            List<Message> firstMessagesCopy = testLocalServer.getMessages();
            firstMessagesCopy.add(hackerMessage);
            List<Message> secondMessagesCopy = testLocalServer.getMessages();
            assertThat(secondMessagesCopy).doesNotContain(hackerMessage);
        }
    }

    @Nested
    @DisplayName("addMember() Method Tests")
    class AddMemberTests {

        @Test
        @DisplayName("addMember() should add new member successfully")
        void testAddMemberAddsNewMemberSuccessfully() {
            testLocalServer = new LocalServer(TEST_SERVER_ID, TEST_SERVER_NAME, TEST_OWNER_ID);
            testLocalServer.addMember(TEST_MEMBER_ID);
            assertThat(testLocalServer.isMember(TEST_MEMBER_ID)).isTrue();
        }

        @Test
        @DisplayName("addMember() should increase member count")
        void testAddMemberIncreasesMemberCount() {
            testLocalServer = new LocalServer(TEST_SERVER_ID, TEST_SERVER_NAME, TEST_OWNER_ID);
            testLocalServer.addMember(TEST_MEMBER_ID);
            assertThat(testLocalServer.getMemberCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("addMember() should not add duplicate member")
        void testAddMemberDoesNotAddDuplicateMember() {
            testLocalServer = new LocalServer(TEST_SERVER_ID, TEST_SERVER_NAME, TEST_OWNER_ID);
            testLocalServer.addMember(TEST_MEMBER_ID);
            testLocalServer.addMember(TEST_MEMBER_ID);
            assertThat(testLocalServer.getMemberCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("addMember() should handle multiple different members")
        void testAddMemberHandlesMultipleDifferentMembers() {
            testLocalServer = new LocalServer(TEST_SERVER_ID, TEST_SERVER_NAME, TEST_OWNER_ID);
            testLocalServer.addMember("member1");
            testLocalServer.addMember("member2");
            testLocalServer.addMember("member3");
            assertThat(testLocalServer.getMemberCount()).isEqualTo(4);
        }
    }

    @Nested
    @DisplayName("removeMember() Method Tests")
    class RemoveMemberTests {

        @Test
        @DisplayName("removeMember() should remove existing member")
        void testRemoveMemberRemovesExistingMember() {
            testLocalServer = new LocalServer(TEST_SERVER_ID, TEST_SERVER_NAME, TEST_OWNER_ID);
            testLocalServer.addMember(TEST_MEMBER_ID);
            testLocalServer.removeMember(TEST_MEMBER_ID);
            assertThat(testLocalServer.isMember(TEST_MEMBER_ID)).isFalse();
        }

        @Test
        @DisplayName("removeMember() should decrease member count")
        void testRemoveMemberDecreasesMemberCount() {
            testLocalServer = new LocalServer(TEST_SERVER_ID, TEST_SERVER_NAME, TEST_OWNER_ID);
            testLocalServer.addMember(TEST_MEMBER_ID);
            testLocalServer.removeMember(TEST_MEMBER_ID);
            assertThat(testLocalServer.getMemberCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("removeMember() should not allow owner removal for user-owned server")
        void testRemoveMemberDoesNotAllowOwnerRemovalForUserOwnedServer() {
            testLocalServer = new LocalServer(TEST_SERVER_ID, TEST_SERVER_NAME, TEST_OWNER_ID);
            testLocalServer.removeMember(TEST_OWNER_ID);
            assertThat(testLocalServer.isMember(TEST_OWNER_ID)).isTrue();
        }

        @Test
        @DisplayName("removeMember() should allow owner removal for SYSTEM-owned server")
        void testRemoveMemberAllowsOwnerRemovalForSystemOwnedServer() {
            testLocalServer = new LocalServer(TEST_SERVER_ID, TEST_SERVER_NAME, "SYSTEM");
            testLocalServer.addMember("user1");
            testLocalServer.removeMember("SYSTEM");
            assertThat(testLocalServer.isMember("SYSTEM")).isFalse();
        }

        @Test
        @DisplayName("removeMember() should handle non-existent member gracefully")
        void testRemoveMemberHandlesNonExistentMemberGracefully() {
            testLocalServer = new LocalServer(TEST_SERVER_ID, TEST_SERVER_NAME, TEST_OWNER_ID);
            testLocalServer.removeMember("nonExistentMember");
            assertThat(testLocalServer.getMemberCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("removeMember() should not affect other members")
        void testRemoveMemberDoesNotAffectOtherMembers() {
            testLocalServer = new LocalServer(TEST_SERVER_ID, TEST_SERVER_NAME, TEST_OWNER_ID);
            testLocalServer.addMember("member1");
            testLocalServer.addMember("member2");
            testLocalServer.removeMember("member1");
            assertThat(testLocalServer.isMember("member2")).isTrue();
        }
    }

    @Nested
    @DisplayName("isMember() Method Tests")
    class IsMemberTests {

        @Test
        @DisplayName("isMember() should return true for existing member")
        void testIsMemberReturnsTrueForExistingMember() {
            testLocalServer = new LocalServer(TEST_SERVER_ID, TEST_SERVER_NAME, TEST_OWNER_ID);
            testLocalServer.addMember(TEST_MEMBER_ID);
            boolean isMemberResult = testLocalServer.isMember(TEST_MEMBER_ID);
            assertThat(isMemberResult).isTrue();
        }

        @Test
        @DisplayName("isMember() should return true for owner")
        void testIsMemberReturnsTrueForOwner() {
            testLocalServer = new LocalServer(TEST_SERVER_ID, TEST_SERVER_NAME, TEST_OWNER_ID);
            boolean isMemberResult = testLocalServer.isMember(TEST_OWNER_ID);
            assertThat(isMemberResult).isTrue();
        }

        @Test
        @DisplayName("isMember() should return false for non-member")
        void testIsMemberReturnsFalseForNonMember() {
            testLocalServer = new LocalServer(TEST_SERVER_ID, TEST_SERVER_NAME, TEST_OWNER_ID);
            boolean isMemberResult = testLocalServer.isMember("stranger");
            assertThat(isMemberResult).isFalse();
        }
    }

    @Nested
    @DisplayName("addMessage() Method Tests")
    class AddMessageTests {

        @Test
        @DisplayName("addMessage() should add message to empty list")
        void testAddMessageAddsMessageToEmptyList() {
            testLocalServer = new LocalServer(TEST_SERVER_ID, TEST_SERVER_NAME, TEST_OWNER_ID);
            Message testMessage = new Message("msg1", "user1", TEST_SERVER_ID, "Hello", Message.MessageType.SERVER_MESSAGE);
            testLocalServer.addMessage(testMessage);
            assertThat(testLocalServer.getMessages()).hasSize(1);
        }

        @Test
        @DisplayName("addMessage() should add message to existing list")
        void testAddMessageAddsMessageToExistingList() {
            testLocalServer = new LocalServer(TEST_SERVER_ID, TEST_SERVER_NAME, TEST_OWNER_ID);
            Message firstMessage = new Message("msg1", "user1", TEST_SERVER_ID, "Hello", Message.MessageType.SERVER_MESSAGE);
            Message secondMessage = new Message("msg2", "user2", TEST_SERVER_ID, "Hi", Message.MessageType.SERVER_MESSAGE);
            testLocalServer.addMessage(firstMessage);
            testLocalServer.addMessage(secondMessage);
            assertThat(testLocalServer.getMessages()).hasSize(2);
        }

        @Test
        @DisplayName("addMessage() should preserve message order")
        void testAddMessagePreservesMessageOrder() {
            testLocalServer = new LocalServer(TEST_SERVER_ID, TEST_SERVER_NAME, TEST_OWNER_ID);
            Message firstMessage = new Message("msg1", "user1", TEST_SERVER_ID, "First", Message.MessageType.SERVER_MESSAGE);
            Message secondMessage = new Message("msg2", "user2", TEST_SERVER_ID, "Second", Message.MessageType.SERVER_MESSAGE);
            testLocalServer.addMessage(firstMessage);
            testLocalServer.addMessage(secondMessage);
            List<Message> serverMessages = testLocalServer.getMessages();
            assertThat(serverMessages.get(0).getContent()).isEqualTo("First");
        }

        @Test
        @DisplayName("addMessage() should store actual message")
        void testAddMessageStoresActualMessage() {
            testLocalServer = new LocalServer(TEST_SERVER_ID, TEST_SERVER_NAME, TEST_OWNER_ID);
            Message testMessage = new Message("msg1", "user1", TEST_SERVER_ID, "Hello", Message.MessageType.SERVER_MESSAGE);
            testLocalServer.addMessage(testMessage);
            assertThat(testLocalServer.getMessages()).contains(testMessage);
        }
    }

    @Nested
    @DisplayName("getMemberCount() Method Tests")
    class GetMemberCountTests {

        @Test
        @DisplayName("getMemberCount() should return initial count of 1")
        void testGetMemberCountReturnsInitialCountOfOne() {
            testLocalServer = new LocalServer(TEST_SERVER_ID, TEST_SERVER_NAME, TEST_OWNER_ID);
            int memberCount = testLocalServer.getMemberCount();
            assertThat(memberCount).isEqualTo(1);
        }

        @Test
        @DisplayName("getMemberCount() should return correct count after additions")
        void testGetMemberCountReturnsCorrectCountAfterAdditions() {
            testLocalServer = new LocalServer(TEST_SERVER_ID, TEST_SERVER_NAME, TEST_OWNER_ID);
            testLocalServer.addMember("member1");
            testLocalServer.addMember("member2");
            int memberCount = testLocalServer.getMemberCount();
            assertThat(memberCount).isEqualTo(3);
        }

        @Test
        @DisplayName("getMemberCount() should return correct count after removals")
        void testGetMemberCountReturnsCorrectCountAfterRemovals() {
            testLocalServer = new LocalServer(TEST_SERVER_ID, TEST_SERVER_NAME, TEST_OWNER_ID);
            testLocalServer.addMember("member1");
            testLocalServer.addMember("member2");
            testLocalServer.removeMember("member1");
            int memberCount = testLocalServer.getMemberCount();
            assertThat(memberCount).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("toString() Method Tests")
    class ToStringTests {

        @Test
        @DisplayName("toString() should return non-null string")
        void testToStringReturnsNonNullString() {
            testLocalServer = new LocalServer(TEST_SERVER_ID, TEST_SERVER_NAME, TEST_OWNER_ID);
            String toStringResult = testLocalServer.toString();
            assertThat(toStringResult).isNotNull();
        }

        @Test
        @DisplayName("toString() should contain serverId")
        void testToStringContainsServerId() {
            testLocalServer = new LocalServer(TEST_SERVER_ID, TEST_SERVER_NAME, TEST_OWNER_ID);
            String toStringResult = testLocalServer.toString();
            assertThat(toStringResult).contains(TEST_SERVER_ID);
        }

        @Test
        @DisplayName("toString() should contain serverName")
        void testToStringContainsServerName() {
            testLocalServer = new LocalServer(TEST_SERVER_ID, TEST_SERVER_NAME, TEST_OWNER_ID);
            String toStringResult = testLocalServer.toString();
            assertThat(toStringResult).contains(TEST_SERVER_NAME);
        }

        @Test
        @DisplayName("toString() should contain ownerId")
        void testToStringContainsOwnerId() {
            testLocalServer = new LocalServer(TEST_SERVER_ID, TEST_SERVER_NAME, TEST_OWNER_ID);
            String toStringResult = testLocalServer.toString();
            assertThat(toStringResult).contains(TEST_OWNER_ID);
        }

        @Test
        @DisplayName("toString() should contain memberCount")
        void testToStringContainsMemberCount() {
            testLocalServer = new LocalServer(TEST_SERVER_ID, TEST_SERVER_NAME, TEST_OWNER_ID);
            String toStringResult = testLocalServer.toString();
            assertThat(toStringResult).contains("1");
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle empty serverId")
        void testHandlesEmptyServerId() {
            testLocalServer = new LocalServer("", TEST_SERVER_NAME, TEST_OWNER_ID);
            assertThat(testLocalServer.getServerId()).isEmpty();
        }

        @Test
        @DisplayName("Should handle empty serverName")
        void testHandlesEmptyServerName() {
            testLocalServer = new LocalServer(TEST_SERVER_ID, "", TEST_OWNER_ID);
            assertThat(testLocalServer.getServerName()).isEmpty();
        }

        @Test
        @DisplayName("Should handle empty ownerId")
        void testHandlesEmptyOwnerId() {
            testLocalServer = new LocalServer(TEST_SERVER_ID, TEST_SERVER_NAME, "");
            assertThat(testLocalServer.getOwnerId()).isEmpty();
        }

        @Test
        @DisplayName("Should handle special characters in serverName")
        void testHandlesSpecialCharactersInServerName() {
            String serverNameWithSpecialChars = "Gaming! @#$% Server";
            testLocalServer = new LocalServer(TEST_SERVER_ID, serverNameWithSpecialChars, TEST_OWNER_ID);
            assertThat(testLocalServer.getServerName()).isEqualTo(serverNameWithSpecialChars);
        }

        @Test
        @DisplayName("Should handle long serverName")
        void testHandlesLongServerName() {
            String longServerName = "A".repeat(1000);
            testLocalServer = new LocalServer(TEST_SERVER_ID, longServerName, TEST_OWNER_ID);
            assertThat(testLocalServer.getServerName()).hasSize(1000);
        }
    }
}