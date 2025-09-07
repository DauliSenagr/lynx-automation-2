package utils.users;

import com.adda52.logging.Logging;
import models.user.User;

/**
 * @author Dauli Sengar
 * @since 28th May 2022
 */

public class UserManager implements Logging {

    protected final static  ThreadLocal<User> users = new ThreadLocal<>();

    public static User getUser() {
        return users.get();
    }

    public static void setUser(User user) {
        users.set(user);
    }

    public static void quitUser(User user) {
        UserFactory.addUser(user);
        users.remove();
    }


}
