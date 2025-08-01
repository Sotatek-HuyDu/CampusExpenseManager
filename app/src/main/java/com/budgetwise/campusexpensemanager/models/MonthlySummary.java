package com.budgetwise.campusexpensemanager.models;

import java.util.List;
import java.util.Map;

public class MonthlySummary {
    private int month;
    private int year;
    private double totalSpent;
    private double totalBudget;
    private double budgetedSpending;
    private double unbudgetedSpending;
    private double recurringSpending;
    private double budgetedRecurringSpending;
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
        this.recurringSpending = 0.0;
        this.budgetedRecurringSpending = 0.0;
        calculateBudgetedAndUnbudgetedSpending();
    }

    public MonthlySummary(int month, int year, double totalSpent, double totalBudget,
                         double budgetedSpending, double unbudgetedSpending,
                         double recurringSpending, double budgetedRecurringSpending,
                         Map<String, Double> categoryBreakdown, Map<String, Double> budgetByCategory,
                         List<Expense> expenses) {
        this.month = month;
        this.year = year;
        this.totalSpent = totalSpent;
        this.totalBudget = totalBudget;
        this.budgetedSpending = budgetedSpending;
        this.unbudgetedSpending = unbudgetedSpending;
        this.recurringSpending = recurringSpending;
        this.budgetedRecurringSpending = budgetedRecurringSpending;
        this.categoryBreakdown = categoryBreakdown;
        this.budgetByCategory = budgetByCategory;
        this.expenses = expenses;
    }

    private void calculateBudgetedAndUnbudgetedSpending() {
        this.budgetedSpending = 0.0;
        this.unbudgetedSpending = 0.0;
        
        if (categoryBreakdown != null && budgetByCategory != null) {
            for (Map.Entry<String, Double> entry : categoryBreakdown.entrySet()) {
                String category = entry.getKey();
                double amount = entry.getValue();
                
                if (budgetByCategory.containsKey(category)) {
                    this.budgetedSpending += amount;
                } else {
                    this.unbudgetedSpending += amount;
                }
            }
        }
    }

    // Getters
    public int getMonth() { return month; }
    public int getYear() { return year; }
    public double getTotalSpent() { return totalSpent; }
    public double getTotalBudget() { return totalBudget; }
    public double getBudgetedSpending() { return budgetedSpending; }
    public double getUnbudgetedSpending() { return unbudgetedSpending; }
    public double getRecurringSpending() { return recurringSpending; }
    public double getBudgetedRecurringSpending() { return budgetedRecurringSpending; }
    public Map<String, Double> getCategoryBreakdown() { return categoryBreakdown; }
    public Map<String, Double> getBudgetByCategory() { return budgetByCategory; }
    public List<Expense> getExpenses() { return expenses; }

    // Setters for recurring spending
    public void setRecurringSpending(double recurringSpending) {
        this.recurringSpending = recurringSpending;
    }

    public void setBudgetedRecurringSpending(double budgetedRecurringSpending) {
        this.budgetedRecurringSpending = budgetedRecurringSpending;
    }

    // Calculated properties
    public double getRemainingBudget() {
        return Math.max(0, totalBudget - budgetedSpending);
    }

    public double getNonRecurringSpending() {
        return totalSpent - recurringSpending;
    }

    public double getNonRecurringBudgetedSpending() {
        return budgetedSpending - budgetedRecurringSpending;
    }

    public double getBudgetUtilization() {
        if (totalBudget == 0) return 0;
        return (budgetedSpending / totalBudget) * 100;
    }

    public boolean isOverBudget() {
        return budgetedSpending > totalBudget;
    }

    public boolean hasUnbudgetedSpending() {
        return unbudgetedSpending > 0;
    }

    public boolean hasBudgetedSpending() {
        return budgetedSpending > 0;
    }

    public boolean hasBudgets() {
        return totalBudget > 0;
    }

    public String getMonthYearString() {
        String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                              "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        return monthNames[month - 1] + " " + year;
    }
} 