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
import com.budgetwise.campusexpensemanager.models.MonthlyTrend;
import com.budgetwise.campusexpensemanager.utils.SessionManager;

import java.util.List;

public class TrendsActivity extends BaseActivity {

    private RecyclerView trendsRecyclerView;
    private TextView emptyStateText;
    private ExpenseAnalyticsRepository analyticsRepository;
    private SessionManager sessionManager;
    private TrendsAdapter trendsAdapter;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_trends;
    }

    @Override
    protected void setupActivity() {
        // Initialize repositories
        analyticsRepository = new ExpenseAnalyticsRepository();
        sessionManager = new SessionManager(this);

        // Initialize views
        trendsRecyclerView = findViewById(R.id.trends_recycler_view);
        emptyStateText = findViewById(R.id.empty_state_text);

        // Setup toolbar
        setupToolbar();

        // Setup RecyclerView
        trendsAdapter = new TrendsAdapter(this);
        trendsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        trendsRecyclerView.setHasFixedSize(true);
        trendsRecyclerView.setAdapter(trendsAdapter);

        // Load trends data
        loadTrends();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Expense Trends");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void loadTrends() {
        String accountId = sessionManager.getAccountId();
        if (accountId == null) {
            Toast.makeText(this, "Please log in to view trends", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading state
        showLoadingState();

        analyticsRepository.getExpenseTrends(accountId, 6, new ExpenseAnalyticsRepository.TrendCallback() {
            @Override
            public void onSuccess(List<MonthlyTrend> trends) {
                runOnUiThread(() -> {
                    if (!isFinishing()) {
                        updateTrendsUI(trends);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    if (!isFinishing()) {
                        showErrorState();
                        Toast.makeText(TrendsActivity.this, 
                            "Failed to load trends: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void updateTrendsUI(List<MonthlyTrend> trends) {
        if (trends != null && !trends.isEmpty()) {
            trendsAdapter.updateTrends(trends);
            trendsRecyclerView.setVisibility(View.VISIBLE);
            emptyStateText.setVisibility(View.GONE);
        } else {
            trendsRecyclerView.setVisibility(View.GONE);
            emptyStateText.setVisibility(View.VISIBLE);
            emptyStateText.setText("No trend data available\nAdd expenses to see trends");
        }
    }

    private void showLoadingState() {
        trendsRecyclerView.setVisibility(View.GONE);
        emptyStateText.setVisibility(View.VISIBLE);
        emptyStateText.setText("Loading trends...");
    }

    private void showErrorState() {
        trendsRecyclerView.setVisibility(View.GONE);
        emptyStateText.setVisibility(View.VISIBLE);
        emptyStateText.setText("Failed to load trends\nPlease try again");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to the activity
        loadTrends();
    }
} 