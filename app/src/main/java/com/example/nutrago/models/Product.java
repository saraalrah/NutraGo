package com.example.nutrago.models;

public class Product {
    private int id;
    private String name;
    private String description;
    private double price;
    private int imageResId;
    private int categoryId; // New field for category reference
    private boolean isInCart;

    // Constructor without ID and categoryId (for simple products)
    public Product(String name, String description, double price, int imageResId) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageResId = imageResId;
        this.categoryId = 0; // Default category
        this.isInCart = false;
    }

    // Constructor with ID but without categoryId (for backward compatibility)
    public Product(int id, String name, String description, double price, int imageResId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageResId = imageResId;
        this.categoryId = 0; // Default category
        this.isInCart = false;
    }

    // Complete constructor with all fields
    public Product(int id, String name, String description, double price, int imageResId, int categoryId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageResId = imageResId;
        this.categoryId = categoryId;
        this.isInCart = false;
    }

    // Constructor without ID but with categoryId (for new products with category)
    public Product(String name, String description, double price, int imageResId, int categoryId) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageResId = imageResId;
        this.categoryId = categoryId;
        this.isInCart = false;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public int getImageResId() {
        return imageResId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public boolean isInCart() {
        return isInCart;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public void setInCart(boolean inCart) {
        isInCart = inCart;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", categoryId=" + categoryId +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Product product = (Product) obj;
        return id == product.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}