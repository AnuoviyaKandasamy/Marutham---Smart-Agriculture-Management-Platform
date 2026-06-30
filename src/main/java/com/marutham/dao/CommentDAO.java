package com.marutham.dao;

import com.marutham.model.Comment;
import com.marutham.util.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentDAO {
    private static final Logger logger = LoggerFactory.getLogger(CommentDAO.class);

    public boolean addComment(Comment comment) {
        String sql = "INSERT INTO comments (post_id, user_id, content) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, comment.getPostId());
            ps.setInt(2, comment.getUserId());
            ps.setString(3, comment.getContent());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error adding comment to post ID: {}", comment.getPostId(), e);
        }
        return false;
    }

    public List<Comment> getCommentsByPost(int postId) {
        List<Comment> list = new ArrayList<>();
        String sql = "SELECT c.*, u.username FROM comments c JOIN users u ON c.user_id = u.user_id WHERE c.post_id = ? ORDER BY c.created_at ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, postId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Comment c = new Comment();
                    c.setCommentId(rs.getInt("comment_id"));
                    c.setUsername(rs.getString("username"));
                    c.setContent(rs.getString("content"));
                    c.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(c);
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting comments for post ID: {}", postId, e);
        }
        return list;
    }
}