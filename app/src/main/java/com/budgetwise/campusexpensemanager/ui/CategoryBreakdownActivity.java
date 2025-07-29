package com.budgetwise.campusexpensemanager.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.budgetwise.campusexpensemanager.R;
import com.budgetwise.campusexpensemanager.firebase.repository.ExpenseAnalyticsRepository;
import com.budgetwise.campusexpensemanager.models.CategoryAnalysis;
import com.budgetwise.campusexpensemanager.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CategoryBreakdownActivity extends BaseActivity {

    private RecyclerView categoryRecyclerView;
    private TextView emptyStateText;
    private TextView monthYearText;
    private ExpenseAnalyticsRepository analyticsRepository;
    private SessionManager sessionManager;
    private CategoryAnalysisAdapter categoryAdapter;
    private int selectedMonth;
    private int selectedYear;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_category_breakdown;
    }

    @Override
    protected void setupActivity() {
        // Get month and year from intent
        selectedMonth = getIntent().getIntExtra("month", Calendar.getInstance().get(Calendar.MONTH) + 1);
        selectedYear = getIntent().getIntExtra("year", Calendar.getInstance().get(Calendar.YEAR));

        // Initialize repositories
        analyticsRepository = new ExpenseAnalyticsRepository();
        sessionManager = new SessionManager(this);

        // Initialize views
        categoryRecyclerView = findViewById(R.id.category_recycler_view);
        emptyStateText = findViewById(R.id.empty_state_text);
        monthYearText = findViewById(R.id.month_year_text);

        // Setup toolbar
        setupToolbar();

        // Setup RecyclerView
        categoryAdapter = new CategoryAnalysisAdapter(this);
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        categoryRecyclerView.setHasFixedSize(true);
        categoryRecyclerView.setAdapter(categoryAdapter);

        // Update month/year display
        updateMonthYearDisplay();

        // Load category analysis
        loadCategoryAnalysis();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Category Breakdown");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void updateMonthYearDisplay() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(selectedYear, selectedMonth - 1, 1);
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        monthYearText.setText(monthFormat.format(calendar.getTime()));
    }

    private void loadCategoryAnalysis() {
        String accountId = sessionManager.getAccountId();
        if (accountId == null) {
            Toast.makeText(this, "Please log in to view category breakdown", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading state
        showLoadingState();

        analyticsRepository.getCategoryAnalysis(accountId, selectedMonth, selectedYear,
            new ExpenseAnalyticsRepository.CategoryAnalysisCallback() {
                @Override
                public void onSuccess(List<CategoryAnalysis> analysis) {
                    runOnUiThread(() -> {
                        if (!isFinishing()) {
                            updateCategoryAnalysisUI(analysis);
                        }
                    });
                }

                @Override
                public void onError(Exception e) {
                    runOnUiThread(() -> {
                        if (!isFinishing()) {
                            showErrorState();
                            Toast.makeText(CategoryBreakdownActivity.this, 
                                "Failed to load category breakdown: " + e.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
    }

    private void updateCategoryAnalysisUI(List<CategoryAnalysis> analysis) {
        if (analysis != null && !analysis.isEmpty()) {
            categoryAdapter.updateCategoryAnalysis(analysis);
            categoryRecyclerView.setVisibility(View.VISIBLE);
            emptyStateText.setVisibility(View.GONE);
        } else {
            categoryRecyclerView.setVisibility(View.GONE);
            emptyStateText.setVisibility(View.VISIBLE);
            emptyStateText.setText("No category data available\nAdd expenses to see breakdown");
        }
    }

    private void showLoadingState() {
        categoryRecyclerView.setVisibility(View.GONE);
        emptyStateText.setVisibility(View.VISIBLE);
        emptyStateText.setText("Loading category breakdown...");
    }

    private void showErrorState() {
        categoryRecyclerView.setVisibility(View.GONE);
        emptyStateText.setVisibility(View.VISIBLE);
        emptyStateText.setText("Failed to load category breakdown\nPlease try again");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to the activity
        loadCategoryAnalysis();
    }
} 