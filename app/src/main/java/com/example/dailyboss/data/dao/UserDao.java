package com.example.dailyboss.data.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.dailyboss.data.DatabaseHelper;
import com.example.dailyboss.domain.model.User;

import java.util.ArrayList;
import java.util.List;

public class UserDao {

    private final DatabaseHelper dbHelper;
    private static final String TAG = "UserDao";

    public UserDao(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // ✨ MODIFIKOVANO: Metoda sada pokušava da ubaci ili ažurira korisnika
    public boolean upsert(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_USER_ID, user.getId());
        values.put(DatabaseHelper.COL_USER_USERNAME, user.getUsername());
        values.put(DatabaseHelper.COL_USER_EMAIL, user.getEmail());
        values.put(DatabaseHelper.COL_USER_PASSWORD, user.getPassword()); // Podsjetnik: Lozinke ne bi trebale biti ovde
        values.put(DatabaseHelper.COL_USER_AVATAR, user.getAvatar());
        values.put(DatabaseHelper.COL_USER_IS_ACTIVE, user.isActive() ? 1 : 0);
        values.put(DatabaseHelper.COL_USER_REG_TIMESTAMP, user.getRegistrationTimestamp());
        values.put(DatabaseHelper.COL_USER_ALLIANCE_ID, user.getAllianceId());
        long result = db.update(DatabaseHelper.TABLE_USERS, values,
                DatabaseHelper.COL_USER_ID + " = ?", new String[]{user.getId()});

        if (result == 0) { // Ako nijedan red nije ažuriran, to znači da korisnik ne postoji, pa ga ubacujemo
            result = db.insert(DatabaseHelper.TABLE_USERS, null, values);
            Log.d(TAG, "Inserting new user: " + user.getUsername() + ", Result: " + result);
        } else {
            Log.d(TAG, "Updating existing user: " + user.getUsername() + ", Result: " + result);
        }
        return result != -1;
    }

    // Prethodna insert metoda (može se ukloniti ili ostaviti ako imate specifične scenarije)
    public boolean insert(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_USER_ID, user.getId());
        values.put(DatabaseHelper.COL_USER_USERNAME, user.getUsername());
        values.put(DatabaseHelper.COL_USER_EMAIL, user.getEmail());
        values.put(DatabaseHelper.COL_USER_PASSWORD, user.getPassword());
        values.put(DatabaseHelper.COL_USER_AVATAR, user.getAvatar());
        values.put(DatabaseHelper.COL_USER_IS_ACTIVE, user.isActive() ? 1 : 0);
        values.put(DatabaseHelper.COL_USER_REG_TIMESTAMP, user.getRegistrationTimestamp());
        values.put(DatabaseHelper.COL_USER_ALLIANCE_ID, user.getAllianceId());
        long result = db.insert(DatabaseHelper.TABLE_USERS, null, values);
        return result != -1;
    }


    // ✨ NOVO: Dohvatanje korisnika po UID-u
    public User getUser(String userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS, null,
                DatabaseHelper.COL_USER_ID + " = ?", new String[]{userId},
                null, null, null);

        User user = null;
        if (cursor.moveToFirst()) {
            user = new User(
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_USERNAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EMAIL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_PASSWORD)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_AVATAR)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_IS_ACTIVE)) == 1,
                    cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_REG_TIMESTAMP)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ALLIANCE_ID))
            );
        }
        cursor.close();
        return user;
    }

    // Prethodna metoda za dohvatanje po username (ostaje)
    public User getUserByUsername(String username) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS, null,
                DatabaseHelper.COL_USER_USERNAME + " = ?", new String[]{username},
                null, null, null);

        User user = null;
        if (cursor.moveToFirst()) {
            user = new User(
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_USERNAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EMAIL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_PASSWORD)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_AVATAR)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_IS_ACTIVE)) == 1,
                    cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_REG_TIMESTAMP)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ALLIANCE_ID))
            );
        }
        cursor.close();
        return user;
    }

    // ✨ NOVO: Metoda za ažuriranje postojećeg korisnika
    public boolean update(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        // UID ne ažuriramo, on je PK
        values.put(DatabaseHelper.COL_USER_USERNAME, user.getUsername());
        values.put(DatabaseHelper.COL_USER_EMAIL, user.getEmail());
        values.put(DatabaseHelper.COL_USER_PASSWORD, user.getPassword());
        values.put(DatabaseHelper.COL_USER_AVATAR, user.getAvatar());
        values.put(DatabaseHelper.COL_USER_IS_ACTIVE, user.isActive() ? 1 : 0);
        values.put(DatabaseHelper.COL_USER_REG_TIMESTAMP, user.getRegistrationTimestamp());
        values.put(DatabaseHelper.COL_USER_ALLIANCE_ID, user.getAllianceId());
        int rowsAffected = db.update(DatabaseHelper.TABLE_USERS, values,
                DatabaseHelper.COL_USER_ID + " = ?", new String[]{user.getId()});
        return rowsAffected > 0;
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS, null, null, null, null, null, null);

        while (cursor.moveToNext()) {
            User user = new User(
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_USERNAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EMAIL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_PASSWORD)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_AVATAR)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_IS_ACTIVE)) == 1,
                    cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_REG_TIMESTAMP)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ALLIANCE_ID))
            );
            users.add(user);
        }

        cursor.close();
        return users;
    }

    public List<User> searchUsersByUsername(String query) {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] selectionArgs = new String[]{"%" + query + "%"};

         String selection = DatabaseHelper.COL_USER_USERNAME + " LIKE ? COLLATE NOCASE";

        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS, null, selection, selectionArgs, null, null, null);

        while (cursor.moveToNext()) {
            User user = new User(
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_USERNAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EMAIL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_PASSWORD)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_AVATAR)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_IS_ACTIVE)) == 1,
                    cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_REG_TIMESTAMP)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ALLIANCE_ID))
            );
            users.add(user);
        }

        cursor.close();
        return users;
    }

    public boolean updateAllianceId(String userId, String allianceId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_USER_ALLIANCE_ID, allianceId != null ? allianceId : "");

        int rowsAffected = db.update(DatabaseHelper.TABLE_USERS, values,
                DatabaseHelper.COL_USER_ID + " = ?", new String[]{userId});
        return rowsAffected > 0;
    }

    public List<User> getUsersByAllianceId(String allianceId) {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DatabaseHelper.COL_USER_ALLIANCE_ID + " = ?";
        String[] selectionArgs = new String[]{allianceId};

        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS, null, selection, selectionArgs, null, null, null);

        while (cursor.moveToNext()) {
            User user = new User(
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_USERNAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EMAIL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_PASSWORD)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_AVATAR)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_IS_ACTIVE)) == 1,
                    cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_REG_TIMESTAMP)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ALLIANCE_ID))
            );
            users.add(user);
        }
        cursor.close();
        return users;
    }
}