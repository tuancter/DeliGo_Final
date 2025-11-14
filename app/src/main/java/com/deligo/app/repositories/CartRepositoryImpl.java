package com.deligo.app.repositories;

import com.deligo.app.models.Cart;
import com.deligo.app.models.CartItem;
import com.deligo.app.models.Food;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartRepositoryImpl implements CartRepository {
    private final FirebaseFirestore firestore;
    private final FoodRepository foodRepository;

    public CartRepositoryImpl() {
        this.firestore = FirebaseFirestore.getInstance();
        this.foodRepository = new FoodRepositoryImpl();
    }

    @Override
    public void getCartItems(String userId, DataCallback<List<CartItem>> callback) {
        // First, get or create the cart for the user
        getOrCreateCart(userId, new DataCallback<String>() {
            @Override
            public void onSuccess(String cartId) {
                // Get cart items from subcollection
                firestore.collection("carts")
                        .document(cartId)
                        .collection("cartItems")
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            List<CartItem> cartItems = new ArrayList<>();
                            int totalItems = queryDocumentSnapshots.size();
                            
                            if (totalItems == 0) {
                                callback.onSuccess(cartItems);
                                return;
                            }

                            final int[] processedCount = {0};
                            
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                CartItem cartItem = document.toObject(CartItem.class);
                                cartItem.setCartItemId(document.getId());
                                cartItem.setCartId(cartId);
                                
                                // Fetch food details for each cart item
                                foodRepository.getFoodById(cartItem.getFoodId(), new FoodRepository.DataCallback<Food>() {
                                    @Override
                                    public void onSuccess(Food food) {
                                        cartItem.setFood(food);
                                        cartItems.add(cartItem);
                                        processedCount[0]++;
                                        
                                        if (processedCount[0] == totalItems) {
                                            callback.onSuccess(cartItems);
                                        }
                                    }

                                    @Override
                                    public void onError(String message) {
                                        // Still add the cart item even if food fetch fails
                                        cartItems.add(cartItem);
                                        processedCount[0]++;
                                        
                                        if (processedCount[0] == totalItems) {
                                            callback.onSuccess(cartItems);
                                        }
                                    }
                                });
                            }
                        })
                        .addOnFailureListener(e -> callback.onError(e.getMessage()));
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    @Override
    public void addToCart(String userId, String foodId, int quantity, String note, ActionCallback callback) {
        // Get or create cart
        getOrCreateCart(userId, new DataCallback<String>() {
            @Override
            public void onSuccess(String cartId) {
                // Check if item already exists in cart
                firestore.collection("carts")
                        .document(cartId)
                        .collection("cartItems")
                        .whereEqualTo("foodId", foodId)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                // Update existing cart item
                                QueryDocumentSnapshot existingItem = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                                CartItem cartItem = existingItem.toObject(CartItem.class);
                                int newQuantity = cartItem.getQuantity() + quantity;
                                
                                updateCartItem(existingItem.getId(), newQuantity, callback);
                            } else {
                                // Get food price first
                                foodRepository.getFoodById(foodId, new FoodRepository.DataCallback<Food>() {
                                    @Override
                                    public void onSuccess(Food food) {
                                        // Create new cart item
                                        Map<String, Object> cartItemData = new HashMap<>();
                                        cartItemData.put("foodId", foodId);
                                        cartItemData.put("quantity", quantity);
                                        cartItemData.put("price", food.getPrice());
                                        cartItemData.put("note", note);
                                        
                                        firestore.collection("carts")
                                                .document(cartId)
                                                .collection("cartItems")
                                                .add(cartItemData)
                                                .addOnSuccessListener(documentReference -> {
                                                    // Update cart updatedAt timestamp
                                                    updateCartTimestamp(cartId);
                                                    callback.onSuccess();
                                                })
                                                .addOnFailureListener(e -> callback.onError(e.getMessage()));
                                    }

                                    @Override
                                    public void onError(String message) {
                                        callback.onError(message);
                                    }
                                });
                            }
                        })
                        .addOnFailureListener(e -> callback.onError(e.getMessage()));
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    @Override
    public void updateCartItem(String cartItemId, int quantity, ActionCallback callback) {
        // Find the cart item by searching all carts
        firestore.collectionGroup("cartItems")
                .whereEqualTo("__name__", cartItemId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentReference cartItemRef = queryDocumentSnapshots.getDocuments().get(0).getReference();
                        
                        cartItemRef.update("quantity", quantity)
                                .addOnSuccessListener(aVoid -> {
                                    // Update cart timestamp
                                    String cartId = cartItemRef.getParent().getParent().getId();
                                    updateCartTimestamp(cartId);
                                    callback.onSuccess();
                                })
                                .addOnFailureListener(e -> callback.onError(e.getMessage()));
                    } else {
                        callback.onError("Cart item not found");
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void removeCartItem(String cartItemId, ActionCallback callback) {
        // Find and delete the cart item
        firestore.collectionGroup("cartItems")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean found = false;
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        if (document.getId().equals(cartItemId)) {
                            found = true;
                            String cartId = document.getReference().getParent().getParent().getId();
                            
                            document.getReference().delete()
                                    .addOnSuccessListener(aVoid -> {
                                        updateCartTimestamp(cartId);
                                        callback.onSuccess();
                                    })
                                    .addOnFailureListener(e -> callback.onError(e.getMessage()));
                            break;
                        }
                    }
                    
                    if (!found) {
                        callback.onError("Cart item not found");
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void clearCart(String userId, ActionCallback callback) {
        getOrCreateCart(userId, new DataCallback<String>() {
            @Override
            public void onSuccess(String cartId) {
                firestore.collection("carts")
                        .document(cartId)
                        .collection("cartItems")
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (queryDocumentSnapshots.isEmpty()) {
                                callback.onSuccess();
                                return;
                            }
                            
                            int totalItems = queryDocumentSnapshots.size();
                            final int[] deletedCount = {0};
                            
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                document.getReference().delete()
                                        .addOnSuccessListener(aVoid -> {
                                            deletedCount[0]++;
                                            if (deletedCount[0] == totalItems) {
                                                updateCartTimestamp(cartId);
                                                callback.onSuccess();
                                            }
                                        })
                                        .addOnFailureListener(e -> callback.onError(e.getMessage()));
                            }
                        })
                        .addOnFailureListener(e -> callback.onError(e.getMessage()));
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    @Override
    public void getCartTotal(String userId, DataCallback<Double> callback) {
        getCartItems(userId, new DataCallback<List<CartItem>>() {
            @Override
            public void onSuccess(List<CartItem> cartItems) {
                double total = 0.0;
                for (CartItem item : cartItems) {
                    total += item.getPrice() * item.getQuantity();
                }
                callback.onSuccess(total);
            }

            @Override
            public void onError(String message) {
                callback.onError(message);
            }
        });
    }

    // Helper method to get or create cart for user
    private void getOrCreateCart(String userId, DataCallback<String> callback) {
        firestore.collection("carts")
                .whereEqualTo("userId", userId)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Cart exists
                        String cartId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        callback.onSuccess(cartId);
                    } else {
                        // Create new cart
                        Cart cart = new Cart();
                        cart.setUserId(userId);
                        cart.setCreatedAt(System.currentTimeMillis());
                        cart.setUpdatedAt(System.currentTimeMillis());
                        
                        firestore.collection("carts")
                                .add(cart)
                                .addOnSuccessListener(documentReference -> {
                                    callback.onSuccess(documentReference.getId());
                                })
                                .addOnFailureListener(e -> callback.onError(e.getMessage()));
                    }
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // Helper method to update cart timestamp
    private void updateCartTimestamp(String cartId) {
        firestore.collection("carts")
                .document(cartId)
                .update("updatedAt", System.currentTimeMillis());
    }
}
