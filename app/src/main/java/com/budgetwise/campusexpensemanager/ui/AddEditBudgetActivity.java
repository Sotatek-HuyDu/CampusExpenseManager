package com.budgetwise.campusexpensemanager.ui;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.budgetwise.campusexpensemanager.R;
import com.budgetwise.campusexpensemanager.firebase.repository.BudgetRepository;
import com.budgetwise.campusexpensemanager.firebase.models.FirebaseBudget;
import com.budgetwise.campusexpensemanager.models.ExpenseCategory;
import com.budgetwise.campusexpensemanager.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class AddEditBudgetActivity extends AppCompatActivity {

    private AutoCompleteTextView categorySpinner;
    private TextInputEditText amountEditText;
    private AutoCompleteTextView monthSpinner;
    private AutoCompleteTextView yearSpinner;
    private MaterialButton saveButton;
    private MaterialButton deleteButton;
    private Toolbar toolbar;

    private BudgetRepository budgetRepository;
    private SessionManager sessionManager;
    private String budgetId;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_budget);

        // Initialize repositories
        budgetRepository = new BudgetRepository();
        sessionManager = new SessionManager(this);

        // Initialize views
        initializeViews();
        setupToolbar();
        setupSpinners();
        setupButtons();

        // Check if we're editing an existing budget
        budgetId = getIntent().getStringExtra("budget_id");
        if (budgetId != null) {
            isEditMode = true;
            loadBudget(budgetId);
        } else {
            // Set default month and year from intent extras
            int month = getIntent().getIntExtra("month", Calendar.getInstance().get(Calendar.MONTH) + 1);
            int year = getIntent().getIntExtra("year", Calendar.getInstance().get(Calendar.YEAR));
            monthSpinner.setText(getMonthName(month), false);
            yearSpinner.setText(String.valueOf(year), false);
        }
    }

    private void initializeViews() {
        categorySpinner = findViewById(R.id.category_spinner);
        amountEditText = findViewById(R.id.amount_edit_text);
        monthSpinner = findViewById(R.id.month_spinner);
        yearSpinner = findViewById(R.id.year_spinner);
        saveButton = findViewById(R.id.save_button);
        deleteButton = findViewById(R.id.delete_button);
        toolbar = findViewById(R.id.toolbar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(isEditMode ? "Edit Budget" : "Add Budget");
        }
    }

    private void setupSpinners() {
        // Setup category spinner
        String[] categories = new String[ExpenseCategory.values().length];
        for (int i = 0; i < ExpenseCategory.values().length; i++) {
            categories[i] = ExpenseCategory.values()[i].getDisplayName();
        }

        CategorySpinnerAdapter categoryAdapter = new CategorySpinnerAdapter(this, java.util.Arrays.asList(categories));
        categorySpinner.setAdapter(categoryAdapter);
        categorySpinner.setText(categories[0], false);

        // Setup month spinner
        String[] months = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        };
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_dropdown_item_1line, months);
        monthSpinner.setAdapter(monthAdapter);

        // Setup year spinner
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        String[] years = {String.valueOf(currentYear), String.valueOf(currentYear + 1), String.valueOf(currentYear + 2)};
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_dropdown_item_1line, years);
        yearSpinner.setAdapter(yearAdapter);
    }

    private void setupButtons() {
        android.util.Log.d("AddEditBudget", "setupButtons() called");
        saveButton.setOnClickListener(v -> {
            android.util.Log.d("AddEditBudget", "Save button clicked");
            saveBudget();
        });
        deleteButton.setOnClickListener(v -> showDeleteConfirmation());
    }

    private void loadBudget(String budgetId) {
        budgetRepository.getBudgetById(budgetId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    FirebaseBudget budget = dataSnapshot.getValue(FirebaseBudget.class);
                    if (budget != null) {
                        budget.setId(dataSnapshot.getKey());
                        populateFields(budget);
                        deleteButton.setVisibility(android.view.View.VISIBLE);
                        if (!isFinishing() && getSupportActionBar() != null) {
                            getSupportActionBar().setTitle("Edit Budget");
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(AddEditBudgetActivity.this, 
                        "Error loading budget: " + databaseError.getMessage(), 
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void populateFields(FirebaseBudget budget) {
        categorySpinner.setText(budget.getCategory(), false);
        amountEditText.setText(String.valueOf(budget.getLimit()));
        monthSpinner.setText(getMonthName(budget.getMonth()), false);
        yearSpinner.setText(String.valueOf(budget.getYear()), false);
    }

    private void saveBudget() {
        android.util.Log.d("AddEditBudget", "saveBudget() called");
        
        String category = categorySpinner.getText().toString();
        String amountStr = amountEditText.getText().toString().trim();
        String monthStr = monthSpinner.getText().toString();
        String yearStr = yearSpinner.getText().toString();

        android.util.Log.d("AddEditBudget", "Form data - Category: " + category + ", Amount: " + amountStr + ", Month: " + monthStr + ", Year: " + yearStr);

        // Validation
        if (category.isEmpty()) {
            android.util.Log.d("AddEditBudget", "Category is empty");
            categorySpinner.setError("Category is required");
            return;
        }

        if (amountStr.isEmpty()) {
            android.util.Log.d("AddEditBudget", "Amount is empty");
            amountEditText.setError("Amount is required");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                android.util.Log.d("AddEditBudget", "Amount is <= 0");
                amountEditText.setError("Amount must be greater than 0");
                return;
            }
        } catch (NumberFormatException e) {
            android.util.Log.d("AddEditBudget", "Invalid amount format");
            amountEditText.setError("Invalid amount");
            return;
        }

        if (monthStr.isEmpty()) {
            android.util.Log.d("AddEditBudget", "Month is empty");
            monthSpinner.setError("Month is required");
            return;
        }

        if (yearStr.isEmpty()) {
            android.util.Log.d("AddEditBudget", "Year is empty");
            yearSpinner.setError("Year is required");
            return;
        }

        int month = getMonthNumber(monthStr);
        int year = Integer.parseInt(yearStr);

        String currentUser = sessionManager.getUsername();
        android.util.Log.d("AddEditBudget", "Current user: " + currentUser);
        
        if (currentUser == null) {
            android.util.Log.d("AddEditBudget", "User not logged in");
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        android.util.Log.d("AddEditBudget", "Creating budget - Category: " + category + ", Amount: " + amount + ", Month: " + month + ", Year: " + year);
        FirebaseBudget budget = new FirebaseBudget(currentUser, category, amount, month, year);
        
        if (isEditMode) {
            android.util.Log.d("AddEditBudget", "Updating existing budget");
            budget.setId(budgetId);
            updateBudget(budget);
        } else {
            android.util.Log.d("AddEditBudget", "Adding new budget");
            addBudget(budget);
        }
    }

    private void addBudget(FirebaseBudget budget) {
        android.util.Log.d("AddEditBudget", "addBudget() called");
        android.util.Log.d("AddEditBudget", "Budget object: " + budget.getCategory() + ", " + budget.getLimit() + ", " + budget.getAccountId());
        
        try {
            android.util.Log.d("AddEditBudget", "Starting Firebase operation...");
            
            budgetRepository.insert(budget)
                    .addOnSuccessListener(aVoid -> {
                        android.util.Log.d("AddEditBudget", "Budget added successfully");
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Budget added successfully", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    })
                    .addOnFailureListener(e -> {
                        android.util.Log.e("AddEditBudget", "Error adding budget: " + e.getMessage(), e);
                        runOnUiThread(() -> {
                            Toast.makeText(this, "Error adding budget: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    })
                    .addOnCompleteListener(task -> {
                        android.util.Log.d("AddEditBudget", "Firebase operation completed. Success: " + task.isSuccessful());
                        if (!task.isSuccessful()) {
                            android.util.Log.e("AddEditBudget", "Task failed with exception: " + task.getException());
                        }
                    });
            
        } catch (Exception e) {
            android.util.Log.e("AddEditBudget", "Exception in addBudget: " + e.getMessage(), e);
            Toast.makeText(this, "Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateBudget(FirebaseBudget budget) {
        android.util.Log.d("AddEditBudget", "updateBudget() called");
        budgetRepository.update(budget)
                .addOnSuccessListener(aVoid -> {
                    android.util.Log.d("AddEditBudget", "Budget updated successfully");
                    Toast.makeText(this, "Budget updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    android.util.Log.d("AddEditBudget", "Error updating budget: " + e.getMessage());
                    Toast.makeText(this, "Error updating budget: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Budget")
                .setMessage("Are you sure you want to delete this budget?")
                .setPositiveButton("Delete", (dialog, which) -> deleteBudget())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteBudget() {
        if (budgetId != null) {
            FirebaseBudget budget = new FirebaseBudget();
            budget.setId(budgetId);
            
            budgetRepository.delete(budget)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Budget deleted successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error deleting budget: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private String getMonthName(int month) {
        String[] months = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        };
        return months[month - 1];
    }

    private int getMonthNumber(String monthName) {
        String[] months = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        };
        for (int i = 0; i < months.length; i++) {
            if (months[i].equals(monthName)) {
                return i + 1;
            }
        }
        return 1; // Default to January
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 