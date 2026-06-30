package com.marutham.controller;

import com.marutham.dao.UserDAO;
import com.marutham.model.User;
import com.marutham.util.JsonUtil;
import com.marutham.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(RegisterServlet.class);
    private final UserDAO userDAO = new UserDAO();

    private static final List<String> ALLOWED_ROLES = List.of("FARMER", "EXPERT");

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String fullName = request.getParameter("fullName");
        String email    = request.getParameter("email");
        String phone    = request.getParameter("phone");
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String role     = request.getParameter("role");

        if (isEmpty(fullName) || isEmpty(email) || isEmpty(username) || isEmpty(password) || isEmpty(role)) {
            JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "All required fields must be filled");
            return;
        }

        ValidationUtil.ValidationResult usernameCheck = ValidationUtil.validateUsername(username);
        if (!usernameCheck.isValid) {
            JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, usernameCheck.message);
            return;
        }

        ValidationUtil.ValidationResult emailCheck = ValidationUtil.validateEmail(email);
        if (!emailCheck.isValid) {
            JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, emailCheck.message);
            return;
        }

        ValidationUtil.ValidationResult passwordCheck = ValidationUtil.validatePassword(password);
        if (!passwordCheck.isValid) {
            JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, passwordCheck.message);
            return;
        }

        if (!isEmpty(phone)) {
            ValidationUtil.ValidationResult phoneCheck = ValidationUtil.validatePhone(phone);
            if (!phoneCheck.isValid) {
                JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, phoneCheck.message);
                return;
            }
        }

        if (!ALLOWED_ROLES.contains(role.toUpperCase())) {
            JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid role selected");
            return;
        }

        try {
            if (userDAO.isUsernameExists(username)) {
                JsonUtil.sendError(response, HttpServletResponse.SC_CONFLICT, "Username already exists");
            } else if (userDAO.isEmailExists(email)) {
                JsonUtil.sendError(response, HttpServletResponse.SC_CONFLICT, "Email already registered");
            } else {
                User user = new User(username, password, email, fullName, phone, role.toUpperCase());
                if (userDAO.registerUser(user)) {
                    logger.info("New user registered: {}", username);
                    JsonUtil.sendSuccess(response, "Registration successful! Please login.");
                } else {
                    JsonUtil.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Registration failed due to server error");
                }
            }
        } catch (Exception e) {
            logger.error("Error during registration for user: {}", username, e);
            JsonUtil.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "An internal error occurred");
        }
    }

    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}
