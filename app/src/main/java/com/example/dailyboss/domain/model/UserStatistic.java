package com.example.dailyboss.domain.model;

// Nema više Map-e direktno u modelu
public class UserStatistic {
    private String id; // Primarni ključ, može biti isti kao userId
    private String userId; // FK na User
    private int activeDaysCount;
    private int totalCreatedTasks;
    private int totalCompletedTasks;
    private int totalFailedTasks;
    private int totalCancelledTasks;
    private int longestTaskStreak;
    private int currentTaskStreak;
    private int totalSpecialMissionsStarted;
    private int totalSpecialMissionsCompleted;
    private long lastStreakUpdateTimestamp;
    private int totalXPPoints;
    private double averageXPEarned;
    private int powerPoints;
    private int coins;
    private String title;

    public UserStatistic() {}

    public UserStatistic(String id, String userId, int activeDaysCount, int totalCreatedTasks,
                         int totalCompletedTasks, int totalFailedTasks, int totalCancelledTasks,
                         int longestTaskStreak, int currentTaskStreak,
                         int totalSpecialMissionsStarted, int totalSpecialMissionsCompleted,
                         long lastStreakUpdateTimestamp, int totalXPPoints, double averageXPEarned,
                         int powerPoints, int coins, String title) {
        this.id = id;
        this.userId = userId;
        this.activeDaysCount = activeDaysCount;
        this.totalCreatedTasks = totalCreatedTasks;
        this.totalCompletedTasks = totalCompletedTasks;
        this.totalFailedTasks = totalFailedTasks;
        this.totalCancelledTasks = totalCancelledTasks;
        this.longestTaskStreak = longestTaskStreak;
        this.totalSpecialMissionsStarted = totalSpecialMissionsStarted;
        this.totalSpecialMissionsCompleted = totalSpecialMissionsCompleted;
        this.lastStreakUpdateTimestamp = lastStreakUpdateTimestamp;
        this.currentTaskStreak = currentTaskStreak;
        this.totalXPPoints = totalXPPoints;
        this.averageXPEarned = averageXPEarned;
        this.powerPoints = powerPoints;
        this.coins = coins;
        this.title = title;
    }

    // Getteri i setteri (ukloni getter/setter za completedTasksByCategory)
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public int getActiveDaysCount() { return activeDaysCount; }

    public int getCurrentTaskStreak() {
        return currentTaskStreak;
    }

    public int getTotalXPPoints() {
        return totalXPPoints;
    }

    public void setTotalXPPoints(int totalXPPoints) {
        this.totalXPPoints = totalXPPoints;
    }

    public double getAverageXPEarned() {
        return averageXPEarned;
    }

    public void setAverageXPEarned(double averageXPEarned) {
        this.averageXPEarned = averageXPEarned;
    }

    public void setCurrentTaskStreak(int currentTaskStreak) {
        this.currentTaskStreak = currentTaskStreak;
    }

    public void setActiveDaysCount(int activeDaysCount) { this.activeDaysCount = activeDaysCount; }
    public int getTotalCreatedTasks() { return totalCreatedTasks; }
    public void setTotalCreatedTasks(int totalCreatedTasks) { this.totalCreatedTasks = totalCreatedTasks; }
    public int getTotalCompletedTasks() { return totalCompletedTasks; }
    public void setTotalCompletedTasks(int totalCompletedTasks) { this.totalCompletedTasks = totalCompletedTasks; }
    public int getTotalFailedTasks() { return totalFailedTasks; }
    public void setTotalFailedTasks(int totalFailedTasks) { this.totalFailedTasks = totalFailedTasks; }
    public int getTotalCancelledTasks() { return totalCancelledTasks; }
    public void setTotalCancelledTasks(int totalCancelledTasks) { this.totalCancelledTasks = totalCancelledTasks; }
    public int getLongestTaskStreak() { return longestTaskStreak; }
    public void setLongestTaskStreak(int longestTaskStreak) { this.longestTaskStreak = longestTaskStreak; }
    // public Map<String, Integer> getCompletedTasksByCategory() { return completedTasksByCategory; } // Uklonjeno
    // public void setCompletedTasksByCategory(Map<String, Integer> completedTasksByCategory) { this.completedTasksByCategory = completedTasksByCategory; } // Uklonjeno
    public int getTotalSpecialMissionsStarted() { return totalSpecialMissionsStarted; }
    public void setTotalSpecialMissionsStarted(int totalSpecialMissionsStarted) { this.totalSpecialMissionsStarted = totalSpecialMissionsStarted; }
    public int getTotalSpecialMissionsCompleted() { return totalSpecialMissionsCompleted; }
    public void setTotalSpecialMissionsCompleted(int totalSpecialMissionsCompleted) { this.totalSpecialMissionsCompleted = totalSpecialMissionsCompleted; }
    public long getLastStreakUpdateTimestamp() { return lastStreakUpdateTimestamp; }
    public void setLastStreakUpdateTimestamp(long lastStreakUpdateTimestamp) { this.lastStreakUpdateTimestamp = lastStreakUpdateTimestamp; }

    // Pomoćna metoda za inkrementiranje broja zadataka po kategoriji biće u DAO-u ili servisu
    // U klasi UserStatistic
    public void incrementActiveDaysCount() {
        this.activeDaysCount++;
    }

    public void incrementTotalCreatedTasks() {
        this.totalCreatedTasks++;
    }

    public void incrementTotalCompletedTasks() {
        this.totalCompletedTasks++;
    }

    public void incrementTotalFailedTasks() {
        this.totalFailedTasks++;
    }

    public void incrementTotalCancelledTasks() {
        this.totalCancelledTasks++;
    }

    public void incrementLongestStreak() {
        this.longestTaskStreak++;
    }

    public void incrementCurrentStrak() {
        this.currentTaskStreak++;
    }

    public void updateLongestTaskStreak(int currentStreak) {
        if (currentStreak > this.longestTaskStreak) {
            this.longestTaskStreak = currentStreak;
        }
    }

    public void incrementTotalSpecialMissionsStarted() {
        this.totalSpecialMissionsStarted++;
    }

    public void incrementTotalSpecialMissionsCompleted() {
        this.totalSpecialMissionsCompleted++;
    }

    public void updateLastStreakTimestamp(long timestamp) {
        this.lastStreakUpdateTimestamp = timestamp;
    }

    public int getPowerPoints() {
        return powerPoints;
    }

    public void setPowerPoints(int powerPoints) {
        this.powerPoints = powerPoints;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    
    /**
     * Kalkuliše nivo korisnika na osnovu totalXPPoints
     * Koristi LevelingService logiku za kalkulaciju nivoa
     */
    public int getLevel() {
        if (totalXPPoints < 200) {
            return 1; // Prvi nivo
        }
        
        int level = 1;
        int xpRequired = 200; // XP za prvi nivo
        
        while (totalXPPoints >= xpRequired) {
            level++;
            // Formula: XP prethodnog nivoa * 2 + XP prethodnog nivoa / 2
            xpRequired = xpRequired * 2 + xpRequired / 2;
        }
        
        return level;
    }
}