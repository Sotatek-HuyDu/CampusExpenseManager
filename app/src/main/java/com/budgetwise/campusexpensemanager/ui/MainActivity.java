package com.budgetwise.campusexpensemanager.ui;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.sqlite.db.SimpleSQLiteQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.budgetwise.campusexpensemanager.R;
import com.budgetwise.campusexpensemanager.database.DatabaseClient;
import com.budgetwise.campusexpensemanager.models.Account;

import java.util.List;
import java.util.concurrent.Executors;

import android.content.Intent;
import android.widget.Button;
import android.widget.TextView;
import com.budgetwise.campusexpensemanager.utils.SessionManager;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Session setup
        SessionManager session = new SessionManager(getApplicationContext());
        String username = session.getUsername();

        TextView textUsername = findViewById(R.id.text_username);
        textUsername.setText(username != null ? "Welcome, " + username + "!" : "Welcome!");

        Button logoutButton = findViewById(R.id.btnLogout);
        logoutButton.setOnClickListener(v -> {
            session.logout();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Optional DB logging & WAL checkpoint
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
