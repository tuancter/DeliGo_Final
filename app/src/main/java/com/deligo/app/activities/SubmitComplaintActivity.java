package com.deligo.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.deligo.app.R;
import com.deligo.app.models.Order;
import com.deligo.app.repositories.OrderRepository;
import com.deligo.app.repositories.OrderRepositoryImpl;
import com.deligo.app.utils.CurrencyUtils;
import com.deligo.app.utils.ViewModelFactory;
import com.deligo.app.viewmodels.ComplaintViewModel;
import android.widget.Button;
import android.widget.EditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SubmitComplaintActivity extends AppCompatActivity {
    private TextView orderIdTextView;
    private TextView orderDateTextView;
    private TextView orderTotalTextView;
    private EditText complaintEditText;
    private Button submitButton;
    private ProgressBar progressBar;

    private ComplaintViewModel complaintViewModel;
    private OrderRepository orderRepository;
    private String orderId;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_complaint);

        initViews();
        setupViewModel();
        setupRepositories();

        // Get current user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, getString(R.string.toast_login_required), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        userId = currentUser.getUid();

        // Get order ID from intent
        orderId = getIntent().getStringExtra("orderId");
        if (orderId == null || orderId.isEmpty()) {
            Toast.makeText(this, getString(R.string.toast_invalid_item), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadOrderDetails();
        setupListeners();
        observeViewModel();
    }

    private void initViews() {
        orderIdTextView = findViewById(R.id.orderIdTextView);
        orderDateTextView = findViewById(R.id.orderDateTextView);
        orderTotalTextView = findViewById(R.id.orderTotalTextView);
        complaintEditText = findViewById(R.id.complaintEditText);
        submitButton = findViewById(R.id.submitButton);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupViewModel() {
        ViewModelFactory factory = new ViewModelFactory();
        complaintViewModel = new ViewModelProvider(this, factory).get(ComplaintViewModel.class);
    }

    private void setupRepositories() {
        orderRepository = new OrderRepositoryImpl();
    }

    private void setupListeners() {
        submitButton.setOnClickListener(v -> submitComplaint());
    }

    private void observeViewModel() {
        complaintViewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null && isLoading) {
                progressBar.setVisibility(View.VISIBLE);
                submitButton.setEnabled(false);
            } else {
                progressBar.setVisibility(View.GONE);
                submitButton.setEnabled(true);
            }
        });

        complaintViewModel.getComplaintSubmitted().observe(this, submitted -> {
            if (submitted != null && submitted) {
                Toast.makeText(this, getString(R.string.toast_complaint_submitted), Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        complaintViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, getString(R.string.error_prefix, error), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadOrderDetails() {
        progressBar.setVisibility(View.VISIBLE);
        orderRepository.getOrderById(orderId, new OrderRepository.DataCallback<Order>() {
            @Override
            public void onSuccess(Order order) {
                progressBar.setVisibility(View.GONE);
                if (order != null) {
                    displayOrderDetails(order);
                } else {
                    Toast.makeText(SubmitComplaintActivity.this, getString(R.string.toast_not_found), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onError(String message) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(SubmitComplaintActivity.this, getString(R.string.error_prefix, message), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayOrderDetails(Order order) {
        orderIdTextView.setText("#" + orderId.substring(0, Math.min(8, orderId.length())).toUpperCase());
        
        // Format date
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        String dateStr = dateFormat.format(new Date(order.getCreatedAt()));
        orderDateTextView.setText(dateStr);
        
        orderTotalTextView.setText(CurrencyUtils.formatVND(order.getTotalAmount()));
    }

    private void submitComplaint() {
        // Validate content
        if (complaintEditText.getText() == null || complaintEditText.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, getString(R.string.toast_describe_complaint), Toast.LENGTH_SHORT).show();
            return;
        }

        String content = complaintEditText.getText().toString().trim();
        
        // Submit complaint
        complaintViewModel.submitComplaint(userId, orderId, content);
    }
}
