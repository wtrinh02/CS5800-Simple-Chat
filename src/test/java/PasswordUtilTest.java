import org.junit.jupiter.api.Test;
import security.PasswordUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PasswordUtilTest {

    @Test
    void hashPasswordShouldReturnSameHashForSameInput() {
        String h1 = PasswordUtil.hashPassword("secret");
        String h2 = PasswordUtil.hashPassword("secret");
        boolean hashesMatch = h1.equals(h2);
        assertEquals(true, hashesMatch);
    }

    @Test
    void hashPasswordShouldNotReturnPlainText() {
        String hash = PasswordUtil.hashPassword("plain-text");
        boolean isDifferent = !"plain-text".equals(hash);
        assertEquals(true, isDifferent);
    }

    @Test
    void hashPasswordShouldTreatNullAsEmptyString() {
        String hNull = PasswordUtil.hashPassword(null);
        String hEmpty = PasswordUtil.hashPassword("");
        boolean hashesEqual = hNull.equals(hEmpty);
        assertEquals(true, hashesEqual);
    }

    @Test
    void hashPasswordShouldProduceHexCharactersOnly() {
        String hash = PasswordUtil.hashPassword("another-secret");
        boolean isHex = hash.matches("^[0-9a-f]+$");
        assertEquals(true, isHex);
    }
}
