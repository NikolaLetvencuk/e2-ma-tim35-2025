package com.example.dailyboss.presentation.fragments;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.dailyboss.R;
import com.example.dailyboss.data.repository.EquipmentRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Locale;
import java.util.Random;

public class BattleFragment extends Fragment implements SensorEventListener {

    private ImageView ivBoss, ivEquipment, ivChest;
    private ProgressBar progressBossHp, progressUserPp;
    private TextView tvAttacksLeft, tvHitChance, tvCoinsWon, tvEquipmentWon;
    private TextView tvBossHpValue, tvUserPpValue;
    private Button btnAttack;
    private View resultPanel;

    private int bossMaxHp = 200; // Ovo će se ažurirati na osnovu nivoa
    private int bossHp;
    private int userPp = 50; // Ovo će se ažurirati iz UserStatistic
    private int attacksLeft = 5;
    private int bossIndex = 1; // Ovo će se ažurirati na osnovu nivoa
    private int successPercent = 95; // 95% hit chance (može se povećati sa opremom)
    private boolean chestOpened = false; // Flag za sprečavanje ponovnog otvaranja kovčega
    private String selectedEquipmentId = null; // Globalna varijabla za odabranu opremu
    private String selectedEquipmentName = null; // Globalna varijabla za naziv opreme

    private Random rng = new Random();
    
    // Dodajemo potrebne servise
    private com.example.dailyboss.data.repository.UserStatisticRepository userStatisticRepository;
    private com.example.dailyboss.data.repository.EquipmentRepository equipmentRepository;
    private String currentUserId;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private long lastShakeTime = 0;
    private static final int SHAKE_WAIT_MS = 500;
    private static final float SHAKE_THRESHOLD = 12f;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_battle, container, false);

        ivBoss = view.findViewById(R.id.ivBoss);
        ivEquipment = view.findViewById(R.id.ivEquipment);
        ivChest = view.findViewById(R.id.ivChest);
        progressBossHp = view.findViewById(R.id.progressBossHp);
        progressUserPp = view.findViewById(R.id.progressUserPp);
        tvAttacksLeft = view.findViewById(R.id.tvAttacksLeft);
        tvHitChance = view.findViewById(R.id.tvHitChance);
        tvBossHpValue = view.findViewById(R.id.tvBossHpValue);
        tvUserPpValue = view.findViewById(R.id.tvUserPpValue);
        btnAttack = view.findViewById(R.id.btnAttack);
        resultPanel = view.findViewById(R.id.resultPanel);
        tvCoinsWon = view.findViewById(R.id.tvCoinsWon);
        tvEquipmentWon = view.findViewById(R.id.tvEquipmentWon);

        bossHp = bossMaxHp; // Ovo će se ažurirati u loadUserData()
        progressBossHp.setMax(bossMaxHp); // Ovo će se ažurirati u loadUserData()
        progressBossHp.setProgress(bossHp);
        progressUserPp.setMax(1000);
        progressUserPp.setProgress(userPp);

        // Pozovi loadUserData() da ažurira boss HP na osnovu nivoa
        loadUserData();

        btnAttack.setOnClickListener(v -> performAttack());

        // Inicijalizuj servise
        userStatisticRepository = new com.example.dailyboss.data.repository.UserStatisticRepository(getContext());
        equipmentRepository = new com.example.dailyboss.data.repository.EquipmentRepository(getContext());
        
        // Dobij trenutnog korisnika
        com.example.dailyboss.data.SharedPreferencesHelper prefs = new com.example.dailyboss.data.SharedPreferencesHelper(getContext());
        currentUserId = prefs.getLoggedInUserId();
        android.util.Log.d("BattleFragment", "onCreateView: currentUserId = " + currentUserId);
        
        if (currentUserId == null) {
            android.util.Log.e("BattleFragment", "currentUserId is null in onCreateView, trying to get from arguments");
            if (getArguments() != null) {
                currentUserId = getArguments().getString("userId");
                android.util.Log.d("BattleFragment", "Got currentUserId from arguments: " + currentUserId);
            }
        }
        
        // Učitaj podatke korisnika
        loadUserData();

        sensorManager = (SensorManager) requireContext().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        return view;
    }

    /**
     * Učitava podatke korisnika iz baze
     */
    private void loadUserData() {
        android.util.Log.d("BattleFragment", "loadUserData: currentUserId = " + currentUserId);
        
        if (currentUserId == null) {
            android.util.Log.e("BattleFragment", "currentUserId is null in loadUserData");
            return;
        }
        
        // Učitaj UserStatistic
        com.example.dailyboss.domain.model.UserStatistic stats = userStatisticRepository.getUserStatistic(currentUserId);
        android.util.Log.d("BattleFragment", "Retrieved UserStatistic for userId: " + currentUserId + ", stats: " + (stats != null ? "found" : "null"));
        
        if (stats != null) {
            android.util.Log.d("BattleFragment", "UserStatistic details: level=" + stats.getLevel() + ", XP=" + stats.getTotalXPPoints() + ", PP=" + stats.getPowerPoints());
            userPp = stats.getPowerPoints();
            android.util.Log.d("BattleFragment", "Loaded base PP from UserStatistic: " + userPp);
            
            // Primijeni equipment bonuse
            int oldPp = userPp;
            userPp = applyEquipmentBonuses(userPp);
            android.util.Log.d("BattleFragment", "PP after equipment bonuses: " + oldPp + " -> " + userPp);
            
            // Primijeni hit chance bonuse
            int oldHitChance = successPercent;
            successPercent = applyHitChanceBonuses(95);
            android.util.Log.d("BattleFragment", "Hit chance after bonuses: " + oldHitChance + "% -> " + successPercent + "%");
            
            // Primijeni čizme bonus za broj napada
            int oldAttacks = attacksLeft;
            attacksLeft = applyBootsBonus(5);
            android.util.Log.d("BattleFragment", "Attacks after boots bonus: " + oldAttacks + " -> " + attacksLeft);
            
            android.util.Log.d("BattleFragment", "Loaded user PP: " + userPp + " (with equipment bonuses)");
            android.util.Log.d("BattleFragment", "Hit chance: " + successPercent + "% (with equipment bonuses)");
            android.util.Log.d("BattleFragment", "Attacks left: " + attacksLeft + " (with equipment bonuses)");
            
            // Ažuriraj PP bar
            progressUserPp.setMax(Math.max(1000, userPp * 2)); // Dinamički max
            progressUserPp.setProgress(userPp);
            
            // Ažuriraj boss HP na osnovu nivoa
            bossIndex = stats.getLevel();
            android.util.Log.d("BattleFragment", "User level from stats: " + bossIndex + ", XP: " + stats.getTotalXPPoints());
            bossMaxHp = calculateBossHp(bossIndex);
            bossHp = bossMaxHp; // Resetuj boss HP
            android.util.Log.d("BattleFragment", "Final boss HP calculation: Level " + bossIndex + " = " + bossMaxHp + " HP");
            
            // Ažuriraj boss HP bar
            progressBossHp.setMax(bossMaxHp);
            progressBossHp.setProgress(bossHp);
            
            // Resetuj chest flag za novu borbu
            chestOpened = false;
            
            // Resetuj equipment varijable za novu borbu
            selectedEquipmentId = null;
            selectedEquipmentName = null;
            
            // Debug log za HP kalkulaciju
            android.util.Log.d("BattleFragment", "Boss HP calculation: Level " + bossIndex + " = " + bossMaxHp + " HP");
            
            // Ažuriraj UI nakon svih kalkulacija
            updateUi();
        } else {
            android.util.Log.e("BattleFragment", "UserStatistic not found for userId: " + currentUserId);
        }
    }
    
    /**
     * Primijeni čizme bonus na broj napada
     */
    private int applyBootsBonus(int baseAttacks) {
        try {
            // Učitaj aktivnu opremu korisnika
            List<com.example.dailyboss.domain.model.UserEquipment> userEquipment = equipmentRepository.getAllUserEquipmentForUser(currentUserId);
            
            int totalBonus = 0;
            
            for (com.example.dailyboss.domain.model.UserEquipment userEq : userEquipment) {
                if (userEq.isActive()) {
                    // Pronađi opremu
                    List<com.example.dailyboss.domain.model.Equipment> allEquipment = equipmentRepository.getAllAvailableEquipment();
                    for (com.example.dailyboss.domain.model.Equipment equipment : allEquipment) {
                        if (equipment.getId().equals(userEq.getEquipmentId())) {
                            // Dodaj čizme bonus za broj napada
                            if (equipment.getId().equals("boots")) {
                                totalBonus += 2; // Čizme daju +40% napada (2 od 5 = 7 napada)
                            }
                            break;
                        }
                    }
                }
            }
            
            android.util.Log.d("BattleFragment", "Applied boots bonus: +" + totalBonus + " attacks");
            return baseAttacks + totalBonus;
            
        } catch (Exception e) {
            android.util.Log.e("BattleFragment", "Error applying boots bonus: " + e.getMessage());
            return baseAttacks;
        }
    }

    /**
     * Primijeni hit chance bonuse na osnovu aktivne opreme
     */
    private int applyHitChanceBonuses(int baseHitChance) {
        try {
            android.util.Log.d("BattleFragment", "Applying hit chance bonuses, base: " + baseHitChance + "%, currentUserId: " + currentUserId);
            
            // Učitaj aktivnu opremu korisnika
            List<com.example.dailyboss.domain.model.UserEquipment> userEquipment = equipmentRepository.getAllUserEquipmentForUser(currentUserId);
            android.util.Log.d("BattleFragment", "Found " + userEquipment.size() + " user equipment items");
            
            int totalBonus = 0;
            
            for (com.example.dailyboss.domain.model.UserEquipment userEq : userEquipment) {
                android.util.Log.d("BattleFragment", "Checking equipment: " + userEq.getEquipmentId() + ", active: " + userEq.isActive());
                
                if (userEq.isActive()) {
                    // Pronađi opremu
                    List<com.example.dailyboss.domain.model.Equipment> allEquipment = equipmentRepository.getAllAvailableEquipment();
                    for (com.example.dailyboss.domain.model.Equipment equipment : allEquipment) {
                        if (equipment.getId().equals(userEq.getEquipmentId())) {
                            android.util.Log.d("BattleFragment", "Found equipment: " + equipment.getName() + " (ID: " + equipment.getId() + ")");
                            
                            // Dodaj hit chance bonus na osnovu tipa opreme
                            android.util.Log.d("BattleFragment", "Equipment type: " + equipment.getType() + ", comparing with ARMOR");
                            
                            if (equipment.getType().equals("ARMOR")) {
                                if (equipment.getId().equals("shield")) {
                                    totalBonus += 10; // Štit daje +10% hit chance (95% -> 100%)
                                    android.util.Log.d("BattleFragment", "Shield hit chance bonus applied: +10%");
                                }
                                // Rukavice i čizme ne daju hit chance bonus
                            }
                            break;
                        }
                    }
                }
            }
            
            int finalHitChance = Math.min(100, baseHitChance + totalBonus); // Maksimalno 100%
            android.util.Log.d("BattleFragment", "Applied hit chance bonuses: +" + totalBonus + "%");
            return finalHitChance;
            
        } catch (Exception e) {
            android.util.Log.e("BattleFragment", "Error applying hit chance bonuses: " + e.getMessage());
            return baseHitChance;
        }
    }
    
    /**
     * Primijeni equipment bonuse na PP
     */
    private int applyEquipmentBonuses(int basePp) {
        try {
            android.util.Log.d("BattleFragment", "Applying equipment bonuses, base PP: " + basePp + ", currentUserId: " + currentUserId);
            
            // Učitaj aktivnu opremu korisnika
            List<com.example.dailyboss.domain.model.UserEquipment> userEquipment = equipmentRepository.getAllUserEquipmentForUser(currentUserId);
            android.util.Log.d("BattleFragment", "Found " + userEquipment.size() + " user equipment items for PP bonuses");
            
            int totalBonus = 0;
            
            for (com.example.dailyboss.domain.model.UserEquipment userEq : userEquipment) {
                android.util.Log.d("BattleFragment", "Checking equipment for PP bonus: " + userEq.getEquipmentId() + ", active: " + userEq.isActive());
                
                if (userEq.isActive()) {
                    // Pronađi opremu
                    List<com.example.dailyboss.domain.model.Equipment> allEquipment = equipmentRepository.getAllAvailableEquipment();
                    for (com.example.dailyboss.domain.model.Equipment equipment : allEquipment) {
                        if (equipment.getId().equals(userEq.getEquipmentId())) {
                            android.util.Log.d("BattleFragment", "Found equipment for PP bonus: " + equipment.getName() + " (ID: " + equipment.getId() + ")");
                            
                            // Dodaj specifične bonuse na osnovu ID-a opreme
                            switch (equipment.getId()) {
                                case "potion_20_pp":
                                    totalBonus += 4; // Potion 20% daje +4 PP
                                    android.util.Log.d("BattleFragment", "Potion 20% bonus: +4 PP");
                                    break;
                                case "potion_40_pp":
                                    totalBonus += 8; // Potion 40% daje +8 PP
                                    android.util.Log.d("BattleFragment", "Potion 40% bonus: +8 PP");
                                    break;
                                case "potion_60_pp":
                                    totalBonus += 12; // Potion 60% daje +12 PP
                                    android.util.Log.d("BattleFragment", "Potion 60% bonus: +12 PP");
                                    break;
                                case "potion_80_pp":
                                    totalBonus += 16; // Potion 80% daje +16 PP
                                    android.util.Log.d("BattleFragment", "Potion 80% bonus: +16 PP");
                                    break;
                                case "potion_5_permanent":
                                    totalBonus += 2; // Trajni napitak 5% daje +2 PP
                                    android.util.Log.d("BattleFragment", "Permanent potion 5% bonus: +2 PP");
                                    break;
                                case "potion_10_permanent":
                                    totalBonus += 4; // Trajni napitak 10% daje +4 PP
                                    android.util.Log.d("BattleFragment", "Permanent potion 10% bonus: +4 PP");
                                    break;
                                case "gloves":
                                    // Rukavice daju +10% od trenutnih PP
                                    int glovesBonus = (int) Math.round(basePp * 0.10);
                                    totalBonus += glovesBonus;
                                    android.util.Log.d("BattleFragment", "Gloves bonus: +" + glovesBonus + " PP (10% of " + basePp + ")");
                                    break;
                                case "shield":
                                    // Štit ne daje PP bonus, samo hit chance
                                    android.util.Log.d("BattleFragment", "Shield: no PP bonus");
                                    break;
                                case "boots":
                                    // Čizme ne daju PP bonus, samo broj napada
                                    android.util.Log.d("BattleFragment", "Boots: no PP bonus");
                                    break;
                                case "sword":
                                    // Mač daje bonus na osnovu currentBonusValue (počinje sa 5%, može se povećati)
                                    double swordBonusPercent = userEq.getCurrentBonusValue();
                                    int swordBonus = (int) Math.round(basePp * swordBonusPercent);
                                    totalBonus += swordBonus;
                                    android.util.Log.d("BattleFragment", "Sword bonus: +" + swordBonus + " PP (" + String.format("%.2f", swordBonusPercent * 100) + "% of " + basePp + ")");
                                    break;
                                case "bow":
                                    // Luk i strela ne daje PP bonus, samo coin bonus
                                    android.util.Log.d("BattleFragment", "Bow: no PP bonus");
                                    break;
                            }
                            break;
                        }
                    }
                }
            }
            
            android.util.Log.d("BattleFragment", "Applied equipment bonuses: +" + totalBonus + " PP");
            android.util.Log.d("BattleFragment", "Base PP: " + basePp + ", Total bonus: " + totalBonus + ", Final PP: " + (basePp + totalBonus));
            return basePp + totalBonus;
            
        } catch (Exception e) {
            android.util.Log.e("BattleFragment", "Error applying equipment bonuses: " + e.getMessage());
            return basePp;
        }
    }
    
    /**
     * Kalkuliše boss HP na osnovu nivoa
     * Formula: HP prethodnog bosa * 2 + HP prethodnog bosa / 2
     */
    private int calculateBossHp(int level) {
        android.util.Log.d("BattleFragment", "Calculating boss HP for level: " + level);
        
        if (level <= 2) {
            android.util.Log.d("BattleFragment", "Level " + level + " <= 2, returning 200 HP");
            return 200; // Prvi bos ima 200 HP
        }
        
        int previousBossHp = calculateBossHp(level - 1);
        // Formula: HP prethodnog bosa * 2 + HP prethodnog bosa / 2
        int newBossHp = previousBossHp * 2 + previousBossHp / 2;
        android.util.Log.d("BattleFragment", "Level " + level + ": Previous HP = " + previousBossHp + ", New HP = " + newBossHp + " (" + previousBossHp + " * 2 + " + previousBossHp + "/2)");
        return newBossHp;
    }

    private void updateUi() {
        tvAttacksLeft.setText(String.format(Locale.getDefault(), "Attacks: %d/5", attacksLeft));
        tvHitChance.setText(String.format(Locale.getDefault(), "Hit chance: %d%%", successPercent));
        progressBossHp.setProgress(Math.max(0, bossHp));
        progressUserPp.setProgress(userPp);
        
        // Ažuriraj HP i PP tekstove
        tvBossHpValue.setText(String.format(Locale.getDefault(), "%d/%d", bossHp, bossMaxHp));
        tvUserPpValue.setText(String.valueOf(userPp));
    }

    private void performAttack() {
        if (attacksLeft <= 0) {
            Toast.makeText(getContext(), "No attacks left", Toast.LENGTH_SHORT).show();
            return;
        }

        // Proveri da li boss već ima 0 HP
        if (bossHp <= 0) {
            Toast.makeText(getContext(), "Boss is already defeated!", Toast.LENGTH_SHORT).show();
            return;
        }

        attacksLeft--;
        boolean hit = rng.nextInt(100) < successPercent;
        if (hit) {
            bossHp -= userPp;
            showBossHitAnimation();
            Toast.makeText(getContext(), "Hit! -" + userPp + " HP", Toast.LENGTH_SHORT).show();
        } else {
            showMissAnimation();
            Toast.makeText(getContext(), "Miss!", Toast.LENGTH_SHORT).show();
        }

        if (bossHp < 0) bossHp = 0;
        updateUi();

        if (bossHp == 0) {
            onBossDefeated();
        } else if (attacksLeft == 0) {
            onAttacksExhausted();
        }
    }

    private void showBossHitAnimation() {
        ivBoss.setImageResource(R.drawable.boss_hit);
        ScaleAnimation anim = new ScaleAnimation(
                1f, 0.95f, 1f, 0.95f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setDuration(120);
        anim.setRepeatCount(1);
        anim.setRepeatMode(Animation.REVERSE);
        ivBoss.startAnimation(anim);

        new Handler().postDelayed(() -> ivBoss.setImageResource(R.drawable.boss_idle), 250);
    }

    private void showMissAnimation() {
        Vibrator v = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null) {
            if (android.os.Build.VERSION.SDK_INT >= 26) {
                v.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                v.vibrate(80);
            }
        }
    }

    private void onBossDefeated() {
        int coins = calculateCoinsForBoss(bossIndex - 1); // Koristi prethodni level za nagradu
        // Uvek dropuj equipment - 50/50 između Clothing i Weapon
        boolean equipmentDropped = true;
        String equipmentType = (rng.nextInt(100) < 50) ? "Clothing" : "Weapon"; // 50/50 šansa
        
        // Odaberi konkretnu opremu i sačuvaj globalno
        selectedEquipmentId = getRandomEquipmentId(equipmentType);
        selectedEquipmentName = getEquipmentNameById(selectedEquipmentId);
        
        android.util.Log.d("BattleFragment", "Boss defeated! Equipment dropped: " + equipmentDropped + ", Type: " + equipmentType + ", ID: " + selectedEquipmentId + ", Name: " + selectedEquipmentName);

        showResult(coins, equipmentType);
        
        // Smanji trajanje opreme nakon završetka borbe
        decrementEquipmentDuration();
    }

    private void onAttacksExhausted() {
        int initialBossHp = bossMaxHp;
        int damageDone = initialBossHp - bossHp;
        boolean halfReduced = damageDone >= (initialBossHp / 2.0);

        // Uvek daj 50% novčića kada se istroše napadi
        int coins = (int) Math.ceil(calculateCoinsForBoss(bossIndex - 1) / 2.0); // Koristi prethodni level za nagradu
        String equipmentType = null;

        if (halfReduced) {
            // Uvek dropuj equipment ako je boss na pola HP - 50/50 između Clothing i Weapon
            equipmentType = (rng.nextInt(100) < 50) ? "Clothing" : "Weapon"; // 50/50 šansa
            
            // Odaberi konkretnu opremu i sačuvaj globalno
            selectedEquipmentId = getRandomEquipmentId(equipmentType);
            selectedEquipmentName = getEquipmentNameById(selectedEquipmentId);
        }
        
        android.util.Log.d("BattleFragment", "Attacks exhausted! Half reduced: " + halfReduced + ", Equipment dropped: " + (equipmentType != null) + ", Type: " + equipmentType);

        showResult(coins, equipmentType);
        
        // Smanji trajanje opreme nakon završetka borbe
        decrementEquipmentDuration();
    }

    /**
     * Smanji trajanje opreme nakon borbe i obriši ako je istekla
     */
    private void decrementEquipmentDuration() {
        android.util.Log.d("BattleFragment", "Decrementing equipment duration after battle");
        try {
            List<com.example.dailyboss.domain.model.UserEquipment> userEquipment = equipmentRepository.getAllUserEquipmentForUser(currentUserId);
            
            for (com.example.dailyboss.domain.model.UserEquipment userEq : userEquipment) {
                if (userEq.isActive()) {
                    // Pronađi opremu da vidimo tip
                    List<com.example.dailyboss.domain.model.Equipment> allEquipment = equipmentRepository.getAllAvailableEquipment();
                    for (com.example.dailyboss.domain.model.Equipment equipment : allEquipment) {
                        if (equipment.getId().equals(userEq.getEquipmentId())) {
                            
                            // Smanji trajanje na osnovu durationBattles vrednosti
                            int currentDuration = userEq.getRemainingDurationBattles();
                            android.util.Log.d("BattleFragment", "Equipment " + equipment.getName() + " (ID: " + userEq.getId() + ") current duration: " + currentDuration + " (should be " + equipment.getDurationBattles() + ")");
                            
                            // Ako je trajanje 0, oprema traje zauvek (oružje, trajni napici)
                            if (currentDuration == 0) {
                                android.util.Log.d("BattleFragment", "Equipment " + equipment.getName() + " (ID: " + userEq.getId() + ") is permanent, no duration decrease");
                                break;
                            }
                            
                            // Smanji trajanje za 1
                            int newDuration = currentDuration - 1;
                            android.util.Log.d("BattleFragment", "Equipment " + equipment.getName() + " (ID: " + userEq.getId() + ") duration: " + currentDuration + " -> " + newDuration);
                            
                            if (newDuration <= 0) {
                                // Obriši opremu potpuno
                                android.util.Log.d("BattleFragment", "Equipment " + equipment.getName() + " (ID: " + userEq.getId() + ") expired, deleting...");
                                equipmentRepository.deleteUserEquipment(userEq.getId(), new EquipmentRepository.UserEquipmentOperationListener() {
                                    @Override
                                    public void onSuccess(com.example.dailyboss.domain.model.UserEquipment userEquipment) {
                                        android.util.Log.d("BattleFragment", "Equipment expired and DELETED: " + equipment.getName() + " (ID: " + userEq.getId() + ")");
                                    }
                                    
                                    @Override
                                    public void onSuccessList(List<com.example.dailyboss.domain.model.UserEquipment> userEquipmentList) {
                                        // Ne koristi se
                                    }
                                    
                                    @Override
                                    public void onFailure(String errorMessage) {
                                        android.util.Log.e("BattleFragment", "Failed to delete expired equipment: " + errorMessage);
                                    }
                                });
                            } else {
                                // Ažuriraj trajanje
                                userEq.setRemainingDurationBattles(newDuration);
                                equipmentRepository.updateUserEquipment(userEq);
                                android.util.Log.d("BattleFragment", "Equipment duration updated: " + equipment.getName() + " (ID: " + userEq.getId() + ") remaining: " + newDuration);
                            }
                            
                            break;
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            android.util.Log.e("BattleFragment", "Error decrementing equipment duration: " + e.getMessage());
        }
    }

    /**
     * Primijeni luk i strela bonus na novčiće (+5%)
     */
    private double applyBowCoinBonus() {
        try {
            // Učitaj aktivnu opremu korisnika
            List<com.example.dailyboss.domain.model.UserEquipment> userEquipment = equipmentRepository.getAllUserEquipmentForUser(currentUserId);
            
            double bonusMultiplier = 1.0; // Osnovni multiplikator
            
            for (com.example.dailyboss.domain.model.UserEquipment userEq : userEquipment) {
                if (userEq.isActive()) {
                    // Pronađi opremu
                    List<com.example.dailyboss.domain.model.Equipment> allEquipment = equipmentRepository.getAllAvailableEquipment();
                    for (com.example.dailyboss.domain.model.Equipment equipment : allEquipment) {
                        if (equipment.getId().equals(userEq.getEquipmentId())) {
                            // Dodaj luk i strela bonus za novčiće na osnovu currentBonusValue
                            if (equipment.getId().equals("bow")) {
                                double bowBonusPercent = userEq.getCurrentBonusValue();
                                bonusMultiplier += bowBonusPercent; // Luk i strela bonus na osnovu currentBonusValue
                                android.util.Log.d("BattleFragment", "Applied bow coin bonus: +" + String.format("%.2f", bowBonusPercent * 100) + "%");
                            }
                            break;
                        }
                    }
                }
            }
            
            return bonusMultiplier;
            
        } catch (Exception e) {
            android.util.Log.e("BattleFragment", "Error applying bow coin bonus: " + e.getMessage());
            return 1.0;
        }
    }

    private int calculateCoinsForBoss(int index) {
        double coins = 200.0;
        for (int i = 1; i < index; i++) {
            coins *= 1.2;
        }
        
        // Primijeni luk i strela bonus (+5% novca)
        double bonusMultiplier = applyBowCoinBonus();
        coins *= bonusMultiplier;
        
        return (int) Math.round(coins);
    }

    private void showResult(int coins, String equipmentType) {
        android.util.Log.d("BattleFragment", "showResult called with coins: " + coins + ", equipmentType: " + equipmentType);
        
        resultPanel.setVisibility(View.VISIBLE);
        tvCoinsWon.setText("Coins: " + coins);
        
        // Koristi globalnu varijablu umesto random generisanja
        String equipmentName = selectedEquipmentName != null ? selectedEquipmentName : "-";
        tvEquipmentWon.setText("Equipment: " + equipmentName);
        
        android.util.Log.d("BattleFragment", "Equipment name displayed: " + equipmentName);

        ivChest.setImageResource(R.drawable.chest_closed);
        ScaleAnimation shake = new ScaleAnimation(
                1f, 1.05f, 1f, 0.95f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        shake.setDuration(120);
        shake.setRepeatCount(3);
        shake.setRepeatMode(Animation.REVERSE);
        ivChest.startAnimation(shake);

        ivChest.setOnClickListener(v -> openChest(coins, equipmentType));
    }
    
    /**
     * Vraća konkretan naziv opreme na osnovu tipa
     */
    private String getEquipmentName(String equipmentType) {
        if (equipmentType == null) {
            return "-";
        }
        
        if (equipmentType.equals("Clothing")) {
            // Random izaberi jednu od 3 armor opreme
            String[] armorTypes = {"Rukavice", "Štit", "Čizme"};
            return armorTypes[rng.nextInt(armorTypes.length)];
        } else if (equipmentType.equals("Weapon")) {
            // Random izaberi jednu od 2 weapon opreme
            String[] weaponTypes = {"Mač", "Luk i Strela"};
            return weaponTypes[rng.nextInt(weaponTypes.length)];
        }
        
        return "-";
    }

    private void openChest(int coins, String equipmentType) {
        android.util.Log.d("BattleFragment", "openChest called with coins: " + coins + ", equipmentType: " + equipmentType);
        
        // Sprečava ponovno otvaranje kovčega
        if (chestOpened) {
            android.util.Log.d("BattleFragment", "Chest already opened, ignoring click");
            return;
        }
        
        chestOpened = true;
        ivChest.setImageResource(R.drawable.chest_open);
        
        // Ukloni click listener da spreči ponovno otvaranje
        ivChest.setOnClickListener(null);
        
        // ✅ DODAJI COINS U USERSTATISTIC
        if (currentUserId != null) {
            com.example.dailyboss.domain.model.UserStatistic stats = userStatisticRepository.getUserStatistic(currentUserId);
            if (stats != null) {
                int oldCoins = stats.getCoins();
                stats.setCoins(oldCoins + coins);
                userStatisticRepository.saveOrUpdate(stats);
                android.util.Log.d("BattleFragment", "Added " + coins + " coins. Old: " + oldCoins + ", New: " + stats.getCoins());
            } else {
                android.util.Log.e("BattleFragment", "UserStatistic not found when adding coins");
            }
        } else {
            android.util.Log.e("BattleFragment", "currentUserId is null when adding coins");
        }
        
        // ✅ DODAJI EQUIPMENT AKO JE DROPPED
        if (equipmentType != null && currentUserId != null) {
            android.util.Log.d("BattleFragment", "Attempting to add equipment ID: " + selectedEquipmentId);
            addSpecificEquipment(selectedEquipmentId);
        } else {
            android.util.Log.d("BattleFragment", "No equipment to add. equipmentType: " + equipmentType + ", currentUserId: " + currentUserId);
        }
        
        // Kreiraj poruku sa konkretnim nazivom opreme
        String equipmentMessage = "";
        if (equipmentType != null && selectedEquipmentName != null) {
            equipmentMessage = " and " + selectedEquipmentName;
        }
        
        Toast.makeText(getContext(), "You won " + coins + " coins" + equipmentMessage, Toast.LENGTH_LONG).show();
    }
    
    /**
     * Dodaje specifičnu opremu korisniku nakon borbe
     */
    private void addSpecificEquipment(String equipmentId) {
        try {
            android.util.Log.d("BattleFragment", "Adding specific equipment ID: " + equipmentId);
            
            if (equipmentId != null) {
                // Proveri da li je ovo oružje (sword ili bow)
                if (equipmentId.equals("sword") || equipmentId.equals("bow")) {
                    // Proveri da li korisnik već ima ovo oružje
                    com.example.dailyboss.domain.model.UserEquipment existingWeapon = equipmentRepository.getUserSpecificEquipment(currentUserId, equipmentId);
                    
                    if (existingWeapon != null) {
                        // Korisnik već ima ovo oružje - povećaj efekat umesto dodavanja duplikata
                        android.util.Log.d("BattleFragment", "User already has " + equipmentId + ", increasing effect instead of adding duplicate");
                        
                        // Povećaj bonusValue za 0.02% (0.0002)
                        double currentBonus = existingWeapon.getCurrentBonusValue();
                        double newBonus = currentBonus + 0.0002; // +0.02%
                        existingWeapon.setCurrentBonusValue(newBonus);
                        
                        // Ažuriraj u bazi
                        equipmentRepository.updateUserEquipment(existingWeapon);
                        
                        android.util.Log.d("BattleFragment", "Increased " + equipmentId + " effect from " + (currentBonus * 100) + "% to " + (newBonus * 100) + "%");
                        
                        // Prikaži poruku korisniku
                        String weaponName = getEquipmentNameById(equipmentId);
                        android.widget.Toast.makeText(getContext(), 
                            weaponName + " efekat povećan sa " + String.format("%.2f", currentBonus * 100) + "% na " + String.format("%.2f", newBonus * 100) + "%", 
                            android.widget.Toast.LENGTH_LONG).show();
                    } else {
                        // Korisnik nema ovo oružje - dodaj novo
                        android.util.Log.d("BattleFragment", "User doesn't have " + equipmentId + ", adding new weapon");
                        addNewWeapon(equipmentId);
                    }
                } else {
                    // Nije oružje - dodaj normalno
                    addNewEquipment(equipmentId);
                }
            } else {
                android.util.Log.e("BattleFragment", "Equipment ID is null");
            }
        } catch (Exception e) {
            android.util.Log.e("BattleFragment", "Error adding equipment: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void addNewWeapon(String equipmentId) {
        // Dodaj novo oružje sa osnovnim efektom
        com.example.dailyboss.domain.model.UserEquipment userEquipment = new com.example.dailyboss.domain.model.UserEquipment(
            java.util.UUID.randomUUID().toString(),
            currentUserId,
            equipmentId,
            1, // quantity = 1
            false, // Neaktivna
            0, // Nema trajanje za oružje
            System.currentTimeMillis(),
            0.05 // Osnovni efekat: 5% = 0.05
        );
        
        boolean success = equipmentRepository.addUserEquipment(userEquipment);
        if (success) {
            android.util.Log.d("BattleFragment", "Successfully added new weapon ID: " + equipmentId + " to user: " + currentUserId);
        } else {
            android.util.Log.e("BattleFragment", "Failed to add new weapon ID: " + equipmentId + " to user: " + currentUserId);
        }
    }
    
    private void addNewEquipment(String equipmentId) {
        // Dodaj novu opremu (nije oružje)
        com.example.dailyboss.domain.model.UserEquipment userEquipment = new com.example.dailyboss.domain.model.UserEquipment(
            java.util.UUID.randomUUID().toString(),
            currentUserId,
            equipmentId,
            1, // quantity = 1
            false, // Neaktivna
            0, // Nema trajanje za opremu dobijenu u borbi
            System.currentTimeMillis(),
            0.0 // currentBonusValue = 0.0
        );
        
        boolean success = equipmentRepository.addUserEquipment(userEquipment);
        if (success) {
            android.util.Log.d("BattleFragment", "Successfully added equipment ID: " + equipmentId + " to user: " + currentUserId);
        } else {
            android.util.Log.e("BattleFragment", "Failed to add equipment ID: " + equipmentId + " to user: " + currentUserId);
        }
    }
    
    /**
     * Vraća random equipment ID na osnovu tipa
     */
    private String getRandomEquipmentId(String equipmentType) {
        if (equipmentType.equals("Clothing")) {
            // Random izaberi jednu od 3 armor opreme
            String[] armorIds = {"gloves", "shield", "boots"};
            return armorIds[rng.nextInt(armorIds.length)];
        } else if (equipmentType.equals("Weapon")) {
            // Random izaberi jednu od 2 weapon opreme
            String[] weaponIds = {"sword", "bow"};
            return weaponIds[rng.nextInt(weaponIds.length)];
        }
        
        return null;
    }
    
    /**
     * Vraća naziv opreme na osnovu ID-a
     */
    private String getEquipmentNameById(String equipmentId) {
        if (equipmentId == null) {
            return "-";
        }
        
        switch (equipmentId) {
            case "gloves":
                return "Rukavice";
            case "shield":
                return "Štit";
            case "boots":
                return "Čizme";
            case "sword":
                return "Mač";
            case "bow":
                return "Luk i Strela";
            default:
                return "Nepoznata Oprema";
        }
    }
    

    @Override
    public void onResume() {
        super.onResume();
        if (accelerometer != null && sensorManager != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (accelerometer != null && sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) return;

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        float gX = x / SensorManager.GRAVITY_EARTH;
        float gY = y / SensorManager.GRAVITY_EARTH;
        float gZ = z / SensorManager.GRAVITY_EARTH;

        float gForce = (float) Math.sqrt(gX * gX + gY * gY + gZ * gZ);

        if (gForce > SHAKE_THRESHOLD) {
            final long now = System.currentTimeMillis();
            if (lastShakeTime + SHAKE_WAIT_MS > now) {
                return;
            }
            lastShakeTime = now;

            if (attacksLeft > 0) {
                performAttack();
            } else if (resultPanel.getVisibility() == View.VISIBLE) {
                openChest(
                        Integer.parseInt(tvCoinsWon.getText().toString().replaceAll("\\D+", "")),
                        tvEquipmentWon.getText().toString().replace("Equipment: ", "").equals("-") ? null : tvEquipmentWon.getText().toString().replace("Equipment: ", "")
                );
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
