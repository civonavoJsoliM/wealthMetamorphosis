package app.wealthmetamorphosis.data.singleton;

import app.wealthmetamorphosis.data.User;

public class UserSingleton {
    private static User user;

    private UserSingleton() {
    }

    public static synchronized User getInstance() {
        return user;
    }

    public static void setUser(User user) {
        UserSingleton.user = user;
    }
}
