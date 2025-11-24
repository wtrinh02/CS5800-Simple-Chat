import java.io.IOException;
import java.net.Socket;

public class TestSocket extends Socket {

    private boolean closed = false;

    @Override
    public synchronized void close() throws IOException {
        closed = true;
    }

    public boolean wasClosed() {
        return closed;
    }
}
