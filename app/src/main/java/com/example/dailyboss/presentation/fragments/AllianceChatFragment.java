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
import com.example.dailyboss.data.repository.AllianceChatRepository;
import com.example.dailyboss.data.repository.AllianceRepository;
import com.example.dailyboss.domain.model.Alliance;
import com.example.dailyboss.domain.model.AllianceMessage;
import com.example.dailyboss.presentation.adapters.AllianceChatAdapter;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.Collections;
import java.util.List;
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
    private AllianceRepository allianceRepository; // Za dohvatanje imena saveza za toolbar

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
            // Možda treba da se vratiš na prethodni fragment
            getParentFragmentManager().popBackStack();
        }

        chatRepository = new AllianceChatRepository(requireContext());
        allianceRepository = new AllianceRepository(requireContext());
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
        loadLocalMessages(); // Prvo učitaj lokalno keširane poruke
        startListeningForNewMessages(); // Zatim postavi real-time listener
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (chatListenerRegistration != null) {
            chatListenerRegistration.remove(); // Ukloni listener kada se fragment uništi
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
                        } else {
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
                // TODO: Ovde implementirajte logiku za slanje notifikacija, ako je poruka od drugog korisnika
                // Možda uporedite sa senderId-jem poslednje poruke i currentUserId-jem
                handleNewMessageNotifications(messages);
            });
        });
        Log.d(TAG, "Chat listener postavljen.");
    }

    private void handleNewMessageNotifications(List<AllianceMessage> messages) {
        if (messages.isEmpty()) return;

        AllianceMessage latestMessage = messages.get(messages.size() - 1);

        // Proveri da li je poslednja poruka od drugog korisnika
        if (!latestMessage.getSenderId().equals(currentUserId)) {
            // TODO: Pokreni Foreground Service ili Notification Manager da prikažeš notifikaciju
            // Ova funkcionalnost zahteva Firebase Cloud Messaging (FCM) za obavestenja u realnom vremenu
            // kada aplikacija nije u prvom planu, ili lokalna notifikacija ako je app otvorena.
            Log.d(TAG, "Nova poruka od drugog korisnika: " + latestMessage.getContent());
            // Primer lokalne notifikacije (za jednostavnost, ali nije idealno za real-time obavestenja van aplikacije)
            // showLocalNotification(latestMessage.getSenderId(), latestMessage.getContent());
        }
    }

    // TODO: Implementirati showLocalNotification metodu i integrisati FCM za push notifikacije.
    // Primer (nije potpuno funkcionalan bez dodatnih klasa i konfiguracije):
    /*
    private void showLocalNotification(String senderId, String messageContent) {
        // Potrebni kanali za notifikacije na Androidu 8.0+
        // NotificationManager notificationManager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);
        // String channelId = "alliance_chat_channel";
        // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        //     NotificationChannel channel = new NotificationChannel(channelId, "Alliance Chat", NotificationManager.IMPORTANCE_DEFAULT);
        //     notificationManager.createNotificationChannel(channel);
        // }

        // Executors.newSingleThreadExecutor().execute(() -> {
        //     try {
        //         String senderUsername = Tasks.await(chatRepository.getUsernameForSender(senderId));
        //         NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), channelId)
        //                 .setSmallIcon(R.drawable.ic_notification) // Vaša ikonica za notifikacije
        //                 .setContentTitle("Nova poruka u savezu od " + senderUsername)
        //                 .setContentText(messageContent)
        //                 .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        //                 .setAutoCancel(true);

        //         // Intent za otvaranje četa kada se klikne na notifikaciju
        //         // Intent intent = new Intent(requireContext(), YourMainActivity.class); // Zamenite YourMainActivity
        //         // intent.putExtra("openChatAllianceId", allianceId);
        //         // PendingIntent pendingIntent = PendingIntent.getActivity(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        //         // builder.setContentIntent(pendingIntent);

        //         // notificationManager.notify(1, builder.build()); // Jedinstveni ID za notifikaciju
        //     } catch (Exception e) {
        //         Log.e(TAG, "Greška pri prikazu notifikacije: " + e.getMessage());
        //     }
        // });
    }
    */
}