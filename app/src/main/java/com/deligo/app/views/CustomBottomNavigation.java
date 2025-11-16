package com.deligo.app.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.deligo.app.R;
import com.deligo.app.models.NavigationItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom bottom navigation view that replaces Material BottomNavigationView.
 * Uses LinearLayout with ImageView and TextView for each navigation item.
 */
public class CustomBottomNavigation extends LinearLayout {

    private static final long DEBOUNCE_DELAY_MS = 300; // 300ms debounce delay
    
    private List<NavigationItem> items;
    private int selectedPosition = 0;
    private OnNavigationItemSelectedListener listener;
    private List<View> itemViews;
    private long lastClickTime = 0;

    /**
     * Listener interface for navigation item selection events
     */
    public interface OnNavigationItemSelectedListener {
        /**
         * Called when a navigation item is selected
         * @param position Position of the selected item
         * @param item The selected NavigationItem
         * @return true if the event was handled
         */
        boolean onNavigationItemSelected(int position, NavigationItem item);
    }

    public CustomBottomNavigation(Context context) {
        super(context);
        init();
    }

    public CustomBottomNavigation(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomBottomNavigation(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * Initialize the view
     */
    private void init() {
        setOrientation(HORIZONTAL);
        items = new ArrayList<>();
        itemViews = new ArrayList<>();
    }

    /**
     * Set the navigation items to display
     * @param items List of NavigationItem to display
     */
    public void setItems(List<NavigationItem> items) {
        this.items = items;
        setupNavigationItems();
    }

    /**
     * Set the selected item by position
     * @param position Position of the item to select
     */
    public void setSelectedItem(int position) {
        if (position < 0 || position >= items.size()) {
            return;
        }
        
        selectedPosition = position;
        updateItemStates();
    }

    /**
     * Set the listener for navigation item selection events
     * @param listener OnNavigationItemSelectedListener to handle selection events
     */
    public void setOnNavigationItemSelectedListener(OnNavigationItemSelectedListener listener) {
        this.listener = listener;
    }

    /**
     * Setup navigation items by inflating item layouts
     */
    private void setupNavigationItems() {
        // Clear existing views
        removeAllViews();
        itemViews.clear();

        LayoutInflater inflater = LayoutInflater.from(getContext());

        for (int i = 0; i < items.size(); i++) {
            final int position = i;
            final NavigationItem item = items.get(i);

            // Inflate item layout
            View itemView = inflater.inflate(R.layout.item_navigation, this, false);
            
            ImageView icon = itemView.findViewById(R.id.navIcon);

            // Set icon
            icon.setImageResource(item.getIconResId());

            // Set click listener
            itemView.setOnClickListener(v -> onItemClicked(position));

            // Add to container
            addView(itemView);
            itemViews.add(itemView);
        }

        // Update initial state
        updateItemStates();
    }

    /**
     * Handle item click with debouncing to prevent rapid clicks
     * @param position Position of the clicked item
     */
    private void onItemClicked(int position) {
        // Debounce rapid clicks
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime < DEBOUNCE_DELAY_MS) {
            return; // Ignore click within debounce window
        }
        lastClickTime = currentTime;
        
        if (position == selectedPosition) {
            return; // Already selected
        }

        selectedPosition = position;
        updateItemStates();

        // Notify listener
        if (listener != null && position < items.size()) {
            listener.onNavigationItemSelected(position, items.get(position));
        }
    }

    /**
     * Update the visual state of all items based on selection
     */
    private void updateItemStates() {
        for (int i = 0; i < items.size() && i < itemViews.size(); i++) {
            NavigationItem item = items.get(i);
            View itemView = itemViews.get(i);
            
            boolean isSelected = (i == selectedPosition);
            item.setSelected(isSelected);
            
            // Set selected state on iconContainer for circular background
            View iconContainer = itemView.findViewById(R.id.iconContainer);
            if (iconContainer != null) {
                iconContainer.setSelected(isSelected);
            }
            
            // Set selected state on icon for color change
            ImageView icon = itemView.findViewById(R.id.navIcon);
            if (icon != null) {
                icon.setSelected(isSelected);
            }
        }
    }

    /**
     * Get the currently selected position
     * @return Currently selected position
     */
    public int getSelectedPosition() {
        return selectedPosition;
    }

    /**
     * Get the list of navigation items
     * @return List of NavigationItem
     */
    public List<NavigationItem> getItems() {
        return items;
    }
}
