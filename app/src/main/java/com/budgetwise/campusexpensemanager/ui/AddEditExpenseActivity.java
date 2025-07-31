package com.budgetwise.campusexpensemanager.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.budgetwise.campusexpensemanager.R;
import com.budgetwise.campusexpensemanager.firebase.ExpenseRepository;
import com.budgetwise.campusexpensemanager.models.Expense;
import com.budgetwise.campusexpensemanager.models.ExpenseCategory;
import com.budgetwise.campusexpensemanager.utils.SessionManager;
import com.budgetwise.campusexpensemanager.utils.CategoryColorUtil;
import com.budgetwise.campusexpensemanager.ui.CategorySpinnerAdapter;
import com.budgetwise.campusexpensemanager.notifications.BudgetNotificationService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddEditExpenseActivity extends AppCompatActivity {

    private TextInputEditText descriptionEditText;
    private TextInputEditText amountEditText;
    private AutoCompleteTextView categorySpinner;
    private TextInputEditText dateEditText;
    private MaterialButton saveButton;
    private MaterialButton deleteButton;
    private Toolbar toolbar;

    private ExpenseRepository expenseRepository;
    private SessionManager sessionManager;
    private BudgetNotificationService budgetNotificationService;
    private Calendar selectedDate;
    private SimpleDateFormat dateFormat;
    private String expenseId;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_expense);

        // Initialize repositories
        expenseRepository = new ExpenseRepository();
        sessionManager = new SessionManager(this);
        budgetNotificationService = new BudgetNotificationService(this);

        // Initialize views
        initializeViews();
        setupToolbar();
        setupCategorySpinner();
        setupDatePicker();
        setupButtons();

        // Check if we're editing an existing expense
        expenseId = getIntent().getStringExtra("expense_id");
        if (expenseId != null) {
            isEditMode = true;
            loadExpense(expenseId);
        } else {
            // Set default date to today
            selectedDate = Calendar.getInstance();
            updateDateDisplay();
        }
        
        // Log user status
        String accountId = sessionManager.getAccountId();
        android.util.Log.d("AddEditExpense", "Account ID: " + accountId);
        android.util.Log.d("AddEditExpense", "Is logged in: " + sessionManager.isLoggedIn());
    }

    private void initializeViews() {
        descriptionEditText = findViewById(R.id.description_edit_text);
        amountEditText = findViewById(R.id.amount_edit_text);
        categorySpinner = findViewById(R.id.category_spinner);
        dateEditText = findViewById(R.id.date_edit_text);
        saveButton = findViewById(R.id.save_button);
        deleteButton = findViewById(R.id.delete_button);
        toolbar = findViewById(R.id.toolbar);

        selectedDate = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(isEditMode ? "Edit Expense" : "Add Expense");
        }
    }

    private void setupCategorySpinner() {
        String[] categories = new String[ExpenseCategory.values().length];
        for (int i = 0; i < ExpenseCategory.values().length; i++) {
            categories[i] = ExpenseCategory.values()[i].getDisplayName();
        }

        // Use the custom CategorySpinnerAdapter that supports colors
        CategorySpinnerAdapter adapter = new CategorySpinnerAdapter(this, java.util.Arrays.asList(categories));
        categorySpinner.setAdapter(adapter);
        categorySpinner.setText(categories[0], false); // Set default to first category
        
        // Set dropdown background to white
        categorySpinner.setDropDownBackgroundDrawable(getResources().getDrawable(android.R.color.white));
    }

    private void setupDatePicker() {
        dateEditText.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view, year, month, dayOfMonth) -> {
                        selectedDate.set(year, month, dayOfMonth);
                        updateDateDisplay();
                    },
                    selectedDate.get(Calendar.YEAR),
                    selectedDate.get(Calendar.MONTH),
                    selectedDate.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });
    }

    private void setupButtons() {
        saveButton.setOnClickListener(v -> {
            android.util.Log.d("AddEditExpense", "Save button clicked");
            saveExpense();
        });
        deleteButton.setOnClickListener(v -> showDeleteConfirmation());
    }

    private void updateDateDisplay() {
        dateEditText.setText(dateFormat.format(selectedDate.getTime()));
    }

    private void loadExpense(String expenseId) {
        expenseRepository.getExpenseById(expenseId, new ExpenseRepository.SingleExpenseCallback() {
            @Override
            public void onSuccess(Expense expense) {
                runOnUiThread(() -> {
                    descriptionEditText.setText(expense.getDescription());
                    amountEditText.setText(String.valueOf(expense.getAmount()));
                    categorySpinner.setText(expense.getCategory(), false);
                    
                    if (expense.getDate() != null) {
                        selectedDate.setTime(expense.getDate());
                        updateDateDisplay();
                    }
                    
                    deleteButton.setVisibility(android.view.View.VISIBLE);
                    if (!isFinishing() && getSupportActionBar() != null) {
                        getSupportActionBar().setTitle("Edit Expense");
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    if (!isFinishing()) {
                        Toast.makeText(AddEditExpenseActivity.this, 
                                "Error loading expense: " + e.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }
        });
    }

    private void saveExpense() {
        android.util.Log.d("AddEditExpense", "saveExpense() called");
        
        String description = descriptionEditText.getText().toString().trim();
        String amountStr = amountEditText.getText().toString().trim();
        String category = categorySpinner.getText().toString();
        
        android.util.Log.d("AddEditExpense", "Form data - Description: " + description + ", Amount: " + amountStr + ", Category: " + category);

        // Validation
        if (description.isEmpty()) {
            descriptionEditText.setError("Description is required");
            return;
        }

        if (amountStr.isEmpty()) {
            amountEditText.setError("Amount is required");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                amountEditText.setError("Amount must be greater than 0");
                return;
            }
        } catch (NumberFormatException e) {
            amountEditText.setError("Invalid amount");
            return;
        }

        if (category.isEmpty()) {
            categorySpinner.setError("Category is required");
            return;
        }

        // Create or update expense
        String accountId = sessionManager.getAccountId();
        
        if (accountId == null || accountId.isEmpty()) {
            Toast.makeText(this, "Error: User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Expense expense = new Expense();
        expense.setDescription(description);
        expense.setAmount(amount);
        expense.setCategory(category);
        expense.setDate(selectedDate.getTime());
        expense.setAccountId(accountId);

        if (isEditMode) {
            expense.setId(expenseId);
            updateExpense(expense);
        } else {
            addExpense(expense);
        }
    }

    private void addExpense(Expense expense) {
        saveButton.setEnabled(false);
        
        expenseRepository.addExpense(expense)
                .addOnSuccessListener(aVoid -> {
                    // Check budget threshold and send notification if needed
                    String accountId = sessionManager.getAccountId();
                    if (accountId != null) {
                        int month = selectedDate.get(Calendar.MONTH) + 1;
                        int year = selectedDate.get(Calendar.YEAR);
                        budgetNotificationService.checkBudgetThreshold(
                            accountId, 
                            expense.getCategory(), 
                            expense.getAmount(), 
                            month, 
                            year
                        );
                    }
                    
                    Toast.makeText(this, "Expense added successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error adding expense: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    saveButton.setEnabled(true);
                });
    }

    private void updateExpense(Expense expense) {
        saveButton.setEnabled(false);
        expenseRepository.updateExpense(expense)
                .addOnSuccessListener(aVoid -> {
                    // Check budget threshold and send notification if needed
                    String accountId = sessionManager.getAccountId();
                    if (accountId != null) {
                        int month = selectedDate.get(Calendar.MONTH) + 1;
                        int year = selectedDate.get(Calendar.YEAR);
                        budgetNotificationService.checkBudgetThreshold(
                            accountId, 
                            expense.getCategory(), 
                            expense.getAmount(), 
                            month, 
                            year
                        );
                    }
                    
                    Toast.makeText(this, "Expense updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error updating expense: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    saveButton.setEnabled(true);
                });
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Expense")
                .setMessage("Are you sure you want to delete this expense?")
                .setPositiveButton("Delete", (dialog, which) -> deleteExpense())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteExpense() {
        deleteButton.setEnabled(false);
        expenseRepository.deleteExpense(expenseId)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Expense deleted successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error deleting expense: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    deleteButton.setEnabled(true);
                });
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