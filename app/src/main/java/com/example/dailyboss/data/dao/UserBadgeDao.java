package com.example.dailyboss.data.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.dailyboss.data.DatabaseHelper;
import com.example.dailyboss.domain.model.Badge;
import com.example.dailyboss.domain.model.UserBadge;

import java.util.ArrayList;
import java.util.List;

public class UserBadgeDao {

    private final DatabaseHelper dbHelper;

    public UserBadgeDao(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public boolean insert(UserBadge userBadge) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_USER_BADGE_ID, userBadge.getId());
        values.put(DatabaseHelper.COL_USER_BADGE_USER_ID, userBadge.getUserId());
        values.put(DatabaseHelper.COL_USER_BADGE_BADGE_ID, userBadge.getBadgeId());
        values.put(DatabaseHelper.COL_USER_BADGE_DATE_ACHIEVED, userBadge.getDateAchieved());

        long result = db.insert(DatabaseHelper.TABLE_USER_BADGES, null, values);
        db.close();
        return result != -1;
    }

    public List<UserBadge> getUserBadges(String userId) {
        List<UserBadge> badges = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_USER_BADGES, null,
                DatabaseHelper.COL_USER_BADGE_USER_ID + " = ?", new String[]{userId},
                null, null, null);

        while (cursor.moveToNext()) {
            UserBadge userBadge = new UserBadge(
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_BADGE_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_BADGE_USER_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_BADGE_BADGE_ID)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_BADGE_DATE_ACHIEVED))
            );
            badges.add(userBadge);
        }
        cursor.close();
        db.close();
        return badges;
    }

    public boolean delete(String userBadgeId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete(DatabaseHelper.TABLE_USER_BADGES,
                DatabaseHelper.COL_USER_BADGE_ID + " = ?", new String[]{userBadgeId});
        db.close();
        return rows > 0;
    }

    public Badge getBadgeById(String badgeId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_BADGES, null,
                DatabaseHelper.COL_BADGE_ID + " = ?", new String[]{badgeId},
                null, null, null);

        Badge badge = null;
        if (cursor.moveToFirst()) {
            badge = new Badge(
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BADGE_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BADGE_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BADGE_DESCRIPTION)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BADGE_ICON_PATH)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BADGE_REQUIRED_COMPLETIONS))
            );
        }

        cursor.close();
        db.close();
        return badge;
    }
}