package com.budgetwise.campusexpensemanager.ui;

import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;
import androidx.core.view.GravityCompat;
import com.budgetwise.campusexpensemanager.notifications.RecurringExpenseService;

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

        // Session setup
        String username = sessionManager.getUsername();

        // Setup top toolbar with right-side username
        Toolbar toolbar = findViewById(R.id.top_toolbar);
        setSupportActionBar(toolbar);

        // Add username view dynamically
        if (username == null) username = "User";
        TextView usernameView = new TextView(this);
        usernameView.setText(username);
        usernameView.setTextColor(getResources().getColor(android.R.color.white));
        usernameView.setTextSize(16);
        usernameView.setPadding(0, 0, 32, 0);
        usernameView.setOnClickListener(v -> {
            if (drawerLayout != null && rightDrawer != null) {
                drawerLayout.openDrawer(GravityCompat.END);
            }
        });
        Toolbar.LayoutParams params = new Toolbar.LayoutParams(
                Toolbar.LayoutParams.WRAP_CONTENT,
                Toolbar.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.END;
        toolbar.addView(usernameView, params);

        // Setup drawer & logout
        drawerLayout = findViewById(R.id.drawer_layout);
        rightDrawer = findViewById(R.id.right_drawer);
        rightDrawer.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_logout) {
                sessionManager.logout();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });



        // TODO: Remove Room database operations when fully migrated to Firebase
        // DB logging and WAL checkpoint - temporarily disabled to prevent crashes
        /*
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Account> accounts = DatabaseClient.getInstance(getApplicationContext())
                    .getAppDatabase()
                    .accountDao()
                    .getAllAccounts();

            for (Account acc : accounts) {
                Log.d("ACCOUNT_CHECK", "ID: " + acc.id + ", Username: " + acc.username);
            }

            SupportSQLiteQuery query = new SimpleSQLiteQuery("PRAGMA wal_checkpoint(FULL)");
            DatabaseClient.getInstance(getApplicationContext())
                    .getAppDatabase()
                    .rawQueryDao()
                    .performCheckpoint(query);

            Log.d("DB_EXPORT", "WAL checkpoint complete (via rawQuery)");
        });
        */

        // Start recurring expense service
        startRecurringExpenseService();
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
