package com.deligo.app.repositories;

import android.util.Log;

import com.deligo.app.constants.OrderStatus;
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
                .whereEqualTo("orderStatus", OrderStatus.COMPLETED.getVietnameseName())
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
        // First, get ALL orders in the date range (without status filter) to debug
        firestore.collection("orders")
                .whereGreaterThanOrEqualTo("createdAt", startDate)
                .whereLessThanOrEqualTo("createdAt", endDate)
                .get()
                .addOnSuccessListener(allOrdersSnapshot -> {
                    // Log all order statuses for debugging
                    Map<String, Integer> statusDebug = new HashMap<>();
                    for (QueryDocumentSnapshot doc : allOrdersSnapshot) {
                        Order order = doc.toObject(Order.class);
                        String status = order.getOrderStatus();
                        statusDebug.put(status, statusDebug.getOrDefault(status, 0) + 1);
                    }
                    // Now get only completed orders
                    firestore.collection("orders")
                            .whereGreaterThanOrEqualTo("createdAt", startDate)
                            .whereLessThanOrEqualTo("createdAt", endDate)
                            .whereEqualTo("orderStatus", OrderStatus.COMPLETED.getVietnameseName())
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
                                    Order order = orderDoc.toObject(Order.class);

                                    firestore.collection("orders")
                                            .document(orderId)
                                            .collection("orderDetails")
                                            .get()
                                            .addOnSuccessListener(detailSnapshots -> {
                                                synchronized (foodQuantityMap) {
                                                    for (QueryDocumentSnapshot detailDoc : detailSnapshots) {
                                                        OrderDetail detail = detailDoc.toObject(OrderDetail.class);
                                                        String foodId = detail.getFoodId();
                                                        int quantity = detail.getQuantity();
                                                        
                                                        int oldQuantity = foodQuantityMap.getOrDefault(foodId, 0);
                                                        int newQuantity = oldQuantity + quantity;
                                                        foodQuantityMap.put(foodId, newQuantity);
                                                    }
                                                }

                                                synchronized (pendingOrders) {
                                                    pendingOrders[0]--;
                                                    // When all orders are processed
                                                    if (pendingOrders[0] == 0) {
                                                        processFoodSales(foodQuantityMap, limit, callback);
                                                    }
                                                }
                                            })
                                            .addOnFailureListener(e -> {
                                                synchronized (pendingOrders) {
                                                    pendingOrders[0]--;
                                                    if (pendingOrders[0] == 0) {
                                                        processFoodSales(foodQuantityMap, limit, callback);
                                                    }
                                                }
                                            });
                                }
                            })
                            .addOnFailureListener(e -> {
                                callback.onError(e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }

    private void processFoodSales(Map<String, Integer> foodQuantityMap, int limit, 
                                  DataCallback<List<FoodSales>> callback) {
        Log.d("TESTMINHTUAN", "========== START processFoodSales ==========");
        Log.d("TESTMINHTUAN", "Food quantity map size: " + foodQuantityMap.size());
        
        if (foodQuantityMap.isEmpty()) {
            Log.d("TESTMINHTUAN", "Food quantity map is empty - returning empty list");
            callback.onSuccess(new ArrayList<>());
            return;
        }

        // Sort by quantity and get top items
        List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(foodQuantityMap.entrySet());
        sortedEntries.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        Log.d("TESTMINHTUAN", "Top foods by quantity:");
        for (int i = 0; i < Math.min(sortedEntries.size(), limit); i++) {
            Map.Entry<String, Integer> entry = sortedEntries.get(i);
            Log.d("TESTMINHTUAN", (i + 1) + ". FoodId: " + entry.getKey() + " | Quantity: " + entry.getValue());
        }

        // Limit the results
        int resultSize = Math.min(limit, sortedEntries.size());
        List<FoodSales> foodSalesList = new ArrayList<>();
        int[] pendingFoods = {resultSize};
        
        Log.d("TESTMINHTUAN", "Fetching details for top " + resultSize + " foods");

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
                                Log.d("TESTMINHTUAN", "Added food: " + food.getName() + " | Quantity: " + quantitySold);
                            } else {
                                Log.e("TESTMINHTUAN", "Food object is null for foodId: " + foodId);
                            }
                        } else {
                            Log.e("TESTMINHTUAN", "Food document does not exist for foodId: " + foodId);
                        }

                        pendingFoods[0]--;
                        Log.d("TESTMINHTUAN", "Pending foods remaining: " + pendingFoods[0]);
                        
                        if (pendingFoods[0] == 0) {
                            // Sort again to maintain order after async operations
                            foodSalesList.sort((a, b) -> 
                                Integer.compare(b.getQuantitySold(), a.getQuantitySold()));
                            
                            Log.d("TESTMINHTUAN", "========== FINAL RESULT ==========");
                            Log.d("TESTMINHTUAN", "Total foods in result: " + foodSalesList.size());
                            for (int j = 0; j < foodSalesList.size(); j++) {
                                FoodSales fs = foodSalesList.get(j);
                                Log.d("TESTMINHTUAN", (j + 1) + ". " + fs.getFood().getName() + " - Sold: " + fs.getQuantitySold());
                            }
                            Log.d("TESTMINHTUAN", "========== END ==========");
                            
                            callback.onSuccess(foodSalesList);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("TESTMINHTUAN", "Failed to fetch food details for " + foodId + ": " + e.getMessage());
                        pendingFoods[0]--;
                        if (pendingFoods[0] == 0) {
                            foodSalesList.sort((a, b) -> 
                                Integer.compare(b.getQuantitySold(), a.getQuantitySold()));
                            callback.onSuccess(foodSalesList);
                        }
                    });
        }
    }

    @Override
    public void getTotalSoldCountForFood(String foodId, DataCallback<Integer> callback) {
        // Get all completed orders
        firestore.collection("orders")
                .whereEqualTo("orderStatus", OrderStatus.COMPLETED.getVietnameseName())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalSold = 0;
                    int[] pendingOrders = {queryDocumentSnapshots.size()};

                    if (pendingOrders[0] == 0) {
                        callback.onSuccess(0);
                        return;
                    }

                    final int[] totalSoldCount = {0};

                    // For each completed order, check if it contains this food
                    for (QueryDocumentSnapshot orderDoc : queryDocumentSnapshots) {
                        String orderId = orderDoc.getId();
                        
                        firestore.collection("orders")
                                .document(orderId)
                                .collection("orderDetails")
                                .whereEqualTo("foodId", foodId)
                                .get()
                                .addOnSuccessListener(detailSnapshots -> {
                                    synchronized (totalSoldCount) {
                                        for (QueryDocumentSnapshot detailDoc : detailSnapshots) {
                                            OrderDetail detail = detailDoc.toObject(OrderDetail.class);
                                            totalSoldCount[0] += detail.getQuantity();
                                        }
                                    }

                                    synchronized (pendingOrders) {
                                        pendingOrders[0]--;
                                        if (pendingOrders[0] == 0) {
                                            callback.onSuccess(totalSoldCount[0]);
                                        }
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    synchronized (pendingOrders) {
                                        pendingOrders[0]--;
                                        if (pendingOrders[0] == 0) {
                                            callback.onSuccess(totalSoldCount[0]);
                                        }
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void getDailyRevenue(long startDate, long endDate, DataCallback<Map<String, Double>> callback) {
        
        firestore.collection("orders")
                .whereGreaterThanOrEqualTo("createdAt", startDate)
                .whereLessThanOrEqualTo("createdAt", endDate)
                .whereEqualTo("orderStatus", OrderStatus.COMPLETED.getVietnameseName())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Map to store date -> total revenue
                    Map<String, Double> dailyRevenueMap = new HashMap<>();
                    java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
                    for (com.google.firebase.firestore.QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Order order = document.toObject(Order.class);
                        // Get date string from timestamp
                        String dateKey = dateFormat.format(new java.util.Date(order.getCreatedAt()));
                        // Add revenue to that date
                        double currentRevenue = dailyRevenueMap.getOrDefault(dateKey, 0.0);
                        dailyRevenueMap.put(dateKey, currentRevenue + order.getTotalAmount());
                    }
                    callback.onSuccess(dailyRevenueMap);
                })
                .addOnFailureListener(e -> {
                    callback.onError(e.getMessage());
                });
    }
}
