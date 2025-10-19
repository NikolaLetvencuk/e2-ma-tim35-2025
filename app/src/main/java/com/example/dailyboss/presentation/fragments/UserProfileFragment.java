package com.example.dailyboss.presentation.fragments;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.Color; // Dodato za boju QR koda
import android.os.Bundle;
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
import com.example.dailyboss.data.repository.UserStatisticRepository;
import com.example.dailyboss.domain.model.User;
import com.example.dailyboss.domain.model.UserStatistic;
import com.example.dailyboss.presentation.fragments.EquipmentFragment; // Dodato
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

    // private Button btnChangePassword; // Uklonjeno, jer je opcija u meniju

    private UserRepository userRepository;
    private UserStatisticRepository statRepository;

    private User currentUser;
    private UserStatistic currentStats;

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
        // btnChangePassword = view.findViewById(R.id.btnChangePassword); // Ovo dugme je sada sakriveno u XML-u, ili ga mozes ukloniti

        userRepository = new UserRepository(requireContext());
        statRepository = new UserStatisticRepository(requireContext());

        loadUserData();

        // Listener za dugme opcija (tri tačke)
        btnOptions.setOnClickListener(v -> showOptionsMenu(v));

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
        String userId = prefs.getLoggedInUserId();

        if (userId == null) {
            Toast.makeText(getContext(), "Nema ulogovanog korisnika", Toast.LENGTH_SHORT).show();
            return;
        }

        userRepository.getUserData(userId, new UserRepository.UserDataListener() {
            @Override
            public void onSuccess(User user) {
                currentUser = user;
                tvUsername.setText(user.getUsername());
                // btnChangePassword.setEnabled(true); // Dugme je sada u meniju, ne treba ga direktno enable/disable
                String avatarName = user.getAvatar(); // npr. "avatar_1"
                int resId = getResources().getIdentifier(avatarName, "drawable", requireContext().getPackageName());
                if (resId != 0) {
                    imgAvatar.setImageResource(resId);
                } else {
                    imgAvatar.setImageResource(R.drawable.avatar_1); // default avatar
                }
                // QR kod se vise ne generise odmah ovde, vec na zahtev
                // String qrData = "user_id:" + userId;
                // generateQRCode(qrData); // Uklonjeno

                loadUserBadgesFragment();
                loadUserEquipmentFragment();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getContext(), "Greška pri učitavanju korisnika", Toast.LENGTH_SHORT).show();
            }
        });

        // Isto za statistiku
        statRepository.getUserStatistic(userId, new UserStatisticRepository.UserStatisticListener() {
            @Override
            public void onSuccess(UserStatistic statistic) {
                currentStats = statistic;
                tvLevelTitle.setText("Level " + statistic.getLevel() + " - Početna titula");
                tvXP.setText("XP: " + statistic.getExperiencePoints());
                tvPP.setText("PP: " + statistic.getWinStreak()); // Pretpostavljam da winStreak predstavlja Power Points
                tvCoins.setText("Coins: 0"); // TODO: Dodao sam coins ovde ako statistika sadrzi
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
        popup.getMenuInflater().inflate(R.menu.profile_options_menu, popup.getMenu()); // Uveriti se da je ovo tačan put do menija

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId(); // Koristi itemId direktno

                if (itemId == R.id.action_show_qr) {
                    showQrCodeDialog();
                    return true;
                } else if (itemId == R.id.action_change_password) {
                    showChangePasswordDialog();
                    return true;
                }
                return false;
            }
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
        String userId = new SharedPreferencesHelper(requireContext()).getLoggedInUserId();
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

        // Proverite da li je fragment_user_profile ID root elementa ili sadrži fragmentUserBadges
        // Ako je fragment_user_profile layout ime datoteke, onda je R.id.fragmentUserBadges ID za Framelayout unutar tog XML-a
        if (isAdded() && getParentFragmentManager() != null) { // Provera da je fragment prikačen
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentUserBadges, badgesFragment)
                    .commit();
        }
    }

    private void loadUserEquipmentFragment() {
        EquipmentFragment equipmentFragment = new EquipmentFragment();

        Bundle args = new Bundle();
        args.putString("userId", currentUser.getId());
        equipmentFragment.setArguments(args);

        if (isAdded() && getParentFragmentManager() != null) { // Provera da je fragment prikačen
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentUserEqupiment, equipmentFragment)
                    .commit();
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
}