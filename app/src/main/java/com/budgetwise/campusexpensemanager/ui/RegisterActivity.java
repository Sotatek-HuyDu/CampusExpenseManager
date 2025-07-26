package com.budgetwise.campusexpensemanager.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.budgetwise.campusexpensemanager.R;
import com.budgetwise.campusexpensemanager.firebase.FirebaseManager;
import com.budgetwise.campusexpensemanager.firebase.models.FirebaseAccount;

public class RegisterActivity extends AppCompatActivity {

    EditText usernameInput, passwordInput;
    Button registerButton;
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        usernameInput = findViewById(R.id.input_username);
        passwordInput = findViewById(R.id.input_password);
        registerButton = findViewById(R.id.button_register);
        
        // Initialize Firebase manager
        firebaseManager = FirebaseManager.getInstance();
        
        // Enable anonymous authentication
        com.google.firebase.auth.FirebaseAuth.getInstance().signInAnonymously()
            .addOnSuccessListener(authResult -> {
                android.util.Log.d("RegisterActivity", "Firebase auth ready");
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("RegisterActivity", "Firebase auth failed: " + e.getMessage());
            });
        

        

        


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

            // Add loading indicator
            registerButton.setEnabled(false);
            registerButton.setText("Creating Account...");

            createNewAccountDirect(username, password);
        });
    }
    

    
        private void createNewAccountDirect(String username, String password) {
        // Add timeout handler
        android.os.Handler timeoutHandler = new android.os.Handler();
        Runnable timeoutRunnable = () -> {
            registerButton.setEnabled(true);
            registerButton.setText("Register");
            Toast.makeText(this, "Account creation timed out. Please check your internet connection.", Toast.LENGTH_LONG).show();
        };
        
        // Set 10 second timeout
        timeoutHandler.postDelayed(timeoutRunnable, 10000);
        
        // Create account data
        java.util.Map<String, Object> accountData = new java.util.HashMap<>();
        accountData.put("username", username);
        accountData.put("password", password);
        accountData.put("createdAt", System.currentTimeMillis());
        
        // Use Singapore region for Firebase
        com.google.firebase.database.FirebaseDatabase database = com.google.firebase.database.FirebaseDatabase.getInstance("https://campus-expense-manager-c16e3-default-rtdb.asia-southeast1.firebasedatabase.app");
        
        // Create account in Firebase
        database.getReference("accounts").child(username).setValue(accountData)
            .addOnSuccessListener(aVoid -> {
                timeoutHandler.removeCallbacks(timeoutRunnable);
                registerButton.setEnabled(true);
                registerButton.setText("Register");
                Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            })
            .addOnFailureListener(e -> {
                timeoutHandler.removeCallbacks(timeoutRunnable);
                registerButton.setEnabled(true);
                registerButton.setText("Register");
                Toast.makeText(this, "Failed to create account: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

}
