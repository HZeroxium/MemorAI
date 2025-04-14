package com.example.memorai.domain.repository;
import androidx.lifecycle.LiveData;

import com.example.memorai.domain.model.Notification;

import java.util.List;

public interface NotificationRepository {
    LiveData<List<Notification>> getNotifications(String userId);
    void sendNotification(String userId, Notification notification);
    void deleteNotification(String userId, String notificationId);
}