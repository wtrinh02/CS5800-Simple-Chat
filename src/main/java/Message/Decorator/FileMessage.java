package Message.Decorator;
import Message.Message;

public class FileMessage extends MessageDecorator {
    private final String filePath;

    public FileMessage(Message wrapped, String filePath) {
        super(wrapped);
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    @Override
    public String toDisplayString() {
        return wrapped.toDisplayString() + " [File: " + filePath + "]";
    }
}