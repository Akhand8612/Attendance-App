package com.eiraj.intel.drone.activities.EmployeeDetail;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.eiraj.intel.drone.Database.DatabaseConnection;
import com.eiraj.intel.drone.R;
import com.eiraj.intel.drone.Utils.Constants;
import com.eiraj.intel.drone.Utils.ImageUtils;
import com.eiraj.intel.drone.Utils.SharedPrefHelper;
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

import dmax.dialog.SpotsDialog;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.eiraj.intel.drone.Utils.Util.internetConnectionAvailable;
import static com.eiraj.intel.drone.rest.ApiClient.BASE_URL_FACE_RECOGNITION;
import static com.eiraj.intel.drone.rest.ApiClient.POST_ATTENDANCE_IN;
import static com.eiraj.intel.drone.rest.ApiClient.POST_ATTENDANCE_OUT;


public class SyncList extends AppCompatActivity {
    public AlertDialog waitingDialog;
    ListView listViewSync;
    TextView message;
    DatabaseConnection databaseConnection;
    Button uploadButton;
    Cursor listViewCursor;
    SimpleCursorAdapter sqldb_adapter;
    ProgressBar bar;
    int flag = 0; // 0 = recognition pending, 1 = recognition done
    boolean isRecognitionLeft = false;
    private SharedPrefHelper sharedPrefHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync_list);
        getId();
        stopService();
        GetSyncList();

        waitingDialog = new SpotsDialog.Builder()
                .setContext(SyncList.this)
                .setMessage("please Wait")
                .setCancelable(false)
                .build();


        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRecognitionLeft)
                    flag = 1;
                uploadButton.setEnabled(false);
                if (internetConnectionAvailable(2000)) {
                    waitingDialog.show();
                    if (flag == 0){
                        checkAllFaces_punchIn();
                    } else if (flag == 1){
                        new PostAttendenceIn(getApplicationContext()).execute();
                    }
                } else {
                    uploadButton.setEnabled(true);
                    Toast.makeText(getApplicationContext(), R.string.msg_alert_no_internet, Toast.LENGTH_SHORT).show();
                }
            }
        });
        uploadButton.setEnabled(true);

    }

    private void stopService() {
        //stopService(new Intent(this, SyncService.class));
    }

    private void getId() {
        listViewSync = (findViewById(R.id.syncList));
        uploadButton = (findViewById(R.id.upload));
        message = (findViewById(R.id.noData));
        bar = findViewById(R.id.progressBar);
        databaseConnection = new DatabaseConnection(SyncList.this);

        sharedPrefHelper = new SharedPrefHelper();
    }

    public void GetSyncList() {
        listViewCursor = databaseConnection.getSyncListReport();
        if (listViewCursor != null) {
            if (listViewCursor.getCount() > 0) {

                //date,training,trainingtype,stylist


                listViewCursor.moveToFirst();

                String[] from = new String[]{"Punch in/out time", "Type", "face_recog_status"};
                int[] to = new int[]{
                        R.id.a,
                        R.id.b,
                        R.id.faceStatus
                };

                sqldb_adapter = new android.widget.SimpleCursorAdapter(this, R.layout.activity_sync_listlist, listViewCursor, from, to) {
                    @Override
                    public void bindView(View view, Context context, Cursor cursor) {
                        try {
                            TextView faceStatus = view.findViewById(R.id.faceStatus);
                            TextView time = view.findViewById(R.id.a);
                            TextView punchInOut = view.findViewById(R.id.b);
                            time.setText(cursor.getString(1));
                            punchInOut.setText(cursor.getString(2));
                            switch (cursor.getString(3)) {
                                case "failed":
                                    faceStatus.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                                    faceStatus.setText("Face not recognized.");
                                    break;
                                case "true":
                                    faceStatus.setTextColor(getResources().getColor(R.color.green));
                                    faceStatus.setText("Face recognized successfully.");
                                    break;
                                case "false":
                                    isRecognitionLeft = true;
                                    faceStatus.setText("Face recognition not done yet.");
                                    break;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            super.bindView(view, context, cursor);
                        }
                    }
                };
                listViewSync.setAdapter(sqldb_adapter);

            } else {
                message.setVisibility(View.VISIBLE);
                //listViewSync.setEmptyView(findViewById(R.id.empty_text_view));
                listViewSync.setVisibility(View.GONE);
            }
        } else {
            //listViewSync.setEmptyView(findViewById(R.id.empty_text_view));
            listViewSync.setVisibility(View.GONE);
        }
    }

    private void checkAllFaces_punchIn() {
        // Checking punch in faces
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

    private synchronized void convertToBitmap(String db_id, String image_str, String ml_id) {
        byte[] decodedString = Base64.decode(image_str, Base64.DEFAULT);
        Bitmap resultBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        uploadImageForRecognition(resultBitmap, ml_id, db_id);
    }

    private void checkAllFaces_punchOut() {
        // Checking punch out faces
        Cursor cursor = databaseConnection.getPunchOutPost_unRecognizedFaces();
        int totalRows_punchIn = cursor.getCount();
        if (totalRows_punchIn > 0) {
            cursor.moveToFirst();
            for (int i = 0; i <= totalRows_punchIn; i++) {
                if (i == totalRows_punchIn) {
                    if (waitingDialog.isShowing())
                        waitingDialog.dismiss();
                    uploadButton.setEnabled(true);
                } else {
                    String db_id = cursor.getString(0);
                    String image_str = cursor.getString(12);
                    String ml_id = cursor.getString(19);
                    convertToBitmap(db_id, image_str, ml_id);
                }
            }
        } else {
            // no faces to check in punch out
            if (waitingDialog.isShowing())
            waitingDialog.dismiss();
            uploadButton.setEnabled(true);
        }
        restart();
    }

    private void restart() {
        Toast.makeText(this, "Recognizing faces, please wait...", Toast.LENGTH_SHORT).show();
        finish();
    }

    private synchronized void uploadImageForRecognition(Bitmap resizedBitmap, String ml_userID, String db_id) {
        MultipartBody.Part imageFile = ImageUtils.handleAndConvertImage(SyncList.this,
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

            DatabaseConnection databaseConnection = new DatabaseConnection(getApplicationContext());
            // Cursor res = databaseConnection.getDelete();
            Cursor res = databaseConnection.getPunchInPost_RecognizedFaces();
            int numRows = res.getCount();
            Log.w("eiraj", String.valueOf(numRows));

            if (numRows > 0) {
                res.moveToFirst();
                for (int i = 0; i < res.getCount(); i++) {

                    List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
                    nameValuePair.add(new BasicNameValuePair("AttRecordID", ""));
                    nameValuePair.add(new BasicNameValuePair("CompGroupID", res.getString(1)));
                    nameValuePair.add(new BasicNameValuePair("CompID", res.getString(2)));
                    nameValuePair.add(new BasicNameValuePair("Unitcode", res.getString(3)));
                    nameValuePair.add(new BasicNameValuePair("UnitLongitude", res.getString(4)));
                    nameValuePair.add(new BasicNameValuePair("UnitLatitude", res.getString(5)));

                    nameValuePair.add(new BasicNameValuePair("Empcode", res.getString(6)));
                   //nameValuePair.add(new BasicNameValuePair("Desicode", res.getString(7)));
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

                        String responseBody = EntityUtils.toString(response.getEntity());
                        Log.w("eiraj", responseBody);

                        databaseConnection.updatePunchIn(res.getString(0));
                        try {
                            Thread.currentThread();
                            Thread.sleep(1500);
                        } catch (InterruptedException e) {

                            e.printStackTrace();
                        }
                        if (responseBody.contains("Success")) {

                        }

                        responseBody = responseBody.substring(1, responseBody.length() - 2);
                        if (responseBody.matches("[0-9]+")) {
                            responseBody = "You Have Logged in Successfully for Today/आपने आज के लिए सफलतापूर्वक लॉग इन किया है";
                            //sharedPrefHelper.setPunchInStatus(1);
                            //sharedPrefHelper.setPunchOutStatus(2);
                            //sharedPrefHelper.setCurrentDesignationName(res.getString(7));
                        }
                        //  databaseConnection.updateFeedback(cursor.getString(0));
                        // writing response to log
                        Log.d("Http Response:", response.toString());


                        //  databaseConnection.insertlog(logid,mobno, GetLoginApi.userCode,currdates,version,manufacturer,model,osversion,logstat,GetLoginApi.DataAreaID);
                    } catch (ClientProtocolException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
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
            Cursor res = databaseConnection.getPunchOutPost_RecognizedOrUnRecognizedFaces();
            int numRows = res.getCount();

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
               //     nameValuePair.add(new BasicNameValuePair("Desicode", res.getString(7)));
                    nameValuePair.add(new BasicNameValuePair("DutyOutLongitude", res.getString(8)));
                    nameValuePair.add(new BasicNameValuePair("DutyOutLatitude", res.getString(9)));
                    nameValuePair.add(new BasicNameValuePair("DutyOutDate", res.getString(10)));
                    nameValuePair.add(new BasicNameValuePair("DutyOutMobileNo", "1111111111312"));
                    nameValuePair.add(new BasicNameValuePair("DutyOutImeiNo", res.getString(11)));

                    Log.e("UserImage", "doInBackground: " + res.getString(12) );

                    /*try {
                        if (res.getString(17).equals("failed")) {*/
                            nameValuePair.add(new BasicNameValuePair("DutyOutImage", res.getString(12)));
                        /*} else {
                            nameValuePair.add(new BasicNameValuePair("DutyOutImage", ""));
                        }
                    } catch (Exception e){
                        nameValuePair.add(new BasicNameValuePair("DutyOutImage", ""));
                        e.printStackTrace();
                    }*/


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

                        String responseBody = EntityUtils.toString(response.getEntity());
                        Log.w("eiraj", responseBody);
                        if (responseBody.contains("Success")) {

                        }
                        if (responseBody.contains("Attendence Has Punched !!!")) {
                            responseBody = "You Have Logged Out Successfully for Today/आपने आज के लिए सफलतापूर्वक लॉग आउट किया है";
                            //sharedPrefHelper.setPunchInStatus(2);
                            //sharedPrefHelper.setPunchOutStatus(1);
                            //sharedPrefHelper.setCurrentDesignationName("null");
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
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    waitingDialog.dismiss();
                    uploadButton.setEnabled(true);
                }
            });

            GetSyncList();
            //startService(new Intent(getApplicationContext(), SyncService.class));
        }

    }
}
