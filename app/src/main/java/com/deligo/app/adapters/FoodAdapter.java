package com.deligo.app.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.deligo.app.R;
import com.deligo.app.models.Food;
import com.deligo.app.repositories.ReviewRepository;
import com.deligo.app.repositories.ReviewRepositoryImpl;
import com.deligo.app.utils.CurrencyUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {
    private List<Food> foodList = new ArrayList<>();
    private OnFoodClickListener listener;
    private ReviewRepository reviewRepository;
    private com.deligo.app.repositories.StatisticsRepository statisticsRepository;

    public interface OnFoodClickListener {
        void onFoodClick(Food food);
    }

    public FoodAdapter(OnFoodClickListener listener) {
        this.listener = listener;
        this.reviewRepository = new ReviewRepositoryImpl();
        this.statisticsRepository = new com.deligo.app.repositories.StatisticsRepositoryImpl();
    }

    public void setFoodList(List<Food> foodList) {
        this.foodList = foodList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_food, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        Food food = foodList.get(position);
        holder.bind(food);
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    class FoodViewHolder extends RecyclerView.ViewHolder {
        private final ImageView foodImageView;
        private final TextView foodNameTextView;
        private final TextView foodPriceTextView;
        private final TextView foodRatingTextView;
        private final TextView foodSoldCountTextView;
        private final TextView availabilityTextView;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            foodImageView = itemView.findViewById(R.id.foodImageView);
            foodNameTextView = itemView.findViewById(R.id.foodNameTextView);
            foodPriceTextView = itemView.findViewById(R.id.foodPriceTextView);
            foodRatingTextView = itemView.findViewById(R.id.foodRatingTextView);
            foodSoldCountTextView = itemView.findViewById(R.id.foodSoldCountTextView);
            availabilityTextView = itemView.findViewById(R.id.availabilityTextView);
        }

        public void bind(Food food) {
            foodNameTextView.setText(food.getName());
            foodPriceTextView.setText(CurrencyUtils.formatVND(food.getPrice()));

            // Set availability status
            if (food.isAvailable()) {
                availabilityTextView.setText("Đang bán");
                availabilityTextView.setTextColor(Color.WHITE);
                availabilityTextView.setBackgroundColor(Color.parseColor("#4CAF50"));
            } else {
                availabilityTextView.setText("Hết hàng");
                availabilityTextView.setTextColor(Color.WHITE);
                availabilityTextView.setBackgroundColor(Color.parseColor("#9E9E9E"));
            }

            // Load sold count from completed orders
            statisticsRepository.getTotalSoldCountForFood(food.getFoodId(), new com.deligo.app.repositories.StatisticsRepository.DataCallback<Integer>() {
                @Override
                public void onSuccess(Integer soldCount) {
                    if (soldCount != null && soldCount > 0) {
                        foodSoldCountTextView.setVisibility(View.VISIBLE);
                        foodSoldCountTextView.setText(String.format(Locale.US, "Đã bán: %d", soldCount));
                    } else {
                        foodSoldCountTextView.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onError(String message) {
                    foodSoldCountTextView.setVisibility(View.GONE);
                }
            });

            // Load average rating
            reviewRepository.getAverageRating(food.getFoodId(), new ReviewRepository.DataCallback<Double>() {
                @Override
                public void onSuccess(Double rating) {
                    if (rating != null && rating > 0) {
                        foodRatingTextView.setVisibility(View.VISIBLE);
                        foodRatingTextView.setText(String.format(Locale.US, "★ %.1f", rating));
                    } else {
                        foodRatingTextView.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onError(String message) {
                    foodRatingTextView.setVisibility(View.GONE);
                }
            });

            // Load image using Glide
            Glide.with(itemView.getContext())
                    .load(food.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .centerCrop()
                    .into(foodImageView);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFoodClick(food);
                }
            });
        }
    }
}
