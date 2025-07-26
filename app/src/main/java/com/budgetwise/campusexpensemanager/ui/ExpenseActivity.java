package com.budgetwise.campusexpensemanager.ui;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.budgetwise.campusexpensemanager.R;
import com.budgetwise.campusexpensemanager.firebase.ExpenseRepository;
import com.budgetwise.campusexpensemanager.models.Expense;
import com.budgetwise.campusexpensemanager.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExpenseActivity extends BaseActivity {

    private RecyclerView expenseRecyclerView;
    private TextView emptyStateText;
    private FloatingActionButton addExpenseFab;
    private ExpenseAdapter expenseAdapter;
    private ExpenseRepository expenseRepository;
    private SessionManager sessionManager;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_expense;
    }

    @Override
    protected void setupActivity() {
        // Initialize repositories
        expenseRepository = new ExpenseRepository();
        sessionManager = new SessionManager(this);

        // Initialize views
        expenseRecyclerView = findViewById(R.id.expense_recycler_view);
        emptyStateText = findViewById(R.id.empty_state_text);
        addExpenseFab = findViewById(R.id.add_expense_fab);

        // Setup toolbar
        setupToolbar();

        // Setup RecyclerView
        expenseAdapter = new ExpenseAdapter(this);
        expenseRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        expenseRecyclerView.setHasFixedSize(true);
        expenseRecyclerView.setAdapter(expenseAdapter);

        // Setup FAB click listener
        addExpenseFab.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditExpenseActivity.class);
            startActivity(intent);
        });

        // Load expenses (excluding recurring expenses which are shown in the Recurring tab)
        loadExpenses();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Manual Expenses");
        }
    }

    private void loadExpenses() {
        String userId = sessionManager.getUsername();
        if (userId != null) {
            expenseRepository.getExpensesByUser(userId, new ExpenseRepository.ExpenseCallback() {
                @Override
                public void onSuccess(List<Expense> expenses) {
                    runOnUiThread(() -> {
                        if (!isFinishing()) {
                            // Filter out recurring expenses (those with [RECURRING] prefix)
                            List<Expense> filteredExpenses = new ArrayList<>();
                            for (Expense expense : expenses) {
                                if (!expense.getDescription().startsWith("[RECURRING]")) {
                                    filteredExpenses.add(expense);
                                }
                            }
                            
                            // Sort expenses by date (newest first)
                            Collections.sort(filteredExpenses, (e1, e2) -> {
                                if (e1.getDate() == null && e2.getDate() == null) return 0;
                                if (e1.getDate() == null) return 1;
                                if (e2.getDate() == null) return -1;
                                return e2.getDate().compareTo(e1.getDate());
                            });

                            expenseAdapter.updateExpenses(filteredExpenses);
                            updateEmptyState(filteredExpenses.isEmpty());
                        }
                    });
                }

                @Override
                public void onError(Exception e) {
                    runOnUiThread(() -> {
                        if (!isFinishing()) {
                            // Handle error - could show a toast or error message
                            updateEmptyState(true);
                        }
                    });
                }
            });
        }
    }

    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
            expenseRecyclerView.setVisibility(View.GONE);
            emptyStateText.setVisibility(View.VISIBLE);
        } else {
            expenseRecyclerView.setVisibility(View.VISIBLE);
            emptyStateText.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload expenses when returning to this activity
        loadExpenses();
    }
} 