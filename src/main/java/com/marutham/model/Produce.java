package com.marutham.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Produce {
    private int produceId;
    private int farmerId;
    private String farmerName; // For display
    private String cropName;
    private String quantity;
    private BigDecimal priceExpected;
    private String location;
    private String description;
    private String contactNumber;
    private String imageUrl;
    private String status;
    private Timestamp createdAt;

    public int getProduceId() { return produceId; }
    public void setProduceId(int produceId) { this.produceId = produceId; }

    public int getFarmerId() { return farmerId; }
    public void setFarmerId(int farmerId) { this.farmerId = farmerId; }

    public String getFarmerName() { return farmerName; }
    public void setFarmerName(String farmerName) { this.farmerName = farmerName; }

    public String getCropName() { return cropName; }
    public void setCropName(String cropName) { this.cropName = cropName; }

    public String getQuantity() { return quantity; }
    public void setQuantity(String quantity) { this.quantity = quantity; }

    public BigDecimal getPriceExpected() { return priceExpected; }
    public void setPriceExpected(BigDecimal priceExpected) { this.priceExpected = priceExpected; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
