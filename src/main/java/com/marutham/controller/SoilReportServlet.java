package com.marutham.controller;

import com.marutham.dao.SoilReportDAO;
import com.marutham.model.SoilReport;
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

@WebServlet("/soil-health")
public class SoilReportServlet extends HttpServlet {
    private SoilReportDAO soilReportDAO = new SoilReportDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            JsonUtil.sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Login required");
            return;
        }

        List<SoilReport> reports = soilReportDAO.getReportsForFarmer(user.getUserId());
        JsonUtil.sendAsJson(response, reports);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null || !"FARMER".equals(user.getRole())) {
            JsonUtil.sendError(response, HttpServletResponse.SC_FORBIDDEN, "Only farmers can add soil reports");
            return;
        }

        try {
            SoilReport report = new SoilReport();
            report.setFarmerId(user.getUserId());
            report.setSoilType(request.getParameter("soilType"));
            
            String phStr = request.getParameter("phLevel");
            if (phStr != null && !phStr.isEmpty()) {
                report.setPhLevel(new BigDecimal(phStr));
            }
            
            report.setNitrogenContent(request.getParameter("nitrogen"));
            report.setPhosphorusContent(request.getParameter("phosphorus"));
            report.setPotassiumContent(request.getParameter("potassium"));
            report.setOrganicCarbon(request.getParameter("organicCarbon"));
            report.setRecommendation(generateRecommendation(report));
            report.setReportFileUrl(request.getParameter("reportFileUrl"));

            if (soilReportDAO.addReport(report)) {
                JsonUtil.sendSuccess(response, "Soil report added successfully");
            } else {
                JsonUtil.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to save report");
            }
        } catch (Exception e) {
            JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid data: " + e.getMessage());
        }
    }

    private String generateRecommendation(SoilReport report) {
        StringBuilder rec = new StringBuilder();
        if (report.getPhLevel() != null) {
            double ph = report.getPhLevel().doubleValue();
            if (ph < 6.0) rec.append("Soil is acidic. Add lime to increase pH. ");
            else if (ph > 7.5) rec.append("Soil is alkaline. Add gypsum to decrease pH. ");
        }
        
        if ("Low".equalsIgnoreCase(report.getNitrogenContent())) {
            rec.append("Nitrogen level is low. Use Urea or organic compost. ");
        }
        
        if (rec.length() == 0) {
            rec.append("Soil parameters are within normal range. Maintain organic matter application.");
        }
        
        return rec.toString();
    }
}
