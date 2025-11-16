package com.deligo.app.repositories;

import com.deligo.app.models.CartItem;
import com.deligo.app.models.Order;
import com.deligo.app.models.OrderDetail;

import java.util.List;

public interface OrderRepository {
    void createOrder(String customerId, String deliveryAddress, String paymentMethod,
                     String note, List<CartItem> cartItems, DataCallback<Order> callback);

    void getOrdersByCustomer(String customerId, DataCallback<List<Order>> callback);

    void getAllOrders(DataCallback<List<Order>> callback);

    void getOrderById(String orderId, DataCallback<Order> callback);

    void updateOrderStatus(String orderId, String status, ActionCallback callback);

    void updatePaymentStatus(String orderId, String status, ActionCallback callback);

    void updateOrderAndPaymentStatus(String orderId, String orderStatus, String paymentStatus, ActionCallback callback);

    void getOrderDetails(String orderId, DataCallback<List<OrderDetail>> callback);

    interface DataCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }

    interface ActionCallback {
        void onSuccess();
        void onError(String message);
    }
}
