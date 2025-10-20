package com.example.dailyboss.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import com.example.dailyboss.data.dao.UserDao;
import com.example.dailyboss.data.dao.UserStatisticDao;
import com.example.dailyboss.domain.model.User;
import com.example.dailyboss.domain.model.UserStatistic;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot; // Dodaj import
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;
import java.util.UUID;

public class UserRepository {

    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore db;
    private final UserDao userDao; // Ostavljamo private
    private final UserStatisticDao userStatisticDao;
    private static final String USERS_COLLECTION = "users";

    public UserRepository(Context context) {
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
        this.userDao = new UserDao(context);
        this.userStatisticDao = new UserStatisticDao(context);
    }

    public interface UserDataListener {
        void onSuccess(User user);
        void onFailure(Exception e);
    }

    public User getLocalUser(String userId) {
        Log.d("TAG", "getLocalUser: " + userId);
        return userDao.getUser(userId);
    }

    public List<User> getLocalUsersByAllianceId(String allianceId) {
        return userDao.getUsersByAllianceId(allianceId);
    }

    public void checkUsernameExists(String username, OnCompleteListener<QuerySnapshot> listener) {
        db.collection(USERS_COLLECTION)
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(listener);
    }

    public void registerUser(String email, String password, User userModel, UserDataListener listener) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(authTask -> {
                    if (authTask.isSuccessful()) {
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            userModel.setId(firebaseUser.getUid());

                            // TODO: Uključi ovo ponovo u production-u!
                            firebaseUser.sendEmailVerification()
                                    .addOnCompleteListener(emailTask -> {
                                        if (emailTask.isSuccessful()) {
                                            Log.d("UserRepository", "Verifikacioni email uspešno poslat.");

                                            db.collection(USERS_COLLECTION)
                                                    .document(firebaseUser.getUid())
                                                    .set(userModel)
                                                    .addOnSuccessListener(aVoid -> {
                                                        if (userDao.upsert(userModel)) {
                                                            listener.onSuccess(userModel);
                                                            UserStatistic userStatistic = new UserStatistic(UUID.randomUUID().toString(), userModel.getId(), 0, 0, 0, 0, 0, 0, 0, 0, 0, System.currentTimeMillis(), 0, 0, 0, 0, "Novajlija");
                                                            userStatisticDao.upsert(userStatistic);
                                                        } else {
                                                            Log.e("UserRepository", "Lokalni upis profila nije uspeo nakon Firestore uspeha.");
                                                            listener.onFailure(new Exception("Lokalni upis profila nije uspeo."));
                                                        }
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Log.e("UserRepository", "Firestore upis profila nije uspeo: " + e.getMessage());
                                                        firebaseUser.delete();
                                                        listener.onFailure(e);
                                                    });
                                        } else {
                                            String errorMsg = "Greška pri slanju verifikacionog emaila: " + emailTask.getException().getMessage();
                                            Log.e("UserRepository", errorMsg);
                                            firebaseUser.delete();
                                            listener.onFailure(emailTask.getException());
                                        }
                                    });
                            
                            Log.d("UserRepository", "Preskačem email verifikaciju za testiranje.");

                            db.collection(USERS_COLLECTION)
                                    .document(firebaseUser.getUid())
                                    .set(userModel)
                                    .addOnSuccessListener(aVoid -> {
                                        if (userDao.upsert(userModel)) { // Koristi upsert
                                            listener.onSuccess(userModel); // ✨ Izmenjeno: Vrati userModel
                                            UserStatistic userStatistic = new UserStatistic(UUID.randomUUID().toString(), userModel.getId(), 0, 0, 0, 0, 0, 0, 0, 0, 0, System.currentTimeMillis(), 0, 0, 0, 0, "Novajlija");
                                            userStatisticDao.upsert(userStatistic);
                                        } else {
                                            Log.e("UserRepository", "Lokalni upis profila nije uspeo nakon Firestore uspeha.");
                                            listener.onFailure(new Exception("Lokalni upis profila nije uspeo."));
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("UserRepository", "Firestore upis profila nije uspeo: " + e.getMessage());
                                        firebaseUser.delete(); // Obrisi Auth nalog ako Firestore ne uspe
                                        listener.onFailure(e);
                                    });
                        }
                    } else {
                        Log.e("UserRepository", "Auth registracija nije uspela: " + authTask.getException().getMessage());
                        listener.onFailure(authTask.getException());
                    }
                });
    }

    public void getUserData(String userId, UserDataListener listener) {
        db.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            User user = document.toObject(User.class);
                            if (user != null) {
                                user.setId(document.getId());
                                listener.onSuccess(user);
                            } else {
                                listener.onFailure(new Exception("Korisnik pronađen, ali nije moguće parsirati podatke."));
                            }
                        } else {
                            listener.onFailure(new Exception("Korisnički podaci nisu pronađeni u Firestore-u."));
                        }
                    } else {
                        listener.onFailure(task.getException());
                    }
                });
    }

    public Task<User> updateAllianceIdInFirestore(String userId, String newAllianceId) {
        return db.collection(USERS_COLLECTION)
                .document(userId)
                .update("allianceId", newAllianceId)

                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        userDao.updateAllianceId(userId, newAllianceId);

                        User updatedUser = userDao.getUser(userId);

                        if (updatedUser != null) {
                            return updatedUser; // Vrati User objekat kao rezultat Task-a
                        } else {
                            throw new Exception("Greška: Nije moguće dohvatiti ažuriranog korisnika iz lokalne baze.");
                        }
                    } else {
                        throw task.getException() != null ? task.getException() : new Exception("Ažuriranje Firestore-a neuspešno.");
                    }
                });
    }


    public void checkAndActivateUser(UserDataListener listener) {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser == null) {
            listener.onFailure(new Exception("Korisnik nije prijavljen."));
            return;
        }

        firebaseUser.reload()
                .addOnCompleteListener(reloadTask -> {
                    if (reloadTask.isSuccessful()) {
                        FirebaseUser reloadedUser = firebaseAuth.getCurrentUser();

                        if (reloadedUser != null && reloadedUser.isEmailVerified()) { // Uklonjena email verifikacija za testiranje
                            Log.d("UserRepository", "Email je verifikovan. Ažuriram Firestore.");

                            getUserData(reloadedUser.getUid(), new UserDataListener() {
                                @Override
                                public void onSuccess(User userFromFirestore) {
                                    userFromFirestore.setActive(true);

                                    db.collection(USERS_COLLECTION)
                                            .document(reloadedUser.getUid())
                                            .set(userFromFirestore) // Updatujemo ceo objekat
                                            .addOnSuccessListener(aVoid -> {
                                                Log.d("UserRepository", "Firestore 'active' postavljen na true.");

                                                if (userDao.upsert(userFromFirestore)) {
                                                    listener.onSuccess(userFromFirestore);
                                                } else {
                                                    Log.e("UserRepository", "Lokalni upis/ažuriranje profila nije uspeo nakon Firestore uspeha.");
                                                    listener.onSuccess(userFromFirestore);
                                                }
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e("UserRepository", "Greška pri ažuriranju Firestore-a: " + e.getMessage());
                                                listener.onFailure(new Exception("Ažuriranje baze nije uspelo."));
                                            });
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    Log.e("UserRepository", "Greška pri dohvatanju korisnika za aktivaciju: " + e.getMessage());
                                    listener.onFailure(new Exception("Korisnički podaci nisu pronađeni."));
                                }
                            });
                        } else {
                            Log.d("UserRepository", "Email još uvek nije verifikovan.");
                            listener.onFailure(new Exception("Email nije verifikovan."));
                        }
                    } else {
                        Log.e("UserRepository", "Greška pri osvežavanju korisnika: " + reloadTask.getException().getMessage());
                        listener.onFailure(reloadTask.getException());
                    }
                });
    }

    public interface PasswordChangeListener {
        void onSuccess(String message);
        void onFailure(String errorMessage);
    }

    public void changeUserPassword(String userId, String newPassword, PasswordChangeListener listener) {
        if (userId == null) {
            listener.onFailure("Korisnik nije pronađen.");
            return;
        }

        User user = userDao.getUser(userId);
        if (user == null) {
            listener.onFailure("Korisnik nije pronađen u lokalnoj bazi.");
            return;
        }

        user.setPassword(newPassword);

        boolean success = userDao.upsert(user);
        if (success) {
            listener.onSuccess("Lozinka je uspešno promenjena.");
        } else {
            listener.onFailure("Greška pri čuvanju nove lozinke.");
        }
    }
}