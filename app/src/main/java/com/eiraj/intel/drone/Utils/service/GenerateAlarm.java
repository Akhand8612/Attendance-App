package com.eiraj.intel.drone.Utils.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;

import java.util.Random;

import static android.content.Context.ALARM_SERVICE;

public class GenerateAlarm {

    public static void createAlarm(Context context, long startAt) {

        Intent intent = new Intent(context, SoundAndNotificationReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, new Random().nextInt(), intent, PendingIntent.FLAG_ONE_SHOT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        if (VERSION.SDK_INT >= VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, startAt, pi);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, startAt, pi);
        }
    }
}
