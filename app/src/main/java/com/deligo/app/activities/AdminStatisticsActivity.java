package com.deligo.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.deligo.app.R;
import com.deligo.app.adapters.FoodSalesAdapter;
import com.deligo.app.utils.ViewModelFactory;
import com.deligo.app.viewmodels.StatisticsViewModel;

import java.util.Locale;
import java.util.Map;

public class AdminStatisticsActivity extends AppCompatActivity {
    private RadioGroup periodRadioGroup;
    private TextView tvTotalRevenue;
    private TextView tvOrderCount;
    private LinearLayout statusContainer;
    private RecyclerView rvTopFoods;
    private TextView tvNoTopFoods;
    private ProgressBar progressBar;
    private Toolbar toolbar;
    private StatisticsViewModel statisticsViewModel;
    private FoodSalesAdapter foodSalesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_statistics);

        initViews();
        setupToolbar();
        setupViewModel();
        setupRecyclerView();
        setupListeners();

        // Load initial statistics (Today)
        statisticsViewModel.loadStatistics(StatisticsViewModel.StatisticsPeriod.TODAY);
    }

    private void initViews() {
        periodRadioGroup = findViewById(R.id.periodRadioGroup);
        tvTotalRevenue = findViewById(R.id.tvTotalRevenue);
        tvOrderCount = findViewById(R.id.tvOrderCount);
        statusContainer = findViewById(R.id.statusContainer);
        rvTopFoods = findViewById(R.id.rvTopFoods);
        tvNoTopFoods = findViewById(R.id.tvNoTopFoods);
        progressBar = findViewById(R.id.progressBar);
        toolbar = findViewById(R.id.toolbar);
    }

    private void setupToolbar(){
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupViewModel() {
        ViewModelFactory factory = new ViewModelFactory();
        statisticsViewModel = new ViewModelProvider(this, factory).get(StatisticsViewModel.class);

        // Observe total revenue
        statisticsViewModel.getTotalRevenue().observe(this, revenue -> {
            if (revenue != null) {
                tvTotalRevenue.setText(String.format(Locale.getDefault(), "$%.2f", revenue));
            }
        });

        // Observe order count
        statisticsViewModel.getOrderCount().observe(this, count -> {
            if (count != null) {
                tvOrderCount.setText(String.valueOf(count));
            }
        });

        // Observe orders by status
        statisticsViewModel.getOrdersByStatus().observe(this, statusMap -> {
            if (statusMap != null) {
                displayOrdersByStatus(statusMap);
            }
        });

        // Observe top selling foods
        statisticsViewModel.getTopSellingFoods().observe(this, foodSalesList -> {
            if (foodSalesList != null && !foodSalesList.isEmpty()) {
                foodSalesAdapter.setFoodSalesList(foodSalesList);
                rvTopFoods.setVisibility(View.VISIBLE);
                tvNoTopFoods.setVisibility(View.GONE);
            } else {
                rvTopFoods.setVisibility(View.GONE);
                tvNoTopFoods.setVisibility(View.VISIBLE);
            }
        });

        // Observe loading state
        statisticsViewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        // Observe error messages
        statisticsViewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerView() {
        foodSalesAdapter = new FoodSalesAdapter();
        rvTopFoods.setLayoutManager(new LinearLayoutManager(this));
        rvTopFoods.setAdapter(foodSalesAdapter);
    }

    private void setupListeners() {
        periodRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            StatisticsViewModel.StatisticsPeriod period;
            
            if (checkedId == R.id.radioToday) {
                period = StatisticsViewModel.StatisticsPeriod.TODAY;
            } else if (checkedId == R.id.radioThisWeek) {
                period = StatisticsViewModel.StatisticsPeriod.THIS_WEEK;
            } else if (checkedId == R.id.radioThisMonth) {
                period = StatisticsViewModel.StatisticsPeriod.THIS_MONTH;
            } else {
                period = StatisticsViewModel.StatisticsPeriod.TODAY;
            }

            statisticsViewModel.loadStatistics(period);
        });
    }

    private void displayOrdersByStatus(Map<String, Integer> statusMap) {
        statusContainer.removeAllViews();

        if (statusMap.isEmpty()) {
            TextView noDataText = new TextView(this);
            noDataText.setText("No orders in this period");
            noDataText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            noDataText.setPadding(16, 16, 16, 16);
            statusContainer.addView(noDataText);
            return;
        }

        for (Map.Entry<String, Integer> entry : statusMap.entrySet()) {
            View statusItem = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, statusContainer, false);
            
            TextView text1 = statusItem.findViewById(android.R.id.text1);
            TextView text2 = statusItem.findViewById(android.R.id.text2);

            text1.setText(capitalizeFirstLetter(entry.getKey()));
            text1.setTextSize(16);
            text1.setTextColor(getResources().getColor(android.R.color.black, null));

            text2.setText(String.format(Locale.getDefault(), "%d orders", entry.getValue()));
            text2.setTextSize(14);
            text2.setTextColor(getResources().getColor(android.R.color.darker_gray, null));

            statusContainer.addView(statusItem);
        }
    }

    private String capitalizeFirstLetter(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }
}
