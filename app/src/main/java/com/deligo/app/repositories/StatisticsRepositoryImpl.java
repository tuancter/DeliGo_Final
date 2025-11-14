package com.deligo.app.repositories;

import com.deligo.app.models.Food;
import com.deligo.app.models.FoodSales;
import com.deligo.app.models.Order;
import com.deligo.app.models.OrderDetail;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatisticsRepositoryImpl implements StatisticsRepository {
    private final FirebaseFirestore firestore;

    public StatisticsRepositoryImpl() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    @Override
    public void getOrdersByDateRange(long startDate, long endDate, DataCallback<List<Order>> callback) {
        firestore.collection("orders")
                .whereGreaterThanOrEqualTo("createdAt", startDate)
                .whereLessThanOrEqualTo("createdAt", endDate)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Order> orders = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Order order = document.toObject(Order.class);
                        order.setOrderId(document.getId());
                        orders.add(order);
                    }
                    callback.onSuccess(orders);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void getTotalRevenue(long startDate, long endDate, DataCallback<Double> callback) {
        firestore.collection("orders")
                .whereGreaterThanOrEqualTo("createdAt", startDate)
                .whereLessThanOrEqualTo("createdAt", endDate)
                .whereEqualTo("orderStatus", "completed")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double totalRevenue = 0.0;
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Order order = document.toObject(Order.class);
                        totalRevenue += order.getTotalAmount();
                    }
                    callback.onSuccess(totalRevenue);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void getOrderCountByStatus(long startDate, long endDate, DataCallback<Map<String, Integer>> callback) {
        firestore.collection("orders")
                .whereGreaterThanOrEqualTo("createdAt", startDate)
                .whereLessThanOrEqualTo("createdAt", endDate)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, Integer> statusCount = new HashMap<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Order order = document.toObject(Order.class);
                        String status = order.getOrderStatus();
                        statusCount.put(status, statusCount.getOrDefault(status, 0) + 1);
                    }
                    callback.onSuccess(statusCount);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void getTopSellingFoods(long startDate, long endDate, int limit, DataCallback<List<FoodSales>> callback) {
        // First, get all orders in the date range
        firestore.collection("orders")
                .whereGreaterThanOrEqualTo("createdAt", startDate)
                .whereLessThanOrEqualTo("createdAt", endDate)
                .whereEqualTo("orderStatus", "completed")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Map to store foodId -> total quantity sold
                    Map<String, Integer> foodQuantityMap = new HashMap<>();
                    int[] pendingOrders = {queryDocumentSnapshots.size()};

                    if (pendingOrders[0] == 0) {
                        callback.onSuccess(new ArrayList<>());
                        return;
                    }

                    // For each order, get its order details
                    for (QueryDocumentSnapshot orderDoc : queryDocumentSnapshots) {
                        String orderId = orderDoc.getId();
                        
                        firestore.collection("orders")
                                .document(orderId)
                                .collection("orderDetails")
                                .get()
                                .addOnSuccessListener(detailSnapshots -> {
                                    for (QueryDocumentSnapshot detailDoc : detailSnapshots) {
                                        OrderDetail detail = detailDoc.toObject(OrderDetail.class);
                                        String foodId = detail.getFoodId();
                                        int quantity = detail.getQuantity();
                                        foodQuantityMap.put(foodId, 
                                            foodQuantityMap.getOrDefault(foodId, 0) + quantity);
                                    }

                                    pendingOrders[0]--;
                                    
                                    // When all orders are processed
                                    if (pendingOrders[0] == 0) {
                                        processFoodSales(foodQuantityMap, limit, callback);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    pendingOrders[0]--;
                                    if (pendingOrders[0] == 0) {
                                        processFoodSales(foodQuantityMap, limit, callback);
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    private void processFoodSales(Map<String, Integer> foodQuantityMap, int limit, 
                                  DataCallback<List<FoodSales>> callback) {
        if (foodQuantityMap.isEmpty()) {
            callback.onSuccess(new ArrayList<>());
            return;
        }

        // Sort by quantity and get top items
        List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(foodQuantityMap.entrySet());
        sortedEntries.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        // Limit the results
        int resultSize = Math.min(limit, sortedEntries.size());
        List<FoodSales> foodSalesList = new ArrayList<>();
        int[] pendingFoods = {resultSize};

        for (int i = 0; i < resultSize; i++) {
            Map.Entry<String, Integer> entry = sortedEntries.get(i);
            String foodId = entry.getKey();
            int quantitySold = entry.getValue();

            // Fetch food details
            firestore.collection("foods")
                    .document(foodId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Food food = documentSnapshot.toObject(Food.class);
                            if (food != null) {
                                food.setFoodId(documentSnapshot.getId());
                                FoodSales foodSales = new FoodSales(food, quantitySold);
                                foodSalesList.add(foodSales);
                            }
                        }

                        pendingFoods[0]--;
                        if (pendingFoods[0] == 0) {
                            // Sort again to maintain order after async operations
                            foodSalesList.sort((a, b) -> 
                                Integer.compare(b.getQuantitySold(), a.getQuantitySold()));
                            callback.onSuccess(foodSalesList);
                        }
                    })
                    .addOnFailureListener(e -> {
                        pendingFoods[0]--;
                        if (pendingFoods[0] == 0) {
                            foodSalesList.sort((a, b) -> 
                                Integer.compare(b.getQuantitySold(), a.getQuantitySold()));
                            callback.onSuccess(foodSalesList);
                        }
                    });
        }
    }
}
