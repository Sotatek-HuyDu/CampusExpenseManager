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

    public void getExpensesByUser(String userId, ExpenseCallback callback) {
        expensesRef.orderByChild("userId").equalTo(userId)
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
    
    public com.google.firebase.database.Query getExpensesByCategoryAndMonth(String userId, String category, int month, int year) {
        return expensesRef.orderByChild("userId").equalTo(userId);
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