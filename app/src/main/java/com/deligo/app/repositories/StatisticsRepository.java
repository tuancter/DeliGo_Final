package com.deligo.app.repositories;

import com.deligo.app.models.FoodSales;
import com.deligo.app.models.Order;

import java.util.List;
import java.util.Map;

public interface StatisticsRepository {
    void getOrdersByDateRange(long startDate, long endDate, DataCallback<List<Order>> callback);

    void getTotalRevenue(long startDate, long endDate, DataCallback<Double> callback);

    void getOrderCountByStatus(long startDate, long endDate, DataCallback<Map<String, Integer>> callback);

    void getTopSellingFoods(long startDate, long endDate, int limit, DataCallback<List<FoodSales>> callback);

    interface DataCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }
}
