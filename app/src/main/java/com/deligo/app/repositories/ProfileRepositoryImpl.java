package com.deligo.app.repositories;

import com.deligo.app.models.User;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfileRepositoryImpl implements ProfileRepository {
    private final FirebaseFirestore firestore;
    
    public ProfileRepositoryImpl() {
        this.firestore = FirebaseFirestore.getInstance();
    }
    
    @Override
    public void getUserProfile(String userId, DataCallback<User> callback) {
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null) {
                        user.setUserId(documentSnapshot.getId());
                        callback.onSuccess(user);
                    } else {
                        callback.onError("Failed to parse user data");
                    }
                } else {
                    callback.onError("User not found");
                }
            })
            .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
    
    @Override
    public void updateProfile(String userId, String fullName, String phone, ActionCallback callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", fullName);
        updates.put("phone", phone);
        
        firestore.collection("users").document(userId)
            .update(updates)
            .addOnSuccessListener(aVoid -> callback.onSuccess())
            .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}
