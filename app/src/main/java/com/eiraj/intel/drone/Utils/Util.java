package com.eiraj.intel.drone.Utils;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.util.Log;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class Util {

    public static final String MY_PREFS_NAME = "MyPrefsFile";
    static Context context;
    private static Util instance = new Util();
    ConnectivityManager connectivityManager;
    NetworkInfo wifiInfo, mobileInfo;
    public static DecimalFormat twoDigitsAfterDecimalDoubleformat = new DecimalFormat("#0.00");
    boolean connected = false;

    public static Util getInstance(Context ctx) {
        context = ctx.getApplicationContext();
        return instance;
    }

    public static boolean internetConnectionAvailable(int timeOut) {
        InetAddress inetAddress = null;
        try {
            Future<InetAddress> future = Executors.newSingleThreadExecutor().submit(new Callable<InetAddress>() {
                @Override
                public InetAddress call() {
                    try {
                        return InetAddress.getByName("google.com");
                    } catch (UnknownHostException e) {
                        return null;
                    }
                }
            });
            inetAddress = future.get(timeOut, TimeUnit.MILLISECONDS);
            future.cancel(true);
        } catch (InterruptedException e) {
        } catch (ExecutionException e) {
        } catch (TimeoutException e) {
        }
        return inetAddress != null && !inetAddress.equals("");
    }

    public static RequestBody getRequestBody_textPlain(String data) {
        return RequestBody.create(MediaType.parse("text/plain"), data);
    }

    public static int getBatteryPercentage(Context context) {

        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, iFilter);

        int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
        int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;

        float batteryPct = level / (float) scale;

        return (int) (batteryPct * 100);
    }

    public static String getCurrentVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            return "NA";
        }
    }

    public static boolean isOnline(Context context) {
        boolean connected = false;
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            connected = networkInfo != null && networkInfo.isAvailable() &&
                    networkInfo.isConnected();
            return connected;


        } catch (Exception e) {
            Log.v("connectivity", e.toString());
        }
        return connected;
    }

    public static double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    public static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    public static double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    public static String getDateTime(int type) {
        String result = "";
        switch (type) {
            case 1:
                result = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                break;
            case 2:
                result = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                break;
            case 3:
                result = new SimpleDateFormat("HH:mm:ss").format(new Date());
                break;
            case 4:
                result = new SimpleDateFormat("h:mm:ssa").format(new Date());
                break;
            case 5:
                result = new SimpleDateFormat("h:mm:ss a").format(new Date());
                break;
            case 6:
                result = new SimpleDateFormat("h:mm a").format(new Date());
                break;
            case 7:
                result = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
                break;
            case 8:
                result = new SimpleDateFormat("dd-MMM-yyyy").format(new Date());
                break;
            case 9:
                result = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(new Date());
                break;
        }

        return result;
    }

    public static boolean checkIfGpsTurnedOn(Context context) {

        LocationManager location_manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        //  LocationListener listner = new MyLocationListener();


        boolean networkLocationEnabled = location_manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        boolean gpsLocationEnabled = location_manager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        return gpsLocationEnabled;
    }
}
