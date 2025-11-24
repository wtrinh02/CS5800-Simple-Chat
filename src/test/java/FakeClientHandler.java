import java.util.ArrayList;
import java.util.List;

public class FakeClientHandler {

    private final List<String> receivedMessages = new ArrayList<>();

    public void sendMessage(String message) {
        receivedMessages.add(message);
    }

    public List<String> getReceivedMessages() {
        return receivedMessages;
    }

    public String getLastMessage() {
        if (receivedMessages.isEmpty()) {
            return null;
        }
        return receivedMessages.get(receivedMessages.size() - 1);
    }
}
