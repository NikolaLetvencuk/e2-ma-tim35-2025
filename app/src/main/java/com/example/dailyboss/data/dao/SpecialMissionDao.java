package com.example.dailyboss.data.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.dailyboss.data.DatabaseHelper;
import com.example.dailyboss.domain.model.SpecialMission;

import java.util.Date;
import java.util.List; // Ostavljam List import ako ti zatreba za getAll

public class SpecialMissionDao {

    private final DatabaseHelper dbHelper;
    private static final String TAG = "SpecialMissionDao";

    public SpecialMissionDao(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    private SpecialMission cursorToSpecialMission(Cursor cursor) {
        SpecialMission mission = new SpecialMission(
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SM_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SM_ALLIANCE_ID)),
                cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SM_NUM_PARTICIPATING_MEMBERS)),
                new Date(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SM_START_TIME))),
                false
        );

        mission.setEndTime(new Date(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SM_END_TIME))));
        mission.setTotalBossHp(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SM_TOTAL_BOSS_HP)));
        mission.setCurrentBossHp(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SM_CURRENT_BOSS_HP)));
        mission.setCompletedSuccessfully(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SM_COMPLETED_SUCCESSFULLY)) > 0);
        mission.setActive(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SM_IS_ACTIVE)) > 0);
        mission.setRewardsAwarded(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SM_REWARD_AWARDED)) > 0);
        // membersTotalDamageDealt je UKLONJEN, tako da ovde nema više logike za mapu

        return mission;
    }

    private ContentValues specialMissionToContentValues(SpecialMission mission) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_SM_ID, mission.getId());
        values.put(DatabaseHelper.COL_SM_ALLIANCE_ID, mission.getAllianceId());
        values.put(DatabaseHelper.COL_SM_START_TIME, mission.getStartTime().getTime());
        values.put(DatabaseHelper.COL_SM_END_TIME, mission.getEndTime().getTime());
        values.put(DatabaseHelper.COL_SM_TOTAL_BOSS_HP, mission.getTotalBossHp());
        values.put(DatabaseHelper.COL_SM_CURRENT_BOSS_HP, mission.getCurrentBossHp());
        values.put(DatabaseHelper.COL_SM_COMPLETED_SUCCESSFULLY, mission.isCompletedSuccessfully() ? 1 : 0);
        values.put(DatabaseHelper.COL_SM_IS_ACTIVE, mission.isActive() ? 1 : 0);
        values.put(DatabaseHelper.COL_SM_NUM_PARTICIPATING_MEMBERS, mission.getNumberOfParticipatingMembers());
        values.put(DatabaseHelper.COL_SM_REWARD_AWARDED, mission.isRewardsAwarded() ? 1 : 0);
        // membersTotalDamageDealt je UKLONJEN, tako da ovde nema više serijalizacije mape
        return values;
    }

    public long insert(SpecialMission mission) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = specialMissionToContentValues(mission);
        long result = db.insert(DatabaseHelper.TABLE_SPECIAL_MISSIONS, null, values);
        Log.d(TAG, "Inserting new SpecialMission: " + mission.getId() + ", Result: " + result);
        return result;
    }

    public SpecialMission getSpecialMissionById(String missionId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        SpecialMission mission = null;

        Cursor cursor = db.query(DatabaseHelper.TABLE_SPECIAL_MISSIONS,
                null,
                DatabaseHelper.COL_SM_ID + " = ?",
                new String[]{missionId},
                null, null, null);

        if (cursor.moveToFirst()) {
            mission = cursorToSpecialMission(cursor);
        }
        cursor.close();
        return mission;
    }

    public SpecialMission getActiveSpecialMissionForAlliance(String allianceId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        SpecialMission mission = null;

        Cursor cursor = db.query(DatabaseHelper.TABLE_SPECIAL_MISSIONS,
                null,
                DatabaseHelper.COL_SM_ALLIANCE_ID + " = ? AND " + DatabaseHelper.COL_SM_IS_ACTIVE + " = 1",
                new String[]{allianceId},
                null, null, null);

        if (cursor.moveToFirst()) {
            mission = cursorToSpecialMission(cursor);
        }
        cursor.close();
        return mission;
    }

    public boolean update(SpecialMission mission) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = specialMissionToContentValues(mission);
        values.remove(DatabaseHelper.COL_SM_ID); // ID se ne ažurira

        int rowsAffected = db.update(DatabaseHelper.TABLE_SPECIAL_MISSIONS, values,
                DatabaseHelper.COL_SM_ID + " = ?", new String[]{mission.getId()});

        Log.d(TAG, "Updating SpecialMission: " + mission.getId() + ", Rows affected: " + rowsAffected);
        return rowsAffected > 0;
    }

    public int delete(String missionId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsDeleted = db.delete(DatabaseHelper.TABLE_SPECIAL_MISSIONS,
                DatabaseHelper.COL_SM_ID + " = ?",
                new String[]{missionId});
        Log.d(TAG, "Deleting SpecialMission: " + missionId + ", Rows deleted: " + rowsDeleted);
        return rowsDeleted;
    }
}