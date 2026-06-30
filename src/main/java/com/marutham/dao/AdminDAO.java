package com.marutham.dao;

import com.marutham.model.User;
import com.marutham.util.ActiveUserTracker;
import com.marutham.util.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

public class AdminDAO {
    private static final Logger logger = LoggerFactory.getLogger(AdminDAO.class);

    public List<Map<String, Object>> getAllUsers(String roleFilter) {
        List<Map<String, Object>> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT user_id, username, email, full_name, phone, role, created_at FROM users");
        if (roleFilter != null && !roleFilter.isEmpty() && !"ALL".equalsIgnoreCase(roleFilter)) {
            sql.append(" WHERE role = ?");
        }
        sql.append(" ORDER BY created_at DESC");
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (roleFilter != null && !roleFilter.isEmpty() && !"ALL".equalsIgnoreCase(roleFilter)) {
                ps.setString(1, roleFilter.toUpperCase());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("userId",     rs.getInt("user_id"));
                    row.put("username",   rs.getString("username"));
                    row.put("email",      rs.getString("email"));
                    row.put("fullName",   rs.getString("full_name"));
                    row.put("phone",      rs.getString("phone"));
                    row.put("role",       rs.getString("role"));
                    row.put("createdAt",  rs.getTimestamp("created_at"));
                    list.add(row);
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting all users with filter: {}", roleFilter, e);
        }
        return list;
    }


    public boolean deleteUser(int userId) {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {

                for (String sql : new String[]{
                    "DELETE FROM activity_logs WHERE user_id = ?",
                    "UPDATE posts SET is_deleted = TRUE WHERE user_id = ?",
                    "DELETE FROM comments WHERE user_id = ?",
                    "DELETE FROM likes WHERE user_id = ?",
                    "DELETE FROM notifications WHERE user_id = ?",
                    "DELETE FROM equipment_requests WHERE requester_id = ?",
                    "DELETE FROM crop_recommendations WHERE user_id = ?",
                    "DELETE FROM consultations WHERE farmer_id = ? OR expert_id = ?"
                }) {
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setInt(1, userId);
                        if (sql.contains("expert_id")) ps.setInt(2, userId);
                        ps.executeUpdate();
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE user_id = ?")) {
                    ps.setInt(1, userId);
                    int affected = ps.executeUpdate();
                    conn.commit();
                    return affected > 0;
                }
            } catch (SQLException e) {
                conn.rollback();
                logger.error("Error deleting user ID: {} - rolled back", userId, e);
            }
        } catch (SQLException e) {
            logger.error("Error getting connection to delete user ID: {}", userId, e);
        }
        return false;
    }

    public List<Map<String, Object>> getActivityLogs(int limit) {
        List<Map<String, Object>> logs = new ArrayList<>();
        String sql = "SELECT l.*, u.username FROM activity_logs l LEFT JOIN users u ON l.user_id = u.user_id ORDER BY l.created_at DESC LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> log = new LinkedHashMap<>();
                    log.put("logId",       rs.getInt("log_id"));
                    log.put("username",    rs.getString("username") != null ? rs.getString("username") : "Guest");
                    log.put("action",      rs.getString("action"));
                    log.put("description", rs.getString("description"));
                    log.put("ipAddress",   rs.getString("ip_address"));
                    log.put("createdAt",   rs.getTimestamp("created_at"));
                    logs.add(log);
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting activity logs", e);
        }
        return logs;
    }

    public boolean updateUserRole(int userId, String role) {
        String sql = "UPDATE users SET role = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, role);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating role for user ID: {}", userId, e);
        }
        return false;
    }

    public List<Map<String, Object>> getAllEquipment() {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT e.*, u.full_name FROM equipment e JOIN users u ON e.owner_id = u.user_id ORDER BY e.equipment_id DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("equipmentId",        rs.getInt("equipment_id"));
                row.put("name",               rs.getString("name"));
                row.put("description",        rs.getString("description"));
                row.put("pricePerDay",        rs.getDouble("price_per_day"));
                row.put("availabilityStatus", rs.getBoolean("availability_status"));
                row.put("ownerName",          rs.getString("full_name"));
                list.add(row);
            }
        } catch (SQLException e) {
            logger.error("Error getting all equipment", e);
        }
        return list;
    }

    public boolean deleteEquipment(int equipmentId) {
        String sql = "DELETE FROM equipment WHERE equipment_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, equipmentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting equipment ID: {}", equipmentId, e);
        }
        return false;
    }

    public Map<String, Object> getSummaryStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalUsers",         countRows("users",         "1=1"));
        stats.put("totalPosts",         countRows("posts",         "1=1"));
        stats.put("totalEquipment",     countRows("equipment",     "1=1"));
        stats.put("totalConsultations", countRows("consultations", "1=1"));
        stats.putAll(getUserRoleCounts());
        return stats;
    }

    public Map<String, Long> getUserRoleCounts() {
        Map<String, Long> roleCounts = new LinkedHashMap<>();
        String sql = "SELECT role, COUNT(*) AS count FROM users GROUP BY role";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                roleCounts.put(rs.getString("role").toLowerCase() + "Count", rs.getLong("count"));
            }
        } catch (SQLException e) {
            logger.error("Error getting user role counts", e);
        }
        return roleCounts;
    }

    public List<User> getActiveUsers() {
        return ActiveUserTracker.getActiveUsers();
    }

    private int countRows(String table, String condition) {
        String sql = "SELECT COUNT(*) AS cnt FROM " + table + " WHERE " + condition;
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt("cnt");
        } catch (SQLException e) {
            logger.error("Error counting rows in table: {}", table, e);
        }
        return 0;
    }
}
