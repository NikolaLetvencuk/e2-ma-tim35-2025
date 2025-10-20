package com.example.dailyboss.domain.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SpecialMission {
    private String id;
    private String allianceId;
    private Date startTime;
    private Date endTime;
    private long totalBossHp;
    private long currentBossHp;
    private boolean completedSuccessfully;
    private boolean isActive;
    private int numberOfParticipatingMembers;
    private boolean rewardsAwarded;
    public SpecialMission() {
    }

    public SpecialMission(String id, String allianceId, int numberOfMembers, Date startTime, boolean rewardsAwarded) {
        this.id = id;
        this.allianceId = allianceId;
        this.startTime = startTime;
        this.endTime = new Date(startTime.getTime() + TimeUnit.DAYS.toMillis(14)); // 2 nedelje
        this.totalBossHp = 100L * numberOfMembers;
        this.currentBossHp = totalBossHp;
        this.completedSuccessfully = false;
        this.isActive = true;
        this.numberOfParticipatingMembers = numberOfMembers;
        this.rewardsAwarded = rewardsAwarded;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAllianceId() { return allianceId; }
    public void setAllianceId(String allianceId) { this.allianceId = allianceId; }

    public Date getStartTime() { return startTime; }
    public void setStartTime(Date startTime) { this.startTime = startTime; }

    public Date getEndTime() { return endTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }

    public long getTotalBossHp() { return totalBossHp; }
    public void setTotalBossHp(long totalBossHp) { this.totalBossHp = totalBossHp; }

    public long getCurrentBossHp() { return currentBossHp; }
    public void setCurrentBossHp(long currentBossHp) { this.currentBossHp = currentBossHp; }

    public boolean isCompletedSuccessfully() { return completedSuccessfully; }
    public void setCompletedSuccessfully(boolean completedSuccessfully) { this.completedSuccessfully = completedSuccessfully; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public int getNumberOfParticipatingMembers() { return numberOfParticipatingMembers; }
    public void setNumberOfParticipatingMembers(int numberOfParticipatingMembers) { this.numberOfParticipatingMembers = numberOfParticipatingMembers; }
    public boolean isRewardsAwarded() { return rewardsAwarded; } // Getter
    public void setRewardsAwarded(boolean rewardsAwarded) { this.rewardsAwarded = rewardsAwarded; }
}