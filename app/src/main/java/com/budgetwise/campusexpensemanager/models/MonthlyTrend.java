package com.budgetwise.campusexpensemanager.models;

public class MonthlyTrend {
    private int month;
    private int year;
    private double totalAmount;

    public MonthlyTrend(int month, int year, double totalAmount) {
        this.month = month;
        this.year = year;
        this.totalAmount = totalAmount;
    }

    // Getters
    public int getMonth() { return month; }
    public int getYear() { return year; }
    public double getTotalAmount() { return totalAmount; }

    // Setters
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getMonthYearString() {
        String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                              "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        return monthNames[month - 1] + " " + year;
    }

    public String getShortMonthString() {
        String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                              "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        return monthNames[month - 1];
    }
} 