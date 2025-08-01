package com.budgetwise.campusexpensemanager.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.widget.EditText;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.budgetwise.campusexpensemanager.R;
import com.budgetwise.campusexpensemanager.utils.SessionManager;
import com.budgetwise.campusexpensemanager.firebase.repository.AccountRepository;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.button.MaterialButton;

public abstract class BaseActivity extends AppCompatActivity {

    protected BottomNavigationView bottomNavigationView;
    protected SessionManager sessionManager;
    protected AccountRepository accountRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceId());
        
        sessionManager = new SessionManager(this);
        accountRepository = new AccountRepository();
        setupBottomNavigation();
        setupActivity();
        
        // Load email from Firebase and then setup drawer
        loadEmailFromFirebaseAndSetupDrawer();
    }

    protected abstract int getLayoutResourceId();
    protected abstract void setupActivity();

    protected void setupToolbar(String title) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(title);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
            }
        }
    }

    protected void setupDrawer() {
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        View drawerContent = findViewById(R.id.right_drawer);
        
        if (drawerLayout != null && drawerContent != null) {
            setupDrawerContent(drawerLayout, drawerContent);
        }
    }
    
    private void setupDrawerContent(DrawerLayout drawerLayout, View drawerContent) {
        // Set personalized greeting
        TextView greetingText = drawerContent.findViewById(R.id.header_greeting);
        if (greetingText != null) {
            String username = sessionManager.getUsername();
            if (username != null && !username.isEmpty()) {
                greetingText.setText("Hello " + username + "!");
            } else {
                greetingText.setText("Hello User!");
            }
        }
        
        // Set email display
        TextView emailText = drawerContent.findViewById(R.id.header_email);
        if (emailText != null) {
            String userEmail = sessionManager.getUserEmail();
            if (userEmail != null && !userEmail.isEmpty()) {
                emailText.setText(userEmail);
                emailText.setVisibility(View.VISIBLE);
            } else {
                emailText.setText("You haven't added email yet");
                emailText.setVisibility(View.VISIBLE);
            }
        }
        
        // Set up logout button
        MaterialButton logoutButton = drawerContent.findViewById(R.id.logout_button);
        if (logoutButton != null) {
            logoutButton.setOnClickListener(v -> {
                // Cancel daily notifications before logout
                com.budgetwise.campusexpensemanager.notifications.DailyNotificationService dailyNotificationService = 
                    new com.budgetwise.campusexpensemanager.notifications.DailyNotificationService(this);
                dailyNotificationService.cancelDailyNotifications();
                
                sessionManager.logout();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
        
        // Set up change email button
        MaterialButton changeEmailButton = drawerContent.findViewById(R.id.change_email_button);
        if (changeEmailButton != null) {
            // Update button text based on email status
            updateEmailButtonText(changeEmailButton);
            
            changeEmailButton.setOnClickListener(v -> {
                showAddEmailDialog();
                drawerLayout.closeDrawer(GravityCompat.END);
            });
        }
        
        // Set up reset password button
        MaterialButton resetPasswordButton = drawerContent.findViewById(R.id.reset_password_button);
        if (resetPasswordButton != null) {
            resetPasswordButton.setOnClickListener(v -> {
                showPasswordResetDialog();
                drawerLayout.closeDrawer(GravityCompat.END);
            });
        }
        
        // Set up give feedback button
        MaterialButton giveFeedbackButton = drawerContent.findViewById(R.id.give_feedback_button);
        if (giveFeedbackButton != null) {
            giveFeedbackButton.setOnClickListener(v -> {
                showFeedbackDialog();
                drawerLayout.closeDrawer(GravityCompat.END);
            });
        }
        
        // Set up navigation item selected listener for future menu items
        NavigationView navigationView = drawerContent.findViewById(R.id.navigation_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(item -> {
                int id = item.getItemId();
                // Handle future menu items here
                drawerLayout.closeDrawer(GravityCompat.END);
                return true;
            });
        }
    }
    
    private void updateEmailButtonText(MaterialButton changeEmailButton) {
        String userEmail = sessionManager.getUserEmail();
        boolean hasEmail = userEmail != null && !userEmail.isEmpty();
        
        // Update the button text
        changeEmailButton.setText(hasEmail ? "Change Email" : "Add Email");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
            if (drawerLayout != null) {
                // Activity has drawer layout - open drawer (menu button behavior)
                drawerLayout.openDrawer(GravityCompat.END);
                return true;
            } else {
                // Activity doesn't have drawer layout - go back (back button behavior)
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_nav);
        if (bottomNavigationView != null) {
            bottomNavigationView.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                
                if (id == R.id.nav_overview) {
                    if (!(this instanceof OverviewActivity)) {
                        Intent intent = new Intent(this, OverviewActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                    return true;
                } else if (id == R.id.nav_expenses) {
                    if (!(this instanceof ExpenseActivity)) {
                        Intent intent = new Intent(this, ExpenseActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    return true;
                } else if (id == R.id.nav_recurring) {
                    if (!(this instanceof RecurringExpenseActivity)) {
                        Intent intent = new Intent(this, RecurringExpenseActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    return true;
                } else if (id == R.id.nav_budget) {
                    if (!(this instanceof BudgetActivity)) {
                        Intent intent = new Intent(this, BudgetActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    return true;
                } else if (id == R.id.nav_report) {
                    if (!(this instanceof ReportActivity)) {
                        Intent intent = new Intent(this, ReportActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    return true;
                }
                return false;
            });
            
            // Set the correct item as selected based on current activity
            setSelectedNavigationItem();
        }
    }

    private void setSelectedNavigationItem() {
        if (this instanceof OverviewActivity) {
            bottomNavigationView.setSelectedItemId(R.id.nav_overview);
        } else if (this instanceof ExpenseActivity) {
            bottomNavigationView.setSelectedItemId(R.id.nav_expenses);
        } else if (this instanceof RecurringExpenseActivity) {
            bottomNavigationView.setSelectedItemId(R.id.nav_recurring);
        } else if (this instanceof BudgetActivity) {
            bottomNavigationView.setSelectedItemId(R.id.nav_budget);
        } else if (this instanceof ReportActivity) {
            bottomNavigationView.setSelectedItemId(R.id.nav_report);
        }
    }
    
    private void showAddEmailDialog() {
        // Create custom dialog layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_email_input, null);
        
        // Get references to views
        TextView dialogTitle = dialogView.findViewById(R.id.dialog_title);
        TextView dialogSubtitle = dialogView.findViewById(R.id.dialog_subtitle);
        com.google.android.material.textfield.TextInputEditText emailInput = dialogView.findViewById(R.id.email_input);
        com.google.android.material.button.MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel);
        com.google.android.material.button.MaterialButton btnSave = dialogView.findViewById(R.id.btn_save);
        
        // Check if user already has email
        String currentEmail = sessionManager.getUserEmail();
        boolean hasEmail = currentEmail != null && !currentEmail.isEmpty();
        
        // Update dialog content based on email status
        if (hasEmail) {
            dialogTitle.setText("Change Email Address");
            dialogSubtitle.setText("Update your email address for notifications and updates");
            emailInput.setText(currentEmail);
            btnSave.setText("Update");
        } else {
            dialogTitle.setText("Add Email Address");
            dialogSubtitle.setText("Enter your email address to receive notifications and updates");
            btnSave.setText("Save");
        }
        
        // Create dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create();
        
        // Set button click listeners
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        btnSave.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            
            // Validation
            if (TextUtils.isEmpty(email)) {
                emailInput.setError("Please enter a valid email address");
                return;
            }
            
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailInput.setError("Please enter a valid email format");
                return;
            }
            
            // Clear any previous errors
            emailInput.setError(null);
            
            // Check if email is already in use by another user
            String accountId = sessionManager.getAccountId();
            if (accountId != null) {
                // Show loading state
                btnSave.setEnabled(false);
                btnSave.setText("Checking...");
                
                accountRepository.isEmailUnique(email, accountId)
                    .addOnSuccessListener(isUnique -> {
                        if (isUnique) {
                            // Email is unique, proceed to save
                            saveEmailToFirebase(email, hasEmail, dialog);
                        } else {
                            // Email already exists
                            emailInput.setError("This email is already registered by another user");
                            btnSave.setEnabled(true);
                            btnSave.setText(hasEmail ? "Update" : "Save");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error checking email: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        btnSave.setEnabled(true);
                        btnSave.setText(hasEmail ? "Update" : "Save");
                    });
            } else {
                // No account ID, save locally only
                sessionManager.saveUserEmail(email);
                Toast.makeText(this, "Email saved locally only", Toast.LENGTH_SHORT).show();
                refreshDrawerHeader();
                dialog.dismiss();
            }
        });
        
        dialog.show();
    }
    
    private void saveEmailToFirebase(String email, boolean hasEmail, AlertDialog dialog) {
        // Save email to both SharedPreferences and Firebase
        sessionManager.saveUserEmail(email);
        
        // Save to Firebase
        String accountId = sessionManager.getAccountId();
        if (accountId != null) {
            accountRepository.updateEmail(accountId, email)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, hasEmail ? "Email updated successfully!" : "Email saved successfully!", Toast.LENGTH_SHORT).show();
                    refreshDrawerHeader();
                    dialog.dismiss();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save email to cloud: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    refreshDrawerHeader();
                });
        } else {
            Toast.makeText(this, "Email saved locally only", Toast.LENGTH_SHORT).show();
            refreshDrawerHeader();
            dialog.dismiss();
        }
    }
    
    private void refreshDrawerHeader() {
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        View drawerContent = findViewById(R.id.right_drawer);
        
        if (drawerLayout != null && drawerContent != null) {
            TextView emailText = drawerContent.findViewById(R.id.header_email);
            if (emailText != null) {
                String userEmail = sessionManager.getUserEmail();
                if (userEmail != null && !userEmail.isEmpty()) {
                    emailText.setText(userEmail);
                    emailText.setVisibility(View.VISIBLE);
                } else {
                    emailText.setText("You haven't added email yet");
                    emailText.setVisibility(View.VISIBLE);
                }
            }
            
            // Update change email button text
            MaterialButton changeEmailButton = drawerContent.findViewById(R.id.change_email_button);
            if (changeEmailButton != null) {
                updateEmailButtonText(changeEmailButton);
            }
        }
    }
    
    private void loadEmailFromFirebaseAndSetupDrawer() {
        String accountId = sessionManager.getAccountId();
        
        // Always try to load from Firebase if we have an account ID
        if (accountId != null) {
            accountRepository.getAccountById(accountId)
                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                        try {
                            if (dataSnapshot.exists()) {
                                for (com.google.firebase.database.DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    try {
                                        com.budgetwise.campusexpensemanager.firebase.models.FirebaseAccount account = 
                                            snapshot.getValue(com.budgetwise.campusexpensemanager.firebase.models.FirebaseAccount.class);
                                        if (account != null && account.getEmail() != null && !account.getEmail().isEmpty()) {
                                            sessionManager.saveUserEmail(account.getEmail());
                                        }
                                        break; // Found the account, no need to continue
                                    } catch (Exception e) {
                                        // Log the error but continue processing
                                        android.util.Log.e("BaseActivity", "Error parsing account data: " + e.getMessage());
                                        continue;
                                    }
                                }
                            }
                        } catch (Exception e) {
                            // Log the error but don't crash the app
                            android.util.Log.e("BaseActivity", "Error loading email from Firebase: " + e.getMessage());
                        }
                        
                        // Always setup drawer after Firebase operation (success or failure)
                        runOnUiThread(() -> {
                            setupDrawer();
                            refreshDrawerHeader();
                        });
                    }
                    
                    @Override
                    public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
                        // Silently fail - email will remain local only
                        android.util.Log.w("BaseActivity", "Firebase query cancelled: " + databaseError.getMessage());
                        
                        // Always setup drawer after Firebase operation (success or failure)
                        runOnUiThread(() -> {
                            setupDrawer();
                            refreshDrawerHeader();
                        });
                    }
                });
        } else {
            // No account ID, setup drawer immediately
            setupDrawer();
            refreshDrawerHeader();
        }
    }
    
    private void loadEmailFromFirebase() {
        String accountId = sessionManager.getAccountId();
        
        // Always try to load from Firebase if we have an account ID
        if (accountId != null) {
            accountRepository.getAccountById(accountId)
                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                        try {
                            if (dataSnapshot.exists()) {
                                for (com.google.firebase.database.DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    try {
                                        com.budgetwise.campusexpensemanager.firebase.models.FirebaseAccount account = 
                                            snapshot.getValue(com.budgetwise.campusexpensemanager.firebase.models.FirebaseAccount.class);
                                        if (account != null && account.getEmail() != null && !account.getEmail().isEmpty()) {
                                            sessionManager.saveUserEmail(account.getEmail());
                                            refreshDrawerHeader();
                                            break;
                                        } else {
                                            // If no email found, still refresh drawer to ensure proper state
                                            refreshDrawerHeader();
                                        }
                                    } catch (Exception e) {
                                        // Log the error but continue processing
                                        android.util.Log.e("BaseActivity", "Error parsing account data: " + e.getMessage());
                                        continue;
                                    }
                                }
                            } else {
                                // No data found, refresh drawer to ensure proper state
                                refreshDrawerHeader();
                            }
                        } catch (Exception e) {
                            // Log the error but don't crash the app
                            android.util.Log.e("BaseActivity", "Error loading email from Firebase: " + e.getMessage());
                            // Still refresh drawer even on error
                            refreshDrawerHeader();
                        }
                    }
                    
                    @Override
                    public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
                        // Silently fail - email will remain local only
                        android.util.Log.w("BaseActivity", "Firebase query cancelled: " + databaseError.getMessage());
                        // Still refresh drawer even on cancellation
                        refreshDrawerHeader();
                    }
                });
        }
    }
    
    private String generatedResetCode = null;
    
    private void showPasswordResetDialog() {
        String userEmail = sessionManager.getUserEmail();
        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(this, "Please add an email address first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Generate a 4-digit code
        generatedResetCode = String.format("%04d", (int)(Math.random() * 10000));
        
        // Create custom dialog layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_password_reset, null);
        
        // Get references to views
        com.google.android.material.textfield.TextInputEditText codeInput = dialogView.findViewById(R.id.code_input);
        com.google.android.material.button.MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel);
        com.google.android.material.button.MaterialButton btnValidate = dialogView.findViewById(R.id.btn_validate);
        
        // Create dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create();
        
        // Set button click listeners
        btnCancel.setOnClickListener(v -> {
            generatedResetCode = null;
            dialog.dismiss();
        });
        
        btnValidate.setOnClickListener(v -> {
            String code = codeInput.getText().toString().trim();
            
            if (TextUtils.isEmpty(code)) {
                codeInput.setError("Please enter the code");
                return;
            }
            
            if (code.length() != 4) {
                codeInput.setError("Code must be 4 digits");
                return;
            }
            
            validateResetCode(code, dialog);
        });
        
        dialog.show();
        
        // Send reset email
        sendResetEmail(userEmail, generatedResetCode);
    }
    

    
    private void validateResetCode(String code, AlertDialog currentDialog) {
        if (generatedResetCode != null && code.equals(generatedResetCode)) {
            generatedResetCode = null;
            currentDialog.dismiss();
            showNewPasswordDialog();
        } else {
            Toast.makeText(this, "Invalid code. Please check your email and try again.", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showNewPasswordDialog() {
        // Create custom dialog layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_new_password, null);
        
        // Get references to views
        com.google.android.material.textfield.TextInputEditText newPasswordInput = dialogView.findViewById(R.id.new_password_input);
        com.google.android.material.textfield.TextInputEditText confirmPasswordInput = dialogView.findViewById(R.id.confirm_password_input);
        com.google.android.material.button.MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel);
        com.google.android.material.button.MaterialButton btnSave = dialogView.findViewById(R.id.btn_save);
        
        // Create dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create();
        
        // Set button click listeners
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        btnSave.setOnClickListener(v -> {
            String newPassword = newPasswordInput.getText().toString().trim();
            String confirmPassword = confirmPasswordInput.getText().toString().trim();
            
            if (TextUtils.isEmpty(newPassword)) {
                newPasswordInput.setError("Please enter a new password");
                return;
            }
            
            if (newPassword.length() < 6) {
                newPasswordInput.setError("Password must be at least 6 characters");
                return;
            }
            
            if (!newPassword.equals(confirmPassword)) {
                confirmPasswordInput.setError("Passwords do not match");
                return;
            }
            
            // Update password
            updatePassword(newPassword, dialog);
        });
        
        dialog.show();
    }
    
    private void updatePassword(String newPassword, AlertDialog dialog) {
        String accountId = sessionManager.getAccountId();
        if (accountId == null) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            return;
        }
        
        // Check if new password is same as current password
        checkCurrentPasswordAndUpdate(newPassword, dialog);
    }
    
    private void checkCurrentPasswordAndUpdate(String newPassword, AlertDialog dialog) {
        String accountId = sessionManager.getAccountId();
        
        // Get current password from Firebase
        com.google.firebase.database.FirebaseDatabase database = com.google.firebase.database.FirebaseDatabase.getInstance("https://campus-expense-manager-c16e3-default-rtdb.asia-southeast1.firebasedatabase.app");
        database.getReference("accounts").child(accountId).child("password").get()
            .addOnSuccessListener(dataSnapshot -> {
                String currentHashedPassword = dataSnapshot.getValue(String.class);
                
                if (currentHashedPassword != null) {
                    // Hash the new password to compare
                    String newHashedPassword = com.budgetwise.campusexpensemanager.utils.PasswordHasher.hashPassword(newPassword);
                    
                    // Check if passwords are the same
                    if (newHashedPassword.equals(currentHashedPassword)) {
                        Toast.makeText(this, "New password cannot be the same as current password", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                
                // Passwords are different, proceed with update
                performPasswordUpdate(newPassword, dialog);
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error checking current password: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
    
    private void performPasswordUpdate(String newPassword, AlertDialog dialog) {
        String accountId = sessionManager.getAccountId();
        
        // Hash the new password
        String hashedPassword = com.budgetwise.campusexpensemanager.utils.PasswordHasher.hashPassword(newPassword);
        
        // Update password in Firebase database
        com.google.firebase.database.FirebaseDatabase database = com.google.firebase.database.FirebaseDatabase.getInstance("https://campus-expense-manager-c16e3-default-rtdb.asia-southeast1.firebasedatabase.app");
        database.getReference("accounts").child(accountId).child("password").setValue(hashedPassword)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Password updated successfully!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to update password: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }
    
    private void sendResetEmail(String userEmail, String resetCode) {
        // Show loading message
        Toast.makeText(this, "Sending reset code to your email...", Toast.LENGTH_SHORT).show();
        
        // Send email using EmailService
        com.budgetwise.campusexpensemanager.utils.EmailService.sendPasswordResetEmail(this, userEmail, resetCode, 
            new com.budgetwise.campusexpensemanager.utils.EmailService.EmailCallback() {
                @Override
                public void onSuccess() {
                    runOnUiThread(() -> {
                        Toast.makeText(BaseActivity.this, "Reset code sent to " + userEmail, Toast.LENGTH_LONG).show();
                    });
                }
                
                @Override
                public void onFailure(String error) {
                    runOnUiThread(() -> {
                        Toast.makeText(BaseActivity.this, "Failed to send email: " + error, Toast.LENGTH_LONG).show();
                        // Fallback: show code in toast for testing
                        Toast.makeText(BaseActivity.this, "Reset code: " + resetCode, Toast.LENGTH_LONG).show();
                    });
                }
            });
    }
    
    private void showFeedbackDialog() {
        // Create custom dialog layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_feedback, null);
        
        // Get references to views
        android.widget.RatingBar ratingOverall = dialogView.findViewById(R.id.rating_overall);
        android.widget.RatingBar ratingDesign = dialogView.findViewById(R.id.rating_design);
        android.widget.RatingBar ratingNavigation = dialogView.findViewById(R.id.rating_navigation);
        android.widget.RatingBar ratingFunctionality = dialogView.findViewById(R.id.rating_functionality);
        android.widget.RatingBar ratingRecommendation = dialogView.findViewById(R.id.rating_recommendation);
        com.google.android.material.textfield.TextInputEditText feedbackInput = dialogView.findViewById(R.id.feedback_input);
        com.google.android.material.button.MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel);
        com.google.android.material.button.MaterialButton btnSubmit = dialogView.findViewById(R.id.btn_submit);
        
        // Create dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create();
        
        // Set button click listeners
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        btnSubmit.setOnClickListener(v -> {
            // Collect all ratings
            float overallRating = ratingOverall.getRating();
            float designRating = ratingDesign.getRating();
            float navigationRating = ratingNavigation.getRating();
            float functionalityRating = ratingFunctionality.getRating();
            float recommendationRating = ratingRecommendation.getRating();
            String feedback = feedbackInput.getText().toString().trim();
            
            // Show loading state
            btnSubmit.setEnabled(false);
            btnSubmit.setText("Sending...");
            
            // Send feedback email
            sendFeedbackEmail(overallRating, designRating, navigationRating, functionalityRating, recommendationRating, feedback, dialog, btnSubmit);
        });
        
        dialog.show();
    }
    
    private void sendFeedbackEmail(float ratingOverall, float ratingDesign, float ratingNavigation, float ratingFunctionality, float ratingRecommendation, String feedback, AlertDialog dialog, com.google.android.material.button.MaterialButton btnSubmit) {
        String userEmail = sessionManager.getUserEmail();
        String userName = sessionManager.getUsername();
        if (userEmail == null || userEmail.isEmpty()) {
            userEmail = "anonymous@user.com";
        }
        if (userName == null || userName.isEmpty()) {
            userName = "Anonymous User";
        }
        
        // Show loading message
        Toast.makeText(this, "Sending feedback...", Toast.LENGTH_SHORT).show();
        
        // Send feedback using EmailService
        com.budgetwise.campusexpensemanager.utils.EmailService.sendFeedbackEmail(this, userEmail, userName, ratingOverall, ratingDesign, ratingNavigation, ratingFunctionality, ratingRecommendation, feedback, 
            new com.budgetwise.campusexpensemanager.utils.EmailService.EmailCallback() {
                @Override
                public void onSuccess() {
                    runOnUiThread(() -> {
                        Toast.makeText(BaseActivity.this, "Thank you for your feedback!", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    });
                }
                
                @Override
                public void onFailure(String error) {
                    runOnUiThread(() -> {
                        Toast.makeText(BaseActivity.this, "Failed to send feedback: " + error, Toast.LENGTH_LONG).show();
                        btnSubmit.setEnabled(true);
                        btnSubmit.setText("Submit");
                    });
                }
            });
    }
    

} 