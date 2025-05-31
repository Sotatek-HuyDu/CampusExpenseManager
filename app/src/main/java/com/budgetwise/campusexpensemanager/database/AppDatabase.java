package com.budgetwise.campusexpensemanager.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.budgetwise.campusexpensemanager.models.Account;
import com.budgetwise.campusexpensemanager.models.Expense;
import com.budgetwise.campusexpensemanager.models.Budget;
import com.budgetwise.campusexpensemanager.models.RecurringExpense;

@Database(
        entities = {Account.class, Expense.class, Budget.class, RecurringExpense.class},
        version = 1
)
public abstract class AppDatabase extends RoomDatabase {
    public abstract AccountDao accountDao();
    public abstract ExpenseDao expenseDao();
    public abstract BudgetDao budgetDao();
    public abstract RecurringExpenseDao recurringExpenseDao();

    public abstract RawQueryDao rawQueryDao();
}
