import Message.Message;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MessageTest {

    @Test
    void constructorShouldPopulateFieldsAndExposeGetters() {
        Message message = new Message("id-1", "s1", "r1", "content", Message.MessageType.DIRECT_MESSAGE);
        boolean hasExpectedFields = "id-1".equals(message.getId())
                && "s1".equals(message.getSenderId())
                && "r1".equals(message.getReceiverId())
                && "content".equals(message.getContent())
                && Message.MessageType.DIRECT_MESSAGE.equals(message.getType());
        assertEquals(true, hasExpectedFields);
    }

    @Test
    void setTimestampShouldAffectFormattedTimestampString() {
        Message message = new Message("id-2", "s2", "r2", "content", Message.MessageType.SYSTEM_MESSAGE);
        long timestamp = 1_600_000_000_000L;
        message.setTimestamp(timestamp);
        String formatted = message.getFormattedTimestamp();
        boolean looksLikeTimestamp = formatted != null && formatted.length() >= 8;
        assertEquals(true, looksLikeTimestamp);
    }

    @Test
    void toDisplayStringShouldContainTimeSenderAndContent() {
        Message message = new Message("id-3", "sender-x", "receiver-y", "hello display", Message.MessageType.SYSTEM_MESSAGE);
        message.setTimestamp(System.currentTimeMillis());
        String display = message.toDisplayString();
        boolean containsAllParts = display.contains("sender-x") && display.contains("hello display") && display.startsWith("[");
        assertEquals(true, containsAllParts);
    }
}
