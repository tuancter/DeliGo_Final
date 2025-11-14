package com.deligo.app.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.deligo.app.models.FoodSales;
import com.deligo.app.repositories.StatisticsRepository;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class StatisticsViewModel extends ViewModel {
    private final MutableLiveData<Double> totalRevenue = new MutableLiveData<>();
    private final MutableLiveData<Integer> orderCount = new MutableLiveData<>();
    private final MutableLiveData<Map<String, Integer>> ordersByStatus = new MutableLiveData<>();
    private final MutableLiveData<List<FoodSales>> topSellingFoods = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    private final StatisticsRepository statisticsRepository;

    public StatisticsViewModel(StatisticsRepository statisticsRepository) {
        this.statisticsRepository = statisticsRepository;
        isLoading.setValue(false);
    }

    public LiveData<Double> getTotalRevenue() {
        return totalRevenue;
    }

    public LiveData<Integer> getOrderCount() {
        return orderCount;
    }

    public LiveData<Map<String, Integer>> getOrdersByStatus() {
        return ordersByStatus;
    }

    public LiveData<List<FoodSales>> getTopSellingFoods() {
        return topSellingFoods;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public void loadStatistics(StatisticsPeriod period) {
        isLoading.setValue(true);
        errorMessage.setValue(null);

        long[] dateRange = calculateDateRange(period);
        long startDate = dateRange[0];
        long endDate = dateRange[1];

        // Load total revenue
        statisticsRepository.getTotalRevenue(startDate, endDate, new StatisticsRepository.DataCallback<Double>() {
            @Override
            public void onSuccess(Double data) {
                totalRevenue.setValue(data);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue("Failed to load revenue: " + message);
            }
        });

        // Load orders by status (also gives us order count)
        statisticsRepository.getOrderCountByStatus(startDate, endDate, new StatisticsRepository.DataCallback<Map<String, Integer>>() {
            @Override
            public void onSuccess(Map<String, Integer> data) {
                ordersByStatus.setValue(data);
                
                // Calculate total order count
                int total = 0;
                for (Integer count : data.values()) {
                    total += count;
                }
                orderCount.setValue(total);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue("Failed to load order statistics: " + message);
            }
        });

        // Load top selling foods
        statisticsRepository.getTopSellingFoods(startDate, endDate, 10, new StatisticsRepository.DataCallback<List<FoodSales>>() {
            @Override
            public void onSuccess(List<FoodSales> data) {
                topSellingFoods.setValue(data);
                isLoading.setValue(false);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue("Failed to load top selling foods: " + message);
                isLoading.setValue(false);
            }
        });
    }

    private long[] calculateDateRange(StatisticsPeriod period) {
        Calendar calendar = Calendar.getInstance();
        long endDate = calendar.getTimeInMillis();
        long startDate;

        switch (period) {
            case TODAY:
                // Start of today
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                startDate = calendar.getTimeInMillis();
                break;

            case THIS_WEEK:
                // Start of this week (Monday)
                calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                startDate = calendar.getTimeInMillis();
                break;

            case THIS_MONTH:
                // Start of this month
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                startDate = calendar.getTimeInMillis();
                break;

            default:
                startDate = 0;
                break;
        }

        return new long[]{startDate, endDate};
    }

    public enum StatisticsPeriod {
        TODAY, THIS_WEEK, THIS_MONTH
    }
}
