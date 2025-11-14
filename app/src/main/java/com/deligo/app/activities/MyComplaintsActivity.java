package com.deligo.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.deligo.app.R;
import com.deligo.app.adapters.ComplaintAdapter;
import com.deligo.app.utils.ViewModelFactory;
import com.deligo.app.viewmodels.ComplaintViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MyComplaintsActivity extends AppCompatActivity {
    private RecyclerView complaintsRecyclerView;
    private LinearLayout emptyStateLayout;
    private ProgressBar progressBar;

    private ComplaintViewModel complaintViewModel;
    private ComplaintAdapter complaintAdapter;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_complaints);

        initViews();
        setupViewModel();

        // Get current user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please login to view complaints", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        userId = currentUser.getUid();

        setupRecyclerView();
        observeViewModel();
        loadComplaints();
    }

    private void initViews() {
        complaintsRecyclerView = findViewById(R.id.complaintsRecyclerView);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupViewModel() {
        ViewModelFactory factory = new ViewModelFactory();
        complaintViewModel = new ViewModelProvider(this, factory).get(ComplaintViewModel.class);
    }

    private void setupRecyclerView() {
        complaintAdapter = new ComplaintAdapter();
        complaintsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        complaintsRecyclerView.setAdapter(complaintAdapter);
    }

    private void observeViewModel() {
        complaintViewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null && isLoading) {
                progressBar.setVisibility(View.VISIBLE);
                complaintsRecyclerView.setVisibility(View.GONE);
                emptyStateLayout.setVisibility(View.GONE);
            } else {
                progressBar.setVisibility(View.GONE);
            }
        });

        complaintViewModel.getComplaints().observe(this, complaints -> {
            if (complaints != null) {
                if (complaints.isEmpty()) {
                    complaintsRecyclerView.setVisibility(View.GONE);
                    emptyStateLayout.setVisibility(View.VISIBLE);
                } else {
                    complaintsRecyclerView.setVisibility(View.VISIBLE);
                    emptyStateLayout.setVisibility(View.GONE);
                    complaintAdapter.setComplaints(complaints);
                }
            }
        });

        complaintViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadComplaints() {
        complaintViewModel.loadUserComplaints(userId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload complaints when returning to this activity
        loadComplaints();
    }
}
