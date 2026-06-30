package com.marutham.dao;

import com.marutham.model.Post;
import com.marutham.util.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostDAO {
    private static final Logger logger = LoggerFactory.getLogger(PostDAO.class);

    public boolean createPost(Post post) {
        String sql = "INSERT INTO posts (user_id, title, content, category) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, post.getUserId());
            ps.setString(2, post.getTitle());
            ps.setString(3, post.getContent());
            ps.setString(4, post.getCategory());
            int affectedRows = ps.executeUpdate();
            logger.info("Create Post: rows affected = {}", affectedRows);
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.error("Error creating post for user ID: {}", post.getUserId(), e);
        }
        return false;
    }

    public boolean updatePost(int postId, int userId, String title, String content, String category) {
        String sql = "UPDATE posts SET title = ?, content = ?, category = ? WHERE post_id = ? AND user_id = ? AND is_deleted = FALSE";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, content);
            ps.setString(3, category);
            ps.setInt(4, postId);
            ps.setInt(5, userId);
            int affectedRows = ps.executeUpdate();
            logger.info("Update Post: rows affected = {}", affectedRows);
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.error("Error updating post ID: {}", postId, e);
        }
        return false;
    }

    public boolean deletePost(int postId, int userId, boolean isAdmin) {
        // Soft delete: update is_deleted flag instead of actual deletion
        String sql = isAdmin ? "UPDATE posts SET is_deleted = TRUE WHERE post_id = ?"
                              : "UPDATE posts SET is_deleted = TRUE WHERE post_id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, postId);
            if (!isAdmin) {
                ps.setInt(2, userId);
            }
            int affectedRows = ps.executeUpdate();
            logger.info("Soft Delete Post: rows affected = {}", affectedRows);
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.error("Error deleting post ID: {}", postId, e);
        }
        return false;
    }

    public List<Post> getAllPosts() {
        return getAllPosts(0);
    }

    public List<Post> getAllPosts(int currentUserId) {
        List<Post> posts = new ArrayList<>();
        String sql = "SELECT p.*, u.username, " +
                "(SELECT COUNT(*) FROM likes l WHERE l.post_id = p.post_id) AS like_count, " +
                "EXISTS(SELECT 1 FROM likes l2 WHERE l2.post_id = p.post_id AND l2.user_id = ?) AS liked " +
                "FROM posts p JOIN users u ON p.user_id = u.user_id " +
                "WHERE p.is_deleted = FALSE " +
                "ORDER BY p.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, currentUserId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    posts.add(mapResultSetToPost(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting all posts", e);
        }
        return posts;
    }

    public List<Post> getPostsByCategory(String category, int currentUserId) {
        List<Post> posts = new ArrayList<>();
        String sql = "SELECT p.*, u.username, " +
                "(SELECT COUNT(*) FROM likes l WHERE l.post_id = p.post_id) AS like_count, " +
                "EXISTS(SELECT 1 FROM likes l2 WHERE l2.post_id = p.post_id AND l2.user_id = ?) AS liked " +
                "FROM posts p JOIN users u ON p.user_id = u.user_id " +
                "WHERE p.category = ? AND p.is_deleted = FALSE " +
                "ORDER BY p.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, currentUserId);
            ps.setString(2, category);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    posts.add(mapResultSetToPost(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting posts by category: {}", category, e);
        }
        return posts;
    }

    private Post mapResultSetToPost(ResultSet rs) throws SQLException {
        Post post = new Post();
        post.setPostId(rs.getInt("post_id"));
        post.setUserId(rs.getInt("user_id"));
        post.setUsername(rs.getString("username"));
        post.setTitle(rs.getString("title"));
        post.setContent(rs.getString("content"));
        post.setCategory(rs.getString("category"));
        post.setCreatedAt(rs.getTimestamp("created_at"));
        post.setLikeCount(rs.getInt("like_count"));
        post.setLikedByCurrentUser(rs.getBoolean("liked"));
        return post;
    }

    public List<Post> searchPosts(String keyword, int currentUserId) {
        List<Post> posts = new ArrayList<>();
        String sql = "SELECT p.*, u.username, " +
                "(SELECT COUNT(*) FROM likes l WHERE l.post_id = p.post_id) AS like_count, " +
                "EXISTS(SELECT 1 FROM likes l2 WHERE l2.post_id = p.post_id AND l2.user_id = ?) AS liked " +
                "FROM posts p JOIN users u ON p.user_id = u.user_id " +
                "WHERE p.is_deleted = FALSE AND (p.title LIKE ? OR p.content LIKE ?) " +
                "ORDER BY p.created_at DESC LIMIT 50";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String like = "%" + keyword + "%";
            ps.setInt(1, currentUserId);
            ps.setString(2, like);
            ps.setString(3, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) posts.add(mapResultSetToPost(rs));
            }
        } catch (SQLException e) {
            logger.error("Error searching posts for keyword: {}", keyword, e);
        }
        return posts;
    }
}
