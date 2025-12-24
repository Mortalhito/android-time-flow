// Category.java
package com.example.timeflow.room.entity;

public class Category {
    private int id;
    private String name;
    private int color;

    public Category() {
    }

    private boolean isDefault;

    public Category(int id, String name, int color) {
        this.id = id;
        this.name = name;
        this.color = color;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getColor() { return color; }
    public void setColor(int color) { this.color = color; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }
}