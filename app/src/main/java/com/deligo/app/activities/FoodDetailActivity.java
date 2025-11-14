package com.deligo.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.deligo.app.R;
import com.deligo.app.adapters.ReviewAdapter;
import com.deligo.app.models.Food;
import com.deligo.app.models.Review;
import com.deligo.app.repositories.FoodRepository;
import com.deligo.app.repositories.FoodRepositoryImpl;
import com.deligo.app.repositories.ReviewRepository;
import com.deligo.app.repositories.ReviewRepositoryImpl;
import com.deligo.app.utils.UIHelper;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class FoodDetailActivity extends AppCompatActivity {
    private ImageView foodImageView;
    private TextView foodNameTextView;
    private TextView foodPriceTextView;
    private TextView foodDescriptionTextView;
    private TextView averageRatingTextView;
    private RecyclerView reviewsRecyclerView;
    private TextView noReviewsTextView;
    private MaterialButton addToCartButton;
    private MaterialButton writeReviewButton;
    private ProgressBar progressBar;

    private ReviewAdapter reviewAdapter;
    private FoodRepository foodRepository;
    private ReviewRepository reviewRepository;
    private String foodId;
    private Food currentFood;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);

        initViews();
        setupRepositories();
        setupRecyclerView();

        // Get food ID from intent
        foodId = getIntent().getStringExtra("foodId");
        if (foodId == null || foodId.isEmpty()) {
            UIHelper.showErrorToast(this, "Invalid food item");
            finish();
            return;
        }

        loadFoodDetails();
        loadReviews();
        loadAverageRating();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload reviews and rating when returning from AddReviewActivity
        if (foodId != null && !foodId.isEmpty()) {
            loadReviews();
            loadAverageRating();
        }
    }

    private void initViews() {
        foodImageView = findViewById(R.id.foodImageView);
        foodNameTextView = findViewById(R.id.foodNameTextView);
        foodPriceTextView = findViewById(R.id.foodPriceTextView);
        foodDescriptionTextView = findViewById(R.id.foodDescriptionTextView);
        averageRatingTextView = findViewById(R.id.averageRatingTextView);
        reviewsRecyclerView = findViewById(R.id.reviewsRecyclerView);
        noReviewsTextView = findViewById(R.id.noReviewsTextView);
        addToCartButton = findViewById(R.id.addToCartButton);
        writeReviewButton = findViewById(R.id.writeReviewButton);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupRepositories() {
        foodRepository = new FoodRepositoryImpl();
        reviewRepository = new ReviewRepositoryImpl();
    }

    private void setupRecyclerView() {
        reviewAdapter = new ReviewAdapter();
        reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reviewsRecyclerView.setAdapter(reviewAdapter);
    }

    private void loadFoodDetails() {
        UIHelper.showLoading(progressBar, true);
        foodRepository.getFoodById(foodId, new FoodRepository.DataCallback<Food>() {
            @Override
            public void onSuccess(Food food) {
                UIHelper.showLoading(progressBar, false);
                if (food != null) {
                    currentFood = food;
                    displayFoodDetails(food);
                } else {
                    UIHelper.showErrorToast(FoodDetailActivity.this, "Food not found");
                    finish();
                }
            }

            @Override
            public void onError(String message) {
                UIHelper.showLoading(progressBar, false);
                String friendlyMessage = UIHelper.getFirestoreErrorMessage(message);
                UIHelper.showErrorSnackbar(findViewById(android.R.id.content), 
                    friendlyMessage, v -> loadFoodDetails());
            }
        });
    }

    private void displayFoodDetails(Food food) {
        foodNameTextView.setText(food.getName());
        foodPriceTextView.setText(String.format("$%.2f", food.getPrice()));
        foodDescriptionTextView.setText(food.getDescription());

        // Load image using Glide
        Glide.with(this)
                .load(food.getImageUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .centerCrop()
                .into(foodImageView);

        // Setup add to cart button
        addToCartButton.setOnClickListener(v -> {
            // TODO: Implement add to cart functionality in future task
            UIHelper.showSuccessToast(this, "Add to cart functionality coming soon");
        });

        // Setup write review button
        writeReviewButton.setOnClickListener(v -> openAddReviewActivity());
    }

    // Method to open AddReviewActivity
    private void openAddReviewActivity() {
        android.content.Intent intent = new android.content.Intent(this, AddReviewActivity.class);
        intent.putExtra("foodId", foodId);
        startActivity(intent);
    }

    private void loadReviews() {
        reviewRepository.getReviewsByFood(foodId, new ReviewRepository.DataCallback<List<Review>>() {
            @Override
            public void onSuccess(List<Review> reviews) {
                if (reviews != null && !reviews.isEmpty()) {
                    reviewAdapter.setReviewList(reviews);
                    reviewsRecyclerView.setVisibility(View.VISIBLE);
                    noReviewsTextView.setVisibility(View.GONE);
                } else {
                    reviewsRecyclerView.setVisibility(View.GONE);
                    noReviewsTextView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onError(String message) {
                String friendlyMessage = UIHelper.getFirestoreErrorMessage(message);
                UIHelper.showErrorSnackbar(findViewById(android.R.id.content), 
                    "Error loading reviews: " + friendlyMessage);
            }
        });
    }

    private void loadAverageRating() {
        reviewRepository.getAverageRating(foodId, new ReviewRepository.DataCallback<Double>() {
            @Override
            public void onSuccess(Double rating) {
                if (rating != null && rating > 0) {
                    averageRatingTextView.setText(String.format("%.1f ⭐", rating));
                } else {
                    averageRatingTextView.setText("No ratings yet");
                }
            }

            @Override
            public void onError(String message) {
                averageRatingTextView.setText("N/A");
            }
        });
    }
}
