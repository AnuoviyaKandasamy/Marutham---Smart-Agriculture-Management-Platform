package com.marutham.controller;

import com.google.gson.Gson;
import com.marutham.dao.PriceRequestDAO;
import com.marutham.model.PriceRequest;
import com.marutham.model.User;
import com.marutham.util.JsonUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/price-requests")
public class PriceRequestServlet extends HttpServlet {
    private final PriceRequestDAO dao = new PriceRequestDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null) { response.setStatus(401); return; }
        User user = (User) session.getAttribute("user");
        if (user == null) { response.setStatus(401); return; }

        response.setContentType("application/json");
        String cropName = request.getParameter("avg");
        if (cropName != null) {
            double avg = dao.getAverageRequestedPrice(cropName);
            response.getWriter().write("{\"averagePrice\":" + avg + "}");
            return;
        }

        if ("ADMIN".equals(user.getRole())) {
            List<PriceRequest> all = dao.getAllPendingRequests();
            response.getWriter().write(gson.toJson(all));
        } else {
            List<PriceRequest> mine = dao.getFarmerRequests(user.getUserId());
            response.getWriter().write(gson.toJson(mine));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null) { response.setStatus(401); return; }
        User user = (User) session.getAttribute("user");
        if (user == null) { response.setStatus(401); return; }

        String action = request.getParameter("action");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if ("submit".equals(action)) {
            String cropName = request.getParameter("cropName");
            String priceStr = request.getParameter("requestedPrice");
            String quantity = request.getParameter("quantity");

            if (cropName == null || cropName.trim().isEmpty()) {
                JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Crop name is required");
                return;
            }
            double requestedPrice;
            try {
                requestedPrice = Double.parseDouble(priceStr);
                if (requestedPrice <= 0) throw new NumberFormatException("Price must be positive");
            } catch (NumberFormatException e) {
                JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid price value");
                return;
            }

            PriceRequest pr = new PriceRequest();
            pr.setFarmerId(user.getUserId());
            pr.setCropName(cropName.trim());
            pr.setRequestedPrice(requestedPrice);
            pr.setQuantity(quantity);
            boolean success = dao.submitRequest(pr);
            response.getWriter().write("{\"success\":" + success + "}");

        } else if ("process".equals(action) && "ADMIN".equals(user.getRole())) {

            int requestId;
            double adminPrice;
            try {
                requestId = Integer.parseInt(request.getParameter("requestId"));
                adminPrice = Double.parseDouble(request.getParameter("adminPrice"));
                if (adminPrice <= 0) throw new NumberFormatException("Admin price must be positive");
            } catch (NumberFormatException e) {
                JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid request ID or price");
                return;
            }
            boolean success = dao.updateStatus(requestId, "PROCESSED", adminPrice);
            response.getWriter().write("{\"success\":" + success + "}");
        } else {
            JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid action or permission denied");
        }
    }
}
