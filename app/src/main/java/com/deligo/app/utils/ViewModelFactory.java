package com.deligo.app.utils;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.deligo.app.repositories.AuthRepository;
import com.deligo.app.repositories.AuthRepositoryImpl;
import com.deligo.app.repositories.CartRepository;
import com.deligo.app.repositories.CartRepositoryImpl;
import com.deligo.app.repositories.CategoryRepository;
import com.deligo.app.repositories.CategoryRepositoryImpl;
import com.deligo.app.repositories.FoodRepository;
import com.deligo.app.repositories.FoodRepositoryImpl;
import com.deligo.app.repositories.OrderRepository;
import com.deligo.app.repositories.OrderRepositoryImpl;
import com.deligo.app.repositories.ProfileRepository;
import com.deligo.app.repositories.ProfileRepositoryImpl;
import com.deligo.app.repositories.ReviewRepository;
import com.deligo.app.repositories.ReviewRepositoryImpl;
import com.deligo.app.repositories.ComplaintRepository;
import com.deligo.app.repositories.ComplaintRepositoryImpl;
import com.deligo.app.repositories.AdminFoodRepository;
import com.deligo.app.repositories.AdminFoodRepositoryImpl;
import com.deligo.app.repositories.AdminCategoryRepository;
import com.deligo.app.repositories.AdminCategoryRepositoryImpl;
import com.deligo.app.repositories.StatisticsRepository;
import com.deligo.app.repositories.StatisticsRepositoryImpl;
import com.deligo.app.viewmodels.AuthViewModel;
import com.deligo.app.viewmodels.CartViewModel;
import com.deligo.app.viewmodels.MenuViewModel;
import com.deligo.app.viewmodels.OrderViewModel;
import com.deligo.app.viewmodels.ProfileViewModel;
import com.deligo.app.viewmodels.ReviewViewModel;
import com.deligo.app.viewmodels.ComplaintViewModel;
import com.deligo.app.viewmodels.AdminMenuViewModel;
import com.deligo.app.viewmodels.AdminOrderViewModel;
import com.deligo.app.viewmodels.StatisticsViewModel;

public class ViewModelFactory implements ViewModelProvider.Factory {
    private final AuthRepository authRepository;
    private final FoodRepository foodRepository;
    private final CategoryRepository categoryRepository;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final ProfileRepository profileRepository;
    private final ReviewRepository reviewRepository;
    private final ComplaintRepository complaintRepository;
    private final AdminFoodRepository adminFoodRepository;
    private final AdminCategoryRepository adminCategoryRepository;
    private final StatisticsRepository statisticsRepository;
    
    public ViewModelFactory(AuthRepository authRepository, FoodRepository foodRepository, CategoryRepository categoryRepository, CartRepository cartRepository, OrderRepository orderRepository, ProfileRepository profileRepository, ReviewRepository reviewRepository, ComplaintRepository complaintRepository, AdminFoodRepository adminFoodRepository, AdminCategoryRepository adminCategoryRepository, StatisticsRepository statisticsRepository) {
        this.authRepository = authRepository;
        this.foodRepository = foodRepository;
        this.categoryRepository = categoryRepository;
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
        this.profileRepository = profileRepository;
        this.reviewRepository = reviewRepository;
        this.complaintRepository = complaintRepository;
        this.adminFoodRepository = adminFoodRepository;
        this.adminCategoryRepository = adminCategoryRepository;
        this.statisticsRepository = statisticsRepository;
    }
    
    // Default constructor with implementations
    public ViewModelFactory() {
        this.authRepository = new AuthRepositoryImpl();
        this.foodRepository = new FoodRepositoryImpl();
        this.categoryRepository = new CategoryRepositoryImpl();
        this.cartRepository = new CartRepositoryImpl();
        this.orderRepository = new OrderRepositoryImpl();
        this.profileRepository = new ProfileRepositoryImpl();
        this.reviewRepository = new ReviewRepositoryImpl();
        this.complaintRepository = new ComplaintRepositoryImpl();
        this.adminFoodRepository = new AdminFoodRepositoryImpl();
        this.adminCategoryRepository = new AdminCategoryRepositoryImpl();
        this.statisticsRepository = new StatisticsRepositoryImpl();
    }
    
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(AuthViewModel.class)) {
            return (T) new AuthViewModel(authRepository);
        } else if (modelClass.isAssignableFrom(MenuViewModel.class)) {
            return (T) new MenuViewModel(foodRepository, categoryRepository);
        } else if (modelClass.isAssignableFrom(CartViewModel.class)) {
            return (T) new CartViewModel(cartRepository);
        } else if (modelClass.isAssignableFrom(OrderViewModel.class)) {
            return (T) new OrderViewModel(orderRepository, cartRepository);
        } else if (modelClass.isAssignableFrom(ProfileViewModel.class)) {
            return (T) new ProfileViewModel(profileRepository, orderRepository);
        } else if (modelClass.isAssignableFrom(ReviewViewModel.class)) {
            return (T) new ReviewViewModel(reviewRepository);
        } else if (modelClass.isAssignableFrom(ComplaintViewModel.class)) {
            return (T) new ComplaintViewModel(complaintRepository);
        } else if (modelClass.isAssignableFrom(AdminMenuViewModel.class)) {
            return (T) new AdminMenuViewModel(adminFoodRepository, adminCategoryRepository, foodRepository, categoryRepository);
        } else if (modelClass.isAssignableFrom(AdminOrderViewModel.class)) {
            return (T) new AdminOrderViewModel(orderRepository);
        } else if (modelClass.isAssignableFrom(StatisticsViewModel.class)) {
            return (T) new StatisticsViewModel(statisticsRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
