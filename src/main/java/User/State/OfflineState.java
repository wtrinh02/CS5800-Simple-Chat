package User.State;

public class OfflineState implements UserState {
    @Override
    public String getStateName() {
        return "OFFLINE";
    }
}
