package com.deligo.app.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.deligo.app.R;
import com.deligo.app.adapters.FoodSalesAdapter;
import com.deligo.app.models.FoodSales;
import com.deligo.app.utils.CurrencyUtils;
import com.deligo.app.utils.ViewModelFactory;
import com.deligo.app.viewmodels.StatisticsViewModel;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.List;
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
    
    // Charts
    private PieChart pieChartOrderStatus;
    private BarChart barChartTopFoods;
    private LineChart lineChartRevenue;

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
        
        // Update line chart with sample data
        updateLineChart();
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
        
        // Charts
        pieChartOrderStatus = findViewById(R.id.pieChartOrderStatus);
        barChartTopFoods = findViewById(R.id.barChartTopFoods);
        lineChartRevenue = findViewById(R.id.lineChartRevenue);
        
        setupCharts();
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
                tvTotalRevenue.setText(CurrencyUtils.formatVND(revenue));
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
                updatePieChart(statusMap);
            }
        });

        // Observe top selling foods
        statisticsViewModel.getTopSellingFoods().observe(this, foodSalesList -> {
            if (foodSalesList != null && !foodSalesList.isEmpty()) {
                foodSalesAdapter.setFoodSalesList(foodSalesList);
                rvTopFoods.setVisibility(View.VISIBLE);
                tvNoTopFoods.setVisibility(View.GONE);
                updateBarChart(foodSalesList);
            } else {
                rvTopFoods.setVisibility(View.GONE);
                tvNoTopFoods.setVisibility(View.VISIBLE);
                barChartTopFoods.clear();
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
            noDataText.setText(R.string.label_no_orders_this_period);
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

            text2.setText(getString(R.string.label_orders_count, entry.getValue()));
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

    private void setupCharts() {
        setupPieChart();
        setupBarChart();
        setupLineChart();
    }

    private void setupPieChart() {
        pieChartOrderStatus.setUsePercentValues(true);
        pieChartOrderStatus.getDescription().setEnabled(false);
        pieChartOrderStatus.setExtraOffsets(5, 10, 5, 5);
        pieChartOrderStatus.setDragDecelerationFrictionCoef(0.95f);
        pieChartOrderStatus.setDrawHoleEnabled(true);
        pieChartOrderStatus.setHoleColor(Color.WHITE);
        pieChartOrderStatus.setTransparentCircleRadius(61f);
        pieChartOrderStatus.setDrawCenterText(true);
        pieChartOrderStatus.setCenterText(getString(R.string.orders_by_status));
        pieChartOrderStatus.setCenterTextSize(14f);
        pieChartOrderStatus.setRotationEnabled(true);
        pieChartOrderStatus.setHighlightPerTapEnabled(true);

        Legend legend = pieChartOrderStatus.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setWordWrapEnabled(true);
    }

    private void setupBarChart() {
        barChartTopFoods.getDescription().setEnabled(false);
        barChartTopFoods.setDrawGridBackground(false);
        barChartTopFoods.setDrawBarShadow(false);
        barChartTopFoods.setHighlightFullBarEnabled(false);
        barChartTopFoods.setPinchZoom(false);
        barChartTopFoods.setDrawValueAboveBar(true);

        XAxis xAxis = barChartTopFoods.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-45);

        barChartTopFoods.getAxisLeft().setDrawGridLines(false);
        barChartTopFoods.getAxisRight().setEnabled(false);
        barChartTopFoods.getAxisLeft().setAxisMinimum(0f);

        Legend legend = barChartTopFoods.getLegend();
        legend.setEnabled(false);
    }

    private void setupLineChart() {
        lineChartRevenue.getDescription().setEnabled(false);
        lineChartRevenue.setDrawGridBackground(false);
        lineChartRevenue.setTouchEnabled(true);
        lineChartRevenue.setDragEnabled(true);
        lineChartRevenue.setScaleEnabled(true);
        lineChartRevenue.setPinchZoom(true);

        XAxis xAxis = lineChartRevenue.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);

        lineChartRevenue.getAxisLeft().setDrawGridLines(true);
        lineChartRevenue.getAxisRight().setEnabled(false);
        lineChartRevenue.getAxisLeft().setAxisMinimum(0f);

        Legend legend = lineChartRevenue.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(false);
    }

    private void updatePieChart(Map<String, Integer> statusMap) {
        if (statusMap == null || statusMap.isEmpty()) {
            pieChartOrderStatus.clear();
            return;
        }

        ArrayList<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : statusMap.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), capitalizeFirstLetter(entry.getKey())));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        // Colors for different statuses
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.rgb(255, 193, 7));  // Pending - Amber
        colors.add(Color.rgb(33, 150, 243)); // Confirmed - Blue
        colors.add(Color.rgb(156, 39, 176)); // Preparing - Purple
        colors.add(Color.rgb(255, 152, 0));  // Ready - Orange
        colors.add(Color.rgb(3, 169, 244));  // Delivering - Light Blue
        colors.add(Color.rgb(76, 175, 80));  // Delivered - Green
        colors.add(Color.rgb(244, 67, 54));  // Cancelled - Red
        dataSet.setColors(colors);

        dataSet.setValueTextSize(11f);
        dataSet.setValueTextColor(Color.WHITE);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });

        pieChartOrderStatus.setData(data);
        pieChartOrderStatus.invalidate();
        pieChartOrderStatus.animateY(1000);
    }

    private void updateBarChart(List<FoodSales> foodSalesList) {
        if (foodSalesList == null || foodSalesList.isEmpty()) {
            barChartTopFoods.clear();
            return;
        }

        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        int maxItems = Math.min(foodSalesList.size(), 10);
        for (int i = 0; i < maxItems; i++) {
            FoodSales foodSales = foodSalesList.get(i);
            entries.add(new BarEntry(i, foodSales.getQuantitySold()));
            
            String foodName = foodSales.getFood() != null ? foodSales.getFood().getName() : "N/A";
            if (foodName.length() > 15) {
                foodName = foodName.substring(0, 12) + "...";
            }
            labels.add(foodName);
        }

        BarDataSet dataSet = new BarDataSet(entries, getString(R.string.quantity_sold));
        dataSet.setColor(ContextCompat.getColor(this, R.color.primary));
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(Color.BLACK);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.8f);

        barChartTopFoods.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChartTopFoods.getXAxis().setLabelCount(labels.size());
        barChartTopFoods.setData(data);
        barChartTopFoods.setFitBars(true);
        barChartTopFoods.invalidate();
        barChartTopFoods.animateY(1000);
    }

    private void updateLineChart() {
        // Sample data for revenue trend
        // In a real app, you would get this from your ViewModel
        ArrayList<Entry> entries = new ArrayList<>();
        
        // Example: Last 7 days revenue (you should replace this with actual data)
        entries.add(new Entry(0, 500000));
        entries.add(new Entry(1, 750000));
        entries.add(new Entry(2, 600000));
        entries.add(new Entry(3, 900000));
        entries.add(new Entry(4, 800000));
        entries.add(new Entry(5, 1200000));
        entries.add(new Entry(6, 1000000));

        LineDataSet dataSet = new LineDataSet(entries, getString(R.string.revenue_vnd));
        dataSet.setColor(ContextCompat.getColor(this, R.color.primary));
        dataSet.setCircleColor(ContextCompat.getColor(this, R.color.primary));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextSize(10f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(ContextCompat.getColor(this, R.color.primary));
        dataSet.setFillAlpha(50);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format(Locale.getDefault(), "%.0fK", value / 1000);
            }
        });

        LineData data = new LineData(dataSet);
        
        ArrayList<String> labels = new ArrayList<>();
        labels.add("Mon");
        labels.add("Tue");
        labels.add("Wed");
        labels.add("Thu");
        labels.add("Fri");
        labels.add("Sat");
        labels.add("Sun");
        
        lineChartRevenue.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        lineChartRevenue.getXAxis().setLabelCount(labels.size());
        lineChartRevenue.setData(data);
        lineChartRevenue.invalidate();
        lineChartRevenue.animateX(1000);
    }
}
