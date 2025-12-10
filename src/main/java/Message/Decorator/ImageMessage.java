package Message.Decorator;

import Message.Message;

public class ImageMessage extends MessageDecorator {
    private final String imagePath;

    public ImageMessage(Message wrapped, String imagePath) {
        super(wrapped);
        this.imagePath = imagePath;
    }

    public String getImagePath() {
        return imagePath;
    }

    @Override
    public String toDisplayString() {
        return wrapped.toDisplayString() + " [Image: " + imagePath + "]";
    }
}
