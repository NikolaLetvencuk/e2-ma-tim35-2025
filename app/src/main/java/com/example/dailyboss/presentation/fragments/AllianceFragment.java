package com.example.dailyboss.presentation.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText; // Dodato za dijalog
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailyboss.R;
import com.example.dailyboss.presentation.adapters.AllianceMembersAdapter;
import com.example.dailyboss.data.repository.AllianceRepository;
import com.example.dailyboss.data.repository.UserRepository;
import com.example.dailyboss.domain.model.Alliance;
import com.example.dailyboss.domain.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class AllianceFragment extends Fragment {

    private TextView tvAllianceName;
    private TextView tvAllianceLeader;
    private TextView tvCountMembers;
    private Button btnLeaveAlliance;
    private Button btnDisbandAlliance;
    private Button btnAllianceAction; // Sada služi za otvaranje četa ili kreiranje saveza
    private RecyclerView rvAllianceMembers;

    private AllianceRepository allianceRepository;
    private UserRepository userRepository;
    private AllianceMembersAdapter adapter;
    private List<User> allianceMembers = new ArrayList<>();

    private String currentUserId;
    private Alliance currentAlliance;

    private static final String TAG = "AllianceFragment";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        allianceRepository = new AllianceRepository(requireContext());
        userRepository = new UserRepository(requireContext());

        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (currentUserId == null) {
            Toast.makeText(requireContext(), "Korisnik nije prijavljen.", Toast.LENGTH_SHORT).show();
            // Razmislite o preusmeravanju korisnika na login ili prikazivanju odgovarajućeg UI-a
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
        rvAllianceMembers = view.findViewById(R.id.rvAllianceMembers);
        tvCountMembers = view.findViewById(R.id.tvCountMembers);

        rvAllianceMembers.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new AllianceMembersAdapter(getContext(), allianceMembers, ""); // Prazan string za leaderId, biće ažuriran kasnije
        rvAllianceMembers.setAdapter(adapter);

        setupListeners();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadAllianceData();
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
            // TODO: Implementiraj logiku za napuštanje saveza kako je opisano u sekciji 7.1
            // Važno: Proveriti da li je misija pokrenuta pre napuštanja
            if (currentAlliance != null && currentUserId != null) {
                // Primer poziva (morate implementirati metodu leaveAlliance u AllianceRepository)
                // allianceRepository.leaveAlliance(currentAlliance.getId(), currentUserId)
                //     .addOnCompleteListener(task -> {
                //         if (task.isSuccessful()) {
                //             Toast.makeText(requireContext(), "Uspešno ste napustili savez.", Toast.LENGTH_SHORT).show();
                //             loadAllianceData(); // Ponovo učitaj podatke da prikaže "nema saveza" stanje
                //         } else {
                //             Toast.makeText(requireContext(), "Greška pri napuštanju saveza: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                //         }
                //     });
                Toast.makeText(requireContext(), "Funkcionalnost napuštanja saveza u razvoju.", Toast.LENGTH_SHORT).show();
            }
        });

        btnDisbandAlliance.setOnClickListener(v -> {
            // TODO: Proveriti da li misija nije pokrenuta pre ukidanja saveza, kao što je opisano u sekciji 7.1
            if (currentAlliance != null && currentUserId != null && currentUserId.equals(currentAlliance.getLeaderId())) {
                new android.app.AlertDialog.Builder(requireContext())
                        .setTitle("Potvrda ukidanja saveza")
                        .setMessage("Da li ste sigurni da želite da ukinete savez '" + currentAlliance.getName() + "'? Svi članovi će biti uklonjeni.")
                        .setPositiveButton("Ukini", (dialog, which) -> {
                            allianceRepository.disbandAlliance(currentAlliance.getId(), currentUserId)
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(requireContext(), "Savez uspešno ukinut!", Toast.LENGTH_SHORT).show();
                                            loadAllianceData(); // Ponovo učitaj podatke da prikaže "nema saveza" stanje
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
                openAllianceChat(); // Sada uvek otvara čet ako je korisnik u savezu
            }
        });
    }

    private void loadAllianceData() {
        if (currentUserId == null) {
            displayNoAllianceState();
            return;
        }

        // Koristimo Task.call za pozivanje sinhronih operacija na pozadinskoj niti
        Tasks.call(Executors.newSingleThreadExecutor(), () -> {
            return allianceRepository.getCurrentAlliance(currentUserId);
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                currentAlliance = task.getResult();
                displayAllianceDetails(currentAlliance);
            } else {
                Log.d(TAG, "Korisnik " + currentUserId + " nije član nijednog saveza ili greška: " + (task.getException() != null ? task.getException().getMessage() : ""));
                currentAlliance = null;
                displayNoAllianceState();
            }
        });
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

            // Ažuriraj adapter sa ispravnim leaderId-jem pre učitavanja članova
            adapter.setAllianceLeaderId(alliance.getLeaderId());
            loadAllianceMembers(alliance.getId());
        });

        if (currentUserId.equals(alliance.getLeaderId())) {
            btnLeaveAlliance.setVisibility(View.GONE);
            btnDisbandAlliance.setVisibility(View.VISIBLE);
            btnAllianceAction.setText("Otvori Ćaskanje"); // I vođa otvara ćaskanje
        } else {
            btnLeaveAlliance.setVisibility(View.VISIBLE);
            btnDisbandAlliance.setVisibility(View.GONE);
            btnAllianceAction.setText("Otvori Ćaskanje");
        }
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

    private void displayNoAllianceState() {
        tvAllianceName.setText("Nisi član nijednog saveza.");
        tvAllianceName.setVisibility(View.VISIBLE);
        tvAllianceLeader.setVisibility(View.GONE);
        btnLeaveAlliance.setVisibility(View.GONE);
        btnDisbandAlliance.setVisibility(View.GONE);
        btnAllianceAction.setText("Kreiraj Novi Savez");
        allianceMembers.clear();
        adapter.updateMembers(allianceMembers);
        tvCountMembers.setText("Članovi saveza: 0"); // Ažuriraj prikaz broja članova
    }

    private void showCreateAllianceDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Kreiraj Novi Savez");

        // Inflate custom layout for the dialog
        final View input = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_create_alliance, null);
        final EditText etAllianceName = input.findViewById(R.id.etAllianceName); // Koristimo EditText

        builder.setView(input);

        builder.setPositiveButton("Kreiraj", (dialog, which) -> {
            String allianceName = etAllianceName.getText().toString().trim();
            if (!allianceName.isEmpty() && currentUserId != null) {
                allianceRepository.createAlliance(allianceName, currentUserId)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(requireContext(), "Savez '" + allianceName + "' uspešno kreiran!", Toast.LENGTH_SHORT).show();
                                loadAllianceData(); // Ponovo učitaj podatke da prikaže novokreirani savez
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
}