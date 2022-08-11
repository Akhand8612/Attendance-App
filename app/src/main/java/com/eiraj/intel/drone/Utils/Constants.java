package com.eiraj.intel.drone.Utils;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class Constants {

    public static String numberUpSample = "";
    public static String model_recog = "";
    public static String model_reg = "";
    public static String faceDetect = "";
    public static String tolerance = "";
    public static String numJitters = "";

    public static final String packageName = "com.eiraj.intel.drone";
    public static final int LOCATION_INTERVAL = 5000;
    public static final int FASTEST_LOCATION_INTERVAL = 1000;

    public static int PUNCH_RANGE_METERS = 50;
    public static int PUNCH_TIME_RANGE_CONSIDER_OT_HOURS = 6;
    public static OkHttpClient okHttpClient_3minTimeout = new OkHttpClient.Builder()
            .readTimeout(180, TimeUnit.SECONDS)
            .connectTimeout(180, TimeUnit.SECONDS)
            .build();
}
