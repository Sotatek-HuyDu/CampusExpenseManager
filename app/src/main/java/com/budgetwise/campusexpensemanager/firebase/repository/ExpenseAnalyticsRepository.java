package com.budgetwise.campusexpensemanager.firebase.repository;

import com.budgetwise.campusexpensemanager.firebase.ExpenseRepository;
import com.budgetwise.campusexpensemanager.firebase.models.FirebaseBudget;
import com.budgetwise.campusexpensemanager.firebase.models.FirebaseExpense;
import com.budgetwise.campusexpensemanager.firebase.models.FirebaseRecurringExpense;
import com.budgetwise.campusexpensemanager.models.ExpenseCategory;
import com.budgetwise.campusexpensemanager.models.MonthlySummary;
import com.budgetwise.campusexpensemanager.models.MonthlyTrend;
import com.budgetwise.campusexpensemanager.models.DailyTrend;
import com.budgetwise.campusexpensemanager.models.CategoryAnalysis;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpenseAnalyticsRepository extends FirebaseRepository {
    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;
    private final RecurringExpenseRepository recurringExpenseRepository;

    public ExpenseAnalyticsRepository() {
        try {
            this.expenseRepository = new ExpenseRepository();
            this.budgetRepository = new BudgetRepository();
            this.recurringExpenseRepository = new RecurringExpenseRepository();
        } catch (Exception e) {
            android.util.Log.e("ExpenseAnalyticsRepository", "Error initializing repositories: " + e.getMessage());
            throw e;
        }
    }

    public void getMonthlySummary(String accountId, int month, int year, MonthlySummaryCallback callback) {
        // Get all expenses for the user
        expenseRepository.getExpensesByUser(accountId, new ExpenseRepository.ExpenseCallback() {
            @Override
            public void onSuccess(List<com.budgetwise.campusexpensemanager.models.Expense> expenses) {
                // Filter expenses for the specified month and year
                List<com.budgetwise.campusexpensemanager.models.Expense> monthlyExpenses = filterExpensesByMonth(expenses, month, year);
                
                // Calculate summary
                double totalSpent = calculateTotalSpent(monthlyExpenses);
                Map<String, Double> categoryBreakdown = calculateCategoryBreakdown(monthlyExpenses);
                
                // Get budget information
                getBudgetForMonth(accountId, month, year, budgets -> {
                    double totalBudget = calculateTotalBudget(budgets);
                    Map<String, Double> budgetByCategory = calculateBudgetByCategory(budgets);
                    
                    MonthlySummary summary = new MonthlySummary(
                        month, year, totalSpent, totalBudget, 
                        categoryBreakdown, budgetByCategory, monthlyExpenses
                    );
                    
                    callback.onSuccess(summary);
                }, e -> {
                    // If budget fetch fails, create summary without budget data
                    MonthlySummary summary = new MonthlySummary(
                        month, year, totalSpent, 0.0, 
                        categoryBreakdown, new HashMap<>(), monthlyExpenses
                    );
                    callback.onSuccess(summary);
                });
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    public void getExpenseTrends(String accountId, int monthsBack, TrendCallback callback) {
        expenseRepository.getExpensesByUser(accountId, new ExpenseRepository.ExpenseCallback() {
            @Override
            public void onSuccess(List<com.budgetwise.campusexpensemanager.models.Expense> expenses) {
                List<MonthlyTrend> trends = calculateTrends(expenses, monthsBack);
                callback.onSuccess(trends);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    public void getCategoryAnalysis(String accountId, int month, int year, CategoryAnalysisCallback callback) {
        expenseRepository.getExpensesByUser(accountId, new ExpenseRepository.ExpenseCallback() {
            @Override
            public void onSuccess(List<com.budgetwise.campusexpensemanager.models.Expense> expenses) {
                List<com.budgetwise.campusexpensemanager.models.Expense> monthlyExpenses = filterExpensesByMonth(expenses, month, year);
                List<CategoryAnalysis> analysis = calculateCategoryAnalysis(monthlyExpenses);
                callback.onSuccess(analysis);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    public void getDailyTrends(String accountId, int daysBack, DailyTrendCallback callback) {
        expenseRepository.getExpensesByUser(accountId, new ExpenseRepository.ExpenseCallback() {
            @Override
            public void onSuccess(List<com.budgetwise.campusexpensemanager.models.Expense> expenses) {
                List<DailyTrend> trends = calculateDailyTrends(expenses, daysBack);
                callback.onSuccess(trends);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    private List<com.budgetwise.campusexpensemanager.models.Expense> filterExpensesByMonth(
            List<com.budgetwise.campusexpensemanager.models.Expense> expenses, int month, int year) {
        List<com.budgetwise.campusexpensemanager.models.Expense> filtered = new ArrayList<>();
        Calendar targetCalendar = Calendar.getInstance();
        targetCalendar.set(year, month - 1, 1, 0, 0, 0);
        targetCalendar.set(Calendar.MILLISECOND, 0);
        
        long startOfMonth = targetCalendar.getTimeInMillis();
        targetCalendar.add(Calendar.MONTH, 1);
        long endOfMonth = targetCalendar.getTimeInMillis();

        for (com.budgetwise.campusexpensemanager.models.Expense expense : expenses) {
            if (expense.getDate() != null) {
                long expenseTime = expense.getDate().getTime();
                if (expenseTime >= startOfMonth && expenseTime < endOfMonth) {
                    filtered.add(expense);
                }
            }
        }
        return filtered;
    }

    private double calculateTotalSpent(List<com.budgetwise.campusexpensemanager.models.Expense> expenses) {
        double total = 0.0;
        for (com.budgetwise.campusexpensemanager.models.Expense expense : expenses) {
            total += expense.getAmount();
        }
        return total;
    }

    private Map<String, Double> calculateCategoryBreakdown(List<com.budgetwise.campusexpensemanager.models.Expense> expenses) {
        Map<String, Double> breakdown = new HashMap<>();
        for (com.budgetwise.campusexpensemanager.models.Expense expense : expenses) {
            String category = expense.getCategory();
            double currentAmount = breakdown.getOrDefault(category, 0.0);
            breakdown.put(category, currentAmount + expense.getAmount());
        }
        return breakdown;
    }

    private void getBudgetForMonth(String accountId, int month, int year, 
                                 java.util.function.Consumer<List<FirebaseBudget>> onSuccess,
                                 java.util.function.Consumer<Exception> onError) {
        budgetRepository.getBudgetByAccountAndMonth(accountId, month, year)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<FirebaseBudget> budgets = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            FirebaseBudget budget = snapshot.getValue(FirebaseBudget.class);
                            if (budget != null) {
                                budget.setId(snapshot.getKey());
                                budgets.add(budget);
                            }
                        }
                        onSuccess.accept(budgets);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        onError.accept(databaseError.toException());
                    }
                });
    }

    private double calculateTotalBudget(List<FirebaseBudget> budgets) {
        double total = 0.0;
        for (FirebaseBudget budget : budgets) {
            total += budget.getLimit();
        }
        return total;
    }

    private Map<String, Double> calculateBudgetByCategory(List<FirebaseBudget> budgets) {
        Map<String, Double> budgetMap = new HashMap<>();
        for (FirebaseBudget budget : budgets) {
            budgetMap.put(budget.getCategory(), budget.getLimit());
        }
        return budgetMap;
    }

    private List<MonthlyTrend> calculateTrends(List<com.budgetwise.campusexpensemanager.models.Expense> expenses, int monthsBack) {
        List<MonthlyTrend> trends = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        
        for (int i = 0; i < monthsBack; i++) {
            int month = calendar.get(Calendar.MONTH) + 1;
            int year = calendar.get(Calendar.YEAR);
            
            List<com.budgetwise.campusexpensemanager.models.Expense> monthlyExpenses = 
                filterExpensesByMonth(expenses, month, year);
            double total = calculateTotalSpent(monthlyExpenses);
            
            trends.add(new MonthlyTrend(month, year, total));
            calendar.add(Calendar.MONTH, -1);
        }
        
        return trends;
    }

    private List<CategoryAnalysis> calculateCategoryAnalysis(List<com.budgetwise.campusexpensemanager.models.Expense> expenses) {
        Map<String, CategoryAnalysis> analysisMap = new HashMap<>();
        
        for (com.budgetwise.campusexpensemanager.models.Expense expense : expenses) {
            String category = expense.getCategory();
            CategoryAnalysis analysis = analysisMap.getOrDefault(category, 
                new CategoryAnalysis(category, 0.0, 0));
            
            analysis.setTotalAmount(analysis.getTotalAmount() + expense.getAmount());
            analysis.setTransactionCount(analysis.getTransactionCount() + 1);
            analysisMap.put(category, analysis);
        }
        
        // Calculate percentages
        double totalAmount = calculateTotalSpent(expenses);
        for (CategoryAnalysis analysis : analysisMap.values()) {
            if (totalAmount > 0) {
                analysis.setPercentageOfTotal((analysis.getTotalAmount() / totalAmount) * 100);
            }
        }
        
        return new ArrayList<>(analysisMap.values());
    }

    private List<DailyTrend> calculateDailyTrends(List<com.budgetwise.campusexpensemanager.models.Expense> expenses, int daysBack) {
        List<DailyTrend> trends = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        
        // Create a map to group expenses by date
        Map<String, List<com.budgetwise.campusexpensemanager.models.Expense>> dailyExpenses = new HashMap<>();
        
        // Group expenses by date
        for (com.budgetwise.campusexpensemanager.models.Expense expense : expenses) {
            if (expense.getDate() != null) {
                Calendar expenseCal = Calendar.getInstance();
                expenseCal.setTime(expense.getDate());
                
                // Check if expense is within the specified days back
                Calendar cutoffDate = Calendar.getInstance();
                cutoffDate.add(Calendar.DAY_OF_YEAR, -daysBack);
                
                if (expenseCal.after(cutoffDate) || expenseCal.equals(cutoffDate)) {
                    String dateKey = String.format("%d-%d-%d", 
                        expenseCal.get(Calendar.YEAR),
                        expenseCal.get(Calendar.MONTH) + 1,
                        expenseCal.get(Calendar.DAY_OF_MONTH));
                    
                    dailyExpenses.computeIfAbsent(dateKey, k -> new ArrayList<>()).add(expense);
                }
            }
        }
        
        // Generate daily trends for the last N days
        for (int i = 0; i < daysBack; i++) {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH) + 1;
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            
            String dateKey = String.format("%d-%d-%d", year, month, day);
            List<com.budgetwise.campusexpensemanager.models.Expense> dayExpenses = dailyExpenses.getOrDefault(dateKey, new ArrayList<>());
            
            double totalAmount = calculateTotalSpent(dayExpenses);
            int transactionCount = dayExpenses.size();
            
            trends.add(new DailyTrend(calendar.getTime(), totalAmount, transactionCount));
            calendar.add(Calendar.DAY_OF_YEAR, -1);
        }
        
        // Reverse to show oldest to newest
        java.util.Collections.reverse(trends);
        return trends;
    }

    // Callback interfaces
    public interface MonthlySummaryCallback {
        void onSuccess(MonthlySummary summary);
        void onError(Exception e);
    }

    public interface TrendCallback {
        void onSuccess(List<MonthlyTrend> trends);
        void onError(Exception e);
    }

    public interface CategoryAnalysisCallback {
        void onSuccess(List<CategoryAnalysis> analysis);
        void onError(Exception e);
    }

    public interface DailyTrendCallback {
        void onSuccess(List<DailyTrend> trends);
        void onError(Exception e);
    }


} 