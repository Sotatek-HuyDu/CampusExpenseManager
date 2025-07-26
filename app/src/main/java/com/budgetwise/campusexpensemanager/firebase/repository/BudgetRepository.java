package com.budgetwise.campusexpensemanager.firebase.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.budgetwise.campusexpensemanager.firebase.models.FirebaseBudget;

public class BudgetRepository extends FirebaseRepository {
    private static final String COLLECTION_NAME = "budgets";
    
    public Task<Void> insert(FirebaseBudget budget) {
        android.util.Log.d("BudgetRepository", "insert() called for budget: " + budget.getCategory() + " - " + budget.getLimit());
        DatabaseReference newBudgetRef = getReference(COLLECTION_NAME).push();
        budget.setId(newBudgetRef.getKey());
        android.util.Log.d("BudgetRepository", "Generated ID: " + budget.getId());
        android.util.Log.d("BudgetRepository", "About to call setValue() with budget: " + budget.getAccountId() + ", " + budget.getCategory() + ", " + budget.getLimit());
        
        Task<Void> task = newBudgetRef.setValue(budget);
        android.util.Log.d("BudgetRepository", "setValue() task returned: " + task);
        return task;
    }
    
    public Query getBudgetsByAccount(String accountId) {
        return getQuery(COLLECTION_NAME)
                .orderByChild("accountId")
                .equalTo(accountId);
    }
    
    public Query getBudgetByAccountAndMonth(String accountId, int month, int year) {
        return getQuery(COLLECTION_NAME)
                .orderByChild("accountId")
                .equalTo(accountId);
    }
    
    public Query getBudgetByAccountAndCategory(String accountId, String category) {
        return getQuery(COLLECTION_NAME)
                .orderByChild("accountId")
                .equalTo(accountId);
    }
    
    public Task<Void> delete(FirebaseBudget budget) {
        return getChildReference(COLLECTION_NAME, budget.getId()).removeValue();
    }
    
    public Task<Void> update(FirebaseBudget budget) {
        return getChildReference(COLLECTION_NAME, budget.getId()).setValue(budget);
    }
    
    public com.google.firebase.database.DatabaseReference getBudgetById(String budgetId) {
        return getChildReference(COLLECTION_NAME, budgetId);
    }
} 