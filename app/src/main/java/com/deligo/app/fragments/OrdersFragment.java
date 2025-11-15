package com.deligo.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.deligo.app.R;
import com.deligo.app.activities.OrderDetailActivity;
import com.deligo.app.adapters.OrderAdapter;
import com.deligo.app.models.Order;
import com.deligo.app.utils.UIHelper;
import com.deligo.app.utils.ViewModelFactory;
import com.deligo.app.viewmodels.OrderViewModel;

/**
 * Fragment displaying order history
 */
public class OrdersFragment extends Fragment implements OrderAdapter.OnOrderClickListener {
    
    private OrderViewModel orderViewModel;
    private OrderAdapter orderAdapter;
    private RecyclerView ordersRecyclerView;
    private ProgressBar progressBar;
    private View emptyOrdersLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_orders, container, false);
        
        initViews(view);
        setupViewModel();
        setupRecyclerView();
        observeViewModel();

        // Load order history
        orderViewModel.loadOrderHistory();
        
        return view;
    }

    private void initViews(View view) {
        ordersRecyclerView = view.findViewById(R.id.ordersRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyOrdersLayout = view.findViewById(R.id.emptyOrdersLayout);
    }

    private void setupViewModel() {
        ViewModelFactory factory = new ViewModelFactory();
        orderViewModel = new ViewModelProvider(this, factory).get(OrderViewModel.class);
    }

    private void setupRecyclerView() {
        orderAdapter = new OrderAdapter(this);
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        ordersRecyclerView.setAdapter(orderAdapter);
    }

    private void observeViewModel() {
        orderViewModel.getOrderHistory().observe(getViewLifecycleOwner(), orders -> {
            if (orders != null) {
                orderAdapter.setOrders(orders);

                // Show/hide empty state
                boolean isEmpty = orders.isEmpty();
                emptyOrdersLayout.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                ordersRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            }
        });

        orderViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            UIHelper.showLoading(progressBar, isLoading);
        });

        orderViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty() && getView() != null) {
                String friendlyMessage = UIHelper.getFirestoreErrorMessage(errorMessage);
                UIHelper.showErrorSnackbar(getView(), 
                    friendlyMessage, v -> orderViewModel.loadOrderHistory());
            }
        });
    }

    @Override
    public void onOrderClick(Order order) {
        // Navigate to order detail activity
        Intent intent = new Intent(getActivity(), OrderDetailActivity.class);
        intent.putExtra("orderId", order.getOrderId());
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload orders when fragment becomes visible
        if (orderViewModel != null) {
            orderViewModel.loadOrderHistory();
        }
    }
}
