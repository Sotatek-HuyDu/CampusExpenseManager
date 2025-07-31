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
        expenseRepository.getExpensesByUser(accountId, new ExpenseRepository.ExpenseCallback() {
            @Override
            public void onSuccess(List<com.budgetwise.campusexpensemanager.models.Expense> expenses) {
                List<com.budgetwise.campusexpensemanager.models.Expense> monthlyExpenses = filterExpensesByMonth(expenses, month, year);
                
                double totalSpent = calculateTotalSpent(monthlyExpenses);
                Map<String, Double> categoryBreakdown = calculateCategoryBreakdown(monthlyExpenses);
                
                // Add recurring expenses to the calculation
                addRecurringExpensesToSummary(accountId, month, year, totalSpent, categoryBreakdown, 
                    (recurringTotal, recurringBreakdown) -> {
                        double finalTotalSpent = totalSpent + recurringTotal;
                        
                        // Merge category breakdowns
                        for (Map.Entry<String, Double> entry : recurringBreakdown.entrySet()) {
                            String category = entry.getKey();
                            double currentAmount = categoryBreakdown.getOrDefault(category, 0.0);
                            categoryBreakdown.put(category, currentAmount + entry.getValue());
                        }
                        
                        getBudgetForMonth(accountId, month, year, budgets -> {
                            double totalBudget = calculateTotalBudget(budgets);
                            Map<String, Double> budgetByCategory = calculateBudgetByCategory(budgets);
                            
                            MonthlySummary summary = new MonthlySummary(
                                month, year, finalTotalSpent, totalBudget, 
                                categoryBreakdown, budgetByCategory, monthlyExpenses
                            );
                            
                            callback.onSuccess(summary);
                        }, e -> {
                            MonthlySummary summary = new MonthlySummary(
                                month, year, finalTotalSpent, 0.0, 
                                categoryBreakdown, new HashMap<>(), monthlyExpenses
                            );
                            callback.onSuccess(summary);
                        });
                    });
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    public void getExpenseTrends(String accountId, int monthsBack, TrendCallback callback) {
        expenseRepository.getAllExpensesByUser(accountId, new ExpenseRepository.ExpenseCallback() {
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
        expenseRepository.getAllExpensesByUser(accountId, new ExpenseRepository.ExpenseCallback() {
            @Override
            public void onSuccess(List<com.budgetwise.campusexpensemanager.models.Expense> expenses) {
                List<com.budgetwise.campusexpensemanager.models.Expense> monthlyExpenses = filterExpensesByMonth(expenses, month, year);
                List<CategoryAnalysis> analysis = calculateCategoryAnalysis(monthlyExpenses);
                
                // Add recurring expenses to category analysis
                addRecurringExpensesToCategoryAnalysis(accountId, month, year, analysis, callback);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    public void getDailyTrends(String accountId, int daysBack, DailyTrendCallback callback) {
        expenseRepository.getAllExpensesByUser(accountId, new ExpenseRepository.ExpenseCallback() {
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
                            if (budget != null && budget.getMonth() == month && budget.getYear() == year) {
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

    private void addRecurringExpensesToSummary(String accountId, int month, int year, 
                                             double currentTotal, Map<String, Double> currentBreakdown,
                                             RecurringExpenseCallback callback) {
        recurringExpenseRepository.getRecurringExpensesByAccount(accountId)
            .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                @Override
                public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                    double recurringTotal = 0.0;
                    Map<String, Double> recurringBreakdown = new HashMap<>();
                    
                    for (com.google.firebase.database.DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        com.budgetwise.campusexpensemanager.firebase.models.FirebaseRecurringExpense recurringExpense = 
                            snapshot.getValue(com.budgetwise.campusexpensemanager.firebase.models.FirebaseRecurringExpense.class);
                        
                        if (recurringExpense != null && shouldCreateExpenseInMonth(recurringExpense, month, year)) {
                            double amount = recurringExpense.getAmount();
                            recurringTotal += amount;
                            
                            String category = recurringExpense.getCategory();
                            double currentAmount = recurringBreakdown.getOrDefault(category, 0.0);
                            recurringBreakdown.put(category, currentAmount + amount);
                        }
                    }
                    
                    callback.onRecurringExpensesCalculated(recurringTotal, recurringBreakdown);
                }

                @Override
                public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
                    callback.onRecurringExpensesCalculated(0.0, new HashMap<>());
                }
            });
    }
    
    private boolean shouldCreateExpenseInMonth(com.budgetwise.campusexpensemanager.firebase.models.FirebaseRecurringExpense recurringExpense, int month, int year) {
        if (recurringExpense.getStartDate() == null || recurringExpense.getEndDate() == null) {
            return false;
        }
        
        Calendar startDate = Calendar.getInstance();
        Calendar endDate = Calendar.getInstance();
        startDate.setTime(recurringExpense.getStartDate());
        endDate.setTime(recurringExpense.getEndDate());
        
        // Create target month start and end dates
        Calendar targetMonthStart = Calendar.getInstance();
        targetMonthStart.set(year, month - 1, 1, 0, 0, 0);
        targetMonthStart.set(Calendar.MILLISECOND, 0);
        
        Calendar targetMonthEnd = Calendar.getInstance();
        targetMonthEnd.set(year, month - 1, targetMonthStart.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59);
        targetMonthEnd.set(Calendar.MILLISECOND, 999);
        
        // Check if the recurring expense period overlaps with the target month
        // The recurring expense should be included if:
        // 1. Start date is before or on the last day of target month AND
        // 2. End date is after or on the first day of target month
        boolean shouldInclude = !startDate.after(targetMonthEnd) && !endDate.before(targetMonthStart);
        
        return shouldInclude;
    }
    
    private void addRecurringExpensesToCategoryAnalysis(String accountId, int month, int year, 
                                                       List<CategoryAnalysis> existingAnalysis, 
                                                       CategoryAnalysisCallback callback) {
        recurringExpenseRepository.getRecurringExpensesByAccount(accountId)
            .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                @Override
                public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                    Map<String, CategoryAnalysis> analysisMap = new HashMap<>();
                    
                    // Convert existing analysis to map for easy lookup
                    for (CategoryAnalysis analysis : existingAnalysis) {
                        analysisMap.put(analysis.getCategory(), analysis);
                    }
                    
                    // Add recurring expenses
                    for (com.google.firebase.database.DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        com.budgetwise.campusexpensemanager.firebase.models.FirebaseRecurringExpense recurringExpense = 
                            snapshot.getValue(com.budgetwise.campusexpensemanager.firebase.models.FirebaseRecurringExpense.class);
                        
                        if (recurringExpense != null && shouldCreateExpenseInMonth(recurringExpense, month, year)) {
                            String category = recurringExpense.getCategory();
                            double amount = recurringExpense.getAmount();
                            
                            CategoryAnalysis analysis = analysisMap.get(category);
                            if (analysis != null) {
                                // Update existing category
                                analysis.setTotalAmount(analysis.getTotalAmount() + amount);
                                analysis.setTransactionCount(analysis.getTransactionCount() + 1);
                            } else {
                                // Create new category
                                analysis = new CategoryAnalysis(category, amount, 1);
                                analysisMap.put(category, analysis);
                            }
                        }
                    }
                    
                    // Recalculate percentages
                    double totalAmount = 0.0;
                    for (CategoryAnalysis analysis : analysisMap.values()) {
                        totalAmount += analysis.getTotalAmount();
                    }
                    
                    for (CategoryAnalysis analysis : analysisMap.values()) {
                        if (totalAmount > 0) {
                            analysis.setPercentageOfTotal((analysis.getTotalAmount() / totalAmount) * 100);
                        }
                    }
                    
                    callback.onSuccess(new ArrayList<>(analysisMap.values()));
                }

                @Override
                public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
                    callback.onSuccess(existingAnalysis);
                }
            });
    }
    
    interface RecurringExpenseCallback {
        void onRecurringExpensesCalculated(double totalAmount, Map<String, Double> categoryBreakdown);
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