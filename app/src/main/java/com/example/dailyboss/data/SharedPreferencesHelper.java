package com.example.dailyboss.data;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesHelper {

    private static final String PREFS_NAME = "dailyboss_prefs";
    private static final String KEY_LAST_SELECTED_COLOR = "last_selected_color";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private SharedPreferences sharedPreferences;

    public SharedPreferencesHelper(Context context) {
        sharedPreferences = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveLastSelectedColor(String color) {
        sharedPreferences.edit().putString(KEY_LAST_SELECTED_COLOR, color).apply();
    }

    public String getLastSelectedColor() {
        return sharedPreferences.getString(KEY_LAST_SELECTED_COLOR, "#FFFFFF");
    }

    public void saveLoggedInUser(String userId, String userName) {
        sharedPreferences.edit()
                .putString(KEY_USER_ID, userId)
                .putString(KEY_USER_NAME, userName)
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .apply();
    }

    public String getLoggedInUserId() {
        return sharedPreferences.getString(KEY_USER_ID, null);
    }

    public String getLoggedInUserName() {
        return sharedPreferences.getString(KEY_USER_NAME, null);
    }

    public boolean isUserLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void logoutUser() {
        sharedPreferences.edit()
                .remove(KEY_USER_ID)
                .remove(KEY_USER_NAME)
                .putBoolean(KEY_IS_LOGGED_IN, false)
                .apply();
    }

    public void clearLoggedInUser() {
        logoutUser();
    }
}
