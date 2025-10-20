package com.example.dailyboss.data.repository;

import android.content.Context;
import android.util.Log;

import com.example.dailyboss.data.dao.AllianceDao;
import com.example.dailyboss.data.dao.AllianceInvitationDao;
import com.example.dailyboss.data.dao.UserDao;
import com.example.dailyboss.domain.model.Alliance;
import com.example.dailyboss.domain.model.AllianceInvitation;
import com.example.dailyboss.domain.model.AllianceInvitationStatus;
import com.example.dailyboss.domain.model.User;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore; // Add this import

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class AllianceRepository {

    private final AllianceDao allianceDao;
    private final AllianceInvitationDao invitationDao;
    private final UserDao localUserDao;
    private final UserRepository userRepository;
    private final ExecutorService executorService;
    private final FirebaseFirestore db; // Add Firestore instance

    private static final String TAG = "AllianceRepository";

    public AllianceRepository(Context context) {
        this.allianceDao = new AllianceDao(context);
        this.invitationDao = new AllianceInvitationDao(context);
        this.localUserDao = new UserDao(context);
        this.userRepository = new UserRepository(context);
        this.executorService = Executors.newSingleThreadExecutor();
        this.db = FirebaseFirestore.getInstance(); // Initialize Firestore
    }

    public Task<Alliance> createAlliance(String allianceName, String leaderId) {
        return Tasks.call(executorService, () -> { // Koristi executorService
            User leader = localUserDao.getUser(leaderId);
            if (leader != null && leader.getAllianceId() != null && !leader.getAllianceId().isEmpty()) {
                throw new IllegalStateException("Korisnik je već član drugog saveza i ne može kreirati novi.");
            }

            String newAllianceId = UUID.randomUUID().toString();
            Alliance newAlliance = new Alliance(newAllianceId, allianceName, leaderId, new Date(), null);

            // Save to Firestore first for immediate global consistency
            Tasks.await(db.collection("alliances").document(newAllianceId).set(newAlliance));
            Log.d(TAG, "Alliance saved to Firestore: " + newAllianceId);

            boolean saved = allianceDao.insert(newAlliance) > 0;

            if (saved) {
                // Update local user and then Firestore user
                localUserDao.updateAllianceId(leaderId, newAllianceId);
                Tasks.await(userRepository.updateAllianceIdInFirestore(leaderId, newAllianceId));
                Log.d(TAG, "Leader's alliance ID updated in local DB and Firestore.");
                return newAlliance;
            } else {
                // If local save fails, try to delete from Firestore to prevent inconsistencies
                db.collection("alliances").document(newAllianceId).delete()
                        .addOnFailureListener(e -> Log.e(TAG, "Failed to delete alliance from Firestore after local DB failure: " + e.getMessage()));
                throw new Exception("Neuspešno čuvanje saveza u lokalnoj bazi.");
            }
        });
    }

    public Task<Void> disbandAlliance(String allianceId, String leaderId) {
        return Tasks.call(executorService, () -> { // Koristi executorService
            Alliance alliance = allianceDao.getAllianceById(allianceId);

            if (alliance == null) {
                throw new IllegalArgumentException("Savez ne postoji.");
            }
            if (!alliance.getLeaderId().equals(leaderId)) {
                throw new SecurityException("Samo vođa može ukinuti savez.");
            }
            if (alliance.isMissionActive()) { // Assuming isMissionActive checks activeSpecialMissionId internally
                throw new IllegalStateException("Ne može se ukinuti savez dok je misija aktivna.");
            }

            List<User> members = localUserDao.getUsersByAllianceId(allianceId);

            // Delete from Firestore first
            Tasks.await(db.collection("alliances").document(allianceId).delete());
            Log.d(TAG, "Alliance deleted from Firestore: " + allianceId);

            if (allianceDao.delete(allianceId) > 0) {
                for (User member : members) {
                    localUserDao.updateAllianceId(member.getId(), null);
                    // Update Firestore for each member
                    Tasks.await(userRepository.updateAllianceIdInFirestore(member.getId(), null));
                    Log.d(TAG, "Member " + member.getId() + " alliance ID set to null in local DB and Firestore.");
                }
                return null;
            } else {
                // If local delete fails, consider re-adding to Firestore if necessary, or just log
                throw new Exception("Greška pri ukidanju saveza.");
            }
        });
    }

    // New method: Leave Alliance
    public Task<Void> leaveAlliance(String allianceId, String userId) {
        return Tasks.call(executorService, () -> {
            User user = localUserDao.getUser(userId);
            if (user == null || !user.getAllianceId().equals(allianceId)) {
                throw new IllegalArgumentException("Korisnik nije član ovog saveza.");
            }

            Alliance alliance = allianceDao.getAllianceById(allianceId);
            if (alliance != null && alliance.isMissionActive()) { // Assuming isMissionActive checks activeSpecialMissionId internally
                throw new IllegalStateException("Ne možete napustiti savez dok je misija aktivna.");
            }

            // Update user's alliance ID to null in local DB and Firestore
            localUserDao.updateAllianceId(userId, null);
            Tasks.await(userRepository.updateAllianceIdInFirestore(userId, null));
            Log.d(TAG, "User " + userId + " successfully left alliance " + allianceId);
            return null;
        });
    }

    public Alliance getCurrentAlliance(String userId) {
        User user = localUserDao.getUser(userId);
        if (user != null && user.getAllianceId() != null) {
            return allianceDao.getAllianceById(user.getAllianceId());
        }
        return null;
    }

    public AllianceInvitation getAllianceInvitation(String invitationId) {
        return invitationDao.getInvitationById(invitationId);
    }

    public Task<String> sendInvitation(String allianceId, String allianceName, String senderId, String receiverId) {
        return Tasks.call(executorService, () -> { // Koristi executorService

            String invitationId = UUID.randomUUID().toString();
            AllianceInvitation invitation = new AllianceInvitation(
                    invitationId, allianceId, allianceName, senderId, receiverId, new Date(), AllianceInvitationStatus.PENDING
            );

            // Save invitation to Firestore
            Tasks.await(db.collection("allianceInvitations").document(invitationId).set(invitation));
            Log.d(TAG, "Invitation sent and saved to Firestore for user: " + receiverId);

            if (invitationDao.insert(invitation) > 0) {
                Log.d(TAG, "Invitation sent and saved to local cache for user: " + receiverId);
                return invitationId;
            } else {
                // If local save fails, try to delete from Firestore
                db.collection("allianceInvitations").document(invitationId).delete()
                        .addOnFailureListener(e -> Log.e(TAG, "Failed to delete invitation from Firestore after local DB failure: " + e.getMessage()));
                throw new Exception("Greška pri slanju pozivnice.");
            }
        });
    }

    public Task<Alliance> acceptInvitation(String invitationId, String receiverId, String newAllianceId) {
        return Tasks.call(executorService, () -> {

            User receiver = localUserDao.getUser(receiverId);
            if (receiver == null) {
                throw new Exception("Korisnik primalac nije pronađen.");
            }

            String currentAllianceId = receiver.getAllianceId();
            if (currentAllianceId != null && !currentAllianceId.isEmpty()) {

                Alliance currentAlliance = allianceDao.getAllianceById(currentAllianceId);

                if (currentAlliance != null && currentAlliance.isMissionActive()) {
                    throw new IllegalStateException(
                            "Misija iz trenutnog saveza ('" + currentAlliance.getName() + "') je aktivna. Ne možete ga napustiti."
                    );
                }

                Log.d(TAG, "Korisnik " + receiverId + " napušta savez: " + currentAllianceId + " da bi prihvatio novi poziv.");

                // Update user's current alliance ID to null in local DB and Firestore
                localUserDao.updateAllianceId(receiverId, null);
                Tasks.await(userRepository.updateAllianceIdInFirestore(receiverId, null));
            }

            // Update user's alliance ID to the new alliance in local DB and Firestore
            localUserDao.updateAllianceId(receiverId, newAllianceId);
            Tasks.await(userRepository.updateAllianceIdInFirestore(receiverId, newAllianceId));

            // Update invitation status in local DB and Firestore
            invitationDao.updateStatus(invitationId, AllianceInvitationStatus.ACCEPTED);
            Tasks.await(db.collection("allianceInvitations").document(invitationId)
                    .update("status", AllianceInvitationStatus.ACCEPTED.name()));


            Alliance newAlliance = allianceDao.getAllianceById(newAllianceId);

            if (newAlliance == null) {
                throw new Exception("Savez nije pronađen nakon prihvatanja poziva.");
            }

            Log.d(TAG, "Korisnik " + receiverId + " se pridružio savezu: " + newAlliance.getName());
            return newAlliance;
        });
    }

    public Task<Void> rejectInvitation(String invitationId) {
        return Tasks.call(executorService, () -> {
            // Delete invitation from local DB and Firestore
            invitationDao.delete(invitationId);
            Tasks.await(db.collection("allianceInvitations").document(invitationId).delete());
            return null;
        });
    }

    public List<AllianceInvitation> getPendingInvitations(String userId) {
        return invitationDao.getPendingInvitationsForUser(userId);
    }

    public Task<Alliance> getAlliance(String allianceId) {
        return Tasks.call(executorService, () -> {
            Log.d(TAG, "Dohvatanje saveza po ID-ju iz lokalnog keša: " + allianceId);
            Alliance alliance = allianceDao.getAllianceById(allianceId);
            if (alliance == null) {
                Log.d(TAG, "Savez sa ID-jem " + allianceId + " nije pronađen u lokalnom kešu.");
                // Try to fetch from Firestore if not found locally
                Task<Alliance> firestoreAllianceTask = db.collection("alliances").document(allianceId).get()
                        .continueWith(task -> {
                            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                                Alliance fetchedAlliance = task.getResult().toObject(Alliance.class);
                                if (fetchedAlliance != null) {
                                    allianceDao.insert(fetchedAlliance); // Cache it locally
                                    return fetchedAlliance;
                                }
                            }
                            return null;
                        });
                alliance = Tasks.await(firestoreAllianceTask);
            }
            return alliance;
        });
    }

    // New method: Update Alliance's Active Special Mission ID
    public Task<Void> updateAllianceActiveSpecialMission(String allianceId, String missionId) {
        return Tasks.call(executorService, () -> {
            // Update in local DB

            allianceDao.updateActiveSpecialMissionId(allianceId, missionId);
            // Update in Firestore
            boolean isActive = (missionId != null && !missionId.isEmpty());
            Tasks.await(db.collection("alliances").document(allianceId).update(
                    "activeSpecialMissionId", missionId,
                    "missionActive", isActive
            ));
            Tasks.await(db.collection("alliances").document(allianceId).update("activeSpecialMissionId", missionId));
            return null;
        });
    }

    // New method: getAllianceById (returns a Task for async calls)
    public Task<Alliance> getAllianceById(String allianceId) {
        return Tasks.call(executorService, () -> {
            return allianceDao.getAllianceById(allianceId);
        });
    }

    public Task<Void> updateAlliance(Alliance alliance) {
        return Tasks.call(executorService, () -> {
            // Update in local DB
            boolean updatedLocal = allianceDao.update(alliance);
            if (!updatedLocal) {
                Log.e(TAG, "Failed to update alliance in local DB: " + alliance.getId());
                throw new Exception("Neuspešno ažuriranje saveza u lokalnoj bazi.");
            }

            // Update in Firestore
            Tasks.await(db.collection("alliances").document(alliance.getId()).set(alliance));
            Log.d(TAG, "Alliance updated in Firestore: " + alliance.getId());
            return null;
        });
    }
}