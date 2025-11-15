package com.deligo.app.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.deligo.app.R;
import com.deligo.app.fragments.CartFragment;
import com.deligo.app.fragments.MenuFragment;
import com.deligo.app.fragments.OrdersFragment;
import com.deligo.app.fragments.ProfileFragment;
import com.deligo.app.models.NavigationItem;
import com.deligo.app.utils.NavigationConfiguration;
import com.deligo.app.utils.NavigationManager;
import com.deligo.app.views.CustomBottomNavigation;

import java.util.List;

public class CustomerMainActivity extends AppCompatActivity {

    private CustomBottomNavigation bottomNavigation;
    private NavigationManager navigationManager;
    private List<NavigationItem> navigationItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_main);

        // Initialize CustomBottomNavigation
        bottomNavigation = findViewById(R.id.bottomNavigation);
        
        // Setup navigation items
        navigationItems = NavigationConfiguration.getCustomerNavigationItems(this);
        bottomNavigation.setItems(navigationItems);
        
        // Initialize NavigationManager with home position 0 (Menu/Home)
        navigationManager = new NavigationManager(
            getSupportFragmentManager(), 
            R.id.fragmentContainer,
            0 // Home position
        );
        
        // Restore state or navigate to default fragment
        if (savedInstanceState != null) {
            navigationManager.restoreState(savedInstanceState);
            int selectedPosition = navigationManager.getCurrentPosition();
            if (selectedPosition >= 0) {
                bottomNavigation.setSelectedItem(selectedPosition);
            }
        } else {
            // Navigate to home (Menu) by default
            navigateToPosition(0);
            bottomNavigation.setSelectedItem(0);
        }
        
        // Setup navigation item click listener
        bottomNavigation.setOnNavigationItemSelectedListener((position, item) -> {
            navigateToPosition(position);
            return true;
        });
    }

    /**
     * Navigate to fragment based on position
     * @param position Position in navigation items
     */
    private void navigateToPosition(int position) {
        Fragment fragment = getFragmentForPosition(position);
        if (fragment != null) {
            navigationManager.navigateToFragment(position, fragment);
        }
    }

    /**
     * Get fragment instance for a given position
     * @param position Position in navigation items
     * @return Fragment instance
     */
    private Fragment getFragmentForPosition(int position) {
        switch (position) {
            case 0: // Home/Menu
                return new MenuFragment();
            case 1: // Cart
                return new CartFragment();
            case 2: // Orders
                return new OrdersFragment();
            case 3: // Profile
                return new ProfileFragment();
            default:
                return null;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (navigationManager != null) {
            navigationManager.saveState(outState);
        }
    }

    @Override
    public void onBackPressed() {
        // Handle back press with NavigationManager
        if (navigationManager != null && navigationManager.handleBackPress()) {
            // Update bottom navigation selection
            int currentPosition = navigationManager.getCurrentPosition();
            bottomNavigation.setSelectedItem(currentPosition);
        } else {
            // Exit app
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (navigationManager != null) {
            navigationManager.clearCache();
        }
    }

}
