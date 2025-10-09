package com.example.dailyboss.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.dailyboss.data.dao.FriendshipDao;
import com.example.dailyboss.domain.model.Friendship;
import com.example.dailyboss.domain.model.User; // Pretpostavljamo da je User model dostupan
import com.example.dailyboss.data.dao.UserDao; // Vaš User DAO
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;

public class FriendshipRepository {

    private final FriendshipDao friendshipDao;
    private final UserDao localUserDao;
    private static final String TAG = "LocalFriendshipRepo";


    public FriendshipRepository(Context context) {
        this.friendshipDao = new FriendshipDao(context);
        this.localUserDao = new UserDao(context);
    }

    public boolean saveAcceptedFriendshipToCache(Friendship friendship) {
        if (!Friendship.STATUS_ACCEPTED.equals(friendship.getStatus())) {
            Log.w(TAG, "Attempted to save non-ACCEPTED friendship status to local cache.");
            return false;
        }
        return friendshipDao.upsert(friendship);
    }

    public Task<Void> sendFriendRequest(String senderId, String receiverId) {
        Log.w(TAG, "sendFriendRequest called, but only running in local mode. No request sent.");
        Friendship friendship = new Friendship(UUID.randomUUID().toString(), senderId, receiverId, System.currentTimeMillis(), Friendship.STATUS_PENDING);
        friendshipDao.upsert(friendship);
        return Tasks.forResult(null);
    }

    public boolean updateFriendshipStatusInCache(String friendshipId, String newStatus) {
        return friendshipDao.updateStatus(friendshipId, newStatus);
    }

    @Nullable
    public Friendship getExistingFriendship(String userId1, String userId2) {
        return friendshipDao.getFriendshipStatus(userId1, userId2);
    }

    public List<User> getAcceptedFriendsList(String currentUserId) {
        List<Friendship> acceptedFriendships = friendshipDao.getAcceptedFriendships(currentUserId);
        List<User> friendsList = new ArrayList<>();

        if (acceptedFriendships.isEmpty()) {
            Log.d(TAG, "No accepted friendships found in local cache for user: " + currentUserId);
            return friendsList;
        }

        for (Friendship friendship : acceptedFriendships) {
            String friendId;

            if (friendship.getSenderId().equals(currentUserId)) {
                friendId = friendship.getReceiverId();
            } else {
                friendId = friendship.getSenderId();
            }

            User friend = localUserDao.getUser(friendId);

            if (friend != null) {
                friendsList.add(friend);
            } else {
                Log.w(TAG, "Friend user profile (ID: " + friendId + ") not found in local cache. Requires remote sync.");
            }
        }

        return friendsList;
    }

    public List<Friendship> getPendingReceivedRequests(String currentUserId) {
        // 1. Dohvatanje PENDING Friendship objekata gde je trenutni korisnik primalac
        List<Friendship> pendingRequests = friendshipDao.getPendingReceivedRequests(currentUserId);
        List<User> senderUsers = new ArrayList<>();

        if (pendingRequests.isEmpty()) {
            Log.d(TAG, "No pending friend requests found in local cache for user: " + currentUserId);
            return pendingRequests;
        }

        // 2. Mapiranje Friendship objekata na User objekte (pošiljaoce)
        for (Friendship request : pendingRequests) {

            // Pošiljalac je onaj čiji profil želimo da prikažemo
            String senderId = request.getSenderId();

            // Dohvatanje User profila pošiljaoca iz lokalne baze
            User sender = localUserDao.getUser(senderId);

            if (sender != null) {
                // Dodajemo pošiljaoca u listu
                senderUsers.add(sender);
            } else {
                Log.w(TAG, "Sender user profile (ID: " + senderId +
                        ") for pending request not found in local cache. Requires remote sync.");
                // Opciono: Možete dodati 'dummy' User objekat ili preskočiti ovaj zahtev
            }
        }

        Log.d(TAG, "Successfully retrieved " + senderUsers.size() + " User profiles for pending requests.");
        return pendingRequests;
    }

    public Task<List<User>> searchUsers(String query) {
        return Tasks.call(Executors.newSingleThreadExecutor(), () -> {
            Log.d(TAG, "Executing local user search for: " + query);
            return localUserDao.searchUsersByUsername(query);
        });
    }

    public UserDao getLocalUserDao() {
        return localUserDao;
    }
}