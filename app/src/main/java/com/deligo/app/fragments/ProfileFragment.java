package com.deligo.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.deligo.app.R;
import com.deligo.app.activities.ChangePasswordActivity;
import com.deligo.app.activities.EditProfileActivity;
import com.deligo.app.activities.LoginActivity;
import com.deligo.app.activities.OrderHistoryActivity;
import com.deligo.app.utils.UIHelper;
import com.deligo.app.utils.ViewModelFactory;
import com.deligo.app.viewmodels.ProfileViewModel;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Fragment displaying user profile
 */
public class ProfileFragment extends Fragment {
    
    private ProfileViewModel profileViewModel;
    private TextView fullNameTextView;
    private TextView emailTextView;
    private TextView phoneTextView;
    private TextView addressTextView;
    private Button editProfileButton;
    private Button changePasswordButton;
    private Button viewOrderHistoryButton;
    private Button logoutButton;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        
        initViews(view);
        setupViewModel();
        setupClickListeners();
        observeViewModel();

        // Load profile
        profileViewModel.loadProfile();
        
        return view;
    }

    private void initViews(View view) {
        fullNameTextView = view.findViewById(R.id.fullNameTextView);
        emailTextView = view.findViewById(R.id.emailTextView);
        phoneTextView = view.findViewById(R.id.phoneTextView);
        addressTextView = view.findViewById(R.id.addressTextView);
        editProfileButton = view.findViewById(R.id.editProfileButton);
        changePasswordButton = view.findViewById(R.id.changePasswordButton);
        viewOrderHistoryButton = view.findViewById(R.id.viewOrderHistoryButton);
        logoutButton = view.findViewById(R.id.logoutButton);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void setupViewModel() {
        ViewModelFactory factory = new ViewModelFactory();
        profileViewModel = new ViewModelProvider(this, factory).get(ProfileViewModel.class);
    }

    private void setupClickListeners() {
        editProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            startActivity(intent);
        });

        changePasswordButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ChangePasswordActivity.class);
            startActivity(intent);
        });

        viewOrderHistoryButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), OrderHistoryActivity.class);
            startActivity(intent);
        });

        logoutButton.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().finish();
            }
        });
    }

    private void observeViewModel() {
        profileViewModel.getUserProfile().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                fullNameTextView.setText(user.getFullName());
                emailTextView.setText(user.getEmail());
                phoneTextView.setText(user.getPhone());
                addressTextView.setText(user.getAddress() != null ? user.getAddress() : "-");
            }
        });

        profileViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            UIHelper.showLoading(progressBar, isLoading);
        });

        profileViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty() && getView() != null) {
                String friendlyMessage = UIHelper.getFirestoreErrorMessage(errorMessage);
                UIHelper.showErrorSnackbar(getView(), 
                    friendlyMessage, v -> profileViewModel.loadProfile());
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload profile when fragment becomes visible
        if (profileViewModel != null) {
            profileViewModel.loadProfile();
        }
    }
}
