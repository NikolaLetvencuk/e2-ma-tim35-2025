package com.example.dailyboss.presentation.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailyboss.R;
import com.example.dailyboss.data.dao.AllianceDao;
import com.example.dailyboss.data.dao.SpecialMissionDao;
import com.example.dailyboss.data.dao.UserMissionProgressDao;
import com.example.dailyboss.domain.model.UserMissionProgress;
import com.example.dailyboss.presentation.adapters.AllianceMembersAdapter;
import com.example.dailyboss.data.repository.AllianceRepository;
import com.example.dailyboss.data.repository.SpecialMissionRepository;
import com.example.dailyboss.data.repository.UserRepository;
import com.example.dailyboss.domain.model.Alliance;
import com.example.dailyboss.domain.model.SpecialMission;
import com.example.dailyboss.domain.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;

public class AllianceFragment extends Fragment {

    private TextView tvAllianceName;
    private TextView tvAllianceLeader;
    private TextView tvCountMembers;
    private Button btnLeaveAlliance;
    private Button btnDisbandAlliance;
    private Button btnAllianceAction;
    private Button btnMainSpecialMissionAction;
    private RecyclerView rvAllianceMembers;

    private AllianceRepository allianceRepository;
    private AllianceDao allianceDao;
    private UserRepository userRepository;
    private SpecialMissionRepository specialMissionRepository;
    private UserMissionProgressDao userMissionProgressDao;
    private SpecialMissionDao specialMissionDao; // I dalje nam treba za lokalni dohvat ako budeš koristio
    private AllianceMembersAdapter adapter;
    private List<User> allianceMembers = new ArrayList<>();

    private String currentUserId;
    private Alliance currentAlliance;
    private SpecialMission activeSpecialMission;

    private static final String TAG = "AllianceFragment";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        allianceRepository = new AllianceRepository(requireContext());
        userRepository = new UserRepository(requireContext());
        specialMissionRepository = new SpecialMissionRepository(requireContext());
        allianceDao = new AllianceDao(requireContext());
        specialMissionDao = new SpecialMissionDao(getContext());
        userMissionProgressDao = new UserMissionProgressDao(getContext());

        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (currentUserId == null) {
            Toast.makeText(requireContext(), "Korisnik nije prijavljen.", Toast.LENGTH_SHORT).show();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alliance, container, false);

        tvAllianceName = view.findViewById(R.id.tvAllianceName);
        tvAllianceLeader = view.findViewById(R.id.tvAllianceLeader);
        btnLeaveAlliance = view.findViewById(R.id.btnLeaveAlliance);
        btnDisbandAlliance = view.findViewById(R.id.btnDisbandAlliance);
        btnAllianceAction = view.findViewById(R.id.btnAllianceAction);
        btnMainSpecialMissionAction = view.findViewById(R.id.btnMainSpecialMissionAction);
        rvAllianceMembers = view.findViewById(R.id.rvAllianceMembers);
        tvCountMembers = view.findViewById(R.id.tvCountMembers);

        rvAllianceMembers.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new AllianceMembersAdapter(getContext(), allianceMembers, "");
        rvAllianceMembers.setAdapter(adapter);

        // OVE LINIJE SU UKLONJENE/KOMENTARISANE JER SU UZROKOVALE NullPointerException
        // if (currentAlliance.isMissionActive()) {
        //     Log.d(TAG, "SpecialMission: " + currentAlliance);
        //     activeSpecialMission = specialMissionDao.getActiveSpecialMissionForAlliance(currentAlliance.getActiveSpecialMissionId());
        //     Log.d(TAG, "SpecialMission: " + currentAlliance);
        // }

        setupListeners();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadAllianceData(); // Poziv loadAllianceData() inicijalizuje currentAlliance
    }

    public static AllianceFragment newInstance(String allianceId) {
        AllianceFragment fragment = new AllianceFragment();
        Bundle args = new Bundle();
        args.putString("allianceId", allianceId);
        fragment.setArguments(args);
        return fragment;
    }

    private void setupListeners() {
        btnLeaveAlliance.setOnClickListener(v -> {
            if (currentAlliance != null && currentUserId != null) {
                if (activeSpecialMission != null && activeSpecialMission.isActive() && !activeSpecialMission.isCompletedSuccessfully()) {
                    Toast.makeText(requireContext(), "Ne možete napustiti savez dok je specijalna misija aktivna!", Toast.LENGTH_LONG).show();
                    return;
                }
                new android.app.AlertDialog.Builder(requireContext())
                        .setTitle("Potvrda napuštanja saveza")
                        .setMessage("Da li ste sigurni da želite da napustite savez '" + currentAlliance.getName() + "'?")
                        .setPositiveButton("Napusti", (dialog, which) -> {
                            allianceRepository.leaveAlliance(currentAlliance.getId(), currentUserId)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(requireContext(), "Savez uspešno napušten!", Toast.LENGTH_SHORT).show();
                                            currentAlliance = null;
                                            activeSpecialMission = null;
                                            displayNoAllianceState();
                                        } else {
                                            Log.e(TAG, "Greška pri napuštanju saveza: " + task.getException().getMessage());
                                            Toast.makeText(requireContext(), "Greška: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                        })
                        .setNegativeButton("Otkaži", (dialog, which) -> dialog.cancel())
                        .show();
            }
        });

        btnDisbandAlliance.setOnClickListener(v -> {
            if (currentAlliance != null && currentUserId != null && currentUserId.equals(currentAlliance.getLeaderId())) {
                if (activeSpecialMission != null && activeSpecialMission.isActive() && !activeSpecialMission.isCompletedSuccessfully()) {
                    Toast.makeText(requireContext(), "Ne možete ukinuti savez dok je specijalna misija aktivna!", Toast.LENGTH_LONG).show();
                    return;
                }

                new android.app.AlertDialog.Builder(requireContext())
                        .setTitle("Potvrda ukidanja saveza")
                        .setMessage("Da li ste sigurni da želite da ukinete savez '" + currentAlliance.getName() + "'? Svi članovi će biti uklonjeni.")
                        .setPositiveButton("Ukini", (dialog, which) -> {
                            allianceRepository.disbandAlliance(currentAlliance.getId(), currentUserId)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(requireContext(), "Savez uspešno ukinut!", Toast.LENGTH_SHORT).show();
                                            currentAlliance = null;
                                            activeSpecialMission = null;
                                            displayNoAllianceState();
                                        } else {
                                            Log.e(TAG, "Greška pri ukidanju saveza: " + task.getException().getMessage());
                                            Toast.makeText(requireContext(), "Greška: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                        })
                        .setNegativeButton("Otkaži", (dialog, which) -> dialog.cancel())
                        .show();
            } else {
                Toast.makeText(requireContext(), "Niste vođa saveza ili nema saveza za ukidanje.", Toast.LENGTH_SHORT).show();
            }
        });

        btnAllianceAction.setOnClickListener(v -> {
            if (currentUserId == null) {
                Toast.makeText(requireContext(), "Korisnik nije prijavljen.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (currentAlliance == null) {
                showCreateAllianceDialog();
            } else {
                openAllianceChat();
            }
        });

        btnMainSpecialMissionAction.setOnClickListener(v -> {
            if (currentAlliance == null) {
                Toast.makeText(requireContext(), "Prvo morate biti član saveza.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (activeSpecialMission != null && activeSpecialMission.isActive() && !activeSpecialMission.isCompletedSuccessfully()) {
                openSpecialMissionDetails(activeSpecialMission.getId());
            } else {
                if (currentUserId != null && currentUserId.equals(currentAlliance.getLeaderId())) {
                    new android.app.AlertDialog.Builder(requireContext())
                            .setTitle("Pokreni Specijalnu Misiju")
                            .setMessage("Da li ste sigurni da želite da pokrenete specijalnu misiju? Misija traje 2 nedelje i ne može se prekinuti.")
                            .setPositiveButton("Pokreni", (dialog, which) -> startSpecialMission())
                            .setNegativeButton("Otkaži", (dialog, which) -> dialog.cancel())
                            .show();
                } else {
                    Toast.makeText(requireContext(), "Nema aktivne specijalne misije u savezu.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadAllianceData() {
        if (currentUserId == null) {
            displayNoAllianceState();
            return;
        }

        Tasks.call(Executors.newSingleThreadExecutor(), () -> {
            User currentUser = userRepository.getLocalUser(currentUserId);
            return currentUser != null ? currentUser.getAllianceId() : null;
        }).addOnCompleteListener(allianceIdTask -> {
            if (allianceIdTask.isSuccessful() && allianceIdTask.getResult() != null) {
                String allianceId = allianceIdTask.getResult();
                allianceRepository.getAlliance(allianceId)
                        .addOnCompleteListener(allianceFetchTask -> {
                            if (allianceFetchTask.isSuccessful() && allianceFetchTask.getResult() != null) {
                                currentAlliance = allianceFetchTask.getResult();
                                displayAllianceDetails(currentAlliance);
                            } else {
                                Log.d(TAG, "Nije pronađen savez za ID: " + allianceId + " ili greška: " + (allianceFetchTask.getException() != null ? allianceFetchTask.getException().getMessage() : ""));
                                currentAlliance = null;
                                displayNoAllianceState();
                            }
                            loadActiveSpecialMission(); // Pozivamo loadActiveSpecialMission tek nakon što je currentAlliance postavljen
                        });
            } else {
                Log.d(TAG, "Korisnik " + currentUserId + " nije član nijednog saveza ili greška: " + (allianceIdTask.getException() != null ? allianceIdTask.getException().getMessage() : ""));
                currentAlliance = null;
                displayNoAllianceState();
                loadActiveSpecialMission(); // Ažuriraj vidljivost dugmadi i kada nema saveza (ovo će resetovati stanje misije)
            }
        });
    }

    private void loadActiveSpecialMission() {
        if (currentAlliance == null) {
            activeSpecialMission = null;
            updateMissionButtonState();
            return;
        }

        if (currentAlliance.getActiveSpecialMissionId() == null || currentAlliance.getActiveSpecialMissionId().isEmpty()) {
            activeSpecialMission = null;
            updateMissionButtonState();
            return;
        }

        specialMissionRepository.getSpecialMissionById(currentAlliance.getActiveSpecialMissionId())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        activeSpecialMission = task.getResult();
                        if (activeSpecialMission.getEndTime().getTime() < System.currentTimeMillis() && !activeSpecialMission.isCompletedSuccessfully()) {
                            Log.d(TAG, "Aktivna misija je istekla i nije uspešno završena. Postavljam je na null.");
                            activeSpecialMission = null;
                            updateAllianceActiveMissionId(currentAlliance.getId(), null);
                            if (currentAlliance.isMissionActive()) {
                                currentAlliance.setMissionActive(false);
                                allianceRepository.updateAlliance(currentAlliance)
                                        .addOnFailureListener(e -> Log.e(TAG, "Failed to update alliance missionActive status after mission expired: " + e.getMessage()));
                            }
                        }
                    } else {
                        Log.e(TAG, "Greška pri dohvatanju aktivne specijalne misije po ID-u iz saveza: " + task.getException());
                        activeSpecialMission = null;
                        updateAllianceActiveMissionId(currentAlliance.getId(), null);
                        if (currentAlliance.isMissionActive()) {
                            currentAlliance.setMissionActive(false);
                            allianceRepository.updateAlliance(currentAlliance)
                                    .addOnFailureListener(e -> Log.e(TAG, "Failed to update alliance missionActive status after mission not found: " + e.getMessage()));
                        }
                    }
                    updateMissionButtonState();
                });
    }

    private void updateAllianceActiveMissionId(String allianceId, String missionId) {
        allianceRepository.updateAllianceActiveSpecialMission(allianceId, missionId)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Uspešno ažuriran activeSpecialMissionId za savez: " + (missionId == null ? "NULL" : missionId));
                        if (currentAlliance != null && currentAlliance.getId().equals(allianceId)) {
                            currentAlliance.setActiveSpecialMissionId(missionId);
                            allianceRepository.updateAlliance(currentAlliance);
                        }
                    } else {
                        Log.e(TAG, "Greška pri ažuriranju activeSpecialMissionId za savez: " + task.getException());
                    }
                });
    }


    private void updateMissionButtonState() {
        Log.d(TAG, "updateMissionButtonState: " + currentAlliance.getActiveSpecialMissionId() + "EE" + activeSpecialMission);
        if (currentAlliance != null) {
            btnMainSpecialMissionAction.setVisibility(View.VISIBLE);
            Log.d(TAG, "updateMissionButtonState:22 " + currentAlliance + "EE" + activeSpecialMission);

            if (activeSpecialMission != null && activeSpecialMission.isActive() && !activeSpecialMission.isCompletedSuccessfully()) {
                btnMainSpecialMissionAction.setText("Pregled specijalne misije");
                btnMainSpecialMissionAction.setBackgroundTintList(getResources().getColorStateList(R.color.button_primary));
            } else {
                if (currentUserId != null && currentUserId.equals(currentAlliance.getLeaderId())) {
                    btnMainSpecialMissionAction.setText("Pokreni Specijalnu Misiju");
                    btnMainSpecialMissionAction.setBackgroundTintList(getResources().getColorStateList(R.color.purple_500));
                } else {
                    btnMainSpecialMissionAction.setVisibility(View.GONE);
                }
            }

            if (currentUserId != null && currentUserId.equals(currentAlliance.getLeaderId())) {
                btnDisbandAlliance.setVisibility(View.VISIBLE);
                btnLeaveAlliance.setVisibility(View.GONE);
            } else {
                btnDisbandAlliance.setVisibility(View.GONE);
                btnLeaveAlliance.setVisibility(View.VISIBLE);
            }

        } else {
            btnMainSpecialMissionAction.setVisibility(View.GONE);
            btnDisbandAlliance.setVisibility(View.GONE);
            btnLeaveAlliance.setVisibility(View.GONE);
        }
    }


    private void displayAllianceDetails(Alliance alliance) {
        tvAllianceName.setText("🛡️ " + alliance.getName());
        tvAllianceName.setVisibility(View.VISIBLE);

        Tasks.call(Executors.newSingleThreadExecutor(), () -> {
            return userRepository.getLocalUser(alliance.getLeaderId());
        }).addOnCompleteListener(leaderTask -> {
            if (leaderTask.isSuccessful() && leaderTask.getResult() != null) {
                tvAllianceLeader.setText(leaderTask.getResult().getUsername());
            } else {
                tvAllianceLeader.setText("Nepoznat vođa");
                Log.e(TAG, "Nije pronađen vođa: " + (leaderTask.getException() != null ? leaderTask.getException().getMessage() : ""));
            }
            tvAllianceLeader.setVisibility(View.VISIBLE);

            adapter.setAllianceLeaderId(alliance.getLeaderId());
            loadAllianceMembers(alliance.getId());
        });

        btnAllianceAction.setText("Otvori Ćaskanje");
        updateMissionButtonState();
    }

    private void loadAllianceMembers(String allianceId) {
        Tasks.call(Executors.newSingleThreadExecutor(), () -> {
            return userRepository.getLocalUsersByAllianceId(allianceId);
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<User> membersFromTask = task.getResult();

                Log.d(TAG, "TASK RESULT SIZE: " + membersFromTask.size());

                allianceMembers.clear();
                allianceMembers.addAll(membersFromTask);
                tvCountMembers.setText("Članovi saveza: " + membersFromTask.size());

                Log.d(TAG, "ALLIANCE MEMBERS AFTER ADD: " + allianceMembers.size());

                adapter.updateMembers(membersFromTask);

                Log.d(TAG, "ADAPTER ITEM COUNT: " + adapter.getItemCount());

            } else {
                Log.e(TAG, "Greška pri dohvatanju članova saveza: " + (task.getException() != null ? task.getException().getMessage() : ""));
                Toast.makeText(requireContext(), "Greška pri učitavanju članova.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void LoadAllianceMembersSpecialMission(String allianceId, String specialMissionId) {
        Tasks.call(Executors.newSingleThreadExecutor(), () -> {
            return userRepository.getLocalUsersByAllianceId(allianceId);
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<User> membersFromTask = task.getResult();

                for (User user : membersFromTask) {
                    String progressId = UUID.randomUUID().toString();
                    UserMissionProgress progress = new UserMissionProgress(progressId, specialMissionId, user.getId(), user.getUsername());
                    userMissionProgressDao.insert(progress);
                }

            } else {
                Log.e(TAG, "Greška pri dohvatanju članova saveza: " + (task.getException() != null ? task.getException().getMessage() : ""));
                Toast.makeText(requireContext(), "Greška pri učitavanju članova.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayNoAllianceState() {
        tvAllianceName.setText("Nisi član nijednog saveza.");
        tvAllianceName.setVisibility(View.VISIBLE);
        tvAllianceLeader.setVisibility(View.GONE);
        btnLeaveAlliance.setVisibility(View.GONE);
        btnDisbandAlliance.setVisibility(View.GONE);
        btnAllianceAction.setText("Kreiraj Novi Savez");
        allianceMembers.clear();
        adapter.updateMembers(allianceMembers);
        tvCountMembers.setText("Članovi saveza: 0");
        updateMissionButtonState();
    }

    private void showCreateAllianceDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Kreiraj Novi Savez");

        final View input = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_create_alliance, null);
        final EditText etAllianceName = input.findViewById(R.id.etAllianceName);

        builder.setView(input);

        builder.setPositiveButton("Kreiraj", (dialog, which) -> {
            String allianceName = etAllianceName.getText().toString().trim();
            if (!allianceName.isEmpty() && currentUserId != null) {
                allianceRepository.createAlliance(allianceName, currentUserId)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(requireContext(), "Savez '" + allianceName + "' uspešno kreiran!", Toast.LENGTH_SHORT).show();
                                loadAllianceData();
                            } else {
                                Log.e(TAG, "Greška pri kreiranju saveza: " + task.getException().getMessage());
                                Toast.makeText(requireContext(), "Greška pri kreiranju saveza: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            } else {
                Toast.makeText(requireContext(), "Ime saveza ne može biti prazno.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Otkaži", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void openAllianceChat() {
        if (currentAlliance != null) {
            Toast.makeText(requireContext(), "Otvori čet za savez: " + currentAlliance.getName(), Toast.LENGTH_SHORT).show();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, AllianceChatFragment.newInstance(currentAlliance.getId()))
                    .addToBackStack(null)
                    .commit();
        } else {
            Toast.makeText(requireContext(), "Niste član nijednog saveza da biste otvorili čet.", Toast.LENGTH_SHORT).show();
        }
    }

    private void startSpecialMission() {
        if (currentAlliance == null || currentUserId == null || !currentUserId.equals(currentAlliance.getLeaderId())) {
            Toast.makeText(requireContext(), "Nemate dozvolu za pokretanje specijalne misije.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (allianceMembers.isEmpty()) {
            Toast.makeText(requireContext(), "Savez mora imati bar jednog člana (vođu) za pokretanje misije.", Toast.LENGTH_SHORT).show();
            return;
        }

        specialMissionRepository.startSpecialMission(currentAlliance.getId(), allianceMembers.size(), allianceMembers)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String newMissionId = task.getResult().getId();
                        updateAllianceActiveMissionId(currentAlliance.getId(), newMissionId);

                        Log.d(TAG, "startSpecialMissionID: " + newMissionId);
                        currentAlliance.setStatus("Active");
                        currentAlliance.setMissionActive(true);
                        currentAlliance.setActiveSpecialMissionId(newMissionId);
                        allianceRepository.updateAlliance(currentAlliance)
                                .addOnCompleteListener(updateAllianceTask -> {
                                    if (updateAllianceTask.isSuccessful()) {
                                        Toast.makeText(requireContext(), "Specijalna misija uspešno pokrenuta!", Toast.LENGTH_SHORT).show();
                                        loadActiveSpecialMission();
                                        openSpecialMissionDetails(newMissionId);
                                    } else {
                                        Log.e(TAG, "Greška pri ažuriranju statusa saveza nakon pokretanja misije: " + updateAllianceTask.getException());
                                        Toast.makeText(requireContext(), "Greška pri ažuriranju statusa saveza.", Toast.LENGTH_LONG).show();
                                    }
                                });
                        LoadAllianceMembersSpecialMission(currentAlliance.getId(), currentAlliance.getActiveSpecialMissionId());
                    } else {
                        Log.e(TAG, "Greška pri pokretanju specijalne misije: " + task.getException());
                        Toast.makeText(requireContext(), "Greška pri pokretanju misije: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void openSpecialMissionDetails(String specialMissionId) {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, SpecialMissionFragment.newInstance(specialMissionId))
                .addToBackStack(null)
                .commit();
    }
}