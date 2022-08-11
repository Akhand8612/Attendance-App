package com.eiraj.intel.drone.Utils.service;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;

public class HandleService {

    // schedule auto punch out service
    public static void scheduleJob(Context context, long startAfter) {
        ComponentName serviceComponent = new ComponentName(context, AutoPunchOutService.class);
        JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);
        builder.setMinimumLatency(1 * 1000); // wait at least
        builder.setOverrideDeadline(startAfter); // maximum delay
        //builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED); // require unmetered network
        //builder.setRequiresDeviceIdle(true); // device should be idle
        //builder.setRequiresCharging(false); // we don't care if the device is charging or not
        JobScheduler jobScheduler = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            jobScheduler = context.getSystemService(JobScheduler.class);
        }
        jobScheduler.schedule(builder.build());
    }

    public static void startLocationTrack(Context context){
        //context.startService(new Intent(context, LocationService.class));
    }

    public static void stopLocationTrack(Context context){
        //context.stopService(new Intent(context, LocationService.class));
    }
}
