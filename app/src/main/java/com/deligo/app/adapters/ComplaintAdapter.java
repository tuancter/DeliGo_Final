package com.deligo.app.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.deligo.app.R;
import com.deligo.app.models.Complaint;
import android.widget.Button;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ComplaintAdapter extends RecyclerView.Adapter<ComplaintAdapter.ComplaintViewHolder> {
    private List<Complaint> complaints = new ArrayList<>();

    public void setComplaints(List<Complaint> complaints) {
        this.complaints = complaints;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ComplaintViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_complaint, parent, false);
        return new ComplaintViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ComplaintViewHolder holder, int position) {
        Complaint complaint = complaints.get(position);
        holder.bind(complaint);
    }

    @Override
    public int getItemCount() {
        return complaints.size();
    }

    static class ComplaintViewHolder extends RecyclerView.ViewHolder {
        private TextView orderIdTextView;
        private TextView dateTextView;
        private TextView contentTextView;
        private Button statusChip;

        public ComplaintViewHolder(@NonNull View itemView) {
            super(itemView);
            orderIdTextView = itemView.findViewById(R.id.orderIdTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            contentTextView = itemView.findViewById(R.id.contentTextView);
            statusChip = itemView.findViewById(R.id.statusChip);
        }

        public void bind(Complaint complaint) {
            // Order ID
            orderIdTextView.setText("Order #" + complaint.getOrderId().substring(0, Math.min(8, complaint.getOrderId().length())).toUpperCase());

            // Date
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            String dateStr = dateFormat.format(new Date(complaint.getCreatedAt()));
            dateTextView.setText(dateStr);

            // Content
            contentTextView.setText(complaint.getContent());

            // Status
            String status = capitalizeFirst(complaint.getStatus());
            statusChip.setText(status);
            statusChip.setBackgroundColor(getStatusColor(complaint.getStatus()));
        }

        private String capitalizeFirst(String text) {
            if (text == null || text.isEmpty()) {
                return text;
            }
            return text.substring(0, 1).toUpperCase() + text.substring(1);
        }

        private int getStatusColor(String status) {
            switch (status.toLowerCase()) {
                case "pending":
                    return Color.parseColor("#FFA726");
                case "resolved":
                    return Color.parseColor("#66BB6A");
                case "rejected":
                    return Color.parseColor("#EF5350");
                default:
                    return Color.parseColor("#9E9E9E");
            }
        }
    }
}
