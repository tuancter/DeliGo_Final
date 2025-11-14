package com.deligo.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.deligo.app.R;
import com.deligo.app.adapters.OrderAdapter;
import com.deligo.app.models.Order;
import com.deligo.app.repositories.CartRepositoryImpl;
import com.deligo.app.repositories.OrderRepositoryImpl;
import com.deligo.app.utils.UIHelper;
import com.deligo.app.utils.ViewModelFactory;
import com.deligo.app.viewmodels.OrderViewModel;

public class OrderHistoryActivity extends AppCompatActivity implements OrderAdapter.OnOrderClickListener {
    private OrderViewModel orderViewModel;
    private OrderAdapter orderAdapter;
    private RecyclerView ordersRecyclerView;
    private ProgressBar progressBar;
    private View emptyOrdersLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        initViews();
        setupToolbar();
        setupViewModel();
        setupRecyclerView();
        observeViewModel();

        // Load order history
        orderViewModel.loadOrderHistory();
    }

    private void initViews() {
        ordersRecyclerView = findViewById(R.id.ordersRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyOrdersLayout = findViewById(R.id.emptyOrdersLayout);
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
        orderAdapter = new OrderAdapter(this);
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ordersRecyclerView.setAdapter(orderAdapter);
    }

    private void observeViewModel() {
        orderViewModel.getOrderHistory().observe(this, orders -> {
            if (orders != null) {
                orderAdapter.setOrders(orders);

                // Show/hide empty state
                boolean isEmpty = orders.isEmpty();
                emptyOrdersLayout.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                ordersRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            }
        });

        orderViewModel.getIsLoading().observe(this, isLoading -> {
            UIHelper.showLoading(progressBar, isLoading);
        });

        orderViewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                String friendlyMessage = UIHelper.getFirestoreErrorMessage(errorMessage);
                UIHelper.showErrorSnackbar(findViewById(android.R.id.content), 
                    friendlyMessage, v -> orderViewModel.loadOrderHistory());
            }
        });
    }

    @Override
    public void onOrderClick(Order order) {
        // Navigate to order detail activity
        Intent intent = new Intent(this, OrderDetailActivity.class);
        intent.putExtra("orderId", order.getOrderId());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload orders when returning to this activity
        orderViewModel.loadOrderHistory();
    }
}
