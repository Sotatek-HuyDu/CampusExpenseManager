package com.budgetwise.campusexpensemanager.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.budgetwise.campusexpensemanager.R;
import com.budgetwise.campusexpensemanager.firebase.models.FirebaseRecurringExpense;
import com.budgetwise.campusexpensemanager.firebase.repository.RecurringExpenseRepository;
import com.budgetwise.campusexpensemanager.models.ExpenseCategory;
import com.budgetwise.campusexpensemanager.utils.SessionManager;
import com.budgetwise.campusexpensemanager.utils.RecurringExpenseUtil;
import com.budgetwise.campusexpensemanager.ui.CategorySpinnerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddEditRecurringExpenseActivity extends AppCompatActivity {

    private EditText descriptionEditText;
    private EditText amountEditText;
    private Spinner categorySpinner;
    private TextView startDateTextView;
    private TextView endDateTextView;
    private Spinner intervalSpinner;
    private Button saveButton;
    private Button deleteButton;

    private RecurringExpenseRepository recurringExpenseRepository;
    private SessionManager sessionManager;
    private String recurringExpenseId;
    private boolean isEditMode = false;

    private Calendar startDate = Calendar.getInstance();
    private Calendar endDate = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_recurring_expense);

        // Initialize repositories
        recurringExpenseRepository = new RecurringExpenseRepository();
        sessionManager = new SessionManager(this);

        // Check if editing existing recurring expense
        recurringExpenseId = getIntent().getStringExtra("recurring_expense_id");
        isEditMode = recurringExpenseId != null;

        // Initialize views
        initializeViews();
        setupToolbar();
        setupSpinners();
        setupDatePickers();
        setupButtons();

        if (isEditMode) {
            loadRecurringExpense();
        }
    }

    private void initializeViews() {
        descriptionEditText = findViewById(R.id.description_edit_text);
        amountEditText = findViewById(R.id.amount_edit_text);
        categorySpinner = findViewById(R.id.category_spinner);
        startDateTextView = findViewById(R.id.start_date_text_view);
        endDateTextView = findViewById(R.id.end_date_text_view);
        intervalSpinner = findViewById(R.id.interval_spinner);
        saveButton = findViewById(R.id.save_button);
        deleteButton = findViewById(R.id.delete_button);

        // Set initial dates
        startDateTextView.setText(dateFormat.format(startDate.getTime()));
        endDate.add(Calendar.YEAR, 1); // Default to 1 year from now
        endDateTextView.setText(dateFormat.format(endDate.getTime()));

        // Show/hide delete button based on mode
        deleteButton.setVisibility(isEditMode ? View.VISIBLE : View.GONE);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(isEditMode ? "Edit Recurring Expense" : "Add Recurring Expense");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupSpinners() {
        // Setup category spinner with icons and colors
        String[] categories = new String[ExpenseCategory.values().length];
        for (int i = 0; i < ExpenseCategory.values().length; i++) {
            categories[i] = ExpenseCategory.values()[i].getDisplayName();
        }
        CategorySpinnerAdapter categoryAdapter = new CategorySpinnerAdapter(this, java.util.Arrays.asList(categories));
        categorySpinner.setAdapter(categoryAdapter);

        // Setup interval spinner
        String[] intervals = {"Weekly", "Bi-weekly", "Monthly", "Yearly"};
        ArrayAdapter<String> intervalAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, intervals);
        intervalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        intervalSpinner.setAdapter(intervalAdapter);
        intervalSpinner.setSelection(2); // Default to Monthly
    }

    private void setupDatePickers() {
        startDateTextView.setOnClickListener(v -> showDatePicker(startDate, startDateTextView));
        endDateTextView.setOnClickListener(v -> showDatePicker(endDate, endDateTextView));
    }

    private void showDatePicker(Calendar calendar, TextView textView) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                calendar.set(year, month, dayOfMonth);
                textView.setText(dateFormat.format(calendar.getTime()));
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void setupButtons() {
        saveButton.setOnClickListener(v -> saveRecurringExpense());
        deleteButton.setOnClickListener(v -> deleteRecurringExpense());
    }

    private void saveRecurringExpense() {
        String description = descriptionEditText.getText().toString().trim();
        String amountStr = amountEditText.getText().toString().trim();
        String category = categorySpinner.getSelectedItem().toString();
        String intervalStr = intervalSpinner.getSelectedItem().toString();

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

        // Convert interval to days
        int intervalDays = RecurringExpenseUtil.getIntervalDays(intervalStr);

        // Create recurring expense object
        FirebaseRecurringExpense recurringExpense = new FirebaseRecurringExpense(
            sessionManager.getAccountId(),
            description,
            amount,
            category,
            startDate.getTime(),
            endDate.getTime(),
            intervalDays
        );

        if (isEditMode) {
            recurringExpense.setId(recurringExpenseId);
            updateRecurringExpense(recurringExpense);
        } else {
            addRecurringExpense(recurringExpense);
        }
    }



    private void addRecurringExpense(FirebaseRecurringExpense recurringExpense) {
        recurringExpenseRepository.insert(recurringExpense)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Recurring expense added successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Failed to add recurring expense", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void updateRecurringExpense(FirebaseRecurringExpense recurringExpense) {
        recurringExpenseRepository.update(recurringExpense)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Recurring expense updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Failed to update recurring expense", Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void deleteRecurringExpense() {
        if (recurringExpenseId != null) {
            FirebaseRecurringExpense recurringExpense = new FirebaseRecurringExpense();
            recurringExpense.setId(recurringExpenseId);
            
            recurringExpenseRepository.delete(recurringExpense)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Recurring expense deleted successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to delete recurring expense", Toast.LENGTH_SHORT).show();
                    }
                });
        }
    }

    private void loadRecurringExpense() {
        if (recurringExpenseId == null) return;
        
        recurringExpenseRepository.getRecurringExpenseById(recurringExpenseId)
            .addValueEventListener(new com.google.firebase.database.ValueEventListener() {
                @Override
                public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                    FirebaseRecurringExpense recurringExpense = dataSnapshot.getValue(FirebaseRecurringExpense.class);
                    if (recurringExpense != null) {
                        populateForm(recurringExpense);
                    }
                }
                
                @Override
                public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
                    Toast.makeText(AddEditRecurringExpenseActivity.this, 
                        "Failed to load recurring expense", Toast.LENGTH_SHORT).show();
                }
            });
    }
    
    private void populateForm(FirebaseRecurringExpense recurringExpense) {
        // Populate description
        descriptionEditText.setText(recurringExpense.getDescription());
        
        // Populate amount
        amountEditText.setText(String.valueOf(recurringExpense.getAmount()));
        
        // Populate category
        String category = recurringExpense.getCategory();
        for (int i = 0; i < categorySpinner.getCount(); i++) {
            if (categorySpinner.getItemAtPosition(i).toString().equals(category)) {
                categorySpinner.setSelection(i);
                break;
            }
        }
        
        // Populate dates
        if (recurringExpense.getStartDate() != null) {
            startDate.setTime(recurringExpense.getStartDate());
            startDateTextView.setText(dateFormat.format(startDate.getTime()));
        }
        
        if (recurringExpense.getEndDate() != null) {
            endDate.setTime(recurringExpense.getEndDate());
            endDateTextView.setText(dateFormat.format(endDate.getTime()));
        }
        
        // Populate interval
        int intervalDays = recurringExpense.getRecurrenceIntervalDays();
        String intervalDescription = RecurringExpenseUtil.getIntervalDescription(intervalDays);
        for (int i = 0; i < intervalSpinner.getCount(); i++) {
            if (intervalSpinner.getItemAtPosition(i).toString().equals(intervalDescription)) {
                intervalSpinner.setSelection(i);
                break;
            }
        }
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