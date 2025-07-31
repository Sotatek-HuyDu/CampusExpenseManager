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
    private Object firebaseStartDate;
    private Object firebaseEndDate;
    private int recurrenceIntervalDays;
    
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
        if (startDate != null) {
            return startDate;
        }
        if (firebaseStartDate instanceof java.util.Map) {
            java.util.Map<String, Object> dateMap = (java.util.Map<String, Object>) firebaseStartDate;
            if (dateMap.containsKey("time")) {
                return new Date((Long) dateMap.get("time"));
            }
        }
        return null;
    }
    
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
    
    public Date getEndDate() {
        if (endDate != null) {
            return endDate;
        }
        if (firebaseEndDate instanceof java.util.Map) {
            java.util.Map<String, Object> dateMap = (java.util.Map<String, Object>) firebaseEndDate;
            if (dateMap.containsKey("time")) {
                return new Date((Long) dateMap.get("time"));
            }
        }
        return null;
    }
    
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
    
    public Object getFirebaseStartDate() {
        return firebaseStartDate;
    }
    
    public void setFirebaseStartDate(Object firebaseStartDate) {
        this.firebaseStartDate = firebaseStartDate;
    }
    
    public Object getFirebaseEndDate() {
        return firebaseEndDate;
    }
    
    public void setFirebaseEndDate(Object firebaseEndDate) {
        this.firebaseEndDate = firebaseEndDate;
    }
    
    public int getRecurrenceIntervalDays() {
        return recurrenceIntervalDays;
    }
    
    public void setRecurrenceIntervalDays(int recurrenceIntervalDays) {
        this.recurrenceIntervalDays = recurrenceIntervalDays;
    }
} 