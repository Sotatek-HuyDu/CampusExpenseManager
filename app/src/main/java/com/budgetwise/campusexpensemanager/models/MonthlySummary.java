package com.budgetwise.campusexpensemanager.models;

import java.util.List;
import java.util.Map;

public class MonthlySummary {
    private int month;
    private int year;
    private double totalSpent;
    private double totalBudget;
    private Map<String, Double> categoryBreakdown;
    private Map<String, Double> budgetByCategory;
    private List<Expense> expenses;

    public MonthlySummary(int month, int year, double totalSpent, double totalBudget,
                         Map<String, Double> categoryBreakdown, Map<String, Double> budgetByCategory,
                         List<Expense> expenses) {
        this.month = month;
        this.year = year;
        this.totalSpent = totalSpent;
        this.totalBudget = totalBudget;
        this.categoryBreakdown = categoryBreakdown;
        this.budgetByCategory = budgetByCategory;
        this.expenses = expenses;
    }

    // Getters
    public int getMonth() { return month; }
    public int getYear() { return year; }
    public double getTotalSpent() { return totalSpent; }
    public double getTotalBudget() { return totalBudget; }
    public Map<String, Double> getCategoryBreakdown() { return categoryBreakdown; }
    public Map<String, Double> getBudgetByCategory() { return budgetByCategory; }
    public List<Expense> getExpenses() { return expenses; }

    // Calculated properties
    public double getRemainingBudget() {
        return Math.max(0, totalBudget - totalSpent);
    }

    public double getBudgetUtilization() {
        if (totalBudget == 0) return 0;
        return (totalSpent / totalBudget) * 100;
    }

    public boolean isOverBudget() {
        return totalSpent > totalBudget;
    }

    public String getMonthYearString() {
        String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                              "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        return monthNames[month - 1] + " " + year;
    }
} 