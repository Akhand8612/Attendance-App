package com.eiraj.intel.drone.Utils.service;

import static com.eiraj.intel.drone.rest.ApiClient.BASE_URL;
import static com.eiraj.intel.drone.rest.ApiClient.BASE_URL_FCM;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.eiraj.intel.drone.R;
import com.eiraj.intel.drone.Utils.SharedPrefHelper;
import com.eiraj.intel.drone.Utils.Util;
import com.eiraj.intel.drone.model.SendNotificationModel;
import com.eiraj.intel.drone.model.UpdateEmployeeStatusPayload;
import com.eiraj.intel.drone.rest.ApiInterface;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LiveTrackService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    // region Variables
    private static final String TAG = "XXX";
    private GoogleApiClient mLocationClient;
    private LocationRequest mLocationRequest = new LocationRequest();
    private final String NOTIFICATION_CHANNEL_ID = "com.eiraj.intel.drone" + ".attendancePunch";
    SharedPrefHelper sharedPrefHelper;
    ApiInterface apiService;
    private String channelName = "Attendance Notification";
    private Double latitude = 0.0, longitude = 0.0;
    // endregion Variables

    @Override
    public void onCreate() {
        super.onCreate();

        startMyOwnForeground(null);

        sharedPrefHelper = new SharedPrefHelper();
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.level(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .connectTimeout(5, TimeUnit.MINUTES)
                .readTimeout(5, TimeUnit.MINUTES)
                .build();

        apiService = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiInterface.class);

        mLocationClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationRequest.setInterval(1000 * 10); // 5 secs
        mLocationRequest.setFastestInterval(1000 * 5); // 1 secs

        int priority = LocationRequest.PRIORITY_HIGH_ACCURACY;

        mLocationRequest.setPriority(priority);
        if (!mLocationClient.isConnected()) {
            mLocationClient.connect();
        }

        startUpdatingEmployeeStatus();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(2);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnected(Bundle dataBundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (mLocationClient.isConnected()) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mLocationClient, mLocationRequest, this);
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            if (location.getLatitude() != 0.0) {
                Log.e(TAG, "onLocationChanged: " + location.getLatitude() + "--" + location.getLongitude());

                latitude = location.getLatitude();
                longitude = location.getLongitude();
                checkIfOutOfRangeAndTriggerAlert(location.getLatitude(), location.getLongitude());
            }
        }
    }

    private void startMyOwnForeground(Double distance) {

        NotificationChannel chan = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        }
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(chan);
        }

        String message = "";

        /** for stopping notification of live location*/

       if (distance != null) {
       //     message = "You are " + Util.twoDigitsAfterDecimalDoubleformat.format(distance) + " meters away from your location";
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder
               // .setOngoing(true)
              //  .setSmallIcon(R.drawable.geoimage)
              //  .setAutoCancel(true)
                //.setContentTitle("You have started your day")
               // .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
               // .setPriority(NotificationManager.IMPORTANCE_MIN)
               // .setCategory(Notification.CATEGORY_SERVICE)
               .build();
        startForeground(2, notification);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    Handler repeatedTaskHandler;
    long interval = 30 * 1000;

    private void startUpdatingEmployeeStatus() {

        sharedPrefHelper.setLocationServicesEnabled(Util.checkIfGpsTurnedOn(getApplicationContext()));

        repeatedTaskHandler = new Handler(Looper.myLooper());
        repeatedTaskHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                UpdateEmployeeStatusPayload payload = new UpdateEmployeeStatusPayload();
                payload.setBattery(Util.getBatteryPercentage(getApplicationContext()));
                payload.setLatitude(latitude);
                payload.setLongitude(longitude);
                payload.setEmpCode(sharedPrefHelper.getEmployeeCode());
                payload.setUnitcode(sharedPrefHelper.getUnitCode());

                triggerAPI_updateEmployeeBatteryStatus(payload);

                if (sharedPrefHelper.isLocationServicesEnabled() != Util.checkIfGpsTurnedOn(getApplicationContext())) {
                    payload.setLocationPermissionAvailable(Util.checkIfGpsTurnedOn(getApplicationContext()));
                    payload.setBattery(null);

                    triggerAPI_updateEmployeeLocationStatus(payload);
                    sharedPrefHelper.setLocationServicesEnabled(Util.checkIfGpsTurnedOn(getApplicationContext()));
                }

                repeatedTaskHandler.postDelayed(this, interval);
            }
        }, interval);
    }

    private void triggerAPI_updateEmployeeBatteryStatus(UpdateEmployeeStatusPayload payload) {
        Call<ResponseBody> updateEmployeeStatus = apiService.updateEmployeeBatteryStatus(payload);
        updateEmployeeStatus.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    private void triggerAPI_updateEmployeeLocationStatus(UpdateEmployeeStatusPayload payload) {
        Call<ResponseBody> updateEmployeeStatus = apiService.updateEmployeeLocationStatus(payload);
        updateEmployeeStatus.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    private void checkIfOutOfRangeAndTriggerAlert(Double latitude, Double longitude) {

        // last location is basically punch in location
        String previousLatitude = sharedPrefHelper.getLiveTrackLastLatitude();
        String previousLongitude = sharedPrefHelper.getLiveTrackLastLongitude();


        if (previousLongitude.equals("0.0") && previousLatitude.equals("0.0")) {
            // no data lets save
            sharedPrefHelper.setLiveTrackLastLatitude(String.valueOf(latitude));
            sharedPrefHelper.setLiveTrackLastLongitude(String.valueOf(longitude));

        } else {

            if (sharedPrefHelper.isDutyActive()) {

                double lastLatitude = Double.parseDouble(previousLatitude);
                double lastLongitude = Double.parseDouble(previousLongitude);

                double meters = 0.0;
                double dis = Util.distance(lastLatitude, lastLongitude, latitude, longitude); // currently in miles
                meters = dis / 0.62137; // currently in kms
                meters = meters * 1000; // now meters

                double minDistance_m = 50; // in meters
                if (sharedPrefHelper.getLiveTrackRadius() > 0) {
                    minDistance_m = sharedPrefHelper.getLiveTrackRadius();
                }

                startMyOwnForeground(meters); // notify ui currently this much meters away

                Log.e(TAG, "checkIfOutOfRangeAndTriggerAlert: meters : " + meters);
                Log.e(TAG, "checkIfOutOfRangeAndTriggerAlert: allowed distance : " + minDistance_m);
                Log.e(TAG, "checkIfOutOfRangeAndTriggerAlert: allowed distance : " + (meters > minDistance_m));

                if (meters > minDistance_m) {
                    // user went outside

                    if (sharedPrefHelper.isSupervisorNotifiedAboutOutOfRange() == false) {

                        sharedPrefHelper.setSupervisorNotifiedAboutOutOfRange(true);
                        sharedPrefHelper.setSupervisorNotifiedAboutInsideRange(false);

                        SendNotificationModel sendNotificationModel = new SendNotificationModel();

                        sendNotificationModel.setNotificationType("guard_out_of_site");
                        sendNotificationModel.setCompId(Integer.parseInt(sharedPrefHelper.getCompanyID()));
                        sendNotificationModel.setEmpCode(sharedPrefHelper.getEmployeeCode());
                        sendNotificationModel.setMessage(String.format("%s is %s meters out of site", sharedPrefHelper.getEmployeeName(), String.valueOf(meters)));
                        sendNotificationModel.setTitle(String.format("%s is out of site!", sharedPrefHelper.getEmployeeName()));
                        sendNotificationModel.setUnitCode(sharedPrefHelper.getUnitCode());
                        sendNotificationModel.setUserInRange(false);

                        alertSupervisor(sendNotificationModel);
                    }
                } else {
                    // user is back inside

                    if (sharedPrefHelper.isSupervisorNotifiedAboutInsideRange() == false) {

                        sharedPrefHelper.setSupervisorNotifiedAboutOutOfRange(false);
                        sharedPrefHelper.setSupervisorNotifiedAboutInsideRange(true);

                        SendNotificationModel sendNotificationModel = new SendNotificationModel();

                        sendNotificationModel.setNotificationType("guard_inside_site");
                        sendNotificationModel.setCompId(Integer.parseInt(sharedPrefHelper.getCompanyID()));
                        sendNotificationModel.setEmpCode(sharedPrefHelper.getEmployeeCode());
                        sendNotificationModel.setMessage(String.format("%s has returned to site", sharedPrefHelper.getEmployeeName()));
                        sendNotificationModel.setTitle(String.format("%s is back in site!", sharedPrefHelper.getEmployeeName()));
                        sendNotificationModel.setUnitCode(sharedPrefHelper.getUnitCode());
                        sendNotificationModel.setUserInRange(true);

                        alertSupervisor(sendNotificationModel);
                    }
                }
            }
        }
    }

    private void alertSupervisor(SendNotificationModel sendNotificationModel) {
        Call<ResponseBody> alertSupervisor = apiService.alertSupervisor(sendNotificationModel);
        alertSupervisor.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }
}