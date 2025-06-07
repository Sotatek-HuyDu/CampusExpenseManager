package com.budgetwise.campusexpensemanager.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;
import androidx.core.view.GravityCompat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.budgetwise.campusexpensemanager.R;
import com.budgetwise.campusexpensemanager.database.DatabaseClient;
import com.budgetwise.campusexpensemanager.models.Account;
import com.budgetwise.campusexpensemanager.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.util.List;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView rightDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Session setup
        SessionManager session = new SessionManager(getApplicationContext());
        String username = session.getUsername();

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
                session.logout();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });

        // Setup bottom nav
        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                // already here
                return true;
            } else if (id == R.id.nav_placeholder1 || id == R.id.nav_placeholder2) {
                // TODO: handle navigation
                return true;
            }
            return false;
        });

        // DB logging and WAL checkpoint
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
    }
}
