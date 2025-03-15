package com.example.memorai.domain.usecase.notification;

import com.example.memorai.data.remote.dto.NotificationDto;
import com.example.memorai.data.repository.NotificationRepository;
import com.google.firebase.database.DatabaseReference;

import java.util.UUID;

public class NotificationUseCase {
    private final NotificationRepository repository;

    public NotificationUseCase() {
        repository = new NotificationRepository();
    }

    public void sendNotificationToFirebase(NotificationDto notification) {
        DatabaseReference databaseRef = null;
        String notificationId = databaseRef.push().getKey();
        notification.setId(notificationId);
        notification.setTimestamp(System.currentTimeMillis());

        if (notificationId != null) {
            databaseRef.child(notificationId).setValue(notification);
        }
    }


    public DatabaseReference fetchNotifications() {
        return repository.getAllNotifications();
    }
}
