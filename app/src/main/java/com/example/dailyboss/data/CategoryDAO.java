package com.example.dailyboss.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.dailyboss.model.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {

    private final DatabaseHelper dbHelper;
    public CategoryDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public boolean insert(Category category) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_ID, category.getId());
        values.put(DatabaseHelper.COL_NAME, category.getName());
        values.put(DatabaseHelper.COL_COLOR, category.getColor());
        android.util.Log.d("CategoryService",
                "Dodajem kategoriju: ID=" + category.getId() + ", Name=" + category.getName() + ", Color=" + category.getColor() + values);
        long result = db.insert(DatabaseHelper.TABLE_CATEGORIES, null, values);
        db.close();
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

                list.add(new Category(id, color, name));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return  list;
    }

    public boolean existsColor(String color) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_CATEGORIES,
                new String[]{DatabaseHelper.COL_ID, DatabaseHelper.COL_COLOR}, // dodajemo i COLOR da možemo ispisati
                DatabaseHelper.COL_COLOR + " = ?",
                new String[]{color},
                null, null, null);

        boolean exists = cursor.moveToFirst();

        cursor.close();
        db.close();
        return exists;
    }

    public List<String> getAllColors() {
        List<String> colors = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_CATEGORIES,
                new String[]{DatabaseHelper.COL_COLOR},
                null, null, null, null, null);

        while (cursor.moveToNext()) {
            colors.add(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_COLOR)));
        }

        cursor.close();
        db.close();
        return colors;
    }

    public boolean updateColor(String id, String newColor) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_COLOR, newColor);

        int updated = db.update(DatabaseHelper.TABLE_CATEGORIES, values,
                DatabaseHelper.COL_ID + " = ?", new String[]{id});
        db.close();
        return  updated > 0;
    }
}
