package com.budgetwise.campusexpensemanager.notifications;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.budgetwise.campusexpensemanager.R;
import com.budgetwise.campusexpensemanager.firebase.ExpenseRepository;
import com.budgetwise.campusexpensemanager.ui.OverviewActivity;
import com.budgetwise.campusexpensemanager.utils.SessionManager;

import java.util.Calendar;
import java.util.List;

public class DailyNotificationService {
    private static final String CHANNEL_ID = "daily_notifications";
    private static final String CHANNEL_NAME = "Daily Reminders";
    private static final String CHANNEL_DESCRIPTION = "Daily budget reminders and spending summaries";
    
    private static final int MORNING_NOTIFICATION_ID = 100;
    private static final int EVENING_NOTIFICATION_ID = 101;
    
    private final Context context;
    private final NotificationManager notificationManager;
    private final SessionManager sessionManager;
    private final ExpenseRepository expenseRepository;
    
    public DailyNotificationService(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        this.sessionManager = new SessionManager(context);
        this.expenseRepository = new ExpenseRepository();
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
    
    public void scheduleDailyNotifications() {
        if (!sessionManager.isLoggedIn()) {
            return;
        }
        
        scheduleMorningNotification();
        scheduleEveningNotification();
    }
    
    public void cancelDailyNotifications() {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        
        Intent morningIntent = new Intent(context, MorningNotificationReceiver.class);
        PendingIntent morningPendingIntent = PendingIntent.getBroadcast(
            context, MORNING_NOTIFICATION_ID, morningIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(morningPendingIntent);
        
        Intent eveningIntent = new Intent(context, EveningNotificationReceiver.class);
        PendingIntent eveningPendingIntent = PendingIntent.getBroadcast(
            context, EVENING_NOTIFICATION_ID, eveningIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(eveningPendingIntent);
    }
    
    private void scheduleMorningNotification() {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 6);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        
        Intent intent = new Intent(context, MorningNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context, MORNING_NOTIFICATION_ID, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.getTimeInMillis(),
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        );
    }
    
    private void scheduleEveningNotification() {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 21);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        
        Intent intent = new Intent(context, EveningNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context, EVENING_NOTIFICATION_ID, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.getTimeInMillis(),
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        );
    }
    
    public static class MorningNotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            DailyNotificationService service = new DailyNotificationService(context);
            service.showMorningNotification();
        }
    }
    
    public static class EveningNotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            DailyNotificationService service = new DailyNotificationService(context);
            service.showEveningNotification();
        }
    }
    
    private void showMorningNotification() {
        if (!sessionManager.isLoggedIn()) {
            return;
        }
        
        String title = "Good Morning!";
        String message = "Good morning! Let's take a look at your current progress this month.";
        
        showNotification(title, message, MORNING_NOTIFICATION_ID);
    }
    
    private void showEveningNotification() {
        if (!sessionManager.isLoggedIn()) {
            return;
        }
        
        String accountId = sessionManager.getAccountId();
        if (accountId == null) {
            return;
        }
        
        calculateTodaySpending(accountId);
    }
    
    private void calculateTodaySpending(String accountId) {
        Calendar today = Calendar.getInstance();
        int day = today.get(Calendar.DAY_OF_MONTH);
        int month = today.get(Calendar.MONTH) + 1;
        int year = today.get(Calendar.YEAR);
        
        expenseRepository.getAllExpensesByUser(accountId, new ExpenseRepository.ExpenseCallback() {
            @Override
            public void onSuccess(List<com.budgetwise.campusexpensemanager.models.Expense> expenses) {
                double todaySpending = 0.0;
                
                for (com.budgetwise.campusexpensemanager.models.Expense expense : expenses) {
                    if (isExpenseToday(expense, day, month, year)) {
                        todaySpending += expense.getAmount();
                    }
                }
                
                // Show notification with only today's actual expenses (no recurring)
                showEveningNotificationWithAmount(todaySpending);
            }
            
            @Override
            public void onError(Exception e) {
                showEveningNotificationWithAmount(0.0);
            }
        });
    }
    
    private boolean isExpenseToday(com.budgetwise.campusexpensemanager.models.Expense expense, int day, int month, int year) {
        if (expense.getDate() == null) return false;
        
        Calendar expenseCal = Calendar.getInstance();
        expenseCal.setTime(expense.getDate());
        
        return expenseCal.get(Calendar.DAY_OF_MONTH) == day &&
               expenseCal.get(Calendar.MONTH) + 1 == month &&
               expenseCal.get(Calendar.YEAR) == year;
    }
    
    private void showEveningNotificationWithAmount(double totalSpending) {
        String title = "Good Night!";
        String message = String.format("About to hit the bed? Today you have spent $%.2f, want to take a look?", totalSpending);
        
        showNotification(title, message, EVENING_NOTIFICATION_ID);
    }
    
    private void showNotification(String title, String message, int notificationId) {
        Intent intent = new Intent(context, OverviewActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 
            0, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.wallet)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent);
        
        notificationManager.notify(notificationId, builder.build());
    }
} 