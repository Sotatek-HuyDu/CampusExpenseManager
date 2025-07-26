# Recurring Expenses Feature

## Overview
The Recurring Expenses feature allows users to set up automatic expenses that repeat at regular intervals (weekly, bi-weekly, monthly, or yearly). These expenses are automatically added to the user's budget when they become due.

## Features

### 1. Add Recurring Expenses
- **Description**: Enter a description for the recurring expense (e.g., "Monthly Rent")
- **Amount**: Set the amount for each occurrence
- **Category**: Choose from existing expense categories
- **Start Date**: When the recurring expense should begin
- **End Date**: When the recurring expense should end
- **Recurrence Interval**: Choose from Weekly, Bi-weekly, Monthly, or Yearly

### 2. View Recurring Expenses
- List all recurring expenses with their details
- Shows description, amount, category, date range, and interval
- Tap any item to edit it

### 3. Edit/Delete Recurring Expenses
- Modify existing recurring expenses
- Delete recurring expenses when no longer needed

### 4. Automatic Processing
- The app automatically processes recurring expenses
- When a recurring expense is due, it creates an actual expense entry
- Users receive notifications about due recurring expenses

## Navigation
- New "Recurring" tab in the bottom navigation
- Accessible from the main navigation menu

## Technical Implementation

### Files Created/Modified:

#### Activities:
- `RecurringExpenseActivity.java` - Main list view for recurring expenses
- `AddEditRecurringExpenseActivity.java` - Form for adding/editing recurring expenses

#### Adapters:
- `RecurringExpenseAdapter.java` - RecyclerView adapter for recurring expenses list

#### Layouts:
- `activity_recurring_expense.xml` - Main recurring expenses screen
- `activity_add_edit_recurring_expense.xml` - Add/edit form
- `item_recurring_expense.xml` - Individual recurring expense item layout

#### Services:
- `RecurringExpenseService.java` - Background service for processing recurring expenses

#### Utilities:
- `RecurringExpenseUtil.java` - Helper methods for recurring expense calculations

#### Navigation:
- Updated `bottom_nav_menu.xml` to include new "Recurring" tab
- Updated `BaseActivity.java` to handle navigation to recurring expenses
- Added new icon `ic_recurring.xml`

#### Manifest:
- Registered new activities and service in `AndroidManifest.xml`

### Data Model:
Uses existing `FirebaseRecurringExpense` model with:
- `id`: Unique identifier
- `accountId`: User account ID
- `description`: Expense description
- `amount`: Amount per occurrence
- `category`: Expense category
- `startDate`: When recurring expense begins
- `endDate`: When recurring expense ends
- `recurrenceIntervalDays`: Days between occurrences

### Firebase Integration:
- Uses existing `RecurringExpenseRepository` for Firebase operations
- Stores recurring expenses in Firebase Realtime Database
- Integrates with existing expense system

## Usage Instructions

### Adding a Recurring Expense:
1. Navigate to the "Recurring" tab
2. Tap the "+" floating action button
3. Fill in the form:
   - Description (required)
   - Amount (required, must be > 0)
   - Category (select from dropdown)
   - Start Date (tap to select)
   - End Date (tap to select)
   - Recurrence Interval (select from dropdown)
4. Tap "Save"

### Editing a Recurring Expense:
1. Navigate to the "Recurring" tab
2. Tap on any recurring expense item
3. Modify the fields as needed
4. Tap "Save" to update or "Delete" to remove

### Viewing Recurring Expenses:
- All recurring expenses are displayed in a list
- Shows description, amount, category, date range, and interval
- Sorted by start date (newest first)

## Future Enhancements
- Add support for custom intervals (e.g., every 3 months)
- Add notifications for upcoming recurring expenses
- Add ability to pause/resume recurring expenses
- Add recurring expense templates
- Add bulk operations (delete multiple, change category, etc.)
- Add recurring expense analytics and reports 