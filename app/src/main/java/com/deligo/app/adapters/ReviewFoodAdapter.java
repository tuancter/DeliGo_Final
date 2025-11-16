package com.deligo.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.deligo.app.R;
import com.deligo.app.models.OrderDetail;
import com.deligo.app.utils.CurrencyUtils;

import java.util.ArrayList;
import java.util.List;

public class ReviewFoodAdapter extends RecyclerView.Adapter<ReviewFoodAdapter.ReviewFoodViewHolder> {
    private List<OrderDetail> orderDetails = new ArrayList<>();
    private OnReviewClickListener reviewClickListener;

    public interface OnReviewClickListener {
        void onReviewClick(OrderDetail orderDetail);
    }

    public ReviewFoodAdapter(OnReviewClickListener listener) {
        this.reviewClickListener = listener;
    }

    public void setOrderDetails(List<OrderDetail> orderDetails) {
        this.orderDetails = orderDetails;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReviewFoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review_food, parent, false);
        return new ReviewFoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewFoodViewHolder holder, int position) {
        OrderDetail orderDetail = orderDetails.get(position);
        holder.bind(orderDetail, reviewClickListener);
    }

    @Override
    public int getItemCount() {
        return orderDetails.size();
    }

    static class ReviewFoodViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivFoodImage;
        private TextView tvFoodName;
        private TextView tvFoodPrice;
        private TextView tvQuantity;
        private Button btnReview;

        public ReviewFoodViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFoodImage = itemView.findViewById(R.id.ivFoodImage);
            tvFoodName = itemView.findViewById(R.id.tvFoodName);
            tvFoodPrice = itemView.findViewById(R.id.tvFoodPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnReview = itemView.findViewById(R.id.btnReview);
        }

        public void bind(OrderDetail orderDetail, OnReviewClickListener listener) {
            if (orderDetail.getFood() != null) {
                // Food name
                tvFoodName.setText(orderDetail.getFood().getName());

                // Food price
                tvFoodPrice.setText(CurrencyUtils.formatVND(orderDetail.getUnitPrice()));

                // Quantity
                tvQuantity.setText(itemView.getContext().getString(R.string.quantity_format, orderDetail.getQuantity()));

                // Load food image
                Glide.with(itemView.getContext())
                        .load(orderDetail.getFood().getImageUrl())
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .centerCrop()
                        .into(ivFoodImage);

                // Review button click
                btnReview.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onReviewClick(orderDetail);
                    }
                });
            } else {
                tvFoodName.setText("Unknown Item");
                tvFoodPrice.setText(CurrencyUtils.formatVND(orderDetail.getUnitPrice()));
                tvQuantity.setText(itemView.getContext().getString(R.string.quantity_format, orderDetail.getQuantity()));
            }
        }
    }
}
