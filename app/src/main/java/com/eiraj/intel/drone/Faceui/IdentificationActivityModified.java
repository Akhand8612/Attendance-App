package com.eiraj.intel.drone.Faceui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.eiraj.intel.drone.Database.DatabaseConnection;
import com.eiraj.intel.drone.R;
import com.eiraj.intel.drone.Utils.AlertDialogs;
import com.eiraj.intel.drone.Utils.Constants;
import com.eiraj.intel.drone.Utils.ImageUtils;
import com.eiraj.intel.drone.Utils.SharedPrefHelper;
import com.eiraj.intel.drone.Utils.Util;
import com.eiraj.intel.drone.Utils.service.HandleService;
import com.eiraj.intel.drone.Utils.service.LiveTrackService;
import com.eiraj.intel.drone.Utils.service.UpdateLocationMonitoringService;
import com.eiraj.intel.drone.activities.EmployeeDetail.Employee_Details;
import com.eiraj.intel.drone.helper.LogHelper;
import com.eiraj.intel.drone.model.FaceRecognizeModel;
import com.eiraj.intel.drone.rest.ApiInterface;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dmax.dialog.SpotsDialog;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.eiraj.intel.drone.Utils.Constants.PUNCH_RANGE_METERS;
import static com.eiraj.intel.drone.Utils.Util.MY_PREFS_NAME;
import static com.eiraj.intel.drone.Utils.service.SyncService.MyPREFERENCES;
import static com.eiraj.intel.drone.activities.EmployeeDetail.MarkAttendance.isTimeAutomatic;
import static com.eiraj.intel.drone.rest.ApiClient.BASE_URL_FACE_RECOGNITION;
import static com.eiraj.intel.drone.rest.ApiClient.POST_ATTENDANCE_IN;
import static com.eiraj.intel.drone.rest.ApiClient.POST_ATTENDANCE_OUT;


public class IdentificationActivityModified extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    // constants
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private final int REQUEST_READ_PHONE_STATE = 1;

    // Tags
    private static final int REQUEST_SELECT_IMAGE = 0;
    private static final String TAG = "IdentificationActivityM";

    // View Elements
    static TextView textViewAddress;
    private TextView siteAddress, identificationStatus;
    public android.app.AlertDialog waitingDialog;

    // Utils and Helpers
    public static String address = "";
    private Handler mHandler;
    String empCode, selectedRadio, type;
    boolean detected = false;
    String supIdStore;
    LocationRequest mLocationRequest = new LocationRequest();
    DatabaseConnection databaseConnection;
    Button identify;
    ProgressDialog progressDialog;
    String destinationLatitude = "0.0", destinationLongitude = "0.0";
    String imageStr = "";
    private String user_id_face_recog = "", ml_userID = "", companyID = "";
    String compGroupID, compid, unitcode, unitLongitude, unitLatitude, empcode,
            //desiCode,
            CreatedByUserID, userName = "", siteName = ""; //designationName = "";
    private double currentLatitude;
    private double currentLongitude;
    private GoogleApiClient mGoogleApiClient;
    private Bitmap mBitmap;
    private SharedPrefHelper sharedPrefHelper;

    // region override methods

    // Lifecycle methods

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identification_modified);

        initView();
        onClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        //Disconnect from API onPause()
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_SELECT_IMAGE:
                if (resultCode == RESULT_OK) {
                    detected = false;

                    // If image is selected successfully, set the image URI and bitmap.

                    /*mBitmap = MainActivityPhotoBlink.bitmap;canticle
                    if (mBitmap != null) {
                        // Show the image on screen.
                        ImageView imageView = findViewById(R.id.image);
                        imageView.setImageBitmap(mBitmap);
                        int Height = mBitmap.getHeight();
                        int Width = mBitmap.getWidth();
                        int newHeight = 200;
                        int newWidth = 200;
                        float scaleWidth = ((float) newWidth) / Width;
                        float scaleHeight = ((float) newHeight) / Height;
                        Matrix matrix = new Matrix();
                        matrix.postScale(scaleWidth, scaleHeight);
                        Bitmap resizedBitmap = Bitmap.createBitmap(mBitmap, 0, 0, Width, Height, matrix, true);

                        uploadImageForRecognition(resizedBitmap);
                        imageStr = "uploaded";
                    }
                }*/

                    int resolution = 600;

                    Bitmap mBitmap = MainActivityPhotoBlink.bitmap;

                    if (mBitmap != null) {
                        // Show the image on screen.
                        ImageView imageView = findViewById(R.id.image);
                        //imageView.setImageBitmap(mBitmap);

                        // Actual Height and Width of image taken
                        int Height = mBitmap.getHeight();
                        int Width = mBitmap.getWidth();

                        // The scaled ratio which will be used
                        float scaleWidthRatio;
                        float scaleHeightRatio;

                        // Calculating the ratio and fixing the height and width of the final image
                        float ratio = (float) Height / resolution;
                        float scaledHeight = resolution;
                        float scaledWidth = (int) (Width / ratio);

                        // Ratio is calculated
                        scaleWidthRatio = ((float) scaledWidth) / Width;
                        scaleHeightRatio = ((float) scaledHeight) / Height;

                        Matrix matrix = new Matrix();
                        matrix.postScale(scaleWidthRatio, scaleHeightRatio);
                        Bitmap resizedBitmap = Bitmap.createBitmap(mBitmap, 0, 0, Width, Height, matrix, true);

                        imageView.setImageBitmap(resizedBitmap);

                        uploadImageForRecognition(resizedBitmap);
                        imageStr = "uploaded";
                    }
                }
                break;
            default:
                break;


        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        customBackPress();
    }

    // Google API Connection Callbacks

    @SuppressLint("MissingPermission")
    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        } else {
            //If everything went fine lets get latitude and longitude
            currentLatitude = location.getLatitude();
            currentLongitude = location.getLongitude();
            destinationLatitude = String.valueOf(currentLatitude);
            destinationLongitude = String.valueOf(currentLongitude);
            Log.w("eirajlocation", String.valueOf(currentLatitude + currentLongitude));


            //    Toast.makeText(this, currentLatitude + " WORKS " + currentLongitude + "", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            Log.e("Error", "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();
        destinationLatitude = String.valueOf(currentLatitude);
        destinationLongitude = String.valueOf(currentLongitude);
        getAddress(getApplicationContext(), currentLatitude, currentLongitude);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                customBackPress();
                break;
        }
        return true;
    }

    // endregion override methods

    // region helper methods

    private void initView() {

        // Init Objects and Widgets
        sharedPrefHelper = new SharedPrefHelper();
        textViewAddress = findViewById(R.id.address);
        siteAddress = findViewById(R.id.siteAddress);
        identify = findViewById(R.id.identify);
        identificationStatus = findViewById(R.id.identificationStatus);

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (waitingDialog.isShowing())
                    waitingDialog.dismiss();
                Intent i = new Intent(getApplicationContext(), Employee_Details.class);

                i.putExtra("internet", "delete post updation");
                startActivity(i);

            }
        };

        check();
        checkPlayServices();

        SharedPreferences sharedpreferences;
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        String companyName = sharedpreferences.getString("companyname", "");
        infoForAttendace();
        databaseConnection = new DatabaseConnection(IdentificationActivityModified.this);

        supIdStore = sharedpreferences.getString("loginCredential", "");
        Intent intent = getIntent();
        waitingDialog = new SpotsDialog.Builder()
                .setContext(IdentificationActivityModified.this)
                .setMessage("Please Wait")
                .setCancelable(false)
                .build();
        empCode = intent.getStringExtra("employeecode");
        selectedRadio = intent.getStringExtra("selectedRadio");
        type = intent.getStringExtra("type");
        if (type.equals("PunchIn")) {
            identify.setText("Punch In");
        } else {
            identify.setText("Punch Out");
        }
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle(getString(R.string.progress_dialog_title));

        LogHelper.clearIdentificationLog();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)//fourth line adds the LocationServices API endpoint from GooglePlayServices
                .build();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(1 * 1000)        // 1 seconds, in milliseconds
                .setFastestInterval(1 * 1000);
        getLastLocationNewMethod();

        userName = getIntent().getStringExtra("userName");
        siteName = getIntent().getStringExtra("siteName");
        //designationName = getIntent().getStringExtra("designationName");

        siteAddress.setText(sharedPrefHelper.getPunchAddress());
        user_id_face_recog = sharedPrefHelper.getUserRegID();
        ml_userID = sharedPrefHelper.getML_registrationID();
        companyID = sharedPrefHelper.getCompanyShortKey();
        setInfo("", true);
    }

    private void uploadImageForRecognition(Bitmap resizedBitmap) {
        progressDialog.setMessage("Recognizing Face");
        progressDialog.show();
        MultipartBody.Part imageFile = ImageUtils.handleAndConvertImage(IdentificationActivityModified.this,
                resizedBitmap, "photo");

        String url = String.format("user_directory/recg%sim.php", ml_userID);


        /*HttpLoggingInterc/
       ptor interceptor = new HttpLoggingInterceptor();
        interceptor.level(H/
       tpLoggingInterceptor.Level.BODY);
        OkHttpClient client/
       = new OkHttpClient.Builder()
                .addInterce/
               tor(interceptor)
                .connectTim/
               out(5, TimeUnit.MINUTES)
                .readTimeou/
               (5, TimeUnit.MINUTES)
                .build();*/
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL_FACE_RECOGNITION)
                .addConverterFactory(GsonConverterFactory.create())
                .client(/*client*/Constants.okHttpClient_3minTimeout)
                .build();
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);

        Call<FaceRecognizeModel> recognizeFace = apiInterface.recognizeFace(
                url,
                imageFile,
                Util.getRequestBody_textPlain(ml_userID));

        Log.e(TAG, "uploadImageForRecognition: " + url + ml_userID);

        recognizeFace.enqueue(new Callback<FaceRecognizeModel>() {
            @Override
            public void onResponse(Call<FaceRecognizeModel> call, Response<FaceRecognizeModel> response) {

                Log.e("XXX", "onResponse: " + response);
                try {
                    if (response.code() == 200) {
                        Log.e("XXX", "onResponse: " + response.body().getCode());
                        Log.e("XXX", "onResponse: " + response.body().getMessage());
                        Log.e("XXX", "onResponse: " + response.body().getStatus());

                        if (response.body().getCode().equals("200")) {
                            detected = true;
                            identificationStatus.setTextColor(getResources().getColor(R.color.green));
                        } else {
                            detected = false;
                            identificationStatus.setTextColor(Color.parseColor("#424242"));
                        }
                        identificationStatus.setText(response.body().getMessage());
                    } else {
                        if (response.code() == 404) {
                            AlertDialogs.show(IdentificationActivityModified.this, "Please ask admin to clear face. [Code - " + response.code() + "]");
                        } else {
                            AlertDialogs.show(IdentificationActivityModified.this, "Something went wrong. [Code - " + response.code() + "]");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(IdentificationActivityModified.this, "Something went wrong.", Toast.LENGTH_SHORT).show();
                }

                if (progressDialog.isShowing())
                    progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<FaceRecognizeModel> call, Throwable t) {
                Log.e("XXX", "onFailure: " + t.getMessage());
                if (progressDialog.isShowing())
                    progressDialog.dismiss();

                AlertDialogs.show(IdentificationActivityModified.this, "Some error occurred, please try again. Code - " + t.getMessage());
            }
        });
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
                            currentLatitude = location.getLatitude();
                            currentLongitude = location.getLongitude();
                            destinationLatitude = String.valueOf(currentLatitude);
                            destinationLongitude = String.valueOf(currentLongitude);
                            getAddress(getApplicationContext(), currentLatitude, currentLongitude);
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

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST);
            } else {
                finish();
            }

            return false;
        }

        return true;
    }

    public void getAddress(Context context, double LATITUDE, double LONGITUDE) {

        /*if (type.equals("PunchIn"))
            sharedPrefHelper.setPunchLatLng(LATITUDE + "," + LONGITUDE);*/

        //Set Address
        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null && addresses.size() > 0) {


                address = addresses.get(0).getAddressLine(0);
                textViewAddress.setText(String.format("%s", address));
                // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();
                String postalCode = addresses.get(0).getPostalCode();
                String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL

            } else {
                HttpClient httpclient = new DefaultHttpClient();
                String postURL = "http://maps.googleapis.com/maps/api/geocode/json?latlng="
                        + LATITUDE + "," + LONGITUDE + "&sensor=true";
                HttpGet httppost = new HttpGet(postURL);
                try {

                    HttpResponse response = httpclient.execute(httppost);
                    BufferedReader rd = new BufferedReader(
                            new InputStreamReader(response.getEntity()
                                    .getContent()));
                    String line = "";
                    StringBuilder sb = new StringBuilder();
                    while ((line = rd.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    String result = sb.toString();

                    JSONObject jobj = new JSONObject(result);
                    JSONArray array = jobj.getJSONArray("results")
                            .getJSONObject(0)
                            .getJSONArray("address_components");
                    int size = array.length();
                    for (int i = 0; i < size; i++) {
                        JSONArray typearray = array.getJSONObject(i)
                                .getJSONArray("types");


                        if (typearray.getString(0).equals("locality")) {
                            address = array.getJSONObject(i).getString(
                                    "long_name");

                        }

                    }

                } catch (Exception e1) {

                }


            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (type.equals("PunchIn"))
            sharedPrefHelper.setPunchAddress(address);
    }

    private void check() {
        LocationManager lm = (LocationManager) IdentificationActivityModified.this.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        if (!gps_enabled && !network_enabled) {
            // notify user
            android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(IdentificationActivityModified.this);
            dialog.setMessage("Gps is not enabled/जीपीएस सक्षम नहीं है");
            dialog.setPositiveButton("Open Setting/सेटिंग खोलें", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    IdentificationActivityModified.this.startActivity(myIntent);
                }
            });
            dialog.setNegativeButton("if you cancel your app will be closed/यदि आप रद्द करते हैं तो आपका ऐप बंद हो जाएगा", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    finish();

                }
            });
            dialog.show();
        }
    }

    @SuppressLint("MissingPermission")
    public String getDeviceIMEI() {
        String deviceUniqueIdentifier = null;
        TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        if (null != tm) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            } else {
                deviceUniqueIdentifier = tm.getDeviceId();

            }
            if (null == deviceUniqueIdentifier || 0 == deviceUniqueIdentifier.length()) {
                deviceUniqueIdentifier = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
            }
        }

        return deviceUniqueIdentifier;
    }

    public void customBackPress() {
        Intent i = new Intent(getApplicationContext(), Employee_Details.class);
        i.putExtra("internet", "c");
        startActivity(i);
        finish();
    }

    private void setInfo(String info, boolean hide) {
        TextView textView = findViewById(R.id.info);
        textView.setText(info);

        if (hide) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setVisibility(View.VISIBLE);
            textView.setText(info);
        }

    }

    private void showConfirmationDialog() {

        View confirmationView = LayoutInflater.from(IdentificationActivityModified.this)
                .inflate(R.layout.attendance_confirmation_dialog, null);

        Dialog dialog = new Dialog(IdentificationActivityModified.this);

        TextView empCode, name, site;
                //designation;
        MaterialButton confirm, cancel;
        empCode = confirmationView.findViewById(R.id.empCodeText);
        name = confirmationView.findViewById(R.id.nameTextView);
        site = confirmationView.findViewById(R.id.siteNameTextView);
       // designation = confirmationView.findViewById(R.id.designationNameTextView);
        confirm = confirmationView.findViewById(R.id.confirmButton);
        cancel = confirmationView.findViewById(R.id.cancelButton);

        empCode.setText(empcode);
        name.setText(userName);
        site.setText(siteName);
        //designation.setText(designationName);

        confirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (type.equals("PunchIn")) {
                    if (isTimeAutomatic(getApplicationContext())) {
                        postAttendanceIn();
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(IdentificationActivityModified.this);
                        builder.setTitle("Need AutoMatic Time");
                        builder.setMessage("This App needs Automatic Android time to punch in.");
                        builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivityForResult(new Intent(android.provider.Settings.ACTION_DATE_SETTINGS), 0);
                                dialog.cancel();
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        builder.show();
                    }

                } else {
                    if (isTimeAutomatic(getApplicationContext())) {
                        postAttendanceOut(empcode);
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(IdentificationActivityModified.this);
                        builder.setTitle("Need AutoMatic Time");
                        builder.setMessage("This App needs Automatic Android time to punch out.");
                        builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivityForResult(new Intent(android.provider.Settings.ACTION_DATE_SETTINGS), 0);
                                dialog.cancel();
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        builder.show();
                    }
                }
            }
        });

        cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                customBackPress();
            }
        });

        dialog.setCanceledOnTouchOutside(false);
        dialog.setContentView(confirmationView);
        dialog.getWindow().setLayout(-1, -2);
        dialog.show();
    }

    // endregion helper methods

    // region onClick Method

    public void selectImage(View view) {
        Intent intent = new Intent(this, MainActivityPhotoBlink.class);
        startActivityForResult(intent, REQUEST_SELECT_IMAGE);
    }

    private void onClickListeners() {
        identify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("IDENTIFY_BUTTON", "onClick: " + type + empCode);
                if (detected) {
                    showConfirmationDialog();
                } else {
                    Toast.makeText(IdentificationActivityModified.this, "Face not detected yet.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // endregion onClick Method

    // region handle attendance

    public void postAttendanceIn() {

        float distanceInMeters = 0;

        if (!sharedPrefHelper.getPunchLatLng().equals("NA")) {

            getLastLocationNewMethod();

            String[] punchLatLng = sharedPrefHelper.getPunchLatLng().split(",");
            double punchInLat = Double.parseDouble(punchLatLng[0]);
            double punchInLng = Double.parseDouble(punchLatLng[1]);

            float[] results = new float[1];
            Location.distanceBetween(punchInLat, punchInLng, currentLatitude, currentLongitude, results);
            distanceInMeters = results[0];
            Log.e(TAG, "DISTANCE_MEASURED :  " + distanceInMeters);
        }

        if (distanceInMeters / 1000 <= PUNCH_RANGE_METERS) {
            Log.e(TAG, "DISTANCE_MEASURED_ADDED :  " + distanceInMeters / 1000);
            if (imageStr.equals("") || imageStr.isEmpty() || imageStr == "") {
                Toast.makeText(IdentificationActivityModified.this, "Kindly Click image first/कृपया पहले छवि पर क्लिक करें", Toast.LENGTH_SHORT).show();

            } else {


                String currentDateAndTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
                ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                @SuppressLint("MissingPermission") NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
                String isOnline = "";
                if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
                    isOnline = "0";
                } else {
                    isOnline = "1";
                }
                String id = empcode + currentDateAndTime;


                SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                editor.putString("unitcode", unitcode);
                editor.putString("empcodeattendance", empcode);
                editor.apply();
                editor.commit();
                int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);

                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
                    Cursor res = databaseConnection.checkPunchInThere(currentDateAndTime);
                    int numRows = res.getCount();
                    if (numRows != 0) {
                        Toast.makeText(getBaseContext(), "you have punched in for today already.Sync the data first/\n" +
                                "आपने आज के लिए उपस्थिति पहले ही चिह्नित कर ली ह||सबसे पहले डेटा सिंक करेंै", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(getApplicationContext(), Employee_Details.class);
                        i.putExtra("internet", "c");
                        startActivity(i);
                    } else {
                        databaseConnection.insertPunchIn(id, compGroupID, compid, unitcode, unitLongitude, unitLatitude, empcode,
                                //desiCode,
                                Double.toString(currentLatitude), Double.toString(currentLongitude), currentDateAndTime, getDeviceIMEI(), imageStr, CreatedByUserID, isOnline, selectedRadio, "false", ml_userID);

                        if (isOnline == "0") {
                            mHandler.sendMessageDelayed(new Message(), 2 * 60000);
                            new PostAttendenceIn(IdentificationActivityModified.this).execute();
                        } else {
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(IdentificationActivityModified.this);
                            builder1.setTitle("Alert");
                            builder1.setMessage("Punch In marked in offline mode/\n" +
                                    "पंच पहले से ऑफ़लाइन मोड में चिह्नित है");
                            builder1.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    Intent i = new Intent(getApplicationContext(), Employee_Details.class);
                                    i.putExtra("internet", "delete post updation");
                                    startActivity(i);
                                }
                            });

                            builder1.show();

                            //  Toast.makeText(getBaseContext(), "Punch In marked in offline mode/पंच मोड ऑफ़लाइन मोड में चिह्नित है", Toast.LENGTH_SHORT).show();


                        }
                    }
                } else {

                    Cursor res = databaseConnection.checkPunchInThere(currentDateAndTime);
                    int numRows = res.getCount();
                    if (numRows != 0) {
                        Toast.makeText(getBaseContext(), "you have punched in for today already.Sync the data first/\n" +
                                "आपने आज के लिए उपस्थिति पहले ही चिह्नित कर ली ह||सबसे पहले डेटा सिंक करेंै", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(getApplicationContext(), Employee_Details.class);
                        i.putExtra("internet", "c");
                        startActivity(i);
                    } else {
                        databaseConnection.insertPunchIn(id, compGroupID, compid, unitcode, unitLongitude, unitLatitude, empcode,
                                //desiCode,
                                Double.toString(currentLatitude), Double.toString(currentLongitude), currentDateAndTime, getDeviceIMEI(), imageStr, CreatedByUserID, isOnline, selectedRadio, "false", ml_userID);

                        if (isOnline == "0") {
                            mHandler.sendMessageDelayed(new Message(), 2 * 60000);
                            new PostAttendenceIn(IdentificationActivityModified.this).execute();
                        } else {
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(IdentificationActivityModified.this);
                            builder1.setTitle("Alert");
                            builder1.setMessage("Punch In marked in offline mode/\n" +
                                    "पंच पहले से ऑफ़लाइन मोड में चिह्नित है");
                            builder1.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    Intent i = new Intent(getApplicationContext(), Employee_Details.class);
                                    i.putExtra("internet", "delete post updation");
                                    startActivity(i);
                                }
                            });

                            builder1.show();

                            //  Toast.makeText(getBaseContext(), "Punch In marked in offline mode/पंच मोड ऑफ़लाइन मोड में चिह्नित है", Toast.LENGTH_SHORT).show();


                        }
                    }
                }

            }

        } else {
            AlertDialogs.show(IdentificationActivityModified.this, getResources().getString(R.string.location_range_out));
        }

        sharedPrefHelper.setUnitCode(unitcode);

    }

    public void postAttendanceOut(String empcode) {


        float distanceInMeters = 0;

        if (!sharedPrefHelper.getPunchLatLng().equals("NA")) {

            getLastLocationNewMethod();

            String[] punchLatLng = sharedPrefHelper.getPunchLatLng().split(",");
            double punchInLat = Double.parseDouble(punchLatLng[0]);
            double punchInLng = Double.parseDouble(punchLatLng[1]);

            float[] results = new float[1];
            Location.distanceBetween(punchInLat, punchInLng, currentLatitude, currentLongitude, results);
            distanceInMeters = results[0];
            Log.e(TAG, "DISTANCE_MEASURED :  " + distanceInMeters);
        }

        if (distanceInMeters / 1000 <= PUNCH_RANGE_METERS) {
            Log.e(TAG, "DISTANCE_MEASURED_ADDED :  " + distanceInMeters / 1000);

            if (imageStr.equals("") || imageStr.isEmpty() || imageStr == "") {
                Toast.makeText(IdentificationActivityModified.this, "Kindly take your photo first/कृपया अपनी तस्वीर पहले ले लो", Toast.LENGTH_SHORT).show();

            } else {


                String currentDateAndTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
                ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                @SuppressLint("MissingPermission") NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
                String isOnline = "";
                if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
                    isOnline = "0";
                } else {
                    isOnline = "1";
                }
                String id = empcode + currentDateAndTime;
                int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);

                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
                    DatabaseConnection databaseConnection = new DatabaseConnection(IdentificationActivityModified.this);
                    Cursor res = databaseConnection.checkPunchOutThere(currentDateAndTime);
                    int numRows = res.getCount();
                    if (numRows != 0) {
                        Toast.makeText(getBaseContext(), "you have punched out for today already.Please Sync data first/\n" +
                                "आपने आज के लिए उपस्थिति पहले ही चिह्नित कर ली है|सबसे पहले डेटा सिंक करें", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(getApplicationContext(), Employee_Details.class);
                        i.putExtra("internet", "c");
                        startActivity(i);
                    } else {
                        databaseConnection.insertPunchOut(id, compGroupID, compid, unitcode, unitLongitude, unitLatitude, empcode,
                                destinationLatitude, destinationLongitude, currentDateAndTime, getDeviceIMEI(), imageStr, CreatedByUserID, isOnline, selectedRadio, "false", ml_userID);
                        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                        editor.putString("empcodeattendance", empcode);
                        editor.apply();
                        editor.commit();
                        if (isOnline == "0") {
                            new PostAttendenceOut(IdentificationActivityModified.this).execute();
                        } else {
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(IdentificationActivityModified.this);
                            builder1.setTitle("Alert");
                            builder1.setMessage("Punch out marked in offline mode/ऑफ़लाइन मोड में चिह्नित पंच आउट करें");
                            builder1.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    Intent i = new Intent(getApplicationContext(), Employee_Details.class);

                                    i.putExtra("internet", "delete post updation");
                                    startActivity(i);
                                }
                            });

                            builder1.show();
                            //  Toast.makeText(getBaseContext(), "Punch out marked in offline mode/ऑफ़लाइन मोड में चिह्नित पंच आउट करें", Toast.LENGTH_SHORT).show();

                        }
                    }
                } else {
                    DatabaseConnection databaseConnection = new DatabaseConnection(IdentificationActivityModified.this);
                    Cursor res = databaseConnection.checkPunchOutThere(currentDateAndTime);
                    int numRows = res.getCount();
                    if (numRows != 0) {
                        Toast.makeText(getBaseContext(), "you have punched out for today already.Please Sync data first/\n" +
                                "आपने आज के लिए उपस्थिति पहले ही चिह्नित कर ली है|सबसे पहले डेटा सिंक करें", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(getApplicationContext(), Employee_Details.class);
                        i.putExtra("internet", "c");
                        startActivity(i);
                    } else {
                        databaseConnection.insertPunchOut(id, compGroupID, compid, unitcode, unitLongitude, unitLatitude, empcode,
                                //desiCode,
                                destinationLatitude, destinationLongitude, currentDateAndTime, getDeviceIMEI(), imageStr, CreatedByUserID, isOnline, selectedRadio, "false", ml_userID);
                        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                        editor.putString("empcodeattendance", empcode);
                        editor.apply();
                        editor.commit();
                        if (isOnline == "0") {
                            new PostAttendenceOut(IdentificationActivityModified.this).execute();
                        } else {
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(IdentificationActivityModified.this);
                            builder1.setTitle("Alert");
                            builder1.setMessage("Punch out marked in offline mode/ऑफ़लाइन मोड में चिह्नित पंच आउट करें");
                            builder1.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    Intent i = new Intent(getApplicationContext(), Employee_Details.class);

                                    i.putExtra("internet", "delete post updation");
                                    startActivity(i);
                                }
                            });

                            builder1.show();
                            //  Toast.makeText(getBaseContext(), "Punch out marked in offline mode/ऑफ़लाइन मोड में चिह्नित पंच आउट करें", Toast.LENGTH_SHORT).show();

                        }
                    }
                }
            }

        } else {
            AlertDialogs.show(IdentificationActivityModified.this, getResources().getString(R.string.location_range_out));
        }
    }

    private void infoForAttendace() {
        databaseConnection = new DatabaseConnection(getApplicationContext());
        Cursor res = databaseConnection.getMarkAttendanceInfo();
        int numRows = res.getCount();
        if (numRows > 0) {


            res.moveToFirst();
            compGroupID = res.getString(0);
            compid = res.getString(1);
            unitcode = res.getString(2);
            unitLongitude = res.getString(3);
            unitLatitude = res.getString(4);
            empcode = res.getString(5);
            //desiCode = res.getString(6);
            CreatedByUserID = res.getString(6);
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
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            waitingDialog.show();
            // bar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            HttpClient httpClient = new DefaultHttpClient();
            HttpParams params = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(params, 3000);

            DatabaseConnection databaseConnection = new DatabaseConnection(IdentificationActivityModified.this);
            Cursor res = databaseConnection.getPunchoutPost();
            int numRows = res.getCount();
            if (numRows > 0) {
                HttpPost httpPost = new HttpPost(POST_ATTENDANCE_OUT);

                Bundle bundle = new Bundle();
                SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                editor.putString("empcodeattendance", empcode);
                editor.apply();                editor.commit();

                res.moveToLast();
                Log.e("PUNCH_OUT", "doInBackground: " + res.getString(1));
                Log.e("PUNCH_OUT", "doInBackground: " + res.getString(2));
                Log.e("PUNCH_OUT", "doInBackground: " + res.getString(3));
                Log.e("PUNCH_OUT", "doInBackground: " + res.getString(4));
                Log.e("PUNCH_OUT", "doInBackground: " + res.getString(5));
                Log.e("PUNCH_OUT", "doInBackground: " + res.getString(6));
                Log.e("PUNCH_OUT", "doInBackground: " + res.getString(7));
                Log.e("PUNCH_OUT", "doInBackground: " + res.getString(8));
                Log.e("PUNCH_OUT", "doInBackground: " + res.getString(9));
                Log.e("PUNCH_OUT", "doInBackground: " + res.getString(10));
                Log.e("PUNCH_OUT", "doInBackground: " + res.getString(11));
                Log.e("PUNCH_OUT", "doInBackground: " + res.getString(12));
                Log.e("PUNCH_OUT", "doInBackground: " + res.getString(13));
                Log.e("PUNCH_OUT", "doInBackground: " + res.getString(14));
                Log.e("PUNCH_OUT", "doInBackground: " + res.getString(15));

                List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
                nameValuePair.add(new BasicNameValuePair("CompGroupID", res.getString(1)));
                nameValuePair.add(new BasicNameValuePair("CompID", res.getString(2)));
                nameValuePair.add(new BasicNameValuePair("Unitcode", res.getString(3)));
                nameValuePair.add(new BasicNameValuePair("UnitLongitude", res.getString(4)));
                nameValuePair.add(new BasicNameValuePair("UnitLatitude", res.getString(5)));
                nameValuePair.add(new BasicNameValuePair("Empcode", res.getString(6)));
               // nameValuePair.add(new BasicNameValuePair("Desicode", res.getString(7)));
                nameValuePair.add(new BasicNameValuePair("DutyOutLongitude", res.getString(8)));
                nameValuePair.add(new BasicNameValuePair("DutyOutLatitude", res.getString(7)));
                nameValuePair.add(new BasicNameValuePair("DutyOutDate", res.getString(9)));
                nameValuePair.add(new BasicNameValuePair("DutyOutMobileNo", "1111111111312"));
                nameValuePair.add(new BasicNameValuePair("DutyOutImeiNo", res.getString(10)));
                nameValuePair.add(new BasicNameValuePair("DutyOutImage", /*null*/res.getString(11)));
                nameValuePair.add(new BasicNameValuePair("CreatedByUserID", res.getString(12)));
                nameValuePair.add(new BasicNameValuePair("CreatedDate", ""));
                nameValuePair.add(new BasicNameValuePair("ModifiedByUserID", ""));
                nameValuePair.add(new BasicNameValuePair("ModifiedDate", ""));
                nameValuePair.add(new BasicNameValuePair("AttRemark", ""));
                nameValuePair.add(new BasicNameValuePair("OfflineMode", res.getString(13)));
                nameValuePair.add(new BasicNameValuePair("AttType", res.getString(14)));
                String battery = String.valueOf(Util.getBatteryPercentage(ctx));
                nameValuePair.add(new BasicNameValuePair("Battery", battery));
                nameValuePair.add(new BasicNameValuePair("Address", textViewAddress.getText().toString()));


//                Log.w("punchout", nameValuePair.toString());
//                Log.w("punchout", "CreatedByUserID" + res.getString(13));
//                Log.w("punchout", "OfflineMode" + res.getString(14));
//                Log.w("punchout", "AttType" + res.getString(14));
                try {
                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
                } catch (UnsupportedEncodingException e) {
                    // writing error to Log
                    e.printStackTrace();
                }

                try {
                    HttpResponse response = httpClient.execute(httpPost);

                    //String dealername, String psrname, String beatname,String selecteddate,String itemname

                    String responseBody = EntityUtils.toString(response.getEntity());
                    Log.e("XXX", "doInBackground: " + responseBody);

                    databaseConnection.updatePunchout(res.getString(0));
                    //  databaseConnection.updateFeedback(cursor.getString(0));
                    // writing response to log
                    Log.d("Http Response:", response.toString());

                    if (responseBody.contains("Attendence Has Punched !!!")) {
                        responseBody = "You Have Logged Out Successfully for Today/आपने आज के लिए सफलतापूर्वक लॉग आउट किया है";
                        sharedPrefHelper.setPunchInStatus(2);
                        sharedPrefHelper.setPunchOutStatus(1);
                        //sharedPrefHelper.setCurrentDesignationName("null");
                        sharedPrefHelper.setLastPunchOutTime(System.currentTimeMillis());
                        //HandleService.stopLocationTrack(IdentificationActivityModified.this);

                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                stopService(new Intent(IdentificationActivityModified.this, UpdateLocationMonitoringService.class));
                                sharedPrefHelper.setAttendanceActive(false);
                                updateLiveTrack(false);
                            }
                        });
                    }

                    if (responseBody.contains("Duty IN Attendance not available")) {
                        responseBody = "Your punch in is not there kindly check attendance report/\n" +
                                "आपका पंच नहीं है, कृपया उपस्थिति रिपोर्ट देखें";
                    }
                    final String finalResponseBody = responseBody;
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            new AlertDialog.Builder(ctx)
                                    .setTitle("Info")
                                    .setMessage((finalResponseBody))
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent i = new Intent(getApplicationContext(), Employee_Details.class);
                                            i.putExtra("internet", "delete post updation");
                                            startActivity(i);
                                        }
                                    })

                                    .show();

                            // Stuff that updates the UI


                        }
                    });

                    //  databaseConnection.insertlog(logid,mobno, GetLoginApi.userCode,currdates,version,manufacturer,model,osversion,logstat,GetLoginApi.DataAreaID);


                } catch (ClientProtocolException e) {
                    // writing exception to log
                    e.printStackTrace();
                } catch (IOException e) {
                    // writing exception to log
                    e.printStackTrace();

                }

            }

            return null;
        }


        @Override
        protected void onPostExecute(Void result) {

            super.onPostExecute(result);
            waitingDialog.dismiss();
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }

    }

    private void updateLiveTrack(boolean startLiveTrack) {
        sharedPrefHelper.setDutyActive(startLiveTrack);
        sharedPrefHelper.setSupervisorNotifiedAboutOutOfRange(false);
        sharedPrefHelper.setSupervisorNotifiedAboutInsideRange(true); // default true as initially user is inside site so don't call inside api in first attempt

        sharedPrefHelper.setUnitCode(unitcode);
        sharedPrefHelper.setEmployeeName(userName);
        sharedPrefHelper.setEmployeeCode(empCode);

        if (startLiveTrack) {
            sharedPrefHelper.setLiveTrackLastLatitude(destinationLatitude);
            sharedPrefHelper.setLiveTrackLastLongitude(destinationLongitude);

            Intent intent = new Intent(IdentificationActivityModified.this, LiveTrackService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
        } else {
            stopService(new Intent(IdentificationActivityModified.this, LiveTrackService.class));
        }
    }

    class PostAttendenceIn extends AsyncTask<Void, Void, Void> {


        private static final String TAG = "PostAttachments";
        Context ctx;


        public PostAttendenceIn(Context ctx) {
            this.ctx = ctx;
            waitingDialog.show();
            // bar.setVisibility(View.VISIBLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);


        }

        @Override
        protected Void doInBackground(Void... arg0) {
            Log.e("ATTENDANCE_IN", "doInBackground: ");
            HttpClient httpClient = new DefaultHttpClient();
            HttpParams params = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(params, 5000);

            HttpPost httpPost = new HttpPost(POST_ATTENDANCE_IN);

            DatabaseConnection databaseConnection = new DatabaseConnection(IdentificationActivityModified.this);
            Cursor res = databaseConnection.getPunchInPost();

            int numRows = res.getCount();
            if (numRows > 0) {
                res.moveToFirst();
                List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();

                Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                editor.putString("empcodeattendance", empcode);
                editor.apply();
                editor.commit();
                nameValuePair.add(new BasicNameValuePair("CompGroupID", res.getString(1)));
                nameValuePair.add(new BasicNameValuePair("CompID", res.getString(2)));
                nameValuePair.add(new BasicNameValuePair("Unitcode", res.getString(3)));
                nameValuePair.add(new BasicNameValuePair("UnitLongitude", res.getString(4)));
                nameValuePair.add(new BasicNameValuePair("UnitLatitude", res.getString(5)));
                nameValuePair.add(new BasicNameValuePair("Empcode", res.getString(6)));
            //   nameValuePair.add(new BasicNameValuePair("Desicode", res.getString(7)));
                nameValuePair.add(new BasicNameValuePair("DutyInLongitude", res.getString(8)));
                nameValuePair.add(new BasicNameValuePair("DutyInLatitude", res.getString(7)));
                nameValuePair.add(new BasicNameValuePair("DutyINDate", res.getString(9)));
                nameValuePair.add(new BasicNameValuePair("DutyInMobileNo", "111111111"));
                nameValuePair.add(new BasicNameValuePair("DutyInImeiNo", res.getString(10)));
                nameValuePair.add(new BasicNameValuePair("DutyInImage", /*null*/res.getString(11)));
                nameValuePair.add(new BasicNameValuePair("CreatedByUserID", res.getString(12)));
                nameValuePair.add(new BasicNameValuePair("CreatedDate", ""));
                nameValuePair.add(new BasicNameValuePair("ModifiedByUserID", ""));
                nameValuePair.add(new BasicNameValuePair("ModifiedDate", ""));
                nameValuePair.add(new BasicNameValuePair("AttRemark", ""));
                nameValuePair.add(new BasicNameValuePair("OfflineMode", res.getString(13)));
                nameValuePair.add(new BasicNameValuePair("AttType", res.getString(14)));
                String battery = String.valueOf(Util.getBatteryPercentage(ctx));
                nameValuePair.add(new BasicNameValuePair("Battery", battery));
                nameValuePair.add(new BasicNameValuePair("Address", textViewAddress.getText().toString()));


                try {
                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
                } catch (UnsupportedEncodingException e) {
                    // writing error to Log
                    e.printStackTrace();
                }

                try {
                    HttpResponse response = httpClient.execute(httpPost);
                    //String dealername, String psrname, String beatname,String selecteddate,String itemname


                    String responseBody = EntityUtils.toString(response.getEntity());
                    Log.e("XXX", "doInBackground: " + responseBody);
                    Log.w("eiraj", responseBody);

                    databaseConnection.updatePunchIn(res.getString(0));
                    responseBody = responseBody.substring(1, responseBody.length() - 2);
                    if (responseBody.matches("[0-9]+")) {
                        responseBody = "You Have Logged in Successfully for Today/आपने आज के लिए सफलतापूर्वक लॉग इन किया है";
                        sharedPrefHelper.setPunchInStatus(1);
                        sharedPrefHelper.setPunchOutStatus(2);
                        //sharedPrefHelper.setCurrentDesignationName(res.getString(7));
                       // HandleService.startLocationTrack(IdentificationActivityModified.this);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateLiveTrack(true);

                                if (VERSION.SDK_INT >= VERSION_CODES.O) {
                                    startForegroundService(new Intent(IdentificationActivityModified.this, UpdateLocationMonitoringService.class));
                                } else {
                                    startService(new Intent(IdentificationActivityModified.this, UpdateLocationMonitoringService.class));
                                }
                                sharedPrefHelper.setAttendanceActive(true);
                            }
                        });

                    }
                    //  databaseConnection.updateFeedback(cursor.getString(0));
                    // writing response to log
                    Log.d("Http Response:", response.toString());

                    final String finalResponseBody = responseBody;
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            new Builder(ctx)
                                    .setTitle("Info")
                                    .setMessage((finalResponseBody))
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            Intent i = new Intent(getApplicationContext(), Employee_Details.class);
                                            i.putExtra("internet", "delete post updation");
                                            startActivity(i);
                                        }
                                    })

                                    .show();

                            // Stuff that updates the UI

                        }
                    });
                    //  databaseConnection.insertlog(logid,mobno, GetLoginApi.userCode,currdates,version,manufacturer,model,osversion,logstat,GetLoginApi.DataAreaID);


                } catch (ClientProtocolException e) {
                    // writing exception to log
                    e.printStackTrace();
                } catch (IOException e) {
                    // writing exception to log
                    e.printStackTrace();

                }


            }

            return null;
        }


        @Override
        protected void onPostExecute(Void result) {


            super.onPostExecute(result);
            if (waitingDialog.isShowing()) {
                waitingDialog.dismiss();
                // Its visible
            }
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }

    }

    // endregion handle attendance
}

