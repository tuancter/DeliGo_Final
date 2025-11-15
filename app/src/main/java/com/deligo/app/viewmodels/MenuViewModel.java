package com.deligo.app.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.deligo.app.models.Category;
import com.deligo.app.models.Food;
import com.deligo.app.repositories.CategoryRepository;
import com.deligo.app.repositories.FoodRepository;

import java.util.List;

public class MenuViewModel extends ViewModel {
    private static final String TAG = "MenuViewModel";
    private final MutableLiveData<List<Food>> foodList = new MutableLiveData<>();
    private final MutableLiveData<List<Category>> categories = new MutableLiveData<>();
    private final MutableLiveData<String> selectedCategory = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    
    private final FoodRepository foodRepository;
    private final CategoryRepository categoryRepository;

    public MenuViewModel(FoodRepository foodRepository, CategoryRepository categoryRepository) {
        this.foodRepository = foodRepository;
        this.categoryRepository = categoryRepository;
    }

    public LiveData<List<Food>> getFoodList() {
        return foodList;
    }

    public LiveData<List<Category>> getCategories() {
        return categories;
    }

    public LiveData<String> getSelectedCategory() {
        return selectedCategory;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadFoods() {
        Log.d(TAG, "loadFoods: Starting to load foods");
        isLoading.setValue(true);
        foodRepository.getAvailableFoods(new FoodRepository.DataCallback<List<Food>>() {
            @Override
            public void onSuccess(List<Food> data) {
                Log.d(TAG, "loadFoods: Success - received " + (data != null ? data.size() : 0) + " foods");
                foodList.setValue(data);
                isLoading.setValue(false);
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "loadFoods: Error - " + message);
                errorMessage.setValue(message);
                isLoading.setValue(false);
            }
        });
    }

    public void loadCategories() {
        Log.d(TAG, "loadCategories: Starting to load categories");
        categoryRepository.getAllCategories(new CategoryRepository.DataCallback<List<Category>>() {
            @Override
            public void onSuccess(List<Category> data) {
                Log.d(TAG, "loadCategories: Success - received " + (data != null ? data.size() : 0) + " categories");
                categories.setValue(data);
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, "loadCategories: Error - " + message);
                errorMessage.setValue(message);
            }
        });
    }

    public void filterByCategory(String categoryId) {
        isLoading.setValue(true);
        selectedCategory.setValue(categoryId);
        
        if (categoryId == null || categoryId.isEmpty()) {
            loadFoods();
            return;
        }
        
        foodRepository.getFoodsByCategory(categoryId, new FoodRepository.DataCallback<List<Food>>() {
            @Override
            public void onSuccess(List<Food> data) {
                foodList.setValue(data);
                isLoading.setValue(false);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
                isLoading.setValue(false);
            }
        });
    }

    public void searchFoods(String query) {
        isLoading.setValue(true);
        
        if (query == null || query.trim().isEmpty()) {
            loadFoods();
            return;
        }
        
        foodRepository.searchFoodsByName(query, new FoodRepository.DataCallback<List<Food>>() {
            @Override
            public void onSuccess(List<Food> data) {
                foodList.setValue(data);
                isLoading.setValue(false);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
                isLoading.setValue(false);
            }
        });
    }
}
