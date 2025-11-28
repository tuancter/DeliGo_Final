package com.deligo.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
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
import android.widget.Button;

public class LoginActivity extends AppCompatActivity {
    
    private EditText etEmail;
    private EditText etPassword;
    private CheckBox cbShowPassword;
    private Button btnLogin;
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
        cbShowPassword = findViewById(R.id.cbShowPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);
        tvRegister = findViewById(R.id.tvRegister);
    }
    
    private void initViewModel() {
        ViewModelFactory factory = new ViewModelFactory();
        authViewModel = new ViewModelProvider(this, factory).get(AuthViewModel.class);
    }
    
    private void setupListeners() {
        btnLogin.setOnClickListener(v -> handleLogin());
        
        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
        
        cbShowPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            etPassword.setSelection(etPassword.getText().length());
        });
    }
    
    private void handleLogin() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        
        // Validation
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email là bắt buộc");
            etEmail.requestFocus();
            return;
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getString(R.string.validation_email_invalid));
            etEmail.requestFocus();
            return;
        }
        
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Mật khẩu là bắt buộc");
            etPassword.requestFocus();
            return;
        }
        
        if (password.length() < 6) {
            etPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
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
                    UIHelper.showSuccessToast(this, getString(R.string.success_login));
                    navigateToMainScreen(authState.getUser());
                    break;
                    
                case ERROR:
                    showLoading(false);
                    String errorMessage = authState.getMessage() != null ? 
                        authState.getMessage() : "Đăng nhập thất bại";
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
    
    private void navigateToMainScreen(User user) {
        if (user == null) {
            UIHelper.showErrorToast(this, getString(R.string.toast_user_not_found));
            return;
        }
        
        Intent intent;
        String role = user.getRole();
        
        if ("admin".equalsIgnoreCase(role)) {
            intent = new Intent(LoginActivity.this, AdminMainActivity.class);
        } else {
            intent = new Intent(LoginActivity.this, CustomerMainActivity.class);
        }
        
        startActivity(intent);
        finish();
    }
}
