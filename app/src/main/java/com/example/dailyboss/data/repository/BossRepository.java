package com.example.dailyboss.data.repository;

import android.content.Context;
import android.util.Log;

import com.example.dailyboss.data.dao.BattleDao;
import com.example.dailyboss.data.dao.UserStatisticDao;
import com.example.dailyboss.domain.model.BattleResult;
import com.example.dailyboss.domain.model.BossData;
import com.example.dailyboss.domain.model.EquipmentDropResult;
import com.example.dailyboss.domain.model.UserStatistic;
import com.example.dailyboss.domain.model.UserEquipment;

import java.util.List;
import java.util.UUID;

/**
 * Repository za upravljanje boss podacima i battle rezultatima
 */
public class BossRepository {
    
    private static final String TAG = "BossRepository";
    
    private final Context context;
    private final BattleDao battleDao;
    private final UserStatisticDao userStatisticDao;
    private final EquipmentRepository equipmentRepository;
    
    public BossRepository(Context context) {
        this.context = context.getApplicationContext();
        this.battleDao = new BattleDao(context);
        this.userStatisticDao = new UserStatisticDao(context);
        this.equipmentRepository = new EquipmentRepository(context);
    }
    
    /**
     * Vraća boss podatke za određeni nivo
     */
    public BossData getBossData(int level) {
        Log.d(TAG, "Getting boss data for level: " + level);
        
        // Za sada koristimo hardkodovane podatke, kasnije možemo dodati u bazu
        BossData bossData = battleDao.getBossDataByLevel(level);
        
        if (bossData == null) {
            // Kreiraj default boss podatke
            bossData = createDefaultBossData(level);
            battleDao.insertBossData(bossData);
        }
        
        Log.d(TAG, "Boss data: " + bossData);
        return bossData;
    }
    
    /**
     * Vraća maksimalni HP boss-a za određeni nivo
     */
    public int getBossMaxHp(int level) {
        BossData bossData = getBossData(level);
        return bossData.getMaxHp();
    }
    
    /**
     * Vraća naziv boss-a za određeni nivo
     */
    public String getBossName(int level) {
        BossData bossData = getBossData(level);
        return bossData.getName();
    }
    
    /**
     * Čuva rezultat borbe
     */
    public void saveBattleResult(String userId, BattleResult result) {
        Log.d(TAG, "Saving battle result for user: " + userId + ", result: " + result);
        
        try {
            // Dodaj novčiće korisniku
            if (result.getCoinsWon() > 0) {
                addCoinsToUser(userId, result.getCoinsWon());
            }
            
            // Dodaj opremu korisniku
            if (result.getEquipmentId() != null && !result.getEquipmentId().isEmpty()) {
                addEquipmentToUser(userId, result.getEquipmentId());
            }
            
            // Sačuvaj battle history
            battleDao.insertBattleHistory(createBattleHistory(userId, result));
            
            Log.d(TAG, "Battle result saved successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "Error saving battle result", e);
        }
    }
    
    /**
     * Procesira equipment drop
     */
    public EquipmentDropResult processEquipmentDrop(String userId, int bossIndex) {
        Log.d(TAG, "Processing equipment drop for user: " + userId + ", boss index: " + bossIndex);
        
        EquipmentDropResult result = new EquipmentDropResult();
        
        try {
            // Generiši equipment drop
            result = equipmentRepository.getEquipmentDrop();
            
            if (result.isDropped()) {
                // Dodaj opremu korisniku
                addEquipmentToUser(userId, result.getEquipmentId());
                Log.d(TAG, "Equipment added to user: " + result.getEquipmentName());
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error processing equipment drop", e);
            result.setDropped(false);
            result.setMessage("Greška pri dobijanju opreme");
        }
        
        return result;
    }
    
    /**
     * Dodaje opremu korisniku
     */
    public void addEquipmentToUser(String userId, String equipmentId) {
        Log.d(TAG, "Adding equipment to user: " + userId + ", equipment: " + equipmentId);
        
        try {
            // Kreiraj UserEquipment objekat
            UserEquipment userEquipment = new UserEquipment(
                    UUID.randomUUID().toString(),
                    userId,
                    equipmentId,
                    1, // quantity
                    false, // isActive - nije aktivna po defaultu
                    0, // remainingDurationBattles
                    0, // activationTimestamp
                    0.0 // currentBonusValue
            );
            
            boolean success = equipmentRepository.addUserEquipment(userEquipment);
            
            if (success) {
                Log.d(TAG, "Equipment added successfully: " + equipmentId);
            } else {
                Log.e(TAG, "Failed to add equipment: " + equipmentId);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error adding equipment to user", e);
        }
    }
    
    /**
     * Dodaje novčiće korisniku
     */
    public void addCoinsToUser(String userId, int coins) {
        Log.d(TAG, "Adding coins to user: " + userId + ", coins: " + coins);
        
        try {
            UserStatistic stats = userStatisticDao.getUserStatistic(userId);
            if (stats != null) {
                stats.setCoins(stats.getCoins() + coins);
                userStatisticDao.upsert(stats);
                Log.d(TAG, "Coins added successfully. New total: " + stats.getCoins());
            } else {
                Log.e(TAG, "User statistics not found for userId: " + userId);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error adding coins to user", e);
        }
    }
    
    /**
     * Vraća broj novčića korisnika
     */
    public int getUserCoins(String userId) {
        Log.d(TAG, "Getting user coins for: " + userId);
        
        try {
            UserStatistic stats = userStatisticDao.getUserStatistic(userId);
            if (stats != null) {
                Log.d(TAG, "User coins: " + stats.getCoins());
                return stats.getCoins();
            } else {
                Log.e(TAG, "User statistics not found for userId: " + userId);
                return 0;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error getting user coins", e);
            return 0;
        }
    }
    
    /**
     * Kreira default boss podatke za određeni nivo
     */
    private BossData createDefaultBossData(int level) {
        Log.d(TAG, "Creating default boss data for level: " + level);
        
        String name = "Boss Level " + level;
        int maxHp = calculateDefaultBossHp(level);
        String imagePath = "boss_level_" + level;
        
        return new BossData(level, name, maxHp, imagePath);
    }
    
    /**
     * Kalkuliše default boss HP za određeni nivo
     */
    private int calculateDefaultBossHp(int level) {
        if (level <= 2) {
            return 200;
        } else if (level <= 5) {
            return 200 + (level - 2) * 100;
        } else if (level <= 10) {
            return 500 + (level - 5) * 200;
        } else {
            return 1500 + (level - 10) * 500;
        }
    }
    
    /**
     * Kreira BattleHistory objekat
     */
    private com.example.dailyboss.domain.model.BattleHistory createBattleHistory(String userId, BattleResult result) {
        com.example.dailyboss.domain.model.BattleHistory history = new com.example.dailyboss.domain.model.BattleHistory();
        history.setUserId(userId);
        history.setBossLevel(result.getNewBossHp() > 0 ? 1 : 0); // Placeholder
        history.setBossDefeated(result.isBossDefeated());
        history.setCoinsWon(result.getCoinsWon());
        history.setEquipmentWon(result.getEquipmentName());
        history.setAttacksUsed(5 - result.getNewAttacksLeft()); // Placeholder
        history.setBattleDate(System.currentTimeMillis());
        return history;
    }
}
