package com.example.dailyboss.presentation.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dailyboss.MainActivity; // Uvoz MainActivity
import com.example.dailyboss.R;
import com.example.dailyboss.presentation.fragments.LoginFragment; // Uvoz LoginFragment
import com.example.dailyboss.presentation.fragments.RegistrationFragment;

// ⭐ KLJUČNO: Implementiraj interfejs LoginFragment-a
public class AuthenticationActivity extends AppCompatActivity
        implements LoginFragment.OnFragmentInteractionListener {
    // Možete dodati i RegistrationFragment.OnFragmentInteractionListener ako je potrebno

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    // ⭐ Podesite početni fragment na LoginFragment
                    .replace(R.id.fragment_container, new LoginFragment())
                    .commit();
        }
    }

    // ... (vaše metode navigateToRegistration, navigateToLogin ostaju iste)

    // ⭐ NOVO: Implementacija metode iz LoginFragment.OnFragmentInteractionListener
    @Override
    public void onNavigateToRegister() {
        // Logika za prelazak na RegistrationFragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new RegistrationFragment())
                .addToBackStack(null)
                .commit();
    }

    public void navigateToLogin() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new LoginFragment())
                .commit();
    }

    // ⭐ NOVO: Kada je prijava uspešna, prebaci na MainActivity
    @Override
    public void onLoginSuccess() {
        // Kreiraj Intent za prelazak na MainActivity
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

        // Uništi AuthenticationActivity da se korisnik ne može vratiti na login/register
        finish();
    }
}