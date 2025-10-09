package com.example.dailyboss.data.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.dailyboss.data.DatabaseHelper;
import com.example.dailyboss.domain.model.Badge;

import java.util.ArrayList;
import java.util.List;

public class BadgeDao {

    private final DatabaseHelper dbHelper;

    public BadgeDao(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public boolean insert(Badge badge) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_BADGE_ID, badge.getId());
        values.put(DatabaseHelper.COL_BADGE_NAME, badge.getName());
        values.put(DatabaseHelper.COL_BADGE_DESCRIPTION, badge.getDescription());
        values.put(DatabaseHelper.COL_BADGE_ICON_PATH, badge.getIconPath());
        values.put(DatabaseHelper.COL_BADGE_REQUIRED_COMPLETIONS, badge.getRequiredCompletions());

        long result = db.insert(DatabaseHelper.TABLE_BADGES, null, values);
        db.close();
        return result != -1;
    }

    public Badge getBadge(String badgeId) {
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

    public List<Badge> getAllBadges() {
        List<Badge> badges = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_BADGES, null, null, null, null, null, null);

        while (cursor.moveToNext()) {
            Badge badge = new Badge(
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BADGE_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BADGE_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BADGE_DESCRIPTION)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BADGE_ICON_PATH)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BADGE_REQUIRED_COMPLETIONS))
            );
            badges.add(badge);
        }
        cursor.close();
        db.close();
        return badges;
    }

    public boolean update(Badge badge) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_BADGE_NAME, badge.getName());
        values.put(DatabaseHelper.COL_BADGE_DESCRIPTION, badge.getDescription());
        values.put(DatabaseHelper.COL_BADGE_ICON_PATH, badge.getIconPath());
        values.put(DatabaseHelper.COL_BADGE_REQUIRED_COMPLETIONS, badge.getRequiredCompletions());

        int rows = db.update(DatabaseHelper.TABLE_BADGES, values,
                DatabaseHelper.COL_BADGE_ID + " = ?", new String[]{badge.getId()});
        db.close();
        return rows > 0;
    }

    public boolean delete(String badgeId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete(DatabaseHelper.TABLE_BADGES,
                DatabaseHelper.COL_BADGE_ID + " = ?", new String[]{badgeId});
        db.close();
        return rows > 0;
    }
}