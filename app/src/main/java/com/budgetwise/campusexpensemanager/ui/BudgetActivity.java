package com.budgetwise.campusexpensemanager.ui;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import com.budgetwise.campusexpensemanager.R;
import com.budgetwise.campusexpensemanager.firebase.repository.BudgetRepository;
import com.budgetwise.campusexpensemanager.firebase.models.FirebaseBudget;
import com.budgetwise.campusexpensemanager.utils.SessionManager;
import com.budgetwise.campusexpensemanager.notifications.RecurringExpenseProcessor;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class BudgetActivity extends BaseActivity {

    private RecyclerView budgetRecyclerView;
    private TextView emptyStateText;
    private FloatingActionButton addBudgetFab;
    private Spinner monthSpinner;
    private Spinner yearSpinner;
    private BudgetAdapter budgetAdapter;
    private BudgetRepository budgetRepository;
    private SessionManager sessionManager;
    private RecurringExpenseProcessor recurringExpenseProcessor;
    
    private int selectedMonth;
    private int selectedYear;
    private List<FirebaseBudget> budgets;
    private List<FirebaseBudget> allBudgets = new ArrayList<>();
    private List<FirebaseBudget> filteredBudgets = new ArrayList<>();

    // Filter components
    private Spinner monthFilterSpinner;
    private Spinner yearFilterSpinner;
    private Spinner categoryFilterSpinner;
    private MaterialButton totalBudgetSortButton;
    private MaterialButton clearFiltersButton;
    private View filterHeader;
    private View filterContent;
    private ImageView expandCollapseIcon;
    
    // Filter states
    private String selectedCategory = "All";
    private String totalBudgetSortOrder = "asc";
    private boolean isFilterExpanded = false;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_budget;
    }

    @Override
    protected void setupActivity() {
        // Initialize repositories
        budgetRepository = new BudgetRepository();
        sessionManager = new SessionManager(this);
        recurringExpenseProcessor = new RecurringExpenseProcessor();
        budgets = new ArrayList<>();

        // Initialize views
        initializeViews();
        setupToolbar("Budget Settings");
        setupDrawer();
        setupRecyclerView();
        setupFilterSpinners();
        setupFilterClickListeners();
        loadBudgets();
        
        // Process recurring expenses to ensure they're included in budget calculations
        processRecurringExpenses();
    }

    private void initializeViews() {
        budgetRecyclerView = findViewById(R.id.budget_recycler_view);
        emptyStateText = findViewById(R.id.empty_state_text);
        addBudgetFab = findViewById(R.id.add_budget_fab);
        monthSpinner = findViewById(R.id.month_spinner);
        yearSpinner = findViewById(R.id.year_spinner);
        
        // Initialize filter components
        monthFilterSpinner = findViewById(R.id.month_filter_spinner);
        yearFilterSpinner = findViewById(R.id.year_filter_spinner);
        categoryFilterSpinner = findViewById(R.id.category_filter_spinner);
        totalBudgetSortButton = findViewById(R.id.total_budget_sort_button);
        clearFiltersButton = findViewById(R.id.clear_filters_button);
        filterHeader = findViewById(R.id.filter_header);
        filterContent = findViewById(R.id.filter_content);
        expandCollapseIcon = findViewById(R.id.expand_collapse_icon);
    }

    private void setupRecyclerView() {
        budgetAdapter = new BudgetAdapter(this, budgets);
        budgetRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        budgetRecyclerView.setHasFixedSize(true);
        budgetRecyclerView.setAdapter(budgetAdapter);

        // Setup FAB click listener
        addBudgetFab.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditBudgetActivity.class);
            intent.putExtra("month", selectedMonth);
            intent.putExtra("year", selectedYear);
            startActivity(intent);
        });
    }

    private void setupFilterSpinners() {
        // Setup month filter spinner
        String[] months = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        };
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<String>(this, 
            android.R.layout.simple_spinner_item, months) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (view instanceof TextView) {
                    ((TextView) view).setTextColor(getResources().getColor(R.color.textPrimary));
                }
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                if (view instanceof TextView) {
                    ((TextView) view).setTextColor(getResources().getColor(R.color.textPrimary));
                }
                return view;
            }
        };
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthFilterSpinner.setAdapter(monthAdapter);

        // Setup year filter spinner
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        String[] years = new String[6];
        for (int i = 0; i < 6; i++) {
            years[i] = String.valueOf(currentYear - 2 + i);
        }
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<String>(this, 
            android.R.layout.simple_spinner_item, years) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (view instanceof TextView) {
                    ((TextView) view).setTextColor(getResources().getColor(R.color.textPrimary));
                }
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                if (view instanceof TextView) {
                    ((TextView) view).setTextColor(getResources().getColor(R.color.textPrimary));
                }
                return view;
            }
        };
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearFilterSpinner.setAdapter(yearAdapter);

        // Setup category filter spinner
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

        // Set current month and year
        selectedMonth = calendar.get(Calendar.MONTH) + 1; // Calendar.MONTH is 0-based
        selectedYear = currentYear;
        monthFilterSpinner.setSelection(selectedMonth - 1);
        yearFilterSpinner.setSelection(2); // Current year is at index 2 (currentYear - 2 + 2)
        categoryFilterSpinner.setSelection(0); // Default to "All"
    }

    private void setupFilterClickListeners() {
        // Month filter listener
        monthFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedMonth = position + 1;
                loadBudgets();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Year filter listener
        yearFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Calendar calendar = Calendar.getInstance();
                int currentYear = calendar.get(Calendar.YEAR);
                selectedYear = currentYear - 2 + position;
                loadBudgets();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Category filter listener
        categoryFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = (String) parent.getItemAtPosition(position);
                loadBudgets();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Total budget sort button listener
        totalBudgetSortButton.setOnClickListener(v -> {
            if (totalBudgetSortOrder.equals("asc")) {
                totalBudgetSortOrder = "desc";
            } else {
                totalBudgetSortOrder = "asc";
            }
            updateSortButtonIcon(totalBudgetSortButton, totalBudgetSortOrder);
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
    }

    private void loadBudgets() {
        String currentUser = sessionManager.getUsername();
        if (currentUser == null) {
            showEmptyState();
            return;
        }

        Log.d("BudgetActivity", "Loading budgets for user: " + currentUser + ", month: " + selectedMonth + ", year: " + selectedYear);

        Query query = budgetRepository.getBudgetByAccountAndMonth(currentUser, selectedMonth, selectedYear);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                allBudgets.clear();
                Log.d("BudgetActivity", "DataSnapshot exists: " + dataSnapshot.exists() + ", children count: " + dataSnapshot.getChildrenCount());
                
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    FirebaseBudget budget = snapshot.getValue(FirebaseBudget.class);
                    if (budget != null && budget.getMonth() == selectedMonth && budget.getYear() == selectedYear) {
                        budget.setId(snapshot.getKey());
                        allBudgets.add(budget);
                        Log.d("BudgetActivity", "Added budget - ID: " + budget.getId() + 
                              ", Category: " + budget.getCategory() + 
                              ", Limit: " + budget.getLimit() + 
                              ", Month: " + budget.getMonth() + 
                              ", Year: " + budget.getYear());
                    } else if (budget != null) {
                        Log.d("BudgetActivity", "Skipped budget - ID: " + snapshot.getKey() + 
                              ", Category: " + budget.getCategory() + 
                              ", Limit: " + budget.getLimit() + 
                              ", Month: " + budget.getMonth() + 
                              ", Year: " + budget.getYear() + 
                              " (not matching selected month/year)");
                    }
                }
                Log.d("BudgetActivity", "Total budgets loaded: " + allBudgets.size());
                applyFilters();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                showEmptyState();
            }
        });
    }
    
    private void applyFilters() {
        filteredBudgets.clear();
        filteredBudgets.addAll(allBudgets);
        
        Log.d("BudgetActivity", "Applying filters - All budgets: " + allBudgets.size() + 
              ", Category filter: " + selectedCategory + 
              ", Sort order: " + totalBudgetSortOrder);
        
        // Apply category filter
        if (!selectedCategory.equals("All")) {
            int beforeFilter = filteredBudgets.size();
            filteredBudgets.removeIf(budget -> !budget.getCategory().equals(selectedCategory));
            int afterFilter = filteredBudgets.size();
            Log.d("BudgetActivity", "Category filter applied - Before: " + beforeFilter + ", After: " + afterFilter);
        }

        // Apply sorting - prioritize total budget sorting
        if (totalBudgetSortOrder.equals("asc")) {
            Collections.sort(filteredBudgets, Comparator.comparing(FirebaseBudget::getLimit));
            Log.d("BudgetActivity", "Sorted budgets in ascending order");
        } else if (totalBudgetSortOrder.equals("desc")) {
            Collections.sort(filteredBudgets, (b1, b2) -> Double.compare(b2.getLimit(), b1.getLimit()));
            Log.d("BudgetActivity", "Sorted budgets in descending order");
        }
        
        // Update adapter
        budgets.clear();
        budgets.addAll(filteredBudgets);
        budgetAdapter.notifyDataSetChanged();
        
        Log.d("BudgetActivity", "Final filtered budgets: " + filteredBudgets.size());
        for (FirebaseBudget budget : filteredBudgets) {
            Log.d("BudgetActivity", "Filtered budget - Category: " + budget.getCategory() + 
                  ", Limit: " + budget.getLimit() + 
                  ", Month: " + budget.getMonth() + 
                  ", Year: " + budget.getYear());
        }
        
        if (filteredBudgets.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
        }
    }

    private void updateSortButtonIcon(MaterialButton button, String sortOrder) {
        if (sortOrder.equals("asc")) {
            button.setIcon(getDrawable(R.drawable.ic_arrow_up));
        } else if (sortOrder.equals("desc")) {
            button.setIcon(getDrawable(R.drawable.ic_arrow_down));
        }
    }

    private void clearAllFilters() {
        // Reset spinner selections
        categoryFilterSpinner.setSelection(0);
        
        // Reset sort states
        totalBudgetSortOrder = "asc";
        
        // Reset filter states
        selectedCategory = "All";
        
        // Reset button icons
        updateSortButtonIcon(totalBudgetSortButton, totalBudgetSortOrder);
        
        // Apply filters
        applyFilters();
    }

    private void showEmptyState() {
        emptyStateText.setVisibility(View.VISIBLE);
        budgetRecyclerView.setVisibility(View.GONE);
    }

    private void hideEmptyState() {
        emptyStateText.setVisibility(View.GONE);
        budgetRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBudgets();
        processRecurringExpenses();
    }
    
    private void processRecurringExpenses() {
        String accountId = sessionManager.getAccountId();
        if (accountId != null) {
            recurringExpenseProcessor.processRecurringExpenses(accountId);
        }
    }
} 