package com.deligo.app.utils;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Manager class for handling Fragment navigation and transactions.
 * Implements fragment caching to improve performance and maintain state.
 * Manages back stack for proper navigation flow.
 */
public class NavigationManager {

    private static final String KEY_SELECTED_POSITION = "selected_position";
    private static final String KEY_FRAGMENT_TAGS = "fragment_tags";
    private static final String KEY_NAVIGATION_STACK = "navigation_stack";

    private FragmentManager fragmentManager;
    private int containerId;
    private Map<Integer, Fragment> fragmentCache;
    private int currentPosition = -1;
    private Stack<Integer> navigationStack;
    private int homePosition = 0;

    /**
     * Constructor for NavigationManager
     * @param fragmentManager FragmentManager to handle transactions
     * @param containerId Resource ID of the container to hold fragments
     */
    public NavigationManager(FragmentManager fragmentManager, int containerId) {
        this.fragmentManager = fragmentManager;
        this.containerId = containerId;
        this.fragmentCache = new HashMap<>();
        this.navigationStack = new Stack<>();
    }

    /**
     * Constructor for NavigationManager with home position
     * @param fragmentManager FragmentManager to handle transactions
     * @param containerId Resource ID of the container to hold fragments
     * @param homePosition Position of the home/default fragment
     */
    public NavigationManager(FragmentManager fragmentManager, int containerId, int homePosition) {
        this.fragmentManager = fragmentManager;
        this.containerId = containerId;
        this.fragmentCache = new HashMap<>();
        this.navigationStack = new Stack<>();
        this.homePosition = homePosition;
    }

    /**
     * Navigate to a fragment by position
     * @param position Position of the navigation item
     * @param fragment Fragment instance to display
     */
    public void navigateToFragment(int position, Fragment fragment) {
        if (currentPosition == position) {
            return; // Already showing this fragment
        }

        if (!isStateSafe()) {
            return; // Skip transaction if state is not safe
        }

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        
        // Add fast fade animation for smooth transitions
        transaction.setCustomAnimations(
            com.deligo.app.R.anim.fade_in_fast,
            com.deligo.app.R.anim.fade_out_fast
        );

        // Hide current fragment if exists
        if (currentPosition >= 0 && fragmentCache.containsKey(currentPosition)) {
            Fragment currentFragment = fragmentCache.get(currentPosition);
            if (currentFragment != null && currentFragment.isAdded()) {
                transaction.hide(currentFragment);
            }
        }

        // Check if fragment is already cached
        Fragment cachedFragment = fragmentCache.get(position);
        
        if (cachedFragment != null && cachedFragment.isAdded()) {
            // Show cached fragment
            transaction.show(cachedFragment);
        } else {
            // Add new fragment to cache
            fragmentCache.put(position, fragment);
            transaction.add(containerId, fragment, getFragmentTag(position));
        }

        // Commit transaction
        transaction.commit();
        
        // Update navigation stack
        updateNavigationStack(position);
        
        currentPosition = position;
    }

    /**
     * Navigate to a fragment by position using fragment class
     * @param position Position of the navigation item
     * @param fragmentClass Class of the fragment to instantiate
     */
    public void navigateToFragment(int position, Class<? extends Fragment> fragmentClass) {
        // Check if fragment is already cached
        Fragment cachedFragment = fragmentCache.get(position);
        
        if (cachedFragment != null) {
            navigateToFragment(position, cachedFragment);
        } else {
            // Create new fragment instance
            Fragment fragment = getOrCreateFragment(fragmentClass);
            if (fragment != null) {
                navigateToFragment(position, fragment);
            }
        }
    }

    /**
     * Handle back button press
     * @return true if navigation was handled, false if should exit app
     */
    public boolean handleBackPress() {
        // If we're at home position, allow app to exit
        if (currentPosition == homePosition || navigationStack.isEmpty()) {
            return false;
        }

        // Pop from stack and navigate to previous position
        if (navigationStack.size() > 1) {
            navigationStack.pop(); // Remove current position
            int previousPosition = navigationStack.peek();
            
            // Navigate without adding to stack again
            navigateToFragmentWithoutStack(previousPosition);
            return true;
        } else {
            // Navigate back to home
            navigateToFragmentWithoutStack(homePosition);
            return true;
        }
    }

    /**
     * Navigate to fragment without modifying navigation stack
     * Used for back navigation
     * @param position Position to navigate to
     */
    private void navigateToFragmentWithoutStack(int position) {
        if (currentPosition == position) {
            return;
        }

        if (!isStateSafe()) {
            return; // Skip transaction if state is not safe
        }

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        
        // Add fast fade animation
        transaction.setCustomAnimations(
            com.deligo.app.R.anim.fade_in_fast,
            com.deligo.app.R.anim.fade_out_fast
        );

        // Hide current fragment
        if (currentPosition >= 0 && fragmentCache.containsKey(currentPosition)) {
            Fragment currentFragment = fragmentCache.get(currentPosition);
            if (currentFragment != null && currentFragment.isAdded()) {
                transaction.hide(currentFragment);
            }
        }

        // Show target fragment
        Fragment targetFragment = fragmentCache.get(position);
        if (targetFragment != null && targetFragment.isAdded()) {
            transaction.show(targetFragment);
        }

        transaction.commit();
        currentPosition = position;
    }

    /**
     * Update navigation stack when navigating to a new position
     * @param position New position to navigate to
     */
    private void updateNavigationStack(int position) {
        // If navigating to home, clear the stack
        if (position == homePosition) {
            navigationStack.clear();
            navigationStack.push(position);
            return;
        }

        // If stack already contains this position, pop until we reach it
        if (navigationStack.contains(position)) {
            while (!navigationStack.isEmpty() && navigationStack.peek() != position) {
                navigationStack.pop();
            }
            if (navigationStack.isEmpty()) {
                navigationStack.push(position);
            }
        } else {
            // Add new position to stack
            navigationStack.push(position);
        }
    }

    /**
     * Get or create a fragment from cache
     * @param fragmentClass Class of the fragment to get or create
     * @return Fragment instance
     */
    private Fragment getOrCreateFragment(Class<? extends Fragment> fragmentClass) {
        try {
            return fragmentClass.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Save the current navigation state
     * @param outState Bundle to save state to
     */
    public void saveState(Bundle outState) {
        outState.putInt(KEY_SELECTED_POSITION, currentPosition);
        
        // Save fragment tags
        String[] tags = new String[fragmentCache.size()];
        int index = 0;
        for (Integer position : fragmentCache.keySet()) {
            tags[index++] = getFragmentTag(position);
        }
        outState.putStringArray(KEY_FRAGMENT_TAGS, tags);
        
        // Save navigation stack
        int[] stackArray = new int[navigationStack.size()];
        index = 0;
        for (Integer pos : navigationStack) {
            stackArray[index++] = pos;
        }
        outState.putIntArray(KEY_NAVIGATION_STACK, stackArray);
    }

    /**
     * Restore navigation state from saved instance
     * @param savedState Bundle containing saved state
     */
    public void restoreState(Bundle savedState) {
        if (savedState == null) {
            return;
        }

        currentPosition = savedState.getInt(KEY_SELECTED_POSITION, -1);
        
        // Restore fragment cache from FragmentManager
        String[] tags = savedState.getStringArray(KEY_FRAGMENT_TAGS);
        if (tags != null) {
            for (String tag : tags) {
                Fragment fragment = fragmentManager.findFragmentByTag(tag);
                if (fragment != null) {
                    int position = getPositionFromTag(tag);
                    fragmentCache.put(position, fragment);
                }
            }
        }
        
        // Restore navigation stack
        int[] stackArray = savedState.getIntArray(KEY_NAVIGATION_STACK);
        if (stackArray != null) {
            navigationStack.clear();
            for (int pos : stackArray) {
                navigationStack.push(pos);
            }
        }
    }

    /**
     * Clear the fragment cache and navigation stack
     */
    public void clearCache() {
        fragmentCache.clear();
        navigationStack.clear();
    }

    /**
     * Get the current selected position
     * @return Current position
     */
    public int getCurrentPosition() {
        return currentPosition;
    }

    /**
     * Set the home position for back navigation
     * @param homePosition Position to use as home/default
     */
    public void setHomePosition(int homePosition) {
        this.homePosition = homePosition;
    }

    /**
     * Check if fragment manager is in valid state for transactions
     * @return true if safe to perform transactions
     */
    private boolean isStateSafe() {
        return !fragmentManager.isDestroyed() && !fragmentManager.isStateSaved();
    }

    /**
     * Generate a unique tag for a fragment at a given position
     * @param position Position of the fragment
     * @return Fragment tag
     */
    private String getFragmentTag(int position) {
        return "fragment_" + position;
    }

    /**
     * Extract position from fragment tag
     * @param tag Fragment tag
     * @return Position extracted from tag
     */
    private int getPositionFromTag(String tag) {
        try {
            return Integer.parseInt(tag.replace("fragment_", ""));
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
