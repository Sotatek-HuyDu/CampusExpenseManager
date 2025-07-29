package com.budgetwise.campusexpensemanager.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.budgetwise.campusexpensemanager.R;
import com.budgetwise.campusexpensemanager.models.MonthlyTrend;

import java.util.ArrayList;
import java.util.List;

public class TrendsAdapter extends RecyclerView.Adapter<TrendsAdapter.ViewHolder> {

    private List<MonthlyTrend> trendsList;
    private Context context;

    public TrendsAdapter(Context context) {
        this.context = context;
        this.trendsList = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_monthly_trend, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MonthlyTrend trend = trendsList.get(position);
        
        // Set month/year
        holder.monthYearText.setText(trend.getMonthYearString());
        
        // Set amount
        holder.amountText.setText(String.format("$%.2f", trend.getTotalAmount()));
        
        // Set trend indicator (simple text for now)
        if (position > 0) {
            MonthlyTrend previousTrend = trendsList.get(position - 1);
            double change = trend.getTotalAmount() - previousTrend.getTotalAmount();
            double percentageChange = previousTrend.getTotalAmount() > 0 ? 
                (change / previousTrend.getTotalAmount()) * 100 : 0;
            
            if (change > 0) {
                holder.trendText.setText(String.format("+$%.2f (+%.1f%%)", change, percentageChange));
                holder.trendText.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
            } else if (change < 0) {
                holder.trendText.setText(String.format("$%.2f (%.1f%%)", change, percentageChange));
                holder.trendText.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
            } else {
                holder.trendText.setText("No change");
                holder.trendText.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
            }
        } else {
            holder.trendText.setText("Baseline");
            holder.trendText.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
        }
    }

    @Override
    public int getItemCount() {
        return trendsList.size();
    }

    public void updateTrends(List<MonthlyTrend> trends) {
        this.trendsList.clear();
        if (trends != null) {
            this.trendsList.addAll(trends);
        }
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView monthYearText;
        TextView amountText;
        TextView trendText;

        ViewHolder(View itemView) {
            super(itemView);
            monthYearText = itemView.findViewById(R.id.month_year_text);
            amountText = itemView.findViewById(R.id.amount_text);
            trendText = itemView.findViewById(R.id.trend_text);
        }
    }
} 