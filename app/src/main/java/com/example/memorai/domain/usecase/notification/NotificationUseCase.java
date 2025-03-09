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

    public void sendNotificationToFirebase(String title, String message) {
        String id = UUID.randomUUID().toString();
        NotificationDto notification = new NotificationDto(id, title, message, System.currentTimeMillis());
        repository.addNotification(notification);
    }

    public DatabaseReference fetchNotifications() {
        return repository.getAllNotifications();
    }
}
