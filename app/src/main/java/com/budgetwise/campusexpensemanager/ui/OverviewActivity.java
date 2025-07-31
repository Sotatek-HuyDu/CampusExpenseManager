package com.budgetwise.campusexpensemanager.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import android.view.View;
import android.view.MotionEvent;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.budgetwise.campusexpensemanager.R;
import com.budgetwise.campusexpensemanager.firebase.repository.ExpenseAnalyticsRepository;
import com.budgetwise.campusexpensemanager.firebase.BudgetRepository;
import com.budgetwise.campusexpensemanager.firebase.FirebaseManager;
import com.budgetwise.campusexpensemanager.models.CategoryAnalysis;
import com.budgetwise.campusexpensemanager.models.MonthlySummary;
import com.budgetwise.campusexpensemanager.models.MonthlyTrend;
import com.budgetwise.campusexpensemanager.models.Budget;
import com.budgetwise.campusexpensemanager.utils.SessionManager;
import com.budgetwise.campusexpensemanager.utils.CategoryColorUtil;
import com.budgetwise.campusexpensemanager.firebase.models.FirebaseExpense;
import com.budgetwise.campusexpensemanager.firebase.models.FirebaseRecurringExpense;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.highlight.Highlight;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import android.widget.LinearLayout;

public class OverviewActivity extends BaseActivity {

    private TextView totalSpentText;
    private TextView totalBudgetText;
    private TextView remainingBudgetText;
    private TextView budgetUtilizationText;
    private TextView budgetedSpendingText;
    private TextView unbudgetedSpendingText;
    private Spinner monthSpinner;
    private Spinner yearSpinner;
    private LinearProgressIndicator budgetProgressBar;
    private CardView spendingSummaryCard;
    private CardView budgetPerformanceCard;
    private CardView trendsCard;
    private CardView categoryCard;
    private CardView pieChartCard;
    private CardView budgetWarningCard;
    private RecyclerView categoryRecyclerView;
    private TextView emptyStateText;
    private PieChart pieChart;
    private TextView pieChartEmptyText;
    private LinearLayout customLegendContainer;
    private LinearLayout budgetWarningsContainer;
    private LinearLayout budgetWarningHeader;
    private LinearLayout budgetWarningIndicators;
    private ImageView budgetWarningExpandIcon;
    private CardView tooltipOverlay;
    private TextView tooltipCategoryName;
    private TextView tooltipAmount;

    private ExpenseAnalyticsRepository analyticsRepository;
    private BudgetRepository budgetRepository;
    private FirebaseManager firebaseManager;
    private SessionManager sessionManager;
    private CategoryAnalysisAdapter categoryAdapter;
    private Calendar currentDate;
    private SimpleDateFormat monthFormat;
         private List<CategoryAnalysis> currentCategoryAnalysis;
     private List<Budget> currentBudgets;
     private boolean isBudgetWarningCollapsed = true;

    // Custom legend formatter to show percentages
    private class CustomLegendFormatter extends ValueFormatter {
        @Override
        public String getFormattedValue(float value) {
            if (currentCategoryAnalysis != null) {
                double total = 0;
                for (CategoryAnalysis cat : currentCategoryAnalysis) {
                    total += cat.getTotalAmount();
                }
                if (total > 0) {
                    double percentage = (value / total) * 100;
                    return String.format("%.1f%%", percentage);
                }
            }
            return String.format("%.1f%%", value);
        }
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_overview;
    }

    @Override
    protected void setupActivity() {
        try {
            // Initialize repositories and managers
            analyticsRepository = new ExpenseAnalyticsRepository();
            firebaseManager = FirebaseManager.getInstance();
            budgetRepository = firebaseManager.getBudgetRepository();
            sessionManager = new SessionManager(this);
            currentDate = Calendar.getInstance();
            monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());

            // Initialize views
            initializeViews();
            setupToolbar("Expense Overview");
            setupDrawer();
            setupRecyclerView();
            setupPieChart();
            setupMonthYearSpinners();
            setupClickListeners();
            setupGlobalTouchListener();

            // Check if user is logged in before loading data
            if (!sessionManager.isLoggedIn()) {
                showErrorState();
                Toast.makeText(this, "Please log in to view overview", Toast.LENGTH_SHORT).show();
            } else {
                // Load current month's data
                loadMonthlySummary();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error setting up overview: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeViews() {
        totalSpentText = findViewById(R.id.total_spent_text);
        totalBudgetText = findViewById(R.id.total_budget_text);
        remainingBudgetText = findViewById(R.id.remaining_budget_text);
        budgetUtilizationText = findViewById(R.id.budget_utilization_text);
        budgetedSpendingText = findViewById(R.id.budgeted_spending_text);
        unbudgetedSpendingText = findViewById(R.id.unbudgeted_spending_text);
        monthSpinner = findViewById(R.id.month_spinner);
        yearSpinner = findViewById(R.id.year_spinner);
        budgetProgressBar = findViewById(R.id.budget_progress_bar);
        spendingSummaryCard = findViewById(R.id.spending_summary_card);
        budgetPerformanceCard = findViewById(R.id.budget_performance_card);
        trendsCard = findViewById(R.id.trends_card);
        categoryCard = findViewById(R.id.category_card);
        pieChartCard = findViewById(R.id.pie_chart_card);
        budgetWarningCard = findViewById(R.id.budget_warning_card);
        categoryRecyclerView = findViewById(R.id.category_recycler_view);
        emptyStateText = findViewById(R.id.empty_state_text);
        pieChart = findViewById(R.id.pie_chart);
        pieChartEmptyText = findViewById(R.id.pie_chart_empty_text);
        customLegendContainer = findViewById(R.id.custom_legend_container);
        budgetWarningsContainer = findViewById(R.id.budget_warnings_container);
        budgetWarningHeader = findViewById(R.id.budget_warning_header);
        budgetWarningIndicators = findViewById(R.id.budget_warning_indicators);
        budgetWarningExpandIcon = findViewById(R.id.budget_warning_expand_icon);
        tooltipOverlay = findViewById(R.id.tooltip_overlay);
        tooltipCategoryName = findViewById(R.id.tooltip_category_name);
        tooltipAmount = findViewById(R.id.tooltip_amount);
    }

    private void setupRecyclerView() {
        categoryAdapter = new CategoryAnalysisAdapter(this);
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        categoryRecyclerView.setHasFixedSize(true);
        categoryRecyclerView.setAdapter(categoryAdapter);
    }

    private void setupPieChart() {
        try {
            // Configure pie chart appearance
            pieChart.setUsePercentValues(true);
            pieChart.getDescription().setEnabled(false);
            pieChart.setExtraOffsets(5, 10, 5, 5);
            pieChart.setDragDecelerationFrictionCoef(0.95f);
            pieChart.setDrawHoleEnabled(true);
            pieChart.setHoleColor(Color.WHITE);
            pieChart.setTransparentCircleColor(Color.WHITE);
            pieChart.setTransparentCircleAlpha(110);
            pieChart.setHoleRadius(30f); // Smaller hole for better fill
            pieChart.setTransparentCircleRadius(33f); // Slightly larger transparent circle
            pieChart.setDrawCenterText(false); // Remove center text
            pieChart.setRotationAngle(0);
            pieChart.setRotationEnabled(true);
            pieChart.setHighlightPerTapEnabled(true);
            pieChart.setEntryLabelColor(Color.BLACK);
            pieChart.setEntryLabelTextSize(12f);
            pieChart.setDrawEntryLabels(false); // Remove labels on slices
            
            // Completely disable the built-in legend
            pieChart.getLegend().setEnabled(false);
            
            // Add click listener for tooltips
            pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                @Override
                public void onValueSelected(com.github.mikephil.charting.data.Entry e, Highlight h) {
                    if (e instanceof PieEntry && currentCategoryAnalysis != null) {
                        PieEntry pieEntry = (PieEntry) e;
                        String categoryName = pieEntry.getLabel().split(" \\(")[0]; // Extract category name before percentage
                        
                        // Find the category analysis for this entry
                        for (CategoryAnalysis cat : currentCategoryAnalysis) {
                            if (cat.getCategory().equals(categoryName)) {
                                showTooltip(categoryName, cat.getTotalAmount(), h.getX(), h.getY());
                                break;
                            }
                        }
                    }
                }

                @Override
                public void onNothingSelected() {
                    hideTooltip();
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Error setting up pie chart: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupMonthYearSpinners() {
        try {
            // Setup month spinner
            String[] months = {"January", "February", "March", "April", "May", "June",
                              "July", "August", "September", "October", "November", "December"};
            ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_spinner_item, months);
            monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            monthSpinner.setAdapter(monthAdapter);

            // Setup year spinner (current year - 5 to current year + 1)
            Calendar cal = Calendar.getInstance();
            int currentYear = cal.get(Calendar.YEAR);
            String[] years = new String[7];
            for (int i = 0; i < 7; i++) {
                years[i] = String.valueOf(currentYear - 5 + i);
            }
            ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_spinner_item, years);
            yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            yearSpinner.setAdapter(yearAdapter);

            // Set current month and year
            int currentMonth = cal.get(Calendar.MONTH);
            monthSpinner.setSelection(currentMonth);
            yearSpinner.setSelection(5); // Current year is at index 5 (currentYear - 5 + 5)

            // Set listeners for month/year changes
            monthSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                    try {
                        currentDate.set(Calendar.MONTH, position);
                        loadMonthlySummary();
                    } catch (Exception e) {
                        Toast.makeText(OverviewActivity.this, "Error changing month: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onNothingSelected(android.widget.AdapterView<?> parent) {
                    // Do nothing
                }
            });

            yearSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                    try {
                        int selectedYear = Integer.parseInt(years[position]);
                        currentDate.set(Calendar.YEAR, selectedYear);
                        loadMonthlySummary();
                    } catch (Exception e) {
                        Toast.makeText(OverviewActivity.this, "Error changing year: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onNothingSelected(android.widget.AdapterView<?> parent) {
                    // Do nothing
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Error setting up spinners: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupClickListeners() {
        try {
            // Quick navigation buttons
            findViewById(R.id.previous_month_button).setOnClickListener(v -> {
                try {
                    currentDate.add(Calendar.MONTH, -1);
                    updateSpinnerSelections();
                    loadMonthlySummary();
                } catch (Exception e) {
                    Toast.makeText(this, "Error navigating: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            findViewById(R.id.current_month_button).setOnClickListener(v -> {
                try {
                    currentDate = Calendar.getInstance();
                    updateSpinnerSelections();
                    loadMonthlySummary();
                } catch (Exception e) {
                    Toast.makeText(this, "Error navigating: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            findViewById(R.id.next_month_button).setOnClickListener(v -> {
                try {
                    currentDate.add(Calendar.MONTH, 1);
                    updateSpinnerSelections();
                    loadMonthlySummary();
                } catch (Exception e) {
                    Toast.makeText(this, "Error navigating: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            // Card click listeners
            trendsCard.setOnClickListener(v -> {
                Intent intent = new Intent(this, DailyTrendsActivity.class);
                intent.putExtra("month", currentDate.get(Calendar.MONTH) + 1);
                intent.putExtra("year", currentDate.get(Calendar.YEAR));
                startActivity(intent);
            });

            categoryCard.setOnClickListener(v -> {
                Intent intent = new Intent(this, CategoryBreakdownActivity.class);
                intent.putExtra("month", currentDate.get(Calendar.MONTH) + 1);
                intent.putExtra("year", currentDate.get(Calendar.YEAR));
                startActivity(intent);
            });
        } catch (Exception e) {
            Toast.makeText(this, "Error setting up click listeners: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupGlobalTouchListener() {
        // Get the root layout of the activity
        View rootView = findViewById(android.R.id.content);
        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    hideTooltip();
                }
                return false; // Let the main layout handle the touch
            }
        });
    }

    private void updateSpinnerSelections() {
        try {
            int month = currentDate.get(Calendar.MONTH);
            int year = currentDate.get(Calendar.YEAR);
            
            monthSpinner.setSelection(month);
            
            // Calculate year position (current year - 5 to current year + 1)
            Calendar cal = Calendar.getInstance();
            int currentYear = cal.get(Calendar.YEAR);
            int yearPosition = year - (currentYear - 5);
            if (yearPosition >= 0 && yearPosition < 7) {
                yearSpinner.setSelection(yearPosition);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error updating spinners: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadMonthlySummary() {
        try {
            int month = currentDate.get(Calendar.MONTH) + 1;
            int year = currentDate.get(Calendar.YEAR);
            String accountId = sessionManager.getAccountId();

            if (accountId == null) {
                showErrorState();
                Toast.makeText(this, "Please log in to view overview", Toast.LENGTH_SHORT).show();
                return;
            }

            // Show loading state
            showLoadingState();

            analyticsRepository.getMonthlySummary(accountId, month, year, 
                new ExpenseAnalyticsRepository.MonthlySummaryCallback() {
                    @Override
                    public void onSuccess(MonthlySummary summary) {
                        runOnUiThread(() -> {
                            if (!isFinishing()) {
                                updateSummaryUI(summary);
                                loadCategoryAnalysis(accountId, month, year);
                                loadBudgetData(accountId, month, year);
                            }
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        runOnUiThread(() -> {
                            if (!isFinishing()) {
                                showErrorState();
                                Toast.makeText(OverviewActivity.this, 
                                    "Failed to load overview: " + e.getMessage(), 
                                    Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
        } catch (Exception e) {
            showErrorState();
            Toast.makeText(this, "Error loading overview: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadCategoryAnalysis(String accountId, int month, int year) {
        try {
            analyticsRepository.getCategoryAnalysis(accountId, month, year,
                new ExpenseAnalyticsRepository.CategoryAnalysisCallback() {
                    @Override
                    public void onSuccess(List<CategoryAnalysis> analysis) {
                        runOnUiThread(() -> {
                            if (!isFinishing()) {
                                currentCategoryAnalysis = analysis; // Store for legend formatter
                                updateCategoryAnalysis(analysis);
                            }
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        runOnUiThread(() -> {
                            if (!isFinishing()) {
                                // Don't show error for category analysis, just leave empty
                                updateCategoryAnalysis(null);
                            }
                        });
                    }
                });
        } catch (Exception e) {
            // Don't show error for category analysis
            updateCategoryAnalysis(null);
        }
    }

    private void loadBudgetData(String accountId, int month, int year) {
        try {
            budgetRepository.getBudgetsByUser(accountId, new BudgetRepository.BudgetCallback() {
                @Override
                public void onSuccess(List<Budget> budgets) {
                    runOnUiThread(() -> {
                        if (!isFinishing()) {
                            // Filter budgets for the current month/year
                            List<Budget> filteredBudgets = new ArrayList<>();
                            for (Budget budget : budgets) {
                                if (budget.month == month && budget.year == year) {
                                    filteredBudgets.add(budget);
                                }
                            }
                            currentBudgets = filteredBudgets;
                            // Budget warnings will be calculated asynchronously in checkBudgetWarnings
                            checkBudgetWarnings(month, year);
                        }
                    });
                }

                @Override
                public void onError(Exception e) {
                    runOnUiThread(() -> {
                        if (!isFinishing()) {
                            // Don't show error for budget loading, just leave empty
                            currentBudgets = new ArrayList<>();
                            // Budget warnings will be calculated asynchronously in checkBudgetWarnings
                            checkBudgetWarnings(month, year);
                        }
                    });
                }
            });
        } catch (Exception e) {
            // Don't show error for budget loading
            currentBudgets = new ArrayList<>();
            // Budget warnings will be calculated asynchronously in checkBudgetWarnings
            checkBudgetWarnings(month, year);
        }
    }

    private void checkBudgetWarnings(int month, int year) {
        try {
            if (currentBudgets == null || currentBudgets.isEmpty()) {
                hideBudgetWarningCard();
                return;
            }

            String accountId = sessionManager.getAccountId();
            int totalBudgetsToCheck = currentBudgets.size();
            final int[] completedBudgets = {0};

            // Create a shared warnings list that will be updated by all budget calculations
            final List<String> allWarnings = Collections.synchronizedList(new ArrayList<>());

            // Check each budget against actual spending (including recurring expenses)
            for (Budget budget : currentBudgets) {
                // Calculate spent amount using the same logic as BudgetAdapter
                calculateSpentAmountForWarning(budget, accountId, allWarnings, totalBudgetsToCheck, completedBudgets);
            }
        } catch (Exception e) {
            hideBudgetWarningCard();
        }
    }

    private void calculateSpentAmountForWarning(Budget budget, String accountId, List<String> allWarnings, int totalBudgetsToCheck, int[] completedBudgets) {
        // Get expenses for the same user and category
        firebaseManager.getExpenseRepository().getExpensesByCategoryAndMonth(
            accountId, 
            budget.category, 
            budget.month, 
            budget.year
        ).addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                final double[] spentAmount = {0.0};
                
                for (com.google.firebase.database.DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    FirebaseExpense expense = 
                        snapshot.getValue(FirebaseExpense.class);
                    if (expense != null && 
                        expense.getCategory().equals(budget.category) &&
                        isExpenseInMonth(expense, budget.month, budget.year)) {
                        spentAmount[0] += expense.getAmount();
                    }
                }
                
                // Now add recurring expenses for this category and month
                addRecurringExpensesToWarningCalculation(budget, spentAmount, allWarnings, totalBudgetsToCheck, completedBudgets);
            }
            
            @Override
            public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
                // Error loading expenses
            }
        });
    }

    private void addRecurringExpensesToWarningCalculation(Budget budget, final double[] spentAmount, List<String> allWarnings, int totalBudgetsToCheck, int[] completedBudgets) {
        firebaseManager.getRecurringExpenseRepository().getRecurringExpensesByAccount(budget.accountId)
            .addValueEventListener(new com.google.firebase.database.ValueEventListener() {
                @Override
                public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                    final double[] recurringAmount = {0.0};
                    
                    for (com.google.firebase.database.DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        FirebaseRecurringExpense recurringExpense = 
                            snapshot.getValue(FirebaseRecurringExpense.class);
                        
                        if (recurringExpense != null && 
                            recurringExpense.getCategory().equals(budget.category)) {
                            // Check if this recurring expense should create an expense in the target month
                            if (shouldCreateExpenseInMonth(recurringExpense, budget.month, budget.year)) {
                                recurringAmount[0] += recurringExpense.getAmount();
                            }
                        }
                    }
                    
                    // Add recurring amount to total spent
                    spentAmount[0] += recurringAmount[0];
                    
                    // Calculate utilization percentage
                    if (budget.limit > 0) {
                        double utilization = (spentAmount[0] / budget.limit) * 100;

                                                 if (utilization > 100) {
                             // Overspent
                             double overAmount = spentAmount[0] - budget.limit;
                             String warning = "You have overspent the " + budget.category + "'s budget by **$" + String.format("%.2f", overAmount) + "**";
                             allWarnings.add(warning);
                             
                         } else if (Math.abs(utilization - 100) < 0.1) {
                             // Exactly at budget (with tolerance for floating point precision)
                             String warning = "You are out of " + budget.category + "'s budget for this month!";
                             allWarnings.add(warning);
                             
                         } else if (utilization >= 80) {
                             // Near budget limit
                             double remainingAmount = budget.limit - spentAmount[0];
                             String warning = "Be careful, you have **$" + String.format("%.2f", remainingAmount) + "** left for " + budget.category + "'s budget!";
                             allWarnings.add(warning);
                             
                         }
                    }
                    
                    // Increment completed budgets counter
                    completedBudgets[0]++;
                    
                    // Update UI on main thread only when all budgets are processed
                     if (completedBudgets[0] >= totalBudgetsToCheck) {
                         runOnUiThread(() -> {
                             if (!allWarnings.isEmpty()) {
                                 showBudgetWarningCard(allWarnings);
                             } else {
                                 hideBudgetWarningCard();
                             }
                         });
                     }
                }
                
                @Override
                public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
                    // Error loading recurring expenses
                }
            });
    }

    private boolean isExpenseInMonth(FirebaseExpense expense, int month, int year) {
        if (expense.getDate() == null) return false;
        
        Calendar expenseCal = Calendar.getInstance();
        expenseCal.setTime(expense.getDate());
        
        return expenseCal.get(Calendar.MONTH) + 1 == month && expenseCal.get(Calendar.YEAR) == year;
    }

    private boolean shouldCreateExpenseInMonth(FirebaseRecurringExpense recurringExpense, int month, int year) {
        if (recurringExpense.getStartDate() == null || recurringExpense.getEndDate() == null) {
            return false;
        }
        
        Calendar startDate = Calendar.getInstance();
        startDate.setTime(recurringExpense.getStartDate());
        
        Calendar endDate = Calendar.getInstance();
        endDate.setTime(recurringExpense.getEndDate());
        
        // Create target month start and end dates
        Calendar targetMonthStart = Calendar.getInstance();
        targetMonthStart.set(year, month - 1, 1, 0, 0, 0); // First day of target month
        
        Calendar targetMonthEnd = Calendar.getInstance();
        targetMonthEnd.set(year, month - 1, targetMonthStart.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59); // Last day of target month
        
        // Check if the recurring expense period overlaps with the target month
        boolean shouldInclude = !startDate.after(targetMonthEnd) && !endDate.before(targetMonthStart);
        
        return shouldInclude;
    }

    private void showBudgetWarningCard(List<String> warnings) {
        try {
            // Clear existing indicators
            budgetWarningIndicators.removeAllViews();
            
            // Clear existing warning cards
            budgetWarningsContainer.removeAllViews();

            // Count warnings by type
            int infoCount = 0, warningCount = 0, dangerCount = 0;
            for (String warning : warnings) {
                if (warning.contains("overspent")) {
                    dangerCount++;
                } else if (warning.contains("out of")) {
                    warningCount++;
                } else {
                    infoCount++;
                }
            }

            // Create warning indicators in the header
            createWarningIndicators(infoCount, warningCount, dangerCount);

            // Create detailed warning cards
            for (String warning : warnings) {
                // Create individual warning card
                androidx.cardview.widget.CardView warningCard = new androidx.cardview.widget.CardView(this);
                LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                );
                cardParams.setMargins(0, 8, 0, 8);
                warningCard.setLayoutParams(cardParams);
                warningCard.setRadius(8);
                warningCard.setCardElevation(2);
                warningCard.setUseCompatPadding(true);
                warningCard.setContentPadding(16, 12, 16, 12);

                // Set card background to white with border
                warningCard.setCardBackgroundColor(Color.WHITE);
                
                // Determine warning type and set border color
                int borderColor;
                int textColor;
                int iconRes;
                
                if (warning.contains("overspent")) {
                    borderColor = getResources().getColor(android.R.color.holo_red_dark);
                    textColor = getResources().getColor(android.R.color.holo_red_dark);
                    iconRes = android.R.drawable.ic_dialog_alert; // Danger icon
                } else if (warning.contains("out of")) {
                    borderColor = getResources().getColor(android.R.color.holo_orange_dark);
                    textColor = getResources().getColor(android.R.color.holo_orange_dark);
                    iconRes = android.R.drawable.ic_dialog_alert; // Warning icon
                } else {
                    borderColor = getResources().getColor(android.R.color.holo_blue_dark);
                    textColor = getResources().getColor(android.R.color.holo_blue_dark);
                    iconRes = android.R.drawable.ic_dialog_info; // Info icon
                }
                
                // Create border drawable
                android.graphics.drawable.GradientDrawable border = new android.graphics.drawable.GradientDrawable();
                border.setColor(Color.WHITE);
                border.setStroke(2, borderColor);
                border.setCornerRadius(8);
                warningCard.setBackground(border);

                // Create horizontal layout for icon and text
                LinearLayout warningLayout = new LinearLayout(this);
                warningLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                warningLayout.setOrientation(LinearLayout.HORIZONTAL);
                warningLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);

                // Create icon
                ImageView warningIcon = new ImageView(this);
                LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(24, 24);
                iconParams.setMargins(0, 0, 12, 0);
                warningIcon.setLayoutParams(iconParams);
                warningIcon.setImageResource(iconRes);
                warningIcon.setColorFilter(textColor);

                                 // Create warning text with bold amounts
                 TextView warningText = new TextView(this);
                 LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                     0,
                     LinearLayout.LayoutParams.WRAP_CONTENT,
                     1.0f
                 );
                 warningText.setLayoutParams(textParams);
                 
                 // Parse bold formatting in warning text
                 String displayText = warning.replace("**", "");
                 android.text.SpannableString spannableString = new android.text.SpannableString(displayText);
                 
                 // Find bold sections (amounts) - simpler approach
                 int startIndex = warning.indexOf("**");
                 int removedChars = 0;
                 while (startIndex != -1) {
                     int endIndex = warning.indexOf("**", startIndex + 2);
                     if (endIndex != -1) {
                         // Calculate the adjusted indices for the display text
                         int displayStartIndex = startIndex - removedChars;
                         int displayEndIndex = endIndex - removedChars - 2; // -2 because we're removing the **
                         
                         if (displayStartIndex >= 0 && displayEndIndex <= displayText.length()) {
                             spannableString.setSpan(
                                 new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                                 displayStartIndex,
                                 displayEndIndex,
                                 android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                             );
                         }
                         
                         removedChars += 4; // We removed 2 ** markers (4 characters total)
                         startIndex = warning.indexOf("**", endIndex + 2);
                     } else {
                         break;
                     }
                 }
                 
                 warningText.setText(spannableString);
                 warningText.setTextSize(14);
                 warningText.setTextColor(textColor);
                 warningText.setGravity(android.view.Gravity.START | android.view.Gravity.CENTER_VERTICAL);

                // Add icon and text to layout
                warningLayout.addView(warningIcon);
                warningLayout.addView(warningText);

                // Add layout to card
                warningCard.addView(warningLayout);

                // Add card to container
                budgetWarningsContainer.addView(warningCard);
            }

            // Set up click listener for header
            setupBudgetWarningHeaderClickListener();

            // Set initial visibility based on collapsed state
            updateBudgetWarningVisibility();

            // Update expand icon to match initial state
            updateBudgetWarningExpandIcon();

            // Show the budget warning card
            budgetWarningCard.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            hideBudgetWarningCard();
        }
    }

                                       private void hideBudgetWarningCard() {
                     try {
                budgetWarningCard.setVisibility(View.GONE);
           } catch (Exception e) {
               // Ignore errors in hideBudgetWarningCard
           }
       }

    private void createWarningIndicators(int infoCount, int warningCount, int dangerCount) {
        try {
            // Add info indicator if count > 0
            if (infoCount > 0) {
                budgetWarningIndicators.addView(createWarningIndicator(infoCount, android.R.drawable.ic_dialog_info, 
                    getResources().getColor(android.R.color.holo_blue_dark)));
            }

            // Add warning indicator if count > 0
            if (warningCount > 0) {
                budgetWarningIndicators.addView(createWarningIndicator(warningCount, android.R.drawable.ic_dialog_alert, 
                    getResources().getColor(android.R.color.holo_orange_dark)));
            }

            // Add danger indicator if count > 0
            if (dangerCount > 0) {
                budgetWarningIndicators.addView(createWarningIndicator(dangerCount, android.R.drawable.ic_dialog_alert, 
                    getResources().getColor(android.R.color.holo_red_dark)));
            }
        } catch (Exception e) {
            // Ignore errors in indicator creation
        }
    }

    private void setupBudgetWarningHeaderClickListener() {
        budgetWarningHeader.setOnClickListener(v -> {
            isBudgetWarningCollapsed = !isBudgetWarningCollapsed;
            updateBudgetWarningVisibility();
            updateBudgetWarningExpandIcon();
        });
    }

    private void updateBudgetWarningExpandIcon() {
        if (isBudgetWarningCollapsed) {
            budgetWarningExpandIcon.setImageResource(R.drawable.ic_arrow_down);
        } else {
            budgetWarningExpandIcon.setImageResource(R.drawable.ic_arrow_up);
        }
    }

          private View createWarningIndicator(int count, int iconRes, int color) {
        // Create horizontal layout for indicator
        LinearLayout indicatorLayout = new LinearLayout(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(12, 0, 0, 0);
        indicatorLayout.setLayoutParams(layoutParams);
        indicatorLayout.setOrientation(LinearLayout.HORIZONTAL);
        indicatorLayout.setGravity(android.view.Gravity.CENTER_VERTICAL);

        // Create count text
        TextView countText = new TextView(this);
        LinearLayout.LayoutParams countParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        countText.setLayoutParams(countParams);
        countText.setText(String.valueOf(count));
        countText.setTextSize(20);
        countText.setTextColor(color);
        countText.setTypeface(null, android.graphics.Typeface.BOLD);

        // Create icon
        ImageView icon = new ImageView(this);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(40, 40);
        iconParams.setMargins(10, 0, 0, 0);
        icon.setLayoutParams(iconParams);
        icon.setImageResource(iconRes);
        icon.setColorFilter(color);

        // Add views to indicator layout
        indicatorLayout.addView(countText);
        indicatorLayout.addView(icon);

        return indicatorLayout;
    }

    private void updateBudgetWarningVisibility() {
        try {
            if (isBudgetWarningCollapsed) {
                budgetWarningsContainer.setVisibility(View.GONE);
            } else {
                budgetWarningsContainer.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            // Ignore errors in visibility update
        }
    }

    private void updateSummaryUI(MonthlySummary summary) {
        try {
            // Update spending summary card (always visible)
            totalSpentText.setText(String.format("$%.2f", summary.getTotalSpent()));
            unbudgetedSpendingText.setText(String.format("$%.2f", summary.getUnbudgetedSpending()));

            // Show/hide unbudgeted spending based on whether it exists
            if (summary.hasUnbudgetedSpending()) {
                unbudgetedSpendingText.setVisibility(View.VISIBLE);
                unbudgetedSpendingText.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
            } else {
                unbudgetedSpendingText.setVisibility(View.GONE);
            }

            // Update budget performance card (only show if user has budgets)
            if (summary.hasBudgets()) {
                // User has budgets - show budget performance card
                budgetedSpendingText.setText(String.format("$%.2f", summary.getBudgetedSpending()));
                totalBudgetText.setText(String.format("$%.2f", summary.getTotalBudget()));
                
                double utilization = summary.getBudgetUtilization();
                budgetUtilizationText.setText(String.format("%.1f%%", utilization));
                remainingBudgetText.setText(String.format("$%.2f", summary.getRemainingBudget()));
                
                // Update progress bar
                budgetProgressBar.setProgress((int) Math.min(utilization, 100));
                
                // Set progress bar color based on utilization
                if (summary.isOverBudget()) {
                    budgetProgressBar.setIndicatorColor(getResources().getColor(android.R.color.holo_red_dark));
                    budgetUtilizationText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                } else if (utilization > 80) {
                    budgetProgressBar.setIndicatorColor(getResources().getColor(android.R.color.holo_orange_dark));
                    budgetUtilizationText.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                } else {
                    budgetProgressBar.setIndicatorColor(getResources().getColor(android.R.color.holo_green_dark));
                    budgetUtilizationText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                }
                
                // Show budget performance card
                budgetPerformanceCard.setVisibility(View.VISIBLE);
            } else {
                // User has no budgets - hide budget performance card
                budgetPerformanceCard.setVisibility(View.GONE);
            }

            // Show/hide cards based on data availability
            spendingSummaryCard.setVisibility(View.VISIBLE);
            trendsCard.setVisibility(View.VISIBLE);
            pieChartCard.setVisibility(View.VISIBLE);
            categoryCard.setVisibility(View.VISIBLE);
            // Budget warning card will be shown/hidden by checkBudgetWarnings method
            emptyStateText.setVisibility(View.GONE);
        } catch (Exception e) {
            Toast.makeText(this, "Error updating UI: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateCategoryAnalysis(List<CategoryAnalysis> analysis) {
        try {
            if (analysis != null && !analysis.isEmpty()) {
                categoryAdapter.updateCategoryAnalysis(analysis);
                categoryRecyclerView.setVisibility(View.VISIBLE);
                updatePieChart(analysis);
            } else {
                categoryRecyclerView.setVisibility(View.GONE);
                showEmptyPieChart();
            }
        } catch (Exception e) {
            categoryRecyclerView.setVisibility(View.GONE);
            showEmptyPieChart();
        }
    }

    private void showLoadingState() {
        try {
            spendingSummaryCard.setVisibility(View.GONE);
            budgetPerformanceCard.setVisibility(View.GONE);
            trendsCard.setVisibility(View.GONE);
            pieChartCard.setVisibility(View.GONE);
            categoryCard.setVisibility(View.GONE);
            budgetWarningCard.setVisibility(View.GONE);
            emptyStateText.setVisibility(View.VISIBLE);
            emptyStateText.setText("Loading overview...");
        } catch (Exception e) {
            // Ignore errors in loading state
        }
    }

    private void showErrorState() {
        try {
            spendingSummaryCard.setVisibility(View.GONE);
            budgetPerformanceCard.setVisibility(View.GONE);
            trendsCard.setVisibility(View.GONE);
            pieChartCard.setVisibility(View.GONE);
            categoryCard.setVisibility(View.GONE);
            budgetWarningCard.setVisibility(View.GONE);
            emptyStateText.setVisibility(View.VISIBLE);
            emptyStateText.setText("Failed to load overview\nPlease try again");
        } catch (Exception e) {
            // Ignore errors in error state
        }
    }

    private void updatePieChart(List<CategoryAnalysis> analysis) {
        try {
            List<PieEntry> entries = new ArrayList<>();
            double totalSpent = 0;
            for (CategoryAnalysis cat : analysis) {
                totalSpent += cat.getTotalAmount();
            }

            // Create custom colors array that matches category colors
            List<Integer> customColors = new ArrayList<>();
            for (CategoryAnalysis cat : analysis) {
                int color = CategoryColorUtil.getCategoryColor(this, cat.getCategory());
                customColors.add(color);
            }

            for (CategoryAnalysis cat : analysis) {
                double percentage = (cat.getTotalAmount() / totalSpent) * 100;
                entries.add(new PieEntry((float) cat.getTotalAmount(), cat.getCategory() + " (" + String.format("%.1f%%", percentage) + ")"));
            }

            PieDataSet dataSet = new PieDataSet(entries, "Spending by Category");
            dataSet.setColors(customColors); // Use custom colors instead of MATERIAL_COLORS
            dataSet.setValueTextSize(12f);
            dataSet.setValueFormatter(new PercentFormatter(pieChart));
            dataSet.setDrawValues(false); // Don't show values on slices
            dataSet.setValueTextColor(Color.BLACK);

            PieData pieData = new PieData(dataSet);
            pieData.setDrawValues(false); // Don't show values on slices
            pieData.setValueTextSize(12f);
            pieData.setValueTextColor(Color.BLACK);

            pieChart.setData(pieData);
            pieChart.invalidate();
            pieChart.animateY(1000);
            
            // Create custom two-column legend
            createCustomLegend(analysis, totalSpent);
        } catch (Exception e) {
            Toast.makeText(this, "Error updating pie chart: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void createCustomLegend(List<CategoryAnalysis> analysis, double totalSpent) {
        try {
            customLegendContainer.removeAllViews();
            
            // Calculate how many items per column (half of total, rounded up)
            int itemsPerColumn = (int) Math.ceil(analysis.size() / 2.0);
            
            // Create horizontal layout for two columns
            LinearLayout horizontalLayout = new LinearLayout(this);
            horizontalLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);
            horizontalLayout.setWeightSum(2);
            
            // Create left column
            LinearLayout leftColumn = new LinearLayout(this);
            leftColumn.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
            ));
            leftColumn.setOrientation(LinearLayout.VERTICAL);
            
            // Create right column
            LinearLayout rightColumn = new LinearLayout(this);
            rightColumn.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
            ));
            rightColumn.setOrientation(LinearLayout.VERTICAL);
            
            // Add legend items to columns
            for (int i = 0; i < analysis.size(); i++) {
                CategoryAnalysis cat = analysis.get(i);
                double percentage = (cat.getTotalAmount() / totalSpent) * 100;
                int color = CategoryColorUtil.getCategoryColor(this, cat.getCategory());
                
                // Create legend item
                LinearLayout legendItem = new LinearLayout(this);
                legendItem.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                legendItem.setOrientation(LinearLayout.HORIZONTAL);
                legendItem.setGravity(android.view.Gravity.CENTER_VERTICAL);
                legendItem.setPadding(0, 4, 0, 4);
                
                // Create color indicator
                View colorIndicator = new View(this);
                colorIndicator.setLayoutParams(new LinearLayout.LayoutParams(16, 16));
                colorIndicator.setBackgroundColor(color);
                colorIndicator.setPadding(0, 0, 8, 0);
                
                // Create text
                TextView textView = new TextView(this);
                textView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                textView.setText(cat.getCategory() + " (" + String.format("%.1f%%", percentage) + ")");
                textView.setTextSize(11);
                textView.setTextColor(Color.BLACK);
                
                // Add views to legend item
                legendItem.addView(colorIndicator);
                legendItem.addView(textView);
                
                // Add to appropriate column
                if (i < itemsPerColumn) {
                    leftColumn.addView(legendItem);
                } else {
                    rightColumn.addView(legendItem);
                }
            }
            
            // Add columns to horizontal layout
            horizontalLayout.addView(leftColumn);
            horizontalLayout.addView(rightColumn);
            
            // Add horizontal layout to container
            customLegendContainer.addView(horizontalLayout);
            customLegendContainer.setVisibility(View.VISIBLE);
            
        } catch (Exception e) {
            Toast.makeText(this, "Error creating custom legend: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showEmptyPieChart() {
        try {
            pieChart.clear();
            pieChart.invalidate();
            pieChart.animateY(1000);
            pieChartEmptyText.setVisibility(View.VISIBLE);
            customLegendContainer.setVisibility(View.GONE);
        } catch (Exception e) {
            Toast.makeText(this, "Error showing empty pie chart: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showTooltip(String categoryName, double amount, float x, float y) {
        try {
            // Get the category color
            int categoryColor = CategoryColorUtil.getCategoryColor(this, categoryName);
            
            // Set the text content
            tooltipCategoryName.setText(categoryName);
            tooltipAmount.setText(String.format("$%.2f", amount));
            
            // Set the text colors to match the category color
            tooltipCategoryName.setTextColor(categoryColor);
            tooltipAmount.setTextColor(categoryColor);
            
            // Show the tooltip
            tooltipOverlay.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            Toast.makeText(this, "Error showing tooltip: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void hideTooltip() {
        try {
            tooltipOverlay.setVisibility(View.GONE);
        } catch (Exception e) {
            Toast.makeText(this, "Error hiding tooltip: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to the activity
        try {
            loadMonthlySummary();
        } catch (Exception e) {
            Toast.makeText(this, "Error refreshing data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
} 