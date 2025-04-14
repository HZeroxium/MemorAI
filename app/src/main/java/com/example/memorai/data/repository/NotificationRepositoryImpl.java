package com.example.memorai.data.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.memorai.domain.model.Notification;
import com.example.memorai.domain.repository.NotificationRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

public class NotificationRepositoryImpl implements NotificationRepository {
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    @Inject
    public NotificationRepositoryImpl(FirebaseFirestore db, FirebaseAuth auth) {
        this.db = db;
        this.auth = auth;
    }

    private CollectionReference getNotificationsCollection(String userId) {
        return db.collection("photos")
                .document(userId)
                .collection("user_notifications");
    }
    @Override
    public LiveData<List<Notification>> getNotifications(String userId) {
        MutableLiveData<List<Notification>> notificationsLiveData = new MutableLiveData<>();
        getNotificationsCollection(userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        notificationsLiveData.setValue(new ArrayList<>());
                        return;
                    }
                    if (snapshot != null) {
                        List<Notification> notifications = new ArrayList<>();
                        for (var doc : snapshot.getDocuments()) {
                            Notification notification = doc.toObject(Notification.class);
                            if (notification != null) {
                                notifications.add(notification);
                            }
                        }
                        notificationsLiveData.setValue(notifications);
                    }
                });
        return notificationsLiveData;
    }

    @Override
    public void sendNotification(String userId, Notification notification) {
        if (userId == null || notification == null) {
            return;
        }
        if (db == null) {
            return;
        }

        db.collection("photos")
                .document(userId)
                .collection("user_notifications")
                .document(notification.getId())
                .set(notification)
                .addOnSuccessListener(documentReference -> {
                })
                .addOnFailureListener(e -> {
                });
    }

    @Override
    public void deleteNotification(String userId, String notificationId) {
        getNotificationsCollection(userId)
                .document(notificationId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                })
                .addOnFailureListener(e -> {
                });
    }
}