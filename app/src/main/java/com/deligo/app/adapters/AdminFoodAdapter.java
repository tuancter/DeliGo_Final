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
import com.deligo.app.models.Food;
import com.deligo.app.utils.CurrencyUtils;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminFoodAdapter extends RecyclerView.Adapter<AdminFoodAdapter.FoodViewHolder> {
    private List<Food> foodList = new ArrayList<>();
    private OnFoodActionListener listener;

    public interface OnFoodActionListener {
        void onEditFood(Food food);
        void onDeleteFood(Food food);
        void onToggleAvailability(Food food);
    }

    public AdminFoodAdapter(OnFoodActionListener listener) {
        this.listener = listener;
    }

    public void setFoodList(List<Food> foodList) {
        this.foodList = foodList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_food, parent, false);
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
        private ImageView foodImageView;
        private TextView foodNameTextView;
        private TextView foodPriceTextView;
        private TextView availabilityTextView;
        private Button toggleAvailabilityButton;
        private Button editButton;
        private Button deleteButton;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            foodImageView = itemView.findViewById(R.id.foodImageView);
            foodNameTextView = itemView.findViewById(R.id.foodNameTextView);
            foodPriceTextView = itemView.findViewById(R.id.foodPriceTextView);
            availabilityTextView = itemView.findViewById(R.id.availabilityTextView);
            toggleAvailabilityButton = itemView.findViewById(R.id.toggleAvailabilityButton);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }

        public void bind(Food food) {
            foodNameTextView.setText(food.getName());
            foodPriceTextView.setText(CurrencyUtils.formatVND(food.getPrice()));

            // Load image with Glide
            if (food.getImageUrl() != null && !food.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(food.getImageUrl())
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .into(foodImageView);
            } else {
                foodImageView.setImageResource(R.drawable.ic_launcher_background);
            }

            // Set availability status
            if (food.isAvailable()) {
                availabilityTextView.setText("Available");
                availabilityTextView.setBackgroundColor(itemView.getContext().getResources().getColor(android.R.color.holo_green_light));
            } else {
                availabilityTextView.setText("Unavailable");
                availabilityTextView.setBackgroundColor(itemView.getContext().getResources().getColor(android.R.color.holo_red_light));
            }

            // Set click listeners
            toggleAvailabilityButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onToggleAvailability(food);
                }
            });

            editButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditFood(food);
                }
            });

            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteFood(food);
                }
            });
        }
    }
}
