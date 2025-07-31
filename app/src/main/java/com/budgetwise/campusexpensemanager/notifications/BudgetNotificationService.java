package com.budgetwise.campusexpensemanager.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.budgetwise.campusexpensemanager.R;
import com.budgetwise.campusexpensemanager.firebase.models.FirebaseBudget;
import com.budgetwise.campusexpensemanager.firebase.repository.BudgetRepository;
import com.budgetwise.campusexpensemanager.firebase.ExpenseRepository;
import com.budgetwise.campusexpensemanager.firebase.models.FirebaseExpense;
import com.budgetwise.campusexpensemanager.firebase.models.FirebaseRecurringExpense;
import com.budgetwise.campusexpensemanager.firebase.repository.RecurringExpenseRepository;
import com.budgetwise.campusexpensemanager.ui.BudgetActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class BudgetNotificationService {
    private static final String CHANNEL_ID = "budget_notifications";
    private static final String CHANNEL_NAME = "Budget Alerts";
    private static final String CHANNEL_DESCRIPTION = "Notifications for budget warnings and alerts";
    
    private static final int BUDGET_WARNING_THRESHOLD = 80; // 80%
    private static final int BUDGET_LIMIT_THRESHOLD = 100; // 100%
    private static final int BUDGET_EXCEEDED_THRESHOLD = 100; // >100%
    
    private final Context context;
    private final NotificationManager notificationManager;
    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;
    private final RecurringExpenseRepository recurringExpenseRepository;
    
    public BudgetNotificationService(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        this.budgetRepository = new BudgetRepository();
        this.expenseRepository = new ExpenseRepository();
        this.recurringExpenseRepository = new RecurringExpenseRepository();
        createNotificationChannel();
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            notificationManager.createNotificationChannel(channel);
        }
    }
    
    public void checkBudgetThreshold(String accountId, String category, double expenseAmount, int month, int year) {
        budgetRepository.getBudgetByAccountAndMonth(accountId, month, year)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    FirebaseBudget budget = null;
                    
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        FirebaseBudget currentBudget = snapshot.getValue(FirebaseBudget.class);
                        if (currentBudget != null && currentBudget.getCategory().equals(category) && 
                            currentBudget.getMonth() == month && currentBudget.getYear() == year) {
                            budget = currentBudget;
                            budget.setId(snapshot.getKey());
                            break;
                        }
                    }
                    
                    if (budget != null) {
                        calculateCurrentSpending(accountId, category, month, year, expenseAmount, budget);
                    }
                }
                
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
    }
    
    private void calculateCurrentSpending(String accountId, String category, int month, int year, 
                                        double newExpenseAmount, FirebaseBudget budget) {
        expenseRepository.getExpensesByCategoryAndMonth(accountId, category, month, year)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    double totalSpent = 0.0;
                    
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        FirebaseExpense expense = snapshot.getValue(FirebaseExpense.class);
                        if (expense != null && isExpenseInMonth(expense, month, year)) {
                            totalSpent += expense.getAmount();
                        }
                    }
                    
                    addRecurringExpensesToCalculation(accountId, category, month, year, totalSpent, budget);
                }
                
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
    }
    
    private void addRecurringExpensesToCalculation(String accountId, String category, int month, int year, 
                                                 double currentTotal, FirebaseBudget budget) {
        recurringExpenseRepository.getRecurringExpensesByAccount(accountId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    double recurringTotal = currentTotal;
                    
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        FirebaseRecurringExpense recurringExpense = snapshot.getValue(FirebaseRecurringExpense.class);
                        if (recurringExpense != null && recurringExpense.getCategory().equals(category)) {
                            if (shouldCreateExpenseInMonth(recurringExpense, month, year)) {
                                recurringTotal += recurringExpense.getAmount();
                            }
                        }
                    }
                    
                    checkThresholdsAndNotify(budget, recurringTotal, category);
                }
                
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    checkThresholdsAndNotify(budget, currentTotal, category);
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
        boolean shouldInclude = !startDate.after(targetMonthEnd) && !endDate.before(targetMonthStart);
        
        return shouldInclude;
    }
    
    private void checkThresholdsAndNotify(FirebaseBudget budget, double totalSpent, String category) {
        double budgetLimit = budget.getLimit();
        double utilizationPercentage = (totalSpent / budgetLimit) * 100;
        
        if (utilizationPercentage > BUDGET_EXCEEDED_THRESHOLD) {
            // Budget exceeded (>100%)
            sendBudgetExceededNotification(category, totalSpent, budgetLimit, utilizationPercentage);
        } else if (Math.abs(utilizationPercentage - BUDGET_LIMIT_THRESHOLD) < 0.1) {
            // Budget limit reached (=100%)
            sendBudgetLimitNotification(category, totalSpent, budgetLimit);
        } else if (utilizationPercentage >= BUDGET_WARNING_THRESHOLD) {
            // Budget warning (>=80%)
            sendBudgetWarningNotification(category, totalSpent, budgetLimit);
        }
    }
    
    private void sendBudgetWarningNotification(String category, double totalSpent, double budgetLimit) {
        double remainingAmount = budgetLimit - totalSpent;
        String title = "Budget Warning";
        String message = String.format("Becareful, you only have $%.2f left to spend on %s!", remainingAmount, category);
        
        showNotification(title, message, 1);
    }
    
    private void sendBudgetLimitNotification(String category, double totalSpent, double budgetLimit) {
        String title = "Budget Limit Reached";
        String message = String.format("Becareful, you have reach the limit for %s's budget.", category);
        
        showNotification(title, message, 2);
    }
    
    private void sendBudgetExceededNotification(String category, double totalSpent, double budgetLimit, double percentage) {
        double overflowAmount = totalSpent - budgetLimit;
        String title = "Budget Exceeded!";
        String message = String.format("Oh no, you have overspent on %s by $%.2f!", category, overflowAmount);
        
        showNotification(title, message, 3);
    }
    
    private void showNotification(String title, String message, int notificationId) {
        // Create intent for when notification is tapped - open BudgetActivity directly
        Intent intent = new Intent(context, com.budgetwise.campusexpensemanager.ui.BudgetActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 
            0, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_warning)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent);
        
        // Show notification
        notificationManager.notify(notificationId, builder.build());
    }
} 