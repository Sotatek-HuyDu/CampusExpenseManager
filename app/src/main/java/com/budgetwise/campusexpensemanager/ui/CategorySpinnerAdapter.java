package com.budgetwise.campusexpensemanager.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.budgetwise.campusexpensemanager.R;
import com.budgetwise.campusexpensemanager.models.ExpenseCategory;
import com.budgetwise.campusexpensemanager.utils.CategoryColorUtil;
import com.budgetwise.campusexpensemanager.utils.CategoryIconUtil;

import java.util.List;

public class CategorySpinnerAdapter extends ArrayAdapter<String> {
    
    private Context context;
    private List<String> categories;
    
    public CategorySpinnerAdapter(Context context, List<String> categories) {
        super(context, R.layout.item_category_spinner, categories);
        this.context = context;
        this.categories = categories;
    }
    
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }
    
    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }
    
    private View createItemView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_category_spinner, parent, false);
        }
        
        TextView categoryText = convertView.findViewById(R.id.category_text);
        View colorIndicator = convertView.findViewById(R.id.color_indicator);
        android.widget.ImageView categoryIcon = convertView.findViewById(R.id.category_icon);
        
        String category = categories.get(position);
        categoryText.setText(category);
        
        // Ensure text color is visible
        categoryText.setTextColor(context.getResources().getColor(R.color.textPrimary));
        
        // Set the color indicator
        int categoryColor = CategoryColorUtil.getCategoryColor(context, category);
        colorIndicator.setBackgroundColor(categoryColor);
        
        // Set the category icon
        int categoryIconRes = CategoryIconUtil.getCategoryIcon(context, category);
        categoryIcon.setImageResource(categoryIconRes);
        categoryIcon.setColorFilter(context.getResources().getColor(R.color.textPrimary));
        
        return convertView;
    }
} 