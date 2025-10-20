package com.example.dailyboss.domain.model;

/**
 * Reprezentuje istoriju borbe
 */
public class BattleHistory {
    private long id;
    private String userId;
    private int bossLevel;
    private boolean bossDefeated;
    private int coinsWon;
    private String equipmentWon;
    private int attacksUsed;
    private long battleDate;

    public BattleHistory() {
    }

    public BattleHistory(String userId, int bossLevel, boolean bossDefeated, 
                        int coinsWon, String equipmentWon, int attacksUsed, long battleDate) {
        this.userId = userId;
        this.bossLevel = bossLevel;
        this.bossDefeated = bossDefeated;
        this.coinsWon = coinsWon;
        this.equipmentWon = equipmentWon;
        this.attacksUsed = attacksUsed;
        this.battleDate = battleDate;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getBossLevel() {
        return bossLevel;
    }

    public void setBossLevel(int bossLevel) {
        this.bossLevel = bossLevel;
    }

    public boolean isBossDefeated() {
        return bossDefeated;
    }

    public void setBossDefeated(boolean bossDefeated) {
        this.bossDefeated = bossDefeated;
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

    public int getAttacksUsed() {
        return attacksUsed;
    }

    public void setAttacksUsed(int attacksUsed) {
        this.attacksUsed = attacksUsed;
    }

    public long getBattleDate() {
        return battleDate;
    }

    public void setBattleDate(long battleDate) {
        this.battleDate = battleDate;
    }

    @Override
    public String toString() {
        return "BattleHistory{" +
                "id=" + id +
                ", userId='" + userId + '\'' +
                ", bossLevel=" + bossLevel +
                ", bossDefeated=" + bossDefeated +
                ", coinsWon=" + coinsWon +
                ", equipmentWon='" + equipmentWon + '\'' +
                ", attacksUsed=" + attacksUsed +
                ", battleDate=" + battleDate +
                '}';
    }
}

