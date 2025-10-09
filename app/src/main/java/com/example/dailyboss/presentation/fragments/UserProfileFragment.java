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
import com.google.zxing.qrcode.QRCodeWriter; // Dodato, umesto BarcodeEncoder
// import com.journeyapps.barcodescanner.BarcodeEncoder; // Uklonjeno, koristimo QRCodeWriter direktno

public class UserProfileFragment extends Fragment {

    private ImageView imgAvatar;
    private TextView tvUsername, tvLevelTitle, tvXP, tvPP, tvCoins;
    // imgQRCode vise nije direktno u layoutu, ali ga mozemo koristiti za generisanje
    // private ImageView imgQRCode; // Uklonjeno, jer se QR kod prikazuje u dijalogu
    private ImageView btnOptions; // NOVO: Dugme za tri tacke (options menu)
    private Button btnViewLevelProgress; // NOVO: Dugme za prikaz napredovanja

    // private Button btnChangePassword; // Uklonjeno, jer je opcija u meniju

    private UserRepository userRepository;
    private UserProfileRepository statRepository;
    private UserStatisticRepository userStatisticRepository;

    private User currentUser;
    private UserProfile currentStats;
    private String displayUserId; // ID korisnika čiji se profil prikazuje
    private boolean isOwnProfile;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false); // Pretpostavka da je fragment_user_profile tvoj azurirani XML

        imgAvatar = view.findViewById(R.id.imgAvatar);
        // imgQRCode = view.findViewById(R.id.qr_code_image); // Nema ga vise u glavnom layoutu, ukloni ga
        tvUsername = view.findViewById(R.id.tvUsername);
        tvLevelTitle = view.findViewById(R.id.tvLevelTitle);
        tvXP = view.findViewById(R.id.tvXP);
        tvPP = view.findViewById(R.id.tvPP);
        tvCoins = view.findViewById(R.id.tvCoins);
        btnOptions = view.findViewById(R.id.btnOptions); // Inicijalizacija novog dugmeta za opcije
        btnViewLevelProgress = view.findViewById(R.id.btnViewLevelProgress); // NOVO
        // btnChangePassword = view.findViewById(R.id.btnChangePassword); // Ovo dugme je sada sakriveno u XML-u, ili ga mozes ukloniti

        userRepository = new UserRepository(requireContext());
        statRepository = new UserProfileRepository(requireContext());
        userStatisticRepository = new UserStatisticRepository(requireContext());
        Bundle args = getArguments();
        if (args != null && args.getString("targetUserId") != null) {
            displayUserId = args.getString("targetUserId");
            Log.d("TAG", "onCreateView: " + displayUserId);
            isOwnProfile = displayUserId.equals(new SharedPreferencesHelper(requireContext()).getLoggedInUserId());
        } else {
            displayUserId = new SharedPreferencesHelper(requireContext()).getLoggedInUserId();
            Log.d("TAG", "onCreateView22: " + displayUserId + args);
            isOwnProfile = true;
        }
        loadUserData();
        btnOptions.setVisibility(View.VISIBLE);
        btnOptions.setOnClickListener(this::showOptionsMenu);


        // NOVO: Listener za prikaz napredovanja kroz nivoe
        btnViewLevelProgress.setOnClickListener(v -> openLevelProgressFragment());

        // btnChangePassword.setOnClickListener(v -> showChangePasswordDialog()); // Ovaj listener se sada premesta u PopupMenu

        return view;
    }

    // Dodato za onViewCreated kako bi se pozvao super metod
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Ovde mozes dodati dodatnu logiku koja zavisi od view-a nakon sto je kreiran
    }


    private void loadUserData() {
        SharedPreferencesHelper prefs = new SharedPreferencesHelper(requireContext());
        String userId = displayUserId;

        if (userId == null) {
            Toast.makeText(getContext(), "Nema ulogovanog korisnika", Toast.LENGTH_SHORT).show();
            return;
        }

        userRepository.getUserData(userId, new UserRepository.UserDataListener() {
            @Override
            public void onSuccess(User user) {
                if (!isAdded()) return; // fragment nije attach-ovan, prekini
                currentUser = user;

                tvUsername.setText(user.getUsername());

                String avatarName = user.getAvatar();
                int resId = getResources().getIdentifier(avatarName, "drawable", requireContext().getPackageName());
                imgAvatar.setImageResource(resId != 0 ? resId : R.drawable.avatar_1);

                // Učitaj fragmente samo jednom (proveri da li već postoje)
                if (getChildFragmentManager().findFragmentById(R.id.fragmentUserBadges) == null) {
                    loadUserBadgesFragment();
                }
                loadUserEquipmentFragment();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Greška pri učitavanju korisnika", Toast.LENGTH_SHORT).show();
            }
        });

        // Isto za statistiku
        statRepository.getUserStatistic(userId, new UserProfileRepository.UserStatisticListener() {
            @Override
            public void onSuccess(UserProfile statistic) {
                if (!isAdded()) return;
                currentStats = statistic;
                UserStatistic userStatistic = userStatisticRepository.getUserStatistic(statistic.getUserId());
                
                // Dobij trenutnu titulu na osnovu nivoa
                com.example.dailyboss.service.LevelingService levelingService = new com.example.dailyboss.service.LevelingService(requireContext());
                String currentTitle = levelingService.getTitleForLevel(statistic.getLevel());
                
                tvLevelTitle.setText("Level " + statistic.getLevel() + " - " + currentTitle);
                tvXP.setText("XP: " + statistic.getExperiencePoints());
                tvPP.setText("PP: " + userStatistic.getPowerPoints());
                tvCoins.setText("Coins: " + userStatistic.getCoins());
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Greška pri učitavanju statistike", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // NOVA metoda za prikaz menija sa opcijama
    private void showOptionsMenu(View v) {
        PopupMenu popup = new PopupMenu(getContext(), v);

        // 🚀 NOVO: Učitavamo meni opcija samo ako je to sopstveni profil
        if (isOwnProfile) {
            popup.getMenuInflater().inflate(R.menu.profile_options_menu, popup.getMenu());
        } else {
            // Meni za prijatelja - može biti samo opcija za QR kod ako to želite da zadržite
            popup.getMenuInflater().inflate(R.menu.friend_profile_options_menu, popup.getMenu());
        }

        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();

            // Logika za sopstveni profil (koja uključuje sve opcije)
            if (isOwnProfile) {
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
                // Logika za profil prijatelja (ako ste mu ostavili neku opciju, npr. QR)
            } else {
                if (itemId == R.id.action_show_qr) {
                    // Prikaz QR koda za prijatelja, ako je to logično
                    showQrCodeDialog();
                    return true;
                }
            }
            return false;
        });
        popup.show();
    }

    // PREMEŠTENA i IZMENJENA metoda za generisanje i prikaz QR koda u dijalogu
    private void showQrCodeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_show_qr_code, null); // Layout za QR dijalog
        ImageView qrImage = dialogView.findViewById(R.id.dialog_qr_code_image);
        TextView qrInfo = dialogView.findViewById(R.id.dialog_qr_info_text);

        // Generisanje QR koda
        String userId = displayUserId;
        String qrData = "user_id:" + userId; // Podaci za QR kod, npr. ID korisnika
        if (currentUser != null) {
            qrData = "user_id:" + currentUser.getId() + ",username:" + currentUser.getUsername(); // Detaljniji QR kod
        }

        try {
            // Koristimo QRCodeWriter direktno iz zxing-core
            Bitmap qrBitmap = generateQrCodeBitmap(qrData, 400, 400); // 400x400 piksela
            qrImage.setImageBitmap(qrBitmap);
            qrInfo.setText("Skeniraj za dodavanje " + (currentUser != null ? currentUser.getUsername() : "prijatelja") + "!");
        } catch (WriterException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Greška pri generisanju QR koda.", Toast.LENGTH_SHORT).show();
            qrInfo.setText("Greška pri generisanju QR koda.");
        }

        builder.setView(dialogView)
                .setTitle("Vaš QR kod")
                .setPositiveButton("Zatvori", (dialog, id) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Pomoćna metoda za generisanje QR koda (prilagođena za zxing-core)
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


    // Uklonjena stara generateQRCode metoda jer se koristi nova generateQrCodeBitmap
    /*
    private void generateQRCode(String data) {
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(data, BarcodeFormat.QR_CODE, 400, 400);
            imgQRCode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }
    */

    private void loadUserBadgesFragment() {
        UserBadgesFragment badgesFragment = new UserBadgesFragment();

        Bundle args = new Bundle();
        args.putString("userId", currentUser.getId());
        badgesFragment.setArguments(args);

        // Koristi getChildFragmentManager() umesto getParentFragmentManager()
        // Ovo osigurava da su child fragmenti pravilno vezani za parent fragment
        if (isAdded()) {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.fragmentUserBadges, badgesFragment)
                    .commit();
        }
    }

    // U UserProfileFragment.java

    private void loadUserEquipmentFragment() {
        // 💡 TRENUTNO POGREŠNO: Koristite ID ulogovanog korisnika (currentUser.getId())
        // android.util.Log.d("UserProfileFragment", "Učitavam EquipmentFragment za userId: " + currentUser.getId());
        // Log.d("TAG", "loadUserEquipmentFragment: " + currentUser.getId());

        // ✅ ISPRAVKA: Koristite ID korisnika čiji se profil prikazuje (displayUserId)

        EquipmentFragment equipmentFragment = new EquipmentFragment();

        // Proveravamo da li je displayUserId postavljen i koristimo ga
        String targetId = displayUserId; // Ovo je ID koji želimo da prosledimo

        android.util.Log.d("UserProfileFragment", "Učitavam EquipmentFragment za target userId: " + targetId);

        Bundle args = new Bundle();
        // 💥 KRITIČNA PROMENA: Prosledite displayUserId, a ne uvek currentUser.getId()
        args.putString("userId", targetId);
        equipmentFragment.setArguments(args);

        // Koristi getChildFragmentManager() umesto getParentFragmentManager()
        if (isAdded()) {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.fragmentUserEqupiment, equipmentFragment)
                    .commit();
            android.util.Log.d("UserProfileFragment", "EquipmentFragment uspešno učitan");
        } else {
            android.util.Log.d("UserProfileFragment", "Fragment nije added, ne mogu da učitam EquipmentFragment");
        }
    }

    // Metoda za promenu lozinke ostaje ista, samo je poziv premešten
    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_password, null); // Pretpostavka da imas dialog_change_password.xml

        TextInputEditText etOld = dialogView.findViewById(R.id.etOldPassword);
        TextInputEditText etNew1 = dialogView.findViewById(R.id.etNewPassword);
        TextInputEditText etNew2 = dialogView.findViewById(R.id.etConfirmPassword);

        builder.setView(dialogView)
                .setTitle("Promeni lozinku")
                .setPositiveButton("Sačuvaj", null) // Postavljamo null, listener dodajemo kasnije
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

                // Dodaj proveru stare lozinke pre promene
                // U realnosti bi ovo trebalo da se radi na serveru za sigurnost
                if (currentUser != null && !currentUser.getPassword().equals(oldPass)) { // OVA PROVERA NIJE SIGURNA! Password se NE ČUVA u User objektu na klijentu!
                    Toast.makeText(getContext(), "Pogrešna stara lozinka", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Sigurniji pristup bi bio da UserRepository ima metodu changePassword(userId, oldPassword, newPassword, listener)
                // koja šalje i staru lozinku na server radi validacije.

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

    // NOVA metoda za otvaranje LevelProgressFragment-a
    private void openLevelProgressFragment() {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new LevelProgressFragment())
                .addToBackStack(null)
                .commit();
    }

    // NOVA metoda za logout
    private void performLogout() {
        // Prikaži confirmation dialog
        new AlertDialog.Builder(requireContext())
                .setTitle("Odjava")
                .setMessage("Da li si siguran da želiš da se odjaviš?")
                .setPositiveButton("Da", (dialog, which) -> {
                    // Obriši podatke iz SharedPreferences
                    SharedPreferencesHelper prefs = new SharedPreferencesHelper(requireContext());
                    prefs.clearLoggedInUser();
                    
                    // Odjavi se iz Firebase-a
                    com.example.dailyboss.service.AuthService authService = 
                            new com.example.dailyboss.service.AuthService(requireContext());
                    authService.logout();
                    
                    // Vrati na AuthenticationActivity
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