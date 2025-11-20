package com.deligo.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.deligo.app.R;
import com.deligo.app.utils.GlideUtils;
import com.deligo.app.models.User;
import com.deligo.app.repositories.AuthRepository;
import com.deligo.app.repositories.AuthRepositoryImpl;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2000; // 2 seconds
    private static final String BACKGROUND_IMAGE_URL = "https://i.pinimg.com/736x/c7/a7/55/c7a7554051337e29db3500ffc29282c4.jpg"; // TODO: Add background image URL
    private AuthRepository authRepository;
    private FirebaseFirestore firestore;
    private ImageView backgroundImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Force light mode for the entire app
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        
        setContentView(R.layout.activity_splash);

        authRepository = new AuthRepositoryImpl();
        firestore = FirebaseFirestore.getInstance();
        
        // Initialize and load background image
        backgroundImage = findViewById(R.id.backgroundImage);
        loadBackgroundImage();

        // Delay for splash screen effect
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            checkAuthenticationAndRoute();
        }, SPLASH_DELAY);
    }
    
    private void loadBackgroundImage() {
        try {
            if (BACKGROUND_IMAGE_URL != null && !BACKGROUND_IMAGE_URL.isEmpty()) {
                GlideUtils.loadImage(this, BACKGROUND_IMAGE_URL, backgroundImage, R.color.design_default_color_primary);
            } else {
                // Set default background color if URL is empty
                backgroundImage.setBackgroundResource(R.color.design_default_color_primary);
            }
        } catch (Exception e) {
            // Handle any exception during image loading
            e.printStackTrace();
            backgroundImage.setBackgroundResource(R.color.design_default_color_primary);
        }
    }

    private void checkAuthenticationAndRoute() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        
        if (currentUser == null) {
            // User not authenticated, go to login
            navigateToLogin();
        } else {
            // User authenticated, check role and route accordingly
            String userId = currentUser.getUid();
            fetchUserRoleAndRoute(userId);
        }
    }

    private void fetchUserRoleAndRoute(String userId) {
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null) {
                        String role = user.getRole();
                        if ("admin".equalsIgnoreCase(role)) {
                            navigateToAdminMain();
                        } else {
                            navigateToCustomerMain();
                        }
                    } else {
                        navigateToLogin();
                    }
                } else {
                    navigateToLogin();
                }
            })
            .addOnFailureListener(e -> {
                navigateToLogin();
            });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToCustomerMain() {
        Intent intent = new Intent(SplashActivity.this, CustomerMainActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToAdminMain() {
        Intent intent = new Intent(SplashActivity.this, AdminMainActivity.class);
        startActivity(intent);
        finish();
    }
}
