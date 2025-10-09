package com.example.dailyboss.domain.model;

public class Badge {
    private String id;           // Jedinstveni ID bedža
    private String name;         // Naziv bedža (npr. "Prvi savezni borac")
    private String description;  // Opis bedža
    private String iconPath;     // Putanja do ikonice/slike bedža
    private int requiredCompletions; // Broj završenih specijalnih zadataka za ovaj bedž

    public Badge() {}

    public Badge(String id, String name, String description, String iconPath, int requiredCompletions) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.iconPath = iconPath;
        this.requiredCompletions = requiredCompletions;
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

    public int getRequiredCompletions() {
        return requiredCompletions;
    }

    public void setRequiredCompletions(int requiredCompletions) {
        this.requiredCompletions = requiredCompletions;
    }
}