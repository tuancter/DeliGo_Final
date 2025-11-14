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
import com.deligo.app.repositories.AuthRepositoryImpl;
import com.deligo.app.utils.UIHelper;
import com.deligo.app.utils.ViewModelFactory;
import com.deligo.app.viewmodels.AuthState;
import com.deligo.app.viewmodels.AuthViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {
    
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private MaterialButton btnLogin;
    private ProgressBar progressBar;
    private TextView tvRegister;
    
    private AuthViewModel authViewModel;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        initViews();
        initViewModel();
        setupListeners();
        observeAuthState();
    }
    
    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);
        tvRegister = findViewById(R.id.tvRegister);
    }
    
    private void initViewModel() {
        ViewModelFactory factory = new ViewModelFactory(new AuthRepositoryImpl());
        authViewModel = new ViewModelProvider(this, factory).get(AuthViewModel.class);
    }
    
    private void setupListeners() {
        btnLogin.setOnClickListener(v -> handleLogin());
        
        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
    
    private void handleLogin() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        
        // Validation
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
        
        authViewModel.login(email, password);
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
                    UIHelper.showSuccessToast(this, "Login successful!");
                    // TODO: Navigate to appropriate main screen based on user role
                    // For now, just finish the activity
                    finish();
                    break;
                    
                case ERROR:
                    showLoading(false);
                    String errorMessage = authState.getMessage() != null ? 
                        authState.getMessage() : "Login failed";
                    String friendlyMessage = UIHelper.getFirestoreErrorMessage(errorMessage);
                    UIHelper.showErrorSnackbar(findViewById(android.R.id.content), 
                        friendlyMessage, v -> handleLogin());
                    break;
            }
        });
    }
    
    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnLogin.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnLogin.setEnabled(true);
        }
    }
}
