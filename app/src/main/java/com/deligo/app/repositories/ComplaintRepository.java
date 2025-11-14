package com.deligo.app.repositories;

import com.deligo.app.models.Complaint;
import java.util.List;

public interface ComplaintRepository {
    void submitComplaint(String userId, String orderId, String content, ActionCallback callback);
    void getComplaintsByUser(String userId, DataCallback<List<Complaint>> callback);
    void getAllComplaints(DataCallback<List<Complaint>> callback);
    void updateComplaintStatus(String complaintId, String status, ActionCallback callback);
    
    interface DataCallback<T> {
        void onSuccess(T data);
        void onError(String message);
    }
    
    interface ActionCallback {
        void onSuccess();
        void onError(String message);
    }
}
