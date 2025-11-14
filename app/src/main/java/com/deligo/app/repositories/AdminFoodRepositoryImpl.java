package com.deligo.app.repositories;

import com.deligo.app.models.Food;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AdminFoodRepositoryImpl implements AdminFoodRepository {
    private final FirebaseFirestore firestore;

    public AdminFoodRepositoryImpl() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    @Override
    public void addFood(Food food, ActionCallback callback) {
        firestore.collection("foods")
                .add(food)
                .addOnSuccessListener(documentReference -> {
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void updateFood(String foodId, Food food, ActionCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("categoryId", food.getCategoryId());
        updates.put("name", food.getName());
        updates.put("description", food.getDescription());
        updates.put("price", food.getPrice());
        updates.put("imageUrl", food.getImageUrl());
        updates.put("isAvailable", food.isAvailable());

        firestore.collection("foods")
                .document(foodId)
                .update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void deleteFood(String foodId, ActionCallback callback) {
        // Set isAvailable to false instead of deleting
        Map<String, Object> updates = new HashMap<>();
        updates.put("isAvailable", false);

        firestore.collection("foods")
                .document(foodId)
                .update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void toggleFoodAvailability(String foodId, boolean isAvailable, ActionCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("isAvailable", isAvailable);

        firestore.collection("foods")
                .document(foodId)
                .update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}
