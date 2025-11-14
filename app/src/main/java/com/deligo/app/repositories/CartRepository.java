package com.deligo.app.repositories;

import com.deligo.app.models.CartItem;

import java.util.List;

public interface CartRepository {
    void getCartItems(String userId, DataCallback<List<CartItem>> callback);
    void addToCart(String userId, String foodId, int quantity, String note, ActionCallback callback);
    void updateCartItem(String cartItemId, int quantity, ActionCallback callback);
    void removeCartItem(String cartItemId, ActionCallback callback);
    void clearCart(String userId, ActionCallback callback);
    void getCartTotal(String userId, DataCallback<Double> callback);

    interface DataCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }

    interface ActionCallback {
        void onSuccess();
        void onError(String message);
    }
}
