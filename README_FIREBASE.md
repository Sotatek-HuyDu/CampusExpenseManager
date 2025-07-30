# Firebase Setup for Campus Expense Manager

## Overview
This project uses Firebase Firestore as the primary database for cloud-based data storage and synchronization.

## What's Included

### 🔥 Firebase Components
- **Firebase Models**: `FirebaseAccount`, `FirebaseExpense`, `FirebaseBudget`, `FirebaseRecurringExpense`
- **Repository Pattern**: Separate repositories for each data type
- **Firebase Manager**: Centralized access to all Firebase operations
- **Usage Examples**: `FirebaseUsageExample` class with common operations

### 📱 Key Features
- **Asynchronous Operations**: All database operations use callbacks
- **Real-time Updates**: Firestore provides live data synchronization
- **Offline Support**: Works offline and syncs when connection is restored
- **Cloud-based**: No local database management required

## Quick Start

### 1. Firebase Project Setup
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project
3. Enable Firestore Database
4. Download `google-services.json` and place it in the `app/` directory

### 2. Using Firebase in Your Code

```java
// Initialize Firebase usage
FirebaseUsageExample firebaseUsage = new FirebaseUsageExample(this);

// Create account
firebaseUsage.createAccount("username", "password");

// Login
firebaseUsage.loginUser("username", "password", new FirebaseUsageExample.LoginCallback() {
    @Override
    public void onLoginSuccess() {
        // Navigate to main screen
    }
    
    @Override
    public void onLoginFailure(String error) {
        // Show error
    }
});

// Add expense
firebaseUsage.addExpense("user123", "Coffee", 5.50, "Food");

// Get expenses
firebaseUsage.getExpenses("user123", new FirebaseUsageExample.ExpensesCallback() {
    @Override
    public void onExpensesLoaded(QuerySnapshot expenses) {
        // Update UI
    }
    
    @Override
    public void onExpensesError(String error) {
        // Handle error
    }
});
```

### 3. Direct Repository Access

```java
FirebaseManager firebaseManager = FirebaseManager.getInstance();

// Add expense directly
FirebaseExpense expense = new FirebaseExpense("user123", "Lunch", 12.00, "Food");
firebaseManager.getExpenseRepository().insert(expense)
    .addOnSuccessListener(aVoid -> {
        // Success
    })
    .addOnFailureListener(e -> {
        // Handle error
    });
```

## File Structure

```
app/src/main/java/com/budgetwise/campusexpensemanager/firebase/
├── models/
│   ├── FirebaseAccount.java
│   ├── FirebaseExpense.java
│   ├── FirebaseBudget.java
│   └── FirebaseRecurringExpense.java
├── repository/
│   ├── FirebaseRepository.java
│   ├── AccountRepository.java
│   ├── ExpenseRepository.java
│   ├── BudgetRepository.java
│   └── RecurringExpenseRepository.java
└── FirebaseManager.java
```

## Security Rules

Set up Firestore security rules in Firebase Console:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

## Benefits

- ✅ **Scalable**: Cloud-based solution that grows with your app
- ✅ **Real-time**: Live data synchronization across devices
- ✅ **Offline**: Works without internet connection
- ✅ **Secure**: Built-in authentication and security rules
- ✅ **Maintenance-free**: No local database management

## Next Steps

1. Set up your Firebase project and add `google-services.json`
2. Test the implementation
3. Verify Firebase integration is working correctly

For detailed setup instructions, see `FIREBASE_SETUP_GUIDE.md`. 