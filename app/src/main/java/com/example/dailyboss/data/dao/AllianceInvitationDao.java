package com.example.dailyboss.data.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.dailyboss.data.DatabaseHelper;
import com.example.dailyboss.domain.model.AllianceInvitation;
import com.example.dailyboss.domain.model.AllianceInvitationStatus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AllianceInvitationDao {

    private final DatabaseHelper dbHelper;
    private static final String TAG = "SQLiteAllianceInvDao";

    public AllianceInvitationDao(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    private AllianceInvitation cursorToInvitation(Cursor cursor) {
        String statusString = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INVITATION_STATUS));

        return new AllianceInvitation(
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INVITATION_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INVITATION_ALLIANCE_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INVITATION_ALLIANCE_NAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INVITATION_SENDER_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INVITATION_RECEIVER_ID)),
                new Date(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INVITATION_SENT_AT))),
                AllianceInvitationStatus.valueOf(statusString)
        );
    }

    private ContentValues invitationToContentValues(AllianceInvitation invitation) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_INVITATION_ID, invitation.getId());
        values.put(DatabaseHelper.COL_INVITATION_ALLIANCE_ID, invitation.getAllianceId());
        values.put(DatabaseHelper.COL_INVITATION_ALLIANCE_NAME, invitation.getAllianceName());
        values.put(DatabaseHelper.COL_INVITATION_SENDER_ID, invitation.getSenderId());
        values.put(DatabaseHelper.COL_INVITATION_RECEIVER_ID, invitation.getReceiverId());
        values.put(DatabaseHelper.COL_INVITATION_SENT_AT, invitation.getSentAt().getTime());
        values.put(DatabaseHelper.COL_INVITATION_STATUS, invitation.getStatus().name());
        return values;
    }

    public long insert(AllianceInvitation invitation) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = invitationToContentValues(invitation);

        long result = db.insert(DatabaseHelper.TABLE_ALLIANCE_INVITATIONS, null, values);
        Log.d(TAG, "Inserting invitation: " + invitation.getId() + ", Result: " + result);
        return result;
    }

    public boolean updateStatus(String invitationId, AllianceInvitationStatus newStatus) { // 🚀 PREDLOG AŽURIRANJA SIGNATURE
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_INVITATION_STATUS, newStatus.name());

        int rowsAffected = db.update(DatabaseHelper.TABLE_ALLIANCE_INVITATIONS, values,
                DatabaseHelper.COL_INVITATION_ID + " = ?", new String[]{invitationId});
        return rowsAffected > 0;
    }

    public List<AllianceInvitation> getPendingInvitationsForUser(String userId) {
        List<AllianceInvitation> invitations = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DatabaseHelper.COL_INVITATION_RECEIVER_ID + " = ? AND " +
                DatabaseHelper.COL_INVITATION_STATUS + " = ?";
        String[] selectionArgs = new String[]{userId, AllianceInvitationStatus.PENDING.name()};

        Cursor cursor = db.query(DatabaseHelper.TABLE_ALLIANCE_INVITATIONS,
                null, selection, selectionArgs, null, null,
                DatabaseHelper.COL_INVITATION_SENT_AT + " DESC");

        while (cursor.moveToNext()) {
            invitations.add(cursorToInvitation(cursor));
        }
        cursor.close();
        Log.d(TAG, "Fetched " + invitations.size() + " pending invitations for user: " + userId);
        return invitations;
    }

    public int delete(String invitationId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsDeleted = db.delete(DatabaseHelper.TABLE_ALLIANCE_INVITATIONS,
                DatabaseHelper.COL_INVITATION_ID + " = ?",
                new String[]{invitationId});
        return rowsDeleted;
    }

    public AllianceInvitation getInvitationById(String invitationId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        AllianceInvitation invitation = null;

        String selection = DatabaseHelper.COL_INVITATION_ID + " = ?";
        String[] selectionArgs = new String[]{invitationId};

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_ALLIANCE_INVITATIONS,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            invitation = cursorToInvitation(cursor);
        }

        cursor.close();
        return invitation;
    }
}