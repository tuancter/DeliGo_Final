package com.deligo.app.repositories;

import com.deligo.app.models.Food;
import java.util.List;

public interface FoodRepository {
    void getAllFoods(DataCallback<List<Food>> callback);
    void getFoodsByCategory(String categoryId, DataCallback<List<Food>> callback);
    void searchFoodsByName(String query, DataCallback<List<Food>> callback);
    void getFoodById(String foodId, DataCallback<Food> callback);
    void getAvailableFoods(DataCallback<List<Food>> callback);
    
    interface DataCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }
}
