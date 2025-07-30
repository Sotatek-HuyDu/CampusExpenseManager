package com.budgetwise.campusexpensemanager.models;

public class RecurringExpense {
    public int id;
    public int accountId;
    public String description;
    public double amount;
    public String category;
    public long startDate;
    public long endDate;
    public int recurrenceIntervalDays;
}
