package com.budgetwise.campusexpensemanager;

import android.app.Application;
import com.google.firebase.FirebaseApp;

public class CampusExpenseManagerApp extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this);
    }
} 