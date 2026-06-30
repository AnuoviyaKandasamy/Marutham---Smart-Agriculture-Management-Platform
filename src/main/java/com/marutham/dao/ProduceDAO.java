package com.marutham.dao;

import com.marutham.model.Produce;
import com.marutham.util.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProduceDAO {
    private static final Logger logger = LoggerFactory.getLogger(ProduceDAO.class);

    public boolean addProduce(Produce produce) {
        String sql = "INSERT INTO produces (farmer_id, crop_name, quantity, price_expected, location, description, contact_number, image_url) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, produce.getFarmerId());
            ps.setString(2, produce.getCropName());
            ps.setString(3, produce.getQuantity());
            ps.setBigDecimal(4, produce.getPriceExpected());
            ps.setString(5, produce.getLocation());
            ps.setString(6, produce.getDescription());
            ps.setString(7, produce.getContactNumber());
            ps.setString(8, produce.getImageUrl());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error adding produce for farmer ID: {}", produce.getFarmerId(), e);
        }
        return false;
    }

    public List<Produce> getAllAvailableProduce() {
        List<Produce> list = new ArrayList<>();
        String sql = "SELECT p.*, u.full_name as farmer_name FROM produces p JOIN users u ON p.farmer_id = u.user_id WHERE p.status = 'AVAILABLE' ORDER BY p.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToProduce(rs));
            }
        } catch (SQLException e) {
            logger.error("Error getting available produce", e);
        }
        return list;
    }

    public List<Produce> getProduceByFarmer(int farmerId) {
        List<Produce> list = new ArrayList<>();
        String sql = "SELECT p.*, u.full_name as farmer_name FROM produces p JOIN users u ON p.farmer_id = u.user_id WHERE p.farmer_id = ? ORDER BY p.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, farmerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToProduce(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting produce for farmer ID: {}", farmerId, e);
        }
        return list;
    }

    public boolean updateStatus(int produceId, String status) {
        String sql = "UPDATE produces SET status = ? WHERE produce_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, produceId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating status for produce ID: {}", produceId, e);
        }
        return false;
    }

    private Produce mapResultSetToProduce(ResultSet rs) throws SQLException {
        Produce p = new Produce();
        p.setProduceId(rs.getInt("produce_id"));
        p.setFarmerId(rs.getInt("farmer_id"));
        p.setFarmerName(rs.getString("farmer_name"));
        p.setCropName(rs.getString("crop_name"));
        p.setQuantity(rs.getString("quantity"));
        p.setPriceExpected(rs.getBigDecimal("price_expected"));
        p.setLocation(rs.getString("location"));
        p.setDescription(rs.getString("description"));
        p.setContactNumber(rs.getString("contact_number"));
        p.setImageUrl(rs.getString("image_url"));
        p.setStatus(rs.getString("status"));
        p.setCreatedAt(rs.getTimestamp("created_at"));
        return p;
    }
}
