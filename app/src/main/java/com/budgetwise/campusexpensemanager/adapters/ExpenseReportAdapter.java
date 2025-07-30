package com.budgetwise.campusexpensemanager.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.budgetwise.campusexpensemanager.R;
import com.budgetwise.campusexpensemanager.models.Expense;
import com.budgetwise.campusexpensemanager.models.Budget;
import com.budgetwise.campusexpensemanager.utils.CategoryColorUtil;
import com.budgetwise.campusexpensemanager.utils.CategoryIconUtil;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ExpenseReportAdapter extends RecyclerView.Adapter<ExpenseReportAdapter.ExpenseViewHolder> {

    private List<Expense> expenses = new ArrayList<>();
    private List<Budget> budgets = new ArrayList<>();

    public void setExpenses(List<Expense> expenses) {
        this.expenses = expenses;
        notifyDataSetChanged();
    }

    public void setBudgets(List<Budget> budgets) {
        this.budgets = budgets;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense_report, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenses.get(position);
        holder.bind(expense);
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    class ExpenseViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivCategoryIcon;
        private TextView tvCategory;
        private TextView tvDescription;
        private TextView tvAmount;
        private TextView tvBudgetPercentage;

        ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCategoryIcon = itemView.findViewById(R.id.iv_category_icon);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvBudgetPercentage = itemView.findViewById(R.id.tv_budget_percentage);
        }

        void bind(Expense expense) {
            // Set category icon and color
            int iconRes = CategoryIconUtil.getCategoryIcon(itemView.getContext(), expense.getCategory());
            int colorRes = CategoryColorUtil.getCategoryColor(itemView.getContext(), expense.getCategory());
            
            ivCategoryIcon.setImageResource(iconRes);
            ivCategoryIcon.setColorFilter(colorRes);

            // Set category name
            tvCategory.setText(expense.getCategory());

            // Set description
            tvDescription.setText(expense.getDescription());

            // Set amount
            DecimalFormat df = new DecimalFormat("#,##0.00");
            tvAmount.setText("$" + df.format(expense.getAmount()));

            // Calculate and set budget percentage
            double budgetPercentage = calculateBudgetPercentage(expense);
            if (budgetPercentage > 0) {
                tvBudgetPercentage.setText(String.format("%.1f%% of budget", budgetPercentage));
                
                // Color code based on percentage
                if (budgetPercentage >= 100) {
                    tvBudgetPercentage.setTextColor(itemView.getContext().getColor(android.R.color.holo_red_dark));
                } else if (budgetPercentage >= 80) {
                    tvBudgetPercentage.setTextColor(itemView.getContext().getColor(android.R.color.holo_orange_dark));
                } else {
                    tvBudgetPercentage.setTextColor(itemView.getContext().getColor(android.R.color.holo_green_dark));
                }
            } else {
                tvBudgetPercentage.setText("No budget set");
                tvBudgetPercentage.setTextColor(itemView.getContext().getColor(android.R.color.darker_gray));
            }
        }

        private double calculateBudgetPercentage(Expense expense) {
            // Find the budget for this category
            if (budgets.isEmpty()) {
                return 0;
            }
            
            String expenseCategory = expense.getCategory();
            if (expenseCategory == null) {
                return 0;
            }
            
            // Try exact match first
            for (Budget budget : budgets) {
                if (budget.category != null && budget.category.equals(expenseCategory)) {
                    if (budget.limit > 0) {
                        return (expense.getAmount() / budget.limit) * 100;
                    }
                    break;
                }
            }
            
            // Try case-insensitive match
            for (Budget budget : budgets) {
                if (budget.category != null && budget.category.equalsIgnoreCase(expenseCategory)) {
                    if (budget.limit > 0) {
                        return (expense.getAmount() / budget.limit) * 100;
                    }
                    break;
                }
            }
            
            // Try trimmed match
            String trimmedExpenseCategory = expenseCategory.trim();
            for (Budget budget : budgets) {
                if (budget.category != null && budget.category.trim().equalsIgnoreCase(trimmedExpenseCategory)) {
                    if (budget.limit > 0) {
                        return (expense.getAmount() / budget.limit) * 100;
                    }
                    break;
                }
            }
            
            return 0;
        }
    }
} 