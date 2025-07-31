package com.budgetwise.campusexpensemanager.firebase.models;

import java.util.Date;

public class FirebaseAccount {
    private String id;
    
    private String username;
    private String password;
    private String email;
    
    private Object createdAt; // Can be Date or Long
    
    // Required empty constructor for Firebase Realtime Database
    public FirebaseAccount() {}
    
    public FirebaseAccount(String username, String password) {
        this.username = username;
        this.password = password;
        this.createdAt = new Date();
    }
    
    public FirebaseAccount(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.createdAt = new Date();
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public Date getCreatedAt() {
        if (createdAt instanceof Date) {
            return (Date) createdAt;
        } else if (createdAt instanceof Long) {
            return new Date((Long) createdAt);
        }
        return null;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }
} 