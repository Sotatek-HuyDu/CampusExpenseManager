package com.budgetwise.campusexpensemanager.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.budgetwise.campusexpensemanager.R;
import com.budgetwise.campusexpensemanager.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

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

    protected void setupToolbar(String title) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(title);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
            }
        }
    }

    protected void setupDrawer() {
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        View drawerContent = findViewById(R.id.right_drawer);
        
        if (drawerLayout != null && drawerContent != null) {
            // Set personalized greeting
            TextView greetingText = drawerContent.findViewById(R.id.header_greeting);
            if (greetingText != null) {
                String username = sessionManager.getUsername();
                if (username != null && !username.isEmpty()) {
                    greetingText.setText("Hello " + username + "!");
                } else {
                    greetingText.setText("Hello User!");
                }
            }
            
            // Set up logout button click listener
            Button logoutButton = drawerContent.findViewById(R.id.logout_button);
            if (logoutButton != null) {
                logoutButton.setOnClickListener(v -> {
                    sessionManager.logout();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });
            }
            
            // Set up navigation item selected listener for future menu items
            NavigationView navigationView = drawerContent.findViewById(R.id.navigation_view);
            if (navigationView != null) {
                navigationView.setNavigationItemSelectedListener(item -> {
                    int id = item.getItemId();
                    // Handle future menu items here
                    drawerLayout.closeDrawer(GravityCompat.END);
                    return true;
                });
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
            if (drawerLayout != null) {
                drawerLayout.openDrawer(GravityCompat.END);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

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
                } else if (id == R.id.nav_report) {
                    if (!(this instanceof ReportActivity)) {
                        Intent intent = new Intent(this, ReportActivity.class);
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
        } else if (this instanceof ReportActivity) {
            bottomNavigationView.setSelectedItemId(R.id.nav_report);
        }
    }
} 