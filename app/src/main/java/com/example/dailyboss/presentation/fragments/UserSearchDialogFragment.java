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


public class UserSearchDialogFragment extends DialogFragment implements UserSearchAdapter.OnUserActionListener { // Promenjeno u DialogFragment

    private EditText etSearchUsername;
    private RecyclerView recyclerViewResults;
    private TextView tvNoSearchResults;
    private ProgressBar progressBar;
    private Button btnSearch;
    private ImageButton btnClose;
    private UserSearchAdapter adapter;
    private FriendshipRepository friendshipRepository;
    private ImageButton btnScanQr;
    private String currentUserId;
    private ActivityResultLauncher<ScanOptions> qrCodeLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        friendshipRepository = new FriendshipRepository(requireContext());
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            currentUserId = "guest_user";
            Toast.makeText(getContext(), "Korisnik nije prijavljen. Statistika možda neće biti dostupna.", Toast.LENGTH_LONG).show();
        }

        qrCodeLauncher = registerForActivityResult(new ScanContract(), result -> {
            if (result.getContents() == null) {
                Toast.makeText(getContext(), "Skeniranje otkazano.", Toast.LENGTH_LONG).show();
            } else {
                String qrData = result.getContents();
                processQrData(qrData); // Pozivanje vaše metode za dekodiranje
            }
        });
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
        btnClose = view.findViewById(R.id.btnClose);
        btnScanQr = view.findViewById(R.id.btnScanQr);

        setupRecyclerView();

        btnClose.setOnClickListener(v -> dismiss());

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

        String targetUserId = null;
        String targetUsername = null;

        try {
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


    private void sendFriendRequestByQr(String targetUserId, String targetUsername) {
        progressBar.setVisibility(View.VISIBLE);
        String usernameDisplay = targetUsername != null ? targetUsername : targetUserId.substring(0, 5) + "...";

        Tasks.call(Executors.newSingleThreadExecutor(), () -> {
                    Friendship existing = friendshipRepository.getExistingFriendship(currentUserId, targetUserId);

                    if (existing != null) {
                        return existing.getStatus();
                    }

                    friendshipRepository.sendFriendRequest(currentUserId, targetUserId);
                    return Friendship.STATUS_PENDING;
                })
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (isAdded() && task.isSuccessful()) {
                        String status = (String) task.getResult();

                        if (status.equals(Friendship.STATUS_ACCEPTED)) {
                            Toast.makeText(getContext(), targetUsername + " je već Vaš prijatelj.", Toast.LENGTH_LONG).show();
                        } else if (status.equals(Friendship.STATUS_PENDING)) {
                            Toast.makeText(getContext(), "Zahtev poslat korisniku: " + usernameDisplay, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getContext(), "Zahtev je već u toku.", Toast.LENGTH_LONG).show();
                        }
                        dismiss();

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


    private void searchUsers(String query) {
        progressBar.setVisibility(View.VISIBLE);
        tvNoSearchResults.setVisibility(View.GONE);
        adapter.updateUsers(new ArrayList<>());

        friendshipRepository.searchUsers(query)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful() && isAdded()) {
                        List<User> results = task.getResult();
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

    @Override
    public void onSendRequest(User user) {
        if (user.getId().equals(currentUserId)) {
            Toast.makeText(getContext(), "Ne možete poslati zahtev sebi!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        Tasks.call(Executors.newSingleThreadExecutor(), () -> {
                    Friendship existing = friendshipRepository.getExistingFriendship(currentUserId, user.getId());

                    if (existing != null) {
                        return existing.getStatus();
                    }

                    friendshipRepository.sendFriendRequest(currentUserId, user.getId());
                    return "NEWLY_SENT";
                })
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (isAdded() && task.isSuccessful()) {
                        String status = (String) task.getResult();

                        if (status.equals(Friendship.STATUS_ACCEPTED)) {
                            Toast.makeText(getContext(), user.getUsername() + " je već Vaš prijatelj.", Toast.LENGTH_LONG).show();
                        } else if (status.equals(Friendship.STATUS_PENDING)) {
                            Toast.makeText(getContext(), "Zahtev je već poslat korisniku " + user.getUsername() + " i čeka na odgovor.", Toast.LENGTH_LONG).show();
                        } else if (status.equals("NEWLY_SENT")) {
                            Toast.makeText(getContext(), "Zahtev za prijateljstvo poslat korisniku " + user.getUsername(), Toast.LENGTH_SHORT).show();

                        }

                    } else if (isAdded()) {
                        Toast.makeText(getContext(), "Neuspešno slanje zahteva. Proverite konekciju.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.WRAP_CONTENT;
            getDialog().getWindow().setLayout(width, height);
        }
    }
}