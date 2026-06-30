package com.marutham.dao;

import com.marutham.model.Equipment;
import com.marutham.util.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EquipmentDAO {
    private static final Logger logger = LoggerFactory.getLogger(EquipmentDAO.class);

    public List<Equipment> getAllAvailable() {
        List<Equipment> list = new ArrayList<>();
        String sql = "SELECT e.*, u.full_name FROM equipment e JOIN users u ON e.owner_id = u.user_id WHERE e.availability_status = TRUE";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToEquipment(rs));
            }
        } catch (SQLException e) {
            logger.error("Error getting all available equipment", e);
        }
        return list;
    }

    public List<Equipment> getByOwner(int ownerId) {
        List<Equipment> list = new ArrayList<>();
        String sql = "SELECT e.*, u.full_name FROM equipment e JOIN users u ON e.owner_id = u.user_id WHERE e.owner_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ownerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToEquipment(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting equipment by owner ID: {}", ownerId, e);
        }
        return list;
    }

    public boolean addEquipment(Equipment eq) {
        String sql = "INSERT INTO equipment (owner_id, name, description, price_per_day) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eq.getOwnerId());
            ps.setString(2, eq.getName());
            ps.setString(3, eq.getDescription());
            ps.setDouble(4, eq.getPricePerDay());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error adding equipment for owner ID: {}", eq.getOwnerId(), e);
        }
        return false;
    }

    public int getOwnerId(int equipmentId) {
        String sql = "SELECT owner_id FROM equipment WHERE equipment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, equipmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("owner_id");
            }
        } catch (SQLException e) {
            logger.error("Error getting owner ID for equipment ID: {}", equipmentId, e);
        }
        return -1;
    }

    public boolean setAvailability(int equipmentId, boolean available) {
        String sql = "UPDATE equipment SET availability_status = ? WHERE equipment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, available);
            ps.setInt(2, equipmentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error setting availability for equipment ID: {}", equipmentId, e);
        }
        return false;
    }

    private Equipment mapResultSetToEquipment(ResultSet rs) throws SQLException {
        Equipment eq = new Equipment();
        eq.setEquipmentId(rs.getInt("equipment_id"));
        eq.setOwnerId(rs.getInt("owner_id"));
        eq.setName(rs.getString("name"));
        eq.setDescription(rs.getString("description"));
        eq.setPricePerDay(rs.getDouble("price_per_day"));
        eq.setAvailable(rs.getBoolean("availability_status"));
        eq.setOwnerName(rs.getString("full_name"));
        return eq;
    }
}