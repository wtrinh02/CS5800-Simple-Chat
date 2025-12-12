import db.SchemaManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChatServerCoreTest {

    @BeforeAll
    static void initializeSchema() {
        SchemaManager.initialize();
    }

    @Test
    void userExistsShouldReturnFalseForUnknownUserId() {
        ChatServer server = new ChatServer();
        boolean exists = server.userExists("does-not-exist");
        assertEquals(false, exists);
    }
}
