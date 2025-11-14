package com.deligo.app.repositories;

import com.deligo.app.models.Category;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CategoryRepositoryImpl implements CategoryRepository {
    private final FirebaseFirestore firestore;

    public CategoryRepositoryImpl() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    @Override
    public void getAllCategories(DataCallback<List<Category>> callback) {
        firestore.collection("categories")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Category> categories = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Category category = document.toObject(Category.class);
                        category.setCategoryId(document.getId());
                        categories.add(category);
                    }
                    callback.onSuccess(categories);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void getCategoryById(String categoryId, DataCallback<Category> callback) {
        firestore.collection("categories")
                .document(categoryId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Category category = documentSnapshot.toObject(Category.class);
                        if (category != null) {
                            category.setCategoryId(documentSnapshot.getId());
                        }
                        callback.onSuccess(category);
                    } else {
                        callback.onError("Category not found");
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}
