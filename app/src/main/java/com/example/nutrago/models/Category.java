package com.example.nutrago.models;

public class Category {
    private int id;
    private String name;
    private String description;
    private String imagePath;

    // Constructor with ID (for fixed database categories)
    public Category(int id, String name, String description, String imagePath) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imagePath = imagePath;
    }

    // Getters only (read-only data)
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getImagePath() {
        return imagePath;
    }

    @Override
    public String toString() {
        return name; // Useful for spinners and dropdowns
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Category category = (Category) obj;
        return id == category.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}