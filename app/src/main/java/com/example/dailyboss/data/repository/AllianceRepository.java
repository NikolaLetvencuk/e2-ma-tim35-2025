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

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService; // Dodato za ExecutorService

/**
 * Repository za upravljanje biznis logikom Saveza (Alliance),
 * uključujući kreiranje, pozive, prihvatanje/odbijanje i ažuriranje članstva.
 */
public class AllianceRepository {

    private final AllianceDao allianceDao;
    private final AllianceInvitationDao invitationDao;
    private final UserDao localUserDao;
    private final UserRepository userRepository;
    private final ExecutorService executorService; // Dodato

    private static final String TAG = "AllianceRepository";

    public AllianceRepository(Context context) {
        this.allianceDao = new AllianceDao(context);
        this.invitationDao = new AllianceInvitationDao(context);
        this.localUserDao = new UserDao(context);
        this.userRepository = new UserRepository(context);
        this.executorService = Executors.newSingleThreadExecutor(); // Inicijalizacija
    }

    public Task<Alliance> createAlliance(String allianceName, String leaderId) {
        return Tasks.call(executorService, () -> { // Koristi executorService
            User leader = localUserDao.getUser(leaderId);
            if (leader != null && leader.getAllianceId() != null && !leader.getAllianceId().isEmpty()) {
                throw new IllegalStateException("Korisnik je već član drugog saveza i ne može kreirati novi.");
            }

            String newAllianceId = UUID.randomUUID().toString();
            Alliance newAlliance = new Alliance(newAllianceId, allianceName, leaderId, new Date());

            boolean saved = allianceDao.insert(newAlliance) > 0;

            if (saved) {
                localUserDao.updateAllianceId(leaderId, newAllianceId);
                try {
                    Tasks.await(userRepository.updateAllianceIdInFirestore(leaderId, newAllianceId));
                } catch (Exception e) {
                    Log.e("REPO_ERROR", "Failed to update alliance ID: " + e.getMessage());
                }
                return newAlliance;
            } else {
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
            if (alliance.isMissionActive()) {
                throw new IllegalStateException("Ne može se ukinuti savez dok je misija aktivna.");
            }

            List<User> members = localUserDao.getUsersByAllianceId(allianceId);

            // 2. Brisanje Saveza
            if (allianceDao.delete(allianceId) > 0) {
                // 3. Resetovanje allianceId za sve članove
                for (User member : members) {
                    localUserDao.updateAllianceId(member.getId(), null);
                    // Opciono: Ažurirati i u Firestore-u za svakog člana
                    // Tasks.await(userRepository.updateAllianceIdInFirestore(member.getId(), null));
                }
                return null;
            } else {
                throw new Exception("Greška pri ukidanju saveza.");
            }
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
            // Provera: Da li je primalac već pozvan? (Opciono, sprečava duplikate)
            // Provera: Da li je primalac već član drugog saveza? (Logika za prihvatanje)

            String invitationId = UUID.randomUUID().toString();
            AllianceInvitation invitation = new AllianceInvitation(
                    invitationId, allianceId, allianceName, senderId, receiverId, new Date(), AllianceInvitationStatus.PENDING
            );

            // Čuvanje poziva u lokalnom kešu primaoca
            if (invitationDao.insert(invitation) > 0) {
                // U realnoj aplikaciji, ovde bi bio poziv ka FCM servisu za slanje notifikacije
                Log.d(TAG, "Invitation sent and saved to local cache for user: " + receiverId);

                // KLJUČNA IZMENA: Vraća ID pozivnice
                return invitationId;
            } else {
                throw new Exception("Greška pri slanju pozivnice.");
            }
        });
    }

    public Task<Alliance> acceptInvitation(String invitationId, String receiverId, String newAllianceId) {
        return Tasks.call(executorService, () -> { // Koristi executorService

            User receiver = localUserDao.getUser(receiverId);
            if (receiver == null) {
                throw new Exception("Korisnik primalac nije pronađen.");
            }

            // 1. PROVERA: Da li je korisnik već član drugog saveza?
            String currentAllianceId = receiver.getAllianceId();
            if (currentAllianceId != null && !currentAllianceId.isEmpty()) {

                Alliance currentAlliance = allianceDao.getAllianceById(currentAllianceId);

                // 2. PROVERA: Da li je misija aktivna u trenutnom savezu?
                if (currentAlliance != null && currentAlliance.isMissionActive()) {
                    throw new IllegalStateException(
                            "Misija iz trenutnog saveza ('" + currentAlliance.getName() + "') je aktivna. Ne možete ga napustiti."
                    );
                }

                // 3. LOGIKA NAPUŠTANJA STAROG SAVEZA (ako nema aktivne misije)
                Log.d(TAG, "Korisnik " + receiverId + " napušta savez: " + currentAllianceId + " da bi prihvatio novi poziv.");

                localUserDao.updateAllianceId(receiverId, null);
                try {
                    Tasks.await(userRepository.updateAllianceIdInFirestore(receiverId, newAllianceId));
                } catch (Exception e) {
                    Log.e("REPO_ERROR", "Failed to update alliance ID: " + e.getMessage());
                }
                // Opciono: Ažuriraj i u Firestore-u da je korisnik napustio stari savez
                // Tasks.await(userRepository.updateAllianceIdInFirestore(receiverId, null));
            }

            // 4. PRIHVATANJE NOVOG POZIVA

            // Postavi novi Alliance ID za korisnika
            localUserDao.updateAllianceId(receiverId, newAllianceId);
            try {
                Tasks.await(userRepository.updateAllianceIdInFirestore(receiverId, newAllianceId));
            } catch (Exception e) {
                Log.e("REPO_ERROR", "Failed to update alliance ID: " + e.getMessage());
            }
            // Ažuriraj status pozivnice
            invitationDao.updateStatus(invitationId, AllianceInvitationStatus.ACCEPTED);

            // Dohvati detalje novog saveza za povratnu vrednost i obaveštenje vođi
            Alliance newAlliance = allianceDao.getAllianceById(newAllianceId);

            if (newAlliance == null) {
                throw new Exception("Savez nije pronađen nakon prihvatanja poziva.");
            }

            Log.d(TAG, "Korisnik " + receiverId + " se pridružio savezu: " + newAlliance.getName());
            return newAlliance;
        });
    }

    public Task<Void> rejectInvitation(String invitationId) {
        return Tasks.call(executorService, () -> { // Koristi executorService
            invitationDao.delete(invitationId);
            return null;
        });
    }

    public List<AllianceInvitation> getPendingInvitations(String userId) {
        // Pretpostavljam da je ova metoda dovoljno brza i može direktno da se pozove
        // Ako radi I/O operacije, treba je umotati u Tasks.call
        return invitationDao.getPendingInvitationsForUser(userId);
    }

    /**
     * Dohvata Alliance objekat iz lokalnog keša po njegovom ID-ju.
     * Ova funkcija je asinhrona i vraća Task<Alliance>.
     * @param allianceId ID saveza koji se dohvaća.
     * @return Task<Alliance> koji se rešava u Alliance objekat ili null ako nije pronađen.
     */
    public Task<Alliance> getAlliance(String allianceId) {
        return Tasks.call(executorService, () -> {
            Log.d(TAG, "Dohvatanje saveza po ID-ju iz lokalnog keša: " + allianceId);
            Alliance alliance = allianceDao.getAllianceById(allianceId);
            if (alliance == null) {
                Log.d(TAG, "Savez sa ID-jem " + allianceId + " nije pronađen u lokalnom kešu.");
            }
            return alliance;
        });
    }
}