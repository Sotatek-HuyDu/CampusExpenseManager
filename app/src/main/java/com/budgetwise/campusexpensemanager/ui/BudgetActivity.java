package com.budgetwise.campusexpensemanager.ui;

import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import com.budgetwise.campusexpensemanager.R;
import com.budgetwise.campusexpensemanager.firebase.repository.BudgetRepository;
import com.budgetwise.campusexpensemanager.firebase.models.FirebaseBudget;
import com.budgetwise.campusexpensemanager.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class BudgetActivity extends BaseActivity {

    private RecyclerView budgetRecyclerView;
    private TextView emptyStateText;
    private FloatingActionButton addBudgetFab;
    private Spinner monthSpinner;
    private Spinner yearSpinner;
    private BudgetAdapter budgetAdapter;
    private BudgetRepository budgetRepository;
    private SessionManager sessionManager;
    
    private int selectedMonth;
    private int selectedYear;
    private List<FirebaseBudget> budgets;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_budget;
    }

    @Override
    protected void setupActivity() {
        // Initialize repositories
        budgetRepository = new BudgetRepository();
        sessionManager = new SessionManager(this);
        budgets = new ArrayList<>();

        // Initialize views
        budgetRecyclerView = findViewById(R.id.budget_recycler_view);
        emptyStateText = findViewById(R.id.empty_state_text);
        addBudgetFab = findViewById(R.id.add_budget_fab);
        monthSpinner = findViewById(R.id.month_spinner);
        yearSpinner = findViewById(R.id.year_spinner);

        // Setup toolbar
        setupToolbar();

        // Setup spinners
        setupMonthYearSpinners();

        // Setup RecyclerView
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

        // Load budgets for current month/year
        loadBudgets();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Budget Settings");
        }
    }

    private void setupMonthYearSpinners() {
        // Setup month spinner
        String[] months = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        };
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_spinner_item, months);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(monthAdapter);

        // Setup year spinner (current year and next 2 years)
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        String[] years = {String.valueOf(currentYear), String.valueOf(currentYear + 1), String.valueOf(currentYear + 2)};
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(yearAdapter);

        // Set current month and year
        selectedMonth = calendar.get(Calendar.MONTH) + 1; // Calendar.MONTH is 0-based
        selectedYear = currentYear;
        monthSpinner.setSelection(selectedMonth - 1);
        yearSpinner.setSelection(0);

        // Setup listeners
        monthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedMonth = position + 1;
                loadBudgets();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        yearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedYear = Integer.parseInt(years[position]);
                loadBudgets();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadBudgets() {
        String currentUser = sessionManager.getUsername();
        if (currentUser == null) {
            showEmptyState();
            return;
        }

        Query query = budgetRepository.getBudgetByAccountAndMonth(currentUser, selectedMonth, selectedYear);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                budgets.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    FirebaseBudget budget = snapshot.getValue(FirebaseBudget.class);
                    if (budget != null && budget.getMonth() == selectedMonth && budget.getYear() == selectedYear) {
                        budget.setId(snapshot.getKey());
                        budgets.add(budget);
                    }
                }
                
                if (budgets.isEmpty()) {
                    showEmptyState();
                } else {
                    hideEmptyState();
                }
                
                budgetAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                showEmptyState();
            }
        });
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
    }
} 