package com.marutham.model;

import java.sql.Timestamp;

public class Consultation {
    private int consultationId;
    private int farmerId;
    private String farmerName;
    private int expertId; // Responder expert
    private String expertName;
    private Integer targetExpertId;
    private String question;
    private String answer;
    private String imageUrl;
    private String status;
    private Timestamp createdAt;

    public int getConsultationId() { return consultationId; }
    public void setConsultationId(int consultationId) { this.consultationId = consultationId; }
    public int getFarmerId() { return farmerId; }
    public void setFarmerId(int farmerId) { this.farmerId = farmerId; }
    public String getFarmerName() { return farmerName; }
    public void setFarmerName(String farmerName) { this.farmerName = farmerName; }
    public int getExpertId() { return expertId; }
    public void setExpertId(int expertId) { this.expertId = expertId; }
    public String getExpertName() { return expertName; }
    public void setExpertName(String expertName) { this.expertName = expertName; }
    public Integer getTargetExpertId() { return targetExpertId; }
    public void setTargetExpertId(Integer targetExpertId) { this.targetExpertId = targetExpertId; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
