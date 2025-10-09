package com.example.dailyboss.data.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.dailyboss.data.DatabaseHelper;
import com.example.dailyboss.domain.model.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryDao {

    private final DatabaseHelper dbHelper;
    public CategoryDao(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public boolean insert(Category category) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_ID, category.getId());
        values.put(DatabaseHelper.COL_NAME, category.getName());
        values.put(DatabaseHelper.COL_COLOR, category.getColor());
        values.put(DatabaseHelper.COL_CATEGORY_USER_ID, category.getUserId());

        android.util.Log.d("CategoryService",
                "Dodajem kategoriju: ID=" + category.getId() + ", Name=" + category.getName() + ", Color=" + category.getColor() + values);
        long result = db.insert(DatabaseHelper.TABLE_CATEGORIES, null, values);
        return  result != -1;
    }

    public List<Category> getAll() {
        List<Category> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_CATEGORIES,
                null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                String id = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NAME));
                String color = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_COLOR));
                String userId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CATEGORY_USER_ID));

                list.add(new Category(id, color, name, userId));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return  list;
    }
    
    // 🆕 Novi metod: Uzmi samo kategorije za određenog korisnika
    public List<Category> getAllByUserId(String userId) {
        List<Category> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_CATEGORIES,
                null, 
                DatabaseHelper.COL_CATEGORY_USER_ID + " = ?",
                new String[]{userId},
                null, null, null);

        if (cursor.moveToFirst()) {
            do {
                String id = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NAME));
                String color = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_COLOR));
                String userIdFromDb = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CATEGORY_USER_ID));

                list.add(new Category(id, color, name, userIdFromDb));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return  list;
    }

    public boolean existsColor(String color, String userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_CATEGORIES,
                new String[]{DatabaseHelper.COL_ID, DatabaseHelper.COL_COLOR},
                DatabaseHelper.COL_COLOR + " = ? AND " + DatabaseHelper.COL_CATEGORY_USER_ID + " = ?",
                new String[]{color, userId},
                null, null, null);

        boolean exists = cursor.moveToFirst();

        cursor.close();
        return exists;
    }

    public List<String> getAllColors(String userId) {
        List<String> colors = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_CATEGORIES,
                new String[]{DatabaseHelper.COL_COLOR},
                DatabaseHelper.COL_CATEGORY_USER_ID + " = ?",
                new String[]{userId},
                null, null, null);

        while (cursor.moveToNext()) {
            colors.add(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_COLOR)));
        }

        cursor.close();
        return colors;
    }

    public boolean updateColor(String id, String newColor) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_COLOR, newColor);

        int updated = db.update(DatabaseHelper.TABLE_CATEGORIES, values,
                DatabaseHelper.COL_ID + " = ?", new String[]{id});
        return  updated > 0;
    }

    public String getColorById(String id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String color = null;

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_CATEGORIES,
                new String[]{DatabaseHelper.COL_COLOR},
                DatabaseHelper.COL_ID + " = ?",
                new String[]{id},
                null,
                null,
                null
        );

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                color = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_COLOR));
            }
            cursor.close();
        }
        return color;
    }
}
