package com.deligo.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.deligo.app.R;
import com.deligo.app.repositories.OrderRepositoryImpl;
import com.deligo.app.repositories.ProfileRepositoryImpl;
import com.deligo.app.utils.UIHelper;
import com.deligo.app.utils.ViewModelFactory;
import com.deligo.app.viewmodels.ProfileViewModel;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileActivity extends AppCompatActivity {
    private ProfileViewModel profileViewModel;
    private TextView fullNameTextView;
    private TextView emailTextView;
    private TextView phoneTextView;
    private Button editProfileButton;
    private Button viewOrderHistoryButton;
    private Button logoutButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        setupToolbar();
        setupViewModel();
        setupClickListeners();
        observeViewModel();

        // Load profile
        profileViewModel.loadProfile();
    }

    private void initViews() {
        fullNameTextView = findViewById(R.id.fullNameTextView);
        emailTextView = findViewById(R.id.emailTextView);
        phoneTextView = findViewById(R.id.phoneTextView);
        editProfileButton = findViewById(R.id.editProfileButton);
        viewOrderHistoryButton = findViewById(R.id.viewOrderHistoryButton);
        logoutButton = findViewById(R.id.logoutButton);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupViewModel() {
        ViewModelFactory factory = new ViewModelFactory(
                null,
                null,
                null,
                null,
                new OrderRepositoryImpl(),
                new ProfileRepositoryImpl()
        );
        profileViewModel = new ViewModelProvider(this, factory).get(ProfileViewModel.class);
    }

    private void setupClickListeners() {
        editProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditProfileActivity.class);
            startActivity(intent);
        });

        viewOrderHistoryButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, OrderHistoryActivity.class);
            startActivity(intent);
        });

        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void observeViewModel() {
        profileViewModel.getUserProfile().observe(this, user -> {
            if (user != null) {
                fullNameTextView.setText(user.getFullName());
                emailTextView.setText(user.getEmail());
                phoneTextView.setText(user.getPhone());
            }
        });

        profileViewModel.getIsLoading().observe(this, isLoading -> {
            UIHelper.showLoading(progressBar, isLoading);
        });

        profileViewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                String friendlyMessage = UIHelper.getFirestoreErrorMessage(errorMessage);
                UIHelper.showErrorSnackbar(findViewById(android.R.id.content), 
                    friendlyMessage, v -> profileViewModel.loadProfile());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload profile when returning to this activity
        profileViewModel.loadProfile();
    }
}
