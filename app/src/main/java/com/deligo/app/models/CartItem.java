package com.deligo.app.models;

import java.io.Serializable;

public class CartItem implements Serializable {
    private String cartItemId;
    private String cartId;
    private String foodId;
    private int quantity;
    private double price;
    private String note;
    private Food food; // Populated when retrieved

    // Required empty constructor for Firestore
    public CartItem() {
    }

    public CartItem(String cartItemId, String cartId, String foodId, int quantity, double price, String note) {
        this.cartItemId = cartItemId;
        this.cartId = cartId;
        this.foodId = foodId;
        this.quantity = quantity;
        this.price = price;
        this.note = note;
    }

    // Getters and Setters
    public String getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(String cartItemId) {
        this.cartItemId = cartItemId;
    }

    public String getCartId() {
        return cartId;
    }

    public void setCartId(String cartId) {
        this.cartId = cartId;
    }

    public String getFoodId() {
        return foodId;
    }

    public void setFoodId(String foodId) {
        this.foodId = foodId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Food getFood() {
        return food;
    }

    public void setFood(Food food) {
        this.food = food;
    }
}
