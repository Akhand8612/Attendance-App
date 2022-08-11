package com.eiraj.intel.drone.Utils.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.eiraj.intel.drone.R;
import com.eiraj.intel.drone.Utils.Constants;
import com.eiraj.intel.drone.Utils.SharedPrefHelper;
import com.eiraj.intel.drone.Utils.Util;
import com.eiraj.intel.drone.model.UpdateEmpLocationModel;
import com.eiraj.intel.drone.rest.ApiClient;
import com.eiraj.intel.drone.rest.ApiInterface;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.eiraj.intel.drone.rest.ApiClient.BASE_URL_FCM;

public class UpdateLocationMonitoringService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    // region Variables
    private static final String TAG = "eirajback";
    private GoogleApiClient mLocationClient;
    private LocationRequest mLocationRequest = new LocationRequest();
    private static final int NOTIFICATION_ID = 99;
    public static final String ACTION_LOCATION_BROADCASTUpdate = UpdateLocationMonitoringService.class.getName() + "LocationBroadcast";
    public static final String EXTRA_LATITUDE = "extra_latitude";
    public static final String EXTRA_LONGITUDE = "extra_longitude";
    private String unitLatitude = "", unitLongitude = "";
    private SharedPrefHelper sharedPrefHelper = new SharedPrefHelper();
    private boolean mAlreadyStartedService = false;
    private String latti = "", longii = "", Battery = "", DutyStatus = "";
    private String NOTIFICATION_CHANNEL_ID = Constants.packageName + ".attendancePunch";
    private String channelName = "Attendance Notification";
    private ApiInterface apiService;
    // endregion Variables

    @Override
    public void onCreate() {
        super.onCreate();

        startMyOwnForeground();

        mLocationClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationRequest.setInterval(Constants.LOCATION_INTERVAL);
        mLocationRequest.setFastestInterval(Constants.FASTEST_LOCATION_INTERVAL);

        int priority = LocationRequest.PRIORITY_HIGH_ACCURACY;

        mLocationRequest.setPriority(priority);
        if (!mLocationClient.isConnected()) {
            mLocationClient.connect();
        }
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
                if (Util.isOnline(getApplicationContext())) {


                    latti = String.valueOf(location.getLatitude());
                    longii = String.valueOf(location.getLongitude());
                    triggerAPI();


                }
                sendMessageToUI(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));
            } else {
                getLastLocationNewMethod();
            }
        }

    }

    private void getLastLocationNewMethod() {
        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // GPS location can be null if GPS is switched off
                        if (location != null) {
                            sendMessageToUI(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("MapDemoActivity", "Error trying to get last GPS location");
                        e.printStackTrace();
                    }
                });
    }

    private void startMyOwnForeground() {

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

        /** for stopping notification */
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder
                .setOngoing(true)
              // .setSmallIcon(R.drawable.geoimage)
                .setAutoCancel(true)
               // .setContentTitle("You have punched in")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }

    private void sendMessageToUI(String lat, String lng) {

        /*Intent intent = new Intent(ACTION_LOCATION_BROADCASTUpdate);
        intent.putExtra(EXTRA_LATITUDE, lat);
        intent.putExtra(EXTRA_LONGITUDE, lng);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);*/
        BatteryManager bm = (BatteryManager) getSystemService(BATTERY_SERVICE);
        int batLevel = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        }

        latti = lat;  // current latitude
        longii = lng; // current longitude
        Battery = String.valueOf(Util.getBatteryPercentage(getApplicationContext()));
        //startMyOwnForeground();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    private void triggerAPI() {
        mAlreadyStartedService = false;

        apiService = ApiClient.getClient().create(ApiInterface.class);

        final String dutyStatusForStatusAPI = DutyStatus;

        unitLatitude = sharedPrefHelper.getUnitLatitude();
        unitLongitude = sharedPrefHelper.getUnitLongitude();

        double km = 0.0; // km distance from unit to current location
        double kmFromPreviousLocation = 0.0; // km distance from unit to current location
        if (!unitLatitude.equals("0.0") && !unitLatitude.isEmpty()) {
            double dis = Util.distance(
                    Double.parseDouble(unitLatitude),
                    Double.parseDouble(unitLongitude),
                    Double.parseDouble(latti),
                    Double.parseDouble(longii));
            km = dis / 0.62137;
        }

        if (!sharedPrefHelper.getPreviousLatitude().equals("0.0") && !sharedPrefHelper.getPreviousLatitude().isEmpty()) {
            double dis = Util.distance(
                    Double.parseDouble(sharedPrefHelper.getPreviousLatitude()),
                    Double.parseDouble(sharedPrefHelper.getPreviousLongitude()),
                    Double.parseDouble(latti),
                    Double.parseDouble(longii));
            kmFromPreviousLocation = dis / 0.62137;
        }

        Geocoder geocoder;
        List<Address> addresses;
        String address = "";
        geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(Double.parseDouble(latti), Double.parseDouble(longii), 1);
            address = addresses.get(0).getAddressLine(0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (sharedPrefHelper.isAttendanceActive()) {
            if (km < 0.5) {
                if (kmFromPreviousLocation > 0.04) {
                    sharedPrefHelper.setPreviousLatitude(latti);
                    sharedPrefHelper.setPreviousLongitude(longii);
                    updateEmpLocation(sharedPrefHelper.getEmployeeCode(), latti, longii, String.valueOf(km), address, sharedPrefHelper.getUnitCode(), sharedPrefHelper.getCompanyID());
                }
            }
        }

        // Testing
        /*sendNotification(
                "dLwBXhtSTBe4XHNZDd3fyx:APA91bGL5TXcaZmMXv1Q34mdXIPFca3IK8WS1fyZ4YT8eB_nZpvLIVfab0imTjK1DZumunfD7YPq4QuH2HYqGy95dz7XZ2-EDzmJcM4El-t7iUX0wIIIqoGfRwzSzHX3p_o4EFiDPkcW",
                "TEST",
                "1010",
                "AKASH SARKAR",
                "CANTICLE BRANCH");*/
    }

    private void updateEmpLocation(String EmpCode, String LastLat, String LastLng, String LastDistance,
                                   String LocAddress, String UnitCode, String CompId) {
        Call<UpdateEmpLocationModel> updateEmployeeLocation = apiService.updateEmployeeLocation(EmpCode, LastLat, LastLng, LastDistance, LocAddress, UnitCode, CompId, Battery);


        updateEmployeeLocation.enqueue(new Callback<UpdateEmpLocationModel>() {
            @Override
            public void onResponse(Call<UpdateEmpLocationModel> call, Response<UpdateEmpLocationModel> response) {
                try {
                    /*String responseData = response.body().string();
                    Log.e("XXX", "onResponse: " + responseData);
                    // "{\"UnitName\":\"OBERTHER CARD SYSTEMS PVT. PTD.\",\"SupFcmToken\":\"\",\"EmpName\":\"\",\"BranchName\":\"NOIDA BRANCH\",\"BranchCode\":3}"
                    responseData = responseData.substring(1, responseData.length() - 1);
                    responseData = responseData.replace("\\", "");

                    JSONObject object = new JSONObject(responseData);*/

                    if (!response.body().getSupFcmToken().isEmpty()) {
                        sendNotification(response.body().getSupFcmToken(),
                                response.body().getUnitName(),
                                sharedPrefHelper.getEmployeeCode(),
                                response.body().getEmpName(),
                                response.body().getBranchName());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<UpdateEmpLocationModel> call, Throwable t) {

            }
        });

    }

    private void sendNotification(String supFcmToken, String unitName, String empCode, String empName, String branchName) {

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.level(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .connectTimeout(5, TimeUnit.MINUTES)
                .readTimeout(5, TimeUnit.MINUTES)
                .build();

        ApiInterface fcmApiInterface = new Retrofit.Builder()
                .baseUrl(BASE_URL_FCM)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiInterface.class);

        JsonObject data = new JsonObject();
        JsonObject innerData = new JsonObject();

        innerData.addProperty("unitName", unitName);
        innerData.addProperty("empCode", empCode);
        innerData.addProperty("empName", empName);
        innerData.addProperty("branchName", branchName);
        innerData.addProperty("title", String.format("%s LEFT SITE !!", empName));
        innerData.addProperty("notificationType", "Out");
        innerData.addProperty("message", String.format("%s has left unit %s in branch %s", empName, unitName, branchName));

        data.add("data", innerData);
        data.addProperty("to", supFcmToken);

        Call<ResponseBody> sendNotification = fcmApiInterface.sendNotification(data);
        sendNotification.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }
}