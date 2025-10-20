package com.example.dailyboss.presentation.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.dailyboss.R;
import com.example.dailyboss.service.AuthService;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginFragment extends Fragment {

    private static final String TAG = "LoginFragment";

    private TextInputLayout emailInputLayout, passwordInputLayout;
    private TextInputEditText emailInput, passwordInput;
    private Button loginButton;
    private TextView registerPrompt, forgotPasswordPrompt;
    private ProgressBar progressBar;
    private Toolbar toolbar;

    private AuthService authService;
    private OnFragmentInteractionListener fragmentInteractionListener; // Listener za komunikaciju sa Activityjem

    public interface OnFragmentInteractionListener {
        void onNavigateToRegister();
        void onLoginSuccess();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            fragmentInteractionListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authService = new AuthService(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        initViews(view);
        setupListeners();
        return view;
    }

    private void initViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);

        // KORISTI NOVE ID-jeve za TextInputLayout
        emailInputLayout = view.findViewById(R.id.emailInput_layout);
        passwordInputLayout = view.findViewById(R.id.passwordInput_layout);

        // KORISTI ORIGINALNE ID-jeve za TextInputEditText
        emailInput = view.findViewById(R.id.emailInput);
        passwordInput = view.findViewById(R.id.passwordInput);

        loginButton = view.findViewById(R.id.loginButton);
        registerPrompt = view.findViewById(R.id.registerPrompt);
        forgotPasswordPrompt = view.findViewById(R.id.forgotPasswordPrompt);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        loginButton.setOnClickListener(v -> attemptLogin());
        registerPrompt.setOnClickListener(v -> {
            if (fragmentInteractionListener != null) {
                fragmentInteractionListener.onNavigateToRegister();
            }
        });
        forgotPasswordPrompt.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Zaboravljena lozinka - nije implementirano", Toast.LENGTH_SHORT).show();
            // Implementirati navigaciju na ForgotPasswordFragment ili sl.
        });
    }

    private void attemptLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();

        // VALIDACIJA
        if (!validateFields(email, password)) {
            return;
        }

        showLoading(true);

        authService.attemptLogin(email, password, new AuthService.AuthStatusListener() {
            @Override
            public void onSuccess(String message) {
                showLoading(false);
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                if (fragmentInteractionListener != null) {
                    fragmentInteractionListener.onLoginSuccess(); // Obavesti Activity o uspešnoj prijavi
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                showLoading(false);
                Toast.makeText(requireContext(), "Greška: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean validateFields(String email, String password) {
        boolean valid = true;

        if (email.isEmpty()) {
            emailInputLayout.setError("Email je obavezan");
            valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputLayout.setError("Neispravan format emaila");
            valid = false;
        } else {
            emailInputLayout.setError(null);
        }

        if (password.isEmpty()) {
            passwordInputLayout.setError("Lozinka je obavezna");
            valid = false;
        } else {
            passwordInputLayout.setError(null);
        }

        return valid;
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            loginButton.setEnabled(false);
            emailInput.setEnabled(false);
            passwordInput.setEnabled(false);
            registerPrompt.setEnabled(false);
            forgotPasswordPrompt.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            loginButton.setEnabled(true);
            emailInput.setEnabled(true);
            passwordInput.setEnabled(true);
            registerPrompt.setEnabled(true);
            forgotPasswordPrompt.setEnabled(true);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        fragmentInteractionListener = null;
    }
}