package com.deligo.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.deligo.app.R;
import com.deligo.app.models.OrderDetail;
import com.deligo.app.utils.CurrencyUtils;

import java.util.ArrayList;
import java.util.List;

public class OrderDetailAdapter extends RecyclerView.Adapter<OrderDetailAdapter.OrderDetailViewHolder> {
    private List<OrderDetail> orderDetails = new ArrayList<>();

    public void setOrderDetails(List<OrderDetail> orderDetails) {
        this.orderDetails = orderDetails;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_detail, parent, false);
        return new OrderDetailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderDetailViewHolder holder, int position) {
        OrderDetail orderDetail = orderDetails.get(position);
        holder.bind(orderDetail);
    }

    @Override
    public int getItemCount() {
        return orderDetails.size();
    }

    static class OrderDetailViewHolder extends RecyclerView.ViewHolder {
        private TextView tvFoodName, tvQuantity, tvPrice;

        public OrderDetailViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFoodName = itemView.findViewById(R.id.tvFoodName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvPrice = itemView.findViewById(R.id.tvPrice);
        }

        public void bind(OrderDetail orderDetail) {
            // Food name
            if (orderDetail.getFood() != null) {
                tvFoodName.setText(orderDetail.getFood().getName());
            } else {
                tvFoodName.setText("Unknown Item");
            }

            // Quantity
            tvQuantity.setText("x" + orderDetail.getQuantity());

            // Price (unit price * quantity)
            double totalPrice = orderDetail.getUnitPrice() * orderDetail.getQuantity();
            tvPrice.setText(CurrencyUtils.formatVND(totalPrice));
        }
    }
}
