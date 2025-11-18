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
import com.deligo.app.constants.OrderStatus;
import com.deligo.app.constants.PaymentStatus;
import com.deligo.app.models.Order;
import com.deligo.app.utils.CurrencyUtils;
import com.deligo.app.utils.ViewModelFactory;
import com.deligo.app.viewmodels.AdminOrderViewModel;
import androidx.appcompat.widget.Toolbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AdminOrderDetailActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private TextView tvOrderId, tvCustomerId, tvOrderDate, tvOrderStatus, tvPaymentStatus;
    private TextView tvPaymentMethod, tvPhoneNumber, tvDeliveryAddress, tvNote, tvTotalAmount;
    private RecyclerView rvOrderDetails;
    private LinearLayout layoutButtons, layoutPaymentButtons;
    private Button btnAccept, btnPreparing, btnComplete, btnCancel;
    private Button btnPaymentCompleted, btnPaymentFailed;
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
            Toast.makeText(this, getString(R.string.error_order_not_found), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupViewModel();
        setupButtons();
        setupPaymentButtons();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvOrderId = findViewById(R.id.tvOrderId);
        tvCustomerId = findViewById(R.id.tvCustomerId);
        tvOrderDate = findViewById(R.id.tvOrderDate);
        tvOrderStatus = findViewById(R.id.tvOrderStatus);
        tvPaymentStatus = findViewById(R.id.tvPaymentStatus);
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
        tvPhoneNumber = findViewById(R.id.tvPhoneNumber);
        tvDeliveryAddress = findViewById(R.id.tvDeliveryAddress);
        tvNote = findViewById(R.id.tvNote);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        rvOrderDetails = findViewById(R.id.rvOrderDetails);
        layoutButtons = findViewById(R.id.layoutButtons);
        layoutPaymentButtons = findViewById(R.id.layoutPaymentButtons);
        btnAccept = findViewById(R.id.btnAccept);
        btnPreparing = findViewById(R.id.btnPreparing);
        btnComplete = findViewById(R.id.btnComplete);
        btnCancel = findViewById(R.id.btnCancel);
        btnPaymentCompleted = findViewById(R.id.btnPaymentCompleted);
        btnPaymentFailed = findViewById(R.id.btnPaymentFailed);
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
                Toast.makeText(this, getString(R.string.toast_order_updated), Toast.LENGTH_SHORT).show();
                viewModel.resetActionSuccess();
                // Reload order details
                viewModel.loadOrderDetails(orderId);
            }
        });

        // Load order details
        viewModel.loadOrderDetails(orderId);
    }

    private void displayOrderInfo(Order order) {
        tvOrderId.setText(getString(R.string.label_order_id, order.getOrderId().substring(0, Math.min(8, order.getOrderId().length()))));
        tvCustomerId.setText(getString(R.string.label_customer_id, order.getCustomerId().substring(0, Math.min(8, order.getCustomerId().length()))));

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        tvOrderDate.setText(getString(R.string.label_date, dateFormat.format(new Date(order.getCreatedAt()))));

        tvOrderStatus.setText(getString(R.string.label_status, capitalizeFirst(order.getOrderStatus())));
        tvPaymentStatus.setText(getString(R.string.label_payment, capitalizeFirst(order.getPaymentStatus())));
        tvPaymentMethod.setText(getString(R.string.label_payment_method, order.getPaymentMethod()));
        
        // Phone Number
        if (order.getPhoneNumber() != null && !order.getPhoneNumber().isEmpty()) {
            tvPhoneNumber.setText(getString(R.string.phone_number) + order.getPhoneNumber());
            tvPhoneNumber.setVisibility(View.VISIBLE);
        } else {
            tvPhoneNumber.setVisibility(View.GONE);
        }
        
        tvDeliveryAddress.setText(getString(R.string.label_address, order.getDeliveryAddress()));

        if (order.getNote() != null && !order.getNote().isEmpty()) {
            tvNote.setText(getString(R.string.label_note, order.getNote()));
            tvNote.setVisibility(View.VISIBLE);
        } else {
            tvNote.setVisibility(View.GONE);
        }

        tvTotalAmount.setText(CurrencyUtils.formatVND(order.getTotalAmount()));
        
        // Show payment buttons only if payment status is pending
        String paymentStatus = order.getPaymentStatus();
        if (paymentStatus != null && PaymentStatus.PENDING.matches(paymentStatus)) {
            layoutPaymentButtons.setVisibility(View.VISIBLE);
        } else {
            layoutPaymentButtons.setVisibility(View.GONE);
        }
    }

    private void configureButtons(Order order) {
        String status = order.getOrderStatus();

        // Hide all buttons first
        btnAccept.setVisibility(View.GONE);
        btnPreparing.setVisibility(View.GONE);
        btnComplete.setVisibility(View.GONE);
        btnCancel.setVisibility(View.GONE);

        // Show appropriate buttons based on status (Vietnamese)
        if (status == null) return;
        
        if (OrderStatus.PENDING.matches(status)) {
            btnAccept.setVisibility(View.VISIBLE);
            btnCancel.setVisibility(View.VISIBLE);
        } else if (OrderStatus.ACCEPTED.matches(status)) {
            btnPreparing.setVisibility(View.VISIBLE);
            btnCancel.setVisibility(View.VISIBLE);
        } else if (OrderStatus.PREPARING.matches(status)) {
            btnComplete.setVisibility(View.VISIBLE);
            btnCancel.setVisibility(View.VISIBLE);
        } else if (OrderStatus.COMPLETED.matches(status) || OrderStatus.CANCELLED.matches(status)) {
            // No buttons for final states
            layoutButtons.setVisibility(View.GONE);
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
                viewModel.updateOrderStatus(currentOrder.getOrderId(), OrderStatus.PREPARING.getVietnameseName());
            }
        });

        btnComplete.setOnClickListener(v -> {
            if (currentOrder != null) {
                viewModel.updateOrderStatus(currentOrder.getOrderId(), OrderStatus.COMPLETED.getVietnameseName());
            }
        });

        btnCancel.setOnClickListener(v -> {
            if (currentOrder != null) {
                new androidx.appcompat.app.AlertDialog.Builder(this, R.style.Theme_DeliGo_Dialog_Alert)
                        .setTitle(getString(R.string.confirm_cancel_order))
                        .setMessage(getString(R.string.confirm_cancel_order_message))
                        .setPositiveButton(getString(R.string.action_confirm), (dialog, which) -> {
                            // Update order status to cancelled and payment status to failed
                            viewModel.updateOrderStatus(currentOrder.getOrderId(), OrderStatus.CANCELLED.getVietnameseName());
                            viewModel.updatePaymentStatus(currentOrder.getOrderId(), PaymentStatus.CANCELLED.getVietnameseName());
                        })
                        .setNegativeButton(getString(R.string.action_cancel), null)
                        .show();
            }
        });
    }

    private void setupPaymentButtons() {
        btnPaymentCompleted.setOnClickListener(v -> {
            if (currentOrder != null) {
                showPaymentConfirmationDialog("completed", getString(R.string.confirm_payment_completed));
            }
        });

        btnPaymentFailed.setOnClickListener(v -> {
            if (currentOrder != null) {
                showPaymentConfirmationDialog("cancelled", getString(R.string.confirm_payment_failed));
            }
        });
    }

    private void showPaymentConfirmationDialog(String paymentStatus, String message) {
        // Get Vietnamese status from enum
        String vietnameseStatus = paymentStatus;
        if ("completed".equals(paymentStatus)) {
            vietnameseStatus = PaymentStatus.COMPLETED.getVietnameseName();
        } else if ("cancelled".equals(paymentStatus)) {
            vietnameseStatus = PaymentStatus.CANCELLED.getVietnameseName();
        }
        
        String finalStatus = vietnameseStatus;
        new androidx.appcompat.app.AlertDialog.Builder(this, R.style.Theme_DeliGo_Dialog_Alert)
                .setTitle(getString(R.string.confirm_payment_update))
                .setMessage(message)
                .setPositiveButton(getString(R.string.action_confirm), (dialog, which) -> {
                    viewModel.updatePaymentStatus(currentOrder.getOrderId(), finalStatus);
                })
                .setNegativeButton(getString(R.string.action_cancel), null)
                .show();
    }

    private String capitalizeFirst(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }
}
