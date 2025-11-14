package com.deligo.app.repositories;

import com.deligo.app.models.Review;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReviewRepositoryImpl implements ReviewRepository {
    private final FirebaseFirestore firestore;

    public ReviewRepositoryImpl() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    @Override
    public void addReview(String userId, String foodId, int rating, String comment, ActionCallback callback) {
        Map<String, Object> reviewData = new HashMap<>();
        reviewData.put("userId", userId);
        reviewData.put("foodId", foodId);
        reviewData.put("rating", rating);
        reviewData.put("comment", comment);
        reviewData.put("createdAt", System.currentTimeMillis());

        firestore.collection("reviews")
                .add(reviewData)
                .addOnSuccessListener(documentReference -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void getReviewsByFood(String foodId, DataCallback<List<Review>> callback) {
        firestore.collection("reviews")
                .whereEqualTo("foodId", foodId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Review> reviews = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Review review = document.toObject(Review.class);
                        review.setReviewId(document.getId());
                        reviews.add(review);
                    }
                    callback.onSuccess(reviews);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void getAverageRating(String foodId, DataCallback<Double> callback) {
        firestore.collection("reviews")
                .whereEqualTo("foodId", foodId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        callback.onSuccess(0.0);
                        return;
                    }

                    double totalRating = 0;
                    int count = 0;
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Review review = document.toObject(Review.class);
                        totalRating += review.getRating();
                        count++;
                    }

                    double averageRating = count > 0 ? totalRating / count : 0.0;
                    callback.onSuccess(averageRating);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void canUserReviewFood(String userId, String foodId, DataCallback<Boolean> callback) {
        // Check if user has a completed order containing this food item
        firestore.collection("orders")
                .whereEqualTo("customerId", userId)
                .whereEqualTo("orderStatus", "completed")
                .get()
                .addOnSuccessListener(orderSnapshots -> {
                    if (orderSnapshots.isEmpty()) {
                        callback.onSuccess(false);
                        return;
                    }

                    // Check if any completed order contains this food item
                    List<String> orderIds = new ArrayList<>();
                    for (QueryDocumentSnapshot orderDoc : orderSnapshots) {
                        orderIds.add(orderDoc.getId());
                    }

                    // Check order details for this food
                    checkOrderDetailsForFood(orderIds, foodId, callback);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    private void checkOrderDetailsForFood(List<String> orderIds, String foodId, DataCallback<Boolean> callback) {
        if (orderIds.isEmpty()) {
            callback.onSuccess(false);
            return;
        }

        // Check the first order
        String orderId = orderIds.get(0);
        firestore.collection("orders")
                .document(orderId)
                .collection("orderDetails")
                .whereEqualTo("foodId", foodId)
                .get()
                .addOnSuccessListener(detailSnapshots -> {
                    if (!detailSnapshots.isEmpty()) {
                        callback.onSuccess(true);
                    } else if (orderIds.size() > 1) {
                        // Check remaining orders
                        checkOrderDetailsForFood(orderIds.subList(1, orderIds.size()), foodId, callback);
                    } else {
                        callback.onSuccess(false);
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}
