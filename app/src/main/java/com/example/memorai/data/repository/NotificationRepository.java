package com.example.memorai.data.repository;

import com.example.memorai.data.remote.dto.NotificationDto;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class NotificationRepository {
    private final DatabaseReference databaseRef;

    public NotificationRepository() {
        databaseRef = FirebaseDatabase.getInstance().getReference("notifications");
    }

    public void addNotification(NotificationDto notification) {
        databaseRef.child(notification.getId()).setValue(notification);
    }

    public DatabaseReference getAllNotifications() {
        return databaseRef;
    }
}