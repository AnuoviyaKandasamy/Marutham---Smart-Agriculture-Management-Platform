package com.marutham.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class SoilReport {
    private int reportId;
    private int farmerId;
    private String soilType;
    private BigDecimal phLevel;
    private String nitrogenContent;
    private String phosphorusContent;
    private String potassiumContent;
    private String organicCarbon;
    private String recommendation;
    private String reportFileUrl;
    private Timestamp createdAt;


    public int getReportId() { return reportId; }
    public void setReportId(int reportId) { this.reportId = reportId; }

    public int getFarmerId() { return farmerId; }
    public void setFarmerId(int farmerId) { this.farmerId = farmerId; }

    public String getSoilType() { return soilType; }
    public void setSoilType(String soilType) { this.soilType = soilType; }

    public BigDecimal getPhLevel() { return phLevel; }
    public void setPhLevel(BigDecimal phLevel) { this.phLevel = phLevel; }

    public String getNitrogenContent() { return nitrogenContent; }
    public void setNitrogenContent(String nitrogenContent) { this.nitrogenContent = nitrogenContent; }

    public String getPhosphorusContent() { return phosphorusContent; }
    public void setPhosphorusContent(String phosphorusContent) { this.phosphorusContent = phosphorusContent; }

    public String getPotassiumContent() { return potassiumContent; }
    public void setPotassiumContent(String potassiumContent) { this.potassiumContent = potassiumContent; }

    public String getOrganicCarbon() { return organicCarbon; }
    public void setOrganicCarbon(String organicCarbon) { this.organicCarbon = organicCarbon; }

    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }

    public String getReportFileUrl() { return reportFileUrl; }
    public void setReportFileUrl(String reportFileUrl) { this.reportFileUrl = reportFileUrl; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
