package com.budgetwise.campusexpensemanager.ui;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import com.budgetwise.campusexpensemanager.R;
import com.budgetwise.campusexpensemanager.firebase.models.FirebaseRecurringExpense;
import com.budgetwise.campusexpensemanager.firebase.repository.RecurringExpenseRepository;
import com.budgetwise.campusexpensemanager.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RecurringExpenseActivity extends BaseActivity {

    private RecyclerView recurringExpenseRecyclerView;
    private TextView emptyStateText;
    private FloatingActionButton addRecurringExpenseFab;
    private RecurringExpenseAdapter recurringExpenseAdapter;
    private RecurringExpenseRepository recurringExpenseRepository;
    private SessionManager sessionManager;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_recurring_expense;
    }

    @Override
    protected void setupActivity() {
        // Initialize repositories
        recurringExpenseRepository = new RecurringExpenseRepository();
        sessionManager = new SessionManager(this);

        // Initialize views
        recurringExpenseRecyclerView = findViewById(R.id.recurring_expense_recycler_view);
        emptyStateText = findViewById(R.id.empty_state_text);
        addRecurringExpenseFab = findViewById(R.id.add_recurring_expense_fab);

        // Setup toolbar
        setupToolbar();

        // Setup RecyclerView
        recurringExpenseAdapter = new RecurringExpenseAdapter(this);
        recurringExpenseRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        recurringExpenseRecyclerView.setHasFixedSize(true);
        recurringExpenseRecyclerView.setAdapter(recurringExpenseAdapter);

        // Setup FAB click listener
        addRecurringExpenseFab.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditRecurringExpenseActivity.class);
            startActivity(intent);
        });

        // Load recurring expenses
        loadRecurringExpenses();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Recurring Expenses");
        }
    }

    private void loadRecurringExpenses() {
        String accountId = sessionManager.getAccountId();
        if (accountId != null) {
            recurringExpenseRepository.getRecurringExpensesByAccount(accountId)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            List<FirebaseRecurringExpense> recurringExpenses = new ArrayList<>();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                FirebaseRecurringExpense recurringExpense = snapshot.getValue(FirebaseRecurringExpense.class);
                                if (recurringExpense != null) {
                                    recurringExpense.setId(snapshot.getKey());
                                    recurringExpenses.add(recurringExpense);
                                }
                            }
                            
                            runOnUiThread(() -> {
                                if (!isFinishing()) {
                                    // Sort by start date (newest first)
                                    Collections.sort(recurringExpenses, (e1, e2) -> {
                                        if (e1.getStartDate() == null && e2.getStartDate() == null) return 0;
                                        if (e1.getStartDate() == null) return 1;
                                        if (e2.getStartDate() == null) return -1;
                                        return e2.getStartDate().compareTo(e1.getStartDate());
                                    });

                                    recurringExpenseAdapter.updateRecurringExpenses(recurringExpenses);
                                    updateEmptyState(recurringExpenses.isEmpty());
                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            runOnUiThread(() -> {
                                if (!isFinishing()) {
                                    updateEmptyState(true);
                                }
                            });
                        }
                    });
        }
    }

    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
            recurringExpenseRecyclerView.setVisibility(View.GONE);
            emptyStateText.setVisibility(View.VISIBLE);
        } else {
            recurringExpenseRecyclerView.setVisibility(View.VISIBLE);
            emptyStateText.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload recurring expenses when returning to this activity
        loadRecurringExpenses();
    }
} 