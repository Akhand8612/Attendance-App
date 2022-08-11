package com.eiraj.intel.drone.Utils.service;

/**
 * Created by RITIK on 4/8/2018.
 */

/**
 *  Modified by Akhand Pratap on 24/07/2022.
 */

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import com.eiraj.intel.drone.Database.DatabaseConnection;
import com.eiraj.intel.drone.Utils.Constants;
import com.eiraj.intel.drone.Utils.ImageUtils;
import com.eiraj.intel.drone.Utils.Util;
import com.eiraj.intel.drone.model.FaceRecognizeModel;
import com.eiraj.intel.drone.rest.ApiInterface;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.eiraj.intel.drone.rest.ApiClient.BASE_URL_FACE_RECOGNITION;
import static com.eiraj.intel.drone.rest.ApiClient.POST_ATTENDANCE_IN;
import static com.eiraj.intel.drone.rest.ApiClient.POST_ATTENDANCE_OUT;

// Service handled from
// Dashboard face : 405
// Employee Details : 147
// Sync List : 514, 114
// Login Activity : 102
public class SyncService extends Service implements Runnable {
    public static final int ALARM_TRIGGER_RESTART = 10 * 1000 * 60;
    public static final String MyPREFERENCES = "MyPrefs";
    private static final long DELAY = 1000 * 6;
    private static final String TAG = "BOOMBOOMTESTGPS";
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;
    boolean detected;
    long delay;
    int flag = 0; // 0 = recognition pending, 1 = recognition done
    boolean isRecognitionLeft = false;
    private boolean running = true;
    private Thread runningThread = null;
    private LocationManager mLocationManager = null;

    private static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void onCreate() {
        super.onCreate();
        Log.d("Service", "SyncService onCreate======================");

        runningThread = new Thread(this);
        runningThread.start();
        Log.d("Service", "SyncService restarted======================");
        registerAlarm();

    }

    private void registerAlarm() {
        Intent restartServiceIntent = new Intent(getApplicationContext(), getClass());
        restartServiceIntent.setPackage(getPackageName());

        PendingIntent restartServicePendingIntent = PendingIntent.getService(getApplicationContext(), 10, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(
                AlarmManager.ELAPSED_REALTIME_WAKEUP, ALARM_TRIGGER_RESTART,
                restartServicePendingIntent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        running = false;
        try {
            runningThread.interrupt();
        } catch (Exception e) {
        }
        Log.d("service", "SyncService onTaskRemoved======================");
        stopSelf();
    }

    public void onDestroy() {
        running = false;
        Log.d("service", "SyncService onDestroy======================");

        Intent restartServiceIntent = new Intent(getApplicationContext(), getClass());
        restartServiceIntent.setPackage(getPackageName());

        PendingIntent restartServicePendingIntent = PendingIntent.getService(getApplicationContext(),
                1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(
                AlarmManager.ELAPSED_REALTIME_WAKEUP, 5000,
                restartServicePendingIntent);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        //super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    public void run() {
        running = true;
        boolean dataExists = false;
        delay = DELAY;

        while (running) {


            dataExists = false;

            if (runningThread.isInterrupted()) {
                return;
            }
            //check here if we have the network
            try {

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            if (isNetworkAvailable(getApplicationContext())) {
                                Log.w(TAG, "network");
                                // TODO : Commented for testing purpose
                                //checkAllFaces_punchIn();
                                //new PostAttendenceIn(getApplicationContext()).execute();

                            }
                        } catch (java.lang.SecurityException ex) {
                            Log.i(TAG, "fail to request location update, ignore", ex);
                        } catch (IllegalArgumentException ex) {
                            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
                        } catch (Exception e) {
                            Log.d("Exceeption", e.toString());
                        }
                        try {

                        } catch (java.lang.SecurityException ex) {
                            Log.i(TAG, "fail to request location update, ignore", ex);
                        } catch (IllegalArgumentException ex) {
                            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
                        } catch (Exception e) {
                            Log.d("Exceeption", e.toString());
                        }
                        Log.d("Service", "Network available execute task======================");
                        delay = DELAY;

                    }
                });


            } catch (Exception e) {
                Log.d("Service", "syncService method Exception" + e.toString());
                return;
            }

            if (!dataExists) {
                try {
                    Thread.sleep(delay);
                } catch (Exception e) {
                }
            }
        }
    }

    private void checkAllFaces_punchIn() {
        // Checking punch in faces
        DatabaseConnection databaseConnection = new DatabaseConnection(getApplicationContext());
        Cursor cursor = databaseConnection.getPunchInPost_unRecognizedFaces();
        int totalRows_punchIn = cursor.getCount();
        if (totalRows_punchIn > 0) {
            cursor.moveToFirst();
            for (int i = 0; i <= totalRows_punchIn; i++) {
                if (i == totalRows_punchIn) {
                    checkAllFaces_punchOut();
                } else {
                    String db_id = cursor.getString(0);
                    String image_str = cursor.getString(12);
                    String ml_id = cursor.getString(19);
                    convertToBitmap(db_id, image_str, ml_id);
                }
            }
        } else {
            // no faces to check in punch in
            checkAllFaces_punchOut();
        }
    }

    private void convertToBitmap(String db_id, String image_str, String ml_id) {
        byte[] decodedString = Base64.decode(image_str, Base64.DEFAULT);
        Bitmap resultBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        uploadImageForRecognition(resultBitmap, ml_id, db_id);
    }

    private void checkAllFaces_punchOut() {
        // Checking punch out faces
        DatabaseConnection databaseConnection = new DatabaseConnection(getApplicationContext());
        Cursor cursor = databaseConnection.getPunchOutPost_unRecognizedFaces();
        int totalRows_punchIn = cursor.getCount();
        if (totalRows_punchIn > 0) {
            cursor.moveToFirst();
            for (int i = 0; i <= totalRows_punchIn; i++) {
                if (i == totalRows_punchIn) {

                } else {
                    String db_id = cursor.getString(0);
                    String image_str = cursor.getString(12);
                    String ml_id = cursor.getString(19);
                    convertToBitmap(db_id, image_str, ml_id);
                }
            }
        } else {
            // no faces to check in punch out
        }
    }

    private void uploadImageForRecognition(Bitmap resizedBitmap, String ml_userID, String db_id) {
        MultipartBody.Part imageFile = ImageUtils.handleAndConvertImage(getApplicationContext(),
                resizedBitmap, "photo");

        String url = String.format("user_directory/recg%sim.php", ml_userID);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL_FACE_RECOGNITION)
                .addConverterFactory(GsonConverterFactory.create())
                .client(Constants.okHttpClient_3minTimeout)
                .build();
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);

        Call<FaceRecognizeModel> recognizeFace = apiInterface.recognizeFace(
                url,
                imageFile,
                Util.getRequestBody_textPlain(ml_userID));

        Log.e("XXX", "uploadImageForRecognition: " + url + ml_userID);

        recognizeFace.enqueue(new Callback<FaceRecognizeModel>() {
            @Override
            public void onResponse(Call<FaceRecognizeModel> call, Response<FaceRecognizeModel> response) {

                Log.e("XXX", "onResponse: " + response);
                try {
                    Log.e("XXX", "onResponse: " + response.body().getCode());
                    Log.e("XXX", "onResponse: " + response.body().getMessage());
                    Log.e("XXX", "onResponse: " + response.body().getStatus());

                    DatabaseConnection databaseConnection = new DatabaseConnection(getApplicationContext());

                    if (response.body().getCode().equals("200")) {
                        databaseConnection.updatePunch_faceRecog(db_id, "true");
                    } else {
                        databaseConnection.updatePunch_faceRecog(db_id, "failed");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<FaceRecognizeModel> call, Throwable t) {
                Log.e("XXX", "onFailure: " + t.getMessage());
            }
        });
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public class PostAttendenceIn extends AsyncTask<Void, Void, Void> {


        private static final String TAG = "PostAttedancein";
        Context ctx;


        public PostAttendenceIn(Context ctx) {
            this.ctx = ctx;


        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpClient httpClient = new DefaultHttpClient();
            HttpParams params = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(params, 3000);


            HttpPost httpPost = new HttpPost(POST_ATTENDANCE_IN);
            Log.w("eiraj", TAG);
            DatabaseConnection databaseConnection = new DatabaseConnection(getApplicationContext());
            // Cursor res = databaseConnection.getDelete();
            Cursor res = databaseConnection.getPunchInPost();
            int numRows = res.getCount();
            Log.w("eiraj", String.valueOf(numRows));
            if (numRows > 0) {
                res.moveToFirst();
                Log.w("eiraj", res.getString(6));
                for (int i = 0; i < res.getCount(); i++) {
                    List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
                    nameValuePair.add(new BasicNameValuePair("AttRecordID", ""));
                    nameValuePair.add(new BasicNameValuePair("CompGroupID", res.getString(1)));
                    nameValuePair.add(new BasicNameValuePair("CompID", res.getString(2)));
                    nameValuePair.add(new BasicNameValuePair("Unitcode", res.getString(3)));
                    nameValuePair.add(new BasicNameValuePair("UnitLongitude", res.getString(4)));
                    nameValuePair.add(new BasicNameValuePair("UnitLatitude", res.getString(5)));

                    nameValuePair.add(new BasicNameValuePair("Empcode", res.getString(6)));
                  //  nameValuePair.add(new BasicNameValuePair("Desicode", res.getString(7)));
                    nameValuePair.add(new BasicNameValuePair("DutyInLongitude", res.getString(8)));
                    nameValuePair.add(new BasicNameValuePair("DutyInLatitude", res.getString(9)));
                    nameValuePair.add(new BasicNameValuePair("DutyINDate", res.getString(10)));
                    nameValuePair.add(new BasicNameValuePair("DutyInMobileNo", "111111111"));
                    nameValuePair.add(new BasicNameValuePair("DutyInImeiNo", res.getString(11)));
                    nameValuePair.add(new BasicNameValuePair("DutyInImage", res.getString(12)));
                    nameValuePair.add(new BasicNameValuePair("CreatedByUserID", res.getString(13)));
                    nameValuePair.add(new BasicNameValuePair("CreatedDate", ""));
                    nameValuePair.add(new BasicNameValuePair("ModifiedByUserID", ""));
                    nameValuePair.add(new BasicNameValuePair("ModifiedDate", ""));
                    nameValuePair.add(new BasicNameValuePair("AttRemark", ""));
                    nameValuePair.add(new BasicNameValuePair("OfflineMode", res.getString(14)));
                    nameValuePair.add(new BasicNameValuePair("AttType", res.getString(15)));


                    try {
                        httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
                    } catch (UnsupportedEncodingException e) {
                        // writing error to Log
                        e.printStackTrace();
                        Log.w("eiraj", "exception" + e.toString());
                    }

                    try {
                        HttpResponse response = httpClient.execute(httpPost);
                        //String dealername, String psrname, String beatname,String selecteddate,String itemname
                        Log.w("eiraj", String.valueOf(response));

                        final String responseBody = EntityUtils.toString(response.getEntity());
                        Log.w("eiraj", responseBody);
                        databaseConnection.updatePunchIn(res.getString(0));
                        if (responseBody.contains("Success")) {

                        }
                        //  databaseConnection.updateFeedback(cursor.getString(0));
                        // writing response to log
                        Log.d("Http Response:", response.toString());


                        //  databaseConnection.insertlog(logid,mobno, GetLoginApi.userCode,currdates,version,manufacturer,model,osversion,logstat,GetLoginApi.DataAreaID);


                    } catch (ClientProtocolException e) {
                        // writing exception to log
                        Log.w("eiraj", "exception" + e.toString());
                        e.printStackTrace();
                    } catch (IOException e) {
                        // writing exception to log
                        Log.w("eiraj", "exception" + e.toString());
                        e.printStackTrace();

                    }

                    res.moveToNext();
                }
            }

            return null;
        }


        @Override
        protected void onPostExecute(Void result) {

            super.onPostExecute(result);
            Log.w("eiraj", "post in onexecute");
            new PostAttendenceOut(getApplicationContext()).execute();
        }

    }

    class PostAttendenceOut extends AsyncTask<Void, Void, Void> {

        private static final String TAG = "PostAttachments";
        Context ctx;
        String username, dataareaid;
        String img = "";
        private byte[] response;

        public PostAttendenceOut(Context ctx) {
            this.ctx = ctx;


        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpClient httpClient = new DefaultHttpClient();
            HttpParams params = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(params, 3000);

            DatabaseConnection databaseConnection = new DatabaseConnection(getApplicationContext());
            Cursor res = databaseConnection.getPunchoutPost();
            int numRows = res.getCount();
            Log.w("eirajout", String.valueOf(numRows));
            if (numRows > 0) {
                HttpPost httpPost = new HttpPost(POST_ATTENDANCE_OUT);
                res.moveToFirst();
                for (int i = 0; i < res.getCount(); i++) {
                    List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
                    nameValuePair.add(new BasicNameValuePair("CompGroupID", res.getString(1)));
                    nameValuePair.add(new BasicNameValuePair("CompID", res.getString(2)));
                    nameValuePair.add(new BasicNameValuePair("Unitcode", res.getString(3)));
                    nameValuePair.add(new BasicNameValuePair("UnitLongitude", res.getString(4)));
                    nameValuePair.add(new BasicNameValuePair("UnitLatitude", res.getString(5)));
                    nameValuePair.add(new BasicNameValuePair("Empcode", res.getString(6)));
                   // nameValuePair.add(new BasicNameValuePair("Desicode", res.getString(7)));
                    nameValuePair.add(new BasicNameValuePair("DutyOutLongitude", res.getString(8)));
                    nameValuePair.add(new BasicNameValuePair("DutyOutLatitude", res.getString(9)));
                    nameValuePair.add(new BasicNameValuePair("DutyOutDate", res.getString(10)));
                    nameValuePair.add(new BasicNameValuePair("DutyOutMobileNo", "1111111111312"));
                    nameValuePair.add(new BasicNameValuePair("DutyOutImeiNo", res.getString(11)));
                    nameValuePair.add(new BasicNameValuePair("DutyOutImage", res.getString(12)));
                    nameValuePair.add(new BasicNameValuePair("CreatedByUserID", res.getString(13)));
                    nameValuePair.add(new BasicNameValuePair("CreatedDate", ""));
                    nameValuePair.add(new BasicNameValuePair("ModifiedByUserID", ""));
                    nameValuePair.add(new BasicNameValuePair("ModifiedDate", ""));
                    nameValuePair.add(new BasicNameValuePair("AttRemark", ""));
                    nameValuePair.add(new BasicNameValuePair("OfflineMode", res.getString(14)));
                    nameValuePair.add(new BasicNameValuePair("AttType", res.getString(15)));


                    try {
                        httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
                    } catch (UnsupportedEncodingException e) {
                        // writing error to Log
                        e.printStackTrace();
                        Log.w("eirajout", "exception" + e.toString());
                    }

                    try {
                        HttpResponse response = httpClient.execute(httpPost);
                        Log.w("eiraj", response.toString());
                        //String dealername, String psrname, String beatname,String selecteddate,String itemname

                        final String responseBody = EntityUtils.toString(response.getEntity());
                        Log.w("eiraj", responseBody);
                        if (responseBody.contains("Success")) {

                        }
                        databaseConnection.updatePunchout(res.getString(0));
                        //  databaseConnection.updateFeedback(cursor.getString(0));
                        // writing response to log
                        Log.d("Http Response:", response.toString());

                        //  databaseConnection.insertlog(logid,mobno, GetLoginApi.userCode,currdates,version,manufacturer,model,osversion,logstat,GetLoginApi.DataAreaID);


                    } catch (ClientProtocolException e) {
                        // writing exception to log
                        e.printStackTrace();
                        Log.w("eirajout", "exception" + e.toString());
                    } catch (IOException e) {
                        // writing exception to log
                        e.printStackTrace();
                        Log.w("eirajout", "exception" + e.toString());
                    }
                    res.moveToNext();
                }

            }
            return null;
        }


        @Override
        protected void onPostExecute(Void result) {

            super.onPostExecute(result);
        }

    }

//    private class DetectionTask extends AsyncTask<InputStream, String, Face[]> {
//        @Override
//        protected Face[] doInBackground(InputStream... params) {
//            // Get an instance of face service client to detect faces in image.
//            FaceServiceClient faceServiceClient = SampleApp.getFaceServiceClient();
//            try {
//                publishProgress("Detecting...");
//
//                // Start detection.
//                return faceServiceClient.detect(
//                        params[0],  /* Input stream of image to detect */
//                        true,       /* Whether to return face ID */
//                        false,       /* Whether to return face landmarks */
//                        /* Which face attributes to analyze, currently we support:
//                           age,gender,headPose,smile,facialHair */
//                        null);
//            } catch (Exception e) {
//                publishProgress(e.getMessage());
//                return null;
//            }
//        }
//
//        @Override
//        protected void onPreExecute() {
//            //setUiBeforeBackgroundTask();
//        }
//
//        @Override
//        protected void onProgressUpdate(String... values) {
//            // Show the status of background detection task on screen.
//            // setUiDuringBackgroundTask(values[0]);
//        }
//
//        @Override
//        protected void onPostExecute(Face[] result) {
//            //  progressDialog.dismiss();
//
//            //  setAllButtonsEnabledStatus(true);
//
//            if (result != null) {
//                // Set the adapter of the ListView which contains the details of detected faces.
////                mFaceListAdapter = new IdentificationActivity.FaceListAdapter(result);
////                ListView listView = findViewById(R.id.list_identified_faces);
////                listView.setAdapter(mFaceListAdapter);
//
//                if (result.length == 0) {
//                    detected = false;
//                    // setInfo("No faces detected!");
//                    Log.w("counterpoint", "No faces detected!");
////                    SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
////                    String counter = prefs.getString("counterpoint", "0");
////                    if(counter.contains("0")) {
////                        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
////                        editor.putString("counterpoint", "1");
////                        editor.apply();
////                    }else if(counter.contains("1")){
////                        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
////                        editor.putString("counterpoint", "2");
////                        editor.apply();
////                        Intent i = new Intent(getApplicationContext(), DashBoard.class);
////                        i.putExtra("isalert","nooo");
////                        startActivity(i);
////
////                    }else if(counter.contains("2")){
////                        Intent i = new Intent(getApplicationContext(), DashBoard.class);
////                        i.putExtra("isalert","nooo");
////                        startActivity(i);
////                    }
//
//                } else {
//                    detected = true;
//                    List<UUID> faceIds = new ArrayList<>();
//                    List<Face> faces;
//
//                    List<IdentifyResult> mIdentifyResults;
//
//                    // The thumbnails of detected faces.
//                    List<Bitmap> faceThumbnails;
//                    faces = new ArrayList<>();
//                    faceThumbnails = new ArrayList<>();
//                    mIdentifyResults = new ArrayList<>();
//                    Face[] detectionResult=result;
//
//                        faces = Arrays.asList(detectionResult);
//                        for (Face face : faces) {
//                            try {
//                                // Crop face thumbnail with five main landmarks drawn from original image.
//                                faceThumbnails.add(ImageHelper.generateFaceThumbnail(
//                                        mBitmap, face.faceRectangle));
//                            } catch (IOException e) {
//                                // Show the exception when generating face thumbnail fails.
//                               // setInfo(e.getMessage());
//                            }
//                        }
//
//                    for (Face face : mFaceListAdapter.faces) {
//                        faceIds.add(face.faceId);
//                    }
//
////            setAllButtonsEnabledStatus(false);
//
//                    new IdentificationTask("5f1c2074-1251-46b4-9857-22dd2c7d5633").execute(
//                            faceIds.toArray(new UUID[faceIds.size()]));
//                    // setInfo("Click on the \"Identify\" button to identify the faces in image.");
//                }
//            } else {
//                detected = true;
//                //setInfo("Click on the \"Identify\" button to identify the faces in image.");
//            }
//
//            // refreshIdentifyButtonEnabledStatus();
//        }
//    }
//
//    private class IdentificationTask extends AsyncTask<UUID, String, IdentifyResult[]> {
//        private boolean mSucceed = true;
//        String mPersonGroupId;
//
//        IdentificationTask(String personGroupId) {
//            this.mPersonGroupId = personGroupId;
//        }
//
//        @Override
//        protected IdentifyResult[] doInBackground(UUID... params) {
//            String logString = "Request: Identifying faces ";
//            for (UUID faceId : params) {
//                logString += faceId.toString() + ", ";
//            }
//            logString += " in group " + mPersonGroupId;
//            // addLog(logString);
//
//            // Get an instance of face service client to detect faces in image.
//            FaceServiceClient faceServiceClient = SampleApp.getFaceServiceClient();
//            try {
//                publishProgress("Getting person group status...");
//
//                TrainingStatus trainingStatus = faceServiceClient.getLargePersonGroupTrainingStatus(
//                        this.mPersonGroupId);     /* personGroupId */
//                if (trainingStatus.status != TrainingStatus.Status.Succeeded) {
//                    publishProgress("Person group training status is " + trainingStatus.status);
//                    String message = "Person group training status is " + trainingStatus.status;
//                    if (message.contains("Person group training status is Failed")) {
//
//
//                    }
//                    mSucceed = false;
//                    return null;
//                }
//
//                publishProgress("Identifying...");
//
//                // Start identification.
//                return faceServiceClient.identityInLargePersonGroup(
//                        this.mPersonGroupId,   /* personGroupId */
//                        params,                  /* faceIds */
//                        1);  /* maxNumOfCandidatesReturned */
//            } catch (Exception e) {
//                mSucceed = false;
//                publishProgress(e.getMessage());
//                //addLog(e.getMessage());
//                return null;
//            }
//        }
//
//        @Override
//        protected void onPreExecute() {
//            //setUiBeforeBackgroundTask();
//        }
//
//        @Override
//        protected void onProgressUpdate(String... values) {
//            // Show the status of background detection task on screen.a
//            // setUiDuringBackgroundTask(values[0]);
//        }
//
//        @Override
//        protected void onPostExecute(IdentifyResult[] result) {
//            // Show the result on screen when detection is done.
//            setUiAfterIdentification(result, mSucceed);
//        }
//    }
}

