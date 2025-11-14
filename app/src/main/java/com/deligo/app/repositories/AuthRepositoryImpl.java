package com.deligo.app.repositories;

import com.deligo.app.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AuthRepositoryImpl implements AuthRepository {
    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore firestore;
    private User cachedUser;
    
    public AuthRepositoryImpl() {
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
    }
    
    @Override
    public void registerUser(String fullName, String email, String phone, 
                            String password, String role, AuthCallback callback) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener(authResult -> {
                String userId = authResult.getUser().getUid();
                User user = new User();
                user.setUserId(userId);
                user.setFullName(fullName);
                user.setEmail(email);
                user.setPhone(phone);
                user.setRole(role);
                user.setStatus("active");
                user.setCreatedAt(System.currentTimeMillis());
                
                firestore.collection("users").document(userId)
                    .set(user)
                    .addOnSuccessListener(aVoid -> {
                        cachedUser = user;
                        callback.onSuccess(user);
                    })
                    .addOnFailureListener(e -> {
                        callback.onError(e.getMessage() != null ? e.getMessage() : "Failed to create user profile");
                    });
            })
            .addOnFailureListener(e -> {
                callback.onError(e.getMessage() != null ? e.getMessage() : "Registration failed");
            });
    }
    
    @Override
    public void loginUser(String email, String password, AuthCallback callback) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener(authResult -> {
                String userId = authResult.getUser().getUid();
                
                firestore.collection("users").document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            User user = documentSnapshot.toObject(User.class);
                            if (user != null) {
                                user.setUserId(userId);
                                cachedUser = user;
                                callback.onSuccess(user);
                            } else {
                                callback.onError("Failed to parse user data");
                            }
                        } else {
                            callback.onError("User profile not found");
                        }
                    })
                    .addOnFailureListener(e -> {
                        callback.onError(e.getMessage() != null ? e.getMessage() : "Failed to fetch user profile");
                    });
            })
            .addOnFailureListener(e -> {
                callback.onError(e.getMessage() != null ? e.getMessage() : "Login failed");
            });
    }
    
    @Override
    public void logoutUser() {
        firebaseAuth.signOut();
        cachedUser = null;
    }
    
    @Override
    public User getCurrentUser() {
        if (cachedUser != null) {
            return cachedUser;
        }
        
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            // Return a minimal user object, full data should be fetched via loginUser
            User user = new User();
            user.setUserId(firebaseUser.getUid());
            user.setEmail(firebaseUser.getEmail());
            return user;
        }
        
        return null;
    }
}
