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
    public void createOrder(String customerId, String phoneNumber, String deliveryAddress, String paymentMethod,
                            String note, List<CartItem> cartItems, DataCallback<Order> callback) {
        // Calculate total amount
        double totalAmount = 0;
        for (CartItem item : cartItems) {
            totalAmount += item.getPrice() * item.getQuantity();
        }

        // Create order object
        Order order = new Order();
        order.setCustomerId(customerId);
        order.setPhoneNumber(phoneNumber);
        order.setDeliveryAddress(deliveryAddress);
        order.setPaymentMethod(paymentMethod);
        order.setNote(note);
        order.setTotalAmount(totalAmount);
        order.setPaymentStatus("Chờ xác nhận");
        order.setOrderStatus("Chờ xác nhận");
        order.setCreatedAt(System.currentTimeMillis());

        long timestamp = System.currentTimeMillis();

        // Lấy mỗi năm (YYYY)
        java.text.SimpleDateFormat yearFormat =
                new java.text.SimpleDateFormat("yyyy", java.util.Locale.getDefault());
        String yearPart = yearFormat.format(new java.util.Date(timestamp));
        // Lấy 4 số cuối của timestamp
        String last4 = String.format("%04d", (int) (timestamp % 10000));
        String customOrderId = "DH" + yearPart + last4;

        order.setOrderId(customOrderId);


        // Create order in Firestore with custom ID
        firestore.collection("orders")
                .document(customOrderId)
                .set(order)
                .addOnSuccessListener(aVoid -> {
                    // Create batch to add order details
                    WriteBatch batch = firestore.batch();

                    for (CartItem cartItem : cartItems) {
                        OrderDetail orderDetail = new OrderDetail();
                        orderDetail.setOrderId(customOrderId);
                        orderDetail.setFoodId(cartItem.getFoodId());
                        orderDetail.setQuantity(cartItem.getQuantity());
                        orderDetail.setUnitPrice(cartItem.getPrice());

                        DocumentReference detailRef = firestore.collection("orders")
                                .document(customOrderId)
                                .collection("orderDetails")
                                .document();
                        orderDetail.setOrderDetailId(detailRef.getId());
                        batch.set(detailRef, orderDetail);
                    }

                    // Commit batch
                    batch.commit()
                            .addOnSuccessListener(batchVoid -> callback.onSuccess(order))
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
        // Convert status to Vietnamese
        String vietnameseStatus = convertStatusToVietnamese(status);

        Map<String, Object> updates = new HashMap<>();
        updates.put("orderStatus", vietnameseStatus);

        firestore.collection("orders")
                .document(orderId)
                .update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    private String convertStatusToVietnamese(String status) {
        if (status == null) return status;

        switch (status.toLowerCase()) {
            case "pending":
                return "Chờ xác nhận";
            case "accepted":
                return "Đã nhận đơn";
            case "preparing":
                return "Đang chuẩn bị";
            case "completed":
                return "Đã hoàn thành";
            case "cancelled":
                return "Bị huỷ";
            default:
                return status;
        }
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
        // Convert statuses to Vietnamese
        String vietnameseOrderStatus = convertStatusToVietnamese(orderStatus);
        String vietnamesePaymentStatus = convertStatusToVietnamese(paymentStatus);

        Map<String, Object> updates = new HashMap<>();
        updates.put("orderStatus", vietnameseOrderStatus);
        updates.put("paymentStatus", vietnamesePaymentStatus);

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
                .whereEqualTo("orderStatus", "Chờ xác nhận")
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
                .whereEqualTo("orderStatus", "Chờ xác nhận")
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
