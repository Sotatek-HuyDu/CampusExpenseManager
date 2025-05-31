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

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        Executors.newSingleThreadExecutor().execute(() -> {
            // ✅ Log accounts for testing
            List<Account> accounts = DatabaseClient.getInstance(getApplicationContext())
                    .getAppDatabase()
                    .accountDao()
                    .getAllAccounts();

            for (Account acc : accounts) {
                Log.d("ACCOUNT_CHECK", "ID: " + acc.id + ", Username: " + acc.username);
            }

            // ✅ WAL checkpoint using Room-safe rawQuery
            SupportSQLiteQuery query = new SimpleSQLiteQuery("PRAGMA wal_checkpoint(FULL)");
            DatabaseClient.getInstance(getApplicationContext())
                    .getAppDatabase()
                    .rawQueryDao()
                    .performCheckpoint(query);

            Log.d("DB_EXPORT", "WAL checkpoint complete (via rawQuery)");
        });
    }
}
