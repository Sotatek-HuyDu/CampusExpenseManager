package com.budgetwise.campusexpensemanager.firebase;

import com.budgetwise.campusexpensemanager.models.Budget;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class BudgetRepository {
    private final DatabaseReference budgetsRef;

    public BudgetRepository() {
        this.budgetsRef = FirebaseDatabase.getInstance("https://campus-expense-manager-c16e3-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("budgets");
    }

    public void getBudgetsByUser(String userId, BudgetCallback callback) {
        budgetsRef.orderByChild("accountId").equalTo(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        List<Budget> budgets = new ArrayList<>();
                        try {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                try {
                                    Budget budget = snapshot.getValue(Budget.class);
                                    if (budget != null) {
                                        // Set the Firebase key as the ID
                                        budget.id = snapshot.getKey();
                                        budgets.add(budget);
                                    }
                                } catch (Exception e) {
                                    android.util.Log.e("BudgetRepository", "Error parsing budget: " + e.getMessage());
                                    // Continue with next budget
                                }
                            }
                            callback.onSuccess(budgets);
                        } catch (Exception e) {
                            android.util.Log.e("BudgetRepository", "Error in onDataChange: " + e.getMessage());
                            callback.onError(e);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        callback.onError(databaseError.toException());
                    }
                });
    }

    public interface BudgetCallback {
        void onSuccess(List<Budget> budgets);
        void onError(Exception e);
    }
} 