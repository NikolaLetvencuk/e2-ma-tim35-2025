package com.example.dailyboss.domain.model;

/**
 * Reprezentuje rezultat borbe
 */
public class BattleResult {
    private boolean success;
    private boolean hit;
    private boolean bossDefeated;
    private boolean attacksExhausted;
    private int coinsWon;
    private String equipmentWon;
    private String equipmentId;
    private String equipmentName;
    private int newBossHp;
    private int newUserPp;
    private int newAttacksLeft;
    private String message;
    private boolean shouldOpenChest;

    public BattleResult() {
    }

    public BattleResult(boolean success, boolean hit, boolean bossDefeated, 
                       boolean attacksExhausted, int coinsWon, String equipmentWon) {
        this.success = success;
        this.hit = hit;
        this.bossDefeated = bossDefeated;
        this.attacksExhausted = attacksExhausted;
        this.coinsWon = coinsWon;
        this.equipmentWon = equipmentWon;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isHit() {
        return hit;
    }

    public void setHit(boolean hit) {
        this.hit = hit;
    }

    public boolean isBossDefeated() {
        return bossDefeated;
    }

    public void setBossDefeated(boolean bossDefeated) {
        this.bossDefeated = bossDefeated;
    }

    public boolean isAttacksExhausted() {
        return attacksExhausted;
    }

    public void setAttacksExhausted(boolean attacksExhausted) {
        this.attacksExhausted = attacksExhausted;
    }

    public int getCoinsWon() {
        return coinsWon;
    }

    public void setCoinsWon(int coinsWon) {
        this.coinsWon = coinsWon;
    }

    public String getEquipmentWon() {
        return equipmentWon;
    }

    public void setEquipmentWon(String equipmentWon) {
        this.equipmentWon = equipmentWon;
    }

    public String getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(String equipmentId) {
        this.equipmentId = equipmentId;
    }

    public String getEquipmentName() {
        return equipmentName;
    }

    public void setEquipmentName(String equipmentName) {
        this.equipmentName = equipmentName;
    }

    public int getNewBossHp() {
        return newBossHp;
    }

    public void setNewBossHp(int newBossHp) {
        this.newBossHp = newBossHp;
    }

    public int getNewUserPp() {
        return newUserPp;
    }

    public void setNewUserPp(int newUserPp) {
        this.newUserPp = newUserPp;
    }

    public int getNewAttacksLeft() {
        return newAttacksLeft;
    }

    public void setNewAttacksLeft(int newAttacksLeft) {
        this.newAttacksLeft = newAttacksLeft;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isShouldOpenChest() {
        return shouldOpenChest;
    }

    public void setShouldOpenChest(boolean shouldOpenChest) {
        this.shouldOpenChest = shouldOpenChest;
    }

    @Override
    public String toString() {
        return "BattleResult{" +
                "success=" + success +
                ", hit=" + hit +
                ", bossDefeated=" + bossDefeated +
                ", attacksExhausted=" + attacksExhausted +
                ", coinsWon=" + coinsWon +
                ", equipmentWon='" + equipmentWon + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}

