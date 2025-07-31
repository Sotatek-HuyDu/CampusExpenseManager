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
import com.budgetwise.campusexpensemanager.utils.PasswordHasher;

public class RegisterActivity extends AppCompatActivity {

    EditText usernameInput, emailInput, passwordInput, confirmPasswordInput;
    Button registerButton;
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        usernameInput = findViewById(R.id.input_username);
        emailInput = findViewById(R.id.input_email);
        passwordInput = findViewById(R.id.input_password);
        confirmPasswordInput = findViewById(R.id.input_confirm_password);
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
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String confirmPassword = confirmPasswordInput.getText().toString().trim();

            // Clear previous errors
            usernameInput.setError(null);
            emailInput.setError(null);
            passwordInput.setError(null);
            confirmPasswordInput.setError(null);

            // Validation
            if (!validateRegistrationInputs(username, email, password, confirmPassword)) {
                return;
            }

            // Add loading indicator
            registerButton.setEnabled(false);
            registerButton.setText("Checking Availability...");

            checkUsernameAndEmailAvailability(username, email, password);
        });
    }
    
    private boolean validateRegistrationInputs(String username, String email, String password, String confirmPassword) {
        // Check if all fields are filled
        if (username.isEmpty()) {
            usernameInput.setError("Username is required");
            return false;
        }
        
        if (email.isEmpty()) {
            emailInput.setError("Email is required");
            return false;
        }
        
        if (password.isEmpty()) {
            passwordInput.setError("Password is required");
            return false;
        }
        
        if (confirmPassword.isEmpty()) {
            confirmPasswordInput.setError("Please confirm your password");
            return false;
        }
        
        // Check minimum length requirements
        if (username.length() < 6) {
            usernameInput.setError("Username must be at least 6 characters");
            return false;
        }
        
        if (password.length() < 6) {
            passwordInput.setError("Password must be at least 6 characters");
            return false;
        }
        
        // Check email format
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Please enter a valid email address");
            return false;
        }
        
        // Check if passwords match
        if (!password.equals(confirmPassword)) {
            confirmPasswordInput.setError("Passwords do not match");
            return false;
        }
        
        return true;
    }
    
    private void checkUsernameAndEmailAvailability(String username, String email, String password) {
        // Add timeout handler
        android.os.Handler timeoutHandler = new android.os.Handler();
        Runnable timeoutRunnable = () -> {
            registerButton.setEnabled(true);
            registerButton.setText("Register");
            Toast.makeText(this, "Account creation timed out. Please check your internet connection.", Toast.LENGTH_LONG).show();
        };
        
        // Set 10 second timeout
        timeoutHandler.postDelayed(timeoutRunnable, 10000);
        
        // Use Singapore region for Firebase
        com.google.firebase.database.FirebaseDatabase database = com.google.firebase.database.FirebaseDatabase.getInstance("https://campus-expense-manager-c16e3-default-rtdb.asia-southeast1.firebasedatabase.app");
        
        // Check both username and email availability
        database.getReference("accounts").orderByChild("username").equalTo(username)
            .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                @Override
                public void onDataChange(com.google.firebase.database.DataSnapshot usernameSnapshot) {
                    if (usernameSnapshot.exists()) {
                        // Username already exists
                        timeoutHandler.removeCallbacks(timeoutRunnable);
                        registerButton.setEnabled(true);
                        registerButton.setText("Register");
                        usernameInput.setError("Username already exists");
                        Toast.makeText(RegisterActivity.this, "Username already exists. Please choose a different username.", Toast.LENGTH_SHORT).show();
                    } else {
                        // Username is unique, now check email
                        checkEmailAvailability(email, username, password, timeoutHandler);
                    }
                }
                
                @Override
                public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
                    timeoutHandler.removeCallbacks(timeoutRunnable);
                    registerButton.setEnabled(true);
                    registerButton.setText("Register");
                    Toast.makeText(RegisterActivity.this, "Error checking username: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }
    
    private void checkEmailAvailability(String email, String username, String password, android.os.Handler timeoutHandler) {
        com.google.firebase.database.FirebaseDatabase database = com.google.firebase.database.FirebaseDatabase.getInstance("https://campus-expense-manager-c16e3-default-rtdb.asia-southeast1.firebasedatabase.app");
        
        database.getReference("accounts").orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                @Override
                public void onDataChange(com.google.firebase.database.DataSnapshot emailSnapshot) {
                    if (emailSnapshot.exists()) {
                        // Email already exists
                        registerButton.setEnabled(true);
                        registerButton.setText("Register");
                        emailInput.setError("Email already exists");
                        Toast.makeText(RegisterActivity.this, "Email already exists. Please use a different email.", Toast.LENGTH_SHORT).show();
                    } else {
                        // Both username and email are unique, proceed to create account
                        registerButton.setText("Creating Account...");
                        createNewAccountWithUniqueKey(username, email, password, timeoutHandler);
                    }
                }
                
                @Override
                public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
                    registerButton.setEnabled(true);
                    registerButton.setText("Register");
                    Toast.makeText(RegisterActivity.this, "Error checking email: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }
    
    private void createNewAccountWithUniqueKey(String username, String email, String password, android.os.Handler timeoutHandler) {
        // Hash the password
        String hashedPassword = PasswordHasher.hashPassword(password);
        
        // Create account data
        java.util.Map<String, Object> accountData = new java.util.HashMap<>();
        accountData.put("username", username);
        accountData.put("email", email);
        accountData.put("password", hashedPassword);
        accountData.put("createdAt", System.currentTimeMillis());
        
        // Use Singapore region for Firebase
        com.google.firebase.database.FirebaseDatabase database = com.google.firebase.database.FirebaseDatabase.getInstance("https://campus-expense-manager-c16e3-default-rtdb.asia-southeast1.firebasedatabase.app");
        
        // Create account in Firebase using push() to generate unique key
        database.getReference("accounts").push().setValue(accountData)
            .addOnSuccessListener(aVoid -> {
                registerButton.setEnabled(true);
                registerButton.setText("Register");
                Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            })
            .addOnFailureListener(e -> {
                registerButton.setEnabled(true);
                registerButton.setText("Register");
                Toast.makeText(this, "Failed to create account: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

}
