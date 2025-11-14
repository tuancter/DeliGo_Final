package com.deligo.app.utils;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.deligo.app.R;
import com.google.android.material.snackbar.Snackbar;

/**
 * Utility class for common UI operations like showing loading, error, and empty states
 */
public class UIHelper {

    /**
     * Show a loading state by displaying a ProgressBar
     */
    public static void showLoading(ProgressBar progressBar, boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Show an error message using Toast
     */
    public static void showErrorToast(Activity activity, String message) {
        if (activity != null && message != null && !message.isEmpty()) {
            Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Show a success message using Toast
     */
    public static void showSuccessToast(Activity activity, String message) {
        if (activity != null && message != null && !message.isEmpty()) {
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Show an error message using Snackbar with retry action
     */
    public static void showErrorSnackbar(View view, String message, View.OnClickListener retryListener) {
        if (view != null && message != null && !message.isEmpty()) {
            Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
            if (retryListener != null) {
                snackbar.setAction("Retry", retryListener);
            }
            snackbar.show();
        }
    }

    /**
     * Show an error message using Snackbar without retry action
     */
    public static void showErrorSnackbar(View view, String message) {
        showErrorSnackbar(view, message, null);
    }

    /**
     * Show a success message using Snackbar
     */
    public static void showSuccessSnackbar(View view, String message) {
        if (view != null && message != null && !message.isEmpty()) {
            Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
        }
    }

    /**
     * Show error layout and hide content
     */
    public static void showErrorLayout(ViewGroup errorLayout, ViewGroup contentLayout, 
                                      String errorMessage, View.OnClickListener retryListener) {
        if (errorLayout != null) {
            errorLayout.setVisibility(View.VISIBLE);
            
            TextView tvErrorMessage = errorLayout.findViewById(R.id.tvErrorMessage);
            if (tvErrorMessage != null && errorMessage != null) {
                tvErrorMessage.setText(errorMessage);
            }
            
            Button btnRetry = errorLayout.findViewById(R.id.btnRetry);
            if (btnRetry != null && retryListener != null) {
                btnRetry.setOnClickListener(retryListener);
            }
        }
        
        if (contentLayout != null) {
            contentLayout.setVisibility(View.GONE);
        }
    }

    /**
     * Show content layout and hide error
     */
    public static void showContentLayout(ViewGroup errorLayout, ViewGroup contentLayout) {
        if (errorLayout != null) {
            errorLayout.setVisibility(View.GONE);
        }
        
        if (contentLayout != null) {
            contentLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Show empty state layout
     */
    public static void showEmptyLayout(ViewGroup emptyLayout, ViewGroup contentLayout, 
                                      String emptyTitle, String emptyMessage) {
        if (emptyLayout != null) {
            emptyLayout.setVisibility(View.VISIBLE);
            
            TextView tvEmptyTitle = emptyLayout.findViewById(R.id.tvEmptyTitle);
            if (tvEmptyTitle != null && emptyTitle != null) {
                tvEmptyTitle.setText(emptyTitle);
            }
            
            TextView tvEmptyMessage = emptyLayout.findViewById(R.id.tvEmptyMessage);
            if (tvEmptyMessage != null && emptyMessage != null) {
                tvEmptyMessage.setText(emptyMessage);
            }
        }
        
        if (contentLayout != null) {
            contentLayout.setVisibility(View.GONE);
        }
    }

    /**
     * Hide empty state layout and show content
     */
    public static void hideEmptyLayout(ViewGroup emptyLayout, ViewGroup contentLayout) {
        if (emptyLayout != null) {
            emptyLayout.setVisibility(View.GONE);
        }
        
        if (contentLayout != null) {
            contentLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Handle network errors with appropriate message
     */
    public static String getNetworkErrorMessage(Exception e) {
        if (e == null) {
            return "An unknown error occurred";
        }
        
        String message = e.getMessage();
        if (message == null || message.isEmpty()) {
            return "An error occurred. Please try again.";
        }
        
        // Check for common Firebase/network errors
        if (message.contains("network") || message.contains("connection")) {
            return "Network error. Please check your internet connection.";
        } else if (message.contains("permission") || message.contains("denied")) {
            return "Permission denied. Please check your access rights.";
        } else if (message.contains("not found")) {
            return "The requested resource was not found.";
        } else if (message.contains("timeout")) {
            return "Request timed out. Please try again.";
        }
        
        return message;
    }

    /**
     * Handle Firestore exceptions with user-friendly messages
     */
    public static String getFirestoreErrorMessage(String errorMessage) {
        if (errorMessage == null || errorMessage.isEmpty()) {
            return "An error occurred. Please try again.";
        }
        
        // Convert technical error messages to user-friendly ones
        if (errorMessage.toLowerCase().contains("permission")) {
            return "You don't have permission to perform this action.";
        } else if (errorMessage.toLowerCase().contains("network")) {
            return "Network error. Please check your internet connection.";
        } else if (errorMessage.toLowerCase().contains("not found")) {
            return "The requested item was not found.";
        } else if (errorMessage.toLowerCase().contains("already exists")) {
            return "This item already exists.";
        } else if (errorMessage.toLowerCase().contains("invalid")) {
            return "Invalid data provided. Please check your input.";
        }
        
        return errorMessage;
    }

    /**
     * Enable or disable a button
     */
    public static void setButtonEnabled(Button button, boolean enabled) {
        if (button != null) {
            button.setEnabled(enabled);
            button.setAlpha(enabled ? 1.0f : 0.5f);
        }
    }
}
