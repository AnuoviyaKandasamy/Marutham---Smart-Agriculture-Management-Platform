package com.marutham.dao;

import com.marutham.util.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class CropDAO {
    private static final Logger logger = LoggerFactory.getLogger(CropDAO.class);

    private static final List<String> ALLOWED_COLUMNS = List.of("soil_type", "season", "water_requirement");

    public List<String> getRecommendedCrops(String soilType, String season, String water) {
        List<String> crops = new ArrayList<>();
        String sql = "SELECT DISTINCT crop_name FROM crops WHERE soil_type = ? AND season = ? AND water_requirement = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, soilType);
            ps.setString(2, season);
            ps.setString(3, water);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) crops.add(rs.getString("crop_name"));
            }
        } catch (SQLException e) {
            logger.error("Error getting recommended crops", e);
        }
        return crops;
    }

    public Map<String, List<String>> getCategorizedRecommendations(String soilType, String season, String water) {
        Map<String, List<String>> results = new LinkedHashMap<>();
        List<String> exact = getRecommendedCrops(soilType, season, water);
        if (!exact.isEmpty()) results.put("Optimal Match", exact);

        List<String> bySoil = getCropsByCriteria("soil_type", soilType);
        if (!bySoil.isEmpty()) results.put("Best for " + soilType + " Soil", bySoil);

        List<String> bySeason = getCropsByCriteria("season", season);
        if (!bySeason.isEmpty()) results.put("Suitable for " + season, bySeason);

        List<String> byWater = getCropsByCriteria("water_requirement", water);
        if (!byWater.isEmpty()) results.put("Water-Adaptive Options (" + water + ")", byWater);

        return results;
    }

    private List<String> getCropsByCriteria(String column, String value) {
        // FIX 1: Validate column name against whitelist before using in query
        if (!ALLOWED_COLUMNS.contains(column)) {
            logger.warn("Rejected invalid column name in getCropsByCriteria: {}", column);
            return Collections.emptyList();
        }
        List<String> crops = new ArrayList<>();
        String sql = "SELECT DISTINCT crop_name FROM crops WHERE " + column + " = ? LIMIT 5";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) crops.add(rs.getString("crop_name"));
            }
        } catch (SQLException e) {
            logger.error("Error getting crops by {}", column, e);
        }
        return crops;
    }

    public boolean saveRecommendationHistory(int userId, String soilType, String season,
                                              String water, String region, String recommendedCrops) {
        String sql = "INSERT INTO crop_recommendations (user_id, soil_type, season, water_availability, region, recommended_crops) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, soilType);
            ps.setString(3, season);
            ps.setString(4, water);
            ps.setString(5, region);
            ps.setString(6, recommendedCrops);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error saving recommendation history for user ID: {}", userId, e);
        }
        return false;
    }

    public List<Map<String, Object>> getHistoryForUser(int userId) {
        List<Map<String, Object>> history = new ArrayList<>();
        String sql = "SELECT * FROM crop_recommendations WHERE user_id = ? ORDER BY created_at DESC LIMIT 20";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("recommendationId", rs.getInt("recommendation_id"));
                    row.put("soilType", rs.getString("soil_type"));
                    row.put("season", rs.getString("season"));
                    row.put("waterAvailability", rs.getString("water_availability"));
                    row.put("region", rs.getString("region"));
                    row.put("recommendedCrops", rs.getString("recommended_crops"));
                    row.put("createdAt", rs.getTimestamp("created_at"));
                    history.add(row);
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting history for user ID: {}", userId, e);
        }
        return history;
    }
}
