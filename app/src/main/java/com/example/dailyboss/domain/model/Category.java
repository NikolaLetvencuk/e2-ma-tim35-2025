package com.example.dailyboss.domain.model;

public class Category {
    private String id;
    private String name;
    private String color;
    private String userId;

    public Category(String id, String color, String name, String userId) {
        this.name = name;
        this.color = color;
        this.id = id;
        this.userId = userId;
    }

    public Category() {}


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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return name;
    }
}
