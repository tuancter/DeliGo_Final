package com.deligo.app.repositories;

import com.deligo.app.models.Food;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FoodRepositoryImpl implements FoodRepository {
    private final FirebaseFirestore firestore;

    public FoodRepositoryImpl() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    @Override
    public void getAllFoods(DataCallback<List<Food>> callback) {
        firestore.collection("foods")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Food> foods = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Food food = document.toObject(Food.class);
                        food.setFoodId(document.getId());
                        foods.add(food);
                    }
                    callback.onSuccess(foods);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void getFoodsByCategory(String categoryId, DataCallback<List<Food>> callback) {
        firestore.collection("foods")
                .whereEqualTo("categoryId", categoryId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Food> foods = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Food food = document.toObject(Food.class);
                        food.setFoodId(document.getId());
                        foods.add(food);
                    }
                    callback.onSuccess(foods);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void searchFoodsByName(String query, DataCallback<List<Food>> callback) {
        firestore.collection("foods")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Food> foods = new ArrayList<>();
                    String lowerQuery = query.toLowerCase();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Food food = document.toObject(Food.class);
                        food.setFoodId(document.getId());
                        if (food.getName().toLowerCase().contains(lowerQuery)) {
                            foods.add(food);
                        }
                    }
                    callback.onSuccess(foods);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void getFoodById(String foodId, DataCallback<Food> callback) {
        firestore.collection("foods")
                .document(foodId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Food food = documentSnapshot.toObject(Food.class);
                        if (food != null) {
                            food.setFoodId(documentSnapshot.getId());
                        }
                        callback.onSuccess(food);
                    } else {
                        callback.onError("Food not found");
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void getAvailableFoods(DataCallback<List<Food>> callback) {
        firestore.collection("foods")
                .whereEqualTo("isAvailable", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Food> foods = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Food food = document.toObject(Food.class);
                        food.setFoodId(document.getId());
                        foods.add(food);
                    }
                    callback.onSuccess(foods);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}
