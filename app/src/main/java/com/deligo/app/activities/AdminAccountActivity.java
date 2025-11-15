package com.deligo.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.deligo.app.R;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Standalone screen for admin account management (profile + logout).
 */
public class AdminAccountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_account);

        setupActions();
    }

    private void setupActions() {
        Button manageProfileButton = findViewById(R.id.manageProfileButton);
        Button logoutButton = findViewById(R.id.logoutButton);
        Button btnBackToDashboard = findViewById(R.id.btnBackToDashboard);

        manageProfileButton.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));

        logoutButton.setOnClickListener(v -> showLogoutConfirmation());
        
        btnBackToDashboard.setOnClickListener(v -> finish());
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
            .setTitle(R.string.action_logout)
            .setMessage(R.string.confirm_logout)
            .setPositiveButton(R.string.action_logout, (dialog, which) -> performLogout())
            .setNegativeButton(R.string.action_cancel, null)
            .show();
    }

    private void performLogout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
