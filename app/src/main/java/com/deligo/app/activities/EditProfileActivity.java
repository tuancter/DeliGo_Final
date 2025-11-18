package com.deligo.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.deligo.app.R;
import com.deligo.app.repositories.OrderRepositoryImpl;
import com.deligo.app.repositories.ProfileRepositoryImpl;
import com.deligo.app.utils.ViewModelFactory;
import com.deligo.app.viewmodels.ProfileViewModel;
import android.widget.EditText;

public class EditProfileActivity extends AppCompatActivity {
    private ProfileViewModel profileViewModel;
    private EditText fullNameEditText;
    private EditText phoneEditText;
    private EditText emailEditText;
    private EditText addressEditText;
    private Button saveButton;
    private Button cancelButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        initViews();
        setupToolbar();
        setupViewModel();
        setupClickListeners();
        observeViewModel();

        // Load current profile
        profileViewModel.loadProfile();
    }

    private void initViews() {
        fullNameEditText = findViewById(R.id.fullNameEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        emailEditText = findViewById(R.id.emailEditText);
        addressEditText = findViewById(R.id.addressEditText);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);
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
        ViewModelFactory factory = new ViewModelFactory();
        profileViewModel = new ViewModelProvider(this, factory).get(ProfileViewModel.class);
    }

    private void setupClickListeners() {
        saveButton.setOnClickListener(v -> {
            String fullName = fullNameEditText.getText() != null ? fullNameEditText.getText().toString().trim() : "";
            String phone = phoneEditText.getText() != null ? phoneEditText.getText().toString().trim() : "";
            String address = addressEditText.getText() != null ? addressEditText.getText().toString().trim() : "";

            // Validate inputs
            if (fullName.isEmpty()) {
                fullNameEditText.setError(getString(R.string.validation_name_required));
                fullNameEditText.requestFocus();
                return;
            }

            if (phone.isEmpty()) {
                phoneEditText.setError(getString(R.string.validation_phone_required));
                phoneEditText.requestFocus();
                return;
            }

            // Update profile
            profileViewModel.updateProfile(fullName, phone, address);
        });

        cancelButton.setOnClickListener(v -> finish());
    }

    private void observeViewModel() {
        profileViewModel.getUserProfile().observe(this, user -> {
            if (user != null) {
                fullNameEditText.setText(user.getFullName());
                phoneEditText.setText(user.getPhone());
                emailEditText.setText(user.getEmail());
                addressEditText.setText(user.getAddress());
            }
        });

        profileViewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            saveButton.setEnabled(!isLoading);
            cancelButton.setEnabled(!isLoading);
        });

        profileViewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        profileViewModel.getProfileUpdated().observe(this, updated -> {
            if (updated) {
                Toast.makeText(this, getString(R.string.toast_profile_updated), Toast.LENGTH_SHORT).show();
                profileViewModel.resetProfileUpdated();
                finish();
            }
        });
    }
}
