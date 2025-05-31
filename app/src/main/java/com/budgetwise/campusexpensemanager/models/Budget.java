package com.budgetwise.campusexpensemanager.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "budgets")
public class Budget {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int accountId;

    public String category;

    public double limit;

    public int month;

    public int year;
}
