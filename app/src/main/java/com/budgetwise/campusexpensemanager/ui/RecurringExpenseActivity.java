package com.budgetwise.campusexpensemanager.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.budgetwise.campusexpensemanager.R;
import com.budgetwise.campusexpensemanager.firebase.repository.RecurringExpenseRepository;
import com.budgetwise.campusexpensemanager.models.RecurringExpense;
import com.budgetwise.campusexpensemanager.ui.RecurringExpenseAdapter;
import com.budgetwise.campusexpensemanager.ui.CategorySpinnerAdapter;
import com.budgetwise.campusexpensemanager.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.widget.Toolbar;

import com.budgetwise.campusexpensemanager.firebase.models.FirebaseRecurringExpense;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RecurringExpenseActivity extends BaseActivity {

    private RecyclerView recurringExpenseRecyclerView;
    private TextView emptyStateText;
    private FloatingActionButton addRecurringExpenseFab;
    private RecurringExpenseAdapter recurringExpenseAdapter;
    private RecurringExpenseRepository recurringExpenseRepository;
    private SessionManager sessionManager;
    private List<FirebaseRecurringExpense> allRecurringExpenses = new ArrayList<>();
    private List<FirebaseRecurringExpense> filteredRecurringExpenses = new ArrayList<>();

    // Filter components
    private Spinner categoryFilterSpinner;
    private MaterialButton amountSortButton;
    private MaterialButton clearFiltersButton;
    private View filterHeader;
    private View filterContent;
    private ImageView expandCollapseIcon;

    // Filter states
    private String selectedCategory = "All";
    private String amountSortOrder = "none"; // "asc", "desc", or "none"
    private boolean isFilterExpanded = false;

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
        initializeViews();
        setupToolbar("Recurring Expenses");
        setupDrawer();
        setupRecyclerView();
        setupFilterSpinners();
        setupFilterClickListeners();
        loadRecurringExpenses();
    }

    private void initializeViews() {
        recurringExpenseRecyclerView = findViewById(R.id.recurring_expense_recycler_view);
        emptyStateText = findViewById(R.id.empty_state_text);
        addRecurringExpenseFab = findViewById(R.id.add_recurring_expense_fab);
        
        // Initialize filter components
        categoryFilterSpinner = findViewById(R.id.category_filter_spinner);
        amountSortButton = findViewById(R.id.amount_sort_button);
        clearFiltersButton = findViewById(R.id.clear_filters_button);
        filterHeader = findViewById(R.id.filter_header);
        filterContent = findViewById(R.id.filter_content);
        expandCollapseIcon = findViewById(R.id.expand_collapse_icon);
    }

    private void setupRecyclerView() {
        recurringExpenseAdapter = new RecurringExpenseAdapter(this);
        recurringExpenseRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        recurringExpenseRecyclerView.setHasFixedSize(true);
        recurringExpenseRecyclerView.setAdapter(recurringExpenseAdapter);
    }

    private void setupFilterSpinners() {
        // Setup category filter spinner with custom adapter
        List<String> categories = new ArrayList<>();
        categories.add("All");
        categories.add("Groceries");
        categories.add("Entertainment");
        categories.add("Education");
        categories.add("Health");
        categories.add("Shopping");
        categories.add("Dining");
        categories.add("Utilities");
        categories.add("Transportation");
        categories.add("Other");
        
        CategorySpinnerAdapter categoryAdapter = new CategorySpinnerAdapter(this, categories);
        categoryFilterSpinner.setAdapter(categoryAdapter);
    }

    private void setupFilterClickListeners() {
        // Category filter listener
        categoryFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = parent.getItemAtPosition(position).toString();
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Amount sort button listener
        amountSortButton.setOnClickListener(v -> {
            if (amountSortOrder.equals("asc")) {
                amountSortOrder = "desc";
            } else if (amountSortOrder.equals("desc")) {
                amountSortOrder = "none";
            } else {
                amountSortOrder = "asc";
            }
            updateSortButtonIcon(amountSortButton, amountSortOrder);
            applyFilters();
        });

        // Clear filters button listener
        clearFiltersButton.setOnClickListener(v -> {
            clearAllFilters();
        });

        // Filter header click listener
        filterHeader.setOnClickListener(v -> {
            isFilterExpanded = !isFilterExpanded;
            if (isFilterExpanded) {
                filterContent.setVisibility(View.VISIBLE);
                expandCollapseIcon.setImageResource(R.drawable.ic_arrow_up);
            } else {
                filterContent.setVisibility(View.GONE);
                expandCollapseIcon.setImageResource(R.drawable.ic_arrow_down);
            }
        });

        // Set initial state to collapsed
        filterContent.setVisibility(View.GONE);
        expandCollapseIcon.setImageResource(R.drawable.ic_arrow_down);

        // Setup FAB click listener
        addRecurringExpenseFab.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditRecurringExpenseActivity.class);
            startActivity(intent);
        });
    }

    private void loadRecurringExpenses() {
        String accountId = sessionManager.getAccountId();
        if (accountId != null) {
            recurringExpenseRepository.getRecurringExpensesByAccount(accountId)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            allRecurringExpenses.clear();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                FirebaseRecurringExpense recurringExpense = snapshot.getValue(FirebaseRecurringExpense.class);
                                if (recurringExpense != null) {
                                    recurringExpense.setId(snapshot.getKey());
                                    allRecurringExpenses.add(recurringExpense);
                                }
                            }
                            applyFilters();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(RecurringExpenseActivity.this, "Error loading recurring expenses: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void applyFilters() {
        filteredRecurringExpenses.clear();
        
        // Apply category filter
        for (FirebaseRecurringExpense expense : allRecurringExpenses) {
            if (selectedCategory.equals("All") || expense.getCategory().equals(selectedCategory)) {
                filteredRecurringExpenses.add(expense);
            }
        }
        
        // Apply amount sorting
        if (amountSortOrder.equals("asc")) {
            Collections.sort(filteredRecurringExpenses, Comparator.comparing(FirebaseRecurringExpense::getAmount));
        } else if (amountSortOrder.equals("desc")) {
            Collections.sort(filteredRecurringExpenses, (e1, e2) -> Double.compare(e2.getAmount(), e1.getAmount()));
        }
        
        // Update adapter
        recurringExpenseAdapter.updateRecurringExpenses(filteredRecurringExpenses);
        updateEmptyState();
    }

    private void updateSortButtonIcon(MaterialButton button, String sortOrder) {
        if (sortOrder.equals("asc")) {
            button.setIcon(getDrawable(R.drawable.ic_arrow_up));
        } else if (sortOrder.equals("desc")) {
            button.setIcon(getDrawable(R.drawable.ic_arrow_down));
        } else {
            button.setIcon(null); // Clear icon for "none" state
        }
    }

    private void clearAllFilters() {
        // Reset spinner
        categoryFilterSpinner.setSelection(0);
        
        // Reset filter states
        selectedCategory = "All";
        amountSortOrder = "none";
        
        // Reset button icon
        updateSortButtonIcon(amountSortButton, amountSortOrder);
        
        // Apply filters
        applyFilters();
    }

    private void updateEmptyState() {
        if (filteredRecurringExpenses.isEmpty()) {
            emptyStateText.setVisibility(View.VISIBLE);
            recurringExpenseRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateText.setVisibility(View.GONE);
            recurringExpenseRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload recurring expenses when returning to this activity
        loadRecurringExpenses();
    }
} 