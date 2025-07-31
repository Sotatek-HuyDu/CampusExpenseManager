package com.budgetwise.campusexpensemanager.ui;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.budgetwise.campusexpensemanager.R;
import com.budgetwise.campusexpensemanager.firebase.models.FirebaseBudget;
import com.budgetwise.campusexpensemanager.firebase.models.FirebaseRecurringExpense;
import com.budgetwise.campusexpensemanager.firebase.ExpenseRepository;
import com.budgetwise.campusexpensemanager.firebase.repository.RecurringExpenseRepository;
import com.budgetwise.campusexpensemanager.utils.CategoryColorUtil;
import com.budgetwise.campusexpensemanager.utils.CategoryIconUtil;
import com.budgetwise.campusexpensemanager.notifications.RecurringExpenseProcessor;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.Calendar;
import java.util.List;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {

    private List<FirebaseBudget> budgets;
    private Context context;
    private ExpenseRepository expenseRepository;
    private RecurringExpenseRepository recurringExpenseRepository;

    public BudgetAdapter(Context context, List<FirebaseBudget> budgets) {
        this.context = context;
        this.budgets = budgets;
        this.expenseRepository = new ExpenseRepository();
        this.recurringExpenseRepository = new RecurringExpenseRepository();
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_budget, parent, false);
        return new BudgetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        FirebaseBudget budget = budgets.get(position);
        holder.bind(budget);
    }

    @Override
    public int getItemCount() {
        return budgets.size();
    }

    public void updateBudgets(List<FirebaseBudget> newBudgets) {
        this.budgets = newBudgets;
        notifyDataSetChanged();
    }

    class BudgetViewHolder extends RecyclerView.ViewHolder {
        private TextView categoryTextView;
        private TextView budgetAmountTextView;
        private TextView spentAmountTextView;
        private TextView remainingAmountTextView;
        private LinearProgressIndicator progressIndicator;
        private android.widget.ImageView categoryIconView;

        public BudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryTextView = itemView.findViewById(R.id.budget_category);
            budgetAmountTextView = itemView.findViewById(R.id.budget_amount);
            spentAmountTextView = itemView.findViewById(R.id.spent_amount);
            remainingAmountTextView = itemView.findViewById(R.id.remaining_amount);
            progressIndicator = itemView.findViewById(R.id.budget_progress);
            categoryIconView = itemView.findViewById(R.id.budget_category_icon);

            // Set click listener for the entire card
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    FirebaseBudget budget = budgets.get(position);
                    Intent intent = new Intent(context, AddEditBudgetActivity.class);
                    intent.putExtra("budget_id", budget.getId());
                    context.startActivity(intent);
                }
            });
        }

        public void bind(FirebaseBudget budget) {
            // Set category with color and icon
            categoryTextView.setText(budget.getCategory());
            int categoryColor = CategoryColorUtil.getCategoryColor(context, budget.getCategory());
            int categoryIcon = CategoryIconUtil.getCategoryIcon(context, budget.getCategory());
            
            // Set background color for the container
            android.view.View categoryContainer = itemView.findViewById(R.id.budget_category_container);
            categoryContainer.setBackgroundColor(categoryColor);
            
            // Set category icon
            categoryIconView.setImageResource(categoryIcon);
            categoryIconView.setColorFilter(android.graphics.Color.WHITE);

            // Set budget amount
            budgetAmountTextView.setText(String.format("$%.2f", budget.getLimit()));

            // Calculate spent amount from actual expenses
            calculateSpentAmount(budget, categoryColor);
        }
        
        private void calculateSpentAmount(FirebaseBudget budget, int categoryColor) {
            expenseRepository.getExpensesByCategoryAndMonth(
                budget.getAccountId(), 
                budget.getCategory(), 
                budget.getMonth(), 
                budget.getYear()
            ).addValueEventListener(new com.google.firebase.database.ValueEventListener() {
                @Override
                public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                    final double[] spentAmount = {0.0};
                    
                    for (com.google.firebase.database.DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        com.budgetwise.campusexpensemanager.firebase.models.FirebaseExpense expense = 
                            snapshot.getValue(com.budgetwise.campusexpensemanager.firebase.models.FirebaseExpense.class);
                        if (expense != null && 
                            expense.getCategory().equals(budget.getCategory()) &&
                            isExpenseInMonth(expense, budget.getMonth(), budget.getYear())) {
                            spentAmount[0] += expense.getAmount();
                        }
                    }
                    
                    addRecurringExpensesToSpentAmount(budget, spentAmount, categoryColor);
                }
                
                @Override
                public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
                }
            });
        }
        
        private void addRecurringExpensesToSpentAmount(FirebaseBudget budget, final double[] spentAmount, int categoryColor) {
            recurringExpenseRepository.getRecurringExpensesByAccount(budget.getAccountId())
                .addValueEventListener(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                        final double[] recurringAmount = {0.0};
                        
                        for (com.google.firebase.database.DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            FirebaseRecurringExpense recurringExpense = snapshot.getValue(FirebaseRecurringExpense.class);
                            
                            if (recurringExpense != null && 
                                recurringExpense.getCategory().equals(budget.getCategory())) {
                                // Check if this recurring expense should create an expense in the target month
                                if (shouldCreateExpenseInMonth(recurringExpense, budget.getMonth(), budget.getYear())) {
                                    recurringAmount[0] += recurringExpense.getAmount();
                                }
                            }
                        }
                        
                        // Add recurring amount to total spent
                        spentAmount[0] += recurringAmount[0];
                        
                        final double remainingAmount = budget.getLimit() - spentAmount[0];
                        final int progress = (int) ((spentAmount[0] / budget.getLimit()) * 100);
                        
                        // Update UI on main thread
                        ((android.app.Activity) context).runOnUiThread(() -> {
                            String spentText = String.format("Spent: $%.2f", spentAmount[0]);
                            if (recurringAmount[0] > 0) {
                                spentText += String.format(" (%.0f recurring)", recurringAmount[0]);
                            }
                            spentAmountTextView.setText(spentText);
                            remainingAmountTextView.setText(String.format("Remaining: $%.2f", remainingAmount));
                            
                            // Set progress
                            progressIndicator.setProgress(progress);
                            
                            // Set progress color based on usage
                            if (progress > 90) {
                                progressIndicator.setIndicatorColor(context.getResources().getColor(android.R.color.holo_red_dark));
                            } else if (progress > 75) {
                                progressIndicator.setIndicatorColor(context.getResources().getColor(android.R.color.holo_orange_dark));
                            } else {
                                progressIndicator.setIndicatorColor(categoryColor);
                            }
                            
                            // Apply visual indicators to the card based on progress
                            applyCardVisualIndicators(progress);
                        });
                    }
                    
                                    @Override
                public void onCancelled(com.google.firebase.database.DatabaseError databaseError) {
                    // Error loading recurring expenses
                }
                });
        }
        
        private boolean shouldCreateExpenseInMonth(FirebaseRecurringExpense recurringExpense, int month, int year) {
            if (recurringExpense.getStartDate() == null || recurringExpense.getEndDate() == null) {
                return false;
            }
            
            Calendar startDate = Calendar.getInstance();
            Calendar endDate = Calendar.getInstance();
            
            startDate.setTime(recurringExpense.getStartDate());
            endDate.setTime(recurringExpense.getEndDate());
            
            // Simple logic: if the recurring expense is active in the target month, include it
            // Check if the start date is before or in the target month, and end date is after or in the target month
            Calendar targetMonthStart = Calendar.getInstance();
            targetMonthStart.set(year, month - 1, 1, 0, 0, 0); // First day of target month
            
            Calendar targetMonthEnd = Calendar.getInstance();
            targetMonthEnd.set(year, month - 1, targetMonthStart.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59); // Last day of target month
            
            boolean shouldInclude = !startDate.after(targetMonthEnd) && !endDate.before(targetMonthStart);
            
            return shouldInclude;
        }
        
        private boolean isExpenseInMonth(com.budgetwise.campusexpensemanager.firebase.models.FirebaseExpense expense, int month, int year) {
            if (expense.getDate() == null) return false;
            
            java.util.Calendar expenseCal = java.util.Calendar.getInstance();
            expenseCal.setTime(expense.getDate());
            
            return expenseCal.get(java.util.Calendar.MONTH) + 1 == month && 
                   expenseCal.get(java.util.Calendar.YEAR) == year;
        }

        private void applyCardVisualIndicators(int progress) {
            // Get the MaterialCardView (the main card container)
            com.google.android.material.card.MaterialCardView cardView = (com.google.android.material.card.MaterialCardView) itemView;
            
            if (progress > 100) {
                // Over 100% - Red border and shadow only
                cardView.setStrokeColor(context.getResources().getColor(android.R.color.holo_red_dark));
                cardView.setStrokeWidth(5);
                cardView.setCardElevation(8);
                cardView.setCardBackgroundColor(context.getResources().getColor(R.color.backgroundWhite));
            } else if (progress >= 80) {
                // 80-100% - Yellow border and shadow
                cardView.setStrokeColor(context.getResources().getColor(android.R.color.holo_orange_light));
                cardView.setStrokeWidth(4);
                cardView.setCardElevation(6);
                cardView.setCardBackgroundColor(context.getResources().getColor(R.color.backgroundWhite));
            } else {
                // 0-80% - No special styling
                cardView.setStrokeWidth(0);
                cardView.setCardElevation(2);
                cardView.setCardBackgroundColor(context.getResources().getColor(R.color.backgroundWhite));
            }
        }
    }
} 