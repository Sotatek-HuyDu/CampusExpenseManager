package com.budgetwise.campusexpensemanager.firebase.models;

import java.util.Date;

public class FirebaseRecurringExpense {
    private String id;
    private String accountId;
    private String description;
    private double amount;
    private String category;
    private Date startDate;
    private Date endDate;
    private int recurrenceIntervalDays;
    
    // Required empty constructor for Firebase Realtime Database
    public FirebaseRecurringExpense() {}
    
    public FirebaseRecurringExpense(String accountId, String description, double amount, 
                                   String category, Date startDate, Date endDate, int recurrenceIntervalDays) {
        this.accountId = accountId;
        this.description = description;
        this.amount = amount;
        this.category = category;
        this.startDate = startDate;
        this.endDate = endDate;
        this.recurrenceIntervalDays = recurrenceIntervalDays;
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
    
    public Date getStartDate() {
        return startDate;
    }
    
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
    
    public Date getEndDate() {
        return endDate;
    }
    
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
    
    public int getRecurrenceIntervalDays() {
        return recurrenceIntervalDays;
    }
    
    public void setRecurrenceIntervalDays(int recurrenceIntervalDays) {
        this.recurrenceIntervalDays = recurrenceIntervalDays;
    }
} 