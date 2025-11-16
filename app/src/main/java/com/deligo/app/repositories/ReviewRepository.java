package com.deligo.app.repositories;

import com.deligo.app.models.Review;
import java.util.List;

public interface ReviewRepository {
    void addReview(String userId, String userName, String foodId, int rating, String comment, ActionCallback callback);
    void getReviewsByFood(String foodId, DataCallback<List<Review>> callback);
    void getAverageRating(String foodId, DataCallback<Double> callback);
    void canUserReviewFood(String userId, String foodId, DataCallback<Boolean> callback);
    
    interface DataCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }
    
    interface ActionCallback {
        void onSuccess();
        void onError(String message);
    }
}
