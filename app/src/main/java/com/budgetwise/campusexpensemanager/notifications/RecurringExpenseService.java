package com.budgetwise.campusexpensemanager.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.budgetwise.campusexpensemanager.R;
import com.budgetwise.campusexpensemanager.firebase.ExpenseRepository;
import com.budgetwise.campusexpensemanager.firebase.models.FirebaseRecurringExpense;
import com.budgetwise.campusexpensemanager.firebase.repository.RecurringExpenseRepository;
import com.budgetwise.campusexpensemanager.models.Expense;
import com.budgetwise.campusexpensemanager.notifications.RecurringExpenseProcessor;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class RecurringExpenseService extends Service {

    private static final String TAG = "RecurringExpenseService";
    private static final String CHANNEL_ID = "recurring_expense_channel";
    private static final int NOTIFICATION_ID = 1;

    private RecurringExpenseRepository recurringExpenseRepository;
    private ExpenseRepository expenseRepository;
    private RecurringExpenseProcessor recurringExpenseProcessor;

    @Override
    public void onCreate() {
        super.onCreate();
        recurringExpenseRepository = new RecurringExpenseRepository();
        expenseRepository = new ExpenseRepository();
        recurringExpenseProcessor = new RecurringExpenseProcessor();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "RecurringExpenseService started");
        if (intent != null) {
            String accountId = intent.getStringExtra("account_id");
            if (accountId != null) {
                processRecurringExpenses(accountId);
            } else {
                processRecurringExpenses();
            }
        } else {
            processRecurringExpenses();
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void processRecurringExpenses() {
        // This would typically be called periodically or when the app starts
        Log.d(TAG, "Processing recurring expenses...");
        
        // For now, we'll process recurring expenses for the current user
        // In a real implementation, you would get all users and process for each
        String accountId = "current_user"; // This should be the actual user ID
        recurringExpenseProcessor.processRecurringExpenses(accountId);
        
        Log.d(TAG, "Recurring expense processing completed");
    }
    
    private void processRecurringExpenses(String accountId) {
        Log.d(TAG, "Processing recurring expenses for account: " + accountId);
        recurringExpenseProcessor.processRecurringExpenses(accountId);
        Log.d(TAG, "Recurring expense processing completed");
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Recurring Expenses",
                NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Notifications for recurring expenses");
            
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showNotification(String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_recurring)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    public static boolean shouldCreateExpense(FirebaseRecurringExpense recurringExpense) {
        Calendar now = Calendar.getInstance();
        Calendar startDate = Calendar.getInstance();
        Calendar endDate = Calendar.getInstance();
        
        if (recurringExpense.getStartDate() != null) {
            startDate.setTime(recurringExpense.getStartDate());
        }
        if (recurringExpense.getEndDate() != null) {
            endDate.setTime(recurringExpense.getEndDate());
        }

        // Check if we're within the date range
        if (now.before(startDate) || now.after(endDate)) {
            return false;
        }

        // Check if it's time for the next occurrence
        // This is a simplified check - in a real implementation, you'd track the last occurrence
        return true;
    }

    public static Date getNextOccurrenceDate(FirebaseRecurringExpense recurringExpense) {
        Calendar nextDate = Calendar.getInstance();
        if (recurringExpense.getStartDate() != null) {
            nextDate.setTime(recurringExpense.getStartDate());
        }
        
        // Add the interval days
        nextDate.add(Calendar.DAY_OF_YEAR, recurringExpense.getRecurrenceIntervalDays());
        
        return nextDate.getTime();
    }
} 