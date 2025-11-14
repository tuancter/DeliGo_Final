package com.deligo.app.repositories;

import com.deligo.app.models.Category;
import java.util.List;

public interface CategoryRepository {
    void getAllCategories(DataCallback<List<Category>> callback);
    void getCategoryById(String categoryId, DataCallback<Category> callback);
    
    interface DataCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }
}
