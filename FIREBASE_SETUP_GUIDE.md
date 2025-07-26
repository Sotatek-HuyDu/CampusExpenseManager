# Firebase Setup Guide (Fresh Database)

## Prerequisites

1. **Firebase Project Setup**
   - Go to [Firebase Console](https://console.firebase.google.com/)
   - Create a new project or use an existing one
   - Enable Firestore Database
   - Set up Authentication (if needed)

2. **Download google-services.json**
   - In Firebase Console, go to Project Settings
   - Add your Android app with package name: `com.budgetwise.campusexpensemanager`
   - Download the `google-services.json` file
   - Place it in the `app/` directory of your project

## Setup Steps

### 1. Dependencies Added ✅
The following dependencies have been added to your project:
- Firebase BOM (Bill of Materials) for version management
- Firebase Auth for authentication
- Firebase Firestore for database
- Firebase Analytics for tracking
- Google Services plugin

### 2. Firebase Models Created ✅
New Firebase-compatible models have been created:
- `FirebaseAccount` - for user accounts
- `FirebaseExpense` - for expense tracking
- `FirebaseBudget` - for budget management
- `FirebaseRecurringExpense` - for recurring expenses

### 3. Firebase Repositories Created ✅
Repository classes for data operations:
- `AccountRepository` - handles account operations
- `ExpenseRepository` - handles expense operations
- `BudgetRepository` - handles budget operations
- `RecurringExpenseRepository` - handles recurring expense operations

### 4. Firebase Manager Created ✅
Centralized manager class (`FirebaseManager`) provides easy access to all repositories.

### 5. Firebase Integration Complete ✅
Login and Register activities now use Firebase instead of Room database.

## Usage Examples

### Basic Firebase Operations

```java
// Get Firebase manager instance
FirebaseManager firebaseManager = FirebaseManager.getInstance();

// Create a new expense
FirebaseExpense expense = new FirebaseExpense("user123", "Coffee", 5.50, "Food");
firebaseManager.getExpenseRepository().insert(expense)
    .addOnSuccessListener(aVoid -> {
        // Success
    })
    .addOnFailureListener(e -> {
        // Handle error
    });

// Get expenses for a user
firebaseManager.getExpenseRepository().getExpensesByAccount("user123")
    .addOnCompleteListener(task -> {
        if (task.isSuccessful()) {
            QuerySnapshot snapshot = task.getResult();
            // Process results
        }
    });
```

### Using Firebase in Your Activities

The Login and Register activities have been updated to use Firebase:

- **RegisterActivity**: Creates new accounts in Firebase Firestore
- **LoginActivity**: Authenticates users against Firebase data
- **SessionManager**: Stores both username and account ID for Firebase compatibility

The implementation is ready to use - just build and run your app!

## Key Features of Firebase Implementation

### 1. Asynchronous Operations
- All Firebase operations are asynchronous with callbacks
- No need to manage background threads manually

### 2. Real-time Updates
- Firestore provides real-time data synchronization
- Use `addSnapshotListener()` for live updates

### 3. Offline Support
- Firebase works offline by default
- Data syncs when connection is restored

### 4. Scalable Architecture
- Cloud-based solution that scales automatically
- No local database management required

## Security Rules

Set up Firestore security rules in Firebase Console:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Allow users to read/write their own data
    match /accounts/{accountId} {
      allow read, write: if request.auth != null && request.auth.uid == accountId;
    }
    
    match /expenses/{expenseId} {
      allow read, write: if request.auth != null && 
        resource.data.accountId == request.auth.uid;
    }
    
    match /budgets/{budgetId} {
      allow read, write: if request.auth != null && 
        resource.data.accountId == request.auth.uid;
    }
    
    match /recurring_expenses/{recurringExpenseId} {
      allow read, write: if request.auth != null && 
        resource.data.accountId == request.auth.uid;
    }
  }
}
```

## Testing

1. **Test CRUD Operations**: Verify create, read, update, delete operations work
2. **Test Offline Capability**: Firebase Firestore works offline by default
3. **Test Security Rules**: Ensure users can only access their own data
4. **Test Real-time Updates**: Verify data syncs across devices

## Performance Considerations

1. **Indexing**: Firestore automatically creates indexes for simple queries
2. **Pagination**: Use `limit()` and `startAfter()` for large datasets
3. **Real-time Updates**: Use `addSnapshotListener()` for real-time data
4. **Offline Persistence**: Enabled by default, can be configured

## Next Steps

1. **Integrate Firebase**: Replace Room database calls with Firebase operations in your activities
2. **Update UI**: Modify activities to handle asynchronous Firebase operations
3. **Add Real-time Features**: Implement real-time updates using Firestore listeners
4. **Remove Room Dependencies**: Once Firebase is fully integrated, remove Room dependencies

## Troubleshooting

### Common Issues

1. **google-services.json not found**
   - Ensure file is in the `app/` directory
   - Clean and rebuild project

2. **Permission denied errors**
   - Check Firestore security rules
   - Verify authentication is set up correctly

3. **Network connectivity issues**
   - Firebase works offline by default
   - Check internet connectivity for initial setup

4. **Data sync issues**
   - Check internet connectivity
   - Verify Firebase project is properly configured

### Debug Tips

- Enable Firebase Analytics for debugging
- Use Firebase Console to monitor Firestore usage
- Check Logcat for detailed error messages
- Use Firebase Emulator for local testing 