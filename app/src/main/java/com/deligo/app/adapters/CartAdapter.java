package com.deligo.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.deligo.app.R;
import com.deligo.app.models.CartItem;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private List<CartItem> cartItems = new ArrayList<>();
    private OnCartItemActionListener listener;

    public interface OnCartItemActionListener {
        void onQuantityChanged(CartItem cartItem, int newQuantity);
        void onRemoveItem(CartItem cartItem);
    }

    public CartAdapter(OnCartItemActionListener listener) {
        this.listener = listener;
    }

    public void setCartItems(List<CartItem> cartItems) {
        this.cartItems = cartItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem cartItem = cartItems.get(position);
        holder.bind(cartItem);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    class CartViewHolder extends RecyclerView.ViewHolder {
        private final ImageView foodImageView;
        private final TextView foodNameTextView;
        private final TextView unitPriceTextView;
        private final TextView quantityTextView;
        private final TextView itemTotalTextView;
        private final TextView noteTextView;
        private final Button decreaseButton;
        private final Button increaseButton;
        private final ImageButton removeButton;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            foodImageView = itemView.findViewById(R.id.foodImageView);
            foodNameTextView = itemView.findViewById(R.id.foodNameTextView);
            unitPriceTextView = itemView.findViewById(R.id.unitPriceTextView);
            quantityTextView = itemView.findViewById(R.id.quantityTextView);
            itemTotalTextView = itemView.findViewById(R.id.itemTotalTextView);
            noteTextView = itemView.findViewById(R.id.noteTextView);
            decreaseButton = itemView.findViewById(R.id.decreaseButton);
            increaseButton = itemView.findViewById(R.id.increaseButton);
            removeButton = itemView.findViewById(R.id.removeButton);
        }

        public void bind(CartItem cartItem) {
            // Set food name
            if (cartItem.getFood() != null) {
                foodNameTextView.setText(cartItem.getFood().getName());
                
                // Load image using Glide
                Glide.with(itemView.getContext())
                        .load(cartItem.getFood().getImageUrl())
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .centerCrop()
                        .into(foodImageView);
            } else {
                foodNameTextView.setText("Unknown Item");
            }

            // Set prices
            unitPriceTextView.setText(String.format("$%.2f", cartItem.getPrice()));
            double itemTotal = cartItem.getPrice() * cartItem.getQuantity();
            itemTotalTextView.setText(String.format("$%.2f", itemTotal));

            // Set quantity
            quantityTextView.setText(String.valueOf(cartItem.getQuantity()));

            // Set note if exists
            if (cartItem.getNote() != null && !cartItem.getNote().trim().isEmpty()) {
                noteTextView.setVisibility(View.VISIBLE);
                noteTextView.setText("Note: " + cartItem.getNote());
            } else {
                noteTextView.setVisibility(View.GONE);
            }

            // Decrease button click
            decreaseButton.setOnClickListener(v -> {
                if (listener != null) {
                    int newQuantity = cartItem.getQuantity() - 1;
                    listener.onQuantityChanged(cartItem, newQuantity);
                }
            });

            // Increase button click
            increaseButton.setOnClickListener(v -> {
                if (listener != null) {
                    int newQuantity = cartItem.getQuantity() + 1;
                    listener.onQuantityChanged(cartItem, newQuantity);
                }
            });

            // Remove button click
            removeButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRemoveItem(cartItem);
                }
            });
        }
    }
}
