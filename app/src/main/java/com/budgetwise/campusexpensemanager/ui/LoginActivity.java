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
import com.budgetwise.campusexpensemanager.utils.SessionManager;

public class LoginActivity extends AppCompatActivity {

    EditText usernameInput, passwordInput;
    Button loginButton;
    TextView goToRegister;
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameInput = findViewById(R.id.input_username);
        passwordInput = findViewById(R.id.input_password);
        loginButton = findViewById(R.id.button_login);
        goToRegister = findViewById(R.id.text_register);
        
        // Initialize Firebase manager
        firebaseManager = FirebaseManager.getInstance();
        
        // Enable anonymous authentication
        com.google.firebase.auth.FirebaseAuth.getInstance().signInAnonymously()
            .addOnSuccessListener(authResult -> {
                android.util.Log.d("LoginActivity", "Firebase auth ready");
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("LoginActivity", "Firebase auth failed: " + e.getMessage());
            });

        // Auto-login if session exists
        SessionManager session = new SessionManager(getApplicationContext());
        if (session.isLoggedIn()) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        loginButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Add loading indicator
            loginButton.setEnabled(false);
            loginButton.setText("Logging in...");

            // Use Singapore region for Firebase
            com.google.firebase.database.FirebaseDatabase database = com.google.firebase.database.FirebaseDatabase.getInstance("https://campus-expense-manager-c16e3-default-rtdb.asia-southeast1.firebasedatabase.app");
            
            // Query for the specific username
            database.getReference("accounts").orderByChild("username").equalTo(username)
                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                        
                        if (dataSnapshot.exists()) {
                            // Check if any account matches both username and password
                            for (com.google.firebase.database.DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                java.util.Map<String, Object> userData = (java.util.Map<String, Object>) snapshot.getValue();
                                if (userData != null && userData.get("password").equals(password)) {
                                    // Login successful
                                    String accountId = snapshot.getKey(); // Use Firebase key as account ID
                                    session.login(username, accountId);

                                    loginButton.setEnabled(true);
                                    loginButton.setText("Login");
                                    Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    intent.putExtra("accountId", accountId);
                                    startActivity(intent);
                                    finish();
                                    return;
                                }
                            }
                            // Password doesn't match
                            loginButton.setEnabled(true);
                            loginButton.setText("Login");
                            Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                        } else {
                            // No matching user found
                            loginButton.setEnabled(true);
                            loginButton.setText("Login");
                            Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                        }
                    }
                    
                    @Override
                    public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
                        loginButton.setEnabled(true);
                        loginButton.setText("Login");
                        Toast.makeText(LoginActivity.this, "Login failed: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        });

        goToRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            finish();
        });
    }
}
