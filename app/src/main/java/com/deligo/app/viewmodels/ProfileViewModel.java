package com.deligo.app.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.deligo.app.models.Order;
import com.deligo.app.models.User;
import com.deligo.app.repositories.OrderRepository;
import com.deligo.app.repositories.ProfileRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class ProfileViewModel extends ViewModel {
    private final MutableLiveData<User> userProfile = new MutableLiveData<>();
    private final MutableLiveData<List<Order>> orderHistory = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> profileUpdated = new MutableLiveData<>(false);

    private final ProfileRepository profileRepository;
    private final OrderRepository orderRepository;
    private final FirebaseAuth firebaseAuth;

    public ProfileViewModel(ProfileRepository profileRepository, OrderRepository orderRepository) {
        this.profileRepository = profileRepository;
        this.orderRepository = orderRepository;
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    public LiveData<User> getUserProfile() {
        return userProfile;
    }

    public LiveData<List<Order>> getOrderHistory() {
        return orderHistory;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<Boolean> getProfileUpdated() {
        return profileUpdated;
    }

    public void loadProfile() {
        String userId = firebaseAuth.getCurrentUser() != null ? firebaseAuth.getCurrentUser().getUid() : null;
        if (userId == null) {
            errorMessage.setValue("User not authenticated");
            return;
        }

        isLoading.setValue(true);
        profileRepository.getUserProfile(userId, new ProfileRepository.DataCallback<User>() {
            @Override
            public void onSuccess(User user) {
                userProfile.setValue(user);
                isLoading.setValue(false);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
                isLoading.setValue(false);
            }
        });
    }

    public void updateProfile(String fullName, String phone, String address) {
        String userId = firebaseAuth.getCurrentUser() != null ? firebaseAuth.getCurrentUser().getUid() : null;
        if (userId == null) {
            errorMessage.setValue("User not authenticated");
            return;
        }

        isLoading.setValue(true);
        profileRepository.updateProfile(userId, fullName, phone, address, new ProfileRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                profileUpdated.setValue(true);
                isLoading.setValue(false);
                // Reload profile to get updated data
                loadProfile();
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
                isLoading.setValue(false);
                profileUpdated.setValue(false);
            }
        });
    }

    public void loadOrderHistory() {
        String userId = firebaseAuth.getCurrentUser() != null ? firebaseAuth.getCurrentUser().getUid() : null;
        if (userId == null) {
            errorMessage.setValue("User not authenticated");
            return;
        }

        isLoading.setValue(true);
        orderRepository.getOrdersByCustomer(userId, new OrderRepository.DataCallback<List<Order>>() {
            @Override
            public void onSuccess(List<Order> orders) {
                orderHistory.setValue(orders);
                isLoading.setValue(false);
            }

            @Override
            public void onError(String message) {
                errorMessage.setValue(message);
                isLoading.setValue(false);
            }
        });
    }

    public void resetProfileUpdated() {
        profileUpdated.setValue(false);
    }
}
