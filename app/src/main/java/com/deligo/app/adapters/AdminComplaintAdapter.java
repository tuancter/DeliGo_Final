package com.deligo.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.deligo.app.R;
import com.deligo.app.models.Complaint;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminComplaintAdapter extends RecyclerView.Adapter<AdminComplaintAdapter.AdminComplaintViewHolder> {
    private List<Complaint> complaints = new ArrayList<>();
    private OnComplaintActionListener listener;

    public interface OnComplaintActionListener {
        void onResolveClicked(Complaint complaint);
        void onRejectClicked(Complaint complaint);
    }

    public AdminComplaintAdapter(OnComplaintActionListener listener) {
        this.listener = listener;
    }

    public void setComplaints(List<Complaint> complaints) {
        this.complaints = complaints;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AdminComplaintViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_complaint, parent, false);
        return new AdminComplaintViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminComplaintViewHolder holder, int position) {
        Complaint complaint = complaints.get(position);
        holder.bind(complaint, listener);
    }

    @Override
    public int getItemCount() {
        return complaints.size();
    }

    static class AdminComplaintViewHolder extends RecyclerView.ViewHolder {
        private TextView userIdTextView;
        private TextView orderIdTextView;
        private TextView dateTextView;
        private TextView contentTextView;
        private Chip statusChip;
        private MaterialButton resolveButton;
        private MaterialButton rejectButton;
        private View actionButtonsLayout;

        public AdminComplaintViewHolder(@NonNull View itemView) {
            super(itemView);
            userIdTextView = itemView.findViewById(R.id.userIdTextView);
            orderIdTextView = itemView.findViewById(R.id.orderIdTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            contentTextView = itemView.findViewById(R.id.contentTextView);
            statusChip = itemView.findViewById(R.id.statusChip);
            resolveButton = itemView.findViewById(R.id.resolveButton);
            rejectButton = itemView.findViewById(R.id.rejectButton);
            actionButtonsLayout = itemView.findViewById(R.id.actionButtonsLayout);
        }

        public void bind(Complaint complaint, OnComplaintActionListener listener) {
            // User ID (shortened)
            String userId = complaint.getUserId();
            if (userId != null && userId.length() > 8) {
                userId = userId.substring(0, 8).toUpperCase();
            }
            userIdTextView.setText(userId);

            // Order ID
            String orderId = complaint.getOrderId();
            if (orderId != null && orderId.length() > 8) {
                orderId = orderId.substring(0, 8).toUpperCase();
            }
            orderIdTextView.setText("Order #" + orderId);

            // Date
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            String dateStr = dateFormat.format(new Date(complaint.getCreatedAt()));
            dateTextView.setText(dateStr);

            // Content
            contentTextView.setText(complaint.getContent());

            // Status
            String status = capitalizeFirst(complaint.getStatus());
            statusChip.setText(status);
            statusChip.setChipBackgroundColorResource(getStatusColorResource(complaint.getStatus()));

            // Show/hide action buttons based on status
            if ("pending".equalsIgnoreCase(complaint.getStatus())) {
                actionButtonsLayout.setVisibility(View.VISIBLE);
                
                resolveButton.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onResolveClicked(complaint);
                    }
                });

                rejectButton.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onRejectClicked(complaint);
                    }
                });
            } else {
                actionButtonsLayout.setVisibility(View.GONE);
            }
        }

        private String capitalizeFirst(String text) {
            if (text == null || text.isEmpty()) {
                return text;
            }
            return text.substring(0, 1).toUpperCase() + text.substring(1);
        }

        private int getStatusColorResource(String status) {
            switch (status.toLowerCase()) {
                case "pending":
                    return android.R.color.holo_orange_light;
                case "resolved":
                    return android.R.color.holo_green_light;
                case "rejected":
                    return android.R.color.holo_red_light;
                default:
                    return android.R.color.darker_gray;
            }
        }
    }
}
