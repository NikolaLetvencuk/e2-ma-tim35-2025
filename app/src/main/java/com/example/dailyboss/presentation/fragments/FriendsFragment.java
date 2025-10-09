package com.example.dailyboss.presentation.fragments;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailyboss.R;
import com.example.dailyboss.data.SharedPreferencesHelper;
import com.example.dailyboss.data.repository.AllianceRepository; // Dodaj import
import com.example.dailyboss.data.repository.FriendshipRepository;
import com.example.dailyboss.domain.model.Alliance; // Dodaj import
import com.example.dailyboss.domain.model.AllianceInvitation;
import com.example.dailyboss.domain.model.Friendship;
import com.example.dailyboss.domain.model.User;
import com.example.dailyboss.presentation.adapters.FriendAdapter;
import com.example.dailyboss.presentation.adapters.FriendRequestAdapter;
import com.example.dailyboss.service.AllianceNotificationHelper;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;

/**
 * Fragment za prikaz liste prijatelja i pending zahteva za prijateljstvo.
 */
public class FriendsFragment extends Fragment implements FriendRequestAdapter.OnRequestActionListener, FriendAdapter.OnFriendOptionClickListener {

    private RecyclerView recyclerViewRequests;
    private RecyclerView recyclerViewFriends;
    private TextView tvNoRequests;
    private TextView tvNoFriends;
    private ProgressBar progressBar;
    private ImageView friendImage; // Ova promenljiva se ne koristi za ImageView avatar
    private FriendRequestAdapter requestAdapter;
    private FriendAdapter friendAdapter;
    private Button btnAddFriend;
    private Button btnCreateAlliance;
    private FriendshipRepository friendshipRepository;
    private AllianceRepository allianceRepository; // Dodaj AllianceRepository
    private String currentUserId;
    private Alliance currentUsersAlliance; // Dodaj za čuvanje trenutnog saveza korisnika

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        friendshipRepository = new FriendshipRepository(requireContext());
        allianceRepository = new AllianceRepository(requireContext()); // Inicijalizuj AllianceRepository
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            currentUserId = "guest_user";
            Toast.makeText(getContext(), "Korisnik nije prijavljen. Statistika možda neće biti dostupna.", Toast.LENGTH_LONG).show();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends_list, container, false);

        recyclerViewRequests = view.findViewById(R.id.recyclerViewFriendRequests);
        btnAddFriend = view.findViewById(R.id.btnAddFriend);
        recyclerViewFriends = view.findViewById(R.id.recyclerViewFriends);
        tvNoRequests = view.findViewById(R.id.tvNoFriendRequests);
        tvNoFriends = view.findViewById(R.id.tvNoFriends);
        progressBar = view.findViewById(R.id.progressBar);
        btnCreateAlliance = view.findViewById(R.id.btnCreateAlliance);

        setupRecyclerViews();
        loadFriendsData();

        btnAddFriend.setOnClickListener(v -> openSearchDialog());
        btnCreateAlliance.setOnClickListener(v -> {
            Alliance checkAlliance = allianceRepository.getCurrentAlliance(currentUserId);

            if (checkAlliance != null) {
                Toast.makeText(requireContext(), "Već ste član saveza: " + checkAlliance.getName(), Toast.LENGTH_SHORT).show();
                return;
            }
            Log.d("TAG", "onCreateView: Alliance2 " + checkAlliance + "ID:" + currentUserId);
            showCreateAllianceDialog();
        });
        currentUsersAlliance = allianceRepository.getCurrentAlliance(currentUserId);
        return view;
    }

    private void openSearchDialog() {
        UserSearchDialogFragment dialog = new UserSearchDialogFragment();
        dialog.show(getChildFragmentManager(), "SearchFriendsDialog");
    }

    private void showCreateAllianceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_alliance, null);
        builder.setView(dialogView);

        EditText etAllianceName = dialogView.findViewById(R.id.etAllianceName);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelAllianceCreation);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirmAllianceCreation);
        TextInputLayout tilAllianceName = dialogView.findViewById(R.id.tilAllianceName);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            String allianceName = etAllianceName.getText().toString().trim();
            if (allianceName.isEmpty()) {
                tilAllianceName.setError("Naziv saveza ne može biti prazan!");
                Toast.makeText(requireContext(), "Naziv saveza ne može biti prazan!", Toast.LENGTH_SHORT).show();
            } else {
                progressBar.setVisibility(View.VISIBLE);
                allianceRepository.createAlliance(allianceName, currentUserId)
                        .addOnCompleteListener(task -> {
                            progressBar.setVisibility(View.GONE);
                            if (task.isSuccessful() && isAdded()) {
                                Alliance createdAlliance = task.getResult();
                                currentUsersAlliance = createdAlliance; // Ažuriraj trenutni savez
                                Toast.makeText(requireContext(), "Savez '" + createdAlliance.getName() + "' kreiran!", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            } else if (isAdded()) {
                                String errorMessage = task.getException() != null ? task.getException().getMessage() : "Greška pri kreiranju saveza.";
                                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        dialog.show();
    }

    private void setupRecyclerViews() {
        requestAdapter = new FriendRequestAdapter(new ArrayList<>(), new HashMap<>(), this);
        recyclerViewRequests.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewRequests.setAdapter(requestAdapter);
        recyclerViewRequests.setNestedScrollingEnabled(false);

        friendAdapter = new FriendAdapter(new HashMap<>());
        friendAdapter.setOnFriendOptionClickListener(this);
        recyclerViewFriends.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewFriends.setAdapter(friendAdapter);
        recyclerViewFriends.setNestedScrollingEnabled(false);
    }

    private Map<String, Object> loadPendingRequestsFromCache() {
        List<Friendship> pendingRequests = friendshipRepository.getPendingReceivedRequests(currentUserId);
        Map<String, User> senderMap = new HashMap<>();

        for (Friendship request : pendingRequests) {
            User sender = friendshipRepository.getLocalUserDao().getUser(request.getSenderId());
            if (sender != null) {
                senderMap.put(request.getSenderId(), sender);
            } else {
                Log.w("FriendsFragment", "Sender user profile (ID: " + request.getSenderId() + ") not found in local cache.");
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("requests", pendingRequests);
        result.put("senders", senderMap);
        return result;
    }

    private void loadFriendsData() {
        progressBar.setVisibility(View.VISIBLE);

        // Lista svih Task-ova koje čekamo pre isključivanja progress bara
        List<Task<?>> allTasks = new ArrayList<>();

        // =========================================================
        // 1. Učitavanje saveza trenutnog korisnika (currentUsersAlliance)
        // =========================================================
        Task<Alliance> allianceTask = Tasks.call(Executors.newSingleThreadExecutor(),
                        () -> allianceRepository.getCurrentAlliance(currentUserId))
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && isAdded()) {
                        currentUsersAlliance = task.getResult();
                        Log.d("TAG", "loadFriendsData: current" + currentUsersAlliance);
                        // ⭐ NOVO: Ažuriraj UI na osnovu statusa
                        if (currentUsersAlliance != null) {
                            btnCreateAlliance.setText("Prikaži moj Savez"); // Promeni tekst
                            btnCreateAlliance.setOnClickListener(v -> openMyAllianceFragment(currentUsersAlliance.getId())); // Nova akcija
                        } else {
                            btnCreateAlliance.setText("Kreiraj Savez");
                            btnCreateAlliance.setOnClickListener(v -> showCreateAllianceDialog()); // Originalna akcija
                        }
                    } else if (isAdded()) {
                        Log.e("FriendsFragment", "Error loading current user's alliance", task.getException());
                    }
                });

        allTasks.add(allianceTask);

        // =========================================================
        // 2. Učitavanje liste prijatelja i statusa njihovih saveza
        // =========================================================
        Task<Map<User, Alliance>> friendsWithAllianceStatusTask = Tasks.call(Executors.newSingleThreadExecutor(),
                        () -> friendshipRepository.getAcceptedFriendsList(currentUserId))
                .onSuccessTask(friendsList -> {
                    // Kreiramo Listu Taskova za dohvat Saveza za svakog prijatelja
                    List<Task<Void>> allianceStatusTasks = new ArrayList<>();
                    Map<User, Alliance> friendStatusMap = new HashMap<>();

                    for (User friend : friendsList) {
                        if (friend.getAllianceId() != null && !friend.getAllianceId().isEmpty()) {

                            // Pokrećemo asinhroni Task za dohvat saveza prijatelja
                            Task<Void> task = Tasks.call(Executors.newSingleThreadExecutor(),
                                    () -> allianceRepository.getCurrentAlliance(friend.getId())
                            ).continueWith(allianceResultTask -> {
                                Alliance alliance = allianceResultTask.isSuccessful() ? allianceResultTask.getResult() : null;
                                friendStatusMap.put(friend, alliance);
                                return null;
                            });
                            allianceStatusTasks.add(task);
                        } else {
                            // Prijatelj nije u savezu, dodajemo ga odmah sa NULL vrednošću
                            friendStatusMap.put(friend, null);
                        }
                    }

                    // Čekamo da se svi Task-ovi za Saveze završe
                    return Tasks.whenAllComplete(allianceStatusTasks)
                            .continueWith(task -> friendStatusMap); // Vraćamo finalnu mapu
                })
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && isAdded()) {
                        @SuppressWarnings("unchecked")
                        Map<User, Alliance> friendStatusMap = (Map<User, Alliance>) task.getResult();

                        friendAdapter.updateFriends(friendStatusMap);
                        tvNoFriends.setVisibility(friendStatusMap.isEmpty() ? View.VISIBLE : View.GONE);
                    } else if (isAdded()) {
                        Log.e("FriendsFragment", "Error loading accepted friends and alliances", task.getException());
                    }
                });

        allTasks.add(friendsWithAllianceStatusTask);

        // =========================================================
        // 3. Učitavanje zahteva za prijateljstvo
        // =========================================================
        Task<Map<String, Object>> requestsTask = Tasks.call(Executors.newSingleThreadExecutor(), this::loadPendingRequestsFromCache)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && isAdded()) {
                        Map<String, Object> result = task.getResult();
                        @SuppressWarnings("unchecked")
                        List<Friendship> requests = (List<Friendship>) result.get("requests");
                        @SuppressWarnings("unchecked")
                        Map<String, User> senders = (Map<String, User>) result.get("senders");

                        requestAdapter.updateRequests(requests, senders);
                        tvNoRequests.setVisibility(requests.isEmpty() ? View.VISIBLE : View.GONE);
                    } else if (isAdded()) {
                        Log.e("FriendsFragment", "Failed to load friend requests from cache.", task.getException());
                        Toast.makeText(getContext(), "Failed to load friend requests.", Toast.LENGTH_SHORT).show();
                        tvNoRequests.setVisibility(View.VISIBLE);
                    }
                });

        allTasks.add(requestsTask);

        // =========================================================
        // 4. Isključi progress bar kada se SVI Task-ovi završe
        // =========================================================
        Tasks.whenAllComplete(allTasks)
                .addOnCompleteListener(task -> {
                    if (isAdded()) {
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void openMyAllianceFragment(String allianceId) {
        if (allianceId == null || allianceId.isEmpty()) {
            Toast.makeText(requireContext(), "Greška: ID saveza nije dostupan.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Koristi novu factory metodu newInstance za kreiranje fragmenta
        AllianceFragment allianceFragment = AllianceFragment.newInstance(allianceId);

        // Otvori fragment
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, allianceFragment)
                .addToBackStack(null) // Omogućava povratak na FriendsFragment pomoću back dugmeta
                .commit();
    }

    @Override
    public void onAccept(Friendship friendship) {
        progressBar.setVisibility(View.VISIBLE);
        Tasks.call(Executors.newSingleThreadExecutor(), () -> {
                    friendship.setStatus(Friendship.STATUS_ACCEPTED);
                    friendshipRepository.saveAcceptedFriendshipToCache(friendship);
                    return null;
                })
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && isAdded()) {
                        Toast.makeText(getContext(), "Request accepted.", Toast.LENGTH_SHORT).show();
                        loadFriendsData();
                    } else if (isAdded()) {
                        Toast.makeText(getContext(), "Accept failed.", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    public void onReject(Friendship friendship) {
        progressBar.setVisibility(View.VISIBLE);
        Tasks.call(Executors.newSingleThreadExecutor(), () -> {
                    // Implementacija brisanja zahteva iz keša
                    return null;
                })
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && isAdded()) {
                        Toast.makeText(getContext(), "Request rejected.", Toast.LENGTH_SHORT).show();
                        loadFriendsData();
                    } else if (isAdded()) {
                        Toast.makeText(getContext(), "Reject failed.", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    public void onFriendOptionsClick(View anchorView, User friend) {
        showFriendOptionsMenu(anchorView, friend);
    }

    private void showFriendOptionsMenu(View anchorView, User friend) {
        PopupMenu popup = new PopupMenu(anchorView.getContext(), anchorView);
        popup.getMenuInflater().inflate(R.menu.friend_options_menu, popup.getMenu());

        MenuItem inviteToAllianceItem = popup.getMenu().findItem(R.id.action_invite_to_alliance);

        // Logika za omogućavanje/onemogućavanje opcije "Pozovi u Savez"
        if (currentUsersAlliance == null) {
            inviteToAllianceItem.setEnabled(false);
            inviteToAllianceItem.setTitle("Pozovi u Savez (Niste u savezu)");
        } else if(!Objects.equals(currentUsersAlliance.getLeaderId(), currentUserId)) {
            inviteToAllianceItem.setEnabled(false);
            inviteToAllianceItem.setTitle("Pozovi u Savez (Niste lider saveza)");
            Log.d("TAG", "showFriendOptionsMenu: " + currentUserId + ":::"+ currentUsersAlliance.getLeaderId());
        } else if (Objects.equals(friend.getAllianceId(), currentUsersAlliance.getId())) {
            // Ako je prijatelj već u nekom savezu
            inviteToAllianceItem.setEnabled(false);
            Log.d("TAG", "showFriendOptionsMenu2: " + friend.getAllianceId() + ":::"+ currentUsersAlliance.getId());
            inviteToAllianceItem.setTitle("Pozovi u Savez (Prijatelj je već u savezu)");
        }
        else {
            inviteToAllianceItem.setEnabled(true);
            inviteToAllianceItem.setTitle("Pozovi u Savez");
        }


        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_invite_to_alliance) {
                // Proveravamo ponovo da bismo bili sigurni
                if (currentUsersAlliance != null && (friend.getAllianceId() == null || friend.getAllianceId().isEmpty() || !Objects.equals(friend.getAllianceId(), currentUsersAlliance.getId()))) {
                    showInviteToAllianceConfirmationDialog(friend);
                } else if (currentUsersAlliance == null) {
                    Toast.makeText(requireContext(), "Morate biti u savezu da biste pozvali prijatelja.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), friend.getUsername() + " je već u savezu.", Toast.LENGTH_SHORT).show();
                }
                return true;
            } else if (itemId == R.id.action_view_profile) {
                Log.d("FriendsFragment", "Pogledaj profil " + friend.getUsername());
                openUserProfileFragment(friend.getId());
                return true;
            } else if (itemId == R.id.action_remove_friend) {
                Log.d("FriendsFragment", "Ukloni " + friend.getUsername());
                // Prikaži dijalog za potvrdu brisanja prijatelja
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void openUserProfileFragment(String userIdToDisplay) {
        UserProfileFragment profileFragment = new UserProfileFragment();

        Bundle args = new Bundle();
        args.putString("targetUserId", userIdToDisplay);
        profileFragment.setArguments(args);

        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, profileFragment)
                .addToBackStack(null)
                .commit();
    }

    private void showInviteToAllianceConfirmationDialog(User friend) {
        if (currentUsersAlliance == null) {
            Toast.makeText(requireContext(), "Morate biti član saveza da biste pozvali druge.", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Pozovi u Savez")
                .setMessage("Da li ste sigurni da želite da pozovete " + friend.getUsername() + " u savez " + currentUsersAlliance.getName() + "?")
                .setPositiveButton("Pozovi", (dialog, which) -> {
                    sendAllianceInvitation(friend);
                })
                .setNegativeButton("Odustani", null)
                .show();
    }

    private void sendAllianceInvitation(User friend) {
        if (currentUsersAlliance == null) {
            Toast.makeText(requireContext(), "Greška: Niste u savezu. Ne možete poslati pozivnicu.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        allianceRepository.sendInvitation(
                        currentUsersAlliance.getId(),
                        currentUsersAlliance.getName(),
                        currentUserId,
                        friend.getId()
                )
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful() && isAdded()) {

                        // =======================================================
                        // 💡 KLJUČNA IZMENA: Dohvatite ID pozivnice
                        String invitationId = (String) task.getResult();
                        // =======================================================

                        Toast.makeText(requireContext(), "Pozivnica poslata za " + friend.getUsername() + "!", Toast.LENGTH_SHORT).show();

                        // =======================================================
                        // POZIVANJE NOTIFIKACIJE ZA LOKALNO TESTIRANJE
                        // =======================================================

                        AllianceNotificationHelper notificationHelper =
                                new AllianceNotificationHelper(requireContext());

                        // 2. Simulacija prijema notifikacije na PRIJATELJEVOM uređaju
                        notificationHelper.showAllianceInvitation(
                                invitationId,
                                currentUsersAlliance.getId(),
                                currentUsersAlliance.getName(),
                                "Pošiljalac: new SharedPreferencesHelper(requireContext()).getLoggedInUsername()",
                                friend.getId()
                        );

                    } else if (isAdded()) {
                        // ... (greška)
                    }
                });
    }
}