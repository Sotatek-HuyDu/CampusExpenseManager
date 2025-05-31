package com.budgetwise.campusexpensemanager.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "recurring_expenses")
public class RecurringExpense {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int accountId;

    public String description;

    public double amount;

    public String category;

    public long startDate;

    public long endDate;

    public int recurrenceIntervalDays;
}
