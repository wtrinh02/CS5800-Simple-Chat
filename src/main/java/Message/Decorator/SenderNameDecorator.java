package Message.Decorator;

import Message.Message;
import db.dao.UserDAO;

public class SenderNameDecorator extends MessageDecorator {

    private final Message message;

    public SenderNameDecorator(MessageComponent wrappee, Message message) {
        super(wrappee);
        this.message = message;
    }

    @Override
    public String getContent() {
        String senderName = UsernameResolver.resolve(message.getSenderId());
        return senderName + ": " + wrappee.getContent();
    }

}

class UsernameResolver {

    private static final UserDAO userDAO = new UserDAO();

    public static String resolve(String userId) {
        if (userId == null || userId.equals("SYSTEM"))
            return "SYSTEM";
        var dbUser = userDAO.getUserById(userId);
        return (dbUser != null) ? dbUser.username() : userId;
    }
}
