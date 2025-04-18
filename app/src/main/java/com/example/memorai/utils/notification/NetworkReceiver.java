package com.example.memorai.utils.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.example.memorai.R;
import com.example.memorai.presentation.ui.activity.MainActivity;

public class NetworkReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        if (activeNetwork == null || !activeNetwork.isConnected()) {
            NotificationHelper.sendSystemNotification(
                    context,
                    context.getString(R.string.network_channel_id),
                    context.getString(R.string.network_alert_title),
                    context.getString(R.string.network_alert_message),
                    new Intent(context, MainActivity.class)
            );
        }
    }
}
