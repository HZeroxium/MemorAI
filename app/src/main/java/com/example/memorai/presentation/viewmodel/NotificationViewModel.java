package com.example.memorai.presentation.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.memorai.domain.model.Notification;
import com.example.memorai.domain.repository.NotificationRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class NotificationViewModel extends ViewModel {
    private final NotificationRepository repository;
    private final LiveData<List<Notification>> notificationsLiveData;

    @Inject
    public NotificationViewModel(NotificationRepository repository) {
        this.repository = repository;
        this.notificationsLiveData = repository.getNotifications(getCurrentUserId());
    }

    public LiveData<List<Notification>> getNotifications() {
        return notificationsLiveData;
    }

    public void sendNotification(Notification notification) {
        repository.sendNotification(getCurrentUserId(), notification);
    }

    public void deleteNotification(String notificationId) {
        repository.deleteNotification(getCurrentUserId(), notificationId);
    }

    private String getCurrentUserId() {
        return Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    }
}