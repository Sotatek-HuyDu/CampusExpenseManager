package com.budgetwise.campusexpensemanager.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.budgetwise.campusexpensemanager.R;
import com.budgetwise.campusexpensemanager.firebase.ExpenseRepository;
import com.budgetwise.campusexpensemanager.models.Expense;
import com.budgetwise.campusexpensemanager.ui.ExpenseAdapter;
import com.budgetwise.campusexpensemanager.ui.CategorySpinnerAdapter;
import com.budgetwise.campusexpensemanager.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import android.widget.ImageView;

public class ExpenseActivity extends BaseActivity {

    private RecyclerView expenseRecyclerView;
    private TextView emptyStateText;
    private FloatingActionButton addExpenseFab;
    private ExpenseAdapter expenseAdapter;
    private ExpenseRepository expenseRepository;
    private SessionManager sessionManager;
    private List<Expense> allExpenses = new ArrayList<>();
    private List<Expense> filteredExpenses = new ArrayList<>();

    // Filter components
    private Spinner categoryFilterSpinner;
    private Spinner monthFilterSpinner;
    private Spinner yearFilterSpinner;
    private MaterialButton dateSortButton;
    private MaterialButton amountSortButton;
    private MaterialButton clearFiltersButton;
    private View filterHeader;
    private View filterContent;
    private ImageView expandCollapseIcon;

    // Filter states
    private String selectedCategory = "All";
    private int selectedMonth = -1; // -1 means all months
    private int selectedYear = -1; // -1 means all years
    private String dateSortOrder = "none"; // "asc", "desc", or "none"
    private String amountSortOrder = "none"; // "asc", "desc", or "none"
    private boolean isFilterExpanded = false;

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
        initializeViews();
        setupToolbar("Expenses");
        setupDrawer();
        setupRecyclerView();
        setupFilterSpinners();
        setupFilterClickListeners();
        loadExpenses();
    }

    private void initializeViews() {
        expenseRecyclerView = findViewById(R.id.expense_recycler_view);
        emptyStateText = findViewById(R.id.empty_state_text);
        addExpenseFab = findViewById(R.id.add_expense_fab);
        
        // Initialize filter components
        categoryFilterSpinner = findViewById(R.id.category_filter_spinner);
        monthFilterSpinner = findViewById(R.id.month_filter_spinner);
        yearFilterSpinner = findViewById(R.id.year_filter_spinner);
        dateSortButton = findViewById(R.id.date_sort_button);
        amountSortButton = findViewById(R.id.amount_sort_button);
        clearFiltersButton = findViewById(R.id.clear_filters_button);
        filterHeader = findViewById(R.id.filter_header);
        filterContent = findViewById(R.id.filter_content);
        expandCollapseIcon = findViewById(R.id.expand_collapse_icon);
    }

    private void setupRecyclerView() {
        expenseAdapter = new ExpenseAdapter(this);
        expenseRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        expenseRecyclerView.setHasFixedSize(true);
        expenseRecyclerView.setAdapter(expenseAdapter);
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

        // Setup month filter spinner
        String[] months = {"All Months", "January", "February", "March", "April", "May", "June",
                          "July", "August", "September", "October", "November", "December"};
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, months);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthFilterSpinner.setAdapter(monthAdapter);

        // Setup year filter spinner
        Calendar cal = Calendar.getInstance();
        int currentYear = cal.get(Calendar.YEAR);
        int currentMonth = cal.get(Calendar.MONTH); // 0-11
        
        String[] years = new String[6];
        years[0] = "All Years";
        for (int i = 1; i < 6; i++) {
            years[i] = String.valueOf(currentYear - 2 + i);
        }
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearFilterSpinner.setAdapter(yearAdapter);
        
        // Set default selections to current month and year
        monthFilterSpinner.setSelection(currentMonth + 1); // +1 because "All Months" is at position 0
        yearFilterSpinner.setSelection(3); // Current year is at position 3 (currentYear - 2 + 3 = currentYear)
        
        // Update the filter state variables
        selectedMonth = currentMonth;
        selectedYear = currentYear;
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

        // Month filter listener
        monthFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedMonth = -1; // All months
                } else {
                    selectedMonth = position - 1; // 0-11 for Calendar.MONTH
                }
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Year filter listener
        yearFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    selectedYear = -1; // All years
                } else {
                    selectedYear = Integer.parseInt(parent.getItemAtPosition(position).toString());
                }
                applyFilters();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Date sort button listener
        dateSortButton.setOnClickListener(v -> {
            if (dateSortOrder.equals("asc")) {
                dateSortOrder = "desc";
            } else if (dateSortOrder.equals("desc")) {
                dateSortOrder = "none";
            } else {
                dateSortOrder = "asc";
            }
            updateSortButtonIcon(dateSortButton, dateSortOrder);
            applyFilters();
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
        addExpenseFab.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditExpenseActivity.class);
            startActivity(intent);
        });
    }

    private void loadExpenses() {
        String accountId = sessionManager.getAccountId();
        if (accountId != null && !accountId.isEmpty()) {
            expenseRepository.getExpensesByUser(accountId, new ExpenseRepository.ExpenseCallback() {
                @Override
                public void onSuccess(List<Expense> expenses) {
                    allExpenses.clear();
                    allExpenses.addAll(expenses);
                    applyFilters();
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(ExpenseActivity.this, "Error loading expenses: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void applyFilters() {
        filteredExpenses.clear();
        
        // Apply category filter
        for (Expense expense : allExpenses) {
            if (selectedCategory.equals("All") || expense.getCategory().equals(selectedCategory)) {
                // Apply month filter
                if (selectedMonth == -1 || (expense.getDate() != null && expense.getDate().getMonth() == selectedMonth)) {
                    // Apply year filter
                    if (selectedYear == -1 || (expense.getDate() != null && expense.getDate().getYear() + 1900 == selectedYear)) {
                        filteredExpenses.add(expense);
                    }
                }
            }
        }
        
        // Apply sorting - prioritize date sorting
        if (dateSortOrder.equals("asc")) {
            Collections.sort(filteredExpenses, (e1, e2) -> {
                // Handle null dates by using timestamp
                if (e1.getDate() == null && e2.getDate() == null) {
                    return Long.compare(e1.getTimestamp(), e2.getTimestamp());
                }
                if (e1.getDate() == null) return 1;
                if (e2.getDate() == null) return -1;
                int dateCompare = e1.getDate().compareTo(e2.getDate());
                // If dates are equal, use amount as secondary sort
                if (dateCompare == 0 && amountSortOrder.equals("asc")) {
                    return Double.compare(e1.getAmount(), e2.getAmount());
                } else if (dateCompare == 0 && amountSortOrder.equals("desc")) {
                    return Double.compare(e2.getAmount(), e1.getAmount());
                }
                return dateCompare;
            });
        } else if (dateSortOrder.equals("desc")) {
            Collections.sort(filteredExpenses, (e1, e2) -> {
                // Handle null dates by using timestamp
                if (e1.getDate() == null && e2.getDate() == null) {
                    return Long.compare(e2.getTimestamp(), e1.getTimestamp());
                }
                if (e1.getDate() == null) return 1;
                if (e2.getDate() == null) return -1;
                int dateCompare = e2.getDate().compareTo(e1.getDate());
                // If dates are equal, use amount as secondary sort
                if (dateCompare == 0 && amountSortOrder.equals("asc")) {
                    return Double.compare(e1.getAmount(), e2.getAmount());
                } else if (dateCompare == 0 && amountSortOrder.equals("desc")) {
                    return Double.compare(e2.getAmount(), e1.getAmount());
                }
                return dateCompare;
            });
        }
        
        // Only apply amount sorting if date sorting is not active
        if (dateSortOrder.equals("none")) {
            if (amountSortOrder.equals("asc")) {
                Collections.sort(filteredExpenses, Comparator.comparing(Expense::getAmount));
            } else if (amountSortOrder.equals("desc")) {
                Collections.sort(filteredExpenses, (e1, e2) -> Double.compare(e2.getAmount(), e1.getAmount()));
            }
        }
        
        // Update adapter
        expenseAdapter.updateExpenses(filteredExpenses);
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
        // Reset spinners
        categoryFilterSpinner.setSelection(0);
        monthFilterSpinner.setSelection(0);
        yearFilterSpinner.setSelection(0);
        
        // Reset filter states
        selectedCategory = "All";
        selectedMonth = -1;
        selectedYear = -1;
        dateSortOrder = "none";
        amountSortOrder = "none";
        
        // Reset button icons
        updateSortButtonIcon(dateSortButton, dateSortOrder);
        updateSortButtonIcon(amountSortButton, amountSortOrder);
        
        // Apply filters
        applyFilters();
    }

    private void updateEmptyState() {
        if (filteredExpenses.isEmpty()) {
            emptyStateText.setVisibility(View.VISIBLE);
            expenseRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateText.setVisibility(View.GONE);
            expenseRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload expenses when returning to this activity
        loadExpenses();
    }
} 