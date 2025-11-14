package com.deligo.app.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.deligo.app.models.CartItem;
import com.deligo.app.repositories.CartRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class CartViewModel extends ViewModel {
    private final MutableLiveData<List<CartItem>> cartItems = new MutableLiveData<>();
    private final MutableLiveData<Double> cartTotal = new MutableLiveData<>(0.0);
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> successMessage = new MutableLiveData<>();
    
    private final CartRepository cartRepository;
    private final FirebaseAuth firebaseAuth;

    public CartViewModel(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    public LiveData<List<CartItem>> getCartItems() {
        return cartItems;
    }

    public LiveData<Double> getCartTotal() {
        return cartTotal;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<String> getSuccessMessage() {
        return successMessage;
    }

    public void loadCart() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            errorMessage.setValue("User not authenticated");
            return;
        }

        isLoading.setValue(true);
        String userId = currentUser.getUid();
        
        cartRepository.getCartItems(userId, new CartRepository.DataCallback<List<CartItem>>() {
            @Override
            public void onSuccess(List<CartItem> data) {
                cartItems.setValue(data);
                isLoading.setValue(false);
                calculateTotal(data);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
                isLoading.setValue(false);
            }
        });
    }

    public void addToCart(String foodId, int quantity, String note) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            errorMessage.setValue("User not authenticated");
            return;
        }

        isLoading.setValue(true);
        String userId = currentUser.getUid();
        
        cartRepository.addToCart(userId, foodId, quantity, note, new CartRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                successMessage.setValue("Item added to cart");
                isLoading.setValue(false);
                loadCart(); // Reload cart to update UI
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
                isLoading.setValue(false);
            }
        });
    }

    public void updateQuantity(String cartItemId, int quantity) {
        if (quantity <= 0) {
            removeItem(cartItemId);
            return;
        }

        isLoading.setValue(true);
        
        cartRepository.updateCartItem(cartItemId, quantity, new CartRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                isLoading.setValue(false);
                loadCart(); // Reload cart to update UI
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
                isLoading.setValue(false);
            }
        });
    }

    public void removeItem(String cartItemId) {
        isLoading.setValue(true);
        
        cartRepository.removeCartItem(cartItemId, new CartRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                successMessage.setValue("Item removed from cart");
                isLoading.setValue(false);
                loadCart(); // Reload cart to update UI
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
                isLoading.setValue(false);
            }
        });
    }

    public void clearCart() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            errorMessage.setValue("User not authenticated");
            return;
        }

        isLoading.setValue(true);
        String userId = currentUser.getUid();
        
        cartRepository.clearCart(userId, new CartRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                successMessage.setValue("Cart cleared");
                isLoading.setValue(false);
                loadCart(); // Reload cart to update UI
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
                isLoading.setValue(false);
            }
        });
    }

    private void calculateTotal(List<CartItem> items) {
        double total = 0.0;
        if (items != null) {
            for (CartItem item : items) {
                total += item.getPrice() * item.getQuantity();
            }
        }
        cartTotal.setValue(total);
    }
}
