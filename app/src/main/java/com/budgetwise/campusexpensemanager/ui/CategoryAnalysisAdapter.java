package com.budgetwise.campusexpensemanager.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.budgetwise.campusexpensemanager.R;
import com.budgetwise.campusexpensemanager.models.CategoryAnalysis;
import com.budgetwise.campusexpensemanager.utils.CategoryColorUtil;
import com.budgetwise.campusexpensemanager.utils.CategoryIconUtil;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;

public class CategoryAnalysisAdapter extends RecyclerView.Adapter<CategoryAnalysisAdapter.ViewHolder> {

    private List<CategoryAnalysis> categoryAnalysisList;
    private Context context;
    private double totalAmount;

    public CategoryAnalysisAdapter(Context context) {
        this.context = context;
        this.categoryAnalysisList = new ArrayList<>();
        this.totalAmount = 0.0;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category_analysis, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoryAnalysis analysis = categoryAnalysisList.get(position);
        
        // Set category name
        holder.categoryNameText.setText(analysis.getCategory());
        
        // Set amount
        holder.amountText.setText(String.format("$%.2f", analysis.getTotalAmount()));
        
        // Set transaction count
        holder.transactionCountText.setText(analysis.getTransactionCount() + " transactions");
        
        // Set percentage
        double percentage = (analysis.getTotalAmount() / totalAmount) * 100;
        holder.percentageText.setText(String.format("%.1f%%", percentage));
        
        // Set progress bar
        holder.progressBar.setProgress((int) percentage);
        
        // Set category color
        int color = CategoryColorUtil.getCategoryColor(context, analysis.getCategory());
        holder.progressBar.setIndicatorColor(color);
        
        // Set budget information if available
        if (analysis.getBudgetLimit() > 0) {
            holder.budgetText.setVisibility(View.VISIBLE);
            holder.budgetText.setText(String.format("Budget: $%.2f", analysis.getBudgetLimit()));
            
            // Show budget utilization
            double utilization = analysis.getBudgetUtilization();
            if (analysis.isOverBudget()) {
                holder.budgetText.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
            } else if (utilization > 80) {
                holder.budgetText.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
            } else {
                holder.budgetText.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
            }
        } else {
            holder.budgetText.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return categoryAnalysisList.size();
    }

    public void updateCategoryAnalysis(List<CategoryAnalysis> analysis) {
        this.categoryAnalysisList.clear();
        
        if (analysis != null) {
            // Calculate total amount for percentage calculations
            this.totalAmount = 0.0;
            for (CategoryAnalysis item : analysis) {
                this.totalAmount += item.getTotalAmount();
            }
            
            // Sort by amount (highest first)
            analysis.sort((a1, a2) -> Double.compare(a2.getTotalAmount(), a1.getTotalAmount()));
            
            this.categoryAnalysisList.addAll(analysis);
        }
        
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView categoryNameText;
        TextView amountText;
        TextView transactionCountText;
        TextView percentageText;
        TextView budgetText;
        LinearProgressIndicator progressBar;

        ViewHolder(View itemView) {
            super(itemView);
            categoryNameText = itemView.findViewById(R.id.category_name_text);
            amountText = itemView.findViewById(R.id.amount_text);
            transactionCountText = itemView.findViewById(R.id.transaction_count_text);
            percentageText = itemView.findViewById(R.id.percentage_text);
            budgetText = itemView.findViewById(R.id.budget_text);
            progressBar = itemView.findViewById(R.id.progress_bar);
        }
    }
} 