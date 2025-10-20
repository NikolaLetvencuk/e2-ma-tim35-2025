package com.example.dailyboss.data.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.dailyboss.data.DatabaseHelper;
import com.example.dailyboss.domain.model.MissionActivityLog;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MissionActivityLogDao {

    private final DatabaseHelper dbHelper;
    private static final String TAG = "MissionActivityLogDao";

    public MissionActivityLogDao(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    private MissionActivityLog cursorToMissionActivityLog(Cursor cursor) {
        return new MissionActivityLog(
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MISSION_LOG_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MISSION_LOG_SPECIAL_MISSION_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MISSION_LOG_USER_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MISSION_LOG_USERNAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MISSION_LOG_ACTIVITY_DESCRIPTION)),
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MISSION_LOG_DAMAGE_DEALT)),
                new Date(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MISSION_LOG_TIMESTAMP)))
        );
    }

    private ContentValues missionActivityLogToContentValues(MissionActivityLog log) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_MISSION_LOG_ID, log.getId());
        values.put(DatabaseHelper.COL_MISSION_LOG_SPECIAL_MISSION_ID, log.getSpecialMissionId());
        values.put(DatabaseHelper.COL_MISSION_LOG_USER_ID, log.getUserId());
        values.put(DatabaseHelper.COL_MISSION_LOG_USERNAME, log.getUsername());
        values.put(DatabaseHelper.COL_MISSION_LOG_ACTIVITY_DESCRIPTION, log.getActivityDescription());
        values.put(DatabaseHelper.COL_MISSION_LOG_DAMAGE_DEALT, log.getDamageDealt());
        values.put(DatabaseHelper.COL_MISSION_LOG_TIMESTAMP, log.getTimestamp().getTime());
        return values;
    }

    public long insert(MissionActivityLog log) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = missionActivityLogToContentValues(log);
        long result = db.insert(DatabaseHelper.TABLE_MISSION_ACTIVITY_LOG, null, values);
        Log.d(TAG, "Inserting new MissionActivityLog: " + log.getId() + ", Result: " + result);
        return result;
    }

    public MissionActivityLog getMissionActivityLogById(String logId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        MissionActivityLog log = null;

        Cursor cursor = db.query(DatabaseHelper.TABLE_MISSION_ACTIVITY_LOG,
                null,
                DatabaseHelper.COL_MISSION_LOG_ID + " = ?",
                new String[]{logId},
                null, null, null);

        if (cursor.moveToFirst()) {
            log = cursorToMissionActivityLog(cursor);
        }
        cursor.close();
        return log;
    }

    public List<MissionActivityLog> getLogsForSpecialMission(String specialMissionId) {
        List<MissionActivityLog> logs = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(DatabaseHelper.TABLE_MISSION_ACTIVITY_LOG,
                null,
                DatabaseHelper.COL_MISSION_LOG_SPECIAL_MISSION_ID + " = ?",
                new String[]{specialMissionId},
                null, null, DatabaseHelper.COL_MISSION_LOG_TIMESTAMP + " DESC"); // Najnoviji prvi

        if (cursor.moveToFirst()) {
            do {
                logs.add(cursorToMissionActivityLog(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return logs;
    }

    public boolean update(MissionActivityLog log) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = missionActivityLogToContentValues(log);
        values.remove(DatabaseHelper.COL_MISSION_LOG_ID); // ID se ne ažurira

        int rowsAffected = db.update(DatabaseHelper.TABLE_MISSION_ACTIVITY_LOG, values,
                DatabaseHelper.COL_MISSION_LOG_ID + " = ?", new String[]{log.getId()});

        Log.d(TAG, "Updating MissionActivityLog: " + log.getId() + ", Rows affected: " + rowsAffected);
        return rowsAffected > 0;
    }

    public int delete(String logId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsDeleted = db.delete(DatabaseHelper.TABLE_MISSION_ACTIVITY_LOG,
                DatabaseHelper.COL_MISSION_LOG_ID + " = ?",
                new String[]{logId});
        Log.d(TAG, "Deleting MissionActivityLog: " + logId + ", Rows deleted: " + rowsDeleted);
        return rowsDeleted;
    }
}