package com.deligo.app.repositories;

import com.deligo.app.models.Category;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AdminCategoryRepositoryImpl implements AdminCategoryRepository {
    private final FirebaseFirestore firestore;

    public AdminCategoryRepositoryImpl() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    @Override
    public void addCategory(String categoryName, ActionCallback callback) {
        Category category = new Category();
        category.setCategoryName(categoryName);

        firestore.collection("categories")
                .add(category)
                .addOnSuccessListener(documentReference -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void updateCategory(String categoryId, String categoryName, ActionCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("categoryName", categoryName);

        firestore.collection("categories")
                .document(categoryId)
                .update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void deleteCategory(String categoryId, ActionCallback callback) {
        firestore.collection("categories")
                .document(categoryId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}
