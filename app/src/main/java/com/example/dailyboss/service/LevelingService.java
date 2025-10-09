package com.example.dailyboss.service;

import android.content.Context;
import android.util.Log;

import com.example.dailyboss.data.dao.UserProfileDao;
import com.example.dailyboss.data.repository.UserProfileRepository;
import com.example.dailyboss.data.repository.UserStatisticRepository;
import com.example.dailyboss.domain.enums.TaskDifficulty;
import com.example.dailyboss.domain.enums.TaskImportance;
import com.example.dailyboss.domain.model.UserProfile;
import com.example.dailyboss.domain.model.UserStatistic;

/**
 * Servis za upravljanje sistemom napredovanja kroz nivoe.
 * Implementira formule za računanje XP, PP, i titula.
 */
public class LevelingService {

    private static final String TAG = "LevelingService";
    
    // Početne vrednosti
    private static final int FIRST_LEVEL_XP_REQUIRED = 200;
    private static final int FIRST_LEVEL_PP_REWARD = 40;
    
    // Titule za svaki nivo
    private static final String[] LEVEL_TITLES = {
            "???",              // Level 0 (ne koristi se)
            "Novajlija",        // Level 1 (0 XP - početni nivo)
            "Šegrt",            // Level 2 (200 XP)
            "Zanatlija",        // Level 3
            "Majstor",          // Level 4
            "Ekspert",          // Level 5
            "Veteran",          // Level 6
            "Šampion",          // Level 7
            "Heroj",            // Level 8
            "Legenda",          // Level 9
            "Mit"               // Level 10+
    };

    private final UserStatisticRepository userStatisticRepository;
    private final UserProfileDao userProfileRepository;
    private final Context context;

    public LevelingService(Context context) {
        this.context = context.getApplicationContext();
        this.userStatisticRepository = new UserStatisticRepository(context);
        this.userProfileRepository = new UserProfileDao(context);
    }

    /**
     * Kalkulator modela - sadrži sve podatke o napredovanju
     */
    public static class LevelInfo {
        public int currentLevel;
        public int currentXP;
        public int xpRequiredForNextLevel;
        public int xpRequiredForCurrentLevel;
        public int currentPP;
        public int currentCoins;
        public String title;
        public float progressPercentage;

        public LevelInfo(int currentLevel, int currentXP, int xpRequiredForNextLevel, 
                        int xpRequiredForCurrentLevel, int currentPP, int currentCoins, String title) {
            this.currentLevel = currentLevel;
            this.currentXP = currentXP;
            this.xpRequiredForNextLevel = xpRequiredForNextLevel;
            this.xpRequiredForCurrentLevel = xpRequiredForCurrentLevel;
            this.currentPP = currentPP;
            this.currentCoins = currentCoins;
            this.title = title;
            
            // Izračunaj procenat progresa u trenutnom nivou
            int xpInCurrentLevel = currentXP - xpRequiredForCurrentLevel;
            int xpNeededInLevel = xpRequiredForNextLevel - xpRequiredForCurrentLevel;
            this.progressPercentage = xpNeededInLevel > 0 ? 
                    (float) xpInCurrentLevel / xpNeededInLevel * 100f : 0f;
        }
    }

    /**
     * Kalkuliše XP potreban za dostizanje određenog nivoa.
     * Level 1 (Novajlija): 0 XP (početni nivo)
     * Level 2 (Šegrt): 200 XP
     * Svaki sledeći: (XP prethodnog * 2 + XP prethodnog / 2), zaokruženo na prvu narednu stotinu
     */
    public static int calculateXPRequiredForLevel(int level) {
        if (level <= 1) return 0;  // Level 1 je početni nivo sa 0 XP
        if (level == 2) return FIRST_LEVEL_XP_REQUIRED;  // Level 2 treba 200 XP

        int previousLevelXP = calculateXPRequiredForLevel(level - 1);
        int calculatedXP = previousLevelXP * 2 + previousLevelXP / 2;
        
        // Zaokruži na prvu narednu stotinu
        return (int) (Math.ceil(calculatedXP / 100.0) * 100);
    }

    /**
     * Kalkuliše PP nagradu za dostizanje određenog nivoa.
     * Level 1 (Novajlija): 0 PP (početni nivo, nema nagrade)
     * Level 2 (Šegrt): 40 PP (prva nagrada)
     * Svaki sledeći: PP prethodnog + 3/4 * PP prethodnog
     */
    public static int calculatePPRewardForLevel(int level) {
        if (level <= 1) return 0;  // Level 1 je početni, nema PP nagradu
        if (level == 2) return FIRST_LEVEL_PP_REWARD;  // Level 2 dobija prvu nagradu

        int previousPP = calculatePPRewardForLevel(level - 1);
        return (int) Math.round(previousPP + (3.0 / 4.0) * previousPP);
    }

    /**
     * Vraća titulu za određeni nivo.
     */
    public static String getTitleForLevel(int level) {
        if (level < 1) level = 1;  // Minimum je level 1
        if (level < LEVEL_TITLES.length) {
            return LEVEL_TITLES[level];
        }
        // Za nivoe iznad predefinisanih
        return LEVEL_TITLES[LEVEL_TITLES.length - 1] + " " + level;
    }

    /**
     * Kalkuliše XP za težinu zadatka prema nivou korisnika.
     * XP bitnosti za prethodni nivo + XP bitnosti za prethodni nivo / 2 (zaokruženo)
     */
    public static int calculateDifficultyXP(TaskDifficulty difficulty, int userLevel) {
        int baseXP = difficulty.getXpValue();
        
        // Za nivo 0 i 1, koristi baznu vrednost
        if (userLevel <= 1) {
            return baseXP;
        }

        // Rekurzivno računanje za prethodne nivoe
        int previousLevelXP = calculateDifficultyXP(difficulty, userLevel - 1);
        return (int) Math.round(previousLevelXP + previousLevelXP / 2.0);
    }

    /**
     * Kalkuliše XP za važnost zadatka prema nivou korisnika.
     */
    public static int calculateImportanceXP(TaskImportance importance, int userLevel) {
        int baseXP = importance.getXpValue();
        
        if (userLevel <= 1) {
            return baseXP;
        }

        int previousLevelXP = calculateImportanceXP(importance, userLevel - 1);
        return (int) Math.round(previousLevelXP + previousLevelXP / 2.0);
    }

    /**
     * Kalkuliše ukupan XP za zadatak (težina + važnost).
     */
    public static int calculateTotalTaskXP(TaskDifficulty difficulty, TaskImportance importance, int userLevel) {
        int difficultyXP = calculateDifficultyXP(difficulty, userLevel);
        int importanceXP = calculateImportanceXP(importance, userLevel);
        return difficultyXP + importanceXP;
    }

    /**
     * Dodaje XP korisniku i proverava da li je postigao novi nivo.
     * @return true ako je korisnik napredovao u nivou
     */
    public boolean addExperiencePoints(String userId, int xpToAdd) {
        Log.d(TAG, "=== addExperiencePoints START ===");
        Log.d(TAG, "userId: " + userId + ", xpToAdd: " + xpToAdd);
        
        UserStatistic stats = userStatisticRepository.getUserStatistic(userId);
        if (stats == null) {
            Log.e(TAG, "User statistics not found for userId: " + userId);
            return false;
        }
        UserProfile userProfile = userProfileRepository.getByUserId(userId);
        int oldLevel = userProfile.getLevel();
        int oldXP = userProfile.getExperiencePoints();
        int newXP = oldXP + xpToAdd;
        
        Log.d(TAG, "Old XP: " + oldXP + ", Adding: " + xpToAdd + ", New XP: " + newXP);

        userProfile.setExperiencePoints(newXP);

        // Provera da li je dostignut novi nivo
        boolean leveledUp = false;
        int currentLevel = userProfile.getLevel();
        int xpRequiredForNextLevel = calculateXPRequiredForLevel(currentLevel + 1);

        Log.d(TAG, "Current level: " + currentLevel + ", XP required for next: " + xpRequiredForNextLevel);

        while (newXP >= xpRequiredForNextLevel) {
            currentLevel++;
            xpRequiredForNextLevel = calculateXPRequiredForLevel(currentLevel + 1);
            leveledUp = true;
            Log.d(TAG, "Level UP! New level: " + currentLevel);
        }

        if (leveledUp) {
            userProfile.setLevel(currentLevel);
            
            // Dodaj PP za novi nivo
            int ppReward = calculatePPRewardForLevel(currentLevel);
            stats.setPowerPoints(stats.getPowerPoints() + ppReward);
            
            // Ažuriraj titulu
            stats.setTitle(getTitleForLevel(currentLevel));

            Log.d(TAG, String.format("User leveled up! %d -> %d. PP reward: %d", 
                    oldLevel, currentLevel, ppReward));
        }

        // Ažuriraj UserStatistic sa novim vrednostima
        stats.setTotalXPPoints(userProfile.getExperiencePoints());

        // Sačuvaj promene
        Log.d(TAG, "Saving stats... Level: " + userProfile.getLevel() + ", XP: " + userProfile.getExperiencePoints() + ", PP: " + stats.getPowerPoints());
        Log.d(TAG, "UserStatistic before save - Level: " + stats.getLevel() + ", XP: " + stats.getTotalXPPoints() + ", PP: " + stats.getPowerPoints());
        boolean saved = userStatisticRepository.saveOrUpdate(stats);
        userProfileRepository.update(userProfile);
        Log.d(TAG, "Save result: " + saved);
        
        // Proveri da li se stats ažuriraju ispravno
        com.example.dailyboss.domain.model.UserStatistic refreshedStats = userStatisticRepository.getUserStatistic(userId);
        Log.d(TAG, "UserStatistic after save - Level: " + refreshedStats.getLevel() + ", XP: " + refreshedStats.getTotalXPPoints() + ", PP: " + refreshedStats.getPowerPoints());
        
        if (!saved) {
            Log.e(TAG, "Failed to save user statistics after XP gain");
        }

        Log.d(TAG, "=== addExperiencePoints END, leveledUp: " + leveledUp + " ===");
        return leveledUp;
    }

    /**
     * Dodaje novčiće korisniku.
     */
    public boolean addCoins(String userId, int coinsToAdd) {
        UserStatistic stats = userStatisticRepository.getUserStatistic(userId);
        if (stats == null) {
            Log.e(TAG, "User statistics not found for userId: " + userId);
            return false;
        }

        stats.setCoins(stats.getCoins() + coinsToAdd);
        return userStatisticRepository.saveOrUpdate(stats);
    }

    /**
     * Vraća informacije o napredovanju korisnika.
     */
    public LevelInfo getLevelInfo(String userId) {
        UserStatistic stats = userStatisticRepository.getUserStatistic(userId);
        UserProfile userProfile = userProfileRepository.getByUserId(userId);

        if (stats == null) {
            Log.e(TAG, "User statistics not found for userId: " + userId);
            return null;
        }

        int currentLevel = userProfile.getLevel();
        int currentXP = userProfile.getExperiencePoints();
        int xpForNextLevel = calculateXPRequiredForLevel(currentLevel + 1);
        int xpForCurrentLevel = calculateXPRequiredForLevel(currentLevel);
        int currentPP = stats.getPowerPoints();
        int currentCoins = stats.getCoins();
        String title = stats.getTitle();

        return new LevelInfo(currentLevel, currentXP, xpForNextLevel, 
                xpForCurrentLevel, currentPP, currentCoins, title);
    }

    /**
     * Resetuje PP korisnika na baznu vrednost (koristi se nakon borbe).
     */
    public boolean resetPowerPoints(String userId) {
        UserStatistic stats = userStatisticRepository.getUserStatistic(userId);
        UserProfile userProfile = userProfileRepository.getByUserId(userId);

        if (stats == null) {
            Log.e(TAG, "User statistics not found for userId: " + userId);
            return false;
        }

        // PP se resetuje na ukupnu nagradu za trenutni nivo
        int totalPPReward = 0;
        for (int level = 2; level <= userProfile.getLevel(); level++) {
            totalPPReward += calculatePPRewardForLevel(level);
        }
        stats.setPowerPoints(totalPPReward);
        userProfileRepository.update(userProfile);
        return userStatisticRepository.saveOrUpdate(stats);
    }

    /**
     * Vraća trenutni PP korisnika.
     */
    public int getCurrentPowerPoints(String userId) {
        UserStatistic stats = userStatisticRepository.getUserStatistic(userId);
        return stats != null ? stats.getPowerPoints() : 0;
    }
}

