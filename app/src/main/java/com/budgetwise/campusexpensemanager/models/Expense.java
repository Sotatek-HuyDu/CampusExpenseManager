package com.budgetwise.campusexpensemanager.models;

import java.util.Date;

public class Expense {
    private String id;
    private String description;
    private double amount;
    private String category;
    private Date date;
    private String accountId;
    private String userId;
    private long timestamp;

    // Default constructor for Firebase
    public Expense() {
    }

    public Expense(String description, double amount, String category, Date date, String accountId) {
        this.description = description;
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.accountId = accountId;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
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
