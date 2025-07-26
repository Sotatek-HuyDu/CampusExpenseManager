package com.budgetwise.campusexpensemanager.utils;

import android.content.Context;
import androidx.core.content.ContextCompat;

import com.budgetwise.campusexpensemanager.R;
import com.budgetwise.campusexpensemanager.models.ExpenseCategory;

public class CategoryColorUtil {
    
    public static int getCategoryColor(Context context, String categoryName) {
        if (categoryName == null) {
            return ContextCompat.getColor(context, R.color.category_other);
        }
        
        switch (categoryName.toLowerCase()) {
            case "rent":
                return ContextCompat.getColor(context, R.color.category_rent);
            case "groceries":
                return ContextCompat.getColor(context, R.color.category_groceries);
            case "transportation":
                return ContextCompat.getColor(context, R.color.category_transportation);
            case "utilities":
                return ContextCompat.getColor(context, R.color.category_utilities);
            case "entertainment":
                return ContextCompat.getColor(context, R.color.category_entertainment);
            case "dining":
                return ContextCompat.getColor(context, R.color.category_dining);
            case "shopping":
                return ContextCompat.getColor(context, R.color.category_shopping);
            case "health":
                return ContextCompat.getColor(context, R.color.category_health);
            case "education":
                return ContextCompat.getColor(context, R.color.category_education);
            default:
                return ContextCompat.getColor(context, R.color.category_other);
        }
    }
    
    public static int getCategoryColor(Context context, ExpenseCategory category) {
        return getCategoryColor(context, category.getDisplayName());
    }
} 