package com.deligo.app.models;

/**
 * Model class representing a navigation item in the bottom navigation bar.
 * Contains information about the icon, title, and selection state.
 */
public class NavigationItem {
    private int id;
    private int iconResId;
    private String title;
    private boolean isSelected;

    /**
     * Constructor for NavigationItem
     * @param id Unique identifier for the navigation item
     * @param iconResId Resource ID for the icon drawable
     * @param title Display title for the navigation item
     */
    public NavigationItem(int id, int iconResId, String title) {
        this.id = id;
        this.iconResId = iconResId;
        this.title = title;
        this.isSelected = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
