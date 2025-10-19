package com.example.dailyboss;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

// Dodajte uvoz za Firebase
import com.example.dailyboss.data.SharedPreferencesHelper;
import com.example.dailyboss.data.dao.UserDao;
import com.example.dailyboss.domain.model.User;
import com.example.dailyboss.presentation.fragments.UserProfileFragment;
import com.google.firebase.auth.FirebaseAuth;

import com.example.dailyboss.presentation.activities.AuthenticationActivity;
import com.example.dailyboss.presentation.fragments.BattleFragment;
import com.example.dailyboss.presentation.fragments.CategoryListFragment;
import com.example.dailyboss.presentation.fragments.HomeFragment;
import com.example.dailyboss.presentation.fragments.TasksFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth; // Polje za Firebase Auth
    private SharedPreferencesHelper prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        prefs = new SharedPreferencesHelper(this);

        boolean isLoggedInLocally = prefs.isUserLoggedIn();
        String userId = prefs.getLoggedInUserId();

        if (userId == null) {
            // Nema ulogovanog korisnika → idi na AuthenticationActivity
            navigateToAuth();
            return;
        }

        UserDao userDao = new UserDao(this);
        User localUser = userDao.getUser(userId);

        if (localUser == null) {
            // User više ne postoji u lokalnoj bazi
            prefs.logoutUser(); // Očisti SharedPreferences
            navigateToAuth();
            return;
        }
        // ⭐ KLJUČNA IZMENA: Proveri da li je korisnik prijavljen
        if (firebaseAuth.getCurrentUser() == null || !isLoggedInLocally) {
            // Ako korisnik NIJE prijavljen, prebaci na Auth Activity
            navigateToAuth();
            return; // Prekini onCreate() metode
        }

        // Ako JESTE prijavljen, nastavi sa glavnim UI (Bottom Nav)
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();

        bottomNavigationView.setOnItemSelectedListener(item -> {
            // ... (ostatak vaše logike za BottomNavigationView)
            if (item.getItemId() == R.id.nav_home) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new HomeFragment())
                        .commit();
                return true;
            } else if (item.getItemId() == R.id.nav_categories) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new CategoryListFragment())
                        .commit();
                return true;
            } else if (item.getItemId() == R.id.activity_tasks) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new TasksFragment())
                        .commit();
                return true;
            } else if (item.getItemId() == R.id.activity_battle) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container,  new BattleFragment())
                        .commit();
                return true;
            } else if (item.getItemId() == R.id.activity_profile) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container,  new UserProfileFragment())
                        .commit();
                return true;
            }
            return false;
        });
    }

    // Preimenovana metoda
    private void navigateToAuth() {
        Intent intent = new Intent(this, AuthenticationActivity.class);
        startActivity(intent);
        finish(); // Zatvori MainActivity dok se korisnik ne prijavi
    }
}