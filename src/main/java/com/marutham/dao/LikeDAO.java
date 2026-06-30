package com.marutham.dao;

import com.marutham.util.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class LikeDAO {
    private static final Logger logger = LoggerFactory.getLogger(LikeDAO.class);

    public int getLikeCount(int postId) {
        String sql = "SELECT COUNT(*) AS cnt FROM likes WHERE post_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, postId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("cnt");
            }
        } catch (SQLException e) {
            logger.error("Error getting like count for post ID: {}", postId, e);
        }
        return 0;
    }

    public boolean toggleLike(int postId, int userId) {
        String checkSql = "SELECT 1 FROM likes WHERE post_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection()) {
            boolean liked;
            try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
                ps.setInt(1, postId);
                ps.setInt(2, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    liked = rs.next();
                }
            }
            if (liked) {
                String deleteSql = "DELETE FROM likes WHERE post_id = ? AND user_id = ?";
                try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                    ps.setInt(1, postId);
                    ps.setInt(2, userId);
                    ps.executeUpdate();
                }
                return false;
            } else {
                String insertSql = "INSERT INTO likes (post_id, user_id) VALUES (?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    ps.setInt(1, postId);
                    ps.setInt(2, userId);
                    ps.executeUpdate();
                }
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error toggling like on post ID {} by user ID {}", postId, userId, e);
        }
        return false;
    }
}
