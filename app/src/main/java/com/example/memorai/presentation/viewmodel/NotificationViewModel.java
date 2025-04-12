package com.example.memorai.presentation.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.memorai.data.remote.dto.NotificationDto;

import java.util.ArrayList;
import java.util.List;

public class NotificationViewModel extends ViewModel {
    private final MutableLiveData<List<NotificationDto>> notifications = new MutableLiveData<>();

    public NotificationViewModel() { }

    public LiveData<List<NotificationDto>> getNotifications() {
        return notifications;
    }
}
