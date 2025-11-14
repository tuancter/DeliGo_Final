package com.deligo.app.utils;

import android.util.Patterns;

/**
 * Utility class for input validation
 */
public class ValidationUtils {
    
    /**
     * Validates email format
     * @param email Email address to validate
     * @return true if email is valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches();
    }
    
    /**
     * Validates phone number format
     * @param phone Phone number to validate
     * @return true if phone is valid, false otherwise
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        String cleanPhone = phone.replaceAll("[^0-9]", "");
        return cleanPhone.length() >= Constants.MIN_PHONE_LENGTH 
                && cleanPhone.length() <= Constants.MAX_PHONE_LENGTH;
    }
    
    /**
     * Validates password strength
     * @param password Password to validate
     * @return true if password meets requirements, false otherwise
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        // Minimum 8 characters, at least one letter and one number
        return password.length() >= Constants.MIN_PASSWORD_LENGTH
                && password.matches(".*[a-zA-Z].*")
                && password.matches(".*[0-9].*");
    }
    
    /**
     * Gets password validation error message
     * @param password Password to validate
     * @return Error message or null if valid
     */
    public static String getPasswordError(String password) {
        if (password == null || password.isEmpty()) {
            return "Password is required";
        }
        if (password.length() < Constants.MIN_PASSWORD_LENGTH) {
            return "Password must be at least " + Constants.MIN_PASSWORD_LENGTH + " characters";
        }
        if (!password.matches(".*[a-zA-Z].*")) {
            return "Password must contain at least one letter";
        }
        if (!password.matches(".*[0-9].*")) {
            return "Password must contain at least one number";
        }
        return null;
    }
    
    /**
     * Validates full name
     * @param fullName Full name to validate
     * @return true if name is valid, false otherwise
     */
    public static boolean isValidFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return false;
        }
        return fullName.trim().length() >= 2;
    }
    
    /**
     * Validates rating value
     * @param rating Rating to validate
     * @return true if rating is valid (1-5), false otherwise
     */
    public static boolean isValidRating(int rating) {
        return rating >= Constants.MIN_RATING && rating <= Constants.MAX_RATING;
    }
    
    /**
     * Validates price value
     * @param price Price to validate
     * @return true if price is valid (positive), false otherwise
     */
    public static boolean isValidPrice(double price) {
        return price > 0;
    }
    
    /**
     * Validates quantity value
     * @param quantity Quantity to validate
     * @return true if quantity is valid (positive), false otherwise
     */
    public static boolean isValidQuantity(int quantity) {
        return quantity > 0;
    }
    
    /**
     * Validates if string is not empty
     * @param text Text to validate
     * @return true if text is not null and not empty, false otherwise
     */
    public static boolean isNotEmpty(String text) {
        return text != null && !text.trim().isEmpty();
    }
    
    // Private constructor to prevent instantiation
    private ValidationUtils() {
        throw new AssertionError("Cannot instantiate ValidationUtils class");
    }
}
