package com.budgetwise.campusexpensemanager.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.budgetwise.campusexpensemanager.models.RecurringExpense;

import java.util.List;

@Dao
public interface RecurringExpenseDao {
    @Insert
    void insert(RecurringExpense recurringExpense);

    @Query("SELECT * FROM recurring_expenses WHERE accountId = :accountId")
    List<RecurringExpense> getAll(int accountId);
}
