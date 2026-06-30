package com.marutham.controller;

import com.marutham.dao.ConsultationDAO;
import com.marutham.dao.NotificationDAO;
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

@WebServlet("/consultation")
public class ConsultationServlet extends HttpServlet {
    private ConsultationDAO consultationDAO = new ConsultationDAO();
    private NotificationDAO notificationDAO = new NotificationDAO();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        int userId = (int) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");

        if ("EXPERT".equals(role) || "ADMIN".equals(role)) {
            JsonUtil.sendAsJson(response, consultationDAO.getPendingForExpert(userId, role));
        } else {
            JsonUtil.sendAsJson(response, consultationDAO.getConsultationsForFarmer(userId));
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        int userId = (int) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        String action = request.getParameter("action");
        Map<String, Object> result = new HashMap<>();

        if ("ask".equals(action) && "FARMER".equals(role)) {
            String question = request.getParameter("question");
            String imageUrl = request.getParameter("imageUrl");
            String targetExpertIdStr = request.getParameter("targetExpertId");
            
            Integer targetExpertId = null;
            if (targetExpertIdStr != null && !targetExpertIdStr.isEmpty()) {
                try {
                    targetExpertId = Integer.parseInt(targetExpertIdStr);
                } catch (NumberFormatException ignored) {}
            }

            if (question == null || question.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "Question cannot be empty");
            } else if (consultationDAO.askQuestion(userId, question, imageUrl, targetExpertId)) {
                result.put("success", true);
            } else {
                result.put("success", false);
                result.put("message", "Failed to submit question");
            }
        } else if ("answer".equals(action) && ("EXPERT".equals(role) || "ADMIN".equals(role))) {
            int consultationId = Integer.parseInt(request.getParameter("consultationId"));
            String answer = request.getParameter("answer");
            int farmerId = consultationDAO.getFarmerId(consultationId);
            if (consultationDAO.answerQuestion(consultationId, userId, answer)) {
                result.put("success", true);
                if (farmerId > 0) {
                    notificationDAO.createNotification(farmerId, "An expert has answered your question.");
                }
            } else {
                result.put("success", false);
                result.put("message", "Failed to submit answer");
            }
        } else {
            result.put("success", false);
            result.put("message", "Invalid action or permission denied");
        }

        JsonUtil.sendAsJson(response, result);
    }
}