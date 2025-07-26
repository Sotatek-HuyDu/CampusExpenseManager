package com.budgetwise.campusexpensemanager.utils;

import com.budgetwise.campusexpensemanager.firebase.models.FirebaseRecurringExpense;

import java.util.Calendar;
import java.util.Date;

public class RecurringExpenseUtil {

    /**
     * Check if a recurring expense should create an expense entry today
     */
    public static boolean shouldCreateExpenseToday(FirebaseRecurringExpense recurringExpense) {
        Calendar today = Calendar.getInstance();
        Calendar startDate = Calendar.getInstance();
        Calendar endDate = Calendar.getInstance();
        
        if (recurringExpense.getStartDate() != null) {
            startDate.setTime(recurringExpense.getStartDate());
        }
        if (recurringExpense.getEndDate() != null) {
            endDate.setTime(recurringExpense.getEndDate());
        }

        // Check if we're within the date range
        if (today.before(startDate) || today.after(endDate)) {
            return false;
        }

        // Check if today is a recurrence date
        return isRecurrenceDate(today, startDate, recurringExpense.getRecurrenceIntervalDays());
    }

    /**
     * Check if a given date is a recurrence date based on start date and interval
     */
    private static boolean isRecurrenceDate(Calendar checkDate, Calendar startDate, int intervalDays) {
        // Calculate days between start date and check date
        long startTime = startDate.getTimeInMillis();
        long checkTime = checkDate.getTimeInMillis();
        long diffTime = checkTime - startTime;
        long diffDays = diffTime / (24 * 60 * 60 * 1000);

        // Don't create expense on the start date itself (diffDays == 0)
        // Only create expenses for future recurrence dates
        return diffDays > 0 && diffDays % intervalDays == 0;
    }

    /**
     * Get the next occurrence date for a recurring expense
     */
    public static Date getNextOccurrenceDate(FirebaseRecurringExpense recurringExpense) {
        Calendar nextDate = Calendar.getInstance();
        if (recurringExpense.getStartDate() != null) {
            nextDate.setTime(recurringExpense.getStartDate());
        }
        
        // Add the interval days
        nextDate.add(Calendar.DAY_OF_YEAR, recurringExpense.getRecurrenceIntervalDays());
        
        return nextDate.getTime();
    }

    /**
     * Get the number of occurrences between start and end date
     */
    public static int getTotalOccurrences(FirebaseRecurringExpense recurringExpense) {
        if (recurringExpense.getStartDate() == null || recurringExpense.getEndDate() == null) {
            return 0;
        }

        Calendar startDate = Calendar.getInstance();
        Calendar endDate = Calendar.getInstance();
        startDate.setTime(recurringExpense.getStartDate());
        endDate.setTime(recurringExpense.getEndDate());

        long startTime = startDate.getTimeInMillis();
        long endTime = endDate.getTimeInMillis();
        long diffTime = endTime - startTime;
        long diffDays = diffTime / (24 * 60 * 60 * 1000);

        return (int) (diffDays / recurringExpense.getRecurrenceIntervalDays()) + 1;
    }

    /**
     * Get the total amount for all occurrences of a recurring expense
     */
    public static double getTotalAmount(FirebaseRecurringExpense recurringExpense) {
        int occurrences = getTotalOccurrences(recurringExpense);
        return occurrences * recurringExpense.getAmount();
    }

    /**
     * Get a human-readable description of the recurrence interval
     */
    public static String getIntervalDescription(int intervalDays) {
        switch (intervalDays) {
            case 7:
                return "Weekly";
            case 14:
                return "Bi-weekly";
            case 30:
                return "Monthly";
            case 365:
                return "Yearly";
            default:
                return String.format("Every %d days", intervalDays);
        }
    }

    /**
     * Get the interval days from a description
     */
    public static int getIntervalDays(String description) {
        switch (description.toLowerCase()) {
            case "weekly":
                return 7;
            case "bi-weekly":
            case "biweekly":
                return 14;
            case "monthly":
                return 30;
            case "yearly":
                return 365;
            default:
                return 30; // Default to monthly
        }
    }
} 