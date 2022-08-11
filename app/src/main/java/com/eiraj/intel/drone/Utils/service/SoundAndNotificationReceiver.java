package com.eiraj.intel.drone.Utils.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.content.ContextCompat;

public class SoundAndNotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        // Start Service
        try {
            ContextCompat.startForegroundService(context, new Intent(context, TriggerSoundAndNotification.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
