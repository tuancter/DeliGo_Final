package com.deligo.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.deligo.app.R;
import com.deligo.app.models.User;
import com.deligo.app.repositories.AuthRepositoryImpl;
import com.deligo.app.utils.UIHelper;
import com.deligo.app.utils.ViewModelFactory;
import com.deligo.app.viewmodels.AuthState;
import com.deligo.app.viewmodels.AuthViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {
    
    private TextInputEditText etFullName;
    private TextInputEditText etEmail;
    private TextInputEditText etPhone;
    private TextInputEditText etPassword;
    private MaterialButton btnRegister;
    private ProgressBar progressBar;
    private TextView tvLogin;
    
    private AuthViewModel authViewModel;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        
        initViews();
        initViewModel();
        setupListeners();
        observeAuthState();
    }
    
    private void initViews() {
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);
        tvLogin = findViewById(R.id.tvLogin);
    }
    
    private void initViewModel() {
        ViewModelFactory factory = new ViewModelFactory();
        authViewModel = new ViewModelProvider(this, factory).get(AuthViewModel.class);
    }
    
    private void setupListeners() {
        btnRegister.setOnClickListener(v -> handleRegister());
        
        tvLogin.setOnClickListener(v -> {
            finish(); // Go back to login
        });
    }
    
    private void handleRegister() {
        String fullName = etFullName.getText() != null ? etFullName.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        
        // Validation
        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("Full name is required");
            etFullName.requestFocus();
            return;
        }
        
        if (fullName.length() < 2) {
            etFullName.setError("Full name must be at least 2 characters");
            etFullName.requestFocus();
            return;
        }
        
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email");
            etEmail.requestFocus();
            return;
        }
        
        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Phone is required");
            etPhone.requestFocus();
            return;
        }
        
        if (!android.util.Patterns.PHONE.matcher(phone).matches() || phone.length() < 10) {
            etPhone.setError("Please enter a valid phone number");
            etPhone.requestFocus();
            return;
        }
        
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }
        
        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }
        
        authViewModel.register(fullName, email, phone, password);
    }
    
    private void observeAuthState() {
        authViewModel.getAuthState().observe(this, authState -> {
            if (authState == null) return;
            
            switch (authState.getStatus()) {
                case IDLE:
                    showLoading(false);
                    break;
                    
                case LOADING:
                    showLoading(true);
                    break;
                    
                case SUCCESS:
                    showLoading(false);
                    UIHelper.showSuccessToast(this, "Registration successful!");
                    navigateToMainScreen(authState.getUser());
                    break;
                    
                case ERROR:
                    showLoading(false);
                    String errorMessage = authState.getMessage() != null ? 
                        authState.getMessage() : "Registration failed";
                    String friendlyMessage = UIHelper.getFirestoreErrorMessage(errorMessage);
                    UIHelper.showErrorSnackbar(findViewById(android.R.id.content), 
                        friendlyMessage, v -> handleRegister());
                    break;
            }
        });
    }
    
    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnRegister.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnRegister.setEnabled(true);
        }
    }
    
    private void navigateToMainScreen(User user) {
        if (user == null) {
            UIHelper.showErrorToast(this, "User data not found");
            return;
        }
        
        Intent intent;
        String role = user.getRole();
        
        if ("admin".equalsIgnoreCase(role)) {
            intent = new Intent(RegisterActivity.this, AdminMainActivity.class);
        } else {
            intent = new Intent(RegisterActivity.this, CustomerMainActivity.class);
        }
        
        startActivity(intent);
        finish();
    }
}
