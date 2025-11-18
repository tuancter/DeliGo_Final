package com.deligo.app.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private List<Order> orders = new ArrayList<>();
    private OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    public OrderAdapter(OnOrderClickListener listener) {
        this.listener = listener;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        private TextView tvOrderId, tvOrderDate, tvOrderStatus, tvPaymentStatus, tvTotalAmount;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvPaymentStatus = itemView.findViewById(R.id.tvPaymentStatus);
            tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount);

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

            // Order Date
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            String dateStr = dateFormat.format(new Date(order.getCreatedAt()));
            tvOrderDate.setText(dateStr);

            // Order Status
            tvOrderStatus.setText(capitalizeFirst(order.getOrderStatus()));
            tvOrderStatus.setTextColor(getStatusColor(order.getOrderStatus()));

            // Payment Status
            tvPaymentStatus.setText(capitalizeFirst(order.getPaymentStatus()));
            tvPaymentStatus.setTextColor(getStatusColor(order.getPaymentStatus()));

            // Total Amount
            tvTotalAmount.setText(CurrencyUtils.formatVND(order.getTotalAmount()));
        }

        private String capitalizeFirst(String text) {
            if (text == null || text.isEmpty()) {
                return text;
            }
            return text.substring(0, 1).toUpperCase() + text.substring(1);
        }

        private int getStatusColor(String status) {
            if (status == null) return Color.parseColor("#757575");
            
            String statusLower = status.toLowerCase();
            if (statusLower.contains("chờ") || statusLower.contains("pending")) {
                return Color.parseColor("#FF9800"); // Orange
            } else if (statusLower.contains("nhận") || statusLower.contains("chuẩn bị") || 
                       statusLower.contains("accepted") || statusLower.contains("preparing")) {
                return Color.parseColor("#2196F3"); // Blue
            } else if (statusLower.contains("hoàn thành") || statusLower.contains("completed")) {
                return Color.parseColor("#4CAF50"); // Green
            } else if (statusLower.contains("huỷ") || statusLower.contains("hủy") || 
                       statusLower.contains("cancelled") || statusLower.contains("failed")) {
                return Color.parseColor("#F44336"); // Red
            } else {
                return Color.parseColor("#757575"); // Gray
            }
        }
    }
}
