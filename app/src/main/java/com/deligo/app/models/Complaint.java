package com.deligo.app.models;

public class Complaint {
    private String complaintId;
    private String userId;
    private String orderId;
    private String content;
    private String status;
    private long createdAt;

    // Required empty constructor for Firestore
    public Complaint() {
    }

    public Complaint(String complaintId, String userId, String orderId, String content, String status, long createdAt) {
        this.complaintId = complaintId;
        this.userId = userId;
        this.orderId = orderId;
        this.content = content;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public String getComplaintId() {
        return complaintId;
    }

    public void setComplaintId(String complaintId) {
        this.complaintId = complaintId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
