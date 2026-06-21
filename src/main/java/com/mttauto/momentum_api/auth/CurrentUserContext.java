package com.mttauto.momentum_api.auth;

import com.mttauto.momentum_api.user.User;

public final class CurrentUserContext {

    private static final ThreadLocal<User> CURRENT_USER = new ThreadLocal<>();

    private CurrentUserContext() {
    }

    public static void set(User user) {
        CURRENT_USER.set(user);
    }

    public static User get() {
        User user = CURRENT_USER.get();
        if (user == null) {
            throw new IllegalStateException("Authenticated user is required");
        }

        return user;
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}
