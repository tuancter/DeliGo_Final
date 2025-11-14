package com.deligo.app.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.deligo.app.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class CustomerMainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_main);

        bottomNavigation = findViewById(R.id.bottomNavigation);
        
        // Set default selection to Home
        bottomNavigation.setSelectedItemId(R.id.nav_home);
        
        // Navigate to MenuActivity by default
        startActivity(new Intent(this, MenuActivity.class));
        
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(CustomerMainActivity.this, MenuActivity.class));
                return true;
            } else if (itemId == R.id.nav_cart) {
                startActivity(new Intent(CustomerMainActivity.this, CartActivity.class));
                return true;
            } else if (itemId == R.id.nav_orders) {
                startActivity(new Intent(CustomerMainActivity.this, OrderHistoryActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(CustomerMainActivity.this, ProfileActivity.class));
                return true;
            }
            
            return false;
        });
    }
}
