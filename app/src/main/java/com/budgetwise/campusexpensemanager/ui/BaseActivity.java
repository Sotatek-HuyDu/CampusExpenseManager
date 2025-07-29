package com.budgetwise.campusexpensemanager.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.budgetwise.campusexpensemanager.R;
import com.budgetwise.campusexpensemanager.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public abstract class BaseActivity extends AppCompatActivity {

    protected BottomNavigationView bottomNavigationView;
    protected SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceId());
        
        sessionManager = new SessionManager(this);
        setupBottomNavigation();
        setupActivity();
    }

    protected abstract int getLayoutResourceId();
    protected abstract void setupActivity();

    private void setupBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_nav);
        if (bottomNavigationView != null) {
            bottomNavigationView.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                
                if (id == R.id.nav_overview) {
                    if (!(this instanceof OverviewActivity)) {
                        Intent intent = new Intent(this, OverviewActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                    return true;
                } else if (id == R.id.nav_expenses) {
                    if (!(this instanceof ExpenseActivity)) {
                        Intent intent = new Intent(this, ExpenseActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    return true;
                } else if (id == R.id.nav_recurring) {
                    if (!(this instanceof RecurringExpenseActivity)) {
                        Intent intent = new Intent(this, RecurringExpenseActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    return true;
                                                } else if (id == R.id.nav_budget) {
                    if (!(this instanceof BudgetActivity)) {
                        Intent intent = new Intent(this, BudgetActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    return true;
                } else if (id == R.id.nav_overview) {
                    if (!(this instanceof OverviewActivity)) {
                        Intent intent = new Intent(this, OverviewActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    return true;

                }
                return false;
            });
            
            // Set the correct item as selected based on current activity
            setSelectedNavigationItem();
        }
    }

    private void setSelectedNavigationItem() {
        if (this instanceof OverviewActivity) {
            bottomNavigationView.setSelectedItemId(R.id.nav_overview);
        } else if (this instanceof ExpenseActivity) {
            bottomNavigationView.setSelectedItemId(R.id.nav_expenses);
        } else if (this instanceof RecurringExpenseActivity) {
            bottomNavigationView.setSelectedItemId(R.id.nav_recurring);
                                } else if (this instanceof BudgetActivity) {
            bottomNavigationView.setSelectedItemId(R.id.nav_budget);
        } else if (this instanceof OverviewActivity) {
            bottomNavigationView.setSelectedItemId(R.id.nav_overview);
        }
        // Add more cases as you add more activities
    }
} 