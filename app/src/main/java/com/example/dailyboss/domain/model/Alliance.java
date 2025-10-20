package com.example.dailyboss.domain.model;

import java.util.Date;
import java.util.List;

public class Alliance {
    private String id;
    private String name;
    private String leaderId;
    private Date createdAt;
    private boolean isMissionActive;
    private String status;
    private String activeSpecialMissionId;

    public Alliance() {}

    public Alliance(String id, String name, String leaderId, Date createdAt, String specialMissionId) {
        this.id = id;
        this.name = name;
        this.leaderId = leaderId;
        this.createdAt = createdAt;
        this.isMissionActive = false;
        this.status = "Active";
        this.activeSpecialMissionId = specialMissionId;
    }

    // Getteri i Setteri
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLeaderId() { return leaderId; }
    public void setLeaderId(String leaderId) { this.leaderId = leaderId; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public boolean isMissionActive() { return isMissionActive; }
    public void setMissionActive(boolean missionActive) { isMissionActive = missionActive; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getActiveSpecialMissionId() { return activeSpecialMissionId; }
    public void setActiveSpecialMissionId(String activeSpecialMissionId) { this.activeSpecialMissionId = activeSpecialMissionId; }

}