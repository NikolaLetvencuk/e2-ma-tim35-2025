package com.example.dailyboss.data.repository;

import android.content.Context;
import android.util.Log;

import com.example.dailyboss.data.dao.EquipmentDao;
import com.example.dailyboss.data.dao.UserEquipmentDao;
import com.example.dailyboss.domain.model.Equipment;
import com.example.dailyboss.domain.model.UserEquipment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore; // Ako se planira sinhronizacija

import java.util.List;
import java.util.UUID;

public class EquipmentRepository {

    private final EquipmentDao equipmentDao;
    private final UserEquipmentDao userEquipmentDao;
    // private final FirebaseFirestore db; // Opciono: Ako se oprema sinhronizuje sa Firestore-om
    private static final String TAG = "EquipmentRepository";

    public EquipmentRepository(Context context) {
        this.equipmentDao = new EquipmentDao(context);
        this.userEquipmentDao = new UserEquipmentDao(context);
        // this.db = FirebaseFirestore.getInstance(); // Opciono
    }

    // --- Metode za Equipment (baza svih tipova opreme) ---

    public interface EquipmentOperationListener {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    public void addEquipment(Equipment equipment, EquipmentOperationListener listener) {
        addOrUpdateEquipment(equipment, listener);
    }

    public void addOrUpdateEquipment(Equipment equipment, EquipmentOperationListener listener) {
        if (equipment.getId() == null || equipment.getId().isEmpty()) {
            equipment.setId(UUID.randomUUID().toString()); // Generiši ID ako ne postoji
        }
        if (equipmentDao.upsert(equipment)) {
            listener.onSuccess();
        } else {
            listener.onFailure("Failed to add or update equipment.");
        }
    }

    public Equipment getEquipmentById(String equipmentId) {
        return equipmentDao.getEquipment(equipmentId);
    }

    public List<Equipment> getAllAvailableEquipment() {
        return equipmentDao.getAllEquipment();
    }

    public void deleteEquipment(String equipmentId, EquipmentOperationListener listener) {
        if (equipmentDao.deleteEquipment(equipmentId)) {
            listener.onSuccess();
        } else {
            listener.onFailure("Failed to delete equipment.");
        }
    }
    
    // Dodatne metode za EquipmentService
    public UserEquipment getUserSpecificEquipment(String userId, String equipmentId) {
        return userEquipmentDao.getUserSpecificEquipment(userId, equipmentId);
    }
    
    public boolean addUserEquipment(UserEquipment userEquipment) {
        return userEquipmentDao.upsert(userEquipment);
    }
    
    public boolean updateUserEquipment(UserEquipment userEquipment) {
        return userEquipmentDao.upsert(userEquipment);
    }
    
    public void getAllUserEquipmentForUser(String userId, UserEquipmentOperationListener listener) {
        List<UserEquipment> userEquipmentList = userEquipmentDao.getAllUserEquipmentForUser(userId);
        if (userEquipmentList != null && !userEquipmentList.isEmpty()) {
            listener.onSuccessList(userEquipmentList);
        } else {
            listener.onFailure("Korisnik nema opreme");
        }
    }
    
    /**
     * Sinhrona verzija za dobijanje korisničke opreme
     */
    public List<UserEquipment> getAllUserEquipmentForUser(String userId) {
        return userEquipmentDao.getAllUserEquipmentForUser(userId);
    }

    // --- Metode za UserEquipment (inventar korisnika) ---

    public interface UserEquipmentOperationListener {
        void onSuccess(UserEquipment userEquipment);
        void onSuccessList(List<UserEquipment> userEquipmentList);
        void onFailure(String errorMessage);
    }

    /**
     * Dodaje novu opremu korisniku ili ažurira postojeću.
     * Ako oprema već postoji i stackable je, povećava se količina/bonus.
     * Ako je nova, kreira se novi unos.
     */
    public void addOrUpdateUserEquipment(String userId, String equipmentId, int quantity, UserEquipmentOperationListener listener) {
        Equipment equipment = equipmentDao.getEquipment(equipmentId);
        if (equipment == null) {
            listener.onFailure("Equipment not found in database.");
            return;
        }

        UserEquipment existingUserEquipment = userEquipmentDao.getUserSpecificEquipment(userId, equipmentId);

        if (existingUserEquipment != null) {
            // Oprema već postoji kod korisnika
            if (equipment.isStackable()) {
                // Ažuriraj količinu i/ili bonus
                if (equipment.isConsumable()) {
                    existingUserEquipment.setQuantity(existingUserEquipment.getQuantity() + quantity);
                } else {
                    // Sabiranje bonusa za trajnu opremu, npr. oružje ili odeća
                    existingUserEquipment.setCurrentBonusValue(existingUserEquipment.getCurrentBonusValue() + equipment.getBonusValue() * quantity);
                }
            } else {
                // Ako nije stackable, možda želimo da blokiramo dodavanje ili da izbacimo grešku
                listener.onFailure("User already has this non-stackable equipment.");
                return;
            }
        } else {
            // Nova oprema za korisnika
            existingUserEquipment = new UserEquipment(
                    UUID.randomUUID().toString(),
                    userId,
                    equipmentId,
                    quantity,
                    false, // Nije aktivna po defaultu
                    0,     // Nema preostalog trajanja dok se ne aktivira
                    0,
                    equipment.getBonusValue() * quantity // Inicijalni bonus
            );
            // Poseban slučaj za odeću gde se sabira postotak odmah
            if (equipment.getType().equals("ARMOR") && equipment.isStackable()) {
                existingUserEquipment.setCurrentBonusValue(equipment.getBonusValue() * quantity);
            }
        }

        if (userEquipmentDao.upsert(existingUserEquipment)) {
            listener.onSuccess(existingUserEquipment);
        } else {
            listener.onFailure("Failed to add or update user equipment.");
        }
    }


    /**
     * Aktivira opremu za korisnika.
     */
    public void activateUserEquipment(String userEquipmentId, UserEquipmentOperationListener listener) {
        UserEquipment userEquipment = userEquipmentDao.getUserEquipment(userEquipmentId);
        if (userEquipment == null) {
            listener.onFailure("User equipment not found.");
            return;
        }
        Equipment equipment = equipmentDao.getEquipment(userEquipment.getEquipmentId());
        if (equipment == null) {
            listener.onFailure("Associated equipment definition not found.");
            return;
        }

        if (userEquipment.isActive()) {
            listener.onFailure("Equipment is already active.");
            return;
        }

        // Postavi aktivno stanje i inicijalizuj trajanje
        userEquipment.setActive(true);
        if (equipment.getDurationBattles() > 0) {
            userEquipment.setRemainingDurationBattles(equipment.getDurationBattles());
        }
        if (equipment.getDurationDays() > 0) {
            userEquipment.setActivationTimestamp(System.currentTimeMillis());
        }

        // Smanji quantity za potrošnu opremu (napitke) ako je aktivirana
        if (equipment.isConsumable() && userEquipment.getQuantity() > 0) {
            userEquipment.setQuantity(userEquipment.getQuantity() - 1);
        } else if (equipment.isConsumable() && userEquipment.getQuantity() == 0) {
            listener.onFailure("No more consumable items of this type left to activate.");
            return;
        }


        if (userEquipmentDao.upsert(userEquipment)) {
            listener.onSuccess(userEquipment);
        } else {
            listener.onFailure("Failed to activate user equipment.");
        }
    }

    /**
     * Deaktivira opremu za korisnika.
     * Često se koristi kada oprema istekne ili je jednokratna.
     */
    public void deactivateUserEquipment(String userEquipmentId, UserEquipmentOperationListener listener) {
        UserEquipment userEquipment = userEquipmentDao.getUserEquipment(userEquipmentId);
        if (userEquipment == null) {
            listener.onFailure("User equipment not found.");
            return;
        }

        userEquipment.setActive(false);
        userEquipment.setRemainingDurationBattles(0); // Reset trajanja
        userEquipment.setActivationTimestamp(0); // Reset trajanja po danima

        // Ako je oprema potrošena i nema je više, možemo je i obrisati iz inventara
        if (userEquipment.getQuantity() == 0 && equipmentDao.getEquipment(userEquipment.getEquipmentId()).isConsumable()) {
            if (userEquipmentDao.deleteUserEquipment(userEquipmentId)) {
                listener.onSuccess((UserEquipment) null);
            } else {
                listener.onFailure("Failed to delete consumed user equipment.");
            }
        } else if (userEquipmentDao.upsert(userEquipment)) {
            listener.onSuccess(userEquipment);
        } else {
            listener.onFailure("Failed to deactivate user equipment.");
        }
    }

    /**
     * Briše korisničku opremu potpuno iz inventara
     */
    public void deleteUserEquipment(String userEquipmentId, UserEquipmentOperationListener listener) {
        if (userEquipmentDao.deleteUserEquipment(userEquipmentId)) {
            listener.onSuccess((UserEquipment) null);
        } else {
            listener.onFailure("Failed to delete user equipment.");
        }
    }

    /**
     * Dohvata svu opremu koju korisnik poseduje.
     */
    public List<UserEquipment> getUserInventory(String userId) {
        return userEquipmentDao.getUserEquipmentForUser(userId);
    }

    /**
     * Smanjuje preostalo trajanje opreme po borbama.
     * Trebalo bi da se poziva nakon svake borbe sa bosom.
     */
    public void decrementEquipmentDuration(String userId) {
        List<UserEquipment> activeEquipment = getUserInventory(userId);
        for (UserEquipment ue : activeEquipment) {
            if (ue.isActive() && ue.getRemainingDurationBattles() > 0) {
                ue.setRemainingDurationBattles(ue.getRemainingDurationBattles() - 1);
                if (ue.getRemainingDurationBattles() <= 0) {
                    // Oprema je istekla, deaktiviraj je
                    deactivateUserEquipment(ue.getId(), new UserEquipmentOperationListener() {
                        @Override
                        public void onSuccess(UserEquipment updatedUe) {
                            Log.d(TAG, "Equipment " + ue.getId() + " duration expired and deactivated.");
                        }

                        @Override
                        public void onSuccessList(List<UserEquipment> userEquipmentList) { 
                            // Nije primenljivo ovde 
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            Log.e(TAG, "Failed to deactivate expired equipment: " + errorMessage);
                        }
                    });
                } else {
                    userEquipmentDao.upsert(ue); // Ažuriraj preostalo trajanje
                }
            }
        }
    }

    /**
     * Računa ukupan bonus za određenog korisnika za specifičan tip bonusa.
     * Npr. za PP, šansu za napad, novčiće.
     */
    public double calculateTotalBonus(String userId, String bonusType) {
        double totalBonus = 0.0;
        List<UserEquipment> userInventory = getUserInventory(userId);

        for (UserEquipment ue : userInventory) {
            if (ue.isActive()) {
                Equipment eq = equipmentDao.getEquipment(ue.getEquipmentId());
                if (eq != null && eq.getBonusType().equals(bonusType)) {
                    // Proveri i trajanje po danima ako je relevantno
                    if (eq.getDurationDays() > 0) {
                        long elapsedDays = (System.currentTimeMillis() - ue.getActivationTimestamp()) / (1000 * 60 * 60 * 24);
                        if (elapsedDays >= eq.getDurationDays()) {
                            // Oprema je istekla po danima, deaktiviraj je
                            deactivateUserEquipment(ue.getId(), new UserEquipmentOperationListener() {
                                @Override
                                public void onSuccess(UserEquipment updatedUe) {
                                    Log.d(TAG, "Equipment " + ue.getId() + " duration expired (days) and deactivated.");
                                }
                                
                                @Override 
                                public void onSuccessList(List<UserEquipment> userEquipmentList) {
                                    // Nije primenljivo ovde
                                }
                                
                                @Override
                                public void onFailure(String errorMessage) {
                                    Log.e(TAG, "Failed to deactivate expired equipment (days): " + errorMessage);
                                }
                            });
                            continue; // Preskoči ovu opremu jer je istekla
                        }
                    }
                    totalBonus += ue.getCurrentBonusValue(); // Koristi currentBonusValue jer on može biti sabran
                }
            }
        }
        return totalBonus;
    }
}