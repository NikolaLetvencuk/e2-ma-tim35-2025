package com.example.dailyboss.domain.enums;

/**
 * Enum za tipove opreme
 */
public enum EquipmentType {
    POTION("POTION", "Napitak"),
    ARMOR("ARMOR", "Odeća"), 
    WEAPON("WEAPON", "Oružje");
    
    private final String code;
    private final String displayName;
    
    EquipmentType(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public static EquipmentType fromCode(String code) {
        for (EquipmentType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return POTION; // Default
    }
}

