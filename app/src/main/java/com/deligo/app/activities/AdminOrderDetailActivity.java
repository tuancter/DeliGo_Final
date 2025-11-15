package com.deligo.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.deligo.app.R;
import com.deligo.app.adapters.OrderDetailAdapter;
import com.deligo.app.models.Order;
import com.deligo.app.utils.ViewModelFactory;
import com.deligo.app.viewmodels.AdminOrderViewModel;
import androidx.appcompat.widget.Toolbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AdminOrderDetailActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private TextView tvOrderId, tvCustomerId, tvOrderDate, tvOrderStatus, tvPaymentStatus;
    private TextView tvPaymentMethod, tvDeliveryAddress, tvNote, tvTotalAmount;
    private RecyclerView rvOrderDetails;
    private LinearLayout layoutButtons;
    private Button btnAccept, btnPreparing, btnComplete, btnCancel;
    private ProgressBar progressBar;

    private AdminOrderViewModel viewModel;
    private OrderDetailAdapter adapter;
    private String orderId;
    private Order currentOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_order_detail);

        // Get order ID from intent
        orderId = getIntent().getStringExtra("orderId");
        if (orderId == null) {
            Toast.makeText(this, "Order ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupViewModel();
        setupButtons();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvOrderId = findViewById(R.id.tvOrderId);
        tvCustomerId = findViewById(R.id.tvCustomerId);
        tvOrderDate = findViewById(R.id.tvOrderDate);
        tvOrderStatus = findViewById(R.id.tvOrderStatus);
        tvPaymentStatus = findViewById(R.id.tvPaymentStatus);
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
        tvDeliveryAddress = findViewById(R.id.tvDeliveryAddress);
        tvNote = findViewById(R.id.tvNote);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        rvOrderDetails = findViewById(R.id.rvOrderDetails);
        layoutButtons = findViewById(R.id.layoutButtons);
        btnAccept = findViewById(R.id.btnAccept);
        btnPreparing = findViewById(R.id.btnPreparing);
        btnComplete = findViewById(R.id.btnComplete);
        btnCancel = findViewById(R.id.btnCancel);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new OrderDetailAdapter();
        rvOrderDetails.setLayoutManager(new LinearLayoutManager(this));
        rvOrderDetails.setAdapter(adapter);
    }

    private void setupViewModel() {
        ViewModelFactory factory = new ViewModelFactory();
        viewModel = new ViewModelProvider(this, factory).get(AdminOrderViewModel.class);

        // Observe selected order
        viewModel.getSelectedOrder().observe(this, order -> {
            if (order != null) {
                currentOrder = order;
                displayOrderInfo(order);
                configureButtons(order);
            }
        });

        // Observe order details
        viewModel.getOrderDetails().observe(this, orderDetails -> {
            if (orderDetails != null) {
                adapter.setOrderDetails(orderDetails);
            }
        });

        // Observe loading state
        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        // Observe errors
        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

        // Observe action success
        viewModel.getActionSuccess().observe(this, success -> {
            if (success != null && success) {
                Toast.makeText(this, "Order updated successfully", Toast.LENGTH_SHORT).show();
                viewModel.resetActionSuccess();
                // Reload order details
                viewModel.loadOrderDetails(orderId);
            }
        });

        // Load order details
        viewModel.loadOrderDetails(orderId);
    }

    private void displayOrderInfo(Order order) {
        tvOrderId.setText("Order ID: #" + order.getOrderId().substring(0, Math.min(8, order.getOrderId().length())));
        tvCustomerId.setText("Customer ID: " + order.getCustomerId().substring(0, Math.min(8, order.getCustomerId().length())));

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        tvOrderDate.setText("Date: " + dateFormat.format(new Date(order.getCreatedAt())));

        tvOrderStatus.setText("Status: " + capitalizeFirst(order.getOrderStatus()));
        tvPaymentStatus.setText("Payment: " + capitalizeFirst(order.getPaymentStatus()));
        tvPaymentMethod.setText("Payment Method: " + order.getPaymentMethod());
        tvDeliveryAddress.setText("Address: " + order.getDeliveryAddress());

        if (order.getNote() != null && !order.getNote().isEmpty()) {
            tvNote.setText("Note: " + order.getNote());
            tvNote.setVisibility(View.VISIBLE);
        } else {
            tvNote.setVisibility(View.GONE);
        }

        tvTotalAmount.setText(String.format("$%.2f", order.getTotalAmount()));
    }

    private void configureButtons(Order order) {
        String status = order.getOrderStatus();

        // Hide all buttons first
        btnAccept.setVisibility(View.GONE);
        btnPreparing.setVisibility(View.GONE);
        btnComplete.setVisibility(View.GONE);
        btnCancel.setVisibility(View.GONE);

        // Show appropriate buttons based on status
        switch (status.toLowerCase()) {
            case "pending":
                btnAccept.setVisibility(View.VISIBLE);
                btnCancel.setVisibility(View.VISIBLE);
                break;

            case "accepted":
                btnPreparing.setVisibility(View.VISIBLE);
                btnCancel.setVisibility(View.VISIBLE);
                break;

            case "preparing":
                btnComplete.setVisibility(View.VISIBLE);
                btnCancel.setVisibility(View.VISIBLE);
                break;

            case "completed":
            case "cancelled":
                // No buttons for final states
                layoutButtons.setVisibility(View.GONE);
                break;
        }
    }

    private void setupButtons() {
        btnAccept.setOnClickListener(v -> {
            if (currentOrder != null) {
                viewModel.acceptOrder(currentOrder.getOrderId());
            }
        });

        btnPreparing.setOnClickListener(v -> {
            if (currentOrder != null) {
                viewModel.updateOrderStatus(currentOrder.getOrderId(), "preparing");
            }
        });

        btnComplete.setOnClickListener(v -> {
            if (currentOrder != null) {
                viewModel.updateOrderStatus(currentOrder.getOrderId(), "completed");
            }
        });

        btnCancel.setOnClickListener(v -> {
            if (currentOrder != null) {
                viewModel.updateOrderStatus(currentOrder.getOrderId(), "cancelled");
            }
        });
    }

    private String capitalizeFirst(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }
}
