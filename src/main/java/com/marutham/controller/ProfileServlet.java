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
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/profile")
public class ProfileServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(ProfileServlet.class);
    private final UserDAO userDAO = new UserDAO();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED); return;
        }

        String type = request.getParameter("type");

        if ("experts".equals(type)) {
            String keyword = request.getParameter("q");
            List<User> experts = userDAO.searchUsers(keyword != null ? keyword : "", "EXPERT");
            JsonUtil.sendAsJson(response, experts);
            return;
        }

        int userId = (int) session.getAttribute("userId");
        User user  = userDAO.getUserById(userId);
        if (user == null) {
            JsonUtil.sendError(response, HttpServletResponse.SC_NOT_FOUND, "User not found");
            return;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("userId",         user.getUserId());
        result.put("username",       user.getUsername());
        result.put("email",          user.getEmail());
        result.put("fullName",       user.getFullName());
        result.put("phone",          user.getPhone());
        result.put("location",       user.getLocation());
        result.put("role",           user.getRole());
        result.put("profilePicture", user.getProfilePicture());
        JsonUtil.sendAsJson(response, result);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED); return;
        }
        int userId   = (int) session.getAttribute("userId");
        String action = request.getParameter("action");
        Map<String, Object> result = new HashMap<>();

        if ("update".equals(action)) {
            String fullName = request.getParameter("fullName");
            String phone    = request.getParameter("phone");
            String location = request.getParameter("location");

            if (fullName == null || fullName.trim().isEmpty()) {
                JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Full name is required");
                return;
            }
            if (phone != null && !phone.trim().isEmpty()) {
                ValidationUtil.ValidationResult phoneCheck = ValidationUtil.validatePhone(phone.trim());
                if (!phoneCheck.isValid) {
                    JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, phoneCheck.message);
                    return;
                }
            }
            boolean ok = userDAO.updateProfile(userId, fullName.trim(),
                    phone != null ? phone.trim() : "",
                    location != null ? location.trim() : "");
            result.put("success", ok);
            if (ok) {

                User updated = userDAO.getUserById(userId);
                if (updated != null) session.setAttribute("user", updated);
                result.put("message", "Profile updated successfully");
            } else {
                result.put("message", "Failed to update profile");
            }
            JsonUtil.sendAsJson(response, result);

        } else if ("changePassword".equals(action)) {
            String currentPwd = request.getParameter("currentPassword");
            String newPwd     = request.getParameter("newPassword");

            ValidationUtil.ValidationResult pwdCheck = ValidationUtil.validatePassword(newPwd);
            if (!pwdCheck.isValid) {
                JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, pwdCheck.message);
                return;
            }

            User user = (User) session.getAttribute("user");
            User verified = userDAO.login(user.getUsername(), currentPwd);
            if (verified == null) {
                JsonUtil.sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Current password is incorrect");
                return;
            }
            boolean ok = userDAO.updatePassword(userId, newPwd);
            result.put("success", ok);
            result.put("message", ok ? "Password changed successfully" : "Failed to change password");
            JsonUtil.sendAsJson(response, result);
        } else {
            JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
        }
    }
}
