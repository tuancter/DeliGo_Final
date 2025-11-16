package com.deligo.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.deligo.app.R;
import com.deligo.app.adapters.AdminComplaintAdapter;
import com.deligo.app.models.Complaint;
import com.deligo.app.utils.ViewModelFactory;
import com.deligo.app.viewmodels.ComplaintViewModel;
import androidx.appcompat.widget.Toolbar;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AdminComplaintsActivity extends AppCompatActivity implements AdminComplaintAdapter.OnComplaintActionListener {
    private ComplaintViewModel complaintViewModel;
    private AdminComplaintAdapter adapter;
    private RecyclerView complaintsRecyclerView;
    private ProgressBar progressBar;
    private View emptyStateLayout;
    private Toolbar toolbar;
    private Button chipAll, chipPending, chipResolved, chipRejected;

    private List<Complaint> allComplaints = new ArrayList<>();
    private String currentFilter = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_complaints);

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupViewModel();
        setupFilterChips();
        loadComplaints();
    }

    private void initViews() {
        complaintsRecyclerView = findViewById(R.id.complaintsRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        chipAll = findViewById(R.id.chipAll);
        chipPending = findViewById(R.id.chipPending);
        chipResolved = findViewById(R.id.chipResolved);
        chipRejected = findViewById(R.id.chipRejected);
        toolbar = findViewById(R.id.toolbar);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new AdminComplaintAdapter(this);
        complaintsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        complaintsRecyclerView.setAdapter(adapter);
    }

    private void setupViewModel() {
        ViewModelFactory factory = new ViewModelFactory();
        complaintViewModel = new ViewModelProvider(this, factory).get(ComplaintViewModel.class);

        // Observe complaints
        complaintViewModel.getComplaints().observe(this, complaints -> {
            if (complaints != null) {
                allComplaints = complaints;
                applyFilter();
            }
        });

        // Observe loading state
        complaintViewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        // Observe error messages
        complaintViewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        // Observe status update
        complaintViewModel.getStatusUpdated().observe(this, statusUpdated -> {
            if (statusUpdated != null && statusUpdated) {
                Toast.makeText(this, getString(R.string.toast_complaint_updated), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupFilterChips() {
        chipAll.setOnClickListener(v -> {
            currentFilter = "all";
            applyFilter();
        });

        chipPending.setOnClickListener(v -> {
            currentFilter = "pending";
            applyFilter();
        });

        chipResolved.setOnClickListener(v -> {
            currentFilter = "resolved";
            applyFilter();
        });

        chipRejected.setOnClickListener(v -> {
            currentFilter = "rejected";
            applyFilter();
        });
    }

    private void applyFilter() {
        List<Complaint> filteredComplaints;

        if ("all".equals(currentFilter)) {
            filteredComplaints = allComplaints;
        } else {
            filteredComplaints = allComplaints.stream()
                    .filter(complaint -> currentFilter.equalsIgnoreCase(complaint.getStatus()))
                    .collect(Collectors.toList());
        }

        adapter.setComplaints(filteredComplaints);

        // Show/hide empty state
        if (filteredComplaints.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            complaintsRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            complaintsRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void loadComplaints() {
        complaintViewModel.loadAllComplaints();
    }

    @Override
    public void onResolveClicked(Complaint complaint) {
        new AlertDialog.Builder(this, R.style.Theme_DeliGo_Dialog_Alert)
                .setTitle(R.string.dialog_resolve_complaint)
                .setMessage(R.string.dialog_resolve_complaint_message)
                .setPositiveButton(R.string.resolve, (dialog, which) -> {
                    complaintViewModel.updateComplaintStatus(complaint.getComplaintId(), "resolved");
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    @Override
    public void onRejectClicked(Complaint complaint) {
        new AlertDialog.Builder(this, R.style.Theme_DeliGo_Dialog_Alert)
                .setTitle(R.string.dialog_reject_complaint)
                .setMessage(R.string.dialog_reject_complaint_message)
                .setPositiveButton(R.string.reject, (dialog, which) -> {
                    complaintViewModel.updateComplaintStatus(complaint.getComplaintId(), "rejected");
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }
}
