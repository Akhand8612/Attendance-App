package com.eiraj.intel.drone.Utils.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.eiraj.intel.drone.R;
import com.eiraj.intel.drone.Utils.Constants;
import com.eiraj.intel.drone.activities.NotificationDashboard;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.util.Random;

public class FirebaseNotification extends FirebaseMessagingService {

    String name = "General Notifications";
    String description = "General notifications for user";
    String CHANNEL_ID = Constants.packageName + ".general";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {

        Log.e("XXX", "onMessageReceived: " + new Gson().toJson(remoteMessage));

        if (remoteMessage.getNotification() != null) {
            showConsoleNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
        } else if (remoteMessage.getData() != null) {
            try {
                String title = remoteMessage.getData().get("title");
                String message = remoteMessage.getData().get("message");
                showConsoleNotification(title, message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.onMessageReceived(remoteMessage);
    }


    private void showConsoleNotification(String title, String message) {

        if (title.equals("start_location_update_on_request")) {
            startLocationUpdates();
        } else if (title.equals("stop_location_update_on_request")) {
            stopLocationUpdates();
        } else {
            Intent intent = new Intent(this, NotificationDashboard.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(title)
                    // THIS STYLE CAN BE ADDED IF THE CONTENT TEXT IS LARGER THAN 1 LINE
                    // AND USING THIS THE NOTIFICATION CAN BE EXPANDED
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setColorized(true)
                    .setVibrate(new long[]{0, 500, 100, 500, 100, 500})
                    .setSound(Settings.System.DEFAULT_RINGTONE_URI)
                    .setContentIntent(pendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_HIGH);

            createNotificationChannel(builder);
        }
    }

    private void stopLocationUpdates() {
        stopService(new Intent(this, TrackLocationOnRequestService.class));
    }

    private void startLocationUpdates() {
        Intent intent = new Intent(this, TrackLocationOnRequestService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    private void createNotificationChannel(NotificationCompat.Builder builder) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(description);
            channel.setShowBadge(true);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            notificationManager.notify(new Random().nextInt(), builder.build());
        } else {
            // THIS IS THE WAY TO USE SYSTEM SERVICE BEFORE API 22
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(new Random().nextInt(), builder.build());
        }

    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }
}
