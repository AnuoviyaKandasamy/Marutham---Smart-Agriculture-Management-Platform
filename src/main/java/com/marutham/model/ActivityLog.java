package com.marutham.model;

import java.sql.Timestamp;

public class ActivityLog {
    private int logId;
    private Integer userId;
    private String action;
    private String description;
    private String ipAddress;
    private Timestamp createdAt;

    public ActivityLog() {}

    public ActivityLog(Integer userId, String action, String description, String ipAddress) {
        this.userId = userId;
        this.action = action;
        this.description = description;
        this.ipAddress = ipAddress;
    }

    public int getLogId() { return logId; }
    public void setLogId(int logId) { this.logId = logId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
