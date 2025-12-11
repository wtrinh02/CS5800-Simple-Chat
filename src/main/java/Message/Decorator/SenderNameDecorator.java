package Message.Decorator;

import Message.Message;

public class SenderNameDecorator extends MessageDecorator {

    private final Message message;

    public SenderNameDecorator(MessageComponent wrappee, Message message) {
        super(wrappee);
        this.message = message;
    }

    @Override
    public String getContent() {
        return message.getSenderId() + ": " + wrappee.getContent();
    }
}
