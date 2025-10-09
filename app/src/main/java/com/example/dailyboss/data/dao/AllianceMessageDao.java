package com.example.dailyboss.data.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.dailyboss.data.DatabaseHelper;
import com.example.dailyboss.domain.model.AllianceMessage;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AllianceMessageDao {

    private final DatabaseHelper dbHelper;
    private static final String TAG = "SQLiteAllianceMsgDao";

    public AllianceMessageDao(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    private AllianceMessage cursorToMessage(Cursor cursor) {
        return new AllianceMessage(
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MESSAGE_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MESSAGE_ALLIANCE_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MESSAGE_SENDER_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MESSAGE_SENDER_USERNAME)), // READ NEW FIELD
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MESSAGE_CONTENT)),
                new Date(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_MESSAGE_TIMESTAMP)))
        );
    }

    private ContentValues messageToContentValues(AllianceMessage message) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_MESSAGE_ID, message.getId());
        values.put(DatabaseHelper.COL_MESSAGE_ALLIANCE_ID, message.getAllianceId());
        values.put(DatabaseHelper.COL_MESSAGE_SENDER_ID, message.getSenderId());
        values.put(DatabaseHelper.COL_MESSAGE_SENDER_USERNAME, message.getSenderUsername()); // WRITE NEW FIELD
        values.put(DatabaseHelper.COL_MESSAGE_CONTENT, message.getContent());
        values.put(DatabaseHelper.COL_MESSAGE_TIMESTAMP, message.getTimestamp().getTime());
        return values;
    }

    // 1. Убацивање нове поруке
    public long insert(AllianceMessage message) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = messageToContentValues(message);

        long result = db.insertWithOnConflict(DatabaseHelper.TABLE_ALLIANCE_MESSAGES, null, values, SQLiteDatabase.CONFLICT_REPLACE); // Use CONFLICT_REPLACE to handle updates if message already exists
        Log.d(TAG, "Inserting message: " + message.getId() + ", Result: " + result);
        return result;
    }

    // 2. Дохватање свих порука за одређени Савез (за ћаскање)
    public List<AllianceMessage> getMessagesForAlliance(String allianceId) {
        List<AllianceMessage> messages = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DatabaseHelper.COL_MESSAGE_ALLIANCE_ID + " = ?";
        String[] selectionArgs = new String[]{allianceId};

        Cursor cursor = db.query(DatabaseHelper.TABLE_ALLIANCE_MESSAGES,
                null, selection, selectionArgs, null, null,
                DatabaseHelper.COL_MESSAGE_TIMESTAMP + " ASC");

        while (cursor.moveToNext()) {
            messages.add(cursorToMessage(cursor));
        }
        cursor.close();
        return messages;
    }
}