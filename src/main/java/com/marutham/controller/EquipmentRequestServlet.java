package com.marutham.controller;

import com.marutham.dao.EquipmentDAO;
import com.marutham.dao.EquipmentRequestDAO;
import com.marutham.dao.NotificationDAO;
import com.marutham.model.EquipmentRequest;
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

@WebServlet("/equipment-requests")
public class EquipmentRequestServlet extends HttpServlet {
    private final EquipmentRequestDAO requestDAO   = new EquipmentRequestDAO();
    private final EquipmentDAO        equipmentDAO = new EquipmentDAO();
    private final NotificationDAO     notificationDAO = new NotificationDAO();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED); return;
        }
        int userId = (int) session.getAttribute("userId");
        String as  = request.getParameter("as");

        if ("owner".equals(as)) {
            JsonUtil.sendAsJson(response, requestDAO.getRequestsForOwner(userId));
        } else {
            JsonUtil.sendAsJson(response, requestDAO.getRequestsForFarmer(userId));
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Map<String, Object> result = new HashMap<>();

        if (session == null || session.getAttribute("userId") == null) {
            result.put("success", false); result.put("message", "Unauthorized");
            JsonUtil.sendAsJson(response, result); return;
        }

        int userId    = (int) session.getAttribute("userId");
        String action = request.getParameter("action");

        try {
            if ("book".equals(action)) {
                int equipmentId = Integer.parseInt(request.getParameter("equipmentId"));
                String startDate = request.getParameter("startDate");
                String endDate   = request.getParameter("endDate");
                boolean ok = requestDAO.createRequest(equipmentId, userId, startDate, endDate);
                if (ok) {
                    int ownerId = equipmentDAO.getOwnerId(equipmentId);
                    if (ownerId > 0) notificationDAO.createNotification(ownerId, "New equipment booking request received.");
                }
                result.put("success", ok);
                if (!ok) result.put("message", "Failed to create booking request");

            } else if ("approve".equals(action) || "reject".equals(action)) {
                int requestId = Integer.parseInt(request.getParameter("requestId"));
                EquipmentRequest req = requestDAO.getById(requestId);
                if (req == null || req.getOwnerId() != userId) {
                    result.put("success", false); result.put("message", "Not authorized");
                    JsonUtil.sendAsJson(response, result); return;
                }
                String newStatus = "approve".equals(action) ? "APPROVED" : "REJECTED";
                boolean ok = requestDAO.updateStatus(requestId, newStatus);
                if (ok) {
                    if ("APPROVED".equals(newStatus)) equipmentDAO.setAvailability(req.getEquipmentId(), false);
                    notificationDAO.createNotification(req.getRequesterId(),
                        "Your booking for '" + req.getEquipmentName() + "' was " + newStatus.toLowerCase() + ".");
                }
                result.put("success", ok);

            } else if ("return".equals(action)) {
                int requestId = Integer.parseInt(request.getParameter("requestId"));
                EquipmentRequest req = requestDAO.getById(requestId);
                if (req == null || req.getOwnerId() != userId) {
                    result.put("success", false); result.put("message", "Not authorized");
                    JsonUtil.sendAsJson(response, result); return;
                }
                boolean ok = requestDAO.markAsReturned(requestId, userId);
                if (ok) {
                    equipmentDAO.setAvailability(req.getEquipmentId(), true);
                    notificationDAO.createNotification(req.getRequesterId(),
                        "Your equipment '" + req.getEquipmentName() + "' has been marked as returned. Thank you!");
                }
                result.put("success", ok);
                if (!ok) result.put("message", "Failed to mark as returned");

            } else {
                result.put("success", false); result.put("message", "Invalid action");
            }
        } catch (NumberFormatException e) {
            result.put("success", false); result.put("message", "Invalid ID parameter");
        }
        JsonUtil.sendAsJson(response, result);
    }
}
