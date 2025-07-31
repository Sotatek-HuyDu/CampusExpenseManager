package com.budgetwise.campusexpensemanager.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "user_session";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_ACCOUNT_ID = "account_id";
    private static final String KEY_USER_EMAIL = "user_email";
    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void login(String username) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USERNAME, username);
        editor.apply();
    }
    
    public void login(String username, String accountId) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_ACCOUNT_ID, accountId);
        editor.apply();
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, null);
    }
    
    public String getAccountId() {
        return prefs.getString(KEY_ACCOUNT_ID, null);
    }
    
    public void saveUserEmail(String email) {
        editor.putString(KEY_USER_EMAIL, email);
        editor.apply();
    }
    
    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, null);
    }
}
