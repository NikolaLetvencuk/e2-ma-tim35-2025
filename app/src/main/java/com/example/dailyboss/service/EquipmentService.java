package com.example.dailyboss.service;

import android.content.Context;
import android.util.Log;

import com.example.dailyboss.data.dao.UserProfileDao;
import com.example.dailyboss.data.repository.EquipmentRepository;
import com.example.dailyboss.data.repository.UserStatisticRepository;
import com.example.dailyboss.domain.enums.EquipmentBonusType;
import com.example.dailyboss.domain.enums.EquipmentType;
import com.example.dailyboss.domain.model.Equipment;
import com.example.dailyboss.domain.model.UserEquipment;
import com.example.dailyboss.domain.model.UserProfile;
import com.example.dailyboss.domain.model.UserStatistic;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Servis za upravljanje opremom korisnika
 */
public class EquipmentService {
    
    private static final String TAG = "EquipmentService";
    
    private final EquipmentRepository equipmentRepository;
    private final UserStatisticRepository userStatisticRepository;
    private final UserProfileDao userProfileRepository;
    private final Context context;
    
    public EquipmentService(Context context) {
        this.context = context.getApplicationContext();
        this.equipmentRepository = new EquipmentRepository(context);
        this.userStatisticRepository = new UserStatisticRepository(context);
        this.userProfileRepository = new UserProfileDao(context);
    }
    
    /**
     * Inicijalizuj osnovnu opremu u bazi (pozovi jednom pri prvom pokretanju)
     */
    public void initializeDefaultEquipment() {
        List<Equipment> defaultEquipment = createDefaultEquipment();
        
        for (Equipment equipment : defaultEquipment) {
            equipmentRepository.addOrUpdateEquipment(equipment, new EquipmentRepository.EquipmentOperationListener() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "Default equipment added: " + equipment.getName());
                }
                
                @Override
                public void onFailure(String errorMessage) {
                    Log.e(TAG, "Failed to add default equipment: " + errorMessage);
                }
            });
        }
    }
    
    /**
     * Kreiraj osnovnu opremu prema specifikaciji
     */
    public List<Equipment> createDefaultEquipment() {
        List<Equipment> equipment = new ArrayList<>();
        
        // NAPITCI - cene su u procentima od nagrade za prethodni nivo
        equipment.add(new Equipment(
                "potion_20_pp", "Napitak Snage (20%)", 
                "Jednokratno povećanje snage za 20%", 
                "ic_potion1", EquipmentType.POTION.getCode(), 
                EquipmentBonusType.POWER_POINTS.getCode(), 0.20, 
                1, 0, 50, true, false  // 50% od nagrade
        ));
        
        equipment.add(new Equipment(
                "potion_40_pp", "Napitak Snage (40%)", 
                "Jednokratno povećanje snage za 40%", 
                "ic_potion2", EquipmentType.POTION.getCode(), 
                EquipmentBonusType.POWER_POINTS.getCode(), 0.40, 
                1, 0, 70, true, false  // 70% od nagrade
        ));
        
        equipment.add(new Equipment(
                "potion_5_permanent", "Trajni Napitak Snage (5%)", 
                "Trajno povećanje snage za 5%", 
                "ic_potion3", EquipmentType.POTION.getCode(), 
                EquipmentBonusType.POWER_POINTS.getCode(), 0.05, 
                0, 0, 200, false, true  // 200% od nagrade
        ));
        
        equipment.add(new Equipment(
                "potion_10_permanent", "Trajni Napitak Snage (10%)", 
                "Trajno povećanje snage za 10%", 
                "ic_potion4", EquipmentType.POTION.getCode(), 
                EquipmentBonusType.POWER_POINTS.getCode(), 0.10, 
                0, 0, 1000, false, true  // 1000% od nagrade
        ));
        
        // ODEĆA
        equipment.add(new Equipment(
                "gloves", "Rukavice", 
                "Povećanje snage za 10% (traje 2 borbe)", 
                "ic_gloves", EquipmentType.ARMOR.getCode(), 
                EquipmentBonusType.POWER_POINTS.getCode(), 0.10, 
                2, 0, 60, false, true  // 60% od nagrade
        ));
        
        equipment.add(new Equipment(
                "shield", "Štit", 
                "Povećanje šanse uspešnog napada za 10% (traje 2 borbe)", 
                "ic_shield", EquipmentType.ARMOR.getCode(), 
                EquipmentBonusType.ATTACK_CHANCE.getCode(), 0.10, 
                2, 0, 60, false, true  // 60% od nagrade
        ));
        
        equipment.add(new Equipment(
                "boots", "Čizme", 
                "Šansa povećanja broja napada za 40% (traje 2 borbe)", 
                "ic_boots", EquipmentType.ARMOR.getCode(), 
                EquipmentBonusType.ATTACK_COUNT.getCode(), 0.40, 
                2, 0, 80, false, true  // 80% od nagrade
        ));
        
        // ORUŽJE
        equipment.add(new Equipment(
                "sword", "Mač", 
                "Trajno povećanje snage za 5%", 
                "ic_sword", EquipmentType.WEAPON.getCode(), 
                EquipmentBonusType.POWER_POINTS.getCode(), 0.05, 
                0, 0, 0, false, true
        ));
        
        equipment.add(new Equipment(
                "bow", "Luk i Strela", 
                "Stalno povećanje procenta dobijenog novca za 5%", 
                "ic_sword", EquipmentType.WEAPON.getCode(), 
                EquipmentBonusType.COIN_BONUS.getCode(), 0.05, 
                0, 0, 0, false, true
        ));
        
        return equipment;
    }
    
    /**
     * Vraća trenutnu cenu opreme za korisnika (za prikaz u shop-u)
     */
    public int getEquipmentPrice(String equipmentId, String userId) {
        UserStatistic stats = userStatisticRepository.getUserStatistic(userId);
        if (stats == null) {
            return 100; // Default cena
        }
        
        int userLevel = stats.getLevel();
        return calculateEquipmentPrice(equipmentId, userLevel);
    }

    /**
     * Kalkuliše cenu opreme na osnovu nagrade od prethodnog boss-a
     */
    private int calculateEquipmentPrice(String equipmentId, int userLevel) {
        // Kalkuliši nagradu od prethodnog boss-a (level - 1)
        int previousBossReward = calculateCoinsForBoss(userLevel - 1);
        
        switch (equipmentId) {
            case "potion_20_pp":
                return (int) Math.round(previousBossReward * 0.50); // 50% od nagrade
            case "potion_40_pp":
                return (int) Math.round(previousBossReward * 0.70); // 70% od nagrade
            case "potion_5_permanent":
                return (int) Math.round(previousBossReward * 2.00); // 200% od nagrade
            case "potion_10_permanent":
                return (int) Math.round(previousBossReward * 10.00); // 1000% od nagrade
            case "gloves":
                return (int) Math.round(previousBossReward * 0.60); // 60% od nagrade
            case "shield":
                return (int) Math.round(previousBossReward * 0.60); // 60% od nagrade
            case "boots":
                return (int) Math.round(previousBossReward * 0.80); // 80% od nagrade
            case "sword":
            case "bow":
                return 0; // Oružje se dobija samo nakon borbe
            default:
                return 100; // Default cena
        }
    }
    
    /**
     * Kalkuliše nagradu od boss-a na osnovu nivoa (kopija iz BattleFragment)
     */
    private int calculateCoinsForBoss(int level) {
        if (level <= 0) return 200; // Prvi boss
        
        double coins = 200.0;
        for (int i = 1; i < level; i++) {
            coins *= 1.2; // +20% za svaki sledeći boss
        }
        return (int) Math.round(coins);
    }

    /**
     * Kupi opremu za korisnika
     */
    public void buyEquipment(String userId, String equipmentId, EquipmentOperationListener listener) {
        // Uzmi statistiku korisnika da proverim novčiće
        UserStatistic stats = userStatisticRepository.getUserStatistic(userId);
        UserProfile userProfile = userProfileRepository.getByUserId(userId);

        if (stats == null) {
            listener.onFailure("Korisnik nije pronađen");
            return;
        }
        
        // Uzmi opremu da vidim cenu
        Equipment equipment = getEquipmentById(equipmentId);
        if (equipment == null) {
            listener.onFailure("Oprema nije pronađena");
            return;
        }
        
        // Kalkuliši dinamičku cenu na osnovu nivoa korisnika
        int userLevel = stats.getLevel();
        int equipmentPrice = calculateEquipmentPrice(equipmentId, userLevel);
        
        Log.d(TAG, "Equipment: " + equipment.getName() + ", User Level: " + userLevel + ", Price: " + equipmentPrice);
        
        Log.d(TAG, "Equipment found: " + equipment.getName() + " (ID: " + equipmentId + "), durationBattles: " + equipment.getDurationBattles());
        
        // Koristi dinamičku cenu
        int price = equipmentPrice;
        
        if (stats.getCoins() < price) {
            listener.onFailure("Nemaš dovoljno novčića. Potrebno: " + price + ", imaš: " + stats.getCoins());
            return;
        }
        
        // Proveri da li korisnik već ima ovu opremu
        UserEquipment existingEquipment = equipmentRepository.getUserSpecificEquipment(userId, equipmentId);
        
        if (existingEquipment != null) {
            // Ako već ima opremu, povećaj količinu
            existingEquipment.setQuantity(existingEquipment.getQuantity() + 1);
            equipmentRepository.updateUserEquipment(existingEquipment);
            Log.d(TAG, "Ažurirana postojeća oprema: " + equipment.getName() + " za korisnika: " + userId);
        } else {
            // Kreiraj novu opremu za korisnika
            int durationBattles = equipment.getDurationBattles();
            Log.d(TAG, "Creating UserEquipment with durationBattles: " + durationBattles + " for equipment: " + equipment.getName());
            
            UserEquipment userEquipment = new UserEquipment(
                    UUID.randomUUID().toString(),
                    userId,
                    equipmentId,
                    1, // quantity
                    false, // not active yet
                    durationBattles, // duration
                    System.currentTimeMillis(),
                    equipment.getBonusValue()
            );
            
            Log.d(TAG, "UserEquipment created with remainingDurationBattles: " + userEquipment.getRemainingDurationBattles());
            
            boolean saved = equipmentRepository.addUserEquipment(userEquipment);
            userProfileRepository.update(userProfile);
            Log.d(TAG, "Kreirana nova oprema: " + equipment.getName() + " za korisnika: " + userId + ", sačuvano: " + saved);
        }
        
        // Oduzmi novčiće
        stats.setCoins(stats.getCoins() - price);
        userStatisticRepository.saveOrUpdate(stats);
        
        listener.onSuccess("Uspešno kupljena oprema: " + equipment.getName() + " za " + price + " novčića");
    }
    
    /**
     * Uzmi opremu po ID-u (koristi hardkodovane podatke za sada)
     */
    private Equipment getEquipmentById(String equipmentId) {
        List<Equipment> allEquipment = createDefaultEquipment();
        for (Equipment equipment : allEquipment) {
            if (equipment.getId().equals(equipmentId)) {
                return equipment;
            }
        }
        return null;
    }
    
    /**
     * Kalkuliši cenu opreme na osnovu nivoa korisnika
     */
    private int calculatePrice(Equipment equipment, int userLevel) {
        // Za sada koristim osnovnu cenu, kasnije možemo dodati logiku na osnovu nivoa
        return equipment.getBasePriceCoins();
    }
    
    /**
     * Aktiviraj opremu
     */
    public void activateEquipment(String userId, String equipmentId, EquipmentOperationListener listener) {
        // TODO: Implementiraj aktivaciju opreme
        listener.onSuccess("Oprema aktivirana");
    }
    
    /**
     * Uzmi aktivnu opremu korisnika
     */
    public List<UserEquipment> getActiveEquipment(String userId) {
        // TODO: Implementiraj dohvatanje aktivne opreme
        return new ArrayList<>();
    }
    
    /**
     * Uzmi sve opreme korisnika
     */
    public List<UserEquipment> getUserEquipment(String userId) {
        // TODO: Implementiraj dohvatanje opreme korisnika
        return new ArrayList<>();
    }
    
    public interface EquipmentOperationListener {
        void onSuccess(String message);
        void onFailure(String errorMessage);
    }

    // Upgrade oružja - povećava currentBonusValue za 0.01 (1%)
    public boolean upgradeWeapon(String userId, String equipmentId) {
        try {
            // Proveri da li korisnik ima dovoljno novčića
            UserStatistic stats = userStatisticRepository.getUserStatistic(userId);
            if (stats == null) {
                android.util.Log.e(TAG, "User statistics not found for upgrade");
                return false;
            }

            // Kalkuliši cenu upgrade-a (60% od prethodnog boss-a)
            int userLevel = stats.getLevel();
            int previousBossReward = calculateCoinsForBoss(userLevel - 1);
            int upgradeCost = (int) Math.round(previousBossReward * 0.60);

            if (stats.getCoins() < upgradeCost) {
                android.util.Log.e(TAG, "Not enough coins for upgrade. Required: " + upgradeCost + ", Available: " + stats.getCoins());
                return false;
            }

            // Pronađi oružje korisnika
            UserEquipment userWeapon = equipmentRepository.getUserSpecificEquipment(userId, equipmentId);
            if (userWeapon == null) {
                android.util.Log.e(TAG, "Weapon not found for upgrade: " + equipmentId);
                return false;
            }

            // Povećaj currentBonusValue za 0.0001 (0.01%)
            double currentBonus = userWeapon.getCurrentBonusValue();
            double newBonus = currentBonus + 0.0001; // +0.01%
            userWeapon.setCurrentBonusValue(newBonus);

            // Ažuriraj oružje u bazi
            equipmentRepository.updateUserEquipment(userWeapon);

            // Oduzmi novčiće
            stats.setCoins(stats.getCoins() - upgradeCost);
            userStatisticRepository.saveOrUpdate(stats);

            android.util.Log.d(TAG, "Weapon upgraded: " + equipmentId + " from " + String.format("%.2f", currentBonus * 100) + "% to " + String.format("%.2f", newBonus * 100) + "%");
            android.util.Log.d(TAG, "Upgrade cost: " + upgradeCost + " coins, remaining: " + stats.getCoins());

            return true;
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error upgrading weapon: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Vraća cenu upgrade-a za oružje
    public int getUpgradeCost(String userId) {
        UserStatistic stats = userStatisticRepository.getUserStatistic(userId);
        if (stats == null) {
            return 0;
        }

        int userLevel = stats.getLevel();
        int previousBossReward = calculateCoinsForBoss(userLevel - 1);
        return (int) Math.round(previousBossReward * 0.60);
    }
}
