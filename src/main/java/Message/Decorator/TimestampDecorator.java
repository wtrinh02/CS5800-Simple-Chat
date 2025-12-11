package Message.Decorator;

import Message.Message;

public class TimestampDecorator extends MessageDecorator {

    private final Message message;

    public TimestampDecorator(MessageComponent wrappee, Message message) {
        super(wrappee);
        this.message = message;
    }

    @Override
    public String getContent() {
        return "[" + message.getFormattedTimestamp() + "] " + wrappee.getContent();
    }
}
