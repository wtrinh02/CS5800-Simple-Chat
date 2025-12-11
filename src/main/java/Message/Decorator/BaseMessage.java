package Message.Decorator;

import Message.Message;

public class BaseMessage implements MessageComponent {

    private final Message message;

    public BaseMessage(Message message) {
        this.message = message;
    }

    @Override
    public String getContent() {
        return message.getContent();
    }

    public Message getMessage() {
        return message;
    }
}
