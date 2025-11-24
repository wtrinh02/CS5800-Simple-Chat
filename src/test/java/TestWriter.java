import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

public class TestWriter {
    private final ByteArrayOutputStream outputStream;
    private final PrintWriter writer;

    public TestWriter() {
        outputStream = new ByteArrayOutputStream();
        writer = new PrintWriter(outputStream, true);
    }

    public PrintWriter getWriter() {
        return writer;
    }

    public String getOutput() {
        return outputStream.toString().trim();
    }
}
