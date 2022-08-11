package com.eiraj.intel.drone.activities.EmployeeDetail;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.eiraj.intel.drone.Database.DatabaseConnection;
import com.eiraj.intel.drone.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import dmax.dialog.SpotsDialog;

import static com.eiraj.intel.drone.Utils.Util.MY_PREFS_NAME;
import static com.eiraj.intel.drone.rest.ApiClient.POST_ATTENDANCE_IN;
import static com.eiraj.intel.drone.rest.ApiClient.POST_ATTENDANCE_OUT;


public class MarkAttendance extends AppCompatActivity implements
        ConnectionCallbacks,
        OnConnectionFailedListener,
        LocationListener {

    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private static final int PERMISSION_CALLBACK_CONSTANT = 100;
    private static final int REQUEST_PERMISSION_SETTING = 101;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    // lists for permissions
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private final int REQUEST_READ_PHONE_STATE = 1;
    public android.app.AlertDialog waitingDialog;
    private final Handler mHandler = new Handler() {
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
    String[] permissionsRequired = new String[]{Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE, Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    Button takePhoto, punchIn, punchOut;
    RadioButton rb;
    String imageStr = "";
    String selectedRadio = "";
    String compGroupID, compid, unitcode, unitLongitude, unitLatitude, empcode,
            //desiCode,
            CreatedByUserID;
    RadioButton radioButton;
    SharedPreferences prefs;
    DatabaseConnection databaseConnection;
    CoordinatorLayout coordinatorLayout;
    private SharedPreferences permissionStatus;
    private boolean sentToSettings = false;
    private RadioGroup radioGroup;
    private ImageView imageView;
    private ProgressBar bar;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private double currentLatitude;
    private double currentLongitude;

    public static boolean isTimeAutomatic(Context c) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return Settings.Global.getInt(c.getContentResolver(), Settings.Global.AUTO_TIME, 0) == 1;
        } else {
            return android.provider.Settings.System.getInt(c.getContentResolver(), android.provider.Settings.System.AUTO_TIME, 0) == 1;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark_attendance);

        checkPlayServices();
        check();
        getId();
        launchActivity();
        setLanguage();
        infoForAttendace();
        radioButtonSelectWhenScreenOpens();


        waitingDialog = new SpotsDialog.Builder()
                .setContext(MarkAttendance.this)
                .setMessage("Please Wait")
                .setCancelable(false)
                .build();
        punchIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isTimeAutomatic(getApplicationContext())) {
                    postAttendanceIn();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MarkAttendance.this);
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


            }
        });
        punchOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isTimeAutomatic(getApplicationContext())) {
                    postAttendanceOut();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MarkAttendance.this);
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
//                mLocationProvider = new LocationProvider(getApplicationContext(), MarkAttendance.this);
//                mLocationProvider.connect();
            }
        });
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.CAMERA},
                                MY_CAMERA_PERMISSION_CODE);
                    } else {
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(cameraIntent, CAMERA_REQUEST);
                    }
                } else {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
                }
            }
        });


        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @SuppressLint("ResourceType")
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                rb = (RadioButton) group.findViewById(checkedId);
                if (null != rb && checkedId > -1) {
                    selectedRadio = rb.getText().toString();

                }

            }
        });
    }

    private void radioButtonSelectWhenScreenOpens() {
        int idWhenScreenOpens = radioButton.getId();
        radioGroup.check(idWhenScreenOpens);
        selectedRadio = "Duty";
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
            CreatedByUserID = res.getString(7);
        }
    }

    private void setLanguage() {
        prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        String language = prefs.getString("lang", null);
        if (language.equals("Hindi")) {
            takePhoto.setText("फोटो लो");
            punchIn.setText("उपस्थिति चिह्नित करें");
            punchOut.setText("बाहर उपस्थिति चिह्नित करें");
        }
    }

    private void getId() {
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        radioGroup.clearCheck();
        takePhoto = findViewById(R.id.takePhoto);
        punchIn = findViewById(R.id.punchIn);
        imageView = findViewById(R.id.imageView);
        bar = (ProgressBar) this.findViewById(R.id.progressBar);
        punchOut = findViewById(R.id.punchOut);
        radioButton = findViewById(R.id.radioButton1);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorlayout);
        permissionStatus = PreferenceManager
                .getDefaultSharedPreferences(this);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                // The next two lines tell the new client that “this” current class will handle connection stuff
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                //fourth line adds the LocationServices API endpoint from GooglePlayServices
                .addApi(LocationServices.API)
                .build();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000);
    }

    @Override
    public void onBackPressed() {

        if (bar.getVisibility() == View.VISIBLE) {
            bar.setVisibility(View.GONE);
        }
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        Intent i = new Intent(getApplicationContext(), Employee_Details.class);
        i.putExtra("internet", "delete post updation");
        startActivity(i);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CALLBACK_CONSTANT) {
            //check if all permissions are granted
            boolean allgranted = false;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    allgranted = true;
                } else {
                    allgranted = false;
                    break;
                }
            }

            if (allgranted) {
                proceedAfterPermission();
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(MarkAttendance.this, permissionsRequired[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(MarkAttendance.this, permissionsRequired[1])
                    || ActivityCompat.shouldShowRequestPermissionRationale(MarkAttendance.this, permissionsRequired[2])) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MarkAttendance.this);
                builder.setTitle("Need Multiple Permissions");
                builder.setMessage("This app needs Camera and Location permissions.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(MarkAttendance.this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else {


            }
        }
    }

    private void proceedAfterPermission() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PERMISSION_SETTING) {
            if (ActivityCompat.checkSelfPermission(MarkAttendance.this, permissionsRequired[0]) == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);

            }
        } else if (requestCode == REQUEST_READ_PHONE_STATE) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);

        } else {
            if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {

                Bitmap photo = (Bitmap) data.getExtras().get("data");
                if (photo != null && !photo.equals("")) {
//                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                    photo.compress(Bitmap.CompressFormat.PNG, 100, baos);
//                    byte[] b = baos.toByteArray();
//                    imageStr = Base64.encodeToString(b, Base64.DEFAULT);

                    int Height = photo.getHeight();
                    int Width = photo.getWidth();
                    int newHeight = 200;
                    int newWidth = 200;
                    float scaleWidth = ((float) newWidth) / Width;
                    float scaleHeight = ((float) newHeight) / Height;
                    Matrix matrix = new Matrix();
                    matrix.postScale(scaleWidth, scaleHeight);
                    Bitmap resizedBitmap = Bitmap.createBitmap(photo, 0, 0, Width, Height, matrix, true);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    resizedBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    byte[] b = baos.toByteArray();
                    imageStr = Base64.encodeToString(b, Base64.DEFAULT);
                    imageView.setImageBitmap(resizedBitmap);

                } else {

                    imageStr = "Redmiphone6error";


                    imageView.setImageBitmap(photo);
                }

            } else {
                imageStr = "Redmiphone6error";


            }
        }
    }

    public String getDeviceIMEI() {
        String deviceUniqueIdentifier = null;
        TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        if (null != tm) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

            } else {
                deviceUniqueIdentifier = tm.getDeviceId();

            }
            if (null == deviceUniqueIdentifier || 0 == deviceUniqueIdentifier.length()) {
                deviceUniqueIdentifier = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
            }
        }

        return deviceUniqueIdentifier;
    }

    /**
     * Return the current state of the permissions needed.
     */


    public void postAttendanceOut() {

        if (imageStr.equals("") || imageStr.isEmpty() || imageStr == "") {
            Toast.makeText(MarkAttendance.this, "Kindly take your photo first/कृपया अपनी तस्वीर पहले ले लो", Toast.LENGTH_SHORT).show();

        } else {


            String currentDateAndTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
            ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
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
            } else {
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
                            Double.toString(currentLatitude), Double.toString(currentLongitude), currentDateAndTime, getDeviceIMEI(), imageStr, CreatedByUserID, isOnline, selectedRadio, "false", "");
                    SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                    editor.putString("empcodeattendance", empcode);
                    editor.apply();
                    editor.commit();
                    if (isOnline == "0") {
                        new PostAttendenceOut(MarkAttendance.this).execute();
                    } else {
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(MarkAttendance.this);
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
    }

    public void postAttendanceIn() {

        if (imageStr.equals("") || imageStr.isEmpty() || imageStr == "") {
            Toast.makeText(MarkAttendance.this, "Kindly Click image first/कृपया पहले छवि पर क्लिक करें", Toast.LENGTH_SHORT).show();

        } else {


            String currentDateAndTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
            ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
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
                            Double.toString(currentLatitude), Double.toString(currentLongitude), currentDateAndTime, getDeviceIMEI(), imageStr, CreatedByUserID, isOnline, selectedRadio, "false", "");

                    if (isOnline == "0") {
                        mHandler.sendMessageDelayed(new Message(), 2 * 60000);
                        new PostAttendenceIn(MarkAttendance.this).execute();
                    } else {
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(MarkAttendance.this);
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


    }

    private boolean hasPermission(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        }

        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
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

    private void check() {
        LocationManager lm = (LocationManager) MarkAttendance.this.getSystemService(Context.LOCATION_SERVICE);
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
            AlertDialog.Builder dialog = new AlertDialog.Builder(MarkAttendance.this);
            dialog.setMessage("Gps is not enabled/जीपीएस सक्षम नहीं है");
            dialog.setPositiveButton("Open Setting/सेटिंग खोलें", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    MarkAttendance.this.startActivity(myIntent);
                    //get gps
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

    private void launchActivity() {
        if (ActivityCompat.checkSelfPermission(MarkAttendance.this, permissionsRequired[0]) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(MarkAttendance.this, permissionsRequired[1]) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(MarkAttendance.this, permissionsRequired[3]) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(MarkAttendance.this, permissionsRequired[2]) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MarkAttendance.this, permissionsRequired[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(MarkAttendance.this, permissionsRequired[1])
                    || ActivityCompat.shouldShowRequestPermissionRationale(MarkAttendance.this, permissionsRequired[3])
                    || ActivityCompat.shouldShowRequestPermissionRationale(MarkAttendance.this, permissionsRequired[2])) {
                //Show Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(MarkAttendance.this);
                builder.setTitle("Need Multiple Permissions/एकाधिक अनुमतियों की आवश्यकता है");
                builder.setMessage("This app needs Camera and Location permissions./इस ऐप को कैमरा और स्थान अनुमतियों की आवश्यकता है।");
                builder.setPositiveButton("Grant/अनुदान", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(MarkAttendance.this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton("Cancel/रद्द करना", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        finish();
                        dialog.cancel();
                    }
                });
                builder.show();
            } else if (permissionStatus.getBoolean(permissionsRequired[0], false)) {
                //Previously Permission Request was cancelled with 'Dont Ask Again',
                // Redirect to Settings after showing Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(MarkAttendance.this);
                builder.setTitle("Need Multiple Permissions");
                builder.setMessage("This app needs Camera and Location permissions./\n" +
                        "इस ऐप को कैमरा और स्थान अनुमतियों की आवश्यकता है।");
                builder.setPositiveButton("Grant/अनुदान", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        sentToSettings = true;
                        // Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        // Uri uri = Uri.fromParts("package", getPackageName(), null);
                        // intent.setData(uri);
                        //startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                        Toast.makeText(getBaseContext(), "Go to Permissions to Grant  Camera and Location/\n" +
                                "अनुदान कैमरा और स्थान पर अनुमतियों पर जाएं", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("Cancel/रद्द करना", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else {
                //just request the permission
                ActivityCompat.requestPermissions(MarkAttendance.this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
            }


            SharedPreferences.Editor editor = permissionStatus.edit();
            editor.putBoolean(permissionsRequired[0], true);
            editor.commit();
        } else {
            //You already have the permission, just go ahead.
            proceedAfterPermission();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Now lets connect to the API
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(this.getClass().getSimpleName(), "onPause()");

        //Disconnect from API onPause()
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }


    }

    /**
     * If connected get lat and long
     */
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

    /**
     * If locationChanges change lat and long
     *
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {
        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();

        // Toast.makeText(this, currentLatitude + " WORKS " + currentLongitude + "", Toast.LENGTH_LONG).show();
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
            HttpClient httpClient = new DefaultHttpClient();
            HttpParams params = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(params, 5000);

            HttpPost httpPost = new HttpPost(POST_ATTENDANCE_IN);

            DatabaseConnection databaseConnection = new DatabaseConnection(MarkAttendance.this);
            Cursor res = databaseConnection.getPunchInPost();
            int numRows = res.getCount();
            if (numRows > 0) {
                res.moveToFirst();
                List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();

                SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                editor.putString("empcodeattendance", empcode);
                editor.apply();
                editor.commit();
                nameValuePair.add(new BasicNameValuePair("CompGroupID", res.getString(1)));
                nameValuePair.add(new BasicNameValuePair("CompID", res.getString(2)));
                nameValuePair.add(new BasicNameValuePair("Unitcode", res.getString(3)));
                nameValuePair.add(new BasicNameValuePair("UnitLongitude", res.getString(4)));
                nameValuePair.add(new BasicNameValuePair("UnitLatitude", res.getString(5)));
                nameValuePair.add(new BasicNameValuePair("Empcode", res.getString(6)));
               // nameValuePair.add(new BasicNameValuePair("Desicode", res.getString(7)));
                nameValuePair.add(new BasicNameValuePair("DutyInLongitude", res.getString(9)));
                nameValuePair.add(new BasicNameValuePair("DutyInLatitude", res.getString(8)));
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
                Log.w("punchin", nameValuePair.toString());
                Log.w("punchin", "CreatedByUserID" + res.getString(13));
                Log.w("punchin", "OfflineMode" + res.getString(14));
                Log.w("punchin", "AttType" + res.getString(14));

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
                    Log.w("eiraj", responseBody);

                    databaseConnection.updatePunchIn(res.getString(0));
                    responseBody = responseBody.substring(1, responseBody.length() - 2);
                    if (responseBody.matches("[0-9]+")) {
                        responseBody = "You Have Logged in Succesfully for Today/आपने आज के लिए सफलतापूर्वक लॉग इन किया है";
                    }
                    //  databaseConnection.updateFeedback(cursor.getString(0));
                    // writing response to log
                    Log.d("Http Response:", response.toString());

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
            if (waitingDialog.isShowing()) {
                waitingDialog.dismiss();
                // Its visible
            }
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
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

            DatabaseConnection databaseConnection = new DatabaseConnection(MarkAttendance.this);
            Cursor res = databaseConnection.getPunchoutPost();
            int numRows = res.getCount();
            if (numRows > 0) {
                HttpPost httpPost = new HttpPost(POST_ATTENDANCE_OUT);

                Bundle bundle = new Bundle();
                SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                editor.putString("empcodeattendance", empcode);
                editor.apply();
                editor.commit();

                res.moveToLast();
                List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
                nameValuePair.add(new BasicNameValuePair("CompGroupID", res.getString(1)));
                nameValuePair.add(new BasicNameValuePair("CompID", res.getString(2)));
                nameValuePair.add(new BasicNameValuePair("Unitcode", res.getString(3)));
                nameValuePair.add(new BasicNameValuePair("UnitLongitude", res.getString(4)));
                nameValuePair.add(new BasicNameValuePair("UnitLatitude", res.getString(5)));
                nameValuePair.add(new BasicNameValuePair("Empcode", res.getString(6)));
              //  nameValuePair.add(new BasicNameValuePair("Desicode", res.getString(7)));


                nameValuePair.add(new BasicNameValuePair("DutyOutLongitude", res.getString(9)));
                nameValuePair.add(new BasicNameValuePair("DutyOutLatitude", res.getString(8)));
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

                Log.w("punchout", nameValuePair.toString());
                Log.w("punchout", "CreatedByUserID" + res.getString(13));
                Log.w("punchout", "OfflineMode" + res.getString(14));
                Log.w("punchout", "AttType" + res.getString(14));
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


                    databaseConnection.updatePunchout(res.getString(0));
                    //  databaseConnection.updateFeedback(cursor.getString(0));
                    // writing response to log
                    Log.d("Http Response:", response.toString());

                    if (responseBody.contains("Attendence Has Punched !!!")) {
                        responseBody = "You Have Logged Out Succesfully for Today/आपने आज के लिए सफलतापूर्वक लॉग आउट किया है";
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

}