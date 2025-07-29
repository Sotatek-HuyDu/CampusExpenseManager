package com.budgetwise.campusexpensemanager.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.budgetwise.campusexpensemanager.R;
import com.budgetwise.campusexpensemanager.firebase.repository.ExpenseAnalyticsRepository;
import com.budgetwise.campusexpensemanager.models.CategoryAnalysis;
import com.budgetwise.campusexpensemanager.models.MonthlySummary;
import com.budgetwise.campusexpensemanager.models.MonthlyTrend;
import com.budgetwise.campusexpensemanager.utils.SessionManager;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class OverviewActivity extends BaseActivity {

    private TextView totalSpentText;
    private TextView totalBudgetText;
    private TextView remainingBudgetText;
    private TextView budgetUtilizationText;
    private Spinner monthSpinner;
    private Spinner yearSpinner;
    private LinearProgressIndicator budgetProgressBar;
    private CardView summaryCard;
    private CardView trendsCard;
    private CardView categoryCard;
    private RecyclerView categoryRecyclerView;
    private TextView emptyStateText;

    private ExpenseAnalyticsRepository analyticsRepository;
    private SessionManager sessionManager;
    private CategoryAnalysisAdapter categoryAdapter;
    private Calendar currentDate;
    private SimpleDateFormat monthFormat;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_overview;
    }

    @Override
    protected void setupActivity() {
        try {
            // Initialize repositories and managers
            analyticsRepository = new ExpenseAnalyticsRepository();
            sessionManager = new SessionManager(this);
            currentDate = Calendar.getInstance();
            monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());

            // Initialize views
            initializeViews();
            setupToolbar();
            setupRecyclerView();
            setupMonthYearSpinners();
            setupClickListeners();

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
        try {
            totalSpentText = findViewById(R.id.total_spent_text);
            totalBudgetText = findViewById(R.id.total_budget_text);
            remainingBudgetText = findViewById(R.id.remaining_budget_text);
            budgetUtilizationText = findViewById(R.id.budget_utilization_text);
            monthSpinner = findViewById(R.id.month_spinner);
            yearSpinner = findViewById(R.id.year_spinner);
            budgetProgressBar = findViewById(R.id.budget_progress_bar);
            summaryCard = findViewById(R.id.summary_card);
            trendsCard = findViewById(R.id.trends_card);
            categoryCard = findViewById(R.id.category_card);
            categoryRecyclerView = findViewById(R.id.category_recycler_view);
            emptyStateText = findViewById(R.id.empty_state_text);
        } catch (Exception e) {
            Toast.makeText(this, "Error initializing views: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            throw e;
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Expense Overview");
        }
    }

    private void setupRecyclerView() {
        categoryAdapter = new CategoryAnalysisAdapter(this);
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        categoryRecyclerView.setHasFixedSize(true);
        categoryRecyclerView.setAdapter(categoryAdapter);
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

    private void updateSummaryUI(MonthlySummary summary) {
        try {
            // Update summary card
            totalSpentText.setText(String.format("$%.2f", summary.getTotalSpent()));
            totalBudgetText.setText(String.format("$%.2f", summary.getTotalBudget()));
            remainingBudgetText.setText(String.format("$%.2f", summary.getRemainingBudget()));

            // Update budget utilization
            double utilization = summary.getBudgetUtilization();
            budgetUtilizationText.setText(String.format("%.1f%%", utilization));
            
            // Update progress bar
            budgetProgressBar.setProgress((int) Math.min(utilization, 100));
            
            // Set progress bar color based on utilization
            if (summary.isOverBudget()) {
                budgetProgressBar.setIndicatorColor(getResources().getColor(android.R.color.holo_red_dark));
            } else if (utilization > 80) {
                budgetProgressBar.setIndicatorColor(getResources().getColor(android.R.color.holo_orange_dark));
            } else {
                budgetProgressBar.setIndicatorColor(getResources().getColor(android.R.color.holo_green_dark));
            }

            // Show/hide cards based on data availability
            summaryCard.setVisibility(View.VISIBLE);
            trendsCard.setVisibility(View.VISIBLE);
            categoryCard.setVisibility(View.VISIBLE);
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
            } else {
                categoryRecyclerView.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            categoryRecyclerView.setVisibility(View.GONE);
        }
    }

    private void showLoadingState() {
        try {
            summaryCard.setVisibility(View.GONE);
            trendsCard.setVisibility(View.GONE);
            categoryCard.setVisibility(View.GONE);
            emptyStateText.setVisibility(View.VISIBLE);
            emptyStateText.setText("Loading overview...");
        } catch (Exception e) {
            // Ignore errors in loading state
        }
    }

    private void showErrorState() {
        try {
            summaryCard.setVisibility(View.GONE);
            trendsCard.setVisibility(View.GONE);
            categoryCard.setVisibility(View.GONE);
            emptyStateText.setVisibility(View.VISIBLE);
            emptyStateText.setText("Failed to load overview\nPlease try again");
        } catch (Exception e) {
            // Ignore errors in error state
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