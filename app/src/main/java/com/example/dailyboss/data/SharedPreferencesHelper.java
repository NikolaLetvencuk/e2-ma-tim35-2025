package com.example.dailyboss.data;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesHelper {

    private static final String PREFS_NAME = "dailyboss_prefs";
    private static final String KEY_LAST_SELECTED_COLOR = "last_selected_color";

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
}
