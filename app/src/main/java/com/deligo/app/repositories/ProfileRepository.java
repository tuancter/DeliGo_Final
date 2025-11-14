package com.deligo.app.repositories;

import com.deligo.app.models.User;

public interface ProfileRepository {
    void getUserProfile(String userId, DataCallback<User> callback);
    void updateProfile(String userId, String fullName, String phone, ActionCallback callback);
    
    interface DataCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }
    
    interface ActionCallback {
        void onSuccess();
        void onError(String message);
    }
}
