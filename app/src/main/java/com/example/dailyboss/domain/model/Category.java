package com.example.dailyboss.domain.model;

public class Category {
    private String id;
    private String name;
    private String color;

    public Category(String id, String color, String name) {
        this.name = name;
        this.color = color;
        this.id = id;
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

    @Override
    public String toString() {
        return name;
    }
}
