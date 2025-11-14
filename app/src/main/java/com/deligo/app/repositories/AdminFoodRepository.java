package com.deligo.app.repositories;

import com.deligo.app.models.Food;

public interface AdminFoodRepository {
    void addFood(Food food, ActionCallback callback);
    void updateFood(String foodId, Food food, ActionCallback callback);
    void deleteFood(String foodId, ActionCallback callback);
    void toggleFoodAvailability(String foodId, boolean isAvailable, ActionCallback callback);
    
    interface ActionCallback {
        void onSuccess();
        void onError(String message);
    }
}
