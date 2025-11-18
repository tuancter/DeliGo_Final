package com.deligo.app.activities;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.deligo.app.R;
import com.deligo.app.repositories.OrderRepository;
import com.deligo.app.repositories.OrderRepositoryImpl;
import com.deligo.app.views.BadgeView;

/**
 * Admin dashboard displaying quick access buttons instead of bottom navigation.
 */
public class AdminMainActivity extends AppCompatActivity {
    private static final String CHANNEL_ID = "admin_orders_channel";
    private static final int NOTIFICATION_ID = 1001;
    
    private OrderRepository orderRepository;
    private BadgeView badgePendingOrders;
    private int previousPendingCount = 0;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        orderRepository = new OrderRepositoryImpl();
        badgePendingOrders = findViewById(R.id.badgePendingOrders);

        setupPermissionLauncher();
        createNotificationChannel();
        requestNotificationPermission();
        setupNavigationButtons();
        setupOrderListener();
    }

    private void setupPermissionLauncher() {
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (!isGranted) {
                        Toast.makeText(this, "Vui lòng cấp quyền thông báo để nhận thông báo đơn hàng mới", Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
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
        accountButton.setOnClickListener(v -> openScreen(ProfileActivity.class));
    }

    private void setupOrderListener() {
        // Listen to pending orders in real-time
        orderRepository.listenToPendingOrders(count -> {
            runOnUiThread(() -> {
                // Update badge
                badgePendingOrders.setCount(count);

                // Show notification if count increased
                if (count > previousPendingCount && previousPendingCount > 0) {
                    showNewOrderNotification(count);
                }

                previousPendingCount = count;
            });
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Đơn hàng mới";
            String description = "Thông báo khi có đơn hàng mới";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void showNewOrderNotification(int pendingCount) {
        Intent intent = new Intent(this, AdminOrdersActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 
                0, 
                intent, 
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Đơn hàng mới!")
                .setContentText("Bạn có " + pendingCount + " đơn hàng chờ xác nhận")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    private void openScreen(Class<?> targetActivity) {
        Intent intent = new Intent(this, targetActivity);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh pending orders count when returning to this screen
        orderRepository.getPendingOrdersCount(new OrderRepository.DataCallback<Integer>() {
            @Override
            public void onSuccess(Integer count) {
                runOnUiThread(() -> {
                    badgePendingOrders.setCount(count);
                    previousPendingCount = count;
                });
            }

            @Override
            public void onError(String message) {
                // Silently fail
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove listener to prevent memory leaks
        if (orderRepository != null) {
            orderRepository.removeOrderListener();
        }
    }
}
