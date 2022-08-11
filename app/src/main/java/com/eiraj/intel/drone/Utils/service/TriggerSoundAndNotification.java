package com.eiraj.intel.drone.Utils.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.eiraj.intel.drone.R;
import com.eiraj.intel.drone.activities.SplashActivity;
import com.eiraj.intel.drone.helper.SampleApp;

public class TriggerSoundAndNotification extends Service {

    String title = "Punch Out Reminder";
    String content = "Dear user, you have passed your punch out time by 15 minutes. Kindly punch out now otherwise your app will be locked.";
    String CHANNEL_ID = "com.eiraj.intel.drone.notifyPunchOut";
    String name = "Punch Out Reminder";
    String description = "Remind user to punch out";
    Intent intent;
    PendingIntent pendingIntent;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("SET_ALARM", "onStartCommand: ");
        // Notification are silent they are to be triggered even if user has turned of notification
        sendNotification();

        SampleApp.getPlaySound().startSound();
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.e("ALARM", "run: STOPPED");
                SampleApp.getPlaySound().stopSound();
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        NotificationManager notificationManager = getSystemService(NotificationManager.class);
                        notificationManager.cancelAll();
                    } else {
                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.cancelAll();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 60000);

        return START_STICKY;
    }

    private void sendNotification() {

        intent = new Intent(this, SplashActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        pendingIntent = PendingIntent.getActivity(this, 1, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        Notification notification = builder
                .setOngoing(true)
                .setTimeoutAfter(60000)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(title)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setContentText(content)
                .setColorized(true)
                .setVibrate(new long[]{0, 500, 100, 500, 100, 500})
                .setSound(Settings.System.DEFAULT_RINGTONE_URI)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .build();

        createNotificationChannel(notification);
    }

    private void createNotificationChannel(Notification notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(description);
            channel.setShowBadge(true);


            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            notificationManager.notify(1, notification);
        } else {
            // THIS IS THE WAY TO USE SYSTEM SERVICE BEFORE API 22
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(1, notification);
        }

        startForeground(1, notification);

    }
}
