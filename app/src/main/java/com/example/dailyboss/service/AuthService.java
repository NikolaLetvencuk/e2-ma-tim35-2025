package com.example.dailyboss.service;

import android.content.Context;
import android.util.Log;

import com.example.dailyboss.data.SharedPreferencesHelper;
import com.example.dailyboss.data.dao.UserDao;
import com.example.dailyboss.data.repository.UserRepository;
import com.example.dailyboss.data.repository.UserProfileRepository;
import com.example.dailyboss.data.repository.UserStatisticRepository;
import com.example.dailyboss.domain.model.User;
import com.example.dailyboss.domain.model.UserProfile;
import com.example.dailyboss.domain.model.UserStatistic;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

import java.util.UUID;

public class AuthService {

    private final UserRepository userRepository;
    private final UserDao userDao;
    private final FirebaseAuth firebaseAuth;
    private final Context context;
    private final UserProfileRepository userProfileRepository;
    private final UserStatisticRepository userStatisticRepository;

    public AuthService(Context context) {
        this.userRepository = new UserRepository(context);
        this.userDao = new UserDao(context);
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.userProfileRepository = new UserProfileRepository(context);
        this.userStatisticRepository = new UserStatisticRepository(context);
        this.context = context;
    }

    public interface AuthStatusListener {
        void onSuccess(String message);
        void onFailure(String errorMessage);
    }

    public void attemptRegistration(String email, String username, String password, String selectedAvatar, AuthStatusListener listener) {

        userRepository.checkUsernameExists(username, task -> {
            if (task.isSuccessful()) {
                if (!task.getResult().isEmpty()) {
                    listener.onFailure("Korisničko ime je zauzeto i ne može se menjati.");
                } else {
                    User newUser = new User(
                            null,
                            username,
                            email,
                            password,
                            selectedAvatar,
                            false,
                            System.currentTimeMillis(),
                            null
                    );

                    userRepository.registerUser(email, password, newUser, new UserRepository.UserDataListener() {
                        @Override
                        public void onSuccess(User registeredUser) {
                            UserProfile stat = new UserProfile(
                                    UUID.randomUUID().toString(),
                                    registeredUser.getId(),
                                    0, 0,
                                    1,
                                    0,
                                    0,
                                    System.currentTimeMillis()
                            );

                            UserStatistic stats = new UserStatistic(
                                    UUID.randomUUID().toString(),
                                    registeredUser.getId(),
                                    0,
                                    0,
                                    0,
                                    0,
                                    0,
                                    0,
                                    0,
                                    0,
                                    0,
                                    System.currentTimeMillis(),
                                    0,
                                    0,
                                    0,
                                    300,
                                    "Novajlija"
                            );

                            boolean statSaved = userProfileRepository.saveOrUpdate(stat);
                            userStatisticRepository.upsertUserStatistic(stats, new UserStatisticRepository.UserStatisticDataListener() {
                                @Override
                                public void onSuccess(UserStatistic s) {
                                    Log.d("TAG", "Uspešno sačuvana ažurirana statistika.");
                                }
                                @Override
                                public void onFailure(Exception e) {
                                    Log.e("TAG", "Greška pri čuvanju ažurirane statistike: " + e.getMessage());
                                }
                            });
                            if (!statSaved) {
                                Log.w("AuthService", "Neuspešno kreiranje statistike korisnika nakon registracije.");
                            }

                            listener.onSuccess("Uspešna registracija! Proverite email za aktivaciju naloga (Link traje 24h).");
                        }

                        @Override
                        public void onFailure(Exception e) {
                            String error = "Greška pri registraciji.";
                            if (e instanceof FirebaseAuthUserCollisionException) {
                                error = "Korisnik sa tim emailom već postoji.";
                            } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                                error = "Email je nevažeći ili lozinka nije dovoljno jaka (min. 6 karaktera).";
                            }
                            listener.onFailure(error);
                        }
                    });
                }
            } else {
                listener.onFailure("Greška prilikom provere korisničkog imena: " + task.getException().getMessage());
            }
        });
    }

    public void attemptLogin(String email, String password, AuthStatusListener listener) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null) {

                            if (!firebaseUser.isEmailVerified()) {
                                listener.onFailure("Molimo vas da prvo verifikujete svoj email nalog.");
                                return;
                            }


                            userRepository.getUserData(firebaseUser.getUid(), new UserRepository.UserDataListener() {
                                @Override
                                public void onSuccess(User user) {
                                    SharedPreferencesHelper prefs = new SharedPreferencesHelper(context);
                                    prefs.saveLoggedInUser(user.getId(), user.getUsername()); // ili getName()
                                    Log.d("TAG", "onSuccess: " + user.getUsername() + user.getAllianceId());
                                    // ✅ Upsert u lokalnu bazu
                                    if (userDao.upsert(user)) {
                                        listener.onSuccess("Uspešna prijava!");
                                    } else {
                                        Log.w("AuthService", "Greška pri lokalnom upisu/ažuriranju korisnika nakon prijave.");
                                        listener.onSuccess("Uspešna prijava (lokalna sinhronizacija možda nije potpuna).");
                                    }
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    Log.e("AuthService", "Greška pri dohvatanju podataka korisnika iz Firestore-a nakon prijave: " + e.getMessage());
                                    listener.onFailure("Prijava uspešna, ali greška pri dohvatanju korisničkih podataka: " + e.getMessage());
                                }
                            });
                        } else {
                            listener.onFailure("Došlo je do neočekivane greške.");
                        }
                    } else {
                        String errorMessage = "Greška pri prijavi.";
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            errorMessage = "Pogrešan email ili lozinka.";
                        }
                        listener.onFailure(errorMessage);
                    }
                });
    }

    public void logout() {
        firebaseAuth.signOut();
    }
}