package com.example.dailyboss.presentation.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailyboss.R;
import com.example.dailyboss.data.dao.MissionActivityLogDao;
import com.example.dailyboss.data.dao.UserMissionProgressDao;
import com.example.dailyboss.data.repository.AllianceChatRepository;
import com.example.dailyboss.data.repository.AllianceRepository;
import com.example.dailyboss.data.repository.SpecialMissionRepository;
import com.example.dailyboss.data.repository.UserRepository;
import com.example.dailyboss.domain.model.Alliance;
import com.example.dailyboss.domain.model.AllianceMessage;
import com.example.dailyboss.domain.model.MissionActivityLog;
import com.example.dailyboss.domain.model.User;
import com.example.dailyboss.domain.model.UserMissionProgress;
import com.example.dailyboss.presentation.adapters.AllianceChatAdapter;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;

public class AllianceChatFragment extends Fragment {

    private static final String ARG_ALLIANCE_ID = "allianceId";

    private String allianceId;
    private String currentUserId;

    private Toolbar toolbarChat;
    private RecyclerView rvChatMessages;
    private EditText etMessageInput;
    private ImageButton btnSendMessage;

    private AllianceChatAdapter chatAdapter;
    private AllianceChatRepository chatRepository;
    private AllianceRepository allianceRepository;
    private SpecialMissionRepository specialMissionRepository;
    private UserMissionProgressDao progressDao;
    private MissionActivityLogDao activityLogDao;
    private UserRepository userRepository;

    private ListenerRegistration chatListenerRegistration;

    private static final String TAG = "AllianceChatFragment";

    public static AllianceChatFragment newInstance(String allianceId) {
        AllianceChatFragment fragment = new AllianceChatFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ALLIANCE_ID, allianceId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            allianceId = getArguments().getString(ARG_ALLIANCE_ID);
        }

        currentUserId = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (allianceId == null || currentUserId == null) {
            Toast.makeText(requireContext(), "Greška: Nedostaju ID saveza ili ID korisnika.", Toast.LENGTH_LONG).show();
            getParentFragmentManager().popBackStack();
        }

        chatRepository = new AllianceChatRepository(requireContext());
        allianceRepository = new AllianceRepository(requireContext());
        progressDao = new UserMissionProgressDao(getContext());
        activityLogDao = new MissionActivityLogDao(getContext());
        allianceRepository = new AllianceRepository(getContext());
        specialMissionRepository = new SpecialMissionRepository(getContext());
        userRepository = new UserRepository(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alliance_chat, container, false);

        toolbarChat = view.findViewById(R.id.toolbarChat);
        rvChatMessages = view.findViewById(R.id.rvChatMessages);
        etMessageInput = view.findViewById(R.id.etMessageInput);
        btnSendMessage = view.findViewById(R.id.btnSendMessage);

        setupToolbar();
        setupRecyclerView();
        setupMessageInput();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadAllianceName();
        loadLocalMessages();
        startListeningForNewMessages();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (chatListenerRegistration != null) {
            chatListenerRegistration.remove();
            Log.d(TAG, "Chat listener removed.");
        }
    }

    private void setupToolbar() {
        if (getActivity() instanceof androidx.appcompat.app.AppCompatActivity) {
            ((androidx.appcompat.app.AppCompatActivity) getActivity()).setSupportActionBar(toolbarChat);
            if (((androidx.appcompat.app.AppCompatActivity) getActivity()).getSupportActionBar() != null) {
                ((androidx.appcompat.app.AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                ((androidx.appcompat.app.AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
        }
        toolbarChat.setNavigationOnClickListener(v -> getParentFragmentManager().popBackStack());
    }

    private void loadAllianceName() {
        if (allianceId == null) return;

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Alliance alliance = Tasks.await(allianceRepository.getAlliance(allianceId));
                if (alliance != null) {
                    requireActivity().runOnUiThread(() -> toolbarChat.setTitle(alliance.getName()));
                } else {
                    requireActivity().runOnUiThread(() -> toolbarChat.setTitle("Savez (nepoznat)"));
                }
            } catch (Exception e) {
                Log.e(TAG, "Greška pri dohvatanju imena saveza: " + e.getMessage());
                requireActivity().runOnUiThread(() -> toolbarChat.setTitle("Savez (greška)"));
            }
        });
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        rvChatMessages.setLayoutManager(layoutManager);
        chatAdapter = new AllianceChatAdapter(requireContext(), chatRepository);
        rvChatMessages.setAdapter(chatAdapter);
    }

    private void setupMessageInput() {
        btnSendMessage.setOnClickListener(v -> sendMessage());
        etMessageInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEND) {
                sendMessage();
                return true;
            }
            return false;
        });
    }

    private void sendMessage() {
        String messageContent = etMessageInput.getText().toString().trim();
        if (messageContent.isEmpty()) {
            Toast.makeText(requireContext(), "Poruka ne može biti prazna.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (allianceId != null && currentUserId != null) {
            chatRepository.sendMessage(allianceId, currentUserId, messageContent)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            etMessageInput.setText(""); // Obriši unos nakon slanja
                            rvChatMessages.scrollToPosition(chatAdapter.getItemCount() - 1); // Skroluj na dno
                            Log.d(TAG, "Poruka uspešno poslata.");
                            Executors.newSingleThreadExecutor().execute(() -> {
                                logAllianceMessageForMission(allianceId, currentUserId);
                            });                        } else {
                            Toast.makeText(requireContext(), "Greška pri slanju poruke: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            Log.e(TAG, "Greška pri slanju poruke: " + task.getException().getMessage());
                        }
                    });
        }
    }

    private void loadLocalMessages() {
        if (allianceId == null) return;

        chatRepository.getLocalMessagesForAlliance(allianceId)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<AllianceMessage> messages = task.getResult();
                        requireActivity().runOnUiThread(() -> {
                            chatAdapter.setMessages(messages);
                            if (!messages.isEmpty()) {
                                rvChatMessages.scrollToPosition(messages.size() - 1); // Skroluj na dno ako ima poruka
                            }
                            Log.d(TAG, "Učitano " + messages.size() + " lokalnih poruka.");
                        });
                    } else {
                        Log.e(TAG, "Greška pri učitavanju lokalnih poruka: " + (task.getException() != null ? task.getException().getMessage() : ""));
                    }
                });
    }


    private void startListeningForNewMessages() {
        if (allianceId == null) return;

        chatListenerRegistration = chatRepository.addMessagesListener(allianceId, messages -> {
            requireActivity().runOnUiThread(() -> {
                Log.d(TAG, "Primljene nove poruke putem listenera. Ukupno: " + messages.size());
                chatAdapter.setMessages(messages);
                if (!messages.isEmpty()) {
                    rvChatMessages.scrollToPosition(messages.size() - 1); // Skroluj na dno za nove poruke
                }
                handleNewMessageNotifications(messages);
            });
        });
        Log.d(TAG, "Chat listener postavljen.");
    }

    private void handleNewMessageNotifications(List<AllianceMessage> messages) {
        if (messages.isEmpty()) return;

        AllianceMessage latestMessage = messages.get(messages.size() - 1);

        if (!latestMessage.getSenderId().equals(currentUserId)) {
            Log.d(TAG, "Nova poruka od drugog korisnika: " + latestMessage.getContent());
        }
    }

    // Pretpostavljena metoda u AllianceChatRepository.java ili novoj servisnoj klasi

    public void logAllianceMessageForMission(String allianceId, String userId) {
        try {
            User user = userRepository.getLocalUser(userId);
            if (user == null || user.getAllianceId() == null) return;

            Alliance alliance = Tasks.await(allianceRepository.getAllianceById(allianceId));
            if (alliance == null || alliance.getActiveSpecialMissionId() == null) return;

            UserMissionProgress progress = progressDao.getUserMissionProgressForUserAndMission(userId, alliance.getActiveSpecialMissionId());
            if (progress == null) return;

            boolean incremented = progress.incrementDailyMessage();

            if (incremented || alliance.isMissionActive()) {
                progressDao.update(progress);

                String logId = UUID.randomUUID().toString();
                String description = "Poslata poruka u savezu";
                Date currentTime = new Date();
                int damage = 4;

                MissionActivityLog missionActivityLog = new MissionActivityLog(
                        logId,
                        alliance.getActiveSpecialMissionId(),
                        userId,
                        user.getUsername(),
                        description,
                        damage,
                        currentTime
                );

                activityLogDao.insert(missionActivityLog);
                specialMissionRepository.applyDamageAndLogActivity(alliance.getActiveSpecialMissionId(), 4, userId, user.getUsername(), "allianceMessage");
                Log.d(TAG, "Mission activity logged for DailyMessage: " + description);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error logging alliance message for mission: " + e.getMessage());
        }
    }
}