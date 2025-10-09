
package com.example.dailyboss.data.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.dailyboss.data.DatabaseHelper;
import com.example.dailyboss.domain.model.Friendship;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object za lokalno keširanje Friendship objekata.
 * Koristi sinhroni pristup za SQLite.
 */
public class FriendshipDao {

    private final DatabaseHelper dbHelper;
    private static final String TAG = "SQLiteFriendshipDao";

    public FriendshipDao(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    private Friendship cursorToFriendship(Cursor cursor) {
        return new Friendship(
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FRIENDSHIP_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FRIENDSHIP_SENDER_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FRIENDSHIP_RECEIVER_ID)),
                cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FRIENDSHIP_TIMESTAMP)),
                cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_FRIENDSHIP_STATUS))
        );
    }

    // --- Pomoćna metoda za konverziju Friendship objekta u ContentValues ---
    private ContentValues friendshipToContentValues(Friendship friendship) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_FRIENDSHIP_ID, friendship.getId());
        values.put(DatabaseHelper.COL_FRIENDSHIP_SENDER_ID, friendship.getSenderId());
        values.put(DatabaseHelper.COL_FRIENDSHIP_RECEIVER_ID, friendship.getReceiverId());
        values.put(DatabaseHelper.COL_FRIENDSHIP_TIMESTAMP, friendship.getTimestamp());
        values.put(DatabaseHelper.COL_FRIENDSHIP_STATUS, friendship.getStatus());
        return values;
    }

    // 1. Ubacivanje ili Ažuriranje (Upsert)
    public boolean upsert(Friendship friendship) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = friendshipToContentValues(friendship);

        // Pokušaj ažuriranja
        int updated = db.update(DatabaseHelper.TABLE_FRIENDSHIPS, values,
                DatabaseHelper.COL_FRIENDSHIP_ID + " = ?", new String[]{friendship.getId()});

        long result;
        if (updated == 0) {
            // Ako ažuriranje nije uspelo, ubaci novi red
            result = db.insert(DatabaseHelper.TABLE_FRIENDSHIPS, null, values);
            Log.d(TAG, "Inserting new friendship: " + friendship.getId() + ", Result: " + result);
        } else {
            result = updated;
            Log.d(TAG, "Updating friendship status: " + friendship.getId() + ", Result: " + result);
        }

        return result != -1;
    }

    // 2. Ažuriranje samo statusa
    public boolean updateStatus(String friendshipId, String newStatus) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_FRIENDSHIP_STATUS, newStatus);

        int rowsAffected = db.update(DatabaseHelper.TABLE_FRIENDSHIPS, values,
                DatabaseHelper.COL_FRIENDSHIP_ID + " = ?", new String[]{friendshipId});
        return rowsAffected > 0;
    }

    // 3. Dohvatanje prihvaćenih prijateljstava za korisnika (Za lokalni keš)
    public List<Friendship> getAcceptedFriendships(String userId) {
        List<Friendship> friendships = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Query za sve ACCEPTED veze gde je korisnik SENDER ILI RECEIVER
        String selection = "(" + DatabaseHelper.COL_FRIENDSHIP_SENDER_ID + " = ? OR " +
                DatabaseHelper.COL_FRIENDSHIP_RECEIVER_ID + " = ?) AND " +
                DatabaseHelper.COL_FRIENDSHIP_STATUS + " = ?";
        String[] selectionArgs = new String[]{userId, userId, Friendship.STATUS_ACCEPTED};

        Cursor cursor = db.query(DatabaseHelper.TABLE_FRIENDSHIPS, null, selection, selectionArgs, null, null, null);

        while (cursor.moveToNext()) {
            friendships.add(cursorToFriendship(cursor));
        }
        cursor.close();
        return friendships;
    }

    public List<Friendship> getPendingReceivedRequests(String userId) {
        List<Friendship> requests = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DatabaseHelper.COL_FRIENDSHIP_RECEIVER_ID + " = ? AND " +
                DatabaseHelper.COL_FRIENDSHIP_STATUS + " = ?";
        String[] selectionArgs = new String[]{userId, Friendship.STATUS_PENDING};

        Cursor cursor = db.query(DatabaseHelper.TABLE_FRIENDSHIPS,
                null,
                selection,
                selectionArgs,
                null,
                null,
                DatabaseHelper.COL_FRIENDSHIP_TIMESTAMP + " DESC");

        while (cursor.moveToNext()) {
            requests.add(cursorToFriendship(cursor));
        }
        cursor.close();
        Log.d(TAG, "Fetched " + requests.size() + " pending requests for user: " + userId);
        return requests;
    }

    // 4. Provera da li prijateljstvo postoji
    public boolean exists(String friendshipId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_FRIENDSHIPS,
                new String[]{DatabaseHelper.COL_FRIENDSHIP_ID},
                DatabaseHelper.COL_FRIENDSHIP_ID + " = ?",
                new String[]{friendshipId},
                null, null, null);

        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    // U FriendshipDao.java

    @Nullable
    public Friendship getFriendshipStatus(String userId1, String userId2) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Friendship friendship = null;

        // Proverava veze gde je (userId1=SENDER I userId2=RECEIVER) ILI (userId2=SENDER I userId1=RECEIVER)
        String selection = "(" + DatabaseHelper.COL_FRIENDSHIP_SENDER_ID + " = ? AND " + DatabaseHelper.COL_FRIENDSHIP_RECEIVER_ID + " = ?) OR " +
                "(" + DatabaseHelper.COL_FRIENDSHIP_SENDER_ID + " = ? AND " + DatabaseHelper.COL_FRIENDSHIP_RECEIVER_ID + " = ?)";

        // Morate proslediti argumente u pravom redosledu: userId1, userId2, userId2, userId1
        String[] selectionArgs = new String[]{userId1, userId2, userId2, userId1};

        Cursor cursor = db.query(DatabaseHelper.TABLE_FRIENDSHIPS, null, selection, selectionArgs, null, null, null);

        if (cursor.moveToFirst()) {
            friendship = cursorToFriendship(cursor);
        }
        cursor.close();
        return friendship;
    }
}