package com.example.memorai.presentation.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.memorai.data.remote.dto.NotificationDto;
import com.example.memorai.domain.usecase.notification.NotificationUseCase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class NotificationViewModel extends ViewModel {
    private final MutableLiveData<List<NotificationDto>> notifications = new MutableLiveData<>();
    private final NotificationUseCase useCase = new NotificationUseCase();

    public void loadNotifications() {
        useCase.fetchNotifications().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<NotificationDto> list = new ArrayList<>();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    NotificationDto notification = snap.getValue(NotificationDto.class);
                    list.add(notification);
                }
                notifications.postValue(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public LiveData<List<NotificationDto>> getNotifications() {
        return notifications;
    }

    public void sendNotification(String title, String message) {
        useCase.sendNotificationToFirebase(title, message);
    }
}
