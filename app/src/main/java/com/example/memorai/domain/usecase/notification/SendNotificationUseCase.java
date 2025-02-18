// domain/usecase/notification/SendNotificationUseCase.java
package com.example.memorai.domain.usecase.notification;

import javax.inject.Inject;

public class SendNotificationUseCase {
    @Inject
    public SendNotificationUseCase() {
    }

    // Trigger a notification (implementation depends on NotificationManager/WorkManager)
    public void execute(String title, String message) {
        // Notification logic goes here
    }
}
