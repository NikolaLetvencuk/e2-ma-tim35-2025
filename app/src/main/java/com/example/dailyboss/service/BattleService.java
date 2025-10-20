package com.example.dailyboss.service;

import android.content.Context;
import android.util.Log;

import com.example.dailyboss.data.dao.TaskInstanceDao;
import com.example.dailyboss.data.dao.UserProfileDao;
import com.example.dailyboss.data.repository.EquipmentRepository;
import com.example.dailyboss.data.repository.TaskInstanceRepositoryImpl;
import com.example.dailyboss.data.repository.UserProfileRepository;
import com.example.dailyboss.data.repository.UserStatisticRepository;
import com.example.dailyboss.domain.enums.TaskStatus;
import com.example.dailyboss.domain.model.BattleResult;
import com.example.dailyboss.domain.model.BattleState;
import com.example.dailyboss.domain.model.EquipmentDropResult;
import com.example.dailyboss.domain.model.TaskInstance;
import com.example.dailyboss.domain.model.UserEquipment;
import com.example.dailyboss.domain.model.UserProfile;
import com.example.dailyboss.domain.model.UserStatistic;

import java.util.List;
import java.util.Random;


public class BattleService {
    
    private static final String TAG = "BattleService";
    
    private final Context context;
    private final EquipmentRepository equipmentRepository;
    private final UserStatisticRepository userStatisticRepository;
    private final Random rng = new Random();
    private TaskInstanceDao taskInstanceRepository;
    private UserProfileDao userProfileRepository;

    public BattleService(Context context) {
        this.context = context.getApplicationContext();
        this.equipmentRepository = new EquipmentRepository(context);
        this.userStatisticRepository = new UserStatisticRepository(context);
        this.taskInstanceRepository = new TaskInstanceDao(context);
        this.userProfileRepository = new UserProfileDao(context);

    }
    

    public int calculateBossHp(int level) {
        Log.d(TAG, "Calculating boss HP for level: " + level);
        
        if (level <= 1) {
            Log.d(TAG, "Level " + level + " <= 1, returning 0 HP (no boss for level 1)");
            return 0;
        } else if (level == 2) {
            Log.d(TAG, "Level " + level + " = 2, returning 200 HP (first boss)");
            return 200;
        } else {
            int previousLevelHp = calculateBossHp(level - 1);
            int newHp = (int) Math.round(previousLevelHp * 2.5);
            Log.d(TAG, "Level " + level + ": Previous HP = " + previousLevelHp + ", New HP = " + newHp + " (" + previousLevelHp + " * 2.5)");
            return newHp;
        }
    }
    

    public int calculateCoinsForBoss(int bossLevel, String userId) {
        // Boss level 2 daje 200 novčića, svaki sledeći +20%
        int baseCoins = 200;
        int index = bossLevel - 1;
        if (index < 0) {
            index = 0;
        }
        double multiplier = Math.pow(1.20, index);
        int coins = (int) Math.round(baseCoins * multiplier);
        
        double bonusMultiplier = applyBowCoinBonus(userId);
        coins = (int) Math.round(coins * bonusMultiplier);
        
        Log.d(TAG, String.format("Boss level %d (index %d): %d coins (base: %d, multiplier: %.2f, bow bonus: %.2f)", 
                bossLevel, index, coins, baseCoins, multiplier, bonusMultiplier));
        return coins;
    }

    public int applyEquipmentBonuses(int basePp, String userId) {
        Log.d(TAG, "Applying equipment bonuses, base PP: " + basePp + ", currentUserId: " + userId);
        
        int totalBonus = 0;
        
        try {
            List<UserEquipment> userEquipment = equipmentRepository.getAllUserEquipmentForUser(userId);
            Log.d(TAG, "Found " + userEquipment.size() + " user equipment items for PP bonuses");
            
            List<com.example.dailyboss.domain.model.Equipment> allEquipment = equipmentRepository.getAllAvailableEquipment();
            
            for (UserEquipment userEq : userEquipment) {
                if (!userEq.isActive()) continue;
                
                Log.d(TAG, "Checking equipment for PP bonus: " + userEq.getEquipmentId() + ", active: " + userEq.isActive());
                
                for (com.example.dailyboss.domain.model.Equipment equipment : allEquipment) {
                    if (equipment.getId().equals(userEq.getEquipmentId())) {
                        Log.d(TAG, "Found equipment for PP bonus: " + equipment.getName() + " (ID: " + equipment.getId() + ")");
                        
                        switch (equipment.getId()) {
                            case "potion_20_pp":
                                int potion20Bonus = (int) Math.round(basePp * 0.20);
                                totalBonus += potion20Bonus;
                                Log.d(TAG, "Potion 20% bonus: +" + potion20Bonus + " PP (20% of " + basePp + ")");
                                break;
                            case "potion_40_pp":
                                int potion40Bonus = (int) Math.round(basePp * 0.40);
                                totalBonus += potion40Bonus;
                                Log.d(TAG, "Potion 40% bonus: +" + potion40Bonus + " PP (40% of " + basePp + ")");
                                break;
                            case "potion_60_pp":
                                int potion60Bonus = (int) Math.round(basePp * 0.60);
                                totalBonus += potion60Bonus;
                                Log.d(TAG, "Potion 60% bonus: +" + potion60Bonus + " PP (60% of " + basePp + ")");
                                break;
                            case "potion_80_pp":
                                int potion80Bonus = (int) Math.round(basePp * 0.80);
                                totalBonus += potion80Bonus;
                                Log.d(TAG, "Potion 80% bonus: +" + potion80Bonus + " PP (80% of " + basePp + ")");
                                break;
                            case "potion_5_permanent":
                                int permanent5Bonus = (int) Math.round(basePp * 0.05);
                                totalBonus += permanent5Bonus;
                                Log.d(TAG, "Permanent potion 5% bonus: +" + permanent5Bonus + " PP (5% of " + basePp + ")");
                                break;
                            case "potion_10_permanent":
                                int permanent10Bonus = (int) Math.round(basePp * 0.10);
                                totalBonus += permanent10Bonus;
                                Log.d(TAG, "Permanent potion 10% bonus: +" + permanent10Bonus + " PP (10% of " + basePp + ")");
                                break;
                            case "gloves":
                                int glovesBonus = (int) Math.round(basePp * 0.10);
                                totalBonus += glovesBonus;
                                Log.d(TAG, "Gloves bonus: +" + glovesBonus + " PP (10% of " + basePp + ")");
                                break;
                            case "shield":
                                Log.d(TAG, "Shield: no PP bonus");
                                break;
                            case "boots":
                                Log.d(TAG, "Boots: no PP bonus");
                                break;
                            case "sword":
                                double swordBonusPercent = userEq.getCurrentBonusValue();
                                int swordBonus = (int) Math.round(basePp * swordBonusPercent);
                                totalBonus += swordBonus;
                                Log.d(TAG, "Sword bonus: +" + swordBonus + " PP (" + String.format("%.2f", swordBonusPercent * 100) + "% of " + basePp + ")");
                                break;
                            case "bow":
                                Log.d(TAG, "Bow: no PP bonus");
                                break;
                            default:
                                Log.d(TAG, "Unknown equipment: " + equipment.getId());
                                break;
                        }
                        break;
                    }
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error applying equipment bonuses", e);
        }
        
        Log.d(TAG, "Applied equipment bonuses: +" + totalBonus + " PP");
        Log.d(TAG, "Base PP: " + basePp + ", Total bonus: " + totalBonus + ", Final PP: " + (basePp + totalBonus));
        return totalBonus;
    }

    public int applyHitChanceBonuses(int baseHitChance, String userId) {
        Log.d(TAG, "Applying hit chance bonuses, base: " + baseHitChance + "%, currentUserId: " + userId);
        
        int totalBonus = 0;
        
        try {
            List<UserEquipment> userEquipment = equipmentRepository.getAllUserEquipmentForUser(userId);
            Log.d(TAG, "Found " + userEquipment.size() + " user equipment items");
            
            List<com.example.dailyboss.domain.model.Equipment> allEquipment = equipmentRepository.getAllAvailableEquipment();
            
            for (UserEquipment userEq : userEquipment) {
                if (!userEq.isActive()) continue;
                
                Log.d(TAG, "Checking equipment: " + userEq.getEquipmentId() + ", active: " + userEq.isActive());
                
                for (com.example.dailyboss.domain.model.Equipment equipment : allEquipment) {
                    if (equipment.getId().equals(userEq.getEquipmentId())) {
                        Log.d(TAG, "Found equipment: " + equipment.getName() + " (ID: " + equipment.getId() + ")");
                        Log.d(TAG, "Equipment type: " + equipment.getType() + ", comparing with ARMOR");
                        
                        if ("shield".equals(equipment.getId())) {
                            totalBonus += 10;
                            Log.d(TAG, "Shield hit chance bonus: +10%");
                        }
                        break;
                    }
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error applying hit chance bonuses", e);
        }
        
        Log.d(TAG, "Applied hit chance bonuses: +" + totalBonus + "%");
        Log.d(TAG, "Hit chance after bonuses: " + baseHitChance + "% -> " + (baseHitChance + totalBonus) + "%");
        return totalBonus;
    }

    public int applyBootsBonus(int baseAttacks, String userId) {
        Log.d(TAG, "Applying boots bonus, base attacks: " + baseAttacks + ", currentUserId: " + userId);
        
        int bonusAttacks = 0;
        
        try {
            List<UserEquipment> userEquipment = equipmentRepository.getAllUserEquipmentForUser(userId);
            
            for (UserEquipment userEq : userEquipment) {
                if (!userEq.isActive()) continue;
                
                if ("boots".equals(userEq.getEquipmentId())) {
                    bonusAttacks = (int) Math.round(baseAttacks * 0.40);
                    Log.d(TAG, "Boots bonus: +" + bonusAttacks + " attacks (40% of " + baseAttacks + ")");
                    break;
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error applying boots bonus", e);
        }
        
        Log.d(TAG, "Applied boots bonus: +" + bonusAttacks + " attacks");
        Log.d(TAG, "Attacks after boots bonus: " + baseAttacks + " -> " + (baseAttacks + bonusAttacks));
        return bonusAttacks;
    }
    

    public double applyBowCoinBonus(String userId) {
        Log.d(TAG, "Applying bow coin bonus, currentUserId: " + userId);
        
        double coinMultiplier = 1.0;
        
        try {
            List<UserEquipment> userEquipment = equipmentRepository.getAllUserEquipmentForUser(userId);
            
            for (UserEquipment userEq : userEquipment) {
                if (!userEq.isActive()) continue;
                
                if ("bow".equals(userEq.getEquipmentId())) {
                    double bowBonusPercent = userEq.getCurrentBonusValue();
                    coinMultiplier += bowBonusPercent;
                    Log.d(TAG, "Bow coin bonus: +" + String.format("%.2f", bowBonusPercent * 100) + "% (" + String.format("%.4f", bowBonusPercent) + ")");
                    break;
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error applying bow coin bonus", e);
        }
        
        Log.d(TAG, "Applied bow coin bonus: " + String.format("%.2f", coinMultiplier) + "x multiplier");
        return coinMultiplier;
    }

    public BattleResult performAttack(BattleState state) {
        Log.d(TAG, "Performing attack on boss with " + state.getBossHp() + " HP");
        
        BattleResult result = new BattleResult();
        result.setSuccess(true);
        
        if (state.getBossHp() <= 0) {
            Log.d(TAG, "Boss already defeated, cannot attack");
            result.setSuccess(false);
            result.setMessage("Boss je već poražen!");
            return result;
        }
        
        if (state.getAttacksLeft() <= 0) {
            Log.d(TAG, "No attacks left");
            result.setSuccess(false);
            result.setMessage("Nema više napada!");
            return result;
        }
        
        int hitRoll = rng.nextInt(100);
        boolean hit = hitRoll < state.getHitChance();
        
        result.setHit(hit);
        result.setNewAttacksLeft(state.getAttacksLeft() - 1);
        
        if (hit) {
            // Napad je pogodio
            int damage = state.getUserPp();
            int newBossHp = Math.max(0, state.getBossHp() - damage);
            
            result.setNewBossHp(newBossHp);
            result.setMessage("Pogodak! " + damage + " damage!");
            
            Log.d(TAG, "Hit! Damage: " + damage + ", Boss HP: " + state.getBossHp() + " -> " + newBossHp);
            
            // Proveri da li je boss poražen
            if (newBossHp <= 0) {
                result.setBossDefeated(true);
                result.setMessage("Boss poražen!");
                Log.d(TAG, "Boss defeated!");
            }
        } else {
            // Napad je promašio
            result.setNewBossHp(state.getBossHp());
            result.setMessage("Promašaj!");
            Log.d(TAG, "Miss! Hit roll: " + hitRoll + " >= " + state.getHitChance());
        }
        
        // Proveri da li su napadi istrošeni
        if (result.getNewAttacksLeft() <= 0) {
            result.setAttacksExhausted(true);
            Log.d(TAG, "Attacks exhausted");
        }
        
        return result;
    }

    public EquipmentDropResult getEquipmentDrop() {
        Log.d(TAG, "Generating equipment drop");
        
        EquipmentDropResult result = new EquipmentDropResult();
        
        if (rng.nextDouble() < 0.5) {
            String equipmentType = rng.nextDouble() < 0.5 ? "CLOTHING" : "WEAPON";
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


    public String getRandomEquipmentId(String equipmentType) {
        Log.d(TAG, "Getting random equipment ID for type: " + equipmentType);
        
        String[] clothingIds = {"gloves", "shield", "boots"};
        String[] weaponIds = {"sword", "bow"};
        
        String[] ids = "CLOTHING".equals(equipmentType) ? clothingIds : weaponIds;
        String selectedId = ids[rng.nextInt(ids.length)];
        
        Log.d(TAG, "Selected equipment ID: " + selectedId);
        return selectedId;
    }
    

    public String getEquipmentNameById(String equipmentId) {
        switch (equipmentId) {
            case "gloves": return "Rukavice";
            case "shield": return "Štit";
            case "boots": return "Čizme";
            case "sword": return "Mač";
            case "bow": return "Luk i Strela";
            default: return "Nepoznata Oprema";
        }
    }

    public boolean addSpecificEquipment(String equipmentId, String userId) {
        Log.d(TAG, "Adding specific equipment ID: " + equipmentId + " to user: " + userId);
        
        try {
            if (equipmentId == null) {
                Log.e(TAG, "Equipment ID is null");
                return false;
            }
            
            if (equipmentId.equals("sword") || equipmentId.equals("bow")) {
                UserEquipment existingWeapon = equipmentRepository.getUserSpecificEquipment(userId, equipmentId);
                
                if (existingWeapon != null) {
                    Log.d(TAG, "User already has " + equipmentId + ", increasing effect instead of adding duplicate");
                    
                    double currentBonus = existingWeapon.getCurrentBonusValue();
                    double newBonus = currentBonus + 0.0002; // +0.02%
                    existingWeapon.setCurrentBonusValue(newBonus);
                    
                    equipmentRepository.updateUserEquipment(existingWeapon);
                    
                    Log.d(TAG, "Increased " + equipmentId + " effect from " + (currentBonus * 100) + "% to " + (newBonus * 100) + "%");
                    return true;
                } else {
                    Log.d(TAG, "User doesn't have " + equipmentId + ", adding new weapon");
                    return addNewWeapon(equipmentId, userId);
                }
            } else {
                return addNewEquipment(equipmentId, userId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error adding equipment: " + e.getMessage(), e);
            return false;
        }
    }

    public int calculateSuccessRateForCurrentStage(String userId, int currentLevel) {
        // Retrieve UserProfile to get the lastActiveTimestamp
        UserProfile userProfile = userProfileRepository.getByUserId(userId);
        long lastLevelUpTimestamp = 0; // Default to 0 if not found or first level

        if (userProfile != null) {
            lastLevelUpTimestamp = userProfile.getLastActiveTimestamp();
            Log.d(TAG, "User Profile found. Last level up timestamp: " + lastLevelUpTimestamp);
        } else {
            Log.w(TAG, "User Profile not found for userId: " + userId + ". Using default lastLevelUpTimestamp.");
        }


        List<TaskInstance> allUserTasks = taskInstanceRepository.getAllByUserId(userId);

        int totalTasksConsidered = 0;
        int successfulTasks = 0;

        for (TaskInstance task : allUserTasks) {
            // Only consider tasks created/started AFTER the last level up
            // Assuming task.getCreationTimestamp() or similar field exists
            // If you don't have a creation timestamp on TaskInstance, this logic won't work as-is.
            // You might need to add task.getCreationTimestamp() or task.getCompletionTimestamp()
            // For now, let's assume TaskInstance has a getCreationTimestamp() or getLastModifiedTimestamp()
            // We will use getLastModifiedTimestamp() as a proxy for "started" if creation isn't available
            // IMPORTANT: You need to define how "stage" is truly measured by task timestamps.
            // For demonstration, let's assume tasks relevant to the current stage are those
            // created *after* the last level-up timestamp.

            // Example: If TaskInstance had getCreationTimestamp()
            // if (task.getCreationTimestamp() >= lastLevelUpTimestamp) {

            // For now, let's assume all tasks are considered unless specified.
            // If `lastActiveTimestamp` truly means 'last level up', then you need to filter tasks
            // that occurred *since* that timestamp. Your `TaskInstance` model needs a timestamp
            // that indicates when it was relevant to the current "stage."
            // Assuming `TaskInstance` has a `getCreationTimestamp()` or `getLastModifiedTimestamp()`:
            if (task.getInstanceDate() >= lastLevelUpTimestamp) { // *** IMPORTANT: Replace with actual timestamp field in TaskInstance ***
                if (task.getStatus() != TaskStatus.PAUSED && task.getStatus() != TaskStatus.CANCELLED) {
                    totalTasksConsidered++;
                    if (task.getStatus() == TaskStatus.DONE) {
                        successfulTasks++;
                    }
                }
            } else {
                Log.d(TAG, "Task " + task.getInstanceId() + " ignored (too old for current stage). Task timestamp: " + task.getInstanceDate());
            }
        }

        if (totalTasksConsidered == 0) {
            // If no tasks were considered for the current stage, return a high default success rate
            // as per your existing logic.
            return 95;
        }

        int successRate = (int) ((double) successfulTasks / totalTasksConsidered * 100);
        Log.d(TAG, "calculateSuccessRateForCurrentStage: totalTasksConsidered=" + totalTasksConsidered +
                ", successfulTasks=" + successfulTasks + ", calculatedSuccessRate=" + successRate);
        return Math.max(0, Math.min(100, successRate));
    }

    private boolean addNewWeapon(String equipmentId, String userId) {
        Log.d(TAG, "Adding new weapon: " + equipmentId + " to user: " + userId);
        
        UserEquipment userEquipment = new UserEquipment(
            java.util.UUID.randomUUID().toString(),
            userId,
            equipmentId,
            1,
            false,
            0,
            System.currentTimeMillis(),
            0.05
        );
        
        boolean success = equipmentRepository.addUserEquipment(userEquipment);
        if (success) {
            Log.d(TAG, "Successfully added new weapon ID: " + equipmentId + " to user: " + userId);
        } else {
            Log.e(TAG, "Failed to add new weapon ID: " + equipmentId + " to user: " + userId);
        }
        return success;
    }
    

    private boolean addNewEquipment(String equipmentId, String userId) {
        Log.d(TAG, "Adding new equipment: " + equipmentId + " to user: " + userId);
        
        UserEquipment userEquipment = new UserEquipment(
            java.util.UUID.randomUUID().toString(),
            userId,
            equipmentId,
            1,
            false,
            0,
            System.currentTimeMillis(),
            0.0 // currentBonusValue = 0.0
        );
        
        boolean success = equipmentRepository.addUserEquipment(userEquipment);
        if (success) {
            Log.d(TAG, "Successfully added equipment ID: " + equipmentId + " to user: " + userId);
        } else {
            Log.e(TAG, "Failed to add equipment ID: " + equipmentId + " to user: " + userId);
        }
        return success;
    }
    

    public void decrementEquipmentDuration(String userId) {
        Log.d(TAG, "Decrementing equipment duration after battle for user: " + userId);
        
        try {
            List<UserEquipment> userEquipment = equipmentRepository.getAllUserEquipmentForUser(userId);
            
            for (UserEquipment userEq : userEquipment) {
                if (userEq.isActive()) {
                    // Pronađi opremu da vidimo tip
                    List<com.example.dailyboss.domain.model.Equipment> allEquipment = equipmentRepository.getAllAvailableEquipment();
                    for (com.example.dailyboss.domain.model.Equipment equipment : allEquipment) {
                        if (equipment.getId().equals(userEq.getEquipmentId())) {
                            
                            // Smanji trajanje na osnovu durationBattles vrednosti
                            int currentDuration = userEq.getRemainingDurationBattles();
                            Log.d(TAG, "Equipment " + equipment.getName() + " (ID: " + userEq.getId() + ") current duration: " + currentDuration + " (should be " + equipment.getDurationBattles() + ")");
                            
                            // Ako je trajanje 0, oprema traje zauvek (oružje, trajni napici)
                            if (currentDuration == 0) {
                                Log.d(TAG, "Equipment " + equipment.getName() + " (ID: " + userEq.getId() + ") is permanent, no duration decrease");
                                break;
                            }
                            
                            // Smanji trajanje za 1
                            int newDuration = currentDuration - 1;
                            Log.d(TAG, "Equipment " + equipment.getName() + " (ID: " + userEq.getId() + ") duration: " + currentDuration + " -> " + newDuration);
                            
                            if (newDuration <= 0) {
                                // Obriši opremu potpuno
                                Log.d(TAG, "Equipment " + equipment.getName() + " (ID: " + userEq.getId() + ") expired, deleting...");
                                equipmentRepository.deleteUserEquipment(userEq.getId(), new EquipmentRepository.UserEquipmentOperationListener() {
                                    @Override
                                    public void onSuccess(UserEquipment userEquipment) {
                                        Log.d(TAG, "Equipment expired and DELETED: " + equipment.getName() + " (ID: " + userEq.getId() + ")");
                                    }
                                    
                                    @Override
                                    public void onSuccessList(List<UserEquipment> userEquipmentList) {
                                        // Ne koristi se
                                    }
                                    
                                    @Override
                                    public void onFailure(String errorMessage) {
                                        Log.e(TAG, "Failed to delete expired equipment: " + errorMessage);
                                    }
                                });
                            } else {
                                // Ažuriraj trajanje
                                userEq.setRemainingDurationBattles(newDuration);
                                equipmentRepository.updateUserEquipment(userEq);
                                Log.d(TAG, "Equipment duration updated: " + equipment.getName() + " (ID: " + userEq.getId() + ") remaining: " + newDuration);
                            }
                            
                            break;
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error decrementing equipment duration: " + e.getMessage(), e);
        }
    }
}
