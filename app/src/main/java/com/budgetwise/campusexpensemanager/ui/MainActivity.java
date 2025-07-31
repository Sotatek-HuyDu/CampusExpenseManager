package com.budgetwise.campusexpensemanager.ui;

import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;
import androidx.core.view.GravityCompat;
import com.budgetwise.campusexpensemanager.notifications.RecurringExpenseService;
import com.budgetwise.campusexpensemanager.notifications.DailyNotificationService;

import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import com.budgetwise.campusexpensemanager.R;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends BaseActivity {

    private DrawerLayout drawerLayout;
    private NavigationView rightDrawer;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_dashboard;
    }

    @Override
    protected void setupActivity() {
        // Start daily notification service
        DailyNotificationService dailyNotificationService = new DailyNotificationService(this);
        dailyNotificationService.scheduleDailyNotifications();
        
        // Redirect to OverviewActivity as the main screen
        Intent overviewIntent = new Intent(this, OverviewActivity.class);
        overviewIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(overviewIntent);
        finish();
    }

    private void startRecurringExpenseService() {
        Intent serviceIntent = new Intent(this, RecurringExpenseService.class);
        String accountId = sessionManager.getAccountId();
        if (accountId != null) {
            serviceIntent.putExtra("account_id", accountId);
        }
        startService(serviceIntent);
    }
}
