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
} 