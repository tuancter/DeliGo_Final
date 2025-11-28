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
     * Handle Firestore exceptions with user-friendly messages in Vietnamese
     */
    public static String getFirestoreErrorMessage(String errorMessage) {
        if (errorMessage == null || errorMessage.isEmpty()) {
            return "Đã xảy ra lỗi. Vui lòng thử lại.";
        }
        
        String lowerError = errorMessage.toLowerCase();
        
        // Firebase Authentication Errors
        if (lowerError.contains("invalid-email") || lowerError.contains("invalid email")) {
            return "Email không hợp lệ. Vui lòng kiểm tra lại.";
        } else if (lowerError.contains("user-disabled") || lowerError.contains("user disabled")) {
            return "Tài khoản này đã bị vô hiệu hóa.";
        } else if (lowerError.contains("user-not-found") || lowerError.contains("user not found")) {
            return "Không tìm thấy tài khoản với email này.";
        } else if (lowerError.contains("wrong-password") || lowerError.contains("wrong password")) {
            return "Mật khẩu không chính xác. Vui lòng thử lại.";
        } else if (lowerError.contains("invalid-credential") || lowerError.contains("invalid credential")) {
            return "Thông tin đăng nhập không hợp lệ. Vui lòng kiểm tra lại email và mật khẩu.";
        } else if (lowerError.contains("email-already-in-use") || lowerError.contains("email already in use")) {
            return "Email này đã được sử dụng. Vui lòng sử dụng email khác.";
        } else if (lowerError.contains("weak-password") || lowerError.contains("weak password")) {
            return "Mật khẩu quá yếu. Vui lòng sử dụng mật khẩu mạnh hơn.";
        } else if (lowerError.contains("operation-not-allowed") || lowerError.contains("operation not allowed")) {
            return "Thao tác này không được phép.";
        } else if (lowerError.contains("too-many-requests") || lowerError.contains("too many requests")) {
            return "Quá nhiều yêu cầu. Vui lòng thử lại sau.";
        } else if (lowerError.contains("requires-recent-login") || lowerError.contains("requires recent login")) {
            return "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.";
        }
        
        // Firebase Firestore Errors
        else if (lowerError.contains("permission") || lowerError.contains("permission-denied")) {
            return "Bạn không có quyền thực hiện thao tác này.";
        } else if (lowerError.contains("not-found") || lowerError.contains("not found")) {
            return "Không tìm thấy dữ liệu yêu cầu.";
        } else if (lowerError.contains("already-exists") || lowerError.contains("already exists")) {
            return "Dữ liệu này đã tồn tại.";
        } else if (lowerError.contains("resource-exhausted") || lowerError.contains("resource exhausted")) {
            return "Đã vượt quá giới hạn. Vui lòng thử lại sau.";
        } else if (lowerError.contains("failed-precondition") || lowerError.contains("failed precondition")) {
            return "Không đáp ứng điều kiện để thực hiện thao tác.";
        } else if (lowerError.contains("aborted")) {
            return "Thao tác đã bị hủy. Vui lòng thử lại.";
        } else if (lowerError.contains("out-of-range") || lowerError.contains("out of range")) {
            return "Giá trị nằm ngoài phạm vi cho phép.";
        } else if (lowerError.contains("unimplemented")) {
            return "Chức năng này chưa được triển khai.";
        } else if (lowerError.contains("internal")) {
            return "Lỗi hệ thống nội bộ. Vui lòng thử lại sau.";
        } else if (lowerError.contains("unavailable")) {
            return "Dịch vụ tạm thời không khả dụng. Vui lòng thử lại sau.";
        } else if (lowerError.contains("data-loss") || lowerError.contains("data loss")) {
            return "Mất dữ liệu không thể khôi phục.";
        } else if (lowerError.contains("unauthenticated")) {
            return "Bạn chưa đăng nhập. Vui lòng đăng nhập để tiếp tục.";
        } else if (lowerError.contains("deadline-exceeded") || lowerError.contains("deadline exceeded")) {
            return "Yêu cầu đã hết thời gian chờ. Vui lòng thử lại.";
        } else if (lowerError.contains("cancelled")) {
            return "Thao tác đã bị hủy.";
        } else if (lowerError.contains("invalid-argument") || lowerError.contains("invalid argument") || lowerError.contains("invalid")) {
            return "Dữ liệu không hợp lệ. Vui lòng kiểm tra lại.";
        }
        
        // Network Errors
        else if (lowerError.contains("network") || lowerError.contains("connection")) {
            return "Lỗi kết nối mạng. Vui lòng kiểm tra kết nối internet của bạn.";
        } else if (lowerError.contains("timeout")) {
            return "Yêu cầu đã hết thời gian chờ. Vui lòng thử lại.";
        }
        
        // Default error message
        return "Đã xảy ra lỗi. Vui lòng thử lại.";
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
