package com.deligo.app.utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Utility class for currency formatting
 */
public class CurrencyUtils {
    
    /**
     * Format price to Vietnamese Dong (VND)
     * @param amount The amount to format
     * @return Formatted string with VND currency symbol
     */
    public static String formatVND(double amount) {
        // Create a decimal format for Vietnamese currency
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(amount) + "đ";
    }
    
    /**
     * Format price to Vietnamese Dong (VND) with full currency name
     * @param amount The amount to format
     * @return Formatted string with VND text
     */
    public static String formatVNDWithText(double amount) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(amount) + " VND";
    }
    
    /**
     * Parse VND string to double
     * @param vndString String in VND format
     * @return Double value
     */
    public static double parseVND(String vndString) {
        try {
            // Remove currency symbols and whitespace
            String cleaned = vndString.replace("đ", "")
                                     .replace("VND", "")
                                     .replace(",", "")
                                     .replace(".", "")
                                     .trim();
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
