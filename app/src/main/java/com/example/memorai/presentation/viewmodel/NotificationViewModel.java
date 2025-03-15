package com.example.memorai.presentation.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.memorai.data.remote.dto.NotificationDto;

import java.util.ArrayList;
import java.util.List;

public class NotificationViewModel extends ViewModel {
    private final MutableLiveData<List<NotificationDto>> notifications = new MutableLiveData<>();

    public NotificationViewModel() {
        loadSampleNotifications(); // Load sẵn 4 notification
    }

    private void loadSampleNotifications() {
        List<NotificationDto> sampleNotifications = new ArrayList<>();

        sampleNotifications.add(new NotificationDto("1", "System Alert", "Your storage is running low! Free up some space.", System.currentTimeMillis()));
        sampleNotifications.add(new NotificationDto("2", "Upload Successful", "Your latest photo has been uploaded to the cloud.", System.currentTimeMillis()));
        sampleNotifications.add(new NotificationDto("3", "Weak Network Connection", "Your internet connection is unstable. Please check your WiFi or mobile data.", System.currentTimeMillis()));
        sampleNotifications.add(new NotificationDto("4", "Cloud Backup Reminder", "You have unsynced photos. Upload them now to keep your data safe.", System.currentTimeMillis()));

        notifications.setValue(sampleNotifications);
    }

    public LiveData<List<NotificationDto>> getNotifications() {
        return notifications;
    }
}
