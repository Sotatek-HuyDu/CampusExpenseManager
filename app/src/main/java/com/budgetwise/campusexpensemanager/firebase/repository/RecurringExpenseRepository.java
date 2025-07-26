package com.budgetwise.campusexpensemanager.firebase.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.budgetwise.campusexpensemanager.firebase.models.FirebaseRecurringExpense;

public class RecurringExpenseRepository extends FirebaseRepository {
    private static final String COLLECTION_NAME = "recurring_expenses";
    
    public Task<Void> insert(FirebaseRecurringExpense recurringExpense) {
        DatabaseReference newRecurringExpenseRef = getReference(COLLECTION_NAME).push();
        recurringExpense.setId(newRecurringExpenseRef.getKey());
        return newRecurringExpenseRef.setValue(recurringExpense);
    }
    
    public Query getRecurringExpensesByAccount(String accountId) {
        return getQuery(COLLECTION_NAME)
                .orderByChild("accountId")
                .equalTo(accountId);
    }
    
    public Query getRecurringExpensesByCategory(String accountId, String category) {
        return getQuery(COLLECTION_NAME)
                .orderByChild("accountId")
                .equalTo(accountId);
    }
    
    public DatabaseReference getRecurringExpenseById(String id) {
        return getChildReference(COLLECTION_NAME, id);
    }
    
    public Task<Void> delete(FirebaseRecurringExpense recurringExpense) {
        return getChildReference(COLLECTION_NAME, recurringExpense.getId()).removeValue();
    }
    
    public Task<Void> update(FirebaseRecurringExpense recurringExpense) {
        return getChildReference(COLLECTION_NAME, recurringExpense.getId()).setValue(recurringExpense);
    }
} 