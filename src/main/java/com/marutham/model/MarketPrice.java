package com.marutham.model;

public class MarketPrice {
    private int priceId;
    private String cropName;
    private double pricePerKg;
    private String marketName;
    private String state;

    public int getPriceId() { return priceId; }
    public void setPriceId(int priceId) { this.priceId = priceId; }
    public String getCropName() { return cropName; }
    public void setCropName(String cropName) { this.cropName = cropName; }
    public double getPricePerKg() { return pricePerKg; }
    public void setPricePerKg(double pricePerKg) { this.pricePerKg = pricePerKg; }
    public String getMarketName() { return marketName; }
    public void setMarketName(String marketName) { this.marketName = marketName; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
}
