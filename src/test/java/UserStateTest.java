import User.State.AwayState;
import User.State.BusyState;
import User.State.OfflineState;
import User.State.OnlineState;
import User.State.UserState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserStateTest {

    @Test
    void onlineStateShouldExposeOnlineName() {
        UserState state = new OnlineState();
        boolean matchesName = "ONLINE".equals(state.getStateName());
        assertEquals(true, matchesName);
    }

    @Test
    void awayStateShouldExposeAwayName() {
        UserState state = new AwayState();
        boolean matchesName = "AWAY".equals(state.getStateName());
        assertEquals(true, matchesName);
    }

    @Test
    void busyStateShouldExposeBusyName() {
        UserState state = new BusyState();
        boolean matchesName = "BUSY".equals(state.getStateName());
        assertEquals(true, matchesName);
    }

    @Test
    void offlineStateShouldExposeOfflineName() {
        UserState state = new OfflineState();
        boolean matchesName = "OFFLINE".equals(state.getStateName());
        assertEquals(true, matchesName);
    }
}
