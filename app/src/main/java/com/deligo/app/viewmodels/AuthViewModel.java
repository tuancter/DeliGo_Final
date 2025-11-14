package com.deligo.app.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.deligo.app.models.User;
import com.deligo.app.repositories.AuthRepository;

public class AuthViewModel extends ViewModel {
    private final MutableLiveData<AuthState> authState = new MutableLiveData<>();
    private final AuthRepository authRepository;
    
    public AuthViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
        authState.setValue(AuthState.idle());
    }
    
    public LiveData<AuthState> getAuthState() {
        return authState;
    }
    
    public void register(String fullName, String email, String phone, String password) {
        authState.setValue(AuthState.loading());
        authRepository.registerUser(fullName, email, phone, password, "customer", 
            new AuthRepository.AuthCallback() {
                @Override
                public void onSuccess(User user) {
                    authState.setValue(AuthState.success(user));
                }
                
                @Override
                public void onError(String message) {
                    authState.setValue(AuthState.error(message));
                }
            });
    }
    
    public void login(String email, String password) {
        authState.setValue(AuthState.loading());
        authRepository.loginUser(email, password, 
            new AuthRepository.AuthCallback() {
                @Override
                public void onSuccess(User user) {
                    authState.setValue(AuthState.success(user));
                }
                
                @Override
                public void onError(String message) {
                    authState.setValue(AuthState.error(message));
                }
            });
    }
    
    public void logout() {
        authRepository.logoutUser();
        authState.setValue(AuthState.idle());
    }
    
    public User getCurrentUser() {
        return authRepository.getCurrentUser();
    }
}
