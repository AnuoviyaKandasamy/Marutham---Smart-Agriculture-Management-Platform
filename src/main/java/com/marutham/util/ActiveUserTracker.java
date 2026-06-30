package com.marutham.util;

import com.marutham.model.User;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ActiveUserTracker implements HttpSessionListener {
    private static final Map<String, User> activeUsers = new ConcurrentHashMap<>();

    public static void addUser(String sessionId, User user) {
        activeUsers.put(sessionId, user);
    }

    public static void removeUser(String sessionId) {
        activeUsers.remove(sessionId);
    }

    public static List<User> getActiveUsers() {
        return new ArrayList<>(activeUsers.values());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        activeUsers.remove(se.getSession().getId());
    }

    @Override
    public void sessionCreated(HttpSessionEvent se) {
    }
}