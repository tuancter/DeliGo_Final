package com.deligo.app.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.deligo.app.R;
import com.deligo.app.adapters.OrderDetailAdapter;
import com.deligo.app.repositories.CartRepositoryImpl;
import com.deligo.app.repositories.OrderRepositoryImpl;
import com.deligo.app.utils.ViewModelFactory;
import com.deligo.app.viewmodels.OrderViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class OrderDetailActivity extends AppCompatActivity {
    private TextView tvOrderId, tvOrderDate, tvOrderStatus, tvPaymentStatus, tvPaymentMethod;
    private TextView tvDeliveryAddress, tvTotalAmount;
    private RecyclerView orderItemsRecyclerView;
    private Button btnSubmitComplaint;
    private ProgressBar progressBar;

    private OrderViewModel orderViewModel;
    private OrderDetailAdapter orderDetailAdapter;
    private String orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        // Get order ID from intent
        orderId = getIntent().getStringExtra("orderId");
        if (orderId == null) {
            Toast.makeText(this, "Order ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupViewModel();
        setupRecyclerView();
        setupListeners();
        observeViewModel();

        // Load order details
        orderViewModel.loadOrderDetails(orderId);
    }

    private void initViews() {
        tvOrderId = findViewById(R.id.tvOrderId);
        tvOrderDate = findViewById(R.id.tvOrderDate);
        tvOrderStatus = findViewById(R.id.tvOrderStatus);
        tvPaymentStatus = findViewById(R.id.tvPaymentStatus);
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
        tvDeliveryAddress = findViewById(R.id.tvDeliveryAddress);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        orderItemsRecyclerView = findViewById(R.id.orderItemsRecyclerView);
        btnSubmitComplaint = findViewById(R.id.btnSubmitComplaint);
        progressBar = findViewById(R.id.progressBar);
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
        ViewModelFactory factory = new ViewModelFactory(
                null,
                null,
                null,
                new CartRepositoryImpl(),
                new OrderRepositoryImpl()
        );
        orderViewModel = new ViewModelProvider(this, factory).get(OrderViewModel.class);
    }

    private void setupRecyclerView() {
        orderDetailAdapter = new OrderDetailAdapter();
        orderItemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        orderItemsRecyclerView.setAdapter(orderDetailAdapter);
    }

    private void setupListeners() {
        btnSubmitComplaint.setOnClickListener(v -> {
            Intent intent = new Intent(OrderDetailActivity.this, SubmitComplaintActivity.class);
            intent.putExtra("orderId", orderId);
            startActivity(intent);
        });
    }

    private void observeViewModel() {
        orderViewModel.getCurrentOrder().observe(this, order -> {
            if (order != null) {
                // Order ID
                tvOrderId.setText("#" + order.getOrderId().substring(0, Math.min(8, order.getOrderId().length())));

                // Order Date
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
                String dateStr = dateFormat.format(new Date(order.getCreatedAt()));
                tvOrderDate.setText(dateStr);

                // Order Status
                tvOrderStatus.setText(capitalizeFirst(order.getOrderStatus()));
                tvOrderStatus.setTextColor(getStatusColor(order.getOrderStatus()));

                // Payment Status
                tvPaymentStatus.setText(capitalizeFirst(order.getPaymentStatus()));
                tvPaymentStatus.setTextColor(getStatusColor(order.getPaymentStatus()));

                // Payment Method
                tvPaymentMethod.setText(order.getPaymentMethod());

                // Delivery Address
                tvDeliveryAddress.setText(order.getDeliveryAddress());

                // Total Amount
                tvTotalAmount.setText(String.format("$%.2f", order.getTotalAmount()));

                // Show complaint button only for completed orders
                if ("completed".equalsIgnoreCase(order.getOrderStatus())) {
                    btnSubmitComplaint.setVisibility(View.VISIBLE);
                } else {
                    btnSubmitComplaint.setVisibility(View.GONE);
                }
            }
        });

        orderViewModel.getOrderDetails().observe(this, orderDetails -> {
            if (orderDetails != null) {
                orderDetailAdapter.setOrderDetails(orderDetails);
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

    private String capitalizeFirst(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    private int getStatusColor(String status) {
        switch (status.toLowerCase()) {
            case "pending":
                return Color.parseColor("#FF9800"); // Orange
            case "accepted":
            case "preparing":
                return Color.parseColor("#2196F3"); // Blue
            case "completed":
                return Color.parseColor("#4CAF50"); // Green
            case "cancelled":
            case "failed":
                return Color.parseColor("#F44336"); // Red
            default:
                return Color.parseColor("#757575"); // Gray
        }
    }
}
