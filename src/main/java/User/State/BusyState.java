package User.State;

public class BusyState implements UserState {
    @Override
    public String getStateName() {
        return "BUSY";
    }
}
