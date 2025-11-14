package com.deligo.app.models;

public class Review {
    private String reviewId;
    private String userId;
    private String foodId;
    private int rating;
    private String comment;
    private long createdAt;
    private User user; // Populated when retrieved

    // Required empty constructor for Firestore
    public Review() {
    }

    public Review(String reviewId, String userId, String foodId, int rating, String comment, long createdAt) {
        this.reviewId = reviewId;
        this.userId = userId;
        this.foodId = foodId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFoodId() {
        return foodId;
    }

    public void setFoodId(String foodId) {
        this.foodId = foodId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
