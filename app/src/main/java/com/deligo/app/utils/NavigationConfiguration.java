package com.deligo.app.utils;

import android.content.Context;

import com.deligo.app.R;
import com.deligo.app.models.NavigationItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration class for navigation items.
 * Provides static methods to get navigation items for different user roles.
 */
public class NavigationConfiguration {

    /**
     * Get navigation items for customer users
     * @param context Context to access string resources
     * @return List of NavigationItem for customer navigation
     */
    public static List<NavigationItem> getCustomerNavigationItems(Context context) {
        List<NavigationItem> items = new ArrayList<>();
        items.add(new NavigationItem(
                R.id.nav_home,
                R.drawable.restaurant,
                context.getString(R.string.nav_menu)
        ));
        items.add(new NavigationItem(
                R.id.nav_cart,
                R.drawable.cart,
                context.getString(R.string.nav_cart)
        ));
        items.add(new NavigationItem(
                R.id.nav_orders,
                R.drawable.order,
                context.getString(R.string.nav_orders)
        ));
        items.add(new NavigationItem(
                R.id.nav_profile,
                R.drawable.profile,
                context.getString(R.string.nav_profile)
        ));
        return items;
    }

    /**
     * Get navigation items for admin users
     * @param context Context to access string resources
     * @return List of NavigationItem for admin navigation
     */
    public static List<NavigationItem> getAdminNavigationItems(Context context) {
        List<NavigationItem> items = new ArrayList<>();
        items.add(new NavigationItem(
                R.id.nav_orders,
                R.drawable.order,
                context.getString(R.string.nav_orders)
        ));
        items.add(new NavigationItem(
                R.id.nav_menu,
                R.drawable.menu,
                context.getString(R.string.nav_menu)
        ));
        items.add(new NavigationItem(
                R.id.nav_statistics,
                android.R.drawable.ic_menu_info_details,
                context.getString(R.string.nav_statistics)
        ));
        items.add(new NavigationItem(
                R.id.nav_complaints,
                android.R.drawable.ic_menu_report_image,
                context.getString(R.string.nav_complaints)
        ));
        items.add(new NavigationItem(
                R.id.nav_profile,
                R.drawable.profile,
                context.getString(R.string.nav_profile)
        ));
        return items;
    }
}
