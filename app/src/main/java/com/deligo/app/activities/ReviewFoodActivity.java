package com.deligo.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.deligo.app.R;
import com.deligo.app.adapters.ReviewFoodAdapter;
import com.deligo.app.models.OrderDetail;
import com.deligo.app.utils.ViewModelFactory;
import com.deligo.app.viewmodels.OrderViewModel;

import java.util.List;

public class ReviewFoodActivity extends AppCompatActivity implements ReviewFoodAdapter.OnReviewClickListener {
    private RecyclerView recyclerViewFoods;
    private ProgressBar progressBar;
    private TextView tvEmptyState;

    private OrderViewModel orderViewModel;
    private ReviewFoodAdapter reviewFoodAdapter;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_food);

        // Get order ID from intent
        orderId = getIntent().getStringExtra("orderId");
        if (orderId == null) {
            Toast.makeText(this, getString(R.string.error_order_not_found), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupViewModel();
        setupRecyclerView();
        observeViewModel();

        // Load order details
        orderViewModel.loadOrderDetails(orderId);
    }

    private void initViews() {
        recyclerViewFoods = findViewById(R.id.recyclerViewFoods);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupViewModel() {
        ViewModelFactory factory = new ViewModelFactory();
        orderViewModel = new ViewModelProvider(this, factory).get(OrderViewModel.class);
    }

    private void setupRecyclerView() {
        reviewFoodAdapter = new ReviewFoodAdapter(this);
        recyclerViewFoods.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewFoods.setAdapter(reviewFoodAdapter);
    }

    private void observeViewModel() {
        orderViewModel.getOrderDetails().observe(this, orderDetails -> {
            if (orderDetails != null && !orderDetails.isEmpty()) {
                reviewFoodAdapter.setOrderDetails(orderDetails);
                recyclerViewFoods.setVisibility(View.VISIBLE);
                tvEmptyState.setVisibility(View.GONE);
            } else {
                recyclerViewFoods.setVisibility(View.GONE);
                tvEmptyState.setVisibility(View.VISIBLE);
            }
        });

        orderViewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        orderViewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onReviewClick(OrderDetail orderDetail) {
        if (orderDetail.getFood() != null) {
            Intent intent = new Intent(this, AddReviewActivity.class);
            intent.putExtra("foodId", orderDetail.getFoodId());
            startActivity(intent);
        } else {
            Toast.makeText(this, getString(R.string.error_food_not_found), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload order details when returning from AddReviewActivity
        if (orderId != null) {
            orderViewModel.loadOrderDetails(orderId);
        }
    }
}
