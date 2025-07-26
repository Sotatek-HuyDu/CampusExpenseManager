package com.budgetwise.campusexpensemanager.models;

public enum ExpenseCategory {
    RENT("Rent"),
    GROCERIES("Groceries"),
    TRANSPORTATION("Transportation"),
    UTILITIES("Utilities"),
    ENTERTAINMENT("Entertainment"),
    DINING("Dining"),
    SHOPPING("Shopping"),
    HEALTH("Health"),
    EDUCATION("Education"),
    OTHER("Other");

    private final String displayName;

    ExpenseCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static ExpenseCategory fromDisplayName(String displayName) {
        for (ExpenseCategory category : values()) {
            if (category.displayName.equals(displayName)) {
                return category;
            }
        }
        return OTHER;
    }
} 