package com.deligo.app.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.deligo.app.models.Category;
import com.deligo.app.models.Food;
import com.deligo.app.repositories.AdminCategoryRepository;
import com.deligo.app.repositories.AdminFoodRepository;
import com.deligo.app.repositories.CategoryRepository;
import com.deligo.app.repositories.FoodRepository;

import java.util.List;

public class AdminMenuViewModel extends ViewModel {
    private final MutableLiveData<List<Food>> foods = new MutableLiveData<>();
    private final MutableLiveData<List<Category>> categories = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> successMessage = new MutableLiveData<>();
    
    private final AdminFoodRepository adminFoodRepository;
    private final AdminCategoryRepository adminCategoryRepository;
    private final FoodRepository foodRepository;
    private final CategoryRepository categoryRepository;

    public AdminMenuViewModel(AdminFoodRepository adminFoodRepository, 
                             AdminCategoryRepository adminCategoryRepository,
                             FoodRepository foodRepository,
                             CategoryRepository categoryRepository) {
        this.adminFoodRepository = adminFoodRepository;
        this.adminCategoryRepository = adminCategoryRepository;
        this.foodRepository = foodRepository;
        this.categoryRepository = categoryRepository;
    }

    public LiveData<List<Food>> getFoods() {
        return foods;
    }

    public LiveData<List<Category>> getCategories() {
        return categories;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<String> getSuccessMessage() {
        return successMessage;
    }

    // Load all foods (including unavailable ones for admin)
    public void loadFoods() {
        isLoading.setValue(true);
        foodRepository.getAllFoods(new FoodRepository.DataCallback<List<Food>>() {
            @Override
            public void onSuccess(List<Food> data) {
                foods.setValue(data);
                isLoading.setValue(false);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
                isLoading.setValue(false);
            }
        });
    }

    public void loadCategories() {
        categoryRepository.getAllCategories(new CategoryRepository.DataCallback<List<Category>>() {
            @Override
            public void onSuccess(List<Category> data) {
                categories.setValue(data);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
            }
        });
    }

    // Food management methods
    public void addFood(Food food) {
        isLoading.setValue(true);
        adminFoodRepository.addFood(food, new AdminFoodRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                successMessage.setValue("Food added successfully");
                isLoading.setValue(false);
                loadFoods(); // Reload the list
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
                isLoading.setValue(false);
            }
        });
    }

    public void updateFood(String foodId, Food food) {
        isLoading.setValue(true);
        adminFoodRepository.updateFood(foodId, food, new AdminFoodRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                successMessage.setValue("Food updated successfully");
                isLoading.setValue(false);
                loadFoods(); // Reload the list
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
                isLoading.setValue(false);
            }
        });
    }

    public void deleteFood(String foodId) {
        isLoading.setValue(true);
        adminFoodRepository.deleteFood(foodId, new AdminFoodRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                successMessage.setValue("Food deleted successfully");
                isLoading.setValue(false);
                loadFoods(); // Reload the list
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
                isLoading.setValue(false);
            }
        });
    }

    public void toggleAvailability(String foodId, boolean isAvailable) {
        isLoading.setValue(true);
        adminFoodRepository.toggleFoodAvailability(foodId, isAvailable, new AdminFoodRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                successMessage.setValue("Food availability updated");
                isLoading.setValue(false);
                loadFoods(); // Reload the list
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
                isLoading.setValue(false);
            }
        });
    }

    // Category management methods
    public void addCategory(String categoryName) {
        isLoading.setValue(true);
        adminCategoryRepository.addCategory(categoryName, new AdminCategoryRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                successMessage.setValue("Category added successfully");
                isLoading.setValue(false);
                loadCategories(); // Reload the list
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
                isLoading.setValue(false);
            }
        });
    }

    public void updateCategory(String categoryId, String categoryName) {
        isLoading.setValue(true);
        adminCategoryRepository.updateCategory(categoryId, categoryName, new AdminCategoryRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                successMessage.setValue("Category updated successfully");
                isLoading.setValue(false);
                loadCategories(); // Reload the list
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
                isLoading.setValue(false);
            }
        });
    }

    public void deleteCategory(String categoryId) {
        isLoading.setValue(true);
        adminCategoryRepository.deleteCategory(categoryId, new AdminCategoryRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                successMessage.setValue("Category deleted successfully");
                isLoading.setValue(false);
                loadCategories(); // Reload the list
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
                isLoading.setValue(false);
            }
        });
    }
}
