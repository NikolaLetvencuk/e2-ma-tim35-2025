package com.example.dailyboss.data.repository;

import android.content.Context;
import android.util.Log;

import com.example.dailyboss.data.dao.AllianceMessageDao;
import com.example.dailyboss.domain.model.AllianceMessage;
import com.example.dailyboss.domain.model.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class AllianceChatRepository {

    private final FirebaseFirestore db;
    private final AllianceMessageDao allianceMessageDao;
    private final UserRepository userRepository;
    private final ExecutorService executorService;

    private static final String COLLECTION_ALLIANCES = "alliances";
    private static final String SUBCOLLECTION_MESSAGES = "messages";
    private static final String TAG = "AllianceChatRepository";

    public AllianceChatRepository(Context context) {
        this.db = FirebaseFirestore.getInstance();
        this.allianceMessageDao = new AllianceMessageDao(context);
        this.userRepository = new UserRepository(context);
        this.executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * Šalje novu poruku u Firestore i kešira je lokalno.
     * Koristi serverski timestamp za preciznost.
     * Dohvata username pošiljaoca pre slanja.
     * @param allianceId ID saveza
     * @param senderId ID pošiljaoca
     * @param content Sadržaj poruke
     * @return Task<Void> za praćenje uspešnosti operacije
     */
    public Task<Void> sendMessage(String allianceId, String senderId, String content) {
        // Prvo dohvati username pošiljaoca
        // Ovo će sada pokušati samo lokalno da dohvati username
        return getUsernameForSender(senderId)
                .continueWithTask(usernameTask -> {
                    if (usernameTask.isSuccessful() && usernameTask.getResult() != null && !usernameTask.getResult().equals("Nepoznat korisnik")) {
                        String senderUsername = usernameTask.getResult();

                        CollectionReference messagesRef = db.collection(COLLECTION_ALLIANCES)
                                .document(allianceId)
                                .collection(SUBCOLLECTION_MESSAGES);

                        DocumentReference newMessageRef = messagesRef.document();
                        String messageId = newMessageRef.getId();

                        Map<String, Object> messageData = new HashMap<>();
                        messageData.put("id", messageId);
                        messageData.put("allianceId", allianceId);
                        messageData.put("senderId", senderId);
                        messageData.put("senderUsername", senderUsername);
                        messageData.put("content", content);
                        messageData.put("timestamp", FieldValue.serverTimestamp());

                        return newMessageRef.set(messageData)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Poruka uspešno poslata u Firestore: " + messageId);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Greška pri slanju poruke: " + e.getMessage());
                                });
                    } else {
                        // Ako username nije uspešno dohvaćen lokalno, nećemo moći da pošaljemo poruku sa username-om.
                        // Ovo je ograničenje ako nema Firebase fallback metode u UserRepository.
                        Log.e(TAG, "Nije moguće dohvatiti username za " + senderId + " iz lokalnog keša pre slanja poruke. Greška: " +
                                (usernameTask.getException() != null ? usernameTask.getException().getMessage() : "Nema rezultata ili nepoznata greška."));
                        return Tasks.forException(new Exception("Failed to get sender username from local cache for message."));
                    }
                });
    }

    /**
     * Dohvata lokalno keširane poruke za dati savez.
     * @param allianceId ID saveza
     * @return Task<List<AllianceMessage>> sa listom AllianceMessage objekata
     */
    public Task<List<AllianceMessage>> getLocalMessagesForAlliance(String allianceId) {
        return Tasks.call(executorService, () -> allianceMessageDao.getMessagesForAlliance(allianceId));
    }

    /**
     * Postavlja real-time listener za poruke u Firestore-u.
     * Sada automatski sadrži senderUsername.
     * @param allianceId ID saveza
     * @param onNewMessages Callback koji se poziva kada stignu nove poruke.
     * @return ListenerRegistration za uklanjanje listenera.
     */
    public ListenerRegistration addMessagesListener(String allianceId, Consumer<List<AllianceMessage>> onNewMessages) {
        CollectionReference messagesRef = db.collection(COLLECTION_ALLIANCES)
                .document(allianceId)
                .collection(SUBCOLLECTION_MESSAGES);

        return messagesRef.orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Slušanje poruka neuspelo.", e);
                        return;
                    }

                    if (snapshots != null) {
                        List<AllianceMessage> allMessages = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : snapshots) {
                            AllianceMessage message = doc.toObject(AllianceMessage.class);
                            message.setId(doc.getId());
                            allMessages.add(message);

                            executorService.execute(() -> {
                                if (message.getTimestamp() == null) {
                                    Log.d(TAG, "Poruka " + message.getId() + " ima null timestamp, preskačem lokalno keširanje za sada.");
                                    return;
                                }
                                long result = allianceMessageDao.insert(message);
                                if (result == -1) {
                                    Log.d(TAG, "Poruka " + message.getId() + " već postoji lokalno, preskačem insert.");
                                } else {
                                    Log.d(TAG, "Poruka keširana lokalno: " + message.getId());
                                }
                            });
                        }
                        onNewMessages.accept(allMessages);
                    }
                });
    }

    /**
     * Dohvata username za datog pošiljaoca.
     * SADA EKSKLUZIVNO POKUŠAVA SAMO LOKALNO.
     * @param userId ID korisnika
     * @return Task<String> sa username-om. Ako nije pronađen lokalno, vraća "Nepoznat korisnik".
     */
    public Task<String> getUsernameForSender(String userId) {
        // Umotaj lokalni poziv u Task koji se izvršava na pozadinskoj niti
        // Očekuje se da userRepository.getLocalUser(userId) vraća User objekat ili null.
        return Tasks.call(executorService, () -> {
            User localUser = userRepository.getLocalUser(userId);
            if (localUser != null) {
                return localUser.getUsername();
            } else {
                Log.w(TAG, "Korisnik " + userId + " nije pronađen lokalno.");
                return "Nepoznat korisnik";
            }
        });
    }
}