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

public class LoginActivity extends AppCompatActivity {

    EditText usernameInput, passwordInput;
    Button loginButton;
    TextView goToRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameInput = findViewById(R.id.input_username);
        passwordInput = findViewById(R.id.input_password);
        loginButton = findViewById(R.id.button_login);
        goToRegister = findViewById(R.id.text_register);

        loginButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both fields", Toast.LENGTH_SHORT).show();
                return;
            }

            Executors.newSingleThreadExecutor().execute(() -> {
                Account user = DatabaseClient.getInstance(getApplicationContext())
                        .getAppDatabase()
                        .accountDao()
                        .findByUsername(username);

                runOnUiThread(() -> {
                    if (user == null || !user.password.equals(password)) {
                        Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.putExtra("accountId", user.id); // if needed later
                        startActivity(intent);
                        finish();
                    }
                });
            });
        });

        goToRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        });
    }
}
