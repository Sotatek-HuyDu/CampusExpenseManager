package com.budgetwise.campusexpensemanager.firebase.models;

public class FirebaseBudget {
    private String id;
    private String accountId;
    private String category;
    private double limit;
    private int month;
    private int year;
    
    // Required empty constructor for Firebase Realtime Database
    public FirebaseBudget() {}
    
    public FirebaseBudget(String accountId, String category, double limit, int month, int year) {
        this.accountId = accountId;
        this.category = category;
        this.limit = limit;
        this.month = month;
        this.year = year;
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
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public double getLimit() {
        return limit;
    }
    
    public void setLimit(double limit) {
        this.limit = limit;
    }
    
    public int getMonth() {
        return month;
    }
    
    public void setMonth(int month) {
        this.month = month;
    }
    
    public int getYear() {
        return year;
    }
    
    public void setYear(int year) {
        this.year = year;
    }
} 