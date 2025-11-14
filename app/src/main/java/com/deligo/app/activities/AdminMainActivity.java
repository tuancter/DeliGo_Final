package com.deligo.app.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.deligo.app.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminMainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        bottomNavigation = findViewById(R.id.bottomNavigation);
        
        // Set default selection to Orders
        bottomNavigation.setSelectedItemId(R.id.nav_orders);
        
        // Navigate to AdminOrdersActivity by default
        startActivity(new Intent(this, AdminOrdersActivity.class));
        
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_orders) {
                startActivity(new Intent(AdminMainActivity.this, AdminOrdersActivity.class));
                return true;
            } else if (itemId == R.id.nav_menu) {
                startActivity(new Intent(AdminMainActivity.this, AdminMenuActivity.class));
                return true;
            } else if (itemId == R.id.nav_statistics) {
                startActivity(new Intent(AdminMainActivity.this, AdminStatisticsActivity.class));
                return true;
            } else if (itemId == R.id.nav_complaints) {
                startActivity(new Intent(AdminMainActivity.this, AdminComplaintsActivity.class));
                return true;
            }
            
            return false;
        });
    }
}
