package com.marutham.dao;

import com.marutham.model.SoilReport;
import com.marutham.util.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SoilReportDAO {
    private static final Logger logger = LoggerFactory.getLogger(SoilReportDAO.class);

    public boolean addReport(SoilReport report) {
        String sql = "INSERT INTO soil_reports (farmer_id, soil_type, ph_level, nitrogen_content, phosphorus_content, potassium_content, organic_carbon, recommendation, report_file_url) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, report.getFarmerId());
            ps.setString(2, report.getSoilType());
            ps.setBigDecimal(3, report.getPhLevel());
            ps.setString(4, report.getNitrogenContent());
            ps.setString(5, report.getPhosphorusContent());
            ps.setString(6, report.getPotassiumContent());
            ps.setString(7, report.getOrganicCarbon());
            ps.setString(8, report.getRecommendation());
            ps.setString(9, report.getReportFileUrl());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error adding soil report for farmer ID: {}", report.getFarmerId(), e);
        }
        return false;
    }

    public List<SoilReport> getReportsForFarmer(int farmerId) {
        List<SoilReport> list = new ArrayList<>();
        String sql = "SELECT * FROM soil_reports WHERE farmer_id = ? ORDER BY created_at DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, farmerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SoilReport r = new SoilReport();
                    r.setReportId(rs.getInt("report_id"));
                    r.setFarmerId(rs.getInt("farmer_id"));
                    r.setSoilType(rs.getString("soil_type"));
                    r.setPhLevel(rs.getBigDecimal("ph_level"));
                    r.setNitrogenContent(rs.getString("nitrogen_content"));
                    r.setPhosphorusContent(rs.getString("phosphorus_content"));
                    r.setPotassiumContent(rs.getString("potassium_content"));
                    r.setOrganicCarbon(rs.getString("organic_carbon"));
                    r.setRecommendation(rs.getString("recommendation"));
                    r.setReportFileUrl(rs.getString("report_file_url"));
                    r.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(r);
                }
            }
        } catch (SQLException e) {
            logger.error("Error getting soil reports for farmer ID: {}", farmerId, e);
        }
        return list;
    }
}
