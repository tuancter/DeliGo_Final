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
import com.deligo.app.activities.AdminOrderDetailActivity;
import com.deligo.app.adapters.AdminOrderAdapter;
import com.deligo.app.models.Order;
import com.deligo.app.utils.UIHelper;
import com.deligo.app.utils.ViewModelFactory;
import com.deligo.app.viewmodels.AdminOrderViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AdminOrdersFragment extends Fragment implements AdminOrderAdapter.OnOrderActionListener {
    private RecyclerView rvOrders;
    private ProgressBar progressBar;
    private View layoutEmpty;

    private AdminOrderViewModel viewModel;
    private AdminOrderAdapter adapter;
    private List<Order> allOrders = new ArrayList<>();
    private String selectedStatus = "all";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_orders, container, false);
        initViews(view);
        setupRecyclerView();
        setupViewModel();
        return view;
    }

    private void initViews(View view) {
        rvOrders = view.findViewById(R.id.rvOrders);
        progressBar = view.findViewById(R.id.progressBar);
        layoutEmpty = view.findViewById(R.id.layoutEmpty);
    }

    private void setupRecyclerView() {
        adapter = new AdminOrderAdapter(this);
        rvOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvOrders.setAdapter(adapter);
    }

    private void setupViewModel() {
        ViewModelFactory factory = new ViewModelFactory();
        viewModel = new ViewModelProvider(this, factory).get(AdminOrderViewModel.class);

        // Observe orders with real-time updates
        viewModel.getOrders().observe(getViewLifecycleOwner(), orders -> {
            if (orders != null) {
                allOrders = orders;
                filterOrders();
            }
        });

        // Observe loading state
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                UIHelper.showLoading(progressBar, isLoading);
            }
        });

        // Observe errors
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty() && getView() != null) {
                String friendlyMessage = UIHelper.getFirestoreErrorMessage(error);
                UIHelper.showErrorSnackbar(getView(), 
                    friendlyMessage, v -> viewModel.loadAllOrders());
            }
        });

        // Observe action success
        viewModel.getActionSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success && getView() != null) {
                UIHelper.showSuccessSnackbar(getView(), 
                    "Order updated successfully");
                viewModel.resetActionSuccess();
            }
        });

        // Load orders
        viewModel.loadAllOrders();
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
        Intent intent = new Intent(requireContext(), AdminOrderDetailActivity.class);
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
