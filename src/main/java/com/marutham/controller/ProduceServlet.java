package com.marutham.controller;

import com.marutham.dao.ProduceDAO;
import com.marutham.model.Produce;
import com.marutham.model.User;
import com.marutham.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@WebServlet("/marketplace")
public class ProduceServlet extends HttpServlet {
    private ProduceDAO produceDAO = new ProduceDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if ("my".equals(action)) {

            if (user == null) {
                JsonUtil.sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Login required");
                return;
            }
            List<Produce> list = produceDAO.getProduceByFarmer(user.getUserId());
            JsonUtil.sendAsJson(response, list);
        } else {

            List<Produce> list = produceDAO.getAllAvailableProduce();
            JsonUtil.sendAsJson(response, list);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null || !"FARMER".equals(user.getRole())) {
            JsonUtil.sendError(response, HttpServletResponse.SC_FORBIDDEN, "Only farmers can list produce");
            return;
        }

        try {
            Produce p = new Produce();
            p.setFarmerId(user.getUserId());
            p.setCropName(request.getParameter("cropName"));
            p.setQuantity(request.getParameter("quantity"));
            p.setPriceExpected(new BigDecimal(request.getParameter("priceExpected")));
            p.setLocation(request.getParameter("location"));
            p.setDescription(request.getParameter("description"));
            p.setContactNumber(request.getParameter("contactNumber"));
            p.setImageUrl(request.getParameter("imageUrl"));
            p.setStatus("AVAILABLE");

            if (produceDAO.addProduce(p)) {
                JsonUtil.sendSuccess(response, "Produce listed successfully");
            } else {
                JsonUtil.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to list produce");
            }
        } catch (Exception e) {
            JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid data: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String produceIdStr = request.getParameter("produceId");
        String status = request.getParameter("status");

        if (produceIdStr != null && status != null) {
            int produceId = Integer.parseInt(produceIdStr);
            if (produceDAO.updateStatus(produceId, status)) {
                JsonUtil.sendSuccess(response, "Status updated");
            } else {
                JsonUtil.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Update failed");
            }
        }
    }
}
