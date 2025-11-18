package com.deligo.app.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.deligo.app.models.CartItem;
import com.deligo.app.models.Order;
import com.deligo.app.models.OrderDetail;
import com.deligo.app.repositories.CartRepository;
import com.deligo.app.repositories.OrderRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class OrderViewModel extends ViewModel {
    private final MutableLiveData<List<Order>> orderHistory = new MutableLiveData<>();
    private final MutableLiveData<Order> currentOrder = new MutableLiveData<>();
    private final MutableLiveData<List<OrderDetail>> orderDetails = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> orderPlaced = new MutableLiveData<>(false);

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final FirebaseAuth firebaseAuth;

    public OrderViewModel(OrderRepository orderRepository, CartRepository cartRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    public LiveData<List<Order>> getOrderHistory() {
        return orderHistory;
    }

    public LiveData<Order> getCurrentOrder() {
        return currentOrder;
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

    public LiveData<Boolean> getOrderPlaced() {
        return orderPlaced;
    }

    public void placeOrder(String deliveryAddress, String paymentMethod, String note, List<CartItem> cartItems) {
        String userId = firebaseAuth.getCurrentUser() != null ? firebaseAuth.getCurrentUser().getUid() : null;
        if (userId == null) {
            errorMessage.setValue("User not authenticated");
            return;
        }

        if (cartItems == null || cartItems.isEmpty()) {
            errorMessage.setValue("Cart is empty");
            return;
        }

        isLoading.setValue(true);
        orderRepository.createOrder(userId, deliveryAddress, paymentMethod, note, cartItems,
                new OrderRepository.DataCallback<Order>() {
                    @Override
                    public void onSuccess(Order order) {
                        // Clear cart after successful order creation
                        cartRepository.clearCart(userId, new CartRepository.ActionCallback() {
                            @Override
                            public void onSuccess() {
                                currentOrder.setValue(order);
                                orderPlaced.setValue(true);
                                isLoading.setValue(false);
                            }

                            @Override
                            public void onError(String message) {
                                // Order created but cart clear failed - still consider success
                                currentOrder.setValue(order);
                                orderPlaced.setValue(true);
                                isLoading.setValue(false);
                            }
                        });
                    }

                    @Override
                    public void onError(String message) {
                        errorMessage.setValue(message);
                        isLoading.setValue(false);
                        orderPlaced.setValue(false);
                    }
                });
    }

    public void loadOrderHistory() {
        String userId = firebaseAuth.getCurrentUser() != null ? firebaseAuth.getCurrentUser().getUid() : null;
        if (userId == null) {
            errorMessage.setValue("User not authenticated");
            return;
        }

        isLoading.setValue(true);
        orderRepository.getOrdersByCustomer(userId, new OrderRepository.DataCallback<List<Order>>() {
            @Override
            public void onSuccess(List<Order> orders) {
                orderHistory.setValue(orders);
                isLoading.setValue(false);
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
                currentOrder.setValue(order);
                
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

    public void resetOrderPlaced() {
        orderPlaced.setValue(false);
    }

    public String getCreatedOrderId() {
        Order order = currentOrder.getValue();
        return order != null ? order.getOrderId() : null;
    }
}
