package com.marutham.controller;

import com.marutham.dao.AdminDAO;
import com.marutham.dao.PostDAO;
import com.marutham.util.JsonUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/admin")
public class AdminServlet extends HttpServlet {
    private final AdminDAO adminDAO = new AdminDAO();
    private final PostDAO  postDAO  = new PostDAO();

    private boolean isAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && "ADMIN".equals(session.getAttribute("role"));
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!isAdmin(request)) { response.sendError(HttpServletResponse.SC_FORBIDDEN); return; }
        String type = request.getParameter("type");
        if ("users".equals(type)) {
            String roleFilter = request.getParameter("role");
            JsonUtil.sendAsJson(response, adminDAO.getAllUsers(roleFilter));
        } else if ("active_users".equals(type)) {
            JsonUtil.sendAsJson(response, adminDAO.getActiveUsers());
        } else if ("posts".equals(type)) {
            JsonUtil.sendAsJson(response, postDAO.getAllPosts());
        } else if ("equipment".equals(type)) {
            JsonUtil.sendAsJson(response, adminDAO.getAllEquipment());
        } else if ("stats".equals(type)) {
            JsonUtil.sendAsJson(response, adminDAO.getSummaryStats());
        } else if ("logs".equals(type)) {
            // FIX 8: Activity logs endpoint now exposed
            String limitStr = request.getParameter("limit");
            int limit = 100;
            try { if (limitStr != null) limit = Integer.parseInt(limitStr); } catch (NumberFormatException ignored) {}
            JsonUtil.sendAsJson(response, adminDAO.getActivityLogs(limit));
        } else {
            JsonUtil.sendAsJson(response, adminDAO.getSummaryStats());
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, Object> result = new HashMap<>();
        if (!isAdmin(request)) {
            result.put("success", false); result.put("message", "Forbidden");
            JsonUtil.sendAsJson(response, result); return;
        }

        String action = request.getParameter("action");
        boolean ok = false;
        try {
            switch (action == null ? "" : action) {
                case "deleteUser":
                    ok = adminDAO.deleteUser(Integer.parseInt(request.getParameter("userId")));
                    break;
                case "updateRole":
                    ok = adminDAO.updateUserRole(Integer.parseInt(request.getParameter("userId")),
                            request.getParameter("role"));
                    break;
                case "deletePost":
                    ok = postDAO.deletePost(Integer.parseInt(request.getParameter("postId")), 0, true);
                    break;
                case "deleteEquipment":
                    ok = adminDAO.deleteEquipment(Integer.parseInt(request.getParameter("equipmentId")));
                    break;
                default:
                    result.put("message", "Unknown action");
            }
        } catch (NumberFormatException e) {
            result.put("success", false);
            result.put("message", "Invalid ID parameter");
            JsonUtil.sendAsJson(response, result);
            return;
        }
        result.put("success", ok);
        JsonUtil.sendAsJson(response, result);
    }
}
