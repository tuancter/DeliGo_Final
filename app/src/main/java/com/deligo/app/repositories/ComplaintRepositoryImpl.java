package com.deligo.app.repositories;

import com.deligo.app.models.Complaint;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComplaintRepositoryImpl implements ComplaintRepository {
    private final FirebaseFirestore firestore;

    public ComplaintRepositoryImpl() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    @Override
    public void submitComplaint(String userId, String orderId, String content, ActionCallback callback) {
        Map<String, Object> complaintData = new HashMap<>();
        complaintData.put("userId", userId);
        complaintData.put("orderId", orderId);
        complaintData.put("content", content);
        complaintData.put("status", "pending");
        complaintData.put("createdAt", System.currentTimeMillis());

        firestore.collection("complaints")
                .add(complaintData)
                .addOnSuccessListener(documentReference -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void getComplaintsByUser(String userId, DataCallback<List<Complaint>> callback) {
        firestore.collection("complaints")
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Complaint> complaints = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Complaint complaint = document.toObject(Complaint.class);
                        complaint.setComplaintId(document.getId());
                        complaints.add(complaint);
                    }
                    callback.onSuccess(complaints);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void getAllComplaints(DataCallback<List<Complaint>> callback) {
        firestore.collection("complaints")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Complaint> complaints = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Complaint complaint = document.toObject(Complaint.class);
                        complaint.setComplaintId(document.getId());
                        complaints.add(complaint);
                    }
                    callback.onSuccess(complaints);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    @Override
    public void updateComplaintStatus(String complaintId, String status, ActionCallback callback) {
        firestore.collection("complaints")
                .document(complaintId)
                .update("status", status)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}
