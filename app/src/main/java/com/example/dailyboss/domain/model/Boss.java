package com.example.dailyboss.domain.model;

public class Boss {
    private String id;
    private int level; // Nivo bosa (1. bos, 2. bos, itd.)
    private int maxHp; // Maksimalni HP bosa
    private int currentHp; // Trenutni HP bosa
    private String name; // Ime bosa (npr. "Goblin King", "Shadow Dragon")
    private String imageUrl; // URL do slike bosa (za animacije)
    private boolean defeated; // Da li je bos poražen

    public Boss() {}

    public Boss(String id, int level, int maxHp, int currentHp, String name, String imageUrl, boolean defeated) {
        this.id = id;
        this.level = level;
        this.maxHp = maxHp;
        this.currentHp = currentHp;
        this.name = name;
        this.imageUrl = imageUrl;
        this.defeated = defeated;
    }

    // Getteri i Setteri
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public int getMaxHp() { return maxHp; }
    public void setMaxHp(int maxHp) { this.maxHp = maxHp; }
    public int getCurrentHp() { return currentHp; }
    public void setCurrentHp(int currentHp) { this.currentHp = currentHp; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public boolean isDefeated() { return defeated; }
    public void setDefeated(boolean defeated) { this.defeated = defeated; }

    public static int calculateNextBossHp(int previousBossHp) {
        return previousBossHp * 2 + previousBossHp / 2;
    }
}