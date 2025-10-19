package com.example.dailyboss.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import com.example.dailyboss.data.dao.UserDao;
import com.example.dailyboss.domain.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot; // Dodaj import
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class UserRepository {

    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore db;
    private final UserDao userDao;
    private static final String USERS_COLLECTION = "users";

    public UserRepository(Context context) { // Konstruktor mora da prima Context
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
        this.userDao = new UserDao(context);
    }

    // MODIFIKOVANO: onSuccess sada prima User objekat
    public interface UserDataListener {
        void onSuccess(User user); // ✨ Izmenjeno: Sada vraća User objekat
        void onFailure(Exception e);
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
                            // 1. AŽURIRAJ MODEL SA UID-om PRE UPISA
                            userModel.setId(firebaseUser.getUid());

                            firebaseUser.sendEmailVerification()
                                    .addOnCompleteListener(emailTask -> {
                                        if (emailTask.isSuccessful()) {
                                            Log.d("UserRepository", "Verifikacioni email uspešno poslat.");

                                            // 2. Čuvanje u Firestore
                                            db.collection(USERS_COLLECTION)
                                                    .document(firebaseUser.getUid())
                                                    .set(userModel)
                                                    .addOnSuccessListener(aVoid -> {
                                                        // 3. SINHRONIZACIJA SA LOKALNIM SQLite-om
                                                        if (userDao.upsert(userModel)) { // Koristi upsert
                                                            listener.onSuccess(userModel); // ✨ Izmenjeno: Vrati userModel
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
                                        } else {
                                            String errorMsg = "Greška pri slanju verifikacionog emaila: " + emailTask.getException().getMessage();
                                            Log.e("UserRepository", errorMsg);
                                            firebaseUser.delete();
                                            listener.onFailure(emailTask.getException());
                                        }
                                    });
                        }
                    } else {
                        Log.e("UserRepository", "Auth registracija nije uspela: " + authTask.getException().getMessage());
                        listener.onFailure(authTask.getException());
                    }
                });
    }

    /**
     * Dohvata korisničke podatke iz Firestore-a na osnovu UID-a.
     */
    public void getUserData(String userId, UserDataListener listener) { // ✨ Nova metoda
        db.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            User user = document.toObject(User.class);
                            if (user != null) {
                                user.setId(document.getId()); // Postavi UID iz dokumenta
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


    /**
     * Proverava da li je korisnik verifikovao email i ažurira 'isActive' u Firestore-u (i SQLite-u).
     */
    public void checkAndActivateUser(UserDataListener listener) { // ✨ Izmenjeno: onSuccess prima User
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser == null) {
            listener.onFailure(new Exception("Korisnik nije prijavljen."));
            return;
        }

        firebaseUser.reload()
                .addOnCompleteListener(reloadTask -> {
                    if (reloadTask.isSuccessful()) {
                        FirebaseUser reloadedUser = firebaseAuth.getCurrentUser();

                        if (reloadedUser != null && reloadedUser.isEmailVerified()) {
                            Log.d("UserRepository", "Email je verifikovan. Ažuriram Firestore.");

                            // Dohvati trenutne podatke korisnika da ih ne prepišemo
                            getUserData(reloadedUser.getUid(), new UserDataListener() {
                                @Override
                                public void onSuccess(User userFromFirestore) {
                                    userFromFirestore.setActive(true); // Ažuriraj active status

                                    db.collection(USERS_COLLECTION)
                                            .document(reloadedUser.getUid())
                                            .set(userFromFirestore) // Updatujemo ceo objekat
                                            .addOnSuccessListener(aVoid -> {
                                                Log.d("UserRepository", "Firestore 'active' postavljen na true.");

                                                // Sinhronizuj i lokalnu bazu
                                                if (userDao.upsert(userFromFirestore)) { // Koristi upsert
                                                    listener.onSuccess(userFromFirestore); // ✨ Vrati ažuriranog korisnika
                                                } else {
                                                    Log.e("UserRepository", "Lokalni upis/ažuriranje profila nije uspeo nakon Firestore uspeha.");
                                                    listener.onSuccess(userFromFirestore); // Ipak javi uspeh, ali sa upozorenjem
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

        // Dohvati korisnika iz lokalne baze
        User user = userDao.getUser(userId);
        if (user == null) {
            listener.onFailure("Korisnik nije pronađen u lokalnoj bazi.");
            return;
        }

        // Postavi novu lozinku
        user.setPassword(newPassword);

        // Ažuriraj korisnika u bazi
        boolean success = userDao.upsert(user);
        if (success) {
            listener.onSuccess("Lozinka je uspešno promenjena.");
        } else {
            listener.onFailure("Greška pri čuvanju nove lozinke.");
        }
    }
}