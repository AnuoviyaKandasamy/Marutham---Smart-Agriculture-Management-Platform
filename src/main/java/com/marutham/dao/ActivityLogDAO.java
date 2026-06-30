package com.marutham.dao;

import com.marutham.model.ActivityLog;
import com.marutham.util.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogDAO {
    private static final Logger logger = LoggerFactory.getLogger(ActivityLogDAO.class);

    public void log(Integer userId, String action, String description, String ipAddress) {
        String sql = "INSERT INTO activity_logs (user_id, action, description, ip_address) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            if (userId != null) stmt.setInt(1, userId);
            else stmt.setNull(1, Types.INTEGER);
            
            stmt.setString(2, action);
            stmt.setString(3, description);
            stmt.setString(4, ipAddress);
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error writing activity log: {}", e.getMessage());
        }
    }

    public List<ActivityLog> getRecentLogs(int limit) {
        List<ActivityLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM activity_logs ORDER BY created_at DESC LIMIT ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                ActivityLog log = new ActivityLog();
                log.setLogId(rs.getInt("log_id"));
                log.setUserId((Integer) rs.getObject("user_id"));
                log.setAction(rs.getString("action"));
                log.setDescription(rs.getString("description"));
                log.setIpAddress(rs.getString("ip_address"));
                log.setCreatedAt(rs.getTimestamp("created_at"));
                logs.add(log);
            }
        } catch (SQLException e) {
            logger.error("Error fetching logs: {}", e.getMessage());
        }
        return logs;
    }
}
