package com.deligo.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.deligo.app.R;
import com.deligo.app.adapters.AdminOrderAdapter;
import com.deligo.app.models.Order;
import com.deligo.app.utils.UIHelper;
import com.deligo.app.utils.ViewModelFactory;
import com.deligo.app.viewmodels.AdminOrderViewModel;
import androidx.appcompat.widget.Toolbar;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AdminOrdersActivity extends AppCompatActivity implements AdminOrderAdapter.OnOrderActionListener {
    private Toolbar toolbar;
    private LinearLayout chipGroupStatus;
    private RecyclerView rvOrders;
    private ProgressBar progressBar;
    private View layoutEmpty;

    private AdminOrderViewModel viewModel;
    private AdminOrderAdapter adapter;
    private List<Order> allOrders = new ArrayList<>();
    private String selectedStatus = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_orders);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupViewModel();
        setupFilters();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        chipGroupStatus = findViewById(R.id.chipGroupStatus);
        rvOrders = findViewById(R.id.rvOrders);
        progressBar = findViewById(R.id.progressBar);
        layoutEmpty = findViewById(R.id.layoutEmpty);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
        
        // Back to Dashboard button
        findViewById(R.id.btnBackToDashboard).setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new AdminOrderAdapter(this);
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        rvOrders.setAdapter(adapter);
    }

    private void setupViewModel() {
        ViewModelFactory factory = new ViewModelFactory();
        viewModel = new ViewModelProvider(this, factory).get(AdminOrderViewModel.class);

        // Observe orders with real-time updates
        viewModel.getOrders().observe(this, orders -> {
            if (orders != null) {
                allOrders = orders;
                filterOrders();
            }
        });

        // Observe loading state
        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null) {
                UIHelper.showLoading(progressBar, isLoading);
            }
        });

        // Observe errors
        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                String friendlyMessage = UIHelper.getFirestoreErrorMessage(error);
                UIHelper.showErrorSnackbar(findViewById(android.R.id.content), 
                    friendlyMessage, v -> viewModel.loadAllOrders());
            }
        });

        // Observe action success
        viewModel.getActionSuccess().observe(this, success -> {
            if (success != null && success) {
                UIHelper.showSuccessSnackbar(findViewById(android.R.id.content), 
                    "Order updated successfully");
                viewModel.resetActionSuccess();
            }
        });

        // Load orders
        viewModel.loadAllOrders();
    }

    private void setupFilters() {
        // Setup click listeners for filter buttons
        findViewById(R.id.chipAll).setOnClickListener(v -> {
            selectedStatus = "all";
            filterOrders();
        });
        findViewById(R.id.chipPending).setOnClickListener(v -> {
            selectedStatus = "pending";
            filterOrders();
        });
        findViewById(R.id.chipAccepted).setOnClickListener(v -> {
            selectedStatus = "accepted";
            filterOrders();
        });
        findViewById(R.id.chipPreparing).setOnClickListener(v -> {
            selectedStatus = "preparing";
            filterOrders();
        });
        findViewById(R.id.chipCompleted).setOnClickListener(v -> {
            selectedStatus = "completed";
            filterOrders();
        });
        findViewById(R.id.chipCancelled).setOnClickListener(v -> {
            selectedStatus = "cancelled";
            filterOrders();
        });
    }

    private void filterOrders() {
        List<Order> filteredOrders;
        
        if ("all".equals(selectedStatus)) {
            filteredOrders = allOrders;
        } else {
            filteredOrders = allOrders.stream()
                    .filter(order -> selectedStatus.equalsIgnoreCase(order.getOrderStatus()))
                    .collect(Collectors.toList());
        }

        adapter.setOrders(filteredOrders);
        
        // Show/hide empty state
        if (filteredOrders.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvOrders.setVisibility(View.GONE);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvOrders.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onOrderClick(Order order) {
        Intent intent = new Intent(this, AdminOrderDetailActivity.class);
        intent.putExtra("orderId", order.getOrderId());
        startActivity(intent);
    }

    @Override
    public void onAcceptOrder(Order order) {
        viewModel.acceptOrder(order.getOrderId());
    }

    @Override
    public void onUpdateStatus(Order order, String newStatus) {
        viewModel.updateOrderStatus(order.getOrderId(), newStatus);
    }
}
