package com.example.dailyboss.presentation.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.dailyboss.R;
import com.example.dailyboss.presentation.activities.AuthenticationActivity;
import com.example.dailyboss.service.AuthService;

public class RegistrationFragment extends Fragment {

    private EditText emailInput, usernameInput, passwordInput, confirmPasswordInput;
    private ImageView[] avatarViews = new ImageView[5];
    private Button registerButton;

    private AuthService authService;
    private String selectedAvatarName = "avatar_1";
    private ImageView selectedAvatarView = null;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        authService = new AuthService(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Koristite fragment_registration.xml
        return inflater.inflate(R.layout.fragment_registration, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupAvatarSelection();

        registerButton.setOnClickListener(v -> registerUser());
    }


    private void initViews(View view) {
        emailInput = view.findViewById(R.id.emailInput);
        usernameInput = view.findViewById(R.id.usernameInput);
        passwordInput = view.findViewById(R.id.passwordInput);
        confirmPasswordInput = view.findViewById(R.id.confirmPasswordInput);
        registerButton = view.findViewById(R.id.registerButton);

        avatarViews[0] = view.findViewById(R.id.avatar1);
        avatarViews[1] = view.findViewById(R.id.avatar2);
        avatarViews[2] = view.findViewById(R.id.avatar3);
        avatarViews[3] = view.findViewById(R.id.avatar4);
        avatarViews[4] = view.findViewById(R.id.avatar5);
    }

    private void setupAvatarSelection() {
        selectedAvatarView = avatarViews[0];
        selectedAvatarView.setBackgroundResource(R.drawable.avatar_border_selected);

        for (int i = 0; i < avatarViews.length; i++) {
            final int index = i + 1;
            final String avatarName = "avatar_" + index;
            avatarViews[i].setOnClickListener(v -> selectAvatar(v, avatarName));
        }
    }

    private void selectAvatar(View v, String avatarName) {
        if (selectedAvatarView != null) {
            selectedAvatarView.setBackgroundResource(R.drawable.avatar_border);
        }
        selectedAvatarView = (ImageView) v;
        selectedAvatarView.setBackgroundResource(R.drawable.avatar_border_selected);
        selectedAvatarName = avatarName;
    }

    private void registerUser() {
        String email = emailInput.getText().toString().trim();
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString();
        String confirmPassword = confirmPasswordInput.getText().toString();

        if (!validateFields(email, username, password, confirmPassword)) {
            return;
        }

        registerButton.setEnabled(false);

        authService.attemptRegistration(email, username, password, selectedAvatarName, new AuthService.AuthStatusListener() {
            @Override
            public void onSuccess(String message) {
                registerButton.setEnabled(true);
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();

                if (requireActivity() instanceof AuthenticationActivity) {
                    ((AuthenticationActivity) requireActivity()).navigateToLogin();
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                registerButton.setEnabled(true);
                Toast.makeText(requireContext(), "Greška: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean validateFields(String email, String username, String password, String confirmPassword) {
        boolean valid = true;

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Neispravan email");
            valid = false;
        }
        if (username.isEmpty() || username.length() < 3) {
            usernameInput.setError("Korisničko ime mora imati min 3 karaktera");
            valid = false;
        }
        if (password.length() < 6) {
            passwordInput.setError("Lozinka mora imati najmanje 6 karaktera");
            valid = false;
        }
        if (!password.equals(confirmPassword)) {
            confirmPasswordInput.setError("Lozinke se ne poklapaju");
            valid = false;
        }
        return valid;
    }
}