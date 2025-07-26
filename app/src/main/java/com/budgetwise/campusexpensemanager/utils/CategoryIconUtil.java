package com.budgetwise.campusexpensemanager.utils;

import android.content.Context;
import androidx.core.content.ContextCompat;

import com.budgetwise.campusexpensemanager.R;
import com.budgetwise.campusexpensemanager.models.ExpenseCategory;

public class CategoryIconUtil {
    
    public static int getCategoryIcon(Context context, String categoryName) {
        if (categoryName == null) {
            return R.drawable.ic_other;
        }
        
        switch (categoryName.toLowerCase()) {
            case "rent":
                return R.drawable.ic_rent;
            case "groceries":
                return R.drawable.ic_groceries;
            case "transportation":
                return R.drawable.ic_transportation;
            case "utilities":
                return R.drawable.ic_utilities;
            case "entertainment":
                return R.drawable.ic_entertainment;
            case "dining":
                return R.drawable.ic_dining;
            case "shopping":
                return R.drawable.ic_shopping;
            case "health":
                return R.drawable.ic_health;
            case "education":
                return R.drawable.ic_education;
            default:
                return R.drawable.ic_other;
        }
    }
    
    public static int getCategoryIcon(Context context, ExpenseCategory category) {
        return getCategoryIcon(context, category.getDisplayName());
    }
} 