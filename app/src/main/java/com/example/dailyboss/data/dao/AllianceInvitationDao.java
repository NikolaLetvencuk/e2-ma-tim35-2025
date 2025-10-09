package com.example.dailyboss.data.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.dailyboss.data.DatabaseHelper;
import com.example.dailyboss.domain.model.AllianceInvitation;
import com.example.dailyboss.domain.model.AllianceInvitationStatus; // 🚀 DODATO: Importujemo Enum

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Data Access Object za lokalno keširanje AllianceInvitation objekata.
 */
public class AllianceInvitationDao {

    private final DatabaseHelper dbHelper;
    private static final String TAG = "SQLiteAllianceInvDao";

    public AllianceInvitationDao(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // 🚀 AŽURIRANO: Konverzija iz Stringa u Enum
    private AllianceInvitation cursorToInvitation(Cursor cursor) {
        // Dohvatamo status kao String iz baze
        String statusString = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INVITATION_STATUS));

        return new AllianceInvitation(
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INVITATION_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INVITATION_ALLIANCE_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INVITATION_ALLIANCE_NAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INVITATION_SENDER_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INVITATION_RECEIVER_ID)),
                new Date(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_INVITATION_SENT_AT))),
                // Koristimo valueOf da konvertujemo String u Enum
                AllianceInvitationStatus.valueOf(statusString)
        );
    }

    // 🚀 AŽURIRANO: Konverzija iz Enum-a u String
    private ContentValues invitationToContentValues(AllianceInvitation invitation) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_INVITATION_ID, invitation.getId());
        values.put(DatabaseHelper.COL_INVITATION_ALLIANCE_ID, invitation.getAllianceId());
        values.put(DatabaseHelper.COL_INVITATION_ALLIANCE_NAME, invitation.getAllianceName());
        values.put(DatabaseHelper.COL_INVITATION_SENDER_ID, invitation.getSenderId());
        values.put(DatabaseHelper.COL_INVITATION_RECEIVER_ID, invitation.getReceiverId());
        values.put(DatabaseHelper.COL_INVITATION_SENT_AT, invitation.getSentAt().getTime());
        // Koristimo name() da dobijemo String reprezentaciju Enum-a za bazu
        values.put(DatabaseHelper.COL_INVITATION_STATUS, invitation.getStatus().name());
        return values;
    }

    // 1. Ubacivanje (Insert) - Funkcija ostaje ista, ali koristi ažuriranu pomoćnu metodu
    public long insert(AllianceInvitation invitation) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = invitationToContentValues(invitation);

        long result = db.insert(DatabaseHelper.TABLE_ALLIANCE_INVITATIONS, null, values);
        Log.d(TAG, "Inserting invitation: " + invitation.getId() + ", Result: " + result);
        return result;
    }

    /**
     * 2. Ažuriranje statusa poziva
     * Tip newStatus sada prima String, ali ga možete promeniti da prima Enum za bolju Type Safety
     */
    public boolean updateStatus(String invitationId, AllianceInvitationStatus newStatus) { // 🚀 PREDLOG AŽURIRANJA SIGNATURE
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        // Ažurirajte bazu sa String vrednošću Enum-a
        values.put(DatabaseHelper.COL_INVITATION_STATUS, newStatus.name());

        int rowsAffected = db.update(DatabaseHelper.TABLE_ALLIANCE_INVITATIONS, values,
                DatabaseHelper.COL_INVITATION_ID + " = ?", new String[]{invitationId});
        return rowsAffected > 0;
    }

    /**
     * 3. Dohvatanje aktivnih poziva za određenog korisnika
     * Koristi Enum vrednost za upit.
     */
    public List<AllianceInvitation> getPendingInvitationsForUser(String userId) {
        List<AllianceInvitation> invitations = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DatabaseHelper.COL_INVITATION_RECEIVER_ID + " = ? AND " +
                DatabaseHelper.COL_INVITATION_STATUS + " = ?";
        // Koristimo PENDING Enum vrednost pretvorenu u String za upit
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

    // 4. Brisanje poziva (ostaje isto)
    public int delete(String invitationId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsDeleted = db.delete(DatabaseHelper.TABLE_ALLIANCE_INVITATIONS,
                DatabaseHelper.COL_INVITATION_ID + " = ?",
                new String[]{invitationId});
        return rowsDeleted;
    }

    // 5. Dohvatanje jedne pozivnice po ID-ju (ostaje isto, ali koristi ažurirani cursorToInvitation)
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