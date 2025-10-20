package com.example.dailyboss.data.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.dailyboss.data.DatabaseHelper;
import com.example.dailyboss.domain.model.UserEquipment;

import java.util.ArrayList;
import java.util.List;

public class UserEquipmentDao {

    private final DatabaseHelper dbHelper;
    private static final String TAG = "UserEquipmentDao";

    public UserEquipmentDao(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public boolean upsert(UserEquipment userEquipment) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_USER_EQUIPMENT_ID, userEquipment.getId());
        values.put(DatabaseHelper.COL_USER_EQUIPMENT_USER_ID, userEquipment.getUserId());
        values.put(DatabaseHelper.COL_USER_EQUIPMENT_EQUIPMENT_ID, userEquipment.getEquipmentId());
        values.put(DatabaseHelper.COL_USER_EQUIPMENT_QUANTITY, userEquipment.getQuantity());
        values.put(DatabaseHelper.COL_USER_EQUIPMENT_IS_ACTIVE, userEquipment.isActive() ? 1 : 0);
        values.put(DatabaseHelper.COL_USER_EQUIPMENT_REMAINING_DURATION_BATTLES, userEquipment.getRemainingDurationBattles());
        values.put(DatabaseHelper.COL_USER_EQUIPMENT_ACTIVATION_TIMESTAMP, userEquipment.getActivationTimestamp());
        values.put(DatabaseHelper.COL_USER_EQUIPMENT_CURRENT_BONUS_VALUE, userEquipment.getCurrentBonusValue());

        long result = db.update(DatabaseHelper.TABLE_USER_EQUIPMENT, values,
                DatabaseHelper.COL_USER_EQUIPMENT_ID + " = ?", new String[]{userEquipment.getId()});

        if (result == 0) {
            result = db.insert(DatabaseHelper.TABLE_USER_EQUIPMENT, null, values);
            Log.d(TAG, "Inserting new user equipment: " + userEquipment.getId() + ", Result: " + result);
        } else {
            Log.d(TAG, "Updating existing user equipment: " + userEquipment.getId() + ", Result: " + result);
        }
        db.close();
        return result != -1;
    }

    public UserEquipment getUserEquipment(String userEquipmentId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_USER_EQUIPMENT, null,
                DatabaseHelper.COL_USER_EQUIPMENT_ID + " = ?", new String[]{userEquipmentId},
                null, null, null);

        UserEquipment userEquipment = null;
        if (cursor.moveToFirst()) {
            userEquipment = new UserEquipment(
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EQUIPMENT_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EQUIPMENT_USER_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EQUIPMENT_EQUIPMENT_ID)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EQUIPMENT_QUANTITY)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EQUIPMENT_IS_ACTIVE)) == 1,
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EQUIPMENT_REMAINING_DURATION_BATTLES)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EQUIPMENT_ACTIVATION_TIMESTAMP)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EQUIPMENT_CURRENT_BONUS_VALUE))
            );
        }
        cursor.close();
        db.close();
        return userEquipment;
    }

    public List<UserEquipment> getUserEquipmentForUser(String userId) {
        List<UserEquipment> userEquipmentList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_USER_EQUIPMENT, null,
                DatabaseHelper.COL_USER_EQUIPMENT_USER_ID + " = ?", new String[]{userId},
                null, null, null);

        while (cursor.moveToNext()) {
            UserEquipment userEquipment = new UserEquipment(
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EQUIPMENT_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EQUIPMENT_USER_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EQUIPMENT_EQUIPMENT_ID)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EQUIPMENT_QUANTITY)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EQUIPMENT_IS_ACTIVE)) == 1,
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EQUIPMENT_REMAINING_DURATION_BATTLES)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EQUIPMENT_ACTIVATION_TIMESTAMP)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EQUIPMENT_CURRENT_BONUS_VALUE))
            );
            userEquipmentList.add(userEquipment);
        }
        cursor.close();
        db.close();
        return userEquipmentList;
    }

    public boolean deleteUserEquipment(String userEquipmentId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsAffected = db.delete(DatabaseHelper.TABLE_USER_EQUIPMENT,
                DatabaseHelper.COL_USER_EQUIPMENT_ID + " = ?", new String[]{userEquipmentId});
        db.close();
        return rowsAffected > 0;
    }
     public UserEquipment getUserSpecificEquipment(String userId, String equipmentId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_USER_EQUIPMENT, null,
                DatabaseHelper.COL_USER_EQUIPMENT_USER_ID + " = ? AND " +
                        DatabaseHelper.COL_USER_EQUIPMENT_EQUIPMENT_ID + " = ?",
                new String[]{userId, equipmentId},
                null, null, null);

        UserEquipment userEquipment = null;
        if (cursor.moveToFirst()) {
            userEquipment = new UserEquipment(
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EQUIPMENT_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EQUIPMENT_USER_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EQUIPMENT_EQUIPMENT_ID)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EQUIPMENT_QUANTITY)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EQUIPMENT_IS_ACTIVE)) == 1,
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EQUIPMENT_REMAINING_DURATION_BATTLES)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EQUIPMENT_ACTIVATION_TIMESTAMP)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EQUIPMENT_CURRENT_BONUS_VALUE))
            );
        }
        cursor.close();
        db.close();
        return userEquipment;
    }

    public List<UserEquipment> getAllUserEquipmentForUser(String userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_USER_EQUIPMENT, null,
                DatabaseHelper.COL_USER_EQUIPMENT_USER_ID + " = ?",
                new String[]{userId},
                null, null, null);

        List<UserEquipment> userEquipmentList = new ArrayList<>();
        while (cursor.moveToNext()) {
            UserEquipment userEquipment = new UserEquipment(
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EQUIPMENT_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EQUIPMENT_USER_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EQUIPMENT_EQUIPMENT_ID)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EQUIPMENT_QUANTITY)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EQUIPMENT_IS_ACTIVE)) == 1,
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EQUIPMENT_REMAINING_DURATION_BATTLES)),
                    cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EQUIPMENT_ACTIVATION_TIMESTAMP)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EQUIPMENT_CURRENT_BONUS_VALUE))
            );
            userEquipmentList.add(userEquipment);
        }
        cursor.close();
        db.close();
        return userEquipmentList;
    }
}