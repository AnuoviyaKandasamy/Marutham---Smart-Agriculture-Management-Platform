package com.marutham.dao;

import com.marutham.model.MarketPrice;
import com.marutham.util.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

public class MarketDAO {
    private static final Logger logger = LoggerFactory.getLogger(MarketDAO.class);

    public List<MarketPrice> getAllPrices() {
        List<MarketPrice> prices = new ArrayList<>();
        String sql = "SELECT * FROM market_prices ORDER BY updated_at DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                MarketPrice mp = new MarketPrice();
                mp.setPriceId(rs.getInt("price_id"));
                mp.setCropName(rs.getString("crop_name"));
                mp.setPricePerKg(rs.getDouble("price_per_kg"));
                mp.setMarketName(rs.getString("market_name"));
                mp.setState(rs.getString("state"));
                prices.add(mp);
            }
        } catch (SQLException e) {
            logger.error("Error getting all market prices", e);
        }
        return prices;
    }

    public List<MarketPrice> getPricesByMarket(String marketName) {
        List<MarketPrice> prices = new ArrayList<>();
        String sql = "SELECT * FROM market_prices WHERE market_name = ? ORDER BY updated_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, marketName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MarketPrice mp = new MarketPrice();
                    mp.setPriceId(rs.getInt("price_id"));
                    mp.setCropName(rs.getString("crop_name"));
                    mp.setPricePerKg(rs.getDouble("price_per_kg"));
                    mp.setMarketName(rs.getString("market_name"));
                    mp.setState(rs.getString("state"));
                    prices.add(mp);
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting prices for market: " + marketName, e);
        }
        return prices;
    }

    public boolean upsertPrice(String cropName, double price, String market, String state) {
        try (Connection conn = DBConnection.getConnection()) {
            Integer priceId = null;
            String effectiveMarket = market;
            String effectiveState = state;

            if (market != null && !market.trim().isEmpty()) {
                String checkSql = "SELECT price_id FROM market_prices WHERE crop_name = ? AND market_name = ?";
                try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                    ps.setString(1, cropName);
                    ps.setString(2, market);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) priceId = rs.getInt("price_id");
                    }
                }
            } else {

                String lookupSql = "SELECT price_id, market_name, state FROM market_prices WHERE crop_name = ? LIMIT 1";
                try (PreparedStatement ps = conn.prepareStatement(lookupSql)) {
                    ps.setString(1, cropName);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            priceId = rs.getInt("price_id");
                            effectiveMarket = rs.getString("market_name");
                            effectiveState = rs.getString("state");
                        }
                    }
                }
            }

            if (priceId != null) {

                String updateSql = "UPDATE market_prices SET price_per_kg = ? WHERE price_id = ?";
                try (PreparedStatement ups = conn.prepareStatement(updateSql)) {
                    ups.setDouble(1, price);
                    ups.setInt(2, priceId);
                    return ups.executeUpdate() > 0;
                }
            } else {

                if (effectiveMarket == null || effectiveMarket.trim().isEmpty()) effectiveMarket = "National Mandi";
                if (effectiveState == null || effectiveState.trim().isEmpty()) effectiveState = "India";
                
                String insertSql = "INSERT INTO market_prices (crop_name, price_per_kg, market_name, state) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ips = conn.prepareStatement(insertSql)) {
                    ips.setString(1, cropName);
                    ips.setDouble(2, price);
                    ips.setString(3, effectiveMarket);
                    ips.setString(4, effectiveState);
                    return ips.executeUpdate() > 0;
                }
            }
        } catch (SQLException e) {
            logger.error("Error upserting market price", e);
        }
        return false;
    }

    public List<Map<String, Object>> getPriceHistory(String cropName, int days) {
        List<Map<String, Object>> history = new ArrayList<>();

        String sql = "SELECT price_per_kg, updated_at FROM market_prices WHERE crop_name = ? " +
                     "AND updated_at >= DATE_SUB(NOW(), INTERVAL ? DAY) ORDER BY updated_at ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cropName);
            ps.setInt(2, days);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("price",      rs.getDouble("price_per_kg"));
                    row.put("recordedAt", rs.getTimestamp("updated_at"));
                    history.add(row);
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting price history for crop: {}", cropName, e);
        }
        return history;
    }
}
