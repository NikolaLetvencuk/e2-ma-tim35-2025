package com.example.dailyboss.data.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.dailyboss.data.DatabaseHelper;
import com.example.dailyboss.domain.model.BattleHistory;
import com.example.dailyboss.domain.model.BossData;

import java.util.ArrayList;
import java.util.List;

public class BattleDao {
    
    private static final String TAG = "BattleDao";
    
    private final DatabaseHelper dbHelper;
    
    public BattleDao(Context context) {
        this.dbHelper = new DatabaseHelper(context);
    }

    public BossData getBossDataByLevel(int level) {
        Log.d(TAG, "Getting boss data for level: " + level);
        
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        BossData bossData = null;
        
        try {
            String[] columns = {
                DatabaseHelper.COL_BOSS_LEVEL,
                DatabaseHelper.COL_BOSS_NAME,
                DatabaseHelper.COL_BOSS_MAX_HP,
                DatabaseHelper.COL_BOSS_IMAGE_PATH,
                DatabaseHelper.COL_BOSS_CREATED_AT
            };
            
            String selection = DatabaseHelper.COL_BOSS_LEVEL + " = ?";
            String[] selectionArgs = {String.valueOf(level)};
            
            Cursor cursor = db.query(
                DatabaseHelper.TABLE_BOSS_DATA,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null
            );
            
            if (cursor.moveToFirst()) {
                bossData = new BossData();
                bossData.setLevel(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BOSS_LEVEL)));
                bossData.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BOSS_NAME)));
                bossData.setMaxHp(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BOSS_MAX_HP)));
                bossData.setImagePath(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BOSS_IMAGE_PATH)));
                bossData.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BOSS_CREATED_AT)));
                
                Log.d(TAG, "Found boss data: " + bossData);
            } else {
                Log.d(TAG, "No boss data found for level: " + level);
            }
            
            cursor.close();
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting boss data", e);
        } finally {
            db.close();
        }
        
        return bossData;
    }

    public long insertBossData(BossData bossData) {
        Log.d(TAG, "Inserting boss data: " + bossData);
        
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long result = -1;
        
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COL_BOSS_LEVEL, bossData.getLevel());
            values.put(DatabaseHelper.COL_BOSS_NAME, bossData.getName());
            values.put(DatabaseHelper.COL_BOSS_MAX_HP, bossData.getMaxHp());
            values.put(DatabaseHelper.COL_BOSS_IMAGE_PATH, bossData.getImagePath());
            values.put(DatabaseHelper.COL_BOSS_CREATED_AT, bossData.getCreatedAt());
            
            result = db.insert(DatabaseHelper.TABLE_BOSS_DATA, null, values);
            
            if (result != -1) {
                Log.d(TAG, "Boss data inserted successfully with ID: " + result);
            } else {
                Log.e(TAG, "Failed to insert boss data");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error inserting boss data", e);
        } finally {
            db.close();
        }
        
        return result;
    }

    public int updateBossData(BossData bossData) {
        Log.d(TAG, "Updating boss data: " + bossData);
        
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int result = 0;
        
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COL_BOSS_NAME, bossData.getName());
            values.put(DatabaseHelper.COL_BOSS_MAX_HP, bossData.getMaxHp());
            values.put(DatabaseHelper.COL_BOSS_IMAGE_PATH, bossData.getImagePath());
            values.put(DatabaseHelper.COL_BOSS_CREATED_AT, bossData.getCreatedAt());
            
            String whereClause = DatabaseHelper.COL_BOSS_LEVEL + " = ?";
            String[] whereArgs = {String.valueOf(bossData.getLevel())};
            
            result = db.update(DatabaseHelper.TABLE_BOSS_DATA, values, whereClause, whereArgs);
            
            if (result > 0) {
                Log.d(TAG, "Boss data updated successfully. Rows affected: " + result);
            } else {
                Log.w(TAG, "No boss data updated");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error updating boss data", e);
        } finally {
            db.close();
        }
        
        return result;
    }

    public long insertBattleHistory(BattleHistory battleHistory) {
        Log.d(TAG, "Inserting battle history: " + battleHistory);
        
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long result = -1;
        
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COL_BATTLE_USER_ID, battleHistory.getUserId());
            values.put(DatabaseHelper.COL_BATTLE_BOSS_LEVEL, battleHistory.getBossLevel());
            values.put(DatabaseHelper.COL_BATTLE_BOSS_DEFEATED, battleHistory.isBossDefeated() ? 1 : 0);
            values.put(DatabaseHelper.COL_BATTLE_COINS_WON, battleHistory.getCoinsWon());
            values.put(DatabaseHelper.COL_BATTLE_EQUIPMENT_WON, battleHistory.getEquipmentWon());
            values.put(DatabaseHelper.COL_BATTLE_ATTACKS_USED, battleHistory.getAttacksUsed());
            values.put(DatabaseHelper.COL_BATTLE_DATE, battleHistory.getBattleDate());
            
            result = db.insert(DatabaseHelper.TABLE_BATTLE_HISTORY, null, values);
            
            if (result != -1) {
                Log.d(TAG, "Battle history inserted successfully with ID: " + result);
            } else {
                Log.e(TAG, "Failed to insert battle history");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error inserting battle history", e);
        } finally {
            db.close();
        }
        
        return result;
    }

    public List<BattleHistory> getBattleHistoryByUserId(String userId) {
        Log.d(TAG, "Getting battle history for user: " + userId);
        
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<BattleHistory> battleHistoryList = new ArrayList<>();
        
        try {
            String[] columns = {
                DatabaseHelper.COL_BATTLE_ID,
                DatabaseHelper.COL_BATTLE_USER_ID,
                DatabaseHelper.COL_BATTLE_BOSS_LEVEL,
                DatabaseHelper.COL_BATTLE_BOSS_DEFEATED,
                DatabaseHelper.COL_BATTLE_COINS_WON,
                DatabaseHelper.COL_BATTLE_EQUIPMENT_WON,
                DatabaseHelper.COL_BATTLE_ATTACKS_USED,
                DatabaseHelper.COL_BATTLE_DATE
            };
            
            String selection = DatabaseHelper.COL_BATTLE_USER_ID + " = ?";
            String[] selectionArgs = {userId};
            String orderBy = DatabaseHelper.COL_BATTLE_DATE + " DESC";
            
            Cursor cursor = db.query(
                DatabaseHelper.TABLE_BATTLE_HISTORY,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                orderBy
            );
            
            while (cursor.moveToNext()) {
                BattleHistory battleHistory = new BattleHistory();
                battleHistory.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BATTLE_ID)));
                battleHistory.setUserId(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BATTLE_USER_ID)));
                battleHistory.setBossLevel(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BATTLE_BOSS_LEVEL)));
                battleHistory.setBossDefeated(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BATTLE_BOSS_DEFEATED)) == 1);
                battleHistory.setCoinsWon(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BATTLE_COINS_WON)));
                battleHistory.setEquipmentWon(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BATTLE_EQUIPMENT_WON)));
                battleHistory.setAttacksUsed(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BATTLE_ATTACKS_USED)));
                battleHistory.setBattleDate(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BATTLE_DATE)));
                
                battleHistoryList.add(battleHistory);
            }
            
            Log.d(TAG, "Found " + battleHistoryList.size() + " battle history records");
            
            cursor.close();
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting battle history", e);
        } finally {
            db.close();
        }
        
        return battleHistoryList;
    }

    public int updateBattleHistory(BattleHistory battleHistory) {
        Log.d(TAG, "Updating battle history: " + battleHistory);
        
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int result = 0;
        
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COL_BATTLE_USER_ID, battleHistory.getUserId());
            values.put(DatabaseHelper.COL_BATTLE_BOSS_LEVEL, battleHistory.getBossLevel());
            values.put(DatabaseHelper.COL_BATTLE_BOSS_DEFEATED, battleHistory.isBossDefeated() ? 1 : 0);
            values.put(DatabaseHelper.COL_BATTLE_COINS_WON, battleHistory.getCoinsWon());
            values.put(DatabaseHelper.COL_BATTLE_EQUIPMENT_WON, battleHistory.getEquipmentWon());
            values.put(DatabaseHelper.COL_BATTLE_ATTACKS_USED, battleHistory.getAttacksUsed());
            values.put(DatabaseHelper.COL_BATTLE_DATE, battleHistory.getBattleDate());
            
            String whereClause = DatabaseHelper.COL_BATTLE_ID + " = ?";
            String[] whereArgs = {String.valueOf(battleHistory.getId())};
            
            result = db.update(DatabaseHelper.TABLE_BATTLE_HISTORY, values, whereClause, whereArgs);
            
            if (result > 0) {
                Log.d(TAG, "Battle history updated successfully. Rows affected: " + result);
            } else {
                Log.w(TAG, "No battle history updated");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error updating battle history", e);
        } finally {
            db.close();
        }
        
        return result;
    }

    public int deleteBattleHistory(long battleId) {
        Log.d(TAG, "Deleting battle history with ID: " + battleId);
        
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int result = 0;
        
        try {
            String whereClause = DatabaseHelper.COL_BATTLE_ID + " = ?";
            String[] whereArgs = {String.valueOf(battleId)};
            
            result = db.delete(DatabaseHelper.TABLE_BATTLE_HISTORY, whereClause, whereArgs);
            
            if (result > 0) {
                Log.d(TAG, "Battle history deleted successfully. Rows affected: " + result);
            } else {
                Log.w(TAG, "No battle history deleted");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error deleting battle history", e);
        } finally {
            db.close();
        }
        
        return result;
    }
}

