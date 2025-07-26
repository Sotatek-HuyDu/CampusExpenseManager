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
import com.budgetwise.campusexpensemanager.firebase.models.FirebaseRecurringExpense;
import com.budgetwise.campusexpensemanager.utils.CategoryColorUtil;
import com.budgetwise.campusexpensemanager.utils.CategoryIconUtil;
import com.budgetwise.campusexpensemanager.utils.RecurringExpenseUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RecurringExpenseAdapter extends RecyclerView.Adapter<RecurringExpenseAdapter.RecurringExpenseViewHolder> {

    private List<FirebaseRecurringExpense> recurringExpenses;
    private Context context;
    private SimpleDateFormat dateFormat;

    public RecurringExpenseAdapter(Context context) {
        this.context = context;
        this.recurringExpenses = new ArrayList<>();
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public RecurringExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recurring_expense, parent, false);
        return new RecurringExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecurringExpenseViewHolder holder, int position) {
        FirebaseRecurringExpense recurringExpense = recurringExpenses.get(position);
        holder.bind(recurringExpense);
    }

    @Override
    public int getItemCount() {
        return recurringExpenses.size();
    }

    public void updateRecurringExpenses(List<FirebaseRecurringExpense> newRecurringExpenses) {
        this.recurringExpenses = newRecurringExpenses;
        notifyDataSetChanged();
    }

    class RecurringExpenseViewHolder extends RecyclerView.ViewHolder {
        private TextView descriptionTextView;
        private TextView amountTextView;
        private TextView categoryTextView;
        private TextView dateRangeTextView;
        private TextView intervalTextView;
        private android.widget.ImageView categoryIconView;

        public RecurringExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            descriptionTextView = itemView.findViewById(R.id.recurring_expense_description);
            amountTextView = itemView.findViewById(R.id.recurring_expense_amount);
            categoryTextView = itemView.findViewById(R.id.recurring_expense_category);
            dateRangeTextView = itemView.findViewById(R.id.recurring_expense_date_range);
            intervalTextView = itemView.findViewById(R.id.recurring_expense_interval);
            categoryIconView = itemView.findViewById(R.id.recurring_expense_category_icon);

            // Set click listener for editing
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    FirebaseRecurringExpense recurringExpense = recurringExpenses.get(position);
                    Intent intent = new Intent(context, AddEditRecurringExpenseActivity.class);
                    intent.putExtra("recurring_expense_id", recurringExpense.getId());
                    context.startActivity(intent);
                }
            });
        }

        public void bind(FirebaseRecurringExpense recurringExpense) {
            descriptionTextView.setText(recurringExpense.getDescription());
            amountTextView.setText(String.format("$%.2f", recurringExpense.getAmount()));
            categoryTextView.setText(recurringExpense.getCategory());
            
            // Set category color and icon
            int categoryColor = CategoryColorUtil.getCategoryColor(context, recurringExpense.getCategory());
            int categoryIcon = CategoryIconUtil.getCategoryIcon(context, recurringExpense.getCategory());
            
            // Set background color for the container
            android.view.View categoryContainer = itemView.findViewById(R.id.recurring_expense_category_container);
            categoryContainer.setBackgroundColor(categoryColor);
            
            // Set category icon
            categoryIconView.setImageResource(categoryIcon);
            categoryIconView.setColorFilter(android.graphics.Color.WHITE);
            
            // Set text color to white for better contrast
            categoryTextView.setTextColor(android.graphics.Color.WHITE);
            
            // Set date range
            if (recurringExpense.getStartDate() != null && recurringExpense.getEndDate() != null) {
                String startDate = dateFormat.format(recurringExpense.getStartDate());
                String endDate = dateFormat.format(recurringExpense.getEndDate());
                dateRangeTextView.setText(String.format("%s - %s", startDate, endDate));
            } else {
                dateRangeTextView.setText("No date range");
            }
            
            // Set interval text
            String intervalDescription = RecurringExpenseUtil.getIntervalDescription(recurringExpense.getRecurrenceIntervalDays());
            intervalTextView.setText(intervalDescription);
        }
    }
} 