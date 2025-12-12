import Message.Message;
import Message.Decorator.BaseMessage;
import Message.Decorator.MessageComponent;
import Message.Decorator.SenderNameDecorator;
import Message.Decorator.TimestampDecorator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MessageDecoratorTest {

    @Test
    void baseMessageShouldReturnUnderlyingContent() {
        Message message = new Message("id-1", "s1", "r1", "decorated", Message.MessageType.DIRECT_MESSAGE);
        MessageComponent base = new BaseMessage(message);
        boolean returnsUnderlyingContent = "decorated".equals(base.getContent());
        assertEquals(true, returnsUnderlyingContent);
    }

    @Test
    void senderNameDecoratorShouldPrefixSenderNameAndColon() {
        Message message = new Message("id-2", "Alice", "r2", "hello", Message.MessageType.DIRECT_MESSAGE);
        MessageComponent base = new BaseMessage(message);
        MessageComponent decorated = new SenderNameDecorator(base, message);
        String content = decorated.getContent();
        boolean isPrefixedWithSender = content.startsWith("Alice: ") && content.endsWith("hello");
        assertEquals(true, isPrefixedWithSender);
    }

    @Test
    void timestampDecoratorShouldPrefixFormattedTimestamp() {
        Message message = new Message("id-3", "s3", "r3", "hi", Message.MessageType.DIRECT_MESSAGE);
        message.setTimestamp(System.currentTimeMillis());
        MessageComponent base = new BaseMessage(message);
        MessageComponent decorated = new TimestampDecorator(base, message);
        String content = decorated.getContent();
        String ts = message.getFormattedTimestamp();
        boolean isPrefixedWithTimestamp = content.startsWith("[" + ts + "] ") && content.endsWith("hi");
        assertEquals(true, isPrefixedWithTimestamp);
    }
}
