package com.marutham.dao;

import com.marutham.model.PriceRequest;
import com.marutham.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PriceRequestDAO {
    
    public boolean submitRequest(PriceRequest req) {
        String sql = "INSERT INTO price_requests (farmer_id, crop_name, requested_price, quantity) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, req.getFarmerId());
            ps.setString(2, req.getCropName());
            ps.setDouble(3, req.getRequestedPrice());
            ps.setString(4, req.getQuantity());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<PriceRequest> getFarmerRequests(int farmerId) {
        List<PriceRequest> list = new ArrayList<>();
        String sql = "SELECT * FROM price_requests WHERE farmer_id = ? ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, farmerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<PriceRequest> getAllPendingRequests() {
        List<PriceRequest> list = new ArrayList<>();
        String sql = "SELECT pr.*, u.full_name FROM price_requests pr JOIN users u ON pr.farmer_id = u.user_id WHERE pr.status = 'PENDING' ORDER BY pr.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PriceRequest req = mapResultSet(rs);
                    req.setFarmerName(rs.getString("full_name"));
                    list.add(req);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public double getAverageRequestedPrice(String cropName) {
        String sql = "SELECT AVG(requested_price) FROM price_requests WHERE crop_name = ? AND status = 'PENDING'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cropName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean updateStatus(int requestId, String status, Double adminPrice) {
        String sql = "UPDATE price_requests SET status = ?, admin_price = ? WHERE request_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            if (adminPrice != null) ps.setDouble(2, adminPrice);
            else ps.setNull(2, Types.DECIMAL);
            ps.setInt(3, requestId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private PriceRequest mapResultSet(ResultSet rs) throws SQLException {
        PriceRequest req = new PriceRequest();
        req.setRequestId(rs.getInt("request_id"));
        req.setFarmerId(rs.getInt("farmer_id"));
        req.setCropName(rs.getString("crop_name"));
        req.setRequestedPrice(rs.getDouble("requested_price"));
        req.setQuantity(rs.getString("quantity"));
        req.setStatus(rs.getString("status"));
        req.setAdminPrice(rs.getObject("admin_price") != null ? rs.getDouble("admin_price") : null);
        req.setCreatedAt(rs.getTimestamp("created_at"));
        return req;
    }
}
