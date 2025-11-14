package com.deligo.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.deligo.app.R;
import com.deligo.app.models.User;
import com.deligo.app.repositories.AuthRepository;
import com.deligo.app.repositories.AuthRepositoryImpl;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2000; // 2 seconds
    private AuthRepository authRepository;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        authRepository = new AuthRepositoryImpl();
        firestore = FirebaseFirestore.getInstance();

        // Delay for splash screen effect
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            checkAuthenticationAndRoute();
        }, SPLASH_DELAY);
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
