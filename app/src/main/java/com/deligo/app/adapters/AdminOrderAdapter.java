package com.deligo.app.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.deligo.app.R;
import com.deligo.app.models.Order;
import com.deligo.app.utils.CurrencyUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.AdminOrderViewHolder> {
    private List<Order> orders = new ArrayList<>();
    private OnOrderActionListener listener;

    public interface OnOrderActionListener {
        void onOrderClick(Order order);
        void onAcceptOrder(Order order);
        void onUpdateStatus(Order order, String newStatus);
    }

    public AdminOrderAdapter(OnOrderActionListener listener) {
        this.listener = listener;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AdminOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_order, parent, false);
        return new AdminOrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminOrderViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    class AdminOrderViewHolder extends RecyclerView.ViewHolder {
        private TextView tvOrderId, tvCustomerId, tvOrderDate, tvOrderStatus, tvTotalAmount;
        private Button btnAccept, btnPreparing, btnComplete, btnCancel;

        public AdminOrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvCustomerId = itemView.findViewById(R.id.tvCustomerId);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnPreparing = itemView.findViewById(R.id.btnPreparing);
            btnComplete = itemView.findViewById(R.id.btnComplete);
            btnCancel = itemView.findViewById(R.id.btnCancel);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onOrderClick(orders.get(position));
                }
            });
        }

        public void bind(Order order) {
            // Order ID
            tvOrderId.setText("Order #" + order.getOrderId().substring(0, Math.min(8, order.getOrderId().length())));

            // Customer ID
            tvCustomerId.setText("Customer: " + order.getCustomerId().substring(0, Math.min(8, order.getCustomerId().length())));

            // Order Date
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            String dateStr = dateFormat.format(new Date(order.getCreatedAt()));
            tvOrderDate.setText(dateStr);

            // Order Status
            tvOrderStatus.setText(capitalizeFirst(order.getOrderStatus()));
            tvOrderStatus.setTextColor(getStatusColor(order.getOrderStatus()));

            // Total Amount
            tvTotalAmount.setText(CurrencyUtils.formatVND(order.getTotalAmount()));

            // Configure buttons based on current status
            configureButtons(order);
        }

        private void configureButtons(Order order) {
            String status = order.getOrderStatus();

            // Hide all buttons first
            btnAccept.setVisibility(View.GONE);
            btnPreparing.setVisibility(View.GONE);
            btnComplete.setVisibility(View.GONE);
            btnCancel.setVisibility(View.GONE);

            // Show appropriate buttons based on status
            switch (status.toLowerCase()) {
                case "pending":
                    btnAccept.setVisibility(View.VISIBLE);
                    btnCancel.setVisibility(View.VISIBLE);
                    btnAccept.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onAcceptOrder(order);
                        }
                    });
                    btnCancel.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onUpdateStatus(order, "cancelled");
                        }
                    });
                    break;

                case "accepted":
                    btnPreparing.setVisibility(View.VISIBLE);
                    btnCancel.setVisibility(View.VISIBLE);
                    btnPreparing.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onUpdateStatus(order, "preparing");
                        }
                    });
                    btnCancel.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onUpdateStatus(order, "cancelled");
                        }
                    });
                    break;

                case "preparing":
                    btnComplete.setVisibility(View.VISIBLE);
                    btnCancel.setVisibility(View.VISIBLE);
                    btnComplete.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onUpdateStatus(order, "completed");
                        }
                    });
                    btnCancel.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onUpdateStatus(order, "cancelled");
                        }
                    });
                    break;

                case "completed":
                case "cancelled":
                    // No buttons for final states
                    break;
            }
        }

        private String capitalizeFirst(String text) {
            if (text == null || text.isEmpty()) {
                return text;
            }
            return text.substring(0, 1).toUpperCase() + text.substring(1);
        }

        private int getStatusColor(String status) {
            switch (status.toLowerCase()) {
                case "pending":
                    return Color.parseColor("#FF9800"); // Orange
                case "accepted":
                case "preparing":
                    return Color.parseColor("#2196F3"); // Blue
                case "completed":
                    return Color.parseColor("#4CAF50"); // Green
                case "cancelled":
                    return Color.parseColor("#F44336"); // Red
                default:
                    return Color.parseColor("#757575"); // Gray
            }
        }
    }
}
