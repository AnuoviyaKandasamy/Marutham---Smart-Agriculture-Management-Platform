package com.marutham.model;

import java.sql.Timestamp;

public class User {
    private int userId;
    private String username;
    private String password;
    private String email;
    private String fullName;
    private String phone;
    private String location;
    private String role;
    private String profilePicture;
    private Timestamp createdAt;

    public User() {}
    
    public User(String username, String password, String email, String fullName, String phone, String role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullName = fullName;
        this.phone = phone;
        this.role = role;
    }

    public User(String username, String password, String email, String fullName, String phone, String location, String role, String profilePicture) {
        this(username, password, email, fullName, phone, role);
        this.location = location;
        this.profilePicture = profilePicture;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}