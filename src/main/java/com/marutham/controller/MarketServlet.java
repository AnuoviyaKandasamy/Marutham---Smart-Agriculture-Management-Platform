package com.marutham.controller;

import com.marutham.dao.MarketDAO;
import com.marutham.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/market")
public class MarketServlet extends HttpServlet {
    private final MarketDAO marketDAO = new MarketDAO();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");


        if ("history".equals(action)) {
            String cropName = request.getParameter("crop");
            String daysStr  = request.getParameter("days");
            if (cropName == null || cropName.trim().isEmpty()) {
                JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Crop name required");
                return;
            }
            int days = 30;
            try { if (daysStr != null) days = Integer.parseInt(daysStr); } catch (NumberFormatException ignored) {}
            JsonUtil.sendAsJson(response, marketDAO.getPriceHistory(cropName.trim(), days));
            return;
        }


        JsonUtil.sendAsJson(response, marketDAO.getAllPrices());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String cropName = request.getParameter("cropName");
        String priceStr = request.getParameter("price");
        String market   = request.getParameter("market");
        String state    = request.getParameter("state");

        if (cropName == null || priceStr == null) {
            JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "cropName and price are required");
            return;
        }
        try {
            double price = Double.parseDouble(priceStr);

            boolean ok = marketDAO.upsertPrice(cropName, price, market, state);
            if (ok) JsonUtil.sendSuccess(response, "Price updated");
            else JsonUtil.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Update failed");
        } catch (NumberFormatException e) {
            JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid price value");
        }
    }
}
