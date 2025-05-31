package com.budgetwise.campusexpensemanager.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.budgetwise.campusexpensemanager.R;
import com.budgetwise.campusexpensemanager.database.DatabaseClient;
import com.budgetwise.campusexpensemanager.models.Account;

import java.util.concurrent.Executors;

public class RegisterActivity extends AppCompatActivity {

    EditText usernameInput, passwordInput;
    Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        usernameInput = findViewById(R.id.input_username);
        passwordInput = findViewById(R.id.input_password);
        registerButton = findViewById(R.id.button_register);

        TextView goToLogin = findViewById(R.id.text_go_to_login);
        goToLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });



        registerButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both fields", Toast.LENGTH_SHORT).show();
                return;
            }

            Executors.newSingleThreadExecutor().execute(() -> {
                Account existing = DatabaseClient.getInstance(getApplicationContext())
                        .getAppDatabase()
                        .accountDao()
                        .findByUsername(username);

                if (existing != null) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show()
                    );
                    return;
                }

                Account newAccount = new Account();
                newAccount.username = username;
                newAccount.password = password;
                newAccount.createdAt = System.currentTimeMillis();

                DatabaseClient.getInstance(getApplicationContext())
                        .getAppDatabase()
                        .accountDao()
                        .insert(newAccount);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                });

            });

        });


    }

}
