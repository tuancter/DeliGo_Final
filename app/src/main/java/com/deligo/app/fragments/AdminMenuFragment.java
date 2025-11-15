package com.deligo.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.deligo.app.R;
import com.deligo.app.activities.AdminMainActivity;

/**
 * Placeholder fragment for Admin Menu functionality
 * Full implementation would mirror AdminMenuActivity logic
 */
public class AdminMenuFragment extends Fragment {

    private ProgressBar progressBar;
    private TextView tvPlaceholder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_menu, container, false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        setupHeader(view);
        progressBar = view.findViewById(R.id.progressBar);
        tvPlaceholder = view.findViewById(R.id.tvPlaceholder);
        
        // For now, show placeholder
        tvPlaceholder.setText("Admin Menu - Coming Soon\n\nThis feature manages restaurant menu items and categories.");
    }

    private void setupHeader(View view) {
        TextView headerTitle = view.findViewById(R.id.headerTitle);
        if (headerTitle != null) {
            headerTitle.setText(R.string.nav_menu);
        }
        View backButton = view.findViewById(R.id.headerBackButton);
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                if (!isAdded()) {
                    return;
                }
                Intent intent = new Intent(requireContext(), AdminMainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                requireActivity().finish();
            });
        }
    }
}
