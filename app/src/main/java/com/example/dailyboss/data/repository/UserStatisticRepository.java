package com.example.dailyboss.data.repository;

import android.content.Context;
import android.util.Log;

import com.example.dailyboss.data.dao.UserStatisticDao;
import com.example.dailyboss.domain.model.UserStatistic;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserStatisticRepository {

    private final FirebaseFirestore db;
    private final UserStatisticDao userStatisticDao;
    private static final String USER_STATISTICS_COLLECTION = "userStatistics"; // Nova kolekcija u Firestore-u
    private static final String TAG = "UserStatisticRepo";

    public UserStatisticRepository(Context context) {
        this.db = FirebaseFirestore.getInstance();
        this.userStatisticDao = new UserStatisticDao(context);
    }

    public interface UserStatisticDataListener {
        void onSuccess(UserStatistic userStatistic);
        void onFailure(Exception e);
    }

    public void upsertUserStatistic(UserStatistic userStatistic, UserStatisticDataListener listener) {
        if (userStatistic.getId() == null || userStatistic.getId().isEmpty()) {
            listener.onFailure(new IllegalArgumentException("UserStatistic ID cannot be null or empty."));
            return;
        }

        db.collection(USER_STATISTICS_COLLECTION)
                .document(userStatistic.getId())
                .set(userStatistic)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "UserStatistic updated in Firestore for userId: " + userStatistic.getUserId());
                    if (userStatisticDao.upsert(userStatistic)) {
                        listener.onSuccess(userStatistic);
                    } else {
                        Log.e(TAG, "Lokalni upis/ažuriranje UserStatistic-a nije uspeo nakon Firestore uspeha.");
                        listener.onFailure(new Exception("Lokalni upis/ažuriranje statistike nije uspeo."));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firestore upis/ažuriranje UserStatistic-a nije uspeo: " + e.getMessage());
                    listener.onFailure(e);
                });
        userStatisticDao.upsert(userStatistic);
    }

    public void getUserStatistic(String userId, UserStatisticDataListener listener) {
        // Prvo pokušaj dohvatiti iz lokalne baze
        UserStatistic localStatistic = userStatisticDao.getUserStatistic(userId);
        if (localStatistic != null) {
            listener.onSuccess(localStatistic);
            Log.d(TAG, "UserStatistic dohvaćen iz lokalne baze za userId: " + userId);
            return;
        }

        db.collection(USER_STATISTICS_COLLECTION)
                .document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            UserStatistic userStatistic = document.toObject(UserStatistic.class);
                            if (userStatistic != null) {
                                userStatistic.setId(document.getId());
                                if (userStatisticDao.upsert(userStatistic)) {
                                    listener.onSuccess(userStatistic);
                                    Log.d(TAG, "UserStatistic dohvaćen iz Firestore-a i upisan lokalno za userId: " + userId);
                                } else {
                                    Log.e(TAG, "Lokalni upis UserStatistic-a nije uspeo nakon Firestore uspeha.");
                                    listener.onFailure(new Exception("Lokalni upis statistike nije uspeo."));
                                }
                            } else {
                                listener.onFailure(new Exception("Statistika pronađena, ali nije moguće parsirati podatke."));
                            }
                        } else {
                            Log.d(TAG, "UserStatistic nije pronađen u Firestore-u za userId: " + userId + ". Kreiram novi.");
                            UserStatistic newUserStatistic = new UserStatistic();
                            newUserStatistic.setId(userId);
                            newUserStatistic.setUserId(userId);
                            upsertUserStatistic(newUserStatistic, listener); // Sačuvaj novi objekat
                        }
                    } else {
                        Log.e(TAG, "Greška pri dohvatanju UserStatistic-a iz Firestore-a: " + task.getException().getMessage());
                        listener.onFailure(task.getException());
                    }
                });
    }

    public void deleteUserStatistic(String userId, UserStatisticDataListener listener) {
        db.collection(USER_STATISTICS_COLLECTION)
                .document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "UserStatistic obrisan iz Firestore-a za userId: " + userId);
                    if (userStatisticDao.deleteUserStatistic(userId)) {
                        listener.onSuccess(null); // Ili vrati potvrdu brisanja
                    } else {
                        Log.e(TAG, "Brisanje UserStatistic-a iz lokalne baze nije uspelo.");
                        listener.onFailure(new Exception("Brisanje statistike iz lokalne baze nije uspelo."));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Greška pri brisanju UserStatistic-a iz Firestore-a: " + e.getMessage());
                    listener.onFailure(e);
                });
    }

    public UserStatistic getUserStatistic(String userId) {
        return userStatisticDao.getUserStatistic(userId);
    }

    public boolean saveOrUpdate(UserStatistic stat) {
        return userStatisticDao.upsert(stat);
    }
}