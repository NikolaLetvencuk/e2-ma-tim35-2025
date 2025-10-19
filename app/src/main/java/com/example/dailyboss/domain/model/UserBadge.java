package com.example.dailyboss.domain.model;

public class UserBadge {
    private String id;        // Jedinstveni ID unosa
    private String userId;    // FK na User
    private String badgeId;   // FK na Badge
    private long dateAchieved; // Vremenska oznaka kada je bedž osvojen

    public UserBadge() {}

    public UserBadge(String id, String userId, String badgeId, long dateAchieved) {
        this.id = id;
        this.userId = userId;
        this.badgeId = badgeId;
        this.dateAchieved = dateAchieved;
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

    public String getBadgeId() {
        return badgeId;
    }

    public void setBadgeId(String badgeId) {
        this.badgeId = badgeId;
    }

    public long getDateAchieved() {
        return dateAchieved;
    }

    public void setDateAchieved(long dateAchieved) {
        this.dateAchieved = dateAchieved;
    }
}