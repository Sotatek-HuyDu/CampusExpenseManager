package com.budgetwise.campusexpensemanager.firebase.models;

import java.util.Date;

public class FirebaseExpense {
    private String id;
    private String accountId;
    private String description;
    private double amount;
    private String category;
    private Date date;
    private Object firebaseDate;
    private String userId;
    private long timestamp;
    
    public FirebaseExpense() {}
    
    public FirebaseExpense(String accountId, String description, double amount, String category) {
        this.accountId = accountId;
        this.description = description;
        this.amount = amount;
        this.category = category;
        this.date = new Date();
    }
    
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
        if (date != null) {
            return date;
        }
        if (firebaseDate instanceof java.util.Map) {
            java.util.Map<String, Object> dateMap = (java.util.Map<String, Object>) firebaseDate;
            if (dateMap.containsKey("time")) {
                return new Date((Long) dateMap.get("time"));
            }
        }
        return null;
    }
    
    public void setDate(Date date) {
        this.date = date;
    }
    
    public Object getFirebaseDate() {
        return firebaseDate;
    }
    
    public void setFirebaseDate(Object firebaseDate) {
        this.firebaseDate = firebaseDate;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
} 