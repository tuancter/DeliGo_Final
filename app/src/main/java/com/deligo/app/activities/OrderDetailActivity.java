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
import com.deligo.app.repositories.ProfileRepositoryImpl;
import com.deligo.app.utils.CurrencyUtils;
import com.deligo.app.utils.ViewModelFactory;
import com.deligo.app.viewmodels.OrderViewModel;
import com.deligo.app.viewmodels.ProfileViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class OrderDetailActivity extends AppCompatActivity {
    private TextView tvOrderId, tvOrderDate, tvOrderStatus, tvPaymentStatus, tvPaymentMethod;
    private TextView tvDeliveryAddress, tvTotalAmount;
    private RecyclerView orderItemsRecyclerView;
    private Button btnReviewProducts;
    private Button btnSubmitComplaint;
    private ProgressBar progressBar;

    private OrderViewModel orderViewModel;
    private ProfileViewModel profileViewModel;
    private OrderDetailAdapter orderDetailAdapter;
    private String orderId;
    private String customerName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

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
        setupListeners();
        observeViewModel();

        // Load order details
        orderViewModel.loadOrderDetails(orderId);
    }

    private Button btnBankTransferInfo;

    private void initViews() {
        tvOrderId = findViewById(R.id.tvOrderId);
        tvOrderDate = findViewById(R.id.tvOrderDate);
        tvOrderStatus = findViewById(R.id.tvOrderStatus);
        tvPaymentStatus = findViewById(R.id.tvPaymentStatus);
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
        tvDeliveryAddress = findViewById(R.id.tvDeliveryAddress);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        orderItemsRecyclerView = findViewById(R.id.orderItemsRecyclerView);
        btnBankTransferInfo = findViewById(R.id.btnBankTransferInfo);
        btnReviewProducts = findViewById(R.id.btnReviewProducts);
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
        ViewModelFactory factory = new ViewModelFactory();
        orderViewModel = new ViewModelProvider(this, factory).get(OrderViewModel.class);
        profileViewModel = new ViewModelProvider(this, factory).get(ProfileViewModel.class);
        
        // Load user profile to get customer name
        profileViewModel.loadProfile();
    }

    private void setupRecyclerView() {
        orderDetailAdapter = new OrderDetailAdapter();
        orderItemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        orderItemsRecyclerView.setAdapter(orderDetailAdapter);
    }

    private void setupListeners() {
        btnReviewProducts.setOnClickListener(v -> {
            Intent intent = new Intent(OrderDetailActivity.this, ReviewFoodActivity.class);
            intent.putExtra("orderId", orderId);
            startActivity(intent);
        });

        btnSubmitComplaint.setOnClickListener(v -> {
            Intent intent = new Intent(OrderDetailActivity.this, SubmitComplaintActivity.class);
            intent.putExtra("orderId", orderId);
            startActivity(intent);
        });
    }
    
    private void openBankTransferInfo(String customerName, double totalAmount) {
        Intent intent = new Intent(OrderDetailActivity.this, BankTransferActivity.class);
        intent.putExtra("customerName", customerName);
        intent.putExtra("totalAmount", totalAmount);
        startActivity(intent);
    }

    private void observeViewModel() {
        // Observe profile to get customer name
        profileViewModel.getUserProfile().observe(this, user -> {
            if (user != null && user.getFullName() != null) {
                customerName = user.getFullName();
            }
        });
        
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
                tvTotalAmount.setText(CurrencyUtils.formatVND(order.getTotalAmount()));

                // Show bank transfer info button if payment method is bank transfer
                if (order.getPaymentMethod() != null && order.getPaymentMethod().contains("Chuyển khoản")) {
                    btnBankTransferInfo.setVisibility(View.VISIBLE);
                    btnBankTransferInfo.setOnClickListener(v -> {
                        openBankTransferInfo(customerName, order.getTotalAmount());
                    });
                } else {
                    btnBankTransferInfo.setVisibility(View.GONE);
                }

                // Show review and complaint buttons only for completed orders
                if ("completed".equalsIgnoreCase(order.getOrderStatus())) {
                    btnReviewProducts.setVisibility(View.VISIBLE);
                    btnSubmitComplaint.setVisibility(View.VISIBLE);
                } else {
                    btnReviewProducts.setVisibility(View.GONE);
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
        if (status == null) return Color.parseColor("#757575");
        
        String statusLower = status.toLowerCase();
        if (statusLower.contains("chờ") || statusLower.contains("pending")) {
            return Color.parseColor("#FF9800"); // Orange
        } else if (statusLower.contains("nhận") || statusLower.contains("chuẩn bị") || 
                   statusLower.contains("accepted") || statusLower.contains("preparing")) {
            return Color.parseColor("#2196F3"); // Blue
        } else if (statusLower.contains("hoàn thành") || statusLower.contains("completed")) {
            return Color.parseColor("#4CAF50"); // Green
        } else if (statusLower.contains("huỷ") || statusLower.contains("hủy") || 
                   statusLower.contains("cancelled") || statusLower.contains("failed")) {
            return Color.parseColor("#F44336"); // Red
        } else {
            return Color.parseColor("#757575"); // Gray
        }
    }
}
