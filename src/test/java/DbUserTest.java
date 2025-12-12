import db.model.DbUser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DbUserTest {

    @Test
    void constructorShouldPopulateRecordFields() {
        DbUser dbUser = new DbUser("id-1", "alice", "alice@example.com");
        boolean hasExpectedFields = "id-1".equals(dbUser.id())
                && "alice".equals(dbUser.username())
                && "alice@example.com".equals(dbUser.email());
        assertEquals(true, hasExpectedFields);
    }
}
