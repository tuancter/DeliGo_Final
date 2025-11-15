package com.deligo.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.deligo.app.R;
import com.deligo.app.models.Food;
import com.deligo.app.repositories.FoodRepository;
import com.deligo.app.repositories.FoodRepositoryImpl;
import com.deligo.app.repositories.ReviewRepositoryImpl;
import com.deligo.app.utils.ViewModelFactory;
import com.deligo.app.viewmodels.ReviewViewModel;
import android.widget.Button;
import android.widget.EditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AddReviewActivity extends AppCompatActivity {
    private ImageView foodImageView;
    private TextView foodNameTextView;
    private TextView foodPriceTextView;
    private RatingBar ratingBar;
    private TextView ratingTextView;
    private EditText commentEditText;
    private Button submitButton;
    private ProgressBar progressBar;

    private ReviewViewModel reviewViewModel;
    private FoodRepository foodRepository;
    private String foodId;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_review);

        initViews();
        setupViewModel();
        setupRepositories();

        // Get current user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please login to submit a review", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        userId = currentUser.getUid();

        // Get food ID from intent
        foodId = getIntent().getStringExtra("foodId");
        if (foodId == null || foodId.isEmpty()) {
            Toast.makeText(this, "Invalid food item", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadFoodDetails();
        setupListeners();
        observeViewModel();
    }

    private void initViews() {
        foodImageView = findViewById(R.id.foodImageView);
        foodNameTextView = findViewById(R.id.foodNameTextView);
        foodPriceTextView = findViewById(R.id.foodPriceTextView);
        ratingBar = findViewById(R.id.ratingBar);
        ratingTextView = findViewById(R.id.ratingTextView);
        commentEditText = findViewById(R.id.commentEditText);
        submitButton = findViewById(R.id.submitButton);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupViewModel() {
        ViewModelFactory factory = new ViewModelFactory();
        reviewViewModel = new ViewModelProvider(this, factory).get(ReviewViewModel.class);
    }

    private void setupRepositories() {
        foodRepository = new FoodRepositoryImpl();
    }

    private void setupListeners() {
        // Update rating text when rating changes
        ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            ratingTextView.setText(String.format("%.0f/5", rating));
        });

        // Submit button click
        submitButton.setOnClickListener(v -> submitReview());
    }

    private void observeViewModel() {
        reviewViewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null && isLoading) {
                progressBar.setVisibility(View.VISIBLE);
                submitButton.setEnabled(false);
            } else {
                progressBar.setVisibility(View.GONE);
                submitButton.setEnabled(true);
            }
        });

        reviewViewModel.getReviewSubmitted().observe(this, submitted -> {
            if (submitted != null && submitted) {
                Toast.makeText(this, "Review submitted successfully!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        reviewViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFoodDetails() {
        progressBar.setVisibility(View.VISIBLE);
        foodRepository.getFoodById(foodId, new FoodRepository.DataCallback<Food>() {
            @Override
            public void onSuccess(Food food) {
                progressBar.setVisibility(View.GONE);
                if (food != null) {
                    displayFoodDetails(food);
                } else {
                    Toast.makeText(AddReviewActivity.this, "Food not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onError(String message) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AddReviewActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayFoodDetails(Food food) {
        foodNameTextView.setText(food.getName());
        foodPriceTextView.setText(String.format("$%.2f", food.getPrice()));

        // Load image using Glide
        Glide.with(this)
                .load(food.getImageUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .centerCrop()
                .into(foodImageView);
    }

    private void submitReview() {
        // Validate rating
        float rating = ratingBar.getRating();
        if (rating == 0) {
            Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get comment (optional)
        String comment = "";
        if (commentEditText.getText() != null) {
            comment = commentEditText.getText().toString().trim();
        }

        // Submit review
        reviewViewModel.submitReview(userId, foodId, (int) rating, comment);
    }
}
