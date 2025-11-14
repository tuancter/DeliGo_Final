package com.deligo.app.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.deligo.app.models.Complaint;
import com.deligo.app.repositories.ComplaintRepository;

import java.util.List;

public class ComplaintViewModel extends ViewModel {
    private final MutableLiveData<List<Complaint>> complaints = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<Boolean> complaintSubmitted = new MutableLiveData<>();
    private final MutableLiveData<Boolean> statusUpdated = new MutableLiveData<>();

    private final ComplaintRepository complaintRepository;

    public ComplaintViewModel(ComplaintRepository complaintRepository) {
        this.complaintRepository = complaintRepository;
        isLoading.setValue(false);
        complaintSubmitted.setValue(false);
        statusUpdated.setValue(false);
    }

    public LiveData<List<Complaint>> getComplaints() {
        return complaints;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<Boolean> getComplaintSubmitted() {
        return complaintSubmitted;
    }

    public LiveData<Boolean> getStatusUpdated() {
        return statusUpdated;
    }

    public void submitComplaint(String userId, String orderId, String content) {
        isLoading.setValue(true);
        complaintSubmitted.setValue(false);
        complaintRepository.submitComplaint(userId, orderId, content, new ComplaintRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                isLoading.setValue(false);
                complaintSubmitted.setValue(true);
            }

            @Override
            public void onError(String message) {
                isLoading.setValue(false);
                errorMessage.setValue(message);
            }
        });
    }

    public void loadUserComplaints(String userId) {
        isLoading.setValue(true);
        complaintRepository.getComplaintsByUser(userId, new ComplaintRepository.DataCallback<List<Complaint>>() {
            @Override
            public void onSuccess(List<Complaint> data) {
                isLoading.setValue(false);
                complaints.setValue(data);
            }

            @Override
            public void onError(String message) {
                isLoading.setValue(false);
                errorMessage.setValue(message);
            }
        });
    }

    public void loadAllComplaints() {
        isLoading.setValue(true);
        complaintRepository.getAllComplaints(new ComplaintRepository.DataCallback<List<Complaint>>() {
            @Override
            public void onSuccess(List<Complaint> data) {
                isLoading.setValue(false);
                complaints.setValue(data);
            }

            @Override
            public void onError(String message) {
                isLoading.setValue(false);
                errorMessage.setValue(message);
            }
        });
    }

    public void updateComplaintStatus(String complaintId, String status) {
        isLoading.setValue(true);
        statusUpdated.setValue(false);
        complaintRepository.updateComplaintStatus(complaintId, status, new ComplaintRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                isLoading.setValue(false);
                statusUpdated.setValue(true);
                // Reload complaints after status update
                loadAllComplaints();
            }

            @Override
            public void onError(String message) {
                isLoading.setValue(false);
                errorMessage.setValue(message);
            }
        });
    }
}
