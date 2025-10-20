package com.example.dailyboss.data.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.dailyboss.data.DatabaseHelper;
import com.example.dailyboss.domain.model.UserMissionProgress;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserMissionProgressDao {

    private final DatabaseHelper dbHelper;
    private static final String TAG = "UserMissionProgressDao";

    public UserMissionProgressDao(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    private UserMissionProgress cursorToUserMissionProgress(Cursor cursor) {
        UserMissionProgress progress = new UserMissionProgress(
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_UMP_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_UMP_SPECIAL_MISSION_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_UMP_USER_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_UMP_USERNAME))
        );

        progress.setBuyInShopCount(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_UMP_BUY_IN_SHOP_COUNT)));
        progress.setRegularBossHitCount(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_UMP_REGULAR_BOSS_HIT_COUNT)));
        progress.setEasyNormalImportantTaskCount(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_UMP_EASY_NORMAL_IMPORTANT_TASK_COUNT)));
        progress.setOtherTasksCount(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_UMP_OTHER_TASKS_COUNT)));
        progress.setNoUnresolvedTasksCompleted(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_UMP_NO_UNRESOLVED_TASKS_COMPLETED)) > 0);

        long lastMessageSentTime = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_UMP_LAST_MESSAGE_SENT_DATE));
        if (lastMessageSentTime != 0) { // Ako je timestamp 0, znači da nije postavljen (null)
            progress.setLastMessageSentDate(new Date(lastMessageSentTime));
        } else {
            progress.setLastMessageSentDate(null);
        }

        // NOVO: Čitanje vrednosti za messageSentDaysCount
        int messageSentDaysCount = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_UMP_MESSAGE_SENT_DAYS_COUNT));
        progress.setMessageSentDaysCount(messageSentDaysCount);


        return progress;
    }

    private ContentValues userMissionProgressToContentValues(UserMissionProgress progress) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_UMP_ID, progress.getId());
        values.put(DatabaseHelper.COL_UMP_SPECIAL_MISSION_ID, progress.getSpecialMissionId());
        values.put(DatabaseHelper.COL_UMP_USER_ID, progress.getUserId());
        values.put(DatabaseHelper.COL_UMP_USERNAME, progress.getUsername());
        values.put(DatabaseHelper.COL_UMP_BUY_IN_SHOP_COUNT, progress.getBuyInShopCount());
        values.put(DatabaseHelper.COL_UMP_REGULAR_BOSS_HIT_COUNT, progress.getRegularBossHitCount());
        values.put(DatabaseHelper.COL_UMP_EASY_NORMAL_IMPORTANT_TASK_COUNT, progress.getEasyNormalImportantTaskCount());
        values.put(DatabaseHelper.COL_UMP_OTHER_TASKS_COUNT, progress.getOtherTasksCount());
        values.put(DatabaseHelper.COL_UMP_NO_UNRESOLVED_TASKS_COMPLETED, progress.isNoUnresolvedTasksCompleted() ? 1 : 0);
        values.put(DatabaseHelper.COL_UMP_LAST_MESSAGE_SENT_DATE, progress.getLastMessageSentDate() != null ? progress.getLastMessageSentDate().getTime() : 0);
        values.put(DatabaseHelper.COL_UMP_MESSAGE_SENT_DAYS_COUNT, progress.getMessageSentDaysCount()); // NOVO: Upisivanje vrednosti za messageSentDaysCount
        return values;
    }

    public long insert(UserMissionProgress progress) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = userMissionProgressToContentValues(progress);
        long result = db.insert(DatabaseHelper.TABLE_USER_MISSION_PROGRESS, null, values);
        Log.d(TAG, "Inserting new UserMissionProgress: " + progress.getId() + ", Result: " + result);
        return result;
    }

    public UserMissionProgress getUserMissionProgressById(String progressId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        UserMissionProgress progress = null;

        Cursor cursor = db.query(DatabaseHelper.TABLE_USER_MISSION_PROGRESS,
                null,
                DatabaseHelper.COL_UMP_ID + " = ?",
                new String[]{progressId},
                null, null, null);

        if (cursor.moveToFirst()) {
            progress = cursorToUserMissionProgress(cursor);
        }
        cursor.close();
        return progress;
    }

    public UserMissionProgress getUserMissionProgressForUserAndMission(String userId, String specialMissionId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        UserMissionProgress progress = null;

        Cursor cursor = db.query(DatabaseHelper.TABLE_USER_MISSION_PROGRESS,
                null,
                DatabaseHelper.COL_UMP_USER_ID + " = ? AND " + DatabaseHelper.COL_UMP_SPECIAL_MISSION_ID + " = ?",
                new String[]{userId, specialMissionId},
                null, null, null);

        if (cursor.moveToFirst()) {
            progress = cursorToUserMissionProgress(cursor);
        }
        cursor.close();
        return progress;
    }

    public List<UserMissionProgress> getAllUserProgressForMission(String specialMissionId) {
        List<UserMissionProgress> progresses = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(DatabaseHelper.TABLE_USER_MISSION_PROGRESS,
                null,
                DatabaseHelper.COL_UMP_SPECIAL_MISSION_ID + " = ?",
                new String[]{specialMissionId},
                null, null, null);

        if (cursor.moveToFirst()) {
            do {
                progresses.add(cursorToUserMissionProgress(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return progresses;
    }

    public boolean update(UserMissionProgress progress) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = userMissionProgressToContentValues(progress);
        values.remove(DatabaseHelper.COL_UMP_ID); // ID se ne ažurira

        int rowsAffected = db.update(DatabaseHelper.TABLE_USER_MISSION_PROGRESS, values,
                DatabaseHelper.COL_UMP_ID + " = ?", new String[]{progress.getId()});

        Log.d(TAG, "Updating UserMissionProgress: " + progress.getId() + ", Rows affected: " + rowsAffected);
        return rowsAffected > 0;
    }

    public int delete(String progressId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsDeleted = db.delete(DatabaseHelper.TABLE_USER_MISSION_PROGRESS,
                DatabaseHelper.COL_UMP_ID + " = ?",
                new String[]{progressId});
        Log.d(TAG, "Deleting UserMissionProgress: " + progressId + ", Rows deleted: " + rowsDeleted);
        return rowsDeleted;
    }
}