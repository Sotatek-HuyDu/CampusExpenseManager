package com.budgetwise.campusexpensemanager.models;

import java.util.Calendar;

public class CalendarDay {
    private int day;
    private int month;
    private int year;
    private boolean isCurrentMonth;
    private boolean isToday;
    private boolean hasExpenses;
    private boolean isSelected;

    public CalendarDay(int day, int month, int year, boolean isCurrentMonth, boolean isToday) {
        this.day = day;
        this.month = month;
        this.year = year;
        this.isCurrentMonth = isCurrentMonth;
        this.isToday = isToday;
        this.hasExpenses = false;
        this.isSelected = false;
    }

    public CalendarDay(int day, int month, int year, boolean isCurrentMonth, boolean isToday, boolean hasExpenses) {
        this.day = day;
        this.month = month;
        this.year = year;
        this.isCurrentMonth = isCurrentMonth;
        this.isToday = isToday;
        this.hasExpenses = hasExpenses;
        this.isSelected = false;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public boolean isCurrentMonth() {
        return isCurrentMonth;
    }

    public void setCurrentMonth(boolean currentMonth) {
        isCurrentMonth = currentMonth;
    }

    public boolean isToday() {
        return isToday;
    }

    public void setToday(boolean today) {
        isToday = today;
    }

    public boolean hasExpenses() {
        return hasExpenses;
    }

    public void setHasExpenses(boolean hasExpenses) {
        this.hasExpenses = hasExpenses;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public Calendar toCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        return calendar;
    }

    @Override
    public String toString() {
        return "CalendarDay{" +
                "day=" + day +
                ", month=" + month +
                ", year=" + year +
                ", isCurrentMonth=" + isCurrentMonth +
                ", isToday=" + isToday +
                '}';
    }
} 