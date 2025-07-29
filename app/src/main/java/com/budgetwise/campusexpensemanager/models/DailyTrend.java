package com.budgetwise.campusexpensemanager.models;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DailyTrend {
    private Date date;
    private double totalAmount;
    private int transactionCount;

    public DailyTrend(Date date, double totalAmount, int transactionCount) {
        this.date = date;
        this.totalAmount = totalAmount;
        this.transactionCount = transactionCount;
    }

    // Getters
    public Date getDate() { return date; }
    public double getTotalAmount() { return totalAmount; }
    public int getTransactionCount() { return transactionCount; }

    // Setters
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public void setTransactionCount(int transactionCount) { this.transactionCount = transactionCount; }

    // Helper methods
    public String getDayString() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
        return sdf.format(date);
    }

    public String getFullDateString() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        return sdf.format(date);
    }

    public String getDayOfWeek() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE", Locale.getDefault());
        return sdf.format(date);
    }

    public int getDayOfMonth() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.DAY_OF_MONTH);
    }

    public int getMonth() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.MONTH) + 1;
    }

    public int getYear() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.YEAR);
    }

    public boolean isToday() {
        Calendar today = Calendar.getInstance();
        Calendar thisDate = Calendar.getInstance();
        thisDate.setTime(date);
        
        return today.get(Calendar.YEAR) == thisDate.get(Calendar.YEAR) &&
               today.get(Calendar.DAY_OF_YEAR) == thisDate.get(Calendar.DAY_OF_YEAR);
    }

    public boolean isThisWeek() {
        Calendar today = Calendar.getInstance();
        Calendar thisDate = Calendar.getInstance();
        thisDate.setTime(date);
        
        return today.get(Calendar.YEAR) == thisDate.get(Calendar.YEAR) &&
               today.get(Calendar.WEEK_OF_YEAR) == thisDate.get(Calendar.WEEK_OF_YEAR);
    }
} 