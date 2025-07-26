package com.budgetwise.campusexpensemanager.firebase.models;

import java.util.Date;

public class FirebaseExpense {
    private String id;
    private String accountId;
    private String description;
    private double amount;
    private String category;
    private Date date;
    
    // Required empty constructor for Firebase Realtime Database
    public FirebaseExpense() {}
    
    public FirebaseExpense(String accountId, String description, double amount, String category) {
        this.accountId = accountId;
        this.description = description;
        this.amount = amount;
        this.category = category;
        this.date = new Date();
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getAccountId() {
        return accountId;
    }
    
    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public double getAmount() {
        return amount;
    }
    
    public void setAmount(double amount) {
        this.amount = amount;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public Date getDate() {
        return date;
    }
    
    public void setDate(Date date) {
        this.date = date;
    }
} 