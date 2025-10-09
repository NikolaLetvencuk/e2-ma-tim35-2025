package com.example.dailyboss.presentation.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton; // Dodato za dugme za zatvaranje
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment; // Promenjeno u DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailyboss.R;
import com.example.dailyboss.data.repository.FriendshipRepository;
import com.example.dailyboss.domain.model.Friendship;
import com.example.dailyboss.domain.model.User;
import com.example.dailyboss.presentation.adapters.UserSearchAdapter;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import com.google.zxing.BarcodeFormat;
import java.util.Arrays;
import java.util.List;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Fragment za pretragu korisnika po imenu i slanje zahteva za prijateljstvo.
 */
public class UserSearchDialogFragment extends DialogFragment implements UserSearchAdapter.OnUserActionListener { // Promenjeno u DialogFragment

    private EditText etSearchUsername;
    private RecyclerView recyclerViewResults;
    private TextView tvNoSearchResults;
    private ProgressBar progressBar;
    private Button btnSearch;
    private ImageButton btnClose; // Dodato dugme za zatvaranje
    private UserSearchAdapter adapter;
    private FriendshipRepository friendshipRepository;
    private ImageButton btnScanQr;
    private String currentUserId;
    private ActivityResultLauncher<ScanOptions> qrCodeLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Koristimo FriendshipRepository koji ima metodu za pretragu korisnika na Firebase-u
        friendshipRepository = new FriendshipRepository(requireContext());
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            currentUserId = "guest_user";
            Toast.makeText(getContext(), "Korisnik nije prijavljen. Statistika možda neće biti dostupna.", Toast.LENGTH_LONG).show();
        }

        qrCodeLauncher = registerForActivityResult(new ScanContract(), result -> {
            if (result.getContents() == null) {
                // Skeniranje otkazano od strane korisnika (npr., klikom na dugme za nazad)
                Toast.makeText(getContext(), "Skeniranje otkazano.", Toast.LENGTH_LONG).show();
            } else {
                // USPEŠNO SKENIRANJE!
                String qrData = result.getContents();
                processQrData(qrData); // Pozivanje vaše metode za dekodiranje
            }
        });
        // Postavi stil za dijalog da bude fullscreen ili prilagodljiv, bez naslova
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_users, container, false);

        etSearchUsername = view.findViewById(R.id.etSearchUsername);
        recyclerViewResults = view.findViewById(R.id.recyclerViewSearchResults);
        tvNoSearchResults = view.findViewById(R.id.tvNoSearchResults);
        progressBar = view.findViewById(R.id.progressBar);
        btnSearch = view.findViewById(R.id.btnSearch);
        btnClose = view.findViewById(R.id.btnClose); // Inicijalizacija dugmeta za zatvaranje
        btnScanQr = view.findViewById(R.id.btnScanQr);

        setupRecyclerView();
        // Uklonjen setupSearchListener jer ne želimo pretragu tokom kucanja

        // Listener za dugme za zatvaranje
        btnClose.setOnClickListener(v -> dismiss()); // Zatvara dijalog

        // Listener za dugme "Pretraži"
        btnSearch.setOnClickListener(v -> {
            String query = etSearchUsername.getText().toString().trim();
            if (!query.isEmpty()) {
                searchUsers(query);
            } else {
                Toast.makeText(getContext(), "Unesite korisničko ime za pretragu.", Toast.LENGTH_SHORT).show();
                adapter.updateUsers(new ArrayList<>());
                tvNoSearchResults.setVisibility(View.GONE);
            }
        });
        btnScanQr.setOnClickListener(v -> startQrScannerActivity());

        return view;
    }

    private void startQrScannerActivity() {
        if (currentUserId.equals("guest_user")) {
            Toast.makeText(getContext(), "Morate biti prijavljeni da biste dodali prijatelja.", Toast.LENGTH_LONG).show();
            return;
        }

        ScanOptions options = new ScanOptions();
        options.setPrompt("Skenirajte QR kod prijatelja");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(com.journeyapps.barcodescanner.CaptureActivity.class);

        List<String> formats = Arrays.asList(BarcodeFormat.QR_CODE.name());
        options.setDesiredBarcodeFormats(formats);

        qrCodeLauncher.launch(options);
    }

    private void processQrData(String qrData) {
        if (TextUtils.isEmpty(qrData) || !qrData.contains("user_id:")) {
            Toast.makeText(getContext(), "Neispravan QR kod.", Toast.LENGTH_LONG).show();
            return;
        }

        // Dekodiranje
        String targetUserId = null;
        String targetUsername = null;

        try {
            // Podela na komponente (npr., "user_id:user_c456" i "username:SuperUser")
            String[] parts = qrData.split(",");

            for (String part : parts) {
                if (part.startsWith("user_id:")) {
                    targetUserId = part.substring("user_id:".length()).trim();
                } else if (part.startsWith("username:")) {
                    targetUsername = part.substring("username:".length()).trim();
                }
            }

        } catch (Exception e) {
            Log.e("SearchFragment", "Error processing QR data: " + e.getMessage());
            Toast.makeText(getContext(), "Greška pri čitanju QR podataka.", Toast.LENGTH_LONG).show();
            return;
        }

        if (targetUserId == null) {
            Toast.makeText(getContext(), "QR kod ne sadrži ID korisnika.", Toast.LENGTH_LONG).show();
            return;
        }

        if (targetUserId.equals(currentUserId)) {
            Toast.makeText(getContext(), "Ne možete dodati sebe.", Toast.LENGTH_LONG).show();
            return;
        }

        // AKCIJA: Slanje zahteva za prijateljstvo na osnovu dekodiranog ID-ja
        sendFriendRequestByQr(targetUserId, targetUsername);
    }

    // U UserSearchDialogFragment.java

    private void sendFriendRequestByQr(String targetUserId, String targetUsername) {
        progressBar.setVisibility(View.VISIBLE);
        String usernameDisplay = targetUsername != null ? targetUsername : targetUserId.substring(0, 5) + "...";

        Tasks.call(Executors.newSingleThreadExecutor(), () -> {
                    Friendship existing = friendshipRepository.getExistingFriendship(currentUserId, targetUserId);

                    if (existing != null) {
                        return existing.getStatus();
                    }

                    friendshipRepository.sendFriendRequest(currentUserId, targetUserId);
                    return Friendship.STATUS_PENDING; // Vrati PENDING ako je novo slanje uspešno
                })
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (isAdded() && task.isSuccessful()) {
                        String status = (String) task.getResult(); // Dohvatamo vraćeni status

                        if (status.equals(Friendship.STATUS_ACCEPTED)) {
                            Toast.makeText(getContext(), targetUsername + " je već Vaš prijatelj.", Toast.LENGTH_LONG).show();
                        } else if (status.equals(Friendship.STATUS_PENDING)) {
                            // Ako je vraćen status PENDING, ili je novo poslato ili već postoji
                            Toast.makeText(getContext(), "Zahtev poslat korisniku: " + usernameDisplay, Toast.LENGTH_LONG).show();
                        } else {
                            // Zahtev je možda već poslat i čeka se (ako status nije ni ACCEPTED ni PENDING - što se ne bi trebalo desiti ovde)
                            Toast.makeText(getContext(), "Zahtev je već u toku.", Toast.LENGTH_LONG).show();
                        }
                        dismiss(); // Zatvori dijalog

                    } else if (isAdded()) {
                        Toast.makeText(getContext(), "Neuspešno slanje zahteva putem QR koda.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void setupRecyclerView() {
        adapter = new UserSearchAdapter(new ArrayList<>(), this);
        recyclerViewResults.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewResults.setAdapter(adapter);
    }

    // Uklonjen setupSearchListener jer se pretraga vrši samo na klik dugmeta

    private void searchUsers(String query) {
        progressBar.setVisibility(View.VISIBLE);
        tvNoSearchResults.setVisibility(View.GONE);
        adapter.updateUsers(new ArrayList<>()); // Očisti prethodne rezultate

        // Poziv ka Repository-ju (koji bi trebalo da pozove FirebaseDataSource.searchUsersByUsername)
        friendshipRepository.searchUsers(query)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful() && isAdded()) {
                        List<User> results = task.getResult();
                        // Filtriramo trenutnog korisnika iz rezultata
                        List<User> filteredResults = new ArrayList<>();
                        for (User user : results) {
                            if (!user.getId().equals(currentUserId)) {
                                filteredResults.add(user);
                            }
                        }

                        adapter.updateUsers(filteredResults);
                        tvNoSearchResults.setVisibility(filteredResults.isEmpty() ? View.VISIBLE : View.GONE);

                    } else if (isAdded()) {
                        Log.e("UserSearch", "Search failed", task.getException());
                        Toast.makeText(getContext(), "Pretraga neuspešna. Proverite konekciju.", Toast.LENGTH_SHORT).show();
                        adapter.updateUsers(new ArrayList<>());
                        tvNoSearchResults.setVisibility(View.VISIBLE);
                    }
                });
    }

    // ==========================================================
    // USER ACTION LISTENER
    // ==========================================================

    // U UserSearchDialogFragment.java

    @Override
    public void onSendRequest(User user) {
        if (user.getId().equals(currentUserId)) {
            Toast.makeText(getContext(), "Ne možete poslati zahtev sebi!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        Tasks.call(Executors.newSingleThreadExecutor(), () -> {
                    // ⭐ KORAK 1: Provera da li prijateljstvo već postoji u KEŠU
                    Friendship existing = friendshipRepository.getExistingFriendship(currentUserId, user.getId());

                    if (existing != null) {
                        // Vrati STATUS ako veza već postoji (ACCEPTED ili PENDING)
                        return existing.getStatus();
                    }

                    // KORAK 2: Ako ne postoji, pošalji novi zahtev
                    friendshipRepository.sendFriendRequest(currentUserId, user.getId());
                    return "NEWLY_SENT"; // Koristimo specifičnu nižu za novi status
                })
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (isAdded() && task.isSuccessful()) {
                        String status = (String) task.getResult();

                        if (status.equals(Friendship.STATUS_ACCEPTED)) {
                            // Već prijatelji
                            Toast.makeText(getContext(), user.getUsername() + " je već Vaš prijatelj.", Toast.LENGTH_LONG).show();
                        } else if (status.equals(Friendship.STATUS_PENDING)) {
                            // Zahtev je već poslat (i čeka)
                            Toast.makeText(getContext(), "Zahtev je već poslat korisniku " + user.getUsername() + " i čeka na odgovor.", Toast.LENGTH_LONG).show();
                        } else if (status.equals("NEWLY_SENT")) {
                            // Tek poslat zahtev (uspeh)
                            Toast.makeText(getContext(), "Zahtev za prijateljstvo poslat korisniku " + user.getUsername(), Toast.LENGTH_SHORT).show();

                            // ⭐ Opcionalno: Ažuriranje UI u listi (da se onemogući dugme)
                            // adapter.notifyDataSetChanged();
                        }

                    } else if (isAdded()) {
                        // Neuspeh u slanju ili problem sa kešom
                        Toast.makeText(getContext(), "Neuspešno slanje zahteva. Proverite konekciju.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    // Da bi DialogFragment zauzeo širinu ekrana
    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.WRAP_CONTENT; // Može biti i MATCH_PARENT ako želiš fullscreen
            getDialog().getWindow().setLayout(width, height);
        }
    }
}