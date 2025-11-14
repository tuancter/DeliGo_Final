package com.deligo.app.utils;

/**
 * Constants class containing all constant values used throughout the application
 */
public class Constants {
    
    // Firestore Collection Names
    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_CATEGORIES = "categories";
    public static final String COLLECTION_FOODS = "foods";
    public static final String COLLECTION_CARTS = "carts";
    public static final String COLLECTION_CART_ITEMS = "cartItems";
    public static final String COLLECTION_ORDERS = "orders";
    public static final String COLLECTION_ORDER_DETAILS = "orderDetails";
    public static final String COLLECTION_REVIEWS = "reviews";
    public static final String COLLECTION_COMPLAINTS = "complaints";
    
    // User Roles
    public static final String ROLE_CUSTOMER = "customer";
    public static final String ROLE_ADMIN = "admin";
    
    // User Status
    public static final String USER_STATUS_ACTIVE = "active";
    public static final String USER_STATUS_INACTIVE = "inactive";
    
    // Payment Status
    public static final String PAYMENT_STATUS_PENDING = "pending";
    public static final String PAYMENT_STATUS_COMPLETED = "completed";
    public static final String PAYMENT_STATUS_FAILED = "failed";
    
    // Order Status
    public static final String ORDER_STATUS_PENDING = "pending";
    public static final String ORDER_STATUS_ACCEPTED = "accepted";
    public static final String ORDER_STATUS_PREPARING = "preparing";
    public static final String ORDER_STATUS_COMPLETED = "completed";
    public static final String ORDER_STATUS_CANCELLED = "cancelled";
    
    // Complaint Status
    public static final String COMPLAINT_STATUS_PENDING = "pending";
    public static final String COMPLAINT_STATUS_RESOLVED = "resolved";
    public static final String COMPLAINT_STATUS_REJECTED = "rejected";
    
    // Payment Methods
    public static final String PAYMENT_METHOD_CASH = "cash";
    public static final String PAYMENT_METHOD_CARD = "card";
    public static final String PAYMENT_METHOD_ONLINE = "online";
    
    // Intent Extra Keys
    public static final String EXTRA_FOOD_ID = "food_id";
    public static final String EXTRA_ORDER_ID = "order_id";
    public static final String EXTRA_CATEGORY_ID = "category_id";
    public static final String EXTRA_USER_ID = "user_id";
    public static final String EXTRA_COMPLAINT_ID = "complaint_id";
    
    // Validation
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MIN_PHONE_LENGTH = 10;
    public static final int MAX_PHONE_LENGTH = 15;
    public static final int MIN_RATING = 1;
    public static final int MAX_RATING = 5;
    
    // Statistics Periods
    public static final String PERIOD_TODAY = "today";
    public static final String PERIOD_THIS_WEEK = "this_week";
    public static final String PERIOD_THIS_MONTH = "this_month";
    
    // Private constructor to prevent instantiation
    private Constants() {
        throw new AssertionError("Cannot instantiate Constants class");
    }
}
