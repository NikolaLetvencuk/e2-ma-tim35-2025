package com.example.dailyboss.domain.model;

/**
 * Reprezentuje podatke o boss-u
 */
public class BossData {
    private int level;
    private String name;
    private int maxHp;
    private String imagePath;
    private long createdAt;

    public BossData() {
    }

    public BossData(int level, String name, int maxHp, String imagePath) {
        this.level = level;
        this.name = name;
        this.maxHp = maxHp;
        this.imagePath = imagePath;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public void setMaxHp(int maxHp) {
        this.maxHp = maxHp;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "BossData{" +
                "level=" + level +
                ", name='" + name + '\'' +
                ", maxHp=" + maxHp +
                ", imagePath='" + imagePath + '\'' +
                '}';
    }
}

