package com.deligo.app.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.deligo.app.R;
import com.deligo.app.fragments.AdminComplaintsFragment;
import com.deligo.app.fragments.AdminMenuFragment;
import com.deligo.app.fragments.AdminOrdersFragment;
import com.deligo.app.fragments.AdminStatisticsFragment;
import com.deligo.app.models.NavigationItem;
import com.deligo.app.utils.NavigationConfiguration;
import com.deligo.app.utils.NavigationManager;
import com.deligo.app.views.CustomBottomNavigation;

import java.util.List;

public class AdminMainActivity extends AppCompatActivity {

    private CustomBottomNavigation bottomNavigation;
    private NavigationManager navigationManager;
    private List<NavigationItem> navigationItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        // Initialize CustomBottomNavigation
        bottomNavigation = findViewById(R.id.bottomNavigation);
        
        // Setup navigation items
        navigationItems = NavigationConfiguration.getAdminNavigationItems(this);
        bottomNavigation.setItems(navigationItems);
        
        // Initialize NavigationManager with orders position 0
        navigationManager = new NavigationManager(
            getSupportFragmentManager(), 
            R.id.fragmentContainer,
            0 // Orders position
        );
        
        // Restore state or navigate to default fragment
        if (savedInstanceState != null) {
            navigationManager.restoreState(savedInstanceState);
            int selectedPosition = navigationManager.getCurrentPosition();
            if (selectedPosition >= 0) {
                bottomNavigation.setSelectedItem(selectedPosition);
            }
        } else {
            // Navigate to Orders by default
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
            case 0: // Orders
                return new AdminOrdersFragment();
            case 1: // Menu
                return new AdminMenuFragment();
            case 2: // Statistics
                return new AdminStatisticsFragment();
            case 3: // Complaints
                return new AdminComplaintsFragment();
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
