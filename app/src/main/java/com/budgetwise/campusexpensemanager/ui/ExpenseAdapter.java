package com.budgetwise.campusexpensemanager.ui;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.budgetwise.campusexpensemanager.R;
import com.budgetwise.campusexpensemanager.models.Expense;
import com.budgetwise.campusexpensemanager.utils.CategoryColorUtil;
import com.budgetwise.campusexpensemanager.utils.CategoryIconUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private List<Expense> expenses;
    private Context context;
    private SimpleDateFormat dateFormat;

    public ExpenseAdapter(Context context) {
        this.context = context;
        this.expenses = new ArrayList<>();
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_expense, parent, false);
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

    public void updateExpenses(List<Expense> newExpenses) {
        this.expenses = newExpenses;
        notifyDataSetChanged();
    }

    class ExpenseViewHolder extends RecyclerView.ViewHolder {
        private TextView descriptionTextView;
        private TextView amountTextView;
        private TextView categoryTextView;
        private TextView dateTextView;
        private android.widget.ImageView categoryIconView;

        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            descriptionTextView = itemView.findViewById(R.id.expense_description);
            amountTextView = itemView.findViewById(R.id.expense_amount);
            categoryTextView = itemView.findViewById(R.id.expense_category);
            dateTextView = itemView.findViewById(R.id.expense_date);
            categoryIconView = itemView.findViewById(R.id.expense_category_icon);

            // Set click listener for editing
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Expense expense = expenses.get(position);
                    Intent intent = new Intent(context, AddEditExpenseActivity.class);
                    intent.putExtra("expense_id", expense.getId());
                    context.startActivity(intent);
                }
            });
        }

        public void bind(Expense expense) {
            descriptionTextView.setText(expense.getDescription());
            amountTextView.setText(String.format("$%.2f", expense.getAmount()));
            categoryTextView.setText(expense.getCategory());
            
            // Set category color and icon
            int categoryColor = CategoryColorUtil.getCategoryColor(context, expense.getCategory());
            int categoryIcon = CategoryIconUtil.getCategoryIcon(context, expense.getCategory());
            
            // Set background color for the container
            android.view.View categoryContainer = itemView.findViewById(R.id.expense_category_container);
            categoryContainer.setBackgroundColor(categoryColor);
            
            // Set category icon
            categoryIconView.setImageResource(categoryIcon);
            categoryIconView.setColorFilter(android.graphics.Color.WHITE);
            
            // Set text color to white for better contrast
            categoryTextView.setTextColor(android.graphics.Color.WHITE);
            
            if (expense.getDate() != null) {
                dateTextView.setText(dateFormat.format(expense.getDate()));
            } else {
                dateTextView.setText("No date");
            }
        }
    }
} 