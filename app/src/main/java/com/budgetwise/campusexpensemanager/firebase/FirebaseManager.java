package com.budgetwise.campusexpensemanager.firebase;

import com.budgetwise.campusexpensemanager.firebase.repository.AccountRepository;
import com.budgetwise.campusexpensemanager.firebase.repository.BudgetRepository;
import com.budgetwise.campusexpensemanager.firebase.ExpenseRepository;
import com.budgetwise.campusexpensemanager.firebase.repository.RecurringExpenseRepository;
import com.budgetwise.campusexpensemanager.firebase.repository.ExpenseAnalyticsRepository;

public class FirebaseManager {
    private static FirebaseManager instance;
    private final AccountRepository accountRepository;
    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;
    private final RecurringExpenseRepository recurringExpenseRepository;
    private final ExpenseAnalyticsRepository analyticsRepository;
    
    private FirebaseManager() {
        // Initialize Firebase repositories
        accountRepository = new AccountRepository();
        expenseRepository = new ExpenseRepository();
        budgetRepository = new BudgetRepository();
        recurringExpenseRepository = new RecurringExpenseRepository();
        analyticsRepository = new ExpenseAnalyticsRepository();
    }
    

    
    public static synchronized FirebaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }
    
    public AccountRepository getAccountRepository() {
        return accountRepository;
    }
    
    public ExpenseRepository getExpenseRepository() {
        return expenseRepository;
    }
    
    public BudgetRepository getBudgetRepository() {
        return budgetRepository;
    }
    
    public RecurringExpenseRepository getRecurringExpenseRepository() {
        return recurringExpenseRepository;
    }
    
    public ExpenseAnalyticsRepository getAnalyticsRepository() {
        return analyticsRepository;
    }
} 