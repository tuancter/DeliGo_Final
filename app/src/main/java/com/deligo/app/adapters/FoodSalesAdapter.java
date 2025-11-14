package com.deligo.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.deligo.app.R;
import com.deligo.app.models.FoodSales;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FoodSalesAdapter extends RecyclerView.Adapter<FoodSalesAdapter.FoodSalesViewHolder> {
    private List<FoodSales> foodSalesList = new ArrayList<>();

    public void setFoodSalesList(List<FoodSales> foodSalesList) {
        this.foodSalesList = foodSalesList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FoodSalesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_food_sales, parent, false);
        return new FoodSalesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodSalesViewHolder holder, int position) {
        FoodSales foodSales = foodSalesList.get(position);
        holder.bind(foodSales);
    }

    @Override
    public int getItemCount() {
        return foodSalesList.size();
    }

    static class FoodSalesViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivFoodImage;
        private final TextView tvFoodName;
        private final TextView tvFoodPrice;
        private final TextView tvQuantitySold;

        public FoodSalesViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFoodImage = itemView.findViewById(R.id.ivFoodImage);
            tvFoodName = itemView.findViewById(R.id.tvFoodName);
            tvFoodPrice = itemView.findViewById(R.id.tvFoodPrice);
            tvQuantitySold = itemView.findViewById(R.id.tvQuantitySold);
        }

        public void bind(FoodSales foodSales) {
            if (foodSales.getFood() != null) {
                tvFoodName.setText(foodSales.getFood().getName());
                tvFoodPrice.setText(String.format(Locale.getDefault(), "$%.2f", foodSales.getFood().getPrice()));

                // Load image with Glide
                if (foodSales.getFood().getImageUrl() != null && !foodSales.getFood().getImageUrl().isEmpty()) {
                    Glide.with(itemView.getContext())
                            .load(foodSales.getFood().getImageUrl())
                            .placeholder(R.drawable.ic_launcher_background)
                            .error(R.drawable.ic_launcher_background)
                            .into(ivFoodImage);
                }
            }

            tvQuantitySold.setText(String.valueOf(foodSales.getQuantitySold()));
        }
    }
}
