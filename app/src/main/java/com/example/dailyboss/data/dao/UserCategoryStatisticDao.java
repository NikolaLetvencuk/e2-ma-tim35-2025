package com.example.dailyboss.data.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.dailyboss.data.DatabaseHelper;
import com.example.dailyboss.domain.model.UserCategoryStatistic;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserCategoryStatisticDao {

    private final DatabaseHelper dbHelper;
    private static final String TAG = "UserCategoryStatDao";

    public UserCategoryStatisticDao(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public boolean upsert(UserCategoryStatistic stat) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_U_CAT_STAT_USER_STAT_ID, stat.getUserStatisticId());
        values.put(DatabaseHelper.COL_U_CAT_STAT_CATEGORY_ID, stat.getCategoryId());
        values.put(DatabaseHelper.COL_U_CAT_STAT_COMPLETED_COUNT, stat.getCompletedCount());
        values.put(DatabaseHelper.COL_U_CAT_STAT_CATEGORY_NAME, stat.getCategoryName());

        // Pokušaj ažurirati postojeći red
        int rowsAffected = db.update(DatabaseHelper.TABLE_USER_CATEGORY_STATISTICS, values,
                DatabaseHelper.COL_U_CAT_STAT_USER_STAT_ID + " = ? AND " +
                        DatabaseHelper.COL_U_CAT_STAT_CATEGORY_ID + " = ?",
                new String[]{stat.getUserStatisticId(), stat.getCategoryId()});

        if (rowsAffected == 0) {
            // Ako nije ažuriran, ubaci novi
            values.put(DatabaseHelper.COL_U_CAT_STAT_ID, UUID.randomUUID().toString()); // Generiši novi ID
            long result = db.insert(DatabaseHelper.TABLE_USER_CATEGORY_STATISTICS, null, values);
            db.close();
            return result != -1;
        }
        db.close();
        return rowsAffected > 0;
    }

    public UserCategoryStatistic getCategoryStatistic(String userStatisticId, String categoryId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_USER_CATEGORY_STATISTICS, null,
                DatabaseHelper.COL_U_CAT_STAT_USER_STAT_ID + " = ? AND " +
                        DatabaseHelper.COL_U_CAT_STAT_CATEGORY_ID + " = ?",
                new String[]{userStatisticId, categoryId},
                null, null, null);

        UserCategoryStatistic stat = null;
        if (cursor.moveToFirst()) {
            stat = new UserCategoryStatistic(
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_U_CAT_STAT_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_U_CAT_STAT_USER_STAT_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_U_CAT_STAT_CATEGORY_ID)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_U_CAT_STAT_COMPLETED_COUNT)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_U_CAT_STAT_CATEGORY_NAME))
            );
        }
        cursor.close();
        db.close();
        return stat;
    }

    public List<UserCategoryStatistic> getAllCategoryStatisticsForUser(String userStatisticId) {
        List<UserCategoryStatistic> stats = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_USER_CATEGORY_STATISTICS, null,
                DatabaseHelper.COL_U_CAT_STAT_USER_STAT_ID + " = ?", new String[]{userStatisticId},
                null, null, null);

        while (cursor.moveToNext()) {
            UserCategoryStatistic stat = new UserCategoryStatistic(
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_U_CAT_STAT_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_U_CAT_STAT_USER_STAT_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_U_CAT_STAT_CATEGORY_ID)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_U_CAT_STAT_COMPLETED_COUNT)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_U_CAT_STAT_CATEGORY_NAME))
            );
            stats.add(stat);
        }
        cursor.close();
        db.close();
        return stats;
    }

    public boolean deleteCategoryStatisticsForUser(String userStatisticId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsAffected = db.delete(DatabaseHelper.TABLE_USER_CATEGORY_STATISTICS,
                DatabaseHelper.COL_U_CAT_STAT_USER_STAT_ID + " = ?", new String[]{userStatisticId});
        db.close();
        return rowsAffected > 0;
    }
}