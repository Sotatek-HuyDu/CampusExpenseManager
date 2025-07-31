package com.budgetwise.campusexpensemanager.firebase.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.budgetwise.campusexpensemanager.firebase.models.FirebaseAccount;

public class AccountRepository extends FirebaseRepository {
    private static final String COLLECTION_NAME = "accounts";
    
    public Task<Void> insert(FirebaseAccount account) {
        DatabaseReference newAccountRef = getReference(COLLECTION_NAME).push();
        account.setId(newAccountRef.getKey());
        return newAccountRef.setValue(account);
    }
    
    public Query getAccountByUsername(String username) {
        return getQuery(COLLECTION_NAME)
                .orderByChild("username")
                .equalTo(username);
    }
    
    public Query getAccountByUsernameAndPassword(String username, String password) {
        return getQuery(COLLECTION_NAME)
                .orderByChild("username")
                .equalTo(username);
    }
    
    public Task<Void> delete(FirebaseAccount account) {
        return getChildReference(COLLECTION_NAME, account.getId()).removeValue();
    }
    
    public Task<Void> update(FirebaseAccount account) {
        return getChildReference(COLLECTION_NAME, account.getId()).setValue(account);
    }
    
    public Task<Void> updateEmail(String accountId, String email) {
        return getChildReference(COLLECTION_NAME, accountId).child("email").setValue(email);
    }
    
    public Query getAccountById(String accountId) {
        return getQuery(COLLECTION_NAME).orderByKey().equalTo(accountId);
    }
    
    public Query getAccountByEmail(String email) {
        return getQuery(COLLECTION_NAME).orderByChild("email").equalTo(email);
    }
    
    public Task<Boolean> isEmailUnique(String email, String currentAccountId) {
        return getAccountByEmail(email).get().continueWith(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                // Check if the found account is the same as current user
                for (DataSnapshot snapshot : task.getResult().getChildren()) {
                    String accountId = snapshot.getKey();
                    if (!accountId.equals(currentAccountId)) {
                        // Email exists and belongs to a different account
                        return false;
                    }
                }
            }
            return true; // Email is unique or belongs to current user
        });
    }
    
    public Task<Boolean> isUsernameUnique(String username) {
        return getAccountByUsername(username).get().continueWith(task -> {
            return !(task.isSuccessful() && task.getResult().exists());
        });
    }
} 