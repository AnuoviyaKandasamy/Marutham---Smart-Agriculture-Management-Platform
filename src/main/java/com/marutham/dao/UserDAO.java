package com.marutham.dao;

import com.marutham.model.User;
import com.marutham.util.DBConnection;
import com.marutham.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);
    
    public boolean isUsernameExists(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.error("Error checking if username exists: {}", username, e);
        }
        return false;
    }

    public boolean isEmailExists(String email) {
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.error("Error checking if email exists: {}", email, e);
        }
        return false;
    }

    public boolean registerUser(User user) {
        String sql = "INSERT INTO users (username, password, email, full_name, phone, location, role, profile_picture) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, PasswordUtil.hashPassword(user.getPassword()));
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getFullName());
            ps.setString(5, user.getPhone());
            ps.setString(6, user.getLocation() != null ? user.getLocation() : "Central Market");
            ps.setString(7, user.getRole());
            ps.setString(8, user.getProfilePicture() != null ? user.getProfilePicture() : "default_dp.png");
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error registering user: {}", user.getUsername(), e);
        }
        return false;
    }

    public User login(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String hashedPassword = rs.getString("password");
                    if (PasswordUtil.checkPassword(password, hashedPassword)) {
                        return mapResultSetToUser(rs);
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error during login for user: {}", username, e);
        }
        return null;
    }

    public User getUserById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting user by ID: {}", userId, e);
        }
        return null;
    }

    public List<User> getFarmersByLocation(String location) {
        List<User> farmers = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = 'FARMER' AND location = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, location);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    farmers.add(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting farmers by location: {}", location, e);
        }
        return farmers;
    }

    public List<String> getAllUniqueLocations() {
        List<String> locations = new ArrayList<>();
        String sql = "SELECT DISTINCT location FROM users WHERE location IS NOT NULL";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                locations.add(rs.getString("location"));
            }
        } catch (SQLException e) {
            logger.error("Error getting unique locations", e);
        }
        return locations;
    }

    public boolean updateProfilePicture(int userId, String profilePicture) {
        String sql = "UPDATE users SET profile_picture = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, profilePicture);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating profile picture for user ID: {}", userId, e);
        }
        return false;
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setFullName(rs.getString("full_name"));
        user.setRole(rs.getString("role"));
        user.setPhone(rs.getString("phone"));
        user.setLocation(rs.getString("location"));
        user.setProfilePicture(rs.getString("profile_picture"));
        return user;
    }

    public boolean updateProfile(int userId, String fullName, String phone, String location) {
        String sql = "UPDATE users SET full_name = ?, phone = ?, location = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fullName);
            ps.setString(2, phone);
            ps.setString(3, location);
            ps.setInt(4, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating profile for user ID: {}", userId, e);
        }
        return false;
    }

    public boolean updatePassword(int userId, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, PasswordUtil.hashPassword(newPassword));
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating password for user ID: {}", userId, e);
        }
        return false;
    }

    public boolean savePasswordResetToken(String email, String token, long expiryMs) {
        String sql = "UPDATE users SET reset_token = ?, reset_token_expiry = ? WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, token);
            ps.setLong(2, expiryMs);
            ps.setString(3, email);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error saving reset token for email: {}", email, e);
        }
        return false;
    }

    public boolean resetPasswordWithToken(String token, String newPassword) {
        String checkSql = "SELECT user_id, reset_token_expiry FROM users WHERE reset_token = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setString(1, token);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long expiry = rs.getLong("reset_token_expiry");
                    if (System.currentTimeMillis() > expiry) {
                        logger.warn("Reset token expired");
                        return false;
                    }
                    int userId = rs.getInt("user_id");
                    String updateSql = "UPDATE users SET password = ?, reset_token = NULL, reset_token_expiry = NULL WHERE user_id = ?";
                    try (PreparedStatement upPs = conn.prepareStatement(updateSql)) {
                        upPs.setString(1, PasswordUtil.hashPassword(newPassword));
                        upPs.setInt(2, userId);
                        return upPs.executeUpdate() > 0;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error resetting password with token", e);
        }
        return false;
    }

    public List<User> searchUsers(String keyword, String role) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE (username LIKE ? OR full_name LIKE ?) AND role = ? LIMIT 20";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String like = "%" + keyword + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, role);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            logger.error("Error searching users with keyword: {}", keyword, e);
        }
        return users;
    }

    public List<User> getAllExperts() {
        List<User> experts = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = 'EXPERT' ORDER BY full_name ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                experts.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            logger.error("Error getting all experts", e);
        }
        return experts;
    }
}
