package com.budgetwise.campusexpensemanager.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.budgetwise.campusexpensemanager.R;
import com.budgetwise.campusexpensemanager.models.CalendarDay;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder> {

    private List<CalendarDay> calendarDays = new ArrayList<>();
    private OnDayClickListener onDayClickListener;

    public interface OnDayClickListener {
        void onDayClick(CalendarDay day);
    }

    public void setOnDayClickListener(OnDayClickListener listener) {
        this.onDayClickListener = listener;
    }

    public void setCalendarDays(List<CalendarDay> days) {
        this.calendarDays = days;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_day, parent, false);
        return new CalendarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        CalendarDay day = calendarDays.get(position);
        holder.bind(day);
    }

    @Override
    public int getItemCount() {
        return calendarDays.size();
    }

    class CalendarViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDay;
        private View rootView;

        CalendarViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tv_day);
            rootView = itemView;

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && onDayClickListener != null) {
                    onDayClickListener.onDayClick(calendarDays.get(position));
                }
            });
        }

        void bind(CalendarDay day) {
            if (day.isCurrentMonth()) {
                tvDay.setText(String.valueOf(day.getDay()));
                tvDay.setVisibility(View.VISIBLE);
                
                if (day.isSelected()) {
                    tvDay.setBackgroundResource(R.drawable.calendar_selected_background);
                    tvDay.setTextColor(itemView.getContext().getColor(android.R.color.white));
                } else if (day.isToday()) {
                    tvDay.setBackgroundResource(R.drawable.calendar_today_background);
                    tvDay.setTextColor(itemView.getContext().getColor(android.R.color.white));
                } else if (day.hasExpenses()) {
                    tvDay.setBackgroundResource(R.drawable.calendar_expense_background);
                    tvDay.setTextColor(itemView.getContext().getColor(android.R.color.white));
                } else {
                    tvDay.setBackgroundResource(0);
                    tvDay.setTextColor(itemView.getContext().getColor(android.R.color.black));
                }
                
                rootView.setEnabled(true);
            } else {
                tvDay.setText(String.valueOf(day.getDay()));
                tvDay.setVisibility(View.VISIBLE);
                tvDay.setTextColor(itemView.getContext().getColor(android.R.color.darker_gray));
                tvDay.setBackgroundResource(0);
                rootView.setEnabled(false);
            }
        }
    }
} 