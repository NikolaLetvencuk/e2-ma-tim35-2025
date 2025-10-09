package com.example.dailyboss.data.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.dailyboss.data.DatabaseHelper;
import com.example.dailyboss.domain.model.Equipment;

import java.util.ArrayList;
import java.util.List;

public class EquipmentDao {

    private final DatabaseHelper dbHelper;
    private static final String TAG = "EquipmentDao";

    public EquipmentDao(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public boolean upsert(Equipment equipment) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_EQUIPMENT_ID, equipment.getId());
        values.put(DatabaseHelper.COL_EQUIPMENT_NAME, equipment.getName());
        values.put(DatabaseHelper.COL_EQUIPMENT_DESCRIPTION, equipment.getDescription());
        values.put(DatabaseHelper.COL_EQUIPMENT_ICON_PATH, equipment.getIconPath());
        values.put(DatabaseHelper.COL_EQUIPMENT_TYPE, equipment.getType());
        values.put(DatabaseHelper.COL_EQUIPMENT_BONUS_TYPE, equipment.getBonusType());
        values.put(DatabaseHelper.COL_EQUIPMENT_BONUS_VALUE, equipment.getBonusValue());
        values.put(DatabaseHelper.COL_EQUIPMENT_DURATION_BATTLES, equipment.getDurationBattles());
        values.put(DatabaseHelper.COL_EQUIPMENT_DURATION_DAYS, equipment.getDurationDays());
        values.put(DatabaseHelper.COL_EQUIPMENT_BASE_PRICE_COINS, equipment.getBasePriceCoins());
        values.put(DatabaseHelper.COL_EQUIPMENT_IS_CONSUMABLE, equipment.isConsumable() ? 1 : 0);
        values.put(DatabaseHelper.COL_EQUIPMENT_IS_STACKABLE, equipment.isStackable() ? 1 : 0);

        long result = db.update(DatabaseHelper.TABLE_EQUIPMENT, values,
                DatabaseHelper.COL_EQUIPMENT_ID + " = ?", new String[]{equipment.getId()});

        if (result == 0) {
            result = db.insert(DatabaseHelper.TABLE_EQUIPMENT, null, values);
            Log.d(TAG, "Inserting new equipment: " + equipment.getName() + ", Result: " + result);
        } else {
            Log.d(TAG, "Updating existing equipment: " + equipment.getName() + ", Result: " + result);
        }
        db.close();
        return result != -1;
    }

    public Equipment getEquipment(String equipmentId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_EQUIPMENT, null,
                DatabaseHelper.COL_EQUIPMENT_ID + " = ?", new String[]{equipmentId},
                null, null, null);

        Equipment equipment = null;
        if (cursor.moveToFirst()) {
            equipment = new Equipment(
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EQUIPMENT_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EQUIPMENT_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EQUIPMENT_DESCRIPTION)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EQUIPMENT_ICON_PATH)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EQUIPMENT_TYPE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EQUIPMENT_BONUS_TYPE)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EQUIPMENT_BONUS_VALUE)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EQUIPMENT_DURATION_BATTLES)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EQUIPMENT_DURATION_DAYS)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EQUIPMENT_BASE_PRICE_COINS)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EQUIPMENT_IS_CONSUMABLE)) == 1,
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EQUIPMENT_IS_STACKABLE)) == 1
            );
        }
        cursor.close();
        db.close();
        return equipment;
    }

    public List<Equipment> getAllEquipment() {
        List<Equipment> equipmentList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_EQUIPMENT, null, null, null, null, null, null);

        while (cursor.moveToNext()) {
            Equipment equipment = new Equipment(
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EQUIPMENT_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EQUIPMENT_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EQUIPMENT_DESCRIPTION)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EQUIPMENT_ICON_PATH)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EQUIPMENT_TYPE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EQUIPMENT_BONUS_TYPE)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EQUIPMENT_BONUS_VALUE)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EQUIPMENT_DURATION_BATTLES)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EQUIPMENT_DURATION_DAYS)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EQUIPMENT_BASE_PRICE_COINS)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EQUIPMENT_IS_CONSUMABLE)) == 1,
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EQUIPMENT_IS_STACKABLE)) == 1
            );
            equipmentList.add(equipment);
        }
        cursor.close();
        db.close();
        return equipmentList;
    }

    public boolean deleteEquipment(String equipmentId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsAffected = db.delete(DatabaseHelper.TABLE_EQUIPMENT,
                DatabaseHelper.COL_EQUIPMENT_ID + " = ?", new String[]{equipmentId});
        db.close();
        return rowsAffected > 0;
    }
}