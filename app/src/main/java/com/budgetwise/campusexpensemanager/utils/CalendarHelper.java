package com.budgetwise.campusexpensemanager.utils;

import com.budgetwise.campusexpensemanager.models.CalendarDay;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalendarHelper {

    public static List<CalendarDay> generateCalendarDays(int year, int month) {
        List<CalendarDay> days = new ArrayList<>();
        
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, 1);
        
        // Get today's date for comparison
        Calendar today = Calendar.getInstance();
        
        // Get the first day of the month and the number of days in the month
        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        
        // Get the last day of the previous month
        calendar.add(Calendar.MONTH, -1);
        int daysInPreviousMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        
        // Add days from previous month to fill the first week
        int previousMonth = calendar.get(Calendar.MONTH);
        int previousYear = calendar.get(Calendar.YEAR);
        
        for (int i = firstDayOfWeek - 1; i > 0; i--) {
            int day = daysInPreviousMonth - i + 1;
            boolean isToday = isSameDay(day, previousMonth, previousYear, today);
            days.add(new CalendarDay(day, previousMonth, previousYear, false, isToday, false));
        }
        
        // Add days of current month
        calendar.add(Calendar.MONTH, 1); // Go back to current month
        for (int day = 1; day <= daysInMonth; day++) {
            boolean isToday = isSameDay(day, month, year, today);
            days.add(new CalendarDay(day, month, year, true, isToday, false));
        }
        
        // Add days from next month to complete the grid (6 weeks = 42 days)
        int remainingDays = 42 - days.size();
        calendar.add(Calendar.MONTH, 1);
        int nextMonth = calendar.get(Calendar.MONTH);
        int nextYear = calendar.get(Calendar.YEAR);
        
        for (int day = 1; day <= remainingDays; day++) {
            boolean isToday = isSameDay(day, nextMonth, nextYear, today);
            days.add(new CalendarDay(day, nextMonth, nextYear, false, isToday, false));
        }
        
        return days;
    }
    
    private static boolean isSameDay(int day, int month, int year, Calendar today) {
        return day == today.get(Calendar.DAY_OF_MONTH) &&
               month == today.get(Calendar.MONTH) &&
               year == today.get(Calendar.YEAR);
    }
} 