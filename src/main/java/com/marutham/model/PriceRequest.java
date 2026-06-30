package com.marutham.model;

import java.sql.Timestamp;

public class PriceRequest {
    private int requestId;
    private int farmerId;
    private String farmerName; // For display
    private String cropName;
    private double requestedPrice;
    private String quantity;
    private String status;
    private Double adminPrice;
    private Timestamp createdAt;

    public int getRequestId() { return requestId; }
    public void setRequestId(int requestId) { this.requestId = requestId; }
    public int getFarmerId() { return farmerId; }
    public void setFarmerId(int farmerId) { this.farmerId = farmerId; }
    public String getFarmerName() { return farmerName; }
    public void setFarmerName(String farmerName) { this.farmerName = farmerName; }
    public String getCropName() { return cropName; }
    public void setCropName(String cropName) { this.cropName = cropName; }
    public double getRequestedPrice() { return requestedPrice; }
    public void setRequestedPrice(double requestedPrice) { this.requestedPrice = requestedPrice; }
    public String getQuantity() { return quantity; }
    public void setQuantity(String quantity) { this.quantity = quantity; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Double getAdminPrice() { return adminPrice; }
    public void setAdminPrice(Double adminPrice) { this.adminPrice = adminPrice; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
