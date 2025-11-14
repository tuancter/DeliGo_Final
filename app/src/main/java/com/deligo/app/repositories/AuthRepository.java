package com.deligo.app.repositories;

import com.deligo.app.models.User;

public interface AuthRepository {
    void registerUser(String fullName, String email, String phone, 
                     String password, String role, AuthCallback callback);
    
    void loginUser(String email, String password, AuthCallback callback);
    
    void logoutUser();
    
    User getCurrentUser();
    
    interface AuthCallback {
        void onSuccess(User user);
        void onError(String message);
    }
}
