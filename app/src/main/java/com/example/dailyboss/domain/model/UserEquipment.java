package com.example.dailyboss.domain.model;

public class UserEquipment {
    private String id;           // Jedinstveni ID unosa
    private String userId;       // FK na User
    private String equipmentId;  // FK na Equipment
    private int quantity;        // Količina (za potrošnu opremu)
    private boolean isActive;    // Da li je oprema trenutno aktivna
    private int remainingDurationBattles; // Preostalo trajanje u borbama
    private long activationTimestamp; // Vremenska oznaka aktivacije (za trajanje po danima)
    private double currentBonusValue; // Trenutna vrednost bonusa (ako se sabira)

    public UserEquipment() {}

    public UserEquipment(String id, String userId, String equipmentId, int quantity,
                         boolean isActive, int remainingDurationBattles, long activationTimestamp, double currentBonusValue) {
        this.id = id;
        this.userId = userId;
        this.equipmentId = equipmentId;
        this.quantity = quantity;
        this.isActive = isActive;
        this.remainingDurationBattles = remainingDurationBattles;
        this.activationTimestamp = activationTimestamp;
        this.currentBonusValue = currentBonusValue;
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

    public String getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(String equipmentId) {
        this.equipmentId = equipmentId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public int getRemainingDurationBattles() {
        return remainingDurationBattles;
    }

    public void setRemainingDurationBattles(int remainingDurationBattles) {
        this.remainingDurationBattles = remainingDurationBattles;
    }

    public long getActivationTimestamp() {
        return activationTimestamp;
    }

    public void setActivationTimestamp(long activationTimestamp) {
        this.activationTimestamp = activationTimestamp;
    }

    public double getCurrentBonusValue() {
        return currentBonusValue;
    }

    public void setCurrentBonusValue(double currentBonusValue) {
        this.currentBonusValue = currentBonusValue;
    }
}