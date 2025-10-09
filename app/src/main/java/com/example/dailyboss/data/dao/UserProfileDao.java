package com.example.dailyboss.data.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.dailyboss.data.DatabaseHelper;
import com.example.dailyboss.domain.model.UserProfile;

public class UserProfileDao {

    private final DatabaseHelper dbHelper;

    public UserProfileDao(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // Insert ili update (upsert)
    public boolean upsert(UserProfile stat) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_PROFILE_ID, stat.getId());
        values.put(DatabaseHelper.COL_PROFILE_USER_ID, stat.getUserId());
        values.put(DatabaseHelper.COL_PROFILE_COMPLETED_TASKS, stat.getCompletedTasks());
        values.put(DatabaseHelper.COL_PROFILE_FAILED_TASKS, stat.getFailedTasks());
        values.put(DatabaseHelper.COL_PROFILE_LEVEL, stat.getLevel());
        values.put(DatabaseHelper.COL_PROFILE_EXP_POINTS, stat.getExperiencePoints());
        values.put(DatabaseHelper.COL_PROFILE_WIN_STREAK, stat.getWinStreak());
        values.put(DatabaseHelper.COL_PROFILE_LAST_ACTIVE, stat.getLastActiveTimestamp());

        // Pokušaj update
        int rows = db.update(DatabaseHelper.TABLE_USER_PROFILE, values,
                DatabaseHelper.COL_PROFILE_ID + " = ?", new String[]{stat.getId()});

        if (rows == 0) {
            long result = db.insert(DatabaseHelper.TABLE_USER_PROFILE, null, values);
            db.close();
            return result != -1;
        }

        db.close();
        return true;
    }

    // Dohvatanje po userId
    public UserProfile getByUserId(String userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_USER_PROFILE, null,
                DatabaseHelper.COL_PROFILE_USER_ID + " = ?", new String[]{userId},
                null, null, null);

        UserProfile stat = null;
        if (cursor.moveToFirst()) {
            stat = new UserProfile(
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PROFILE_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PROFILE_USER_ID)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PROFILE_COMPLETED_TASKS)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PROFILE_FAILED_TASKS)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PROFILE_LEVEL)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PROFILE_EXP_POINTS)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PROFILE_WIN_STREAK)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PROFILE_LAST_ACTIVE))
            );
        }

        cursor.close();
        db.close();
        return stat;
    }

    // Samo update
    public boolean update(UserProfile stat) {
        return upsert(stat); // upsert pokriva i update
    }
}