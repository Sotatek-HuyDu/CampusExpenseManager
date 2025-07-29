package com.budgetwise.campusexpensemanager.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.budgetwise.campusexpensemanager.R;
import com.budgetwise.campusexpensemanager.firebase.repository.ExpenseAnalyticsRepository;
import com.budgetwise.campusexpensemanager.models.DailyTrend;
import com.budgetwise.campusexpensemanager.utils.SessionManager;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DailyTrendsActivity extends BaseActivity {

    private LineChart lineChart;
    private TextView totalSpentText;
    private TextView averageDailyText;
    private TextView highestDayText;
    private TextView emptyStateText;
    
    private ExpenseAnalyticsRepository analyticsRepository;
    private SessionManager sessionManager;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_daily_trends;
    }

    @Override
    protected void setupActivity() {
        // Initialize repositories
        analyticsRepository = new ExpenseAnalyticsRepository();
        sessionManager = new SessionManager(this);

        // Initialize views
        lineChart = findViewById(R.id.line_chart);
        totalSpentText = findViewById(R.id.total_spent_text);
        averageDailyText = findViewById(R.id.average_daily_text);
        highestDayText = findViewById(R.id.highest_day_text);
        emptyStateText = findViewById(R.id.empty_state_text);

        // Setup toolbar
        setupToolbar();

        // Setup chart
        setupChart();

        // Load daily trends
        loadDailyTrends();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            // Get selected month/year from intent
            int selectedMonth = getIntent().getIntExtra("month", -1);
            int selectedYear = getIntent().getIntExtra("year", -1);
            
            if (selectedMonth != -1 && selectedYear != -1) {
                String[] months = {"January", "February", "March", "April", "May", "June",
                                  "July", "August", "September", "October", "November", "December"};
                String title = months[selectedMonth - 1] + " " + selectedYear + " - Daily Trends";
                getSupportActionBar().setTitle(title);
            } else {
                getSupportActionBar().setTitle("Daily Spending Trends");
            }
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupChart() {
        // Configure chart appearance
        lineChart.getDescription().setEnabled(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setPinchZoom(true);
        lineChart.setDrawGridBackground(false);

        // Configure X axis
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < dailyTrends.size()) {
                    return dailyTrends.get(index).getDayString();
                }
                return "";
            }
        });

        // Configure Y axis
        lineChart.getAxisLeft().setDrawGridLines(true);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
    }

    private List<DailyTrend> dailyTrends = new ArrayList<>();

    private void loadDailyTrends() {
        String accountId = sessionManager.getAccountId();
        if (accountId == null) {
            Toast.makeText(this, "Please log in to view daily trends", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading state
        showLoadingState();

        // Get selected month/year from intent, or use current month
        int selectedMonth = getIntent().getIntExtra("month", -1);
        int selectedYear = getIntent().getIntExtra("year", -1);
        
        if (selectedMonth != -1 && selectedYear != -1) {
            // Load data for the selected month
            loadDailyTrendsForMonth(accountId, selectedMonth, selectedYear);
                } else {
            // Load last 30 days of data (default behavior)
            analyticsRepository.getDailyTrends(accountId, 30, new ExpenseAnalyticsRepository.DailyTrendCallback() {
                @Override
                public void onSuccess(List<DailyTrend> trends) {
                    runOnUiThread(() -> {
                        if (!isFinishing()) {
                            updateDailyTrendsUI(trends);
                        }
                    });
                }

                @Override
                public void onError(Exception e) {
                    runOnUiThread(() -> {
                        if (!isFinishing()) {
                            showErrorState();
                            Toast.makeText(DailyTrendsActivity.this, 
                                "Failed to load daily trends: " + e.getMessage(), 
                                Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
    }

    private void loadDailyTrendsForMonth(String accountId, int month, int year) {
        // Calculate the number of days in the selected month
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, 1);
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        
        // Load daily trends for the specific month
        analyticsRepository.getDailyTrends(accountId, daysInMonth, new ExpenseAnalyticsRepository.DailyTrendCallback() {
            @Override
            public void onSuccess(List<DailyTrend> trends) {
                runOnUiThread(() -> {
                    if (!isFinishing()) {
                        // Filter trends to only include the selected month
                        List<DailyTrend> monthlyTrends = new ArrayList<>();
                        for (DailyTrend trend : trends) {
                            if (trend.getMonth() == month && trend.getYear() == year) {
                                monthlyTrends.add(trend);
                            }
                        }
                        updateDailyTrendsUI(monthlyTrends);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    if (!isFinishing()) {
                        showErrorState();
                        Toast.makeText(DailyTrendsActivity.this, 
                            "Failed to load daily trends: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void updateDailyTrendsUI(List<DailyTrend> trends) {
        if (trends != null && !trends.isEmpty()) {
            this.dailyTrends = trends;
            
            // Update summary statistics
            updateSummaryStats(trends);
            
            // Update chart
            updateChart(trends);
            
            // Show chart, hide empty state
            lineChart.setVisibility(View.VISIBLE);
            emptyStateText.setVisibility(View.GONE);
        } else {
            showEmptyState();
        }
    }

    private void updateSummaryStats(List<DailyTrend> trends) {
        double totalSpent = 0;
        double highestDay = 0;
        String highestDayDate = "";
        
        for (DailyTrend trend : trends) {
            totalSpent += trend.getTotalAmount();
            if (trend.getTotalAmount() > highestDay) {
                highestDay = trend.getTotalAmount();
                highestDayDate = trend.getFullDateString();
            }
        }
        
        double averageDaily = totalSpent / trends.size();
        
        totalSpentText.setText(String.format("$%.2f", totalSpent));
        averageDailyText.setText(String.format("$%.2f", averageDaily));
        highestDayText.setText(String.format("$%.2f on %s", highestDay, highestDayDate));
    }

    private void updateChart(List<DailyTrend> trends) {
        ArrayList<Entry> entries = new ArrayList<>();
        
        for (int i = 0; i < trends.size(); i++) {
            DailyTrend trend = trends.get(i);
            entries.add(new Entry(i, (float) trend.getTotalAmount()));
        }
        
        LineDataSet dataSet = new LineDataSet(entries, "Daily Spending");
        dataSet.setColor(Color.parseColor("#2196F3"));
        dataSet.setCircleColor(Color.parseColor("#2196F3"));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(true);
        dataSet.setValueTextSize(10f);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        
        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        lineChart.invalidate();
    }

    private void showLoadingState() {
        lineChart.setVisibility(View.GONE);
        emptyStateText.setVisibility(View.VISIBLE);
        emptyStateText.setText("Loading daily trends...");
    }

    private void showErrorState() {
        lineChart.setVisibility(View.GONE);
        emptyStateText.setVisibility(View.VISIBLE);
        emptyStateText.setText("Failed to load daily trends\nPlease try again");
    }

    private void showEmptyState() {
        lineChart.setVisibility(View.GONE);
        emptyStateText.setVisibility(View.VISIBLE);
        emptyStateText.setText("No daily trend data available\nAdd expenses to see trends");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to the activity
        loadDailyTrends();
    }
} 