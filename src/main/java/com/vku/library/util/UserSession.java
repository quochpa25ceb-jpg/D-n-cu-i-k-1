package com.vku.library.util;

import com.vku.library.model.User;

public class UserSession {
    private static User loggedInUser;

    public static void setInstance(User user) {
        loggedInUser = user;
    }

    public static User getInstance() {
        return loggedInUser;
    }

    public static void cleanSession() {
        loggedInUser = null;
    }
}