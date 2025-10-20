package com.example.dailyboss.data.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.dailyboss.data.DatabaseHelper;
import com.example.dailyboss.domain.model.Alliance;

import java.util.Date;

public class AllianceDao {

    private final DatabaseHelper dbHelper;
    private static final String TAG = "SQLiteAllianceDao";

    public AllianceDao(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    private Alliance cursorToAlliance(Cursor cursor) {
        Alliance alliance = new Alliance(
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ALLIANCE_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ALLIANCE_NAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ALLIANCE_LEADER_ID)),
                new Date(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ALLIANCE_CREATED_AT))),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ALLIANCE_SPECIAL_MISSION_ID))
        );
        alliance.setMissionActive(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ALLIANCE_MISSION_ACTIVE)) > 0);
        alliance.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ALLIANCE_STATUS)));

        return alliance;
    }

    private ContentValues allianceToContentValues(Alliance alliance) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_ALLIANCE_ID, alliance.getId());
        values.put(DatabaseHelper.COL_ALLIANCE_NAME, alliance.getName());
        values.put(DatabaseHelper.COL_ALLIANCE_LEADER_ID, alliance.getLeaderId());
        values.put(DatabaseHelper.COL_ALLIANCE_CREATED_AT, alliance.getCreatedAt().getTime());
        values.put(DatabaseHelper.COL_ALLIANCE_MISSION_ACTIVE, alliance.isMissionActive() ? 1 : 0);
        values.put(DatabaseHelper.COL_ALLIANCE_STATUS, alliance.getStatus());
        values.put(DatabaseHelper.COL_ALLIANCE_SPECIAL_MISSION_ID, alliance.getActiveSpecialMissionId());
        return values;
    }

    public long insert(Alliance alliance) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = allianceToContentValues(alliance);

        long result = db.insert(DatabaseHelper.TABLE_ALLIANCES, null, values);
        Log.d(TAG, "Inserting new alliance: " + alliance.getId() + ", Result: " + result);
        return result;
    }

    public long updateActiveSpecialMissionId(String allianceId, String missionId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("activeSpecialMissionId", missionId);

        long result = db.update(DatabaseHelper.TABLE_ALLIANCES, values, "id = ?", new String[]{allianceId});
        db.close();
        Log.d("AllianceDao", "Updated activeSpecialMissionId for alliance " + allianceId + " to " + missionId + ", rows affected: " + result);
        return result;
    }

    public Alliance getAllianceById(String allianceId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Alliance alliance = null;

        Cursor cursor = db.query(DatabaseHelper.TABLE_ALLIANCES,
                null,
                DatabaseHelper.COL_ALLIANCE_ID + " = ?",
                new String[]{allianceId},
                null, null, null);

        if (cursor.moveToFirst()) {
            alliance = cursorToAlliance(cursor);
        }
        cursor.close();
        return alliance;
    }

    public boolean update(Alliance alliance) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = allianceToContentValues(alliance);

        values.remove(DatabaseHelper.COL_ALLIANCE_ID);

        int rowsAffected = db.update(DatabaseHelper.TABLE_ALLIANCES, values,
                DatabaseHelper.COL_ALLIANCE_ID + " = ?", new String[]{alliance.getId()});

        Log.d(TAG, "Updating alliance: " + alliance.getId() + ", Rows affected: " + rowsAffected);
        return rowsAffected > 0;
    }

    public int delete(String allianceId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsDeleted = db.delete(DatabaseHelper.TABLE_ALLIANCES,
                DatabaseHelper.COL_ALLIANCE_ID + " = ?",
                new String[]{allianceId});
        Log.d(TAG, "Deleting alliance: " + allianceId + ", Rows deleted: " + rowsDeleted);
        return rowsDeleted;
    }
}