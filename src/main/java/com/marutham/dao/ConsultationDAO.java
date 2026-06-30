package com.marutham.dao;

import com.marutham.model.Consultation;
import com.marutham.util.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConsultationDAO {
    private static final Logger logger = LoggerFactory.getLogger(ConsultationDAO.class);
    
    public boolean askQuestion(int farmerId, String question, String imageUrl, Integer targetExpertId) {
        String sql = "INSERT INTO consultations (farmer_id, question, image_url, target_expert_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, farmerId);
            ps.setString(2, question);
            ps.setString(3, imageUrl);
            if (targetExpertId != null) ps.setInt(4, targetExpertId);
            else ps.setNull(4, Types.INTEGER);
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error asking question for farmer ID: {}", farmerId, e);
        }
        return false;
    }

    public List<Consultation> getConsultationsForFarmer(int farmerId) {
        List<Consultation> list = new ArrayList<>();
        String sql = "SELECT c.*, u.full_name as expert_name FROM consultations c LEFT JOIN users u ON c.expert_id = u.user_id WHERE c.farmer_id = ? ORDER BY c.created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, farmerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Consultation c = mapResultSetToConsultation(rs);
                    c.setExpertName(rs.getString("expert_name"));
                    list.add(c);
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting consultations for farmer ID: {}", farmerId, e);
        }
        return list;
    }

    public List<Consultation> getPendingForExpert(int expertId, String role) {
        List<Consultation> list = new ArrayList<>();
        String sql;

        if ("ADMIN".equals(role)) {
            sql = "SELECT c.*, u.full_name as farmer_name FROM consultations c JOIN users u ON c.farmer_id = u.user_id WHERE c.status = 'PENDING' ORDER BY c.created_at ASC";
        } else {
            sql = "SELECT c.*, u.full_name as farmer_name FROM consultations c JOIN users u ON c.farmer_id = u.user_id WHERE c.status = 'PENDING' AND (c.target_expert_id IS NULL OR c.target_expert_id = ?) ORDER BY c.created_at ASC";
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            if (!"ADMIN".equals(role)) {
                ps.setInt(1, expertId);
            }
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Consultation c = mapResultSetToConsultation(rs);
                    c.setFarmerName(rs.getString("farmer_name"));
                    list.add(c);
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting pending consultations for expert ID: {}", expertId, e);
        }
        return list;
    }

    private Consultation mapResultSetToConsultation(ResultSet rs) throws SQLException {
        Consultation c = new Consultation();
        c.setConsultationId(rs.getInt("consultation_id"));
        c.setFarmerId(rs.getInt("farmer_id"));
        c.setQuestion(rs.getString("question"));
        c.setAnswer(rs.getString("answer"));
        c.setImageUrl(rs.getString("image_url"));
        c.setStatus(rs.getString("status"));
        c.setCreatedAt(rs.getTimestamp("created_at"));
        
        int targetId = rs.getInt("target_expert_id");
        if (!rs.wasNull()) {
            c.setTargetExpertId(targetId);
        }
        return c;
    }

    public int getFarmerId(int consultationId) {
        String sql = "SELECT farmer_id FROM consultations WHERE consultation_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, consultationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("farmer_id");
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting farmer ID for consultation ID: {}", consultationId, e);
        }
        return -1;
    }

    public boolean answerQuestion(int consultationId, int expertId, String answer) {
        String sql = "UPDATE consultations SET expert_id = ?, answer = ?, status = 'ANSWERED' WHERE consultation_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, expertId);
            ps.setString(2, answer);
            ps.setInt(3, consultationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error answering consultation ID: {}", consultationId, e);
        }
        return false;
    }
}
