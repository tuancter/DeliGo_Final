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
import com.deligo.app.repositories.CartRepository;
import com.deligo.app.repositories.CartRepositoryImpl;
import com.deligo.app.utils.CurrencyUtils;
import com.deligo.app.utils.UIHelper;
import android.widget.Button;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class FoodDetailActivity extends AppCompatActivity {
    private ImageView foodImageView;
    private TextView foodNameTextView;
    private TextView foodPriceTextView;
    private TextView foodDescriptionTextView;
    private TextView averageRatingTextView;
    private RecyclerView reviewsRecyclerView;
    private TextView noReviewsTextView;
    private Button addToCartButton;
    private Button backButton;
    private ProgressBar progressBar;

    private ReviewAdapter reviewAdapter;
    private FoodRepository foodRepository;
    private ReviewRepository reviewRepository;
    private CartRepository cartRepository;
    private FirebaseAuth firebaseAuth;
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
        backButton = findViewById(R.id.backButton);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupRepositories() {
        foodRepository = new FoodRepositoryImpl();
        reviewRepository = new ReviewRepositoryImpl();
        cartRepository = new CartRepositoryImpl();
        firebaseAuth = FirebaseAuth.getInstance();
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
        foodPriceTextView.setText(CurrencyUtils.formatVND(food.getPrice()));
        foodDescriptionTextView.setText(food.getDescription());

        // Load image using Glide
        Glide.with(this)
                .load(food.getImageUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .centerCrop()
                .into(foodImageView);

        // Setup add to cart button
        addToCartButton.setOnClickListener(v -> addToCart());

        // Setup back button
        backButton.setOnClickListener(v -> finish());
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

    private void addToCart() {
        // Check if user is authenticated
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            UIHelper.showErrorToast(this, "Please login to add items to cart");
            return;
        }

        // Check if food is loaded
        if (currentFood == null || foodId == null) {
            UIHelper.showErrorToast(this, "Food information not available");
            return;
        }

        // Check if food is available
        if (!currentFood.isAvailable()) {
            UIHelper.showErrorSnackbar(findViewById(android.R.id.content), 
                getString(R.string.error_item_out_of_stock));
            return;
        }

        // Show loading
        UIHelper.showLoading(progressBar, true);
        addToCartButton.setEnabled(false);

        String userId = currentUser.getUid();
        int quantity = 1; // Default quantity
        String note = ""; // No note by default

        cartRepository.addToCart(userId, foodId, quantity, note, new CartRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                UIHelper.showLoading(progressBar, false);
                addToCartButton.setEnabled(true);
                UIHelper.showSuccessSnackbar(findViewById(android.R.id.content), 
                    "Added " + currentFood.getName() + " to cart");
            }

            @Override
            public void onError(String message) {
                UIHelper.showLoading(progressBar, false);
                addToCartButton.setEnabled(true);
                String friendlyMessage = UIHelper.getFirestoreErrorMessage(message);
                UIHelper.showErrorSnackbar(findViewById(android.R.id.content), 
                    friendlyMessage, v -> addToCart());
            }
        });
    }
}
