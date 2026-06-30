package com.marutham.controller;

import com.marutham.dao.UserDAO;
import com.marutham.model.User;
import com.marutham.util.JsonUtil;
import com.marutham.util.ActiveUserTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(LoginServlet.class);
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
            JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Username and password are required");
            return;
        }

        try {
            User user = userDAO.login(username, password);
            if (user != null) {
                HttpSession session = request.getSession(true);
                session.setAttribute("user", user);
                session.setAttribute("userId", user.getUserId());
                session.setAttribute("role", user.getRole());
                
                // Track as active user
                ActiveUserTracker.addUser(session.getId(), user);

                logger.info("User logged in: {}", username);

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("role", user.getRole());
                result.put("redirect", "dashboard.html");
                JsonUtil.sendAsJson(response, result);
            } else {
                logger.warn("Failed login attempt for user: {}", username);
                JsonUtil.sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid username or password");
            }
        } catch (Exception e) {
            logger.error("Error during login for user: {}", username, e);
            JsonUtil.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An internal error occurred");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Map<String, Object> result = new HashMap<>();
        
        if (session != null && session.getAttribute("user") != null) {
            result.put("loggedIn", true);
            result.put("user", session.getAttribute("user"));
        } else {
            result.put("loggedIn", false);
        }
        JsonUtil.sendAsJson(response, result);
    }
}