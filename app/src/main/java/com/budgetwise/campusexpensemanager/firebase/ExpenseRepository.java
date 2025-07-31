package com.budgetwise.campusexpensemanager.firebase;

import com.budgetwise.campusexpensemanager.models.Expense;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ExpenseRepository {
    private final DatabaseReference expensesRef;

    public ExpenseRepository() {
        this.expensesRef = FirebaseDatabase.getInstance("https://campus-expense-manager-c16e3-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("expenses");
    }

    public Task<Void> addExpense(Expense expense) {
        String expenseId = expensesRef.push().getKey();
        expense.setId(expenseId);
        
        android.util.Log.d("ExpenseRepository", "Adding expense with ID: " + expenseId);
        android.util.Log.d("ExpenseRepository", "Expense data: " + expense.getDescription() + ", " + expense.getAmount() + ", " + expense.getCategory());
        
        return expensesRef.child(expenseId).setValue(expense)
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        android.util.Log.d("ExpenseRepository", "Expense added successfully to Firebase");
                        expense.setId(expenseId);
                    } else {
                        android.util.Log.e("ExpenseRepository", "Failed to add expense: " + task.getException());
                    }
                    return task.getResult();
                });
    }

    public Task<Void> updateExpense(Expense expense) {
        return expensesRef.child(expense.getId()).setValue(expense);
    }

    public Task<Void> deleteExpense(String expenseId) {
        return expensesRef.child(expenseId).removeValue();
    }

    public void getExpensesByUser(String accountId, ExpenseCallback callback) {
        String username = getUsernameFromAccountId(accountId);
        
        if (username != null) {
            expensesRef.orderByChild("userId").equalTo(username)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            List<Expense> expenses = new ArrayList<>();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Expense expense = snapshot.getValue(Expense.class);
                                if (expense != null && !isRecurringExpense(expense)) {
                                    expense.setId(snapshot.getKey());
                                    expense.setAccountId(accountId);
                                    expenses.add(expense);
                                }
                            }
                            callback.onSuccess(expenses);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            callback.onError(databaseError.toException());
                        }
                    });
        } else {
            expensesRef.orderByChild("accountId").equalTo(accountId)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            List<Expense> expenses = new ArrayList<>();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Expense expense = snapshot.getValue(Expense.class);
                                if (expense != null && !isRecurringExpense(expense)) {
                                    expense.setId(snapshot.getKey());
                                    expenses.add(expense);
                                }
                            }
                            callback.onSuccess(expenses);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            callback.onError(databaseError.toException());
                        }
                    });
        }
    }
    
    private boolean isRecurringExpense(Expense expense) {
        return expense.getDescription() != null && 
               expense.getDescription().startsWith("[RECURRING]");
    }
    
    public void getAllExpensesByUser(String accountId, ExpenseCallback callback) {
        String username = getUsernameFromAccountId(accountId);
        
        if (username != null) {
            expensesRef.orderByChild("userId").equalTo(username)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            List<Expense> expenses = new ArrayList<>();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Expense expense = snapshot.getValue(Expense.class);
                                if (expense != null) {
                                    expense.setId(snapshot.getKey());
                                    expense.setAccountId(accountId);
                                    expenses.add(expense);
                                }
                            }
                            callback.onSuccess(expenses);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            callback.onError(databaseError.toException());
                        }
                    });
        } else {
            expensesRef.orderByChild("accountId").equalTo(accountId)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            List<Expense> expenses = new ArrayList<>();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Expense expense = snapshot.getValue(Expense.class);
                                if (expense != null) {
                                    expense.setId(snapshot.getKey());
                                    expenses.add(expense);
                                }
                            }
                            callback.onSuccess(expenses);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            callback.onError(databaseError.toException());
                        }
                    });
        }
    }
    
    private String getUsernameFromAccountId(String accountId) {
        try {
            com.google.firebase.database.DatabaseReference accountRef = 
                com.google.firebase.database.FirebaseDatabase.getInstance("https://campus-expense-manager-c16e3-default-rtdb.asia-southeast1.firebasedatabase.app")
                    .getReference("accounts").child(accountId);
            
            com.google.firebase.database.DataSnapshot snapshot = 
                com.google.android.gms.tasks.Tasks.await(accountRef.get());
            
            if (snapshot.exists()) {
                com.budgetwise.campusexpensemanager.firebase.models.FirebaseAccount account = 
                    snapshot.getValue(com.budgetwise.campusexpensemanager.firebase.models.FirebaseAccount.class);
                return account != null ? account.getUsername() : null;
            }
        } catch (Exception e) {
            android.util.Log.e("ExpenseRepository", "Error getting username from accountId: " + e.getMessage());
        }
        return null;
    }

    public void getExpenseById(String expenseId, SingleExpenseCallback callback) {
        expensesRef.child(expenseId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Expense expense = dataSnapshot.getValue(Expense.class);
                if (expense != null) {
                    expense.setId(dataSnapshot.getKey());
                    callback.onSuccess(expense);
                } else {
                    callback.onError(new Exception("Expense not found"));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onError(databaseError.toException());
            }
        });
    }
    
    public com.google.firebase.database.Query getExpensesByCategoryAndMonth(String accountId, String category, int month, int year) {
        String username = getUsernameFromAccountId(accountId);
        
        if (username != null) {
            return expensesRef.orderByChild("userId").equalTo(username);
        } else {
            return expensesRef.orderByChild("accountId").equalTo(accountId);
        }
    }
    
    public com.google.firebase.database.Query getAllExpensesByCategoryAndMonth(String accountId, String category, int month, int year) {
        String username = getUsernameFromAccountId(accountId);
        
        if (username != null) {
            return expensesRef.orderByChild("userId").equalTo(username);
        } else {
            return expensesRef.orderByChild("accountId").equalTo(accountId);
        }
    }

    public Task<Void> deleteRecurringExpenseRecords(String accountId) {
        String username = getUsernameFromAccountId(accountId);
        
        if (username != null) {
            return expensesRef.orderByChild("userId").equalTo(username)
                    .get().continueWith(task -> {
                        if (task.isSuccessful()) {
                            for (DataSnapshot snapshot : task.getResult().getChildren()) {
                                Expense expense = snapshot.getValue(Expense.class);
                                if (expense != null && isRecurringExpense(expense)) {
                                    expensesRef.child(snapshot.getKey()).removeValue();
                                }
                            }
                        }
                        return null;
                    });
        } else {
            return expensesRef.orderByChild("accountId").equalTo(accountId)
                    .get().continueWith(task -> {
                        if (task.isSuccessful()) {
                            for (DataSnapshot snapshot : task.getResult().getChildren()) {
                                Expense expense = snapshot.getValue(Expense.class);
                                if (expense != null && isRecurringExpense(expense)) {
                                    expensesRef.child(snapshot.getKey()).removeValue();
                                }
                            }
                        }
                        return null;
                    });
        }
    }

    public interface ExpenseCallback {
        void onSuccess(List<Expense> expenses);
        void onError(Exception e);
    }

    public interface SingleExpenseCallback {
        void onSuccess(Expense expense);
        void onError(Exception e);
    }
} 