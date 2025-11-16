package com.deligo.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.deligo.app.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    private TextInputEditText currentPasswordEditText;
    private TextInputEditText newPasswordEditText;
    private TextInputEditText confirmPasswordEditText;
    private Button saveButton;
    private Button cancelButton;
    private ProgressBar progressBar;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        firebaseAuth = FirebaseAuth.getInstance();

        initViews();
        setupToolbar();
        setupClickListeners();
    }

    private void initViews() {
        currentPasswordEditText = findViewById(R.id.currentPasswordEditText);
        newPasswordEditText = findViewById(R.id.newPasswordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
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

    private void setupClickListeners() {
        saveButton.setOnClickListener(v -> changePassword());
        cancelButton.setOnClickListener(v -> finish());
    }

    private void changePassword() {
        String currentPassword = currentPasswordEditText.getText() != null 
            ? currentPasswordEditText.getText().toString().trim() : "";
        String newPassword = newPasswordEditText.getText() != null 
            ? newPasswordEditText.getText().toString().trim() : "";
        String confirmPassword = confirmPasswordEditText.getText() != null 
            ? confirmPasswordEditText.getText().toString().trim() : "";

        // Validate inputs
        if (currentPassword.isEmpty()) {
            currentPasswordEditText.setError(getString(R.string.validation_current_password_required));
            currentPasswordEditText.requestFocus();
            return;
        }

        if (newPassword.isEmpty()) {
            newPasswordEditText.setError(getString(R.string.validation_new_password_required));
            newPasswordEditText.requestFocus();
            return;
        }

        if (newPassword.length() < 8) {
            newPasswordEditText.setError(getString(R.string.validation_password_short));
            newPasswordEditText.requestFocus();
            return;
        }

        if (confirmPassword.isEmpty()) {
            confirmPasswordEditText.setError(getString(R.string.validation_confirm_password_required));
            confirmPasswordEditText.requestFocus();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            confirmPasswordEditText.setError(getString(R.string.validation_password_mismatch));
            confirmPasswordEditText.requestFocus();
            return;
        }

        // Get current user
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null || user.getEmail() == null) {
            Toast.makeText(this, getString(R.string.toast_login_required), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Show loading
        progressBar.setVisibility(View.VISIBLE);
        saveButton.setEnabled(false);
        cancelButton.setEnabled(false);

        // Re-authenticate user with current password
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
        
        user.reauthenticate(credential)
            .addOnSuccessListener(aVoid -> {
                // Current password is correct, now update to new password
                user.updatePassword(newPassword)
                    .addOnSuccessListener(aVoid2 -> {
                        progressBar.setVisibility(View.GONE);
                        saveButton.setEnabled(true);
                        cancelButton.setEnabled(true);
                        Toast.makeText(this, getString(R.string.toast_password_changed), Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        saveButton.setEnabled(true);
                        cancelButton.setEnabled(true);
                        Toast.makeText(this, 
                            getString(R.string.error_password_change_failed) + ": " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    });
            })
            .addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                saveButton.setEnabled(true);
                cancelButton.setEnabled(true);
                Toast.makeText(this, getString(R.string.validation_current_password_incorrect), Toast.LENGTH_SHORT).show();
                currentPasswordEditText.setError(getString(R.string.validation_current_password_incorrect));
                currentPasswordEditText.requestFocus();
            });
    }
}
