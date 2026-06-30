package com.marutham.controller;

import com.marutham.dao.EquipmentDAO;
import com.marutham.model.Equipment;
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

@WebServlet("/equipment")
public class EquipmentServlet extends HttpServlet {
    private EquipmentDAO equipmentDAO = new EquipmentDAO();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String mine = request.getParameter("mine");
        if ("true".equals(mine)) {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("userId") == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            int ownerId = (int) session.getAttribute("userId");
            JsonUtil.sendAsJson(response, equipmentDAO.getByOwner(ownerId));
        } else {
            JsonUtil.sendAsJson(response, equipmentDAO.getAllAvailable());
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Map<String, Object> result = new HashMap<>();

        if (session == null || session.getAttribute("userId") == null) {
            result.put("success", false);
            result.put("message", "Unauthorized");
            JsonUtil.sendAsJson(response, result);
            return;
        }

        int ownerId = (int) session.getAttribute("userId");
        String name = request.getParameter("name");
        String description = request.getParameter("description");
        double price;
        try {
            price = Double.parseDouble(request.getParameter("pricePerDay"));
        } catch (NumberFormatException e) {
            result.put("success", false);
            result.put("message", "Invalid price");
            JsonUtil.sendAsJson(response, result);
            return;
        }

        Equipment eq = new Equipment();
        eq.setOwnerId(ownerId);
        eq.setName(name);
        eq.setDescription(description);
        eq.setPricePerDay(price);

        if (equipmentDAO.addEquipment(eq)) {
            result.put("success", true);
        } else {
            result.put("success", false);
            result.put("message", "Failed to add equipment");
        }
        JsonUtil.sendAsJson(response, result);
    }
}