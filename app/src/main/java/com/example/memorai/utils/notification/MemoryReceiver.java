package com.example.memorai.utils.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import androidx.core.app.NotificationCompat;
import com.example.memorai.R;
import com.example.memorai.presentation.ui.activity.MainActivity;
import android.app.PendingIntent;
import android.os.Build;

public class MemoryReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        sendNotification(context);
    }

    private void sendNotification(Context context) {
        String channelId = "memory_channel";
        String channelName = "Memory Alerts";

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Kiểm tra và tạo Notification Channel cho Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel existingChannel = notificationManager.getNotificationChannel(channelId);
            if (existingChannel == null) { // Chỉ tạo nếu channel chưa tồn tại
                NotificationChannel channel = new NotificationChannel(
                        channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
                channel.setDescription("Thông báo về bộ nhớ thấp");
                notificationManager.createNotificationChannel(channel);
            }
        }

        // Intent mở app khi nhấn vào thông báo
        Intent openAppIntent = new Intent(context, MainActivity.class);
        openAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Tạo thông báo
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Cảnh báo bộ nhớ")
                .setContentText("Bộ nhớ thấp, hãy dọn dẹp ngay!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify(1001, builder.build());
    }
}
