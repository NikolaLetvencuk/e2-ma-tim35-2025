package com.example.dailyboss.presentation.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailyboss.R;
import com.example.dailyboss.data.dao.MissionActivityLogDao;
import com.example.dailyboss.data.dao.UserMissionProgressDao;
import com.example.dailyboss.domain.model.MissionActivityLog; // Import the MissionActivityLog model
import com.example.dailyboss.domain.model.UserMissionProgress;
import com.example.dailyboss.presentation.adapters.MissionActivityLogAdapter;
import com.example.dailyboss.presentation.adapters.UserMissionProgressAdapter;
import com.example.dailyboss.presentation.viewmodels.SpecialMissionViewModel;
import com.example.dailyboss.service.SpecialMissionService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class SpecialMissionFragment extends Fragment {

    private static final String ARG_MISSION_ID = "specialMissionId";

    private SpecialMissionViewModel viewModel;

    private TextView tvMissionTitle;
    private TextView tvBossName;
    private TextView tvBossHp;
    private ProgressBar pbBossHp;
    private TextView tvMissionTimeRemaining;
    private TextView tvMissionStatus;

    // Moj napredak
    private TextView tvMyShopBuys;
    private TextView tvMyBossHits;
    private TextView tvMyEasyNormalImportantTasks;
    private TextView tvMyOtherTasks;
    private TextView tvMyNoUnresolvedTasks;
    private TextView tvMyAllianceMessages;
    private TextView tvMyTotalDamage;

    private RecyclerView rvAllianceMissionProgress;
    private UserMissionProgressAdapter allianceProgressAdapter;

    private RecyclerView rvMissionActivityLogs;
    private MissionActivityLogAdapter activityLogAdapter;
    private UserMissionProgressDao userMissionProgressDao;
    private MissionActivityLogDao missionActivityLogDao;
    private SpecialMissionService specialMissionService;
    private String specialMissionId;

    public SpecialMissionFragment() {
        // Required empty public constructor
    }

    public static SpecialMissionFragment newInstance(String specialMissionId) {
        SpecialMissionFragment fragment = new SpecialMissionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MISSION_ID, specialMissionId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            specialMissionId = getArguments().getString(ARG_MISSION_ID);
        }
        viewModel = new ViewModelProvider(this).get(SpecialMissionViewModel.class);
        // Initialize DAOs here so they are ready for setupRecyclerViews
        userMissionProgressDao = new UserMissionProgressDao(requireContext());
        missionActivityLogDao = new MissionActivityLogDao(requireContext());
        specialMissionService = new SpecialMissionService(requireContext()); // Initialize the new service

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_special_mission, container, false);

        initViews(view);
        setupRecyclerViews(); // This will now fetch and populate immediately
        observeViewModel(); // Still observing ViewModel, so data will be updated again when ViewModel loads

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (specialMissionId != null) {
//            Log.d("SpecialMissionFragment", "Loading data for mission ID: " + specialMissionId);
//            // ViewModel calls will still trigger, potentially updating adapters again
//            // if initial data load here is slower than ViewModel's background work.
            viewModel.loadSpecialMissionDetails(specialMissionId);
//            viewModel.loadAllUserProgressForMission(specialMissionId);
            viewModel.loadCurrentUserProgressForMission(specialMissionId);
//            viewModel.loadMissionActivityLogs(specialMissionId);
        } else {
            Toast.makeText(requireContext(), "ID specijalne misije nije prosleđen.", Toast.LENGTH_SHORT).show();
            Log.e("SpecialMissionFragment", "Special Mission ID is NULL!");
            // Možda se vratiti na prethodni fragment
        }
    }

    private void initViews(View view) {
        tvMissionTitle = view.findViewById(R.id.tvMissionTitle);
        tvBossName = view.findViewById(R.id.tvBossName);
        tvBossHp = view.findViewById(R.id.tvBossHp);
        pbBossHp = view.findViewById(R.id.pbBossHp);
        tvMissionTimeRemaining = view.findViewById(R.id.tvMissionTimeRemaining);
        tvMissionStatus = view.findViewById(R.id.tvMissionStatus);

        tvMyBossHits = view.findViewById(R.id.tvMyBossHits);
        tvMyEasyNormalImportantTasks = view.findViewById(R.id.tvMyEasyNormalImportantTasks);
        tvMyNoUnresolvedTasks = view.findViewById(R.id.tvMyNoUnresolvedTasks);
        tvMyOtherTasks = view.findViewById(R.id.tvMyOtherTasks);
        tvMyShopBuys = view.findViewById(R.id.tvMyShopBuys);
        tvMyAllianceMessages = view.findViewById(R.id.tvMyAllianceMessages);
        tvMyTotalDamage = view.findViewById(R.id.tvMyTotalDamage);

        rvAllianceMissionProgress = view.findViewById(R.id.rvAllianceMissionProgress);
        rvMissionActivityLogs = view.findViewById(R.id.rvMissionActivityLogs);

        // DAOs are now initialized in onCreate
        // userMissionProgressDao = new UserMissionProgressDao(getContext());
        // missionActivityLogDao = new MissionActivityLogDao(getContext());
    }

    private void setupRecyclerViews() {
        // --- MODIFIED SECTION ---
        List<UserMissionProgress> userMissionProgresses = new ArrayList<>();
        List<MissionActivityLog> missionActivityLogs = new ArrayList<>();

        // Perform DAO calls on a background thread.
        // For simplicity, I'm using a direct call here which is usually not ideal
        // in a ViewModel-based architecture for initial setup.
        // In a real app, these would ideally be observed from LiveData in the ViewModel,
        // or handled with async tasks if you truly want to bypass ViewModel for data loading.
        // However, to directly answer your request:

        if (specialMissionId != null) {
            // This is a blocking call, but for initial setup to populate an adapter,
            // it's sometimes tolerated if the data set is guaranteed to be small and fast.
            // For larger datasets, you'd want to wrap this in an AsyncTask or a Coroutine/Executor.
            userMissionProgresses = userMissionProgressDao.getAllUserProgressForMission(specialMissionId);
            missionActivityLogs = missionActivityLogDao.getLogsForSpecialMission(specialMissionId); // Assuming this method exists and returns a List
        }


        allianceProgressAdapter = new UserMissionProgressAdapter(requireContext(), userMissionProgresses);
        rvAllianceMissionProgress.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvAllianceMissionProgress.setAdapter(allianceProgressAdapter);

        activityLogAdapter = new MissionActivityLogAdapter(requireContext(), missionActivityLogs);
        rvMissionActivityLogs.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvMissionActivityLogs.setAdapter(activityLogAdapter);
        // --- END MODIFIED SECTION ---
    }

    private void observeViewModel() {
        viewModel.specialMission.observe(getViewLifecycleOwner(), specialMission -> {
            if (specialMission != null) {
                tvMissionTitle.setText(specialMission.isCompletedSuccessfully() ? "Specijalna Misija - Završena!" : "Specijalna Misija Saveza");
                tvBossHp.setText(String.format(Locale.getDefault(), "HP: %d/%d", specialMission.getCurrentBossHp(), specialMission.getTotalBossHp()));
                pbBossHp.setMax((int) specialMission.getTotalBossHp());
                pbBossHp.setProgress((int) specialMission.getCurrentBossHp());

                // Izračunavanje preostalog vremena
                long currentTime = new Date().getTime();
                long endTime = specialMission.getEndTime().getTime();
                long diffMillis = endTime - currentTime;
                Log.d("TAG", "observeViewModel: " + specialMissionId);
                specialMissionService.checkAndAwardMissionCompletion(specialMissionId);

                if (diffMillis <= 0) {
                    tvMissionTimeRemaining.setText("Vreme isteklo!");
                    tvMissionStatus.setText("Status: " + (specialMission.isCompletedSuccessfully() ? "Uspešno završena" : "Neuspešna"));
                    if (specialMission.isCompletedSuccessfully() && !specialMission.isRewardsAwarded()) { // Pretpostavka da postoji getter
                        viewModel.checkAndAwardMissionCompletion(specialMissionId);
                        specialMissionService.checkAndAwardMissionCompletion(specialMissionId);
                    }
                } else {
                    long diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis);
                    long diffHours = TimeUnit.MILLISECONDS.toHours(diffMillis) % 24;
                    long diffMinutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis) % 60;
                    tvMissionTimeRemaining.setText(String.format(Locale.getDefault(), "Preostalo vreme: %d dana, %d sati, %d minuta", diffDays, diffHours, diffMinutes));
                    tvMissionStatus.setText("Status: Aktivna");
                }
            }
        });

        viewModel.allUserProgress.observe(getViewLifecycleOwner(), progressList -> {
            if (progressList != null) {
                Log.d("SpecialMissionFragment", "Observer received " + progressList.size() + " user progress items.");
                allianceProgressAdapter.updateProgress(progressList);
            } else {
                Log.d("SpecialMissionFragment", "Observer received NULL progressList.");
            }
        });

        viewModel.currentUserProgress.observe(getViewLifecycleOwner(), userProgress -> {
            if (userProgress != null) {
                tvMyShopBuys.setText(String.format("Kupovina u prodavnici: %d/5 (-%d HP)", userProgress.getBuyInShopCount(), userProgress.getBuyInShopCount() * 2));
                tvMyBossHits.setText(String.format("Uspešan udarac u borbi: %d/10 (-%d HP)", userProgress.getRegularBossHitCount(), userProgress.getRegularBossHitCount() * 2));
                tvMyEasyNormalImportantTasks.setText(String.format("Rešeni laki/normalni/važni zadaci: %d/10 (-%d HP)", userProgress.getEasyNormalImportantTaskCount(), userProgress.getEasyNormalImportantTaskCount()));
                tvMyOtherTasks.setText(String.format("Rešeni ostali zadaci: %d/6 (-%d HP)", userProgress.getOtherTasksCount(), userProgress.getOtherTasksCount() * 4));
                tvMyNoUnresolvedTasks.setText(String.format("Bez nerešenih zadataka: %s (-%d HP)", userProgress.isNoUnresolvedTasksCompleted() ? "Da" : "Ne", userProgress.isNoUnresolvedTasksCompleted() ? 10 : 0));

                // Za poruke, ako želite da prikažete "X/14 dana", morali biste da pratite dane u UserMissionProgress
                // Trenutno prati samo poslednji dan kada je poruka poslata
                String messageStatus = (userProgress.getLastMessageSentDate() != null) ?
                        ("Poslednja poruka: " + new SimpleDateFormat("dd.MM.", Locale.getDefault()).format(userProgress.getLastMessageSentDate())) : "Nijedna";
                tvMyAllianceMessages.setText(String.format("Poslata poruka u savezu (dnevno): %s (-%d HP)", messageStatus, (userProgress.getLastMessageSentDate() != null) ? 4 : 0)); // Ova 4 HP nisu skroz tačna jer se dodaju samo jednom dnevno, a ne za svaki put kada se izračuna
                tvMyTotalDamage.setText(String.format("Ukupna šteta: %d HP", userProgress.calculateTotalDamageDealt()));
            }
        });

        viewModel.missionActivityLogs.observe(getViewLifecycleOwner(), activityLogs -> {
            if (activityLogs != null) {
                Log.d("SpecialMissionFragment", "Observer received " + activityLogs.size() + " activity logs.");
                activityLogAdapter.updateLogs(activityLogs);
            } else {
                Log.d("SpecialMissionFragment", "Observer received NULL activityLogs.");
            }
        });

        viewModel.errorMessage.observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }
}