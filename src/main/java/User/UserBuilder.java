package User;

import Message.Message;
import java.util.*;

public class UserBuilder {
    private static final String EMPTY_STRING = "";
    private static final boolean DEFAULT_ONLINE_STATUS = false;

    private String userId;
    private String username;
    private String email;
    private Set<String> friendIds;
    private Map<String, List<Message>> directMessages;
    private boolean online;

    public UserBuilder() {
        this.userId = EMPTY_STRING;
        this.username = EMPTY_STRING;
        this.email = EMPTY_STRING;
        this.friendIds = new HashSet<>();
        this.directMessages = new HashMap<>();
        this.online = DEFAULT_ONLINE_STATUS;
    }

    public UserBuilder setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public UserBuilder setUsername(String username) {
        this.username = username;
        return this;
    }

    public UserBuilder setEmail(String email) {
        this.email = email;
        return this;
    }

    public UserBuilder setFriendIds(Set<String> friendIds) {
        this.friendIds = friendIds;
        return this;
    }

    public UserBuilder setDirectMessages(Map<String, List<Message>> directMessages) {
        this.directMessages = directMessages;
        return this;
    }

    public UserBuilder setOnline(boolean online) {
        this.online = online;
        return this;
    }

    public User build() {
        return new User(userId, username, email, friendIds, directMessages, online);
    }
}
