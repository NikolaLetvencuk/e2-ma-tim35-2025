package com.example.dailyboss.presentation.fragments;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.Color; // Dodato za boju QR koda
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem; // Dodato za PopupMenu
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu; // Dodato za PopupMenu
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable; // Dodato za @Nullable
import androidx.fragment.app.Fragment;

import com.example.dailyboss.R;
import com.example.dailyboss.data.SharedPreferencesHelper;
import com.example.dailyboss.data.repository.UserRepository;
import com.example.dailyboss.data.repository.UserProfileRepository;
import com.example.dailyboss.data.repository.UserStatisticRepository;
import com.example.dailyboss.domain.model.User;
import com.example.dailyboss.domain.model.UserProfile;
import com.example.dailyboss.domain.model.UserStatistic;
import com.google.android.material.textfield.TextInputEditText;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;

public class UserProfileFragment extends Fragment {

    private ImageView imgAvatar;
    private TextView tvUsername, tvLevelTitle, tvXP;
    private TextView tvPP, tvCoins;

    private ImageView btnOptions;
    private Button btnViewLevelProgress;

    private UserRepository userRepository;
    private UserProfileRepository statRepository;
    private UserStatisticRepository userStatisticRepository;

    private User currentUser;
    private UserProfile currentStats;
    private String displayUserId;
    private boolean isOwnProfile;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);

        imgAvatar = view.findViewById(R.id.imgAvatar);
        tvUsername = view.findViewById(R.id.tvUsername);
        tvLevelTitle = view.findViewById(R.id.tvLevelTitle);
        tvXP = view.findViewById(R.id.tvXP);
        tvPP = view.findViewById(R.id.tvPP);
        tvCoins = view.findViewById(R.id.tvCoins);
        btnOptions = view.findViewById(R.id.btnOptions);
        btnViewLevelProgress = view.findViewById(R.id.btnViewLevelProgress);

        userRepository = new UserRepository(requireContext());
        statRepository = new UserProfileRepository(requireContext());
        userStatisticRepository = new UserStatisticRepository(requireContext());

        Bundle args = getArguments();
        String loggedInUserId = new SharedPreferencesHelper(requireContext()).getLoggedInUserId();

        if (args != null && args.getString("targetUserId") != null) {
            displayUserId = args.getString("targetUserId");
            isOwnProfile = displayUserId.equals(loggedInUserId);
        } else {
            displayUserId = loggedInUserId;
            isOwnProfile = true;
        }

        loadUserData();

        if (isOwnProfile) {
            btnOptions.setVisibility(View.VISIBLE);
            btnOptions.setOnClickListener(this::showOptionsMenu);
        } else {
            btnOptions.setVisibility(View.GONE);
        }

        btnViewLevelProgress.setOnClickListener(v -> openLevelProgressFragment());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void loadUserData() {
        String userId = displayUserId;

        if (userId == null) {
            Toast.makeText(getContext(), "Nema ulogovanog korisnika", Toast.LENGTH_SHORT).show();
            return;
        }

        userRepository.getUserData(userId, new UserRepository.UserDataListener() {
            @Override
            public void onSuccess(User user) {
                if (!isAdded()) return;
                currentUser = user;

                tvUsername.setText(user.getUsername());

                String avatarName = user.getAvatar();
                int resId = getResources().getIdentifier(avatarName, "drawable", requireContext().getPackageName());
                imgAvatar.setImageResource(resId != 0 ? resId : R.drawable.avatar_1);

                loadUserBadgesFragment();
                loadUserEquipmentFragment();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Greška pri učitavanju korisnika", Toast.LENGTH_SHORT).show();
            }
        });

        statRepository.getUserStatistic(userId, new UserProfileRepository.UserStatisticListener() {
            @Override
            public void onSuccess(UserProfile statistic) {
                if (!isAdded()) return;
                currentStats = statistic;
                UserStatistic userStatistic = userStatisticRepository.getUserStatistic(statistic.getUserId());

                com.example.dailyboss.service.LevelingService levelingService = new com.example.dailyboss.service.LevelingService(requireContext());
                String currentTitle = levelingService.getTitleForLevel(statistic.getLevel());

                tvLevelTitle.setText("Level " + statistic.getLevel() + " - " + currentTitle);
                tvXP.setText("XP: " + statistic.getExperiencePoints());

                if (isOwnProfile) {
                    tvPP.setVisibility(View.VISIBLE);
                    tvCoins.setVisibility(View.VISIBLE);
                    tvPP.setText("PP: " + userStatistic.getPowerPoints());
                    tvCoins.setText("Coins: " + userStatistic.getCoins());
                } else {
                    // Ako je tuđi profil, sakrij Power Points i Coins
                    tvPP.setVisibility(View.GONE);
                    tvCoins.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Greška pri učitavanju statistike", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void showOptionsMenu(View v) {
        PopupMenu popup = new PopupMenu(getContext(), v);

        if (isOwnProfile) {
            popup.getMenuInflater().inflate(R.menu.profile_options_menu, popup.getMenu());
        } else {
            popup.getMenuInflater().inflate(R.menu.friend_profile_options_menu, popup.getMenu());
        }

        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.action_show_qr) {
                showQrCodeDialog();
                return true;
            } else if (itemId == R.id.action_change_password) {
                showChangePasswordDialog();
                return true;
            } else if (itemId == R.id.action_statistics) {
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new StatisticsFragment())
                        .addToBackStack(null)
                        .commit();
                return true;
            } else if (itemId == R.id.action_logout) {
                performLogout();
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void showQrCodeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_show_qr_code, null);
        ImageView qrImage = dialogView.findViewById(R.id.dialog_qr_code_image);
        TextView qrInfo = dialogView.findViewById(R.id.dialog_qr_info_text);

        String qrData = "user_id:" + displayUserId;
        if (currentUser != null) {
            qrData = "user_id:" + currentUser.getId() + ",username:" + currentUser.getUsername();
        }

        try {
            Bitmap qrBitmap = generateQrCodeBitmap(qrData, 400, 400);
            qrImage.setImageBitmap(qrBitmap);
            qrInfo.setText("Skeniraj za dodavanje " + (currentUser != null ? currentUser.getUsername() : "prijatelja") + "!");
        } catch (WriterException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Greška pri generisanju QR koda.", Toast.LENGTH_SHORT).show();
            qrInfo.setText("Greška pri generisanju QR koda.");
        }

        builder.setView(dialogView)
                .setTitle("QR kod")
                .setPositiveButton("Zatvori", (dialog, id) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private Bitmap generateQrCodeBitmap(String text, int width, int height) throws WriterException {
        QRCodeWriter writer = new QRCodeWriter();
        com.google.zxing.common.BitMatrix bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, width, height);
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE;
            }
        }
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.RGB_565);
    }

    private void loadUserBadgesFragment() {
        UserBadgesFragment badgesFragment = new UserBadgesFragment();
        Bundle args = new Bundle();
        args.putString("userId", currentUser.getId());
        badgesFragment.setArguments(args);

        if (isAdded()) {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.fragmentUserBadges, badgesFragment)
                    .commit();
        }
    }

    private void loadUserEquipmentFragment() {
        EquipmentFragment equipmentFragment = new EquipmentFragment();
        String targetId = displayUserId;

        Bundle args = new Bundle();
        args.putString("userId", targetId);
        equipmentFragment.setArguments(args);

        if (isAdded()) {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.fragmentUserEqupiment, equipmentFragment)
                    .commit();
        }
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_password, null); // Pretpostavka da imas dialog_change_password.xml

        TextInputEditText etOld = dialogView.findViewById(R.id.etOldPassword);
        TextInputEditText etNew1 = dialogView.findViewById(R.id.etNewPassword);
        TextInputEditText etNew2 = dialogView.findViewById(R.id.etConfirmPassword);

        builder.setView(dialogView)
                .setTitle("Promeni lozinku")
                .setPositiveButton("Sačuvaj", null)
                .setNegativeButton("Otkaži", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> {
            Button btnPositive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btnPositive.setOnClickListener(v -> {
                String oldPass = etOld.getText().toString().trim();
                String newPass1 = etNew1.getText().toString().trim();
                String newPass2 = etNew2.getText().toString().trim();

                if (oldPass.isEmpty() || newPass1.isEmpty() || newPass2.isEmpty()) {
                    Toast.makeText(getContext(), "Popunite sva polja", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!newPass1.equals(newPass2)) {
                    Toast.makeText(getContext(), "Nove lozinke se ne poklapaju", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (currentUser != null && !currentUser.getPassword().equals(oldPass)) { // OVA PROVERA NIJE SIGURNA! Password se NE ČUVA u User objektu na klijentu!
                    Toast.makeText(getContext(), "Pogrešna stara lozinka", Toast.LENGTH_SHORT).show();
                    return;
                }

                userRepository.changeUserPassword(currentUser.getId(), newPass1, new UserRepository.PasswordChangeListener() {
                    @Override
                    public void onSuccess(String message) {
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });

        dialog.show();
    }

    private void openLevelProgressFragment() {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new LevelProgressFragment())
                .addToBackStack(null)
                .commit();
    }

    private void performLogout() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Odjava")
                .setMessage("Da li si siguran da želiš da se odjaviš?")
                .setPositiveButton("Da", (dialog, which) -> {
                    SharedPreferencesHelper prefs = new SharedPreferencesHelper(requireContext());
                    prefs.clearLoggedInUser();
                    
                    com.example.dailyboss.service.AuthService authService =
                            new com.example.dailyboss.service.AuthService(requireContext());
                    authService.logout();
                    
                    android.content.Intent intent = new android.content.Intent(
                            requireActivity(), 
                            com.example.dailyboss.presentation.activities.AuthenticationActivity.class
                    );
                    intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | 
                                   android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                    
                    Toast.makeText(getContext(), "Uspešno si se odjavio", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Otkaži", null)
                .show();
    }
}