package com.deligo.app.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.deligo.app.models.Order;
import com.deligo.app.models.OrderDetail;
import com.deligo.app.repositories.OrderRepository;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminOrderViewModel extends ViewModel {
    private final MutableLiveData<List<Order>> orders = new MutableLiveData<>();
    private final MutableLiveData<Order> selectedOrder = new MutableLiveData<>();
    private final MutableLiveData<List<OrderDetail>> orderDetails = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> actionSuccess = new MutableLiveData<>(false);

    private final OrderRepository orderRepository;
    private final FirebaseFirestore firestore;
    private ListenerRegistration ordersListener;

    // Valid status transitions
    private static final Map<String, List<String>> VALID_TRANSITIONS = new HashMap<>();
    static {
        VALID_TRANSITIONS.put("pending", Arrays.asList("accepted", "cancelled"));
        VALID_TRANSITIONS.put("accepted", Arrays.asList("preparing", "cancelled"));
        VALID_TRANSITIONS.put("preparing", Arrays.asList("completed", "cancelled"));
        VALID_TRANSITIONS.put("completed", new ArrayList<>());
        VALID_TRANSITIONS.put("cancelled", new ArrayList<>());
    }

    public AdminOrderViewModel(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
        this.firestore = FirebaseFirestore.getInstance();
    }

    public LiveData<List<Order>> getOrders() {
        return orders;
    }

    public LiveData<Order> getSelectedOrder() {
        return selectedOrder;
    }

    public LiveData<List<OrderDetail>> getOrderDetails() {
        return orderDetails;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<Boolean> getActionSuccess() {
        return actionSuccess;
    }

    public void loadAllOrders() {
        // Remove existing listener if any
        if (ordersListener != null) {
            ordersListener.remove();
        }

        isLoading.setValue(true);

        // Set up real-time listener
        ordersListener = firestore.collection("orders")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((queryDocumentSnapshots, error) -> {
                    if (error != null) {
                        errorMessage.setValue(error.getMessage());
                        isLoading.setValue(false);
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        List<Order> orderList = new ArrayList<>();
                        queryDocumentSnapshots.forEach(document -> {
                            Order order = document.toObject(Order.class);
                            order.setOrderId(document.getId());
                            orderList.add(order);
                        });
                        orders.setValue(orderList);
                        isLoading.setValue(false);
                    }
                });
    }

    public void acceptOrder(String orderId) {
        isLoading.setValue(true);
        
        // First get the order to check current status
        orderRepository.getOrderById(orderId, new OrderRepository.DataCallback<Order>() {
            @Override
            public void onSuccess(Order order) {
                if (!"pending".equals(order.getOrderStatus())) {
                    errorMessage.setValue("Only pending orders can be accepted");
                    isLoading.setValue(false);
                    return;
                }

                // Update status to accepted
                orderRepository.updateOrderStatus(orderId, "accepted", new OrderRepository.ActionCallback() {
                    @Override
                    public void onSuccess() {
                        actionSuccess.setValue(true);
                        isLoading.setValue(false);
                    }

                    @Override
                    public void onError(String message) {
                        errorMessage.setValue(message);
                        isLoading.setValue(false);
                    }
                });
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
                isLoading.setValue(false);
            }
        });
    }

    public void updateOrderStatus(String orderId, String newStatus) {
        isLoading.setValue(true);

        // First get the order to validate transition
        orderRepository.getOrderById(orderId, new OrderRepository.DataCallback<Order>() {
            @Override
            public void onSuccess(Order order) {
                String currentStatus = order.getOrderStatus();

                // Validate status transition
                if (!isValidTransition(currentStatus, newStatus)) {
                    errorMessage.setValue("Invalid status transition from " + currentStatus + " to " + newStatus);
                    isLoading.setValue(false);
                    return;
                }

                // Update status
                orderRepository.updateOrderStatus(orderId, newStatus, new OrderRepository.ActionCallback() {
                    @Override
                    public void onSuccess() {
                        actionSuccess.setValue(true);
                        isLoading.setValue(false);
                    }

                    @Override
                    public void onError(String message) {
                        errorMessage.setValue(message);
                        isLoading.setValue(false);
                    }
                });
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
                isLoading.setValue(false);
            }
        });
    }

    public void loadOrderDetails(String orderId) {
        isLoading.setValue(true);

        // Load order info
        orderRepository.getOrderById(orderId, new OrderRepository.DataCallback<Order>() {
            @Override
            public void onSuccess(Order order) {
                selectedOrder.setValue(order);

                // Load order details
                orderRepository.getOrderDetails(orderId, new OrderRepository.DataCallback<List<OrderDetail>>() {
                    @Override
                    public void onSuccess(List<OrderDetail> details) {
                        orderDetails.setValue(details);
                        isLoading.setValue(false);
                    }

                    @Override
                    public void onError(String message) {
                        errorMessage.setValue(message);
                        isLoading.setValue(false);
                    }
                });
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
                isLoading.setValue(false);
            }
        });
    }

    private boolean isValidTransition(String currentStatus, String newStatus) {
        List<String> validNextStatuses = VALID_TRANSITIONS.get(currentStatus);
        return validNextStatuses != null && validNextStatuses.contains(newStatus);
    }

    public void resetActionSuccess() {
        actionSuccess.setValue(false);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Remove listener when ViewModel is cleared
        if (ordersListener != null) {
            ordersListener.remove();
        }
    }
}
