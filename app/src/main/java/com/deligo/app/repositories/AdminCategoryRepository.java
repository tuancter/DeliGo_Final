package com.deligo.app.repositories;

public interface AdminCategoryRepository {
    void addCategory(String categoryName, ActionCallback callback);
    void updateCategory(String categoryId, String categoryName, ActionCallback callback);
    void deleteCategory(String categoryId, ActionCallback callback);
    
    interface ActionCallback {
        void onSuccess();
        void onError(String message);
    }
}
