import Message.Message;

import java.util.*;

public class LocalServer {
    private static final String SYSTEM_OWNER = "SYSTEM";

    private final String serverId;
    private final String serverName;
    private final String ownerId;
    private final Set<String> members;
    private final List<Message> messages;

    public LocalServer(String serverId, String serverName, String ownerId) {
        validateConstructorParameters(serverId, serverName, ownerId);
        this.serverId = serverId;
        this.serverName = serverName;
        this.ownerId = ownerId;
        this.members = new HashSet<>();
        this.messages = new ArrayList<>();
        addOwnerAsMember();
    }

    private void validateConstructorParameters(String serverId, String serverName, String ownerId) {
        if (serverId == null || serverName == null || ownerId == null) {
            throw new IllegalArgumentException("Server parameters cannot be null");
        }
    }

    private void addOwnerAsMember() {
        members.add(ownerId);
    }

    public String getServerId() {
        return serverId;
    }

    public String getServerName() {
        return serverName;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public Set<String> getMembers() {
        return new HashSet<>(members);
    }

    public List<Message> getMessages() {
        return new ArrayList<>(messages);
    }

    public void addMember(String userId) {
        if (userId != null) {
            members.add(userId);
        }
    }

    public void removeMember(String userId) {
        if (canRemoveMember(userId)) {
            members.remove(userId);
        }
    }

    private boolean canRemoveMember(String userId) {
        return isSystemOwned() || !isOwner(userId);
    }

    private boolean isSystemOwned() {
        return SYSTEM_OWNER.equals(ownerId);
    }

    private boolean isOwner(String userId) {
        return ownerId.equals(userId);
    }

    public boolean isMember(String userId) {
        return members.contains(userId);
    }

    public void addMessage(Message message) {
        if (message != null) {
            messages.add(message);
        }
    }

    public int getMemberCount() {
            if (isSystemOwned()) {
                return Math.max(0, members.size() - 1);
            }
            return members.size();
    }

    @Override
    public String toString() {
        return String.format("LocalServer{serverId='%s', serverName='%s', ownerId='%s', memberCount=%d}",
                serverId, serverName, ownerId, members.size());
    }
}