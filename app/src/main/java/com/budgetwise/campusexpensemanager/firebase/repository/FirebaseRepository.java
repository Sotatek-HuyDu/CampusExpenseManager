package com.budgetwise.campusexpensemanager.firebase.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class FirebaseRepository {
    protected final FirebaseDatabase database;
    
    public FirebaseRepository() {
        // Use Singapore region to match the working expense repository
        this.database = FirebaseDatabase.getInstance("https://campus-expense-manager-c16e3-default-rtdb.asia-southeast1.firebasedatabase.app");
    }
    
    protected DatabaseReference getReference(String path) {
        return database.getReference(path);
    }
    
    protected DatabaseReference getChildReference(String parentPath, String childPath) {
        return database.getReference(parentPath).child(childPath);
    }
    
    protected Query getQuery(String path) {
        return database.getReference(path);
    }
} 