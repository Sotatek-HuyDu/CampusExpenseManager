package com.budgetwise.campusexpensemanager.models;

public class CategoryAnalysis {
    private String category;
    private double totalAmount;
    private int transactionCount;
    private double budgetLimit;
    private double percentageOfTotal;

    public CategoryAnalysis(String category, double totalAmount, int transactionCount) {
        this.category = category;
        this.totalAmount = totalAmount;
        this.transactionCount = transactionCount;
        this.budgetLimit = 0.0;
        this.percentageOfTotal = 0.0;
    }

    public CategoryAnalysis(String category, double totalAmount, int transactionCount, 
                          double budgetLimit, double percentageOfTotal) {
        this.category = category;
        this.totalAmount = totalAmount;
        this.transactionCount = transactionCount;
        this.budgetLimit = budgetLimit;
        this.percentageOfTotal = percentageOfTotal;
    }

    // Getters
    public String getCategory() { return category; }
    public double getTotalAmount() { return totalAmount; }
    public int getTransactionCount() { return transactionCount; }
    public double getBudgetLimit() { return budgetLimit; }
    public double getPercentageOfTotal() { return percentageOfTotal; }

    // Setters
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public void setTransactionCount(int transactionCount) { this.transactionCount = transactionCount; }
    public void setBudgetLimit(double budgetLimit) { this.budgetLimit = budgetLimit; }
    public void setPercentageOfTotal(double percentageOfTotal) { this.percentageOfTotal = percentageOfTotal; }

    // Calculated properties
    public double getRemainingBudget() {
        return Math.max(0, budgetLimit - totalAmount);
    }

    public double getBudgetUtilization() {
        if (budgetLimit == 0) return 0;
        return (totalAmount / budgetLimit) * 100;
    }

    public boolean isOverBudget() {
        return totalAmount > budgetLimit;
    }

    public double getAverageTransactionAmount() {
        if (transactionCount == 0) return 0;
        return totalAmount / transactionCount;
    }
} 