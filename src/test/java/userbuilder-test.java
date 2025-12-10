import Message.Message;
import User.User;
import User.*;
import org.junit.jupiter.api.*;
import static org.assertj.core.api.Assertions.*;

import java.util.*;

@DisplayName("User.User.State.UserBuilder Class Tests")
class UserBuilderTest {

    private UserBuilder testUserBuilder;
    private static final String TEST_USER_ID = "user1";
    private static final String TEST_USERNAME = "Alice";
    private static final String TEST_EMAIL = "alice@test.com";

    @BeforeEach
    void setUp() {
        testUserBuilder = new UserBuilder();
    }

    @AfterEach
    void tearDown() {
        testUserBuilder = null;
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create User.User.State.UserBuilder with default values")
        void testConstructorCreatesUserBuilderWithDefaultValues() {
            testUserBuilder = new UserBuilder();
            assertThat(testUserBuilder).isNotNull();
        }

        @Test
        @DisplayName("Should build user with empty userId by default")
        void testBuildsUserWithEmptyUserIdByDefault() {
            User builtUser = testUserBuilder.build();
            assertThat(builtUser.getUserId()).isEmpty();
        }

        @Test
        @DisplayName("Should build user with empty username by default")
        void testBuildsUserWithEmptyUsernameByDefault() {
            User builtUser = testUserBuilder.build();
            assertThat(builtUser.getUsername()).isEmpty();
        }

        @Test
        @DisplayName("Should build user with empty email by default")
        void testBuildsUserWithEmptyEmailByDefault() {
            User builtUser = testUserBuilder.build();
            assertThat(builtUser.getEmail()).isEmpty();
        }

        @Test
        @DisplayName("Should build user with offline status by default")
        void testBuildsUserWithOfflineStatusByDefault() {
            User builtUser = testUserBuilder.build();
            assertThat(builtUser.isOnline()).isFalse();
        }

        @Test
        @DisplayName("Should build user with empty friend list by default")
        void testBuildsUserWithEmptyFriendListByDefault() {
            User builtUser = testUserBuilder.build();
            assertThat(builtUser.getFriendIds()).isEmpty();
        }
    }

    @Nested
    @DisplayName("setUserId() Method Tests")
    class SetUserIdTests {

        @Test
        @DisplayName("setUserId() should set userId correctly")
        void testSetUserIdSetsUserIdCorrectly() {
            User builtUser = testUserBuilder.setUserId(TEST_USER_ID).build();
            assertThat(builtUser.getUserId()).isEqualTo(TEST_USER_ID);
        }

        @Test
        @DisplayName("setUserId() should return User.User.State.UserBuilder for chaining")
        void testSetUserIdReturnsUserBuilderForChaining() {
            UserBuilder returnedBuilder = testUserBuilder.setUserId(TEST_USER_ID);
            assertThat(returnedBuilder).isSameAs(testUserBuilder);
        }

        @Test
        @DisplayName("setUserId() should handle empty userId")
        void testSetUserIdHandlesEmptyUserId() {
            User builtUser = testUserBuilder.setUserId("").build();
            assertThat(builtUser.getUserId()).isEmpty();
        }

        @Test
        @DisplayName("setUserId() should override previous userId")
        void testSetUserIdOverridesPreviousUserId() {
            User builtUser = testUserBuilder
                    .setUserId("oldId")
                    .setUserId(TEST_USER_ID)
                    .build();
            assertThat(builtUser.getUserId()).isEqualTo(TEST_USER_ID);
        }
    }

    @Nested
    @DisplayName("setUsername() Method Tests")
    class SetUsernameTests {

        @Test
        @DisplayName("setUsername() should set username correctly")
        void testSetUsernameSetsUsernameCorrectly() {
            User builtUser = testUserBuilder.setUsername(TEST_USERNAME).build();
            assertThat(builtUser.getUsername()).isEqualTo(TEST_USERNAME);
        }

        @Test
        @DisplayName("setUsername() should return User.User.State.UserBuilder for chaining")
        void testSetUsernameReturnsUserBuilderForChaining() {
            UserBuilder returnedBuilder = testUserBuilder.setUsername(TEST_USERNAME);
            assertThat(returnedBuilder).isSameAs(testUserBuilder);
        }

        @Test
        @DisplayName("setUsername() should handle empty username")
        void testSetUsernameHandlesEmptyUsername() {
            User builtUser = testUserBuilder.setUsername("").build();
            assertThat(builtUser.getUsername()).isEmpty();
        }

        @Test
        @DisplayName("setUsername() should override previous username")
        void testSetUsernameOverridesPreviousUsername() {
            User builtUser = testUserBuilder
                    .setUsername("OldName")
                    .setUsername(TEST_USERNAME)
                    .build();
            assertThat(builtUser.getUsername()).isEqualTo(TEST_USERNAME);
        }
    }

    @Nested
    @DisplayName("setEmail() Method Tests")
    class SetEmailTests {

        @Test
        @DisplayName("setEmail() should set email correctly")
        void testSetEmailSetsEmailCorrectly() {
            User builtUser = testUserBuilder.setEmail(TEST_EMAIL).build();
            assertThat(builtUser.getEmail()).isEqualTo(TEST_EMAIL);
        }

        @Test
        @DisplayName("setEmail() should return User.User.State.UserBuilder for chaining")
        void testSetEmailReturnsUserBuilderForChaining() {
            UserBuilder returnedBuilder = testUserBuilder.setEmail(TEST_EMAIL);
            assertThat(returnedBuilder).isSameAs(testUserBuilder);
        }

        @Test
        @DisplayName("setEmail() should handle empty email")
        void testSetEmailHandlesEmptyEmail() {
            User builtUser = testUserBuilder.setEmail("").build();
            assertThat(builtUser.getEmail()).isEmpty();
        }

        @Test
        @DisplayName("setEmail() should override previous email")
        void testSetEmailOverridesPreviousEmail() {
            User builtUser = testUserBuilder
                    .setEmail("old@test.com")
                    .setEmail(TEST_EMAIL)
                    .build();
            assertThat(builtUser.getEmail()).isEqualTo(TEST_EMAIL);
        }
    }

    @Nested
    @DisplayName("setFriendIds() Method Tests")
    class SetFriendIdsTests {

        @Test
        @DisplayName("setFriendIds() should set friendIds correctly")
        void testSetFriendIdSetsFriendIdsCorrectly() {
            Set<String> testFriendIds = new HashSet<>(Arrays.asList("friend1", "friend2"));
            User builtUser = testUserBuilder.setFriendIds(testFriendIds).build();
            assertThat(builtUser.getFriendIds()).containsExactlyInAnyOrder("friend1", "friend2");
        }

        @Test
        @DisplayName("setFriendIds() should return User.User.State.UserBuilder for chaining")
        void testSetFriendIdsReturnsUserBuilderForChaining() {
            Set<String> testFriendIds = new HashSet<>();
            UserBuilder returnedBuilder = testUserBuilder.setFriendIds(testFriendIds);
            assertThat(returnedBuilder).isSameAs(testUserBuilder);
        }

        @Test
        @DisplayName("setFriendIds() should handle empty set")
        void testSetFriendIdsHandlesEmptySet() {
            Set<String> emptyFriendIds = new HashSet<>();
            User builtUser = testUserBuilder.setFriendIds(emptyFriendIds).build();
            assertThat(builtUser.getFriendIds()).isEmpty();
        }

        @Test
        @DisplayName("setFriendIds() should override previous friendIds")
        void testSetFriendIdsOverridesPreviousFriendIds() {
            Set<String> oldFriendIds = new HashSet<>(Arrays.asList("old1", "old2"));
            Set<String> newFriendIds = new HashSet<>(Arrays.asList("new1", "new2"));
            User builtUser = testUserBuilder
                    .setFriendIds(oldFriendIds)
                    .setFriendIds(newFriendIds)
                    .build();
            assertThat(builtUser.getFriendIds()).containsExactlyInAnyOrder("new1", "new2");
        }
    }

    @Nested
    @DisplayName("setDirectMessages() Method Tests")
    class SetDirectMessagesTests {

        @Test
        @DisplayName("setDirectMessages() should set directMessages correctly")
        void testSetDirectMessagesSetsDirectMessagesCorrectly() {
            Map<String, List<Message>> testDirectMessages = new HashMap<>();
            List<Message> messageList = new ArrayList<>();
            messageList.add(new Message("msg1", "user1", "user2", "Hello", Message.MessageType.DIRECT_MESSAGE));
            testDirectMessages.put("conv1", messageList);
            
            User builtUser = testUserBuilder.setDirectMessages(testDirectMessages).build();
            assertThat(builtUser.getDirectMessages("conv1")).hasSize(1);
        }

        @Test
        @DisplayName("setDirectMessages() should return User.User.State.UserBuilder for chaining")
        void testSetDirectMessagesReturnsUserBuilderForChaining() {
            Map<String, List<Message>> testDirectMessages = new HashMap<>();
            UserBuilder returnedBuilder = testUserBuilder.setDirectMessages(testDirectMessages);
            assertThat(returnedBuilder).isSameAs(testUserBuilder);
        }

        @Test
        @DisplayName("setDirectMessages() should handle empty map")
        void testSetDirectMessagesHandlesEmptyMap() {
            Map<String, List<Message>> emptyDirectMessages = new HashMap<>();
            User builtUser = testUserBuilder.setDirectMessages(emptyDirectMessages).build();
            assertThat(builtUser.getDirectMessages("anyConversation")).isEmpty();
        }
    }

    @Nested
    @DisplayName("setOnline() Method Tests")
    class SetOnlineTests {

        @Test
        @DisplayName("setOnline(true) should set online status to true")
        void testSetOnlineTrueSetsOnlineStatusToTrue() {
            User builtUser = testUserBuilder.setOnline(true).build();
            assertThat(builtUser.isOnline()).isTrue();
        }

        @Test
        @DisplayName("setOnline(false) should set online status to false")
        void testSetOnlineFalseSetsOnlineStatusToFalse() {
            User builtUser = testUserBuilder.setOnline(false).build();
            assertThat(builtUser.isOnline()).isFalse();
        }

        @Test
        @DisplayName("setOnline() should return User.User.State.UserBuilder for chaining")
        void testSetOnlineReturnsUserBuilderForChaining() {
            UserBuilder returnedBuilder = testUserBuilder.setOnline(true);
            assertThat(returnedBuilder).isSameAs(testUserBuilder);
        }

        @Test
        @DisplayName("setOnline() should override previous online status")
        void testSetOnlineOverridesPreviousOnlineStatus() {
            User builtUser = testUserBuilder
                    .setOnline(true)
                    .setOnline(false)
                    .build();
            assertThat(builtUser.isOnline()).isFalse();
        }
    }

    @Nested
    @DisplayName("build() Method Tests")
    class BuildTests {

        @Test
        @DisplayName("build() should create User.User with all set properties")
        void testBuildCreatesUserWithAllSetProperties() {
            User builtUser = testUserBuilder
                    .setUserId(TEST_USER_ID)
                    .setUsername(TEST_USERNAME)
                    .setEmail(TEST_EMAIL)
                    .setOnline(true)
                    .build();
            
            assertThat(builtUser.getUserId()).isEqualTo(TEST_USER_ID);
        }

        @Test
        @DisplayName("build() should return non-null User.User")
        void testBuildReturnsNonNullUser() {
            User builtUser = testUserBuilder.build();
            assertThat(builtUser).isNotNull();
        }

        @Test
        @DisplayName("build() should create User.User instance")
        void testBuildCreatesUserInstance() {
            User builtUser = testUserBuilder.build();
            assertThat(builtUser).isInstanceOf(User.class);
        }
    }

    @Nested
    @DisplayName("Method Chaining Tests")
    class MethodChainingTests {

        @Test
        @DisplayName("Should support full method chaining")
        void testSupportsFullMethodChaining() {
            Set<String> testFriendIds = new HashSet<>(Arrays.asList("friend1"));
            Map<String, List<Message>> testDirectMessages = new HashMap<>();
            
            User builtUser = testUserBuilder
                    .setUserId(TEST_USER_ID)
                    .setUsername(TEST_USERNAME)
                    .setEmail(TEST_EMAIL)
                    .setFriendIds(testFriendIds)
                    .setDirectMessages(testDirectMessages)
                    .setOnline(true)
                    .build();
            
            assertThat(builtUser).isNotNull();
        }

        @Test
        @DisplayName("Should allow partial method chaining")
        void testAllowsPartialMethodChaining() {
            User builtUser = testUserBuilder
                    .setUserId(TEST_USER_ID)
                    .setUsername(TEST_USERNAME)
                    .build();
            
            assertThat(builtUser.getUserId()).isEqualTo(TEST_USER_ID);
        }

        @Test
        @DisplayName("Should allow methods in any order")
        void testAllowsMethodsInAnyOrder() {
            User builtUser = testUserBuilder
                    .setOnline(true)
                    .setEmail(TEST_EMAIL)
                    .setUserId(TEST_USER_ID)
                    .setUsername(TEST_USERNAME)
                    .build();
            
            assertThat(builtUser.getUsername()).isEqualTo(TEST_USERNAME);
        }
    }

    @Nested
    @DisplayName("Multiple Build Tests")
    class MultipleBuildTests {

        @Test
        @DisplayName("Should allow building multiple users from same builder")
        void testAllowsBuildingMultipleUsersFromSameBuilder() {
            testUserBuilder.setUserId(TEST_USER_ID).setUsername(TEST_USERNAME);
            
            User firstUser = testUserBuilder.build();
            User secondUser = testUserBuilder.build();
            
            assertThat(firstUser).isNotSameAs(secondUser);
        }

        @Test
        @DisplayName("Should create independent user instances")
        void testCreatesIndependentUserInstances() {
            User firstUser = testUserBuilder.setUserId("user1").build();
            User secondUser = testUserBuilder.setUserId("user2").build();
            
            assertThat(firstUser.getUserId()).isNotEqualTo(secondUser.getUserId());
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle null friendIds by using reference")
        void testHandlesNullFriendIdsByUsingReference() {
            User builtUser = testUserBuilder.setFriendIds(null).build();
            assertThat(builtUser.getFriendIds()).isEmpty();
        }

        @Test
        @DisplayName("Should handle null directMessages by using reference")
        void testHandlesNullDirectMessagesByUsingReference() {
            User builtUser = testUserBuilder.setDirectMessages(null).build();
            assertThat(builtUser.getDirectMessages("anyConversation")).isEmpty();
        }

        @Test
        @DisplayName("Should handle special characters in fields")
        void testHandlesSpecialCharactersInFields() {
            String specialUsername = "User.User!@#$%";
            User builtUser = testUserBuilder.setUsername(specialUsername).build();
            assertThat(builtUser.getUsername()).isEqualTo(specialUsername);
        }

        @Test
        @DisplayName("Should handle very long strings")
        void testHandlesVeryLongStrings() {
            String longUsername = "A".repeat(1000);
            User builtUser = testUserBuilder.setUsername(longUsername).build();
            assertThat(builtUser.getUsername()).hasSize(1000);
        }
    }
}