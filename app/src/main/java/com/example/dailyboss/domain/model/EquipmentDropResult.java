package com.example.dailyboss.domain.model;

/**
 * Reprezentuje rezultat equipment drop-a
 */
public class EquipmentDropResult {
    private String equipmentId;
    private String equipmentName;
    private boolean dropped;
    private String equipmentType;
    private String message;

    public EquipmentDropResult() {
    }

    public EquipmentDropResult(String equipmentId, String equipmentName, boolean dropped) {
        this.equipmentId = equipmentId;
        this.equipmentName = equipmentName;
        this.dropped = dropped;
    }

    // Getters and Setters
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

    public boolean isDropped() {
        return dropped;
    }

    public void setDropped(boolean dropped) {
        this.dropped = dropped;
    }

    public String getEquipmentType() {
        return equipmentType;
    }

    public void setEquipmentType(String equipmentType) {
        this.equipmentType = equipmentType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "EquipmentDropResult{" +
                "equipmentId='" + equipmentId + '\'' +
                ", equipmentName='" + equipmentName + '\'' +
                ", dropped=" + dropped +
                ", equipmentType='" + equipmentType + '\'' +
                '}';
    }
}

