package com.example.dailyboss.domain.enums;

/**
 * Enum za tipove bonusa opreme
 */
public enum EquipmentBonusType {
    POWER_POINTS("POWER_POINTS", "Snaga"),
    ATTACK_CHANCE("ATTACK_CHANCE", "Šansa Napada"),
    ATTACK_COUNT("ATTACK_COUNT", "Broj Napada"),
    COIN_BONUS("COIN_BONUS", "Bonus Novčića");
    
    private final String code;
    private final String displayName;
    
    EquipmentBonusType(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public static EquipmentBonusType fromCode(String code) {
        for (EquipmentBonusType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return POWER_POINTS; // Default
    }
}

