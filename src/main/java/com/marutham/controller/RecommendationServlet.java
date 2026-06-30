package com.marutham.controller;

import com.marutham.dao.CropDAO;
import com.marutham.util.JsonUtil;
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
import java.util.stream.Collectors;

@WebServlet("/recommendation")
public class RecommendationServlet extends HttpServlet {
    private CropDAO cropDAO = new CropDAO();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        int userId = (int) session.getAttribute("userId");
        JsonUtil.sendAsJson(response, cropDAO.getHistoryForUser(userId));
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String soil = request.getParameter("soilType");
        String season = request.getParameter("season");
        String water = request.getParameter("water");
        String region = request.getParameter("region");

        Map<String, List<String>> categorized = cropDAO.getCategorizedRecommendations(soil, season, water);

        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("userId") != null) {
            int userId = (int) session.getAttribute("userId");
            String summary = categorized.getOrDefault("Optimal Match", List.of("N/A")).stream().collect(Collectors.joining(", "));
            cropDAO.saveRecommendationHistory(userId, soil, season, water, region, summary);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("categorized", categorized);
        JsonUtil.sendAsJson(response, result);
    }
}