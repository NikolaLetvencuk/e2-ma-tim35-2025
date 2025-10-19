package com.example.dailyboss.data.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.dailyboss.data.DatabaseHelper;
import com.example.dailyboss.domain.model.UserStatistic;

public class UserStatisticDao {

    private final DatabaseHelper dbHelper;

    public UserStatisticDao(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // Insert ili update (upsert)
    public boolean upsert(UserStatistic stat) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_STAT_ID, stat.getId());
        values.put(DatabaseHelper.COL_STAT_USER_ID, stat.getUserId());
        values.put(DatabaseHelper.COL_STAT_COMPLETED_TASKS, stat.getCompletedTasks());
        values.put(DatabaseHelper.COL_STAT_FAILED_TASKS, stat.getFailedTasks());
        values.put(DatabaseHelper.COL_STAT_LEVEL, stat.getLevel());
        values.put(DatabaseHelper.COL_STAT_EXP_POINTS, stat.getExperiencePoints());
        values.put(DatabaseHelper.COL_STAT_WIN_STREAK, stat.getWinStreak());
        values.put(DatabaseHelper.COL_STAT_LAST_ACTIVE, stat.getLastActiveTimestamp());

        // Pokušaj update
        int rows = db.update(DatabaseHelper.TABLE_USER_STATISTICS, values,
                DatabaseHelper.COL_STAT_ID + " = ?", new String[]{stat.getId()});

        if (rows == 0) {
            long result = db.insert(DatabaseHelper.TABLE_USER_STATISTICS, null, values);
            db.close();
            return result != -1;
        }

        db.close();
        return true;
    }

    // Dohvatanje po userId
    public UserStatistic getByUserId(String userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_USER_STATISTICS, null,
                DatabaseHelper.COL_STAT_USER_ID + " = ?", new String[]{userId},
                null, null, null);

        UserStatistic stat = null;
        if (cursor.moveToFirst()) {
            stat = new UserStatistic(
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_STAT_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_STAT_USER_ID)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_STAT_COMPLETED_TASKS)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_STAT_FAILED_TASKS)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_STAT_LEVEL)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_STAT_EXP_POINTS)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_STAT_WIN_STREAK)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_STAT_LAST_ACTIVE))
            );
        }

        cursor.close();
        db.close();
        return stat;
    }

    // Samo update
    public boolean update(UserStatistic stat) {
        return upsert(stat); // upsert pokriva i update
    }
}