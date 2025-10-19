package com.example.dailyboss.domain.model;

public class Equipment {
    private String id;           // Jedinstveni ID opreme
    private String name;         // Naziv opreme (npr. "Eliksir snage", "Kožne čizme")
    private String description;  // Opis opreme
    private String iconPath;     // Putanja do ikonice/slike opreme
    private String type;         // Tip opreme (npr. "POTION", "ARMOR", "WEAPON")
    private String bonusType;    // Vrsta bonusa (npr. "POWER_POINTS", "ATTACK_CHANCE", "COIN_BONUS")
    private double bonusValue;   // Vrednost bonusa (npr. 0.20 za 20%, 0.05 za 5%)
    private int durationBattles; // Trajanje u broju borbi (0 ako je trajno ili jednokratno)
    private int durationDays;    // Trajanje u broju dana (0 ako nije vezano za dane)
    private int basePriceCoins;  // Osnovna cena u novčićima
    private boolean isConsumable; // Da li je oprema potrošna (npr. napitak)
    private boolean isStackable;  // Da li se efekti opreme sabiraju (npr. isto oružje)

    public Equipment() {}

    public Equipment(String id, String name, String description, String iconPath, String type,
                     String bonusType, double bonusValue, int durationBattles, int durationDays,
                     int basePriceCoins, boolean isConsumable, boolean isStackable) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.iconPath = iconPath;
        this.type = type;
        this.bonusType = bonusType;
        this.bonusValue = bonusValue;
        this.durationBattles = durationBattles;
        this.durationDays = durationDays;
        this.basePriceCoins = basePriceCoins;
        this.isConsumable = isConsumable;
        this.isStackable = isStackable;
    }

    // Getteri i setteri
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBonusType() {
        return bonusType;
    }

    public void setBonusType(String bonusType) {
        this.bonusType = bonusType;
    }

    public double getBonusValue() {
        return bonusValue;
    }

    public void setBonusValue(double bonusValue) {
        this.bonusValue = bonusValue;
    }

    public int getDurationBattles() {
        return durationBattles;
    }

    public void setDurationBattles(int durationBattles) {
        this.durationBattles = durationBattles;
    }

    public int getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(int durationDays) {
        this.durationDays = durationDays;
    }

    public int getBasePriceCoins() {
        return basePriceCoins;
    }

    public void setBasePriceCoins(int basePriceCoins) {
        this.basePriceCoins = basePriceCoins;
    }

    public boolean isConsumable() {
        return isConsumable;
    }

    public void setConsumable(boolean consumable) {
        isConsumable = consumable;
    }

    public boolean isStackable() {
        return isStackable;
    }

    public void setStackable(boolean stackable) {
        isStackable = stackable;
    }
}