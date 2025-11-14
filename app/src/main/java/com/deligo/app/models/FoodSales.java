package com.deligo.app.models;

public class FoodSales {
    private Food food;
    private int quantitySold;

    public FoodSales() {
    }

    public FoodSales(Food food, int quantitySold) {
        this.food = food;
        this.quantitySold = quantitySold;
    }

    public Food getFood() {
        return food;
    }

    public void setFood(Food food) {
        this.food = food;
    }

    public int getQuantitySold() {
        return quantitySold;
    }

    public void setQuantitySold(int quantitySold) {
        this.quantitySold = quantitySold;
    }
}
