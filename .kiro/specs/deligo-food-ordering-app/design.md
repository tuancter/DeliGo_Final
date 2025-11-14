# Design Document - DeliGo Food Ordering App

## Overview

DeliGo là ứng dụng Android native được xây dựng bằng Kotlin, sử dụng kiến trúc MVVM (Model-View-ViewModel) với Firebase Firestore làm backend. Ứng dụng hỗ trợ hai luồng người dùng riêng biệt: Customer và Admin, với giao diện và chức năng tương ứng cho từng vai trò.

### Technology Stack

- **Language**: Java
- **Architecture**: MVVM (Model-View-ViewModel)
- **UI Framework**: XML Layouts
- **Backend**: Firebase Firestore
- **Authentication**: Firebase Authentication
- **Image Loading**: Glide (load images from internet URLs)
- **Async Operations**: LiveData + Callbacks
- **Navigation**: Intent-based navigation

## Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Presentation Layer                    │
│  ┌──────────────────┐         ┌──────────────────┐     │
│  │  Customer UI     │         │    Admin UI      │     │
│  │  (Activities)    │         │  (Activities)    │     │
│  └────────┬─────────┘         └────────┬─────────┘     │
│           │                             │                │
│  ┌────────▼─────────────────────────────▼─────────┐    │
│  │              ViewModels (LiveData)              │    │
│  └────────┬────────────────────────────┬──────────┘    │
└───────────┼────────────────────────────┼───────────────┘
            │                            │
┌───────────▼────────────────────────────▼───────────────┐
│                    Data Layer                          │
│  ┌──────────────────────────────────────────────┐    │
│  │              Repositories                     │    │
│  └──────────────────┬───────────────────────────┘    │
│                     │                                  │
│  ┌──────────────────▼───────────────────────────┐    │
│  │         Firebase Firestore                    │    │
│  │         (Data Source)                         │    │
│  └───────────────────────────────────────────────┘    │
└────────────────────────────────────────────────────────┘
```

### MVVM Pattern Implementation

- **Model**: Java classes (POJOs) representing Firestore documents
- **View**: Activities với XML Layouts displaying UI
- **ViewModel**: Business logic, LiveData state management, và communication với repositories

## Components and Interfaces

### 1. Authentication Module

#### AuthRepository
```java
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
```

#### AuthViewModel
```java
public class AuthViewModel extends ViewModel {
    private MutableLiveData<AuthState> authState = new MutableLiveData<>();
    private AuthRepository authRepository;
    
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
    
    public void login(String email, String password) { /* similar */ }
    public void logout() { /* similar */ }
}

public class AuthState {
    public enum Status { IDLE, LOADING, SUCCESS, ERROR }
    private Status status;
    private User user;
    private String message;
    
    public static AuthState idle() { /* ... */ }
    public static AuthState loading() { /* ... */ }
    public static AuthState success(User user) { /* ... */ }
    public static AuthState error(String message) { /* ... */ }
}
```

### 2. Menu Module (Customer)

#### FoodRepository
```java
public interface FoodRepository {
    void getAllFoods(DataCallback<List<Food>> callback);
    void getFoodsByCategory(String categoryId, DataCallback<List<Food>> callback);
    void searchFoodsByName(String query, DataCallback<List<Food>> callback);
    void getFoodById(String foodId, DataCallback<Food> callback);
    void getAvailableFoods(DataCallback<List<Food>> callback);
    
    interface DataCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }
}
```

#### CategoryRepository
```java
public interface CategoryRepository {
    void getAllCategories(DataCallback<List<Category>> callback);
    void getCategoryById(String categoryId, DataCallback<Category> callback);
}
```

#### MenuViewModel
```java
public class MenuViewModel extends ViewModel {
    private MutableLiveData<List<Food>> foodList = new MutableLiveData<>();
    private MutableLiveData<List<Category>> categories = new MutableLiveData<>();
    private MutableLiveData<String> selectedCategory = new MutableLiveData<>();
    private FoodRepository foodRepository;
    private CategoryRepository categoryRepository;
    
    public LiveData<List<Food>> getFoodList() { return foodList; }
    public LiveData<List<Category>> getCategories() { return categories; }
    
    public void loadFoods() { /* ... */ }
    public void loadCategories() { /* ... */ }
    public void filterByCategory(String categoryId) { /* ... */ }
    public void searchFoods(String query) { /* ... */ }
}
```

### 3. Cart Module

#### CartRepository
```java
public interface CartRepository {
    void getCartItems(String userId, DataCallback<List<CartItem>> callback);
    void addToCart(String userId, String foodId, int quantity, String note, ActionCallback callback);
    void updateCartItem(String cartItemId, int quantity, ActionCallback callback);
    void removeCartItem(String cartItemId, ActionCallback callback);
    void clearCart(String userId, ActionCallback callback);
    void getCartTotal(String userId, DataCallback<Double> callback);
    
    interface ActionCallback {
        void onSuccess();
        void onError(String message);
    }
}
```

#### CartViewModel
```java
public class CartViewModel extends ViewModel {
    private MutableLiveData<List<CartItem>> cartItems = new MutableLiveData<>();
    private MutableLiveData<Double> cartTotal = new MutableLiveData<>();
    private CartRepository cartRepository;
    
    public LiveData<List<CartItem>> getCartItems() { return cartItems; }
    public LiveData<Double> getCartTotal() { return cartTotal; }
    
    public void loadCart() { /* ... */ }
    public void addToCart(String foodId, int quantity, String note) { /* ... */ }
    public void updateQuantity(String cartItemId, int quantity) { /* ... */ }
    public void removeItem(String cartItemId) { /* ... */ }
    public void clearCart() { /* ... */ }
}
```

### 4. Order Module

#### OrderRepository
```java
public interface OrderRepository {
    void createOrder(String customerId, String deliveryAddress, String paymentMethod, 
                    String note, List<CartItem> cartItems, DataCallback<Order> callback);
    void getOrdersByCustomer(String customerId, DataCallback<List<Order>> callback);
    void getAllOrders(DataCallback<List<Order>> callback);
    void getOrderById(String orderId, DataCallback<Order> callback);
    void updateOrderStatus(String orderId, String status, ActionCallback callback);
    void updatePaymentStatus(String orderId, String status, ActionCallback callback);
    void getOrderDetails(String orderId, DataCallback<List<OrderDetail>> callback);
}
```

#### OrderViewModel (Customer)
```java
public class OrderViewModel extends ViewModel {
    private MutableLiveData<List<Order>> orderHistory = new MutableLiveData<>();
    private MutableLiveData<Order> currentOrder = new MutableLiveData<>();
    private OrderRepository orderRepository;
    
    public LiveData<List<Order>> getOrderHistory() { return orderHistory; }
    public LiveData<Order> getCurrentOrder() { return currentOrder; }
    
    public void placeOrder(String deliveryAddress, String paymentMethod, String note) { /* ... */ }
    public void loadOrderHistory() { /* ... */ }
    public void loadOrderDetails(String orderId) { /* ... */ }
}
```

#### AdminOrderViewModel
```java
public class AdminOrderViewModel extends ViewModel {
    private MutableLiveData<List<Order>> orders = new MutableLiveData<>();
    private MutableLiveData<Order> selectedOrder = new MutableLiveData<>();
    private OrderRepository orderRepository;
    
    public LiveData<List<Order>> getOrders() { return orders; }
    public LiveData<Order> getSelectedOrder() { return selectedOrder; }
    
    public void loadAllOrders() { /* ... */ }
    public void acceptOrder(String orderId) { /* ... */ }
    public void updateOrderStatus(String orderId, String status) { /* ... */ }
    public void loadOrderDetails(String orderId) { /* ... */ }
}
```

### 5. Admin Menu Management Module

#### AdminFoodRepository
```java
public interface AdminFoodRepository {
    void addFood(Food food, ActionCallback callback);
    void updateFood(String foodId, Food food, ActionCallback callback);
    void deleteFood(String foodId, ActionCallback callback);
    void toggleFoodAvailability(String foodId, boolean isAvailable, ActionCallback callback);
}
```

#### AdminCategoryRepository
```java
public interface AdminCategoryRepository {
    void addCategory(String categoryName, ActionCallback callback);
    void updateCategory(String categoryId, String categoryName, ActionCallback callback);
    void deleteCategory(String categoryId, ActionCallback callback);
}
```

#### AdminMenuViewModel
```java
public class AdminMenuViewModel extends ViewModel {
    private MutableLiveData<List<Food>> foods = new MutableLiveData<>();
    private MutableLiveData<List<Category>> categories = new MutableLiveData<>();
    private AdminFoodRepository adminFoodRepository;
    private AdminCategoryRepository adminCategoryRepository;
    
    public LiveData<List<Food>> getFoods() { return foods; }
    public LiveData<List<Category>> getCategories() { return categories; }
    
    public void addFood(Food food) { /* ... */ }
    public void updateFood(String foodId, Food food) { /* ... */ }
    public void deleteFood(String foodId) { /* ... */ }
    public void toggleAvailability(String foodId, boolean isAvailable) { /* ... */ }
    
    public void addCategory(String categoryName) { /* ... */ }
    public void updateCategory(String categoryId, String categoryName) { /* ... */ }
    public void deleteCategory(String categoryId) { /* ... */ }
}
```

### 6. Statistics Module (Admin)

#### StatisticsRepository
```java
public interface StatisticsRepository {
    void getOrdersByDateRange(long startDate, long endDate, DataCallback<List<Order>> callback);
    void getTotalRevenue(long startDate, long endDate, DataCallback<Double> callback);
    void getOrderCountByStatus(long startDate, long endDate, DataCallback<Map<String, Integer>> callback);
    void getTopSellingFoods(long startDate, long endDate, int limit, DataCallback<List<FoodSales>> callback);
}

public class FoodSales {
    private Food food;
    private int quantitySold;
    // Constructor, getters, setters
}
```

#### StatisticsViewModel
```java
public class StatisticsViewModel extends ViewModel {
    private MutableLiveData<Double> totalRevenue = new MutableLiveData<>();
    private MutableLiveData<Integer> orderCount = new MutableLiveData<>();
    private MutableLiveData<Map<String, Integer>> ordersByStatus = new MutableLiveData<>();
    private MutableLiveData<List<FoodSales>> topSellingFoods = new MutableLiveData<>();
    private StatisticsRepository statisticsRepository;
    
    public LiveData<Double> getTotalRevenue() { return totalRevenue; }
    public LiveData<Integer> getOrderCount() { return orderCount; }
    public LiveData<Map<String, Integer>> getOrdersByStatus() { return ordersByStatus; }
    public LiveData<List<FoodSales>> getTopSellingFoods() { return topSellingFoods; }
    
    public void loadStatistics(StatisticsPeriod period) { /* ... */ }
}

public enum StatisticsPeriod {
    TODAY, THIS_WEEK, THIS_MONTH
}
```

### 7. Review Module

#### ReviewRepository
```java
public interface ReviewRepository {
    void addReview(String userId, String foodId, int rating, String comment, ActionCallback callback);
    void getReviewsByFood(String foodId, DataCallback<List<Review>> callback);
    void getAverageRating(String foodId, DataCallback<Double> callback);
    void canUserReviewFood(String userId, String foodId, DataCallback<Boolean> callback);
}
```

#### ReviewViewModel
```java
public class ReviewViewModel extends ViewModel {
    private MutableLiveData<List<Review>> reviews = new MutableLiveData<>();
    private MutableLiveData<Double> averageRating = new MutableLiveData<>();
    private ReviewRepository reviewRepository;
    
    public LiveData<List<Review>> getReviews() { return reviews; }
    public LiveData<Double> getAverageRating() { return averageRating; }
    
    public void loadReviews(String foodId) { /* ... */ }
    public void submitReview(String foodId, int rating, String comment) { /* ... */ }
}
```

### 8. Complaint Module

#### ComplaintRepository
```java
public interface ComplaintRepository {
    void submitComplaint(String userId, String orderId, String content, ActionCallback callback);
    void getComplaintsByUser(String userId, DataCallback<List<Complaint>> callback);
    void getAllComplaints(DataCallback<List<Complaint>> callback);
    void updateComplaintStatus(String complaintId, String status, ActionCallback callback);
}
```

#### ComplaintViewModel
```java
public class ComplaintViewModel extends ViewModel {
    private MutableLiveData<List<Complaint>> complaints = new MutableLiveData<>();
    private ComplaintRepository complaintRepository;
    
    public LiveData<List<Complaint>> getComplaints() { return complaints; }
    
    public void submitComplaint(String orderId, String content) { /* ... */ }
    public void loadUserComplaints() { /* ... */ }
    public void loadAllComplaints() { /* ... */ } // Admin only
    public void updateComplaintStatus(String complaintId, String status) { /* ... */ } // Admin only
}
```

### 9. Profile Module

#### ProfileRepository
```java
public interface ProfileRepository {
    void getUserProfile(String userId, DataCallback<User> callback);
    void updateProfile(String userId, String fullName, String phone, ActionCallback callback);
}
```

#### ProfileViewModel
```java
public class ProfileViewModel extends ViewModel {
    private MutableLiveData<User> userProfile = new MutableLiveData<>();
    private MutableLiveData<List<Order>> orderHistory = new MutableLiveData<>();
    private ProfileRepository profileRepository;
    private OrderRepository orderRepository;
    
    public LiveData<User> getUserProfile() { return userProfile; }
    public LiveData<List<Order>> getOrderHistory() { return orderHistory; }
    
    public void loadProfile() { /* ... */ }
    public void updateProfile(String fullName, String phone) { /* ... */ }
    public void loadOrderHistory() { /* ... */ }
}
```

## Data Models

### Firestore Collections Structure

```
users/
  {userId}/
    - fullName: String
    - email: String
    - phone: String
    - role: String ("customer" | "admin")
    - status: String ("active" | "inactive")
    - createdAt: Timestamp

categories/
  {categoryId}/
    - categoryName: String

foods/
  {foodId}/
    - categoryId: String
    - name: String
    - description: String
    - price: Double
    - imageUrl: String
    - isAvailable: Boolean

carts/
  {cartId}/
    - userId: String
    - createdAt: Timestamp
    - updatedAt: Timestamp
    
    cartItems/ (subcollection)
      {cartItemId}/
        - foodId: String
        - quantity: Int
        - price: Double
        - note: String?

orders/
  {orderId}/
    - customerId: String
    - deliveryAddress: String
    - totalAmount: Double
    - paymentMethod: String
    - paymentStatus: String ("pending" | "completed" | "failed")
    - orderStatus: String ("pending" | "accepted" | "preparing" | "completed" | "cancelled")
    - note: String?
    - createdAt: Timestamp
    
    orderDetails/ (subcollection)
      {orderDetailId}/
        - foodId: String
        - quantity: Int
        - unitPrice: Double

reviews/
  {reviewId}/
    - userId: String
    - foodId: String
    - rating: Int (1-5)
    - comment: String?
    - createdAt: Timestamp

complaints/
  {complaintId}/
    - userId: String
    - orderId: String
    - content: String
    - status: String ("pending" | "resolved" | "rejected")
    - createdAt: Timestamp
```

### Java Model Classes (POJOs)

```java
public class User {
    private String userId;
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private String status;
    private long createdAt;
    
    public User() {} // Required for Firestore
    
    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    // ... other getters/setters
}

public class Category {
    private String categoryId;
    private String categoryName;
    
    public Category() {}
    // Getters and Setters
}

public class Food {
    private String foodId;
    private String categoryId;
    private String name;
    private String description;
    private double price;
    private String imageUrl;
    private boolean isAvailable;
    
    public Food() {}
    // Getters and Setters
}

public class CartItem {
    private String cartItemId;
    private String cartId;
    private String foodId;
    private int quantity;
    private double price;
    private String note;
    private Food food; // Populated when retrieved
    
    public CartItem() {}
    // Getters and Setters
}

public class Order {
    private String orderId;
    private String customerId;
    private String deliveryAddress;
    private double totalAmount;
    private String paymentMethod;
    private String paymentStatus;
    private String orderStatus;
    private String note;
    private long createdAt;
    
    public Order() {}
    // Getters and Setters
}

public class OrderDetail {
    private String orderDetailId;
    private String orderId;
    private String foodId;
    private int quantity;
    private double unitPrice;
    private Food food; // Populated when retrieved
    
    public OrderDetail() {}
    // Getters and Setters
}

public class Review {
    private String reviewId;
    private String userId;
    private String foodId;
    private int rating;
    private String comment;
    private long createdAt;
    private User user; // Populated when retrieved
    
    public Review() {}
    // Getters and Setters
}

public class Complaint {
    private String complaintId;
    private String userId;
    private String orderId;
    private String content;
    private String status;
    private long createdAt;
    
    public Complaint() {}
    // Getters and Setters
}
```

## Error Handling

### Error Types

```java
public class AppError {
    public enum ErrorType {
        NETWORK_ERROR,
        AUTHENTICATION_ERROR,
        VALIDATION_ERROR,
        FIRESTORE_ERROR,
        UNKNOWN_ERROR
    }
    
    private ErrorType type;
    private String message;
    
    public AppError(ErrorType type, String message) {
        this.type = type;
        this.message = message;
    }
    
    // Getters
    public ErrorType getType() { return type; }
    public String getMessage() { return message; }
}
```

### Error Handling Strategy

1. **Repository Level**: Wrap Firestore operations trong try-catch blocks, return `Result<T>` hoặc emit errors trong Flow
2. **ViewModel Level**: Handle errors từ repositories, update UI state với error messages
3. **UI Level**: Display error messages qua Snackbar, Toast, hoặc Dialog
4. **Network Errors**: Retry logic với exponential backoff cho failed operations
5. **Validation Errors**: Client-side validation trước khi gửi data đến Firestore

### Example Error Handling

```kotlin
// Repository
override suspend fun addFood(food: Food, imageUri: Uri?): Result<Food> {
    return try {
        val imageUrl = imageUri?.let { uploadImage(it) } ?: ""
        val foodWithImage = food.copy(imageUrl = imageUrl)
        val docRef = firestore.collection("foods").add(foodWithImage).await()
        Result.success(foodWithImage.copy(foodId = docRef.id))
    } catch (e: Exception) {
        Result.failure(AppError.FirestoreError(e.message ?: "Failed to add food"))
    }
}

// ViewModel
fun addFood(food: Food, imageUri: Uri?) {
    viewModelScope.launch {
        _uiState.value = UiState.Loading
        adminFoodRepository.addFood(food, imageUri)
            .onSuccess { 
                _uiState.value = UiState.Success("Food added successfully")
            }
            .onFailure { error ->
                _uiState.value = UiState.Error(error.message ?: "Unknown error")
            }
    }
}
```

## Testing Strategy

### Unit Tests

- **ViewModels**: Test business logic, state management, và interaction với repositories
- **Repositories**: Test data transformation và error handling với mocked Firestore
- **Use Cases**: Test individual business operations
- **Utilities**: Test helper functions và extensions

### Integration Tests

- **Repository + Firestore**: Test với Firebase Emulator Suite
- **End-to-End Flows**: Test complete user journeys (login → browse → add to cart → checkout)

### UI Tests

- **Espresso/Compose Testing**: Test UI interactions và navigation
- **Screenshot Tests**: Verify UI appearance across different screen sizes

### Test Coverage Goals

- ViewModels: 80%+
- Repositories: 70%+
- UI Components: 60%+

## Security Considerations

### Firebase Security Rules

```javascript
rules_version = '2';

service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if true;
    }
  }
}
```

### Password Security

- Passwords được hash bởi Firebase Authentication
- Không lưu plain-text passwords trong Firestore
- Implement password strength requirements (minimum 8 characters, mix of letters and numbers)

### Data Validation

- Client-side validation cho tất cả user inputs
- Server-side validation qua Firebase Security Rules
- Sanitize user inputs để prevent injection attacks

## Performance Optimization

### Firestore Optimization

1. **Indexing**: Create composite indexes cho complex queries
2. **Pagination**: Implement pagination cho large lists (orders, foods)
3. **Caching**: Use Firestore offline persistence
4. **Batch Operations**: Use batch writes khi possible

### Image Optimization

1. **Compression**: Compress images trước khi upload
2. **Thumbnails**: Generate và store thumbnails cho food images
3. **Lazy Loading**: Load images on-demand với Coil

### App Performance

1. **Lazy Initialization**: Use lazy delegates cho heavy objects
2. **Coroutines**: Use structured concurrency để avoid memory leaks
3. **ProGuard**: Enable code shrinking và obfuscation cho release builds

## Navigation Structure

### Customer Flow

```
SplashScreen
    ↓
LoginScreen ←→ RegisterScreen
    ↓
CustomerMainScreen (Bottom Navigation)
    ├── HomeScreen (Menu)
    │   └── FoodDetailScreen
    │       └── ReviewScreen
    ├── CartScreen
    │   └── CheckoutScreen
    │       └── OrderConfirmationScreen
    ├── OrdersScreen
    │   └── OrderDetailScreen
    │       └── ComplaintScreen
    └── ProfileScreen
        └── EditProfileScreen
```

### Admin Flow

```
SplashScreen
    ↓
LoginScreen
    ↓
AdminMainScreen (Bottom Navigation)
    ├── OrderManagementScreen
    │   └── OrderDetailScreen
    ├── MenuManagementScreen
    │   ├── AddFoodScreen
    │   ├── EditFoodScreen
    │   └── CategoryManagementScreen
    ├── StatisticsScreen
    └── ComplaintManagementScreen
```

## Repository Implementation Pattern

### Example Repository Implementation

```java
public class AuthRepositoryImpl implements AuthRepository {
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    
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
                    .addOnSuccessListener(aVoid -> callback.onSuccess(user))
                    .addOnFailureListener(e -> callback.onError(e.getMessage()));
            })
            .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
    
    // ... other methods
}
```

### ViewModel Factory Pattern

```java
public class ViewModelFactory implements ViewModelProvider.Factory {
    private AuthRepository authRepository;
    private FoodRepository foodRepository;
    // ... other repositories
    
    public ViewModelFactory() {
        // Initialize repositories
        this.authRepository = new AuthRepositoryImpl();
        this.foodRepository = new FoodRepositoryImpl();
    }
    
    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        if (modelClass.isAssignableFrom(AuthViewModel.class)) {
            return (T) new AuthViewModel(authRepository);
        } else if (modelClass.isAssignableFrom(MenuViewModel.class)) {
            return (T) new MenuViewModel(foodRepository, categoryRepository);
        }
        // ... other ViewModels
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
```

## Build Configuration

### Gradle Dependencies (build.gradle)

```groovy
dependencies {
    // Firebase
    implementation platform('com.google.firebase:firebase-bom:32.7.0')
    implementation 'com.google.firebase:firebase-auth'
    implementation 'com.google.firebase:firebase-firestore'
    
    // Jetpack
    implementation 'androidx.lifecycle:lifecycle-viewmodel:2.6.2'
    implementation 'androidx.lifecycle:lifecycle-livedata:2.6.2'
    
    // Image Loading
    implementation 'com.github.bumptech.glide:glide:4.16.0'
    annotationProcessor 'com.github.bumptech.glide:compiler:4.16.0'
    
    // UI
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.10.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    implementation 'androidx.recyclerview:recyclerview:1.3.2'
    implementation 'androidx.cardview:cardview:1.0.0'
}
```
