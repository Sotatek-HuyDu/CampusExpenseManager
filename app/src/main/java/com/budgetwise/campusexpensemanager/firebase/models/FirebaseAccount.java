package com.budgetwise.campusexpensemanager.firebase.models;

import java.util.Date;

public class FirebaseAccount {
    private String id;
    
    private String username;
    private String password;
    
    private Date createdAt;
    
    // Required empty constructor for Firebase Realtime Database
    public FirebaseAccount() {}
    
    public FirebaseAccount(String username, String password) {
        this.username = username;
        this.password = password;
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
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
} 