package User.State;

public class OnlineState implements UserState {

    @Override
    public String getStateName() {
        return "ONLINE";
    }
}
