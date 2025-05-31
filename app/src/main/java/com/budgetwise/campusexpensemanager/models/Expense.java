package com.budgetwise.campusexpensemanager.models;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "expenses",
        foreignKeys = @ForeignKey(
                entity = Account.class,
                parentColumns = "id",
                childColumns = "accountId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("accountId")}
)
public class Expense {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int accountId;

    public String description;

    public double amount;

    public String category;

    public long date;
}
