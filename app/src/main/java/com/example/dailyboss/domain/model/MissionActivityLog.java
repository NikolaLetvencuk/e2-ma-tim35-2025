package com.example.dailyboss.domain.model;

import java.util.Date;

public class MissionActivityLog {
    private String id;
    private String specialMissionId;
    private String userId;
    private String username;
    private String activityDescription;
    private int damageDealt;
    private Date timestamp;

    public MissionActivityLog() {}

    public MissionActivityLog(String id, String specialMissionId, String userId, String username, String activityDescription, int damageDealt, Date timestamp) {
        this.id = id;
        this.specialMissionId = specialMissionId;
        this.userId = userId;
        this.username = username;
        this.activityDescription = activityDescription;
        this.damageDealt = damageDealt;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSpecialMissionId() { return specialMissionId; }
    public void setSpecialMissionId(String specialMissionId) { this.specialMissionId = specialMissionId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getActivityDescription() { return activityDescription; }
    public void setActivityDescription(String activityDescription) { this.activityDescription = activityDescription; }

    public int getDamageDealt() { return damageDealt; }
    public void setDamageDealt(int damageDealt) { this.damageDealt = damageDealt; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    public String getDisplayMessage() {
        return username + " " + activityDescription + " -" + damageDealt + "HP";
    }
}