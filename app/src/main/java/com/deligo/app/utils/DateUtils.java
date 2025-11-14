package com.deligo.app.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class for date formatting and range calculations
 */
public class DateUtils {
    
    // Date format patterns
    private static final String PATTERN_FULL_DATE_TIME = "dd/MM/yyyy HH:mm";
    private static final String PATTERN_DATE_ONLY = "dd/MM/yyyy";
    private static final String PATTERN_TIME_ONLY = "HH:mm";
    private static final String PATTERN_MONTH_YEAR = "MM/yyyy";
    
    /**
     * Formats timestamp to full date and time string
     * @param timestamp Timestamp in milliseconds
     * @return Formatted date time string (dd/MM/yyyy HH:mm)
     */
    public static String formatDateTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat(PATTERN_FULL_DATE_TIME, Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
    
    /**
     * Formats timestamp to date only string
     * @param timestamp Timestamp in milliseconds
     * @return Formatted date string (dd/MM/yyyy)
     */
    public static String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat(PATTERN_DATE_ONLY, Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
    
    /**
     * Formats timestamp to time only string
     * @param timestamp Timestamp in milliseconds
     * @return Formatted time string (HH:mm)
     */
    public static String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat(PATTERN_TIME_ONLY, Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
    
    /**
     * Formats timestamp to month and year string
     * @param timestamp Timestamp in milliseconds
     * @return Formatted month/year string (MM/yyyy)
     */
    public static String formatMonthYear(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat(PATTERN_MONTH_YEAR, Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
    
    /**
     * Gets the start of today (00:00:00)
     * @return Timestamp in milliseconds
     */
    public static long getStartOfToday() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
    
    /**
     * Gets the end of today (23:59:59)
     * @return Timestamp in milliseconds
     */
    public static long getEndOfToday() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTimeInMillis();
    }
    
    /**
     * Gets the start of current week (Monday 00:00:00)
     * @return Timestamp in milliseconds
     */
    public static long getStartOfWeek() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
    
    /**
     * Gets the end of current week (Sunday 23:59:59)
     * @return Timestamp in milliseconds
     */
    public static long getEndOfWeek() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTimeInMillis();
    }
    
    /**
     * Gets the start of current month (1st day 00:00:00)
     * @return Timestamp in milliseconds
     */
    public static long getStartOfMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
    
    /**
     * Gets the end of current month (last day 23:59:59)
     * @return Timestamp in milliseconds
     */
    public static long getEndOfMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTimeInMillis();
    }
    
    /**
     * Gets date range based on period
     * @param period Period constant (PERIOD_TODAY, PERIOD_THIS_WEEK, PERIOD_THIS_MONTH)
     * @return Array with [startTimestamp, endTimestamp]
     */
    public static long[] getDateRange(String period) {
        long[] range = new long[2];
        
        switch (period) {
            case Constants.PERIOD_TODAY:
                range[0] = getStartOfToday();
                range[1] = getEndOfToday();
                break;
            case Constants.PERIOD_THIS_WEEK:
                range[0] = getStartOfWeek();
                range[1] = getEndOfWeek();
                break;
            case Constants.PERIOD_THIS_MONTH:
                range[0] = getStartOfMonth();
                range[1] = getEndOfMonth();
                break;
            default:
                range[0] = getStartOfToday();
                range[1] = getEndOfToday();
                break;
        }
        
        return range;
    }
    
    /**
     * Calculates the difference in days between two timestamps
     * @param timestamp1 First timestamp
     * @param timestamp2 Second timestamp
     * @return Number of days difference
     */
    public static long getDaysDifference(long timestamp1, long timestamp2) {
        long diff = Math.abs(timestamp1 - timestamp2);
        return diff / (24 * 60 * 60 * 1000);
    }
    
    /**
     * Checks if a timestamp is today
     * @param timestamp Timestamp to check
     * @return true if timestamp is today, false otherwise
     */
    public static boolean isToday(long timestamp) {
        return timestamp >= getStartOfToday() && timestamp <= getEndOfToday();
    }
    
    /**
     * Gets relative time string (e.g., "Just now", "5 minutes ago", "2 hours ago")
     * @param timestamp Timestamp to format
     * @return Relative time string
     */
    public static String getRelativeTime(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;
        
        if (diff < 60000) { // Less than 1 minute
            return "Just now";
        } else if (diff < 3600000) { // Less than 1 hour
            long minutes = diff / 60000;
            return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
        } else if (diff < 86400000) { // Less than 1 day
            long hours = diff / 3600000;
            return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        } else if (diff < 604800000) { // Less than 1 week
            long days = diff / 86400000;
            return days + " day" + (days > 1 ? "s" : "") + " ago";
        } else {
            return formatDate(timestamp);
        }
    }
    
    // Private constructor to prevent instantiation
    private DateUtils() {
        throw new AssertionError("Cannot instantiate DateUtils class");
    }
}
