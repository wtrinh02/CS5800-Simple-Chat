package Message.Decorator;
import Message.Message;

public abstract class MessageDecorator extends Message {
    protected final Message wrapped;

    public MessageDecorator(Message wrapped) {
        super(
                wrapped.getMessageId(),
                wrapped.getSenderId(),
                wrapped.getReceiverId(),
                wrapped.getContent(),
                wrapped.getType()
        );
        this.wrapped = wrapped;
    }

    @Override
    public String toDisplayString() {
        return wrapped.toDisplayString();
    }
}
