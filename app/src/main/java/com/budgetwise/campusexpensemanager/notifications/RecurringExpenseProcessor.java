package com.budgetwise.campusexpensemanager.notifications;

import android.util.Log;

import com.budgetwise.campusexpensemanager.firebase.ExpenseRepository;
import com.budgetwise.campusexpensemanager.firebase.models.FirebaseRecurringExpense;
import com.budgetwise.campusexpensemanager.firebase.repository.RecurringExpenseRepository;
import com.budgetwise.campusexpensemanager.models.Expense;
import com.budgetwise.campusexpensemanager.utils.RecurringExpenseUtil;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class RecurringExpenseProcessor {

    private static final String TAG = "RecurringExpenseProcessor";
    private final RecurringExpenseRepository recurringExpenseRepository;
    private final ExpenseRepository expenseRepository;
    private final ConcurrentHashMap<String, Long> processedExpenses = new ConcurrentHashMap<>();

    public RecurringExpenseProcessor() {
        this.recurringExpenseRepository = new RecurringExpenseRepository();
        this.expenseRepository = new ExpenseRepository();
        
        // Clean up old processed expenses periodically
        cleanOldProcessedExpenses();
    }
    
    private void cleanOldProcessedExpenses() {
        long currentTime = System.currentTimeMillis();
        long oneDayAgo = currentTime - 86400000; // 24 hours
        
        processedExpenses.entrySet().removeIf(entry -> entry.getValue() < oneDayAgo);
    }

    /**
     * Process all recurring expenses and create actual expense entries for those that are due
     */
    public void processRecurringExpenses(String accountId) {
        Log.d(TAG, "Processing recurring expenses for account: " + accountId);
        
        // Clean up old processed expenses
        cleanOldProcessedExpenses();
        
        recurringExpenseRepository.getRecurringExpensesByAccount(accountId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        FirebaseRecurringExpense recurringExpense = snapshot.getValue(FirebaseRecurringExpense.class);
                        if (recurringExpense != null) {
                            recurringExpense.setId(snapshot.getKey());
                            processSingleRecurringExpense(recurringExpense);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "Error loading recurring expenses: " + databaseError.getMessage());
                }
            });
    }

    /**
     * Process a single recurring expense and create expense entries if due
     */
    private void processSingleRecurringExpense(FirebaseRecurringExpense recurringExpense) {
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
            return;
        }



        // Check if today is a recurrence date
        if (RecurringExpenseUtil.shouldCreateExpenseToday(recurringExpense)) {
            // Create a unique key for this recurring expense and date
            String expenseKey = recurringExpense.getId() + "_" + today.get(Calendar.YEAR) + "_" + 
                              (today.get(Calendar.MONTH) + 1) + "_" + today.get(Calendar.DAY_OF_MONTH);
            
            // Check if we've already processed this expense recently (within 1 hour)
            long currentTime = System.currentTimeMillis();
            Long lastProcessed = processedExpenses.get(expenseKey);
            if (lastProcessed != null && (currentTime - lastProcessed) < 3600000) { // 1 hour
                Log.d(TAG, "Skipping recently processed expense: " + expenseKey);
                return;
            }
            
            // Mark as processed
            processedExpenses.put(expenseKey, currentTime);
            
            createExpenseFromRecurring(recurringExpense, today.getTime());
        }
    }

    /**
     * Create an actual expense entry from a recurring expense
     */
    private void createExpenseFromRecurring(FirebaseRecurringExpense recurringExpense, Date expenseDate) {
        // Check if this expense already exists for this date
        checkIfExpenseExists(recurringExpense, expenseDate, exists -> {
            if (!exists) {
                // Create the expense entry with a special description to identify it as recurring
                String recurringDescription = "[RECURRING] " + recurringExpense.getDescription();
                Expense expense = new Expense(
                    recurringDescription,
                    recurringExpense.getAmount(),
                    recurringExpense.getCategory(),
                    expenseDate,
                    recurringExpense.getAccountId()
                );

                // Add to Firebase
                expenseRepository.addExpense(expense)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Created expense from recurring: " + recurringExpense.getDescription() + 
                                  " for date: " + expenseDate);
                        } else {
                            Log.e(TAG, "Failed to create expense from recurring: " + task.getException());
                        }
                    });
            } else {
                Log.d(TAG, "Expense already exists for recurring: " + recurringExpense.getDescription() + 
                      " on date: " + expenseDate);
            }
        });
    }

    /**
     * Check if an expense entry already exists for this recurring expense on the given date
     */
    private void checkIfExpenseExists(FirebaseRecurringExpense recurringExpense, Date expenseDate, 
                                    ExpenseExistsCallback callback) {
        Calendar expenseCal = Calendar.getInstance();
        expenseCal.setTime(expenseDate);
        
        expenseRepository.getExpensesByCategoryAndMonth(
            recurringExpense.getAccountId(), 
            recurringExpense.getCategory(), 
            expenseCal.get(Calendar.MONTH) + 1, 
            expenseCal.get(Calendar.YEAR)
        ).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean exists = false;
                
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Expense expense = snapshot.getValue(Expense.class);
                    if (expense != null && 
                        (expense.getDescription().equals(recurringExpense.getDescription()) ||
                         expense.getDescription().equals("[RECURRING] " + recurringExpense.getDescription())) &&
                        expense.getAmount() == recurringExpense.getAmount() &&
                        isSameDay(expense.getDate(), expenseDate)) {
                        exists = true;
                        Log.d(TAG, "Found existing expense: " + expense.getDescription() + " on " + expenseDate);
                        break;
                    }
                }
                
                callback.onResult(exists);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Error checking if expense exists: " + databaseError.getMessage());
                callback.onResult(false);
            }
        });
    }

    /**
     * Check if two dates are the same day
     */
    private boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) return false;
        
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
               cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * Calculate total amount from recurring expenses for a specific month and category
     */
    public static double calculateRecurringExpenseAmount(List<FirebaseRecurringExpense> recurringExpenses, 
                                                       String category, int month, int year) {
        double totalAmount = 0.0;
        Calendar targetMonth = Calendar.getInstance();
        targetMonth.set(year, month - 1, 1); // month is 0-based in Calendar
        
        for (FirebaseRecurringExpense recurringExpense : recurringExpenses) {
            if (recurringExpense.getCategory().equals(category)) {
                // Check if this recurring expense should create an expense in the target month
                if (shouldCreateExpenseInMonth(recurringExpense, targetMonth)) {
                    totalAmount += recurringExpense.getAmount();
                }
            }
        }
        
        return totalAmount;
    }

    /**
     * Check if a recurring expense should create an expense in the given month
     */
    private static boolean shouldCreateExpenseInMonth(FirebaseRecurringExpense recurringExpense, Calendar targetMonth) {
        Calendar startDate = Calendar.getInstance();
        Calendar endDate = Calendar.getInstance();
        
        if (recurringExpense.getStartDate() != null) {
            startDate.setTime(recurringExpense.getStartDate());
        }
        if (recurringExpense.getEndDate() != null) {
            endDate.setTime(recurringExpense.getEndDate());
        }

        // Check if target month is within the date range
        if (targetMonth.before(startDate) || targetMonth.after(endDate)) {
            return false;
        }

        // Check if the recurring expense falls on this month
        Calendar firstDayOfMonth = (Calendar) targetMonth.clone();
        firstDayOfMonth.set(Calendar.DAY_OF_MONTH, 1);
        
        Calendar lastDayOfMonth = (Calendar) targetMonth.clone();
        lastDayOfMonth.set(Calendar.DAY_OF_MONTH, lastDayOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH));

        // Check if any occurrence falls within this month
        Calendar occurrenceDate = (Calendar) startDate.clone();
        while (occurrenceDate.before(lastDayOfMonth)) {
            if (occurrenceDate.after(firstDayOfMonth) || occurrenceDate.equals(firstDayOfMonth)) {
                return true;
            }
            occurrenceDate.add(Calendar.DAY_OF_YEAR, recurringExpense.getRecurrenceIntervalDays());
        }

        return false;
    }

    interface ExpenseExistsCallback {
        void onResult(boolean exists);
    }
} 