package com.example.dailyboss.data.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.dailyboss.data.DatabaseHelper;
import com.example.dailyboss.domain.model.UserStatistic;

public class UserStatisticDao {

    private final DatabaseHelper dbHelper;
    private static final String TAG = "UserStatisticDao";

    public UserStatisticDao(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public boolean upsert(UserStatistic userStatistic) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_STAT_ID, userStatistic.getId());
        values.put(DatabaseHelper.COL_STAT_USER_ID, userStatistic.getUserId());
        values.put(DatabaseHelper.COL_STAT_ACTIVE_DAYS_COUNT, userStatistic.getActiveDaysCount());
        values.put(DatabaseHelper.COL_STAT_TOTAL_CREATED_TASKS, userStatistic.getTotalCreatedTasks());
        values.put(DatabaseHelper.COL_STAT_TOTAL_COMPLETED_TASKS, userStatistic.getTotalCompletedTasks());
        values.put(DatabaseHelper.COL_STAT_TOTAL_FAILED_TASKS, userStatistic.getTotalFailedTasks());
        values.put(DatabaseHelper.COL_STAT_TOTAL_CANCELLED_TASKS, userStatistic.getTotalCancelledTasks());
        values.put(DatabaseHelper.COL_STAT_LONGEST_TASK_STREAK, userStatistic.getLongestTaskStreak());
        values.put(DatabaseHelper.COL_STAT_CURRENT_TASK_STREAK, userStatistic.getCurrentTaskStreak());
        values.put(DatabaseHelper.COL_STAT_TOTAL_SPECIAL_MISSIONS_STARTED, userStatistic.getTotalSpecialMissionsStarted());
        values.put(DatabaseHelper.COL_STAT_TOTAL_SPECIAL_MISSIONS_COMPLETED, userStatistic.getTotalSpecialMissionsCompleted());
        values.put(DatabaseHelper.COL_STAT_LAST_STREAK_UPDATE_TIMESTAMP, userStatistic.getLastStreakUpdateTimestamp());
        values.put(DatabaseHelper.COL_STAT_TOTAL_EXP_POINTS, userStatistic.getTotalXPPoints());
        values.put(DatabaseHelper.COL_STAT_AVERAGE_EXP_EARNED, userStatistic.getAverageXPEarned());
        values.put(DatabaseHelper.COL_STAT_POWER_POINTS, userStatistic.getPowerPoints());
        values.put(DatabaseHelper.COL_STAT_COINS, userStatistic.getCoins());
        values.put(DatabaseHelper.COL_STAT_TITLE, userStatistic.getTitle());


        long result = db.update(DatabaseHelper.TABLE_USER_STATISTICS, values,
                DatabaseHelper.COL_STAT_ID + " = ?", new String[]{userStatistic.getId()});

        if (result == 0) {
            result = db.insert(DatabaseHelper.TABLE_USER_STATISTICS, null, values);
            Log.d(TAG, "Inserting new UserStatistic for userId: " + userStatistic.getUserId() + ", Result: " + result);
        } else {
            Log.d(TAG, "Updating existing UserStatistic for userId: " + userStatistic.getUserId() + ", Result: " + result);
        }
        db.close();
        return result != -1;
    }

    public UserStatistic getUserStatistic(String userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_USER_STATISTICS, null,
                DatabaseHelper.COL_STAT_USER_ID + " = ?", new String[]{userId},
                null, null, null);

        UserStatistic userStatistic = null;
        if (cursor.moveToFirst()) {
            userStatistic = new UserStatistic(
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_STAT_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_STAT_USER_ID)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_STAT_ACTIVE_DAYS_COUNT)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_STAT_TOTAL_CREATED_TASKS)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_STAT_TOTAL_COMPLETED_TASKS)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_STAT_TOTAL_FAILED_TASKS)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_STAT_TOTAL_CANCELLED_TASKS)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_STAT_LONGEST_TASK_STREAK)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_STAT_CURRENT_TASK_STREAK)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_STAT_TOTAL_SPECIAL_MISSIONS_STARTED)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_STAT_TOTAL_SPECIAL_MISSIONS_COMPLETED)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_STAT_LAST_STREAK_UPDATE_TIMESTAMP)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_STAT_TOTAL_EXP_POINTS)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_STAT_AVERAGE_EXP_EARNED)),
                    getIntOrDefault(cursor, DatabaseHelper.COL_STAT_POWER_POINTS, 50),
                    getIntOrDefault(cursor, DatabaseHelper.COL_STAT_COINS, 0),
                    getStringOrDefault(cursor, DatabaseHelper.COL_STAT_TITLE, "Novajlija")
                    );
        }
        cursor.close();
        db.close();
        return userStatistic;
    }

    public boolean deleteUserStatistic(String userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsAffected = db.delete(DatabaseHelper.TABLE_USER_STATISTICS,
                DatabaseHelper.COL_STAT_USER_ID + " = ?", new String[]{userId});
        db.close();
        return rowsAffected > 0;
    }

    private int getIntOrDefault(Cursor cursor, String columnName, int defaultValue) {
        try {
            int index = cursor.getColumnIndexOrThrow(columnName);
            return cursor.getInt(index);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private long getLongOrDefault(Cursor cursor, String columnName, long defaultValue) {
        try {
            int index = cursor.getColumnIndexOrThrow(columnName);
            return cursor.getLong(index);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private String getStringOrDefault(Cursor cursor, String columnName, String defaultValue) {
        try {
            int index = cursor.getColumnIndexOrThrow(columnName);
            return cursor.getString(index);
        } catch (Exception e) {
            return defaultValue;
        }
    }
}