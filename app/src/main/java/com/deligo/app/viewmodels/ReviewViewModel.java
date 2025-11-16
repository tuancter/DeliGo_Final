package com.deligo.app.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.deligo.app.models.Review;
import com.deligo.app.repositories.ReviewRepository;

import java.util.List;

public class ReviewViewModel extends ViewModel {
    private final MutableLiveData<List<Review>> reviews = new MutableLiveData<>();
    private final MutableLiveData<Double> averageRating = new MutableLiveData<>();
    private final MutableLiveData<Boolean> canReview = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<Boolean> reviewSubmitted = new MutableLiveData<>();

    private final ReviewRepository reviewRepository;

    public ReviewViewModel(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
        isLoading.setValue(false);
        reviewSubmitted.setValue(false);
    }

    public LiveData<List<Review>> getReviews() {
        return reviews;
    }

    public LiveData<Double> getAverageRating() {
        return averageRating;
    }

    public LiveData<Boolean> getCanReview() {
        return canReview;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<Boolean> getReviewSubmitted() {
        return reviewSubmitted;
    }

    public void loadReviews(String foodId) {
        isLoading.setValue(true);
        reviewRepository.getReviewsByFood(foodId, new ReviewRepository.DataCallback<List<Review>>() {
            @Override
            public void onSuccess(List<Review> data) {
                isLoading.setValue(false);
                reviews.setValue(data);
            }

            @Override
            public void onError(String message) {
                isLoading.setValue(false);
                errorMessage.setValue(message);
            }
        });
    }

    public void loadAverageRating(String foodId) {
        reviewRepository.getAverageRating(foodId, new ReviewRepository.DataCallback<Double>() {
            @Override
            public void onSuccess(Double data) {
                averageRating.setValue(data);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
            }
        });
    }

    public void checkCanReview(String userId, String foodId) {
        reviewRepository.canUserReviewFood(userId, foodId, new ReviewRepository.DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean data) {
                canReview.setValue(data);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
            }
        });
    }

    public void submitReview(String userId, String userName, String foodId, int rating, String comment) {
        isLoading.setValue(true);
        reviewSubmitted.setValue(false);
        reviewRepository.addReview(userId, userName, foodId, rating, comment, new ReviewRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                isLoading.setValue(false);
                reviewSubmitted.setValue(true);
                // Reload reviews and average rating
                loadReviews(foodId);
                loadAverageRating(foodId);
            }

            @Override
            public void onError(String message) {
                isLoading.setValue(false);
                errorMessage.setValue(message);
            }
        });
    }
}
