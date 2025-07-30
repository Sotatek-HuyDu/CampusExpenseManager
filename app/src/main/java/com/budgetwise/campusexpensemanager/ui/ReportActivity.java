package com.budgetwise.campusexpensemanager.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.LinearLayout;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.budgetwise.campusexpensemanager.R;
import com.budgetwise.campusexpensemanager.adapters.CalendarAdapter;
import com.budgetwise.campusexpensemanager.firebase.FirebaseManager;
import com.budgetwise.campusexpensemanager.firebase.ExpenseRepository;
import com.budgetwise.campusexpensemanager.firebase.BudgetRepository;
import com.budgetwise.campusexpensemanager.models.CalendarDay;
import com.budgetwise.campusexpensemanager.models.Expense;
import com.budgetwise.campusexpensemanager.models.Budget;
import com.budgetwise.campusexpensemanager.adapters.ExpenseReportAdapter;
import com.budgetwise.campusexpensemanager.utils.CalendarHelper;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ArrayList;

public class ReportActivity extends BaseActivity {

    private CardView calendarCard;
    private ImageButton btnPrevMonth;
    private ImageButton btnNextMonth;
    private ImageButton btnPrevYear;
    private ImageButton btnNextYear;
    private TextView tvMonthYear;
    private RecyclerView calendarRecyclerView;
    private CalendarAdapter calendarAdapter;
    
    private Calendar currentDate;
    private Map<String, Boolean> expenseDays = new HashMap<>();
    private FirebaseManager firebaseManager;
    private CalendarDay selectedDay = null;
    private TextView tvSelectedDate;
    
    // Expense listing components
    private LinearLayout expensesContainer;
    private RecyclerView expensesRecyclerView;
    private TextView tvNoExpenses;
    private ExpenseReportAdapter expenseReportAdapter;
    private List<Expense> allExpenses = new ArrayList<>();
    private List<Budget> allBudgets = new ArrayList<>();

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_report;
    }

    @Override
    protected void setupActivity() {
        setupToolbar("Reports");
        setupDrawer();
        
        currentDate = Calendar.getInstance();
        initializeViews();
        setupCalendarCard();
        setupCalendarRecyclerView();
        setupExpenseRecyclerView();
        updateMonthYearDisplay();
        loadAllData();
    }

    private void initializeViews() {
        calendarCard = findViewById(R.id.calendar_card);
        btnPrevMonth = findViewById(R.id.btn_prev_month);
        btnNextMonth = findViewById(R.id.btn_next_month);
        btnPrevYear = findViewById(R.id.btn_prev_year);
        btnNextYear = findViewById(R.id.btn_next_year);
        tvMonthYear = findViewById(R.id.tv_month_year);
        calendarRecyclerView = findViewById(R.id.calendar_recycler_view);
        tvSelectedDate = findViewById(R.id.tv_selected_date);
        
        // Initialize expense listing components
        expensesContainer = findViewById(R.id.expenses_container);
        expensesRecyclerView = findViewById(R.id.expenses_recycler_view);
        tvNoExpenses = findViewById(R.id.tv_no_expenses);
        
        firebaseManager = FirebaseManager.getInstance();
    }

    private void setupCalendarCard() {
        // Month navigation
        btnPrevMonth.setOnClickListener(v -> {
            currentDate.add(Calendar.MONTH, -1);
            selectedDay = null; // Clear selection when changing month
            updateMonthYearDisplay();
            updateSelectedDateDisplay();
            hideExpensesContainer(); // Hide expenses when changing month
            loadExpensesForMonth(); // Reload expenses for new month
        });

        btnNextMonth.setOnClickListener(v -> {
            currentDate.add(Calendar.MONTH, 1);
            selectedDay = null; // Clear selection when changing month
            updateMonthYearDisplay();
            updateSelectedDateDisplay();
            hideExpensesContainer(); // Hide expenses when changing month
            loadExpensesForMonth(); // Reload expenses for new month
        });

        // Year navigation
        btnPrevYear.setOnClickListener(v -> {
            currentDate.add(Calendar.YEAR, -1);
            selectedDay = null; // Clear selection when changing year
            updateMonthYearDisplay();
            updateSelectedDateDisplay();
            hideExpensesContainer(); // Hide expenses when changing year
            loadExpensesForMonth(); // Reload expenses for new year
        });

        btnNextYear.setOnClickListener(v -> {
            currentDate.add(Calendar.YEAR, 1);
            selectedDay = null; // Clear selection when changing year
            updateMonthYearDisplay();
            updateSelectedDateDisplay();
            hideExpensesContainer(); // Hide expenses when changing year
            loadExpensesForMonth(); // Reload expenses for new year
        });
    }

    private void setupCalendarRecyclerView() {
        calendarAdapter = new CalendarAdapter();
        calendarRecyclerView.setLayoutManager(new GridLayoutManager(this, 7)); // 7 days per week
        calendarRecyclerView.setAdapter(calendarAdapter);
        
        // Set click listener for calendar days
        calendarAdapter.setOnDayClickListener(day -> {
            if (day.isCurrentMonth()) {
                selectedDay = day;
                updateSelectedDateDisplay();
                updateCalendarData(); // Refresh calendar to show selection
                loadExpensesForSelectedDate(); // Load expenses for selected date
            }
        });
        
        updateCalendarData();
        loadExpensesForMonth();
    }

    private void loadExpensesForMonth() {
        String accountId = sessionManager.getAccountId();
        if (accountId == null) return;

        int year = currentDate.get(Calendar.YEAR);
        int month = currentDate.get(Calendar.MONTH) + 1; // Calendar.MONTH is 0-based

        firebaseManager.getExpenseRepository().getExpensesByUser(accountId, new ExpenseRepository.ExpenseCallback() {
            @Override
            public void onSuccess(List<Expense> expenses) {
                expenseDays.clear();
                
                for (Expense expense : expenses) {
                    Calendar expenseDate = Calendar.getInstance();
                    expenseDate.setTime(expense.getDate());
                    
                    // Convert Firebase year offset to full year (e.g., 125 -> 2025)
                    int expenseYear = expenseDate.get(Calendar.YEAR);
                    if (expenseYear < 1000) {
                        // If year is less than 1000, it's likely an offset from 1900
                        expenseYear = 1900 + expenseYear;
                    }
                    
                    // Check if expense is in the current month/year
                    if (expenseYear == year && 
                        expenseDate.get(Calendar.MONTH) == currentDate.get(Calendar.MONTH)) {
                        
                        String dayKey = String.valueOf(expenseDate.get(Calendar.DAY_OF_MONTH));
                        expenseDays.put(dayKey, true);
                    }
                }
                
                updateCalendarData();
            }

            @Override
            public void onError(Exception e) {
                android.util.Log.e("ReportActivity", "Error loading expenses for month: " + e.getMessage());
                // Continue with empty expense days
                expenseDays.clear();
                updateCalendarData();
            }
        });
    }

    private void updateMonthYearDisplay() {
        String monthYear = currentDate.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) 
                + " " + currentDate.get(Calendar.YEAR);
        tvMonthYear.setText(monthYear);
    }

    private void updateSelectedDateDisplay() {
        if (selectedDay != null) {
            String selectedDateText = "Selected Date: " + selectedDay.getDay() + "/" + 
                    (selectedDay.getMonth() + 1) + "/" + selectedDay.getYear();
            tvSelectedDate.setText(selectedDateText);
        } else {
            tvSelectedDate.setText("No date selected");
        }
    }

    private void updateCalendarData() {
        int year = currentDate.get(Calendar.YEAR);
        int month = currentDate.get(Calendar.MONTH);
        
        List<CalendarDay> days = CalendarHelper.generateCalendarDays(year, month);
        
        // Mark days with expenses and selection
        for (CalendarDay day : days) {
            if (day.isCurrentMonth()) {
                String dayKey = String.valueOf(day.getDay());
                day.setHasExpenses(expenseDays.containsKey(dayKey));
                
                // Mark as selected if it matches the selected day
                if (selectedDay != null && 
                    day.getDay() == selectedDay.getDay() && 
                    day.getMonth() == selectedDay.getMonth() && 
                    day.getYear() == selectedDay.getYear()) {
                    day.setSelected(true);
                }
            }
        }
        
        calendarAdapter.setCalendarDays(days);
    }

    private void setupExpenseRecyclerView() {
        expenseReportAdapter = new ExpenseReportAdapter();
        expensesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        expensesRecyclerView.setAdapter(expenseReportAdapter);
    }

    private void loadAllData() {
        loadAllExpenses();
        loadAllBudgets();
    }

    private void loadAllExpenses() {
        String accountId = sessionManager.getAccountId();
        if (accountId == null) return;

        firebaseManager.getExpenseRepository().getExpensesByUser(accountId, new ExpenseRepository.ExpenseCallback() {
            @Override
            public void onSuccess(List<Expense> expenses) {
                allExpenses = expenses;
                loadExpensesForMonth(); // Refresh calendar highlights
                
                // Set today as selected after expenses are loaded
                setTodayAsSelected();
            }

            @Override
            public void onError(Exception e) {
                android.util.Log.e("ReportActivity", "Error loading all expenses: " + e.getMessage());
                // Initialize with empty list to prevent crashes
                allExpenses = new ArrayList<>();
                loadExpensesForMonth(); // Refresh calendar highlights
                
                // Set today as selected even if there's an error
                setTodayAsSelected();
            }
        });
    }

    private void loadAllBudgets() {
        String accountId = sessionManager.getAccountId();
        if (accountId == null) return;

        firebaseManager.getBudgetRepository().getBudgetsByUser(accountId, new BudgetRepository.BudgetCallback() {
            @Override
            public void onSuccess(List<Budget> budgets) {
                allBudgets = budgets;
                expenseReportAdapter.setBudgets(budgets);
            }

            @Override
            public void onError(Exception e) {
                android.util.Log.e("ReportActivity", "Error loading budgets: " + e.getMessage());
                // Initialize with empty list to prevent crashes
                allBudgets = new ArrayList<>();
                expenseReportAdapter.setBudgets(allBudgets);
            }
        });
    }

    private void loadExpensesForSelectedDate() {
        if (selectedDay == null) {
            hideExpensesContainer();
            return;
        }

        List<Expense> expensesForDate = new ArrayList<>();
        
        for (Expense expense : allExpenses) {
            Calendar expenseDate = Calendar.getInstance();
            expenseDate.setTime(expense.getDate());
            
            // Convert Firebase year offset to full year
            int expenseYear = expenseDate.get(Calendar.YEAR);
            if (expenseYear < 1000) {
                expenseYear = 1900 + expenseYear;
            }
            
            // Check if expense is on the selected date
            if (expenseYear == selectedDay.getYear() &&
                expenseDate.get(Calendar.MONTH) == selectedDay.getMonth() &&
                expenseDate.get(Calendar.DAY_OF_MONTH) == selectedDay.getDay()) {
                expensesForDate.add(expense);
            }
        }
        
        displayExpenses(expensesForDate);
    }

    private void displayExpenses(List<Expense> expenses) {
        if (expenses.isEmpty()) {
            showNoExpensesMessage();
        } else {
            showExpensesList(expenses);
        }
    }

    private void showExpensesList(List<Expense> expenses) {
        expensesContainer.setVisibility(View.VISIBLE);
        tvNoExpenses.setVisibility(View.GONE);
        expenseReportAdapter.setExpenses(expenses);
    }

    private void showNoExpensesMessage() {
        expensesContainer.setVisibility(View.VISIBLE);
        tvNoExpenses.setVisibility(View.VISIBLE);
        expenseReportAdapter.setExpenses(new ArrayList<>());
    }

    private void hideExpensesContainer() {
        expensesContainer.setVisibility(View.GONE);
    }
    
    private void setTodayAsSelected() {
        Calendar today = Calendar.getInstance();
        
        // Create a CalendarDay object for today
        selectedDay = new CalendarDay(
            today.get(Calendar.DAY_OF_MONTH),
            today.get(Calendar.MONTH),
            today.get(Calendar.YEAR),
            true, // isCurrentMonth
            false, // hasExpenses (will be updated when data loads)
            true   // isSelected
        );
        
        // Update the display
        updateSelectedDateDisplay();
        updateCalendarData();
        
        // Load expenses for today
        loadExpensesForSelectedDate();
    }
} 