package com.deligo.app.repositories;

import com.deligo.app.models.CartItem;
import com.deligo.app.models.Food;
import com.deligo.app.models.Order;
import com.deligo.app.models.OrderDetail;
import com.deligo.app.utils.Constants;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderRepositoryImpl implements OrderRepository {
    private final FirebaseFirestore firestore;
    private ListenerRegistration orderListener;

    public OrderRepositoryImpl() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    @Override
    public void createOrder(String customerId, String deliveryAddress, String paymentMethod,
                           String note, List<CartItem> cartItems, DataCallback<Order> callback) {
        // Calculate total amount
        double totalAmount = 0;
        for (CartItem item : cartItems) {
            totalAmount += item.getPrice() * item.getQuantity();
        }

        // Create order object
        Order order = new Order();
        order.setCustomerId(customerId);
        order.setDeliveryAddress(deliveryAddress);
        order.setPaymentMethod(paymentMethod);
        order.setNote(note);
        order.setTotalAmount(totalAmount);
        order.setPaymentStatus("pending");
        order.setOrderStatus("pending");
        order.setCreatedAt(System.currentTimeMillis());

        // Create order in Firestore
        firestore.collection("orders")
                .add(order)
                .addOnSuccessListener(documentReference -> {
                    String orderId = documentReference.getId();
                    order.setOrderId(orderId);

                    // Create batch to add order details
                    WriteBatch batch = firestore.batch();

                    for (CartItem cartItem : cartItems) {
                        OrderDetail orderDetail = new OrderDetail();
                        orderDetail.setOrderId(orderId);
                        orderDetail.setFoodId(cartItem.getFoodId());
                        orderDetail.setQuantity(cartItem.getQuantity());
                        orderDetail.setUnitPrice(cartItem.getPrice());

                        DocumentReference detailRef = documentReference
                                .collection("orderDetails")
                                .document();
                        orderDetail.setOrderDetailId(detailRef.getId());
                        batch.set(detailRef, orderDetail);
                    }

                    // Commit batch
                    batch.commit()
                            .addOnSuccessListener(aVoid -> callback.onSuccess(order))
                            .addOnFailureListener(e -> callback.onError(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void getOrdersByCustomer(String customerId, DataCallback<List<Order>> callback) {
        firestore.collection("orders")
                .whereEqualTo("customerId", customerId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Order> orders = new ArrayList<>();
                    queryDocumentSnapshots.forEach(document -> {
                        Order order = document.toObject(Order.class);
                        order.setOrderId(document.getId());
                        orders.add(order);
                    });
                    callback.onSuccess(orders);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void getAllOrders(DataCallback<List<Order>> callback) {
        firestore.collection("orders")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Order> orders = new ArrayList<>();
                    queryDocumentSnapshots.forEach(document -> {
                        Order order = document.toObject(Order.class);
                        order.setOrderId(document.getId());
                        orders.add(order);
                    });
                    callback.onSuccess(orders);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void getOrderById(String orderId, DataCallback<Order> callback) {
        firestore.collection("orders")
                .document(orderId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Order order = documentSnapshot.toObject(Order.class);
                        if (order != null) {
                            order.setOrderId(documentSnapshot.getId());
                            callback.onSuccess(order);
                        } else {
                            callback.onError("Failed to parse order");
                        }
                    } else {
                        callback.onError("Order not found");
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void updateOrderStatus(String orderId, String status, ActionCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("orderStatus", status);

        firestore.collection("orders")
                .document(orderId)
                .update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void updatePaymentStatus(String orderId, String status, ActionCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("paymentStatus", status);

        firestore.collection("orders")
                .document(orderId)
                .update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void updateOrderAndPaymentStatus(String orderId, String orderStatus, String paymentStatus, ActionCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("orderStatus", orderStatus);
        updates.put("paymentStatus", paymentStatus);

        firestore.collection("orders")
                .document(orderId)
                .update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void getOrderDetails(String orderId, DataCallback<List<OrderDetail>> callback) {
        firestore.collection("orders")
                .document(orderId)
                .collection("orderDetails")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<OrderDetail> orderDetails = new ArrayList<>();
                    int[] pendingCount = {queryDocumentSnapshots.size()};

                    if (pendingCount[0] == 0) {
                        callback.onSuccess(orderDetails);
                        return;
                    }

                    queryDocumentSnapshots.forEach(document -> {
                        OrderDetail orderDetail = document.toObject(OrderDetail.class);
                        orderDetail.setOrderDetailId(document.getId());

                        // Fetch food information
                        firestore.collection("foods")
                                .document(orderDetail.getFoodId())
                                .get()
                                .addOnSuccessListener(foodDoc -> {
                                    if (foodDoc.exists()) {
                                        Food food = foodDoc.toObject(Food.class);
                                        if (food != null) {
                                            food.setFoodId(foodDoc.getId());
                                            orderDetail.setFood(food);
                                        }
                                    }
                                    orderDetails.add(orderDetail);
                                    pendingCount[0]--;

                                    if (pendingCount[0] == 0) {
                                        callback.onSuccess(orderDetails);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    orderDetails.add(orderDetail);
                                    pendingCount[0]--;

                                    if (pendingCount[0] == 0) {
                                        callback.onSuccess(orderDetails);
                                    }
                                });
                    });
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void getPendingOrdersCount(DataCallback<Integer> callback) {
        firestore.collection("orders")
                .whereEqualTo("orderStatus", Constants.ORDER_STATUS_PENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    callback.onSuccess(queryDocumentSnapshots.size());
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void listenToPendingOrders(OrderCountListener listener) {
        // Remove existing listener if any
        removeOrderListener();

        // Listen to pending orders in real-time
        orderListener = firestore.collection("orders")
                .whereEqualTo("orderStatus", Constants.ORDER_STATUS_PENDING)
                .addSnapshotListener((queryDocumentSnapshots, error) -> {
                    if (error != null) {
                        return;
                    }
                    if (queryDocumentSnapshots != null) {
                        listener.onCountChanged(queryDocumentSnapshots.size());
                    }
                });
    }

    @Override
    public void removeOrderListener() {
        if (orderListener != null) {
            orderListener.remove();
            orderListener = null;
        }
    }
}
