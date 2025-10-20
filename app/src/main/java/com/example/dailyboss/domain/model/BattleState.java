package com.example.dailyboss.domain.model;

/**
 * Reprezentuje trenutno stanje borbe
 */
public class BattleState {
    private int bossHp;
    private int bossMaxHp;
    private int userPp;
    private int attacksLeft;
    private int bossIndex;
    private String userId;
    private int hitChance;
    private boolean chestOpened;
    private String selectedEquipmentId;
    private String selectedEquipmentName;

    public BattleState() {
    }

    public BattleState(int bossHp, int bossMaxHp, int userPp, int attacksLeft, 
                      int bossIndex, String userId, int hitChance, boolean chestOpened) {
        this.bossHp = bossHp;
        this.bossMaxHp = bossMaxHp;
        this.userPp = userPp;
        this.attacksLeft = attacksLeft;
        this.bossIndex = bossIndex;
        this.userId = userId;
        this.hitChance = hitChance;
        this.chestOpened = chestOpened;
    }

    // Getters and Setters
    public int getBossHp() {
        return bossHp;
    }

    public void setBossHp(int bossHp) {
        this.bossHp = bossHp;
    }

    public int getBossMaxHp() {
        return bossMaxHp;
    }

    public void setBossMaxHp(int bossMaxHp) {
        this.bossMaxHp = bossMaxHp;
    }

    public int getUserPp() {
        return userPp;
    }

    public void setUserPp(int userPp) {
        this.userPp = userPp;
    }

    public int getAttacksLeft() {
        return attacksLeft;
    }

    public void setAttacksLeft(int attacksLeft) {
        this.attacksLeft = attacksLeft;
    }

    public int getBossIndex() {
        return bossIndex;
    }

    public void setBossIndex(int bossIndex) {
        this.bossIndex = bossIndex;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getHitChance() {
        return hitChance;
    }

    public void setHitChance(int hitChance) {
        this.hitChance = hitChance;
    }

    public boolean isChestOpened() {
        return chestOpened;
    }

    public void setChestOpened(boolean chestOpened) {
        this.chestOpened = chestOpened;
    }

    public String getSelectedEquipmentId() {
        return selectedEquipmentId;
    }

    public void setSelectedEquipmentId(String selectedEquipmentId) {
        this.selectedEquipmentId = selectedEquipmentId;
    }

    public String getSelectedEquipmentName() {
        return selectedEquipmentName;
    }

    public void setSelectedEquipmentName(String selectedEquipmentName) {
        this.selectedEquipmentName = selectedEquipmentName;
    }

    @Override
    public String toString() {
        return "BattleState{" +
                "bossHp=" + bossHp +
                ", bossMaxHp=" + bossMaxHp +
                ", userPp=" + userPp +
                ", attacksLeft=" + attacksLeft +
                ", bossIndex=" + bossIndex +
                ", userId='" + userId + '\'' +
                ", hitChance=" + hitChance +
                ", chestOpened=" + chestOpened +
                '}';
    }
}

