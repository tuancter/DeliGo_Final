package com.deligo.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.deligo.app.R;

/**
 * Admin dashboard displaying quick access buttons instead of bottom navigation.
 */
public class AdminMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        setupNavigationButtons();
    }

    private void setupNavigationButtons() {
        Button ordersButton = findViewById(R.id.buttonAdminOrders);
        Button menuButton = findViewById(R.id.buttonAdminMenu);
        Button statisticsButton = findViewById(R.id.buttonAdminStatistics);
        Button complaintsButton = findViewById(R.id.buttonAdminComplaints);
        Button accountButton = findViewById(R.id.buttonAdminAccount);

        ordersButton.setOnClickListener(v -> openScreen(AdminOrdersActivity.class));
        menuButton.setOnClickListener(v -> openScreen(AdminMenuActivity.class));
        statisticsButton.setOnClickListener(v -> openScreen(AdminStatisticsActivity.class));
        complaintsButton.setOnClickListener(v -> openScreen(AdminComplaintsActivity.class));
        accountButton.setOnClickListener(v -> openScreen(AdminAccountActivity.class));
    }

    private void openScreen(Class<?> targetActivity) {
        Intent intent = new Intent(this, targetActivity);
        startActivity(intent);
    }
}
