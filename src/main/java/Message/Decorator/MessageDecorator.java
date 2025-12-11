package Message.Decorator;

public abstract class MessageDecorator implements MessageComponent {

    protected final MessageComponent wrappee;

    public MessageDecorator(MessageComponent wrappee) {
        this.wrappee = wrappee;
    }

    @Override
    public String getContent() {
        return wrappee.getContent();
    }
}
