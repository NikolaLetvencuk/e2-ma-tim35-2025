package com.example.dailyboss.domain.model;

public class UserStatistic {

    private String id;          // primarni ključ (može i auto-generated)
    private String userId;      // FK na User
    private int completedTasks;
    private int failedTasks;
    private int level;
    private int experiencePoints;
    private int winStreak;
    private long lastActiveTimestamp; // poslednja aktivnost korisnika

    public UserStatistic() {}

    public UserStatistic(String id, String userId, int completedTasks, int failedTasks,
                         int level, int experiencePoints, int winStreak, long lastActiveTimestamp) {
        this.id = id;
        this.userId = userId;
        this.completedTasks = completedTasks;
        this.failedTasks = failedTasks;
        this.level = level;
        this.experiencePoints = experiencePoints;
        this.winStreak = winStreak;
        this.lastActiveTimestamp = lastActiveTimestamp;
    }

    // Getteri i setteri
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getCompletedTasks() {
        return completedTasks;
    }

    public void setCompletedTasks(int completedTasks) {
        this.completedTasks = completedTasks;
    }

    public int getFailedTasks() {
        return failedTasks;
    }

    public void setFailedTasks(int failedTasks) {
        this.failedTasks = failedTasks;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getExperiencePoints() {
        return experiencePoints;
    }

    public void setExperiencePoints(int experiencePoints) {
        this.experiencePoints = experiencePoints;
    }

    public int getWinStreak() {
        return winStreak;
    }

    public void setWinStreak(int winStreak) {
        this.winStreak = winStreak;
    }

    public long getLastActiveTimestamp() {
        return lastActiveTimestamp;
    }

    public void setLastActiveTimestamp(long lastActiveTimestamp) {
        this.lastActiveTimestamp = lastActiveTimestamp;
    }
}