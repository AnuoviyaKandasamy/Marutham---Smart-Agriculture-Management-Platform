package com.marutham.dao;

import com.marutham.model.EquipmentRequest;
import com.marutham.util.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EquipmentRequestDAO {
    private static final Logger logger = LoggerFactory.getLogger(EquipmentRequestDAO.class);

    public boolean createRequest(int equipmentId, int requesterId, String startDate, String endDate) {
        String sql = "INSERT INTO equipment_requests (equipment_id, requester_id, start_date, end_date, status) VALUES (?, ?, ?, ?, 'PENDING')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, equipmentId);
            ps.setInt(2, requesterId);
            ps.setString(3, startDate);
            ps.setString(4, endDate);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error creating equipment request for equipment ID: {} by requester ID: {}", equipmentId, requesterId, e);
        }
        return false;
    }

    public boolean updateStatus(int requestId, String status) {
        String sql = "UPDATE equipment_requests SET status = ? WHERE request_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, requestId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating status for request ID: {}", requestId, e);
        }
        return false;
    }

    public List<EquipmentRequest> getRequestsForOwner(int ownerId) {
        List<EquipmentRequest> list = new ArrayList<>();
        String sql = "SELECT r.*, e.name AS equipment_name, e.owner_id, u.full_name AS requester_name, u.phone AS requester_phone, u.email AS requester_email " +
                "FROM equipment_requests r " +
                "JOIN equipment e ON r.equipment_id = e.equipment_id " +
                "JOIN users u ON r.requester_id = u.user_id " +
                "WHERE e.owner_id = ? ORDER BY r.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ownerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToRequest(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting requests for owner ID: {}", ownerId, e);
        }
        return list;
    }

    public List<EquipmentRequest> getRequestsForFarmer(int requesterId) {
        List<EquipmentRequest> list = new ArrayList<>();
        String sql = "SELECT r.*, e.name AS equipment_name, e.owner_id, u.full_name AS requester_name, u.phone AS requester_phone, u.email AS requester_email " +
                "FROM equipment_requests r " +
                "JOIN equipment e ON r.equipment_id = e.equipment_id " +
                "JOIN users u ON r.requester_id = u.user_id " +
                "WHERE r.requester_id = ? ORDER BY r.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requesterId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToRequest(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting requests for farmer ID: {}", requesterId, e);
        }
        return list;
    }

    public EquipmentRequest getById(int requestId) {
        String sql = "SELECT r.*, e.name AS equipment_name, e.owner_id, u.full_name AS requester_name, u.phone AS requester_phone, u.email AS requester_email " +
                "FROM equipment_requests r " +
                "JOIN equipment e ON r.equipment_id = e.equipment_id " +
                "JOIN users u ON r.requester_id = u.user_id " +
                "WHERE r.request_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultSetToRequest(rs);
            }
        } catch (SQLException e) {
            logger.error("Error getting request by ID: {}", requestId, e);
        }
        return null;
    }

    private EquipmentRequest mapResultSetToRequest(ResultSet rs) throws SQLException {
        EquipmentRequest r = new EquipmentRequest();
        r.setRequestId(rs.getInt("request_id"));
        r.setEquipmentId(rs.getInt("equipment_id"));
        r.setEquipmentName(rs.getString("equipment_name"));
        r.setRequesterId(rs.getInt("requester_id"));
        r.setRequesterName(rs.getString("requester_name"));
        r.setRequesterPhone(rs.getString("requester_phone"));
        r.setRequesterEmail(rs.getString("requester_email"));
        r.setOwnerId(rs.getInt("owner_id"));
        r.setStartDate(rs.getString("start_date"));
        r.setEndDate(rs.getString("end_date"));
        r.setStatus(rs.getString("status"));
        r.setCreatedAt(rs.getTimestamp("created_at"));
        return r;
    }

    public boolean markAsReturned(int requestId, int ownerId) {
        String sql = "UPDATE equipment_requests SET status = 'RETURNED' WHERE request_id = ? AND " +
                     "equipment_id IN (SELECT equipment_id FROM equipment WHERE owner_id = ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            ps.setInt(2, ownerId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error marking request {} as returned", requestId, e);
        }
        return false;
    }
}
