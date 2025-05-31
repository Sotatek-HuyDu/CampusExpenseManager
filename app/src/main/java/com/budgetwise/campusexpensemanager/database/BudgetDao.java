package com.budgetwise.campusexpensemanager.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.budgetwise.campusexpensemanager.models.Budget;

import java.util.List;

@Dao
public interface BudgetDao {
    @Insert
    void insert(Budget budget);

    @Query("SELECT * FROM budgets WHERE accountId = :accountId AND month = :month AND year = :year")
    List<Budget> getBudgetsForMonth(int accountId, int month, int year);
}
