package com.budgetwise.campusexpensemanager.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete;

import com.budgetwise.campusexpensemanager.models.Expense;

import java.util.List;

@Dao
public interface ExpenseDao {
    @Insert
    void insert(Expense expense);

    @Query("SELECT * FROM expenses WHERE accountId = :accountId ORDER BY date DESC")
    List<Expense> getExpensesByAccount(int accountId);

    @Delete
    void delete(Expense expense);
}
