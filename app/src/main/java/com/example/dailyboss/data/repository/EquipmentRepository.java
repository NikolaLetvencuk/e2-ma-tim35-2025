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
    private static final String TAG = "EquipmentRepository";

    public EquipmentRepository(Context context) {
        this.equipmentDao = new EquipmentDao(context);
        this.userEquipmentDao = new UserEquipmentDao(context);
    }


    public interface EquipmentOperationListener {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    public void addEquipment(Equipment equipment, EquipmentOperationListener listener) {
        addOrUpdateEquipment(equipment, listener);
    }

    public void addOrUpdateEquipment(Equipment equipment, EquipmentOperationListener listener) {
        if (equipment.getId() == null || equipment.getId().isEmpty()) {
            equipment.setId(UUID.randomUUID().toString());
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
    

    public List<UserEquipment> getAllUserEquipmentForUser(String userId) {
        return userEquipmentDao.getAllUserEquipmentForUser(userId);
    }


    public interface UserEquipmentOperationListener {
        void onSuccess(UserEquipment userEquipment);
        void onSuccessList(List<UserEquipment> userEquipmentList);
        void onFailure(String errorMessage);
    }


    public void addOrUpdateUserEquipment(String userId, String equipmentId, int quantity, UserEquipmentOperationListener listener) {
        Equipment equipment = equipmentDao.getEquipment(equipmentId);
        if (equipment == null) {
            listener.onFailure("Equipment not found in database.");
            return;
        }

        UserEquipment existingUserEquipment = userEquipmentDao.getUserSpecificEquipment(userId, equipmentId);

        if (existingUserEquipment != null) {
            if (equipment.isStackable()) {
                if (equipment.isConsumable()) {
                    existingUserEquipment.setQuantity(existingUserEquipment.getQuantity() + quantity);
                } else {
                    existingUserEquipment.setCurrentBonusValue(existingUserEquipment.getCurrentBonusValue() + equipment.getBonusValue() * quantity);
                }
            } else {
                listener.onFailure("User already has this non-stackable equipment.");
                return;
            }
        } else {
            existingUserEquipment = new UserEquipment(
                    UUID.randomUUID().toString(),
                    userId,
                    equipmentId,
                    quantity,
                    false,
                    0,
                    0,
                    equipment.getBonusValue() * quantity
            );
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

        userEquipment.setActive(true);
        if (equipment.getDurationBattles() > 0) {
            userEquipment.setRemainingDurationBattles(equipment.getDurationBattles());
        }
        if (equipment.getDurationDays() > 0) {
            userEquipment.setActivationTimestamp(System.currentTimeMillis());
        }

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

    public void deactivateUserEquipment(String userEquipmentId, UserEquipmentOperationListener listener) {
        UserEquipment userEquipment = userEquipmentDao.getUserEquipment(userEquipmentId);
        if (userEquipment == null) {
            listener.onFailure("User equipment not found.");
            return;
        }

        userEquipment.setActive(false);
        userEquipment.setRemainingDurationBattles(0);
        userEquipment.setActivationTimestamp(0);

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

    public void deleteUserEquipment(String userEquipmentId, UserEquipmentOperationListener listener) {
        if (userEquipmentDao.deleteUserEquipment(userEquipmentId)) {
            listener.onSuccess((UserEquipment) null);
        } else {
            listener.onFailure("Failed to delete user equipment.");
        }
    }

    public List<UserEquipment> getUserInventory(String userId) {
        return userEquipmentDao.getUserEquipmentForUser(userId);
    }

    public void decrementEquipmentDuration(String userId) {
        List<UserEquipment> activeEquipment = getUserInventory(userId);
        for (UserEquipment ue : activeEquipment) {
            if (ue.isActive() && ue.getRemainingDurationBattles() > 0) {
                ue.setRemainingDurationBattles(ue.getRemainingDurationBattles() - 1);
                if (ue.getRemainingDurationBattles() <= 0) {
                    deactivateUserEquipment(ue.getId(), new UserEquipmentOperationListener() {
                        @Override
                        public void onSuccess(UserEquipment updatedUe) {
                            Log.d(TAG, "Equipment " + ue.getId() + " duration expired and deactivated.");
                        }

                        @Override
                        public void onSuccessList(List<UserEquipment> userEquipmentList) { 
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            Log.e(TAG, "Failed to deactivate expired equipment: " + errorMessage);
                        }
                    });
                } else {
                    userEquipmentDao.upsert(ue);
                }
            }
        }
    }

    public double calculateTotalBonus(String userId, String bonusType) {
        double totalBonus = 0.0;
        List<UserEquipment> userInventory = getUserInventory(userId);

        for (UserEquipment ue : userInventory) {
            if (ue.isActive()) {
                Equipment eq = equipmentDao.getEquipment(ue.getEquipmentId());
                if (eq != null && eq.getBonusType().equals(bonusType)) {
                    if (eq.getDurationDays() > 0) {
                        long elapsedDays = (System.currentTimeMillis() - ue.getActivationTimestamp()) / (1000 * 60 * 60 * 24);
                        if (elapsedDays >= eq.getDurationDays()) {
                            deactivateUserEquipment(ue.getId(), new UserEquipmentOperationListener() {
                                @Override
                                public void onSuccess(UserEquipment updatedUe) {
                                    Log.d(TAG, "Equipment " + ue.getId() + " duration expired (days) and deactivated.");
                                }
                                
                                @Override 
                                public void onSuccessList(List<UserEquipment> userEquipmentList) {
                                }
                                
                                @Override
                                public void onFailure(String errorMessage) {
                                    Log.e(TAG, "Failed to deactivate expired equipment (days): " + errorMessage);
                                }
                            });
                            continue;
                        }
                    }
                    totalBonus += ue.getCurrentBonusValue();
                }
            }
        }
        return totalBonus;
    }

    public com.example.dailyboss.domain.model.EquipmentDropResult getEquipmentDrop() {
        Log.d(TAG, "Generating equipment drop");
        
        com.example.dailyboss.domain.model.EquipmentDropResult result = new com.example.dailyboss.domain.model.EquipmentDropResult();
        
        if (Math.random() < 0.5) {
            String equipmentType = Math.random() < 0.5 ? "CLOTHING" : "WEAPON";
            String equipmentId = getRandomEquipmentId(equipmentType);
            String equipmentName = getEquipmentNameById(equipmentId);
            
            result.setDropped(true);
            result.setEquipmentId(equipmentId);
            result.setEquipmentName(equipmentName);
            result.setEquipmentType(equipmentType);
            result.setMessage("Dobili ste opremu: " + equipmentName);
            
            Log.d(TAG, "Equipment dropped: " + equipmentName + " (ID: " + equipmentId + ", Type: " + equipmentType + ")");
        } else {
            result.setDropped(false);
            result.setMessage("Niste dobili opremu");
            Log.d(TAG, "No equipment dropped");
        }
        
        return result;
    }

    private String getRandomEquipmentId(String equipmentType) {
        Log.d(TAG, "Getting random equipment ID for type: " + equipmentType);
        
        String[] clothingIds = {"gloves", "shield", "boots"};
        String[] weaponIds = {"sword", "bow"};
        
        String[] ids = "CLOTHING".equals(equipmentType) ? clothingIds : weaponIds;
        String selectedId = ids[(int) (Math.random() * ids.length)];
        
        Log.d(TAG, "Selected equipment ID: " + selectedId);
        return selectedId;
    }

    private String getEquipmentNameById(String equipmentId) {
        switch (equipmentId) {
            case "gloves": return "Rukavice";
            case "shield": return "Štit";
            case "boots": return "Čizme";
            case "sword": return "Mač";
            case "bow": return "Luk i Strela";
            default: return "Nepoznata Oprema";
        }
    }
}