package com.eiraj.intel.drone.activities.EmployeeDetail;

/**
 *  Modified by Akhand Pratap Singh
 */

import static com.eiraj.intel.drone.Utils.Util.MY_PREFS_NAME;
import static com.eiraj.intel.drone.Utils.Util.internetConnectionAvailable;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.eiraj.intel.drone.Database.DatabaseConnection;
import com.eiraj.intel.drone.Faceui.IdentificationActivityModified;
import com.eiraj.intel.drone.Faceui.MainActivityPhotoBlink;
import com.eiraj.intel.drone.R;
import com.eiraj.intel.drone.Utils.AlertDialogs;
import com.eiraj.intel.drone.Utils.SharedPrefHelper;
import com.eiraj.intel.drone.Utils.service.AutoPunchOutService;
import com.eiraj.intel.drone.activities.Dashboard.SosActivity;
import com.eiraj.intel.drone.activities.Dashboard.Video_Play;
import com.eiraj.intel.drone.activities.SplashActivity;
import com.eiraj.intel.drone.helper.CheckInternet;
import com.eiraj.intel.drone.model.SaveFCMTokenModel;
//import com.eiraj.intel.drone.model.UserDesignation;
import com.eiraj.intel.drone.rest.ApiClient;
import com.eiraj.intel.drone.rest.ApiInterface;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.crashlytics.internal.network.HttpResponse;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.google.zxing.qrcode.encoder.QRCode;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class Employee_Details extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    // lists for permissions
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private static final int ZBAR_CAMERA_PERMISSION = 1;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private static final int CAMERA_REQUEST = 1888;
    public final int CUSTOMIZED_REQUEST_CODE = 0x0000ffff;
    private final int REQUEST_READ_PHONE_STATE = 1;
    private final int REQUEST_LOCATION_PERMISSION = 1;
    TextView name, fathersname, employeeid, companyname, siteunit, //designation,
            fathersnameTitle, companynameTitle, employeeidTitle, siteunitTitle,
            //designationTitle,
            nameTitle, infoTextView;
//    Spinner spinner;
    Button unitcodebutton, employeecodebutton;
    //List<UserDesignation> listData;
    String selectedItem = "";
   // String Desicode = "";
    String CreatedByUserID = "";
    String CompGroupID = "";
    String DesiRecordID = "";
    String IsActive = "";
    String getRemark = "";
    SharedPreferences prefs;
    String unitname = "";
    String language = "";
    String isclick = "";
    DatabaseConnection databaseConnection;
    String UnitLatitude = "", Unitcode = "", UnitLongitude = "";
    String Compid = "", Empcode = "", EmpRecordID = "", Empname = "", EmpFHName = "", CompName = "", ml_id = "", employeeName = "";
    RadioButton rb;
    String selectedRadio = "";
    RadioButton radioButton;
    RadioButton radioButton2;
    Button punchOut;
    String imageStr = "";
    String type = "";
    /**
     * If locationChanges change lat and long
     */
    String address = "";
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private double currentLatitude = 0.0;
    private double currentLongitude = 0.0;
    private RadioGroup radioGroup;
 // private int selectedSpinnerItemPosition = 0;
    private SharedPrefHelper sharedPrefHelper;
    private ApiInterface apiInterface;
    private FloatingActionButton clearCache;
    private TextView onlineOfflineText;
    private CheckInternet checkInternet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee__details);
        databaseConnection = new DatabaseConnection(getApplicationContext());
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        getId();
        check();
        checkPlayServices();
        requestLocationPermission();
        Intent intent = this.getIntent();
        //startService(new Intent(getApplicationContext(), SyncService.class));
        if (intent != null) {
            String isInternetMessageEmployeeDetail = intent.getExtras().getString("internet");
            if (isInternetMessageEmployeeDetail.equals("delete post updation")) {
                //databaseConnection.deleteUserDesignation();
            }
        }
        ml_id = new SharedPrefHelper().getML_registrationID();

        setLanguage();
        setUnitname();

        setTitles();
        employeeInfoCount();
        handlePunchStatus();
        onClickListeners();
        getDesiredRadiusForLiveTrack();
        updateFCMToken();

        //showInstructions();
        checkInternet.viewChangesOn(onlineOfflineText);
    }

    private void onClickListeners() {

//        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                selectedItem = parent.getItemAtPosition(position).toString();
//                selectedSpinnerItemPosition = position;
//
//                if (selectedSpinnerItemPosition != 0)
//                    sharedPrefHelper.setCurrentDesignationPosition(selectedSpinnerItemPosition);
//
//                Cursor res3 = databaseConnection.getUserDesignation(selectedItem);
//
//                //desName text,CreatedByUserID text,Desicode text,CompGroupID text,
//                // DesiRecordID text,IsActive text,getRemark text
//                int numRows3 = res3.getCount();
//                if (numRows3 > 0) {
//                    res3.moveToFirst();
//
//                    CompGroupID = res3.getString(3);
//                    Desicode = res3.getString(2);
//                    CreatedByUserID = res3.getString(1);
//                    DesiRecordID = res3.getString(4);
//                    IsActive = res3.getString(5);
//                    getRemark = res3.getString(6);
//                }
//            } // to close the onItemSelected
//
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });

        unitcodebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isclick = "scan unit code";
                SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                editor.putString("isclickEmployee_Details", isclick);
                editor.apply();

                new IntentIntegrator(Employee_Details.this).initiateScan();


            }

        });

        employeecodebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    type = "punchin";
                    String isOnline = "";
                    ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

                    NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
                    if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
                        isOnline = "0";
                    } else {
                        isOnline = "1";
                    }
                    if (isOnline == "0") {
                        isclick = "employee button";
                        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                        editor.putString("isclickEmployee_Details", isclick);
                        editor.apply();
//                new IntentIntegrator(Employee_Details.this).initiateScan();

                        /** For validation of QR CODE*/

                        if(selectedRadio.contains("Duty")){
                            if(unitname.isEmpty()){
                                Toast.makeText(Employee_Details.this, "SCAN UNIT QR CODE", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }

                        /** For the OD punchin */

                        if(siteunit.getText().toString().contains("OD")){

                            CompGroupID = "2";
                            UnitLongitude = prefs.getString("UnitLongitude", "77.08");
                            UnitLatitude = prefs.getString("UnitLatitude", "29.00");
                            Unitcode = "43087";
                            Compid = "6";
                        }
                        else{
                            CompGroupID = prefs.getString("companyGroudId", "");
                            UnitLongitude = prefs.getString("UnitLongitude", "");
                            UnitLatitude = prefs.getString("UnitLatitude", "");
                            Unitcode = prefs.getString("UnitCode", "");
                            Compid = prefs.getString("companyId", "");
                        }


                        databaseConnection.deleteAttendanceDetail();
                        databaseConnection.insertAttendanceDetail(CompGroupID, Compid, Unitcode, UnitLongitude, UnitLatitude, employeeid.getText().toString(), CreatedByUserID);

                        Intent i = new Intent(getApplicationContext(), IdentificationActivityModified.class);
                        i.putExtra("employeecode", employeeid.getText().toString());
                        i.putExtra("selectedRadio", selectedRadio);
                        i.putExtra("type", "PunchIn");
                        i.putExtra("userName", Empname);
                        if(siteunit.getText().toString().contains("OD")) {
                            i.putExtra("siteName", selectedRadio);
                        }
                        else{
                            i.putExtra("siteName", unitname);
                        }
                      //  i.putExtra("designationName", selectedItem);
                        startActivity(i);
                        finish();
                    } else {
                        /*if (!sharedPrefHelper.getPunchInStatus()) {*/
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(Employee_Details.this);
                        builder1.setTitle("Alert");
                        builder1.setMessage("Punch In marked in offline mode?/ऑफ़लाइन मोड में चिह्नित पंच आउट करें?");
                        builder1.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String currentDateAndTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
                                String id = Empcode + currentDateAndTime;
                                CompGroupID = prefs.getString("companyGroudId", "");
                                UnitLongitude = prefs.getString("UnitLongitude", "");
                                UnitLatitude = prefs.getString("UnitLatitude", "");
                                Unitcode = prefs.getString("UnitCode", "");
                                Compid = prefs.getString("companyId", "");

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    if (checkSelfPermission(Manifest.permission.CAMERA)
                                            != PackageManager.PERMISSION_GRANTED) {
                                        requestPermissions(new String[]{Manifest.permission.CAMERA},
                                                MY_CAMERA_PERMISSION_CODE);
                                    } else {
                                        Intent intent = new Intent(Employee_Details.this, MainActivityPhotoBlink.class);
                                        startActivityForResult(intent, CAMERA_REQUEST);
                                        /*Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                        cameraIntent.putExtra("android.intent.extras.CAMERA_FACING", android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT);
                                        cameraIntent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1);
                                        cameraIntent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true);
                                        startActivityForResult(cameraIntent, CAMERA_REQUEST);*/
                                    }
                                } else {
                                    Intent intent = new Intent(Employee_Details.this, MainActivityPhotoBlink.class);
                                    startActivityForResult(intent, CAMERA_REQUEST);
                                    /*Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                    cameraIntent.putExtra("android.intent.extras.CAMERA_FACING", android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT);
                                    cameraIntent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1);
                                    cameraIntent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true);
                                    startActivityForResult(cameraIntent, CAMERA_REQUEST);*/
                                }

                            }
                        });
                        builder1.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                                dialog.cancel();

                            }
                        });
                        builder1.show();
                        /*} else {
                            Toast.makeText(Employee_Details.this, "Already punched in for today.", Toast.LENGTH_SHORT).show();
                        }*/
                    }
                }
                  //  AlertDialogs.show(Employee_Details.this, getResources().getString(R.string.select_designation_error));
        });


        punchOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                if (selectedSpinnerItemPosition == 0) {

                    type = "punchout";
                    String isOnline = "";
                    ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

                    NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
                    if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
                        isOnline = "0";
                    } else {
                        isOnline = "1";
                    }
                    if (isOnline == "0") {
                        isclick = "employee button";
                        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                        editor.putString("isclickEmployee_Details", isclick);
                        editor.apply();
//                new IntentIntegrator(Employee_Details.this).initiateScan();

                        /** For validation of QR CODE*/

                        if(selectedRadio.contains("Duty")){
                            if(unitname.isEmpty()){
                                Toast.makeText(Employee_Details.this, "SCAN UNIT QR CODE", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }

                        /** For the OD punchout */

                        if(siteunit.getText().toString().contains("OD")){

                            CompGroupID = "2";
                            UnitLongitude = prefs.getString("UnitLongitude", "77.08");
                            UnitLatitude = prefs.getString("UnitLatitude", "29.00");
                            Unitcode = "43087";
                            Compid = "6";
                        }
                        else{
                            CompGroupID = prefs.getString("companyGroudId", "");
                            UnitLongitude = prefs.getString("UnitLongitude", "");
                            UnitLatitude = prefs.getString("UnitLatitude", "");
                            Unitcode = prefs.getString("UnitCode", "");
                            Compid = prefs.getString("companyId", "");
                        }
                       /* CompGroupID = prefs.getString("companyGroudId", "");
                        UnitLongitude = prefs.getString("UnitLongitude", "");
                        UnitLatitude = prefs.getString("UnitLatitude", "");
                        Unitcode = prefs.getString("UnitCode", "");
                        Compid = prefs.getString("companyId", "");*/
                        databaseConnection.deleteAttendanceDetail();
                        databaseConnection.insertAttendanceDetail(CompGroupID, Compid, Unitcode, UnitLongitude, UnitLatitude, employeeid.getText().toString(),
                                //Desicode,
                                CreatedByUserID);

                        Intent i = new Intent(getApplicationContext(), IdentificationActivityModified.class);
                        i.putExtra("employeecode", employeeid.getText().toString());
                        i.putExtra("selectedRadio", selectedRadio);
                        i.putExtra("type", "Punch Out");
                        i.putExtra("userName", Empname);
                        if(siteunit.getText().toString().contains("OD")) {
                            i.putExtra("siteName", selectedRadio);
                        }
                        else{
                            i.putExtra("siteName", unitname);
                        }
//                        i.putExtra("designationName", selectedItem);
                        startActivity(i);
                        finish();
                    } else {
                        /*if (!sharedPrefHelper.getPunchOutStatus()) {*/
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(Employee_Details.this);
                        builder1.setTitle("Alert");
                        builder1.setMessage("Punch Out marked in offline mode?/ऑफ़लाइन मोड में चिह्नित पंच आउट करें?");
                        builder1.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    if (checkSelfPermission(Manifest.permission.CAMERA)
                                            != PackageManager.PERMISSION_GRANTED) {
                                        requestPermissions(new String[]{Manifest.permission.CAMERA},
                                                MY_CAMERA_PERMISSION_CODE);
                                    } else {
                                        Intent intent = new Intent(Employee_Details.this, MainActivityPhotoBlink.class);
                                        startActivityForResult(intent, CAMERA_REQUEST);
                                        /*Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                        cameraIntent.putExtra("android.intent.extras.CAMERA_FACING", android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT);
                                        cameraIntent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1);
                                        cameraIntent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true);
                                        startActivityForResult(cameraIntent, CAMERA_REQUEST);*/
                                    }
                                } else {
                                    Intent intent = new Intent(Employee_Details.this, MainActivityPhotoBlink.class);
                                    startActivityForResult(intent, CAMERA_REQUEST);
                                    /*Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                    cameraIntent.putExtra("android.intent.extras.CAMERA_FACING", android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT);
                                    cameraIntent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1);
                                    cameraIntent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true);
                                    startActivityForResult(cameraIntent, CAMERA_REQUEST);*/
                                }

                            }
                        });
                        builder1.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                                dialog.cancel();

                            }
                        });
                        builder1.show();
                        /*} else {
                            Toast.makeText(Employee_Details.this, "Already Punched out for today.", Toast.LENGTH_SHORT).show();
                        }*/
                    }

//                sharedPrefHelper.setLastPunchOutTime(System.currentTimeMillis());
//                    if(punchOut.isEnabled()){
//                        punchOut.invalidate();
//                        employeecodebutton.isEnabled();
//
//                    }

                }
                //else {
                    //AlertDialogs.show(Employee_Details.this, getResources().getString(R.string.select_designation_error));
                //}
            //}

        });

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                // The next two lines tell the new client that “this” current class will handle connection stuff
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                //fourth line adds the LocationServices API endpoint from GooglePlayServices
                .addApi(LocationServices.API)
                .build();
        radioButtonSelectWhenScreenOpens();
        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(1 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @SuppressLint("ResourceType")
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                rb = (RadioButton) group.findViewById(checkedId);
                if (null != rb && checkedId > -1) {
                    selectedRadio = rb.getText().toString();
                    if(selectedRadio.contains("OD")) {
                        siteunit.setText(selectedRadio);
                        unitcodebutton.setVisibility(View.GONE);
                    }
                    else{
                        unitcodebutton.setVisibility(View.VISIBLE);
                        siteunit.setText("");
                    }

                }

            }
        });

        clearCache.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPrefHelper.clearAll();
                ((ActivityManager) getSystemService(ACTIVITY_SERVICE)).clearApplicationUserData();
                startActivity(new Intent(Employee_Details.this, SplashActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            }
        });
    }

    private void showInstructions() {
        /*if (infoTextView == null) {
            infoTextView = findViewById(R.id.infoTextView);
            infoTextView.setSelected(true);
            infoTextView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            infoTextView.setSingleLine(true);
        }
        if (sharedPrefHelper.getPunchInStatus()) {
            // Punched In for today
            infoTextView.setText("You Have punched in for today. आपने आज के लिए पंच किया है। ");
            if (sharedPrefHelper.getPunchOutStatus()) {
                // punched Out for today
                infoTextView.setText("Your attendance is complete for today. आपकी उपस्थिति आज के लिए पूरी हो गई है। ");
            }
        } else {
            // Not punched in today
            infoTextView.setText(String.format("Hello %s, no attendance marked today. नमस्कार %s, आज कोई उपस्थिति दर्ज नहीं की गई है। ", employeeName, employeeName));
        }

        if (sharedPrefHelper.getPendingOfflineSync()) {
            if (infoTextView.getText().toString().trim().isEmpty()) {
                infoTextView.setText("You have offline punches waiting to be synced. आपका ऑफ़लाइन पंच सिंक किए जाने की प्रतीक्षा कर रहे हैं। ");
            } else {
                infoTextView.append("You have offline punches waiting to be synced. आपका ऑफ़लाइन पंच सिंक किए जाने की प्रतीक्षा कर रहे हैं। ");
            }
        }*/
    }

    private void radioButtonSelectWhenScreenOpens() {
        int idWhenScreenOpens = radioButton.getId();
        radioGroup.check(idWhenScreenOpens);
        selectedRadio = "Duty";
    }


    private void employeeInfoCount() {
        final Cursor res = databaseConnection.getEmployeeInfoCount();
        int numRows = res.getCount();
        if (numRows > 0) {
            res.moveToFirst();
            employeeName = res.getString(0);
            this.Empname = employeeName;
            String EmpFHName = res.getString(1);
            String CompName = res.getString(2);
            this.CompName = CompName;
            String EmployeeInfo = res.getString(3);
            name.setText(employeeName);
            fathersname.setText(EmpFHName);
            companyname.setText(CompName);
            employeeid.setText(EmployeeInfo);
        }
    }

    private void setTitles() {
        if (language.equals("English")) {
            fathersnameTitle.setText("  Fathers name  ");
            employeeidTitle.setText("  Employee ID  ");
            companynameTitle.setText("  Company Name  ");
            siteunitTitle.setText("  Site/Unit  ");
            unitcodebutton.setText("Scan Unit QR code");
            employeecodebutton.setText("Punch In");

        } else {
            fathersnameTitle.setText("  पिता का नाम  ");
            employeeidTitle.setText("  कर्मचारी आयडी  ");
            companynameTitle.setText("  कंपनी का नाम  ");
            siteunitTitle.setText("  साइट / यूनिट  ");
            unitcodebutton.setText("स्कैन यूनिट क्यूआर कोड");
            employeecodebutton.setText("स्कैन कर्मचारी क्यूआर कोड");
            nameTitle.setText("  नाम  ");

        }
    }

    private void setLanguage() {
        prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        //"No name defined" is the default value.
        language = prefs.getString("lang", "English");
    }

    private void setUnitname() {
        unitname = prefs.getString("UnitName", "");
        siteunit.setText(unitname);
    }





    private void getId() {
        sharedPrefHelper = new SharedPrefHelper();
        name = findViewById(R.id.name);
        fathersname = findViewById(R.id.fathersname);
        employeeid = findViewById(R.id.employeeid);
        companyname = findViewById(R.id.companyname);
        siteunit = findViewById(R.id.siteunit);
//        spinner = findViewById(R.id.spinner);
        fathersnameTitle = findViewById(R.id.fathersnameTitle);
        employeeidTitle = findViewById(R.id.employeeidTitle);
        onlineOfflineText = findViewById(R.id.onlineOfflineText);
        companynameTitle = findViewById(R.id.companynameTitle);
        siteunitTitle = findViewById(R.id.siteunitTitle);
        nameTitle = findViewById(R.id.nameTitle);
        unitcodebutton = findViewById(R.id.unitcodebutton);
        employeecodebutton = findViewById(R.id.employeecodebutton);
        punchOut = findViewById(R.id.punchOut);
        clearCache = findViewById(R.id.clearCache);
        radioButton = findViewById(R.id.radioButton1);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        radioGroup.clearCheck();

        // checkInternet = SampleApp.getCheckInternet();
        checkInternet = new CheckInternet(Employee_Details.this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main2, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            finishAffinity();
        }


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Signature/Up button, so long
        // as you specify strenghtCheckUpArrow parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_example) {
            int numRows = 0;
            databaseConnection = new DatabaseConnection(getApplicationContext());
            Cursor res = databaseConnection.getEmployeeInfoCount();
            numRows = res.getCount();
            res.moveToFirst();

            if (numRows > 0) {
                Empcode = res.getString(3);
                Intent i = new Intent(getApplicationContext(), AttendanceReport.class);
                i.putExtra("Empcode", Empcode);
                i.putExtra("Compid", "1");
                startActivity(i);
            } else {
                Toast.makeText(this, "No Employee Code/कोई कर्मचारी कोड नहीं", Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (id == R.id.setting) {
            Intent i = new Intent(getApplicationContext(), Setting.class);

            startActivity(i);
            return true;
        } else if (id == R.id.sync) {
            Intent i = new Intent(getApplicationContext(), SyncList.class);

            startActivity(i);

        } else if (id == R.id.play_tutorial) {
            Intent i = new Intent(getApplicationContext(), Video_Play.class);
            startActivity(i);
            return true;
        } /*else if (id == R.id.weeklyOff) {
            Intent i = new Intent(getApplicationContext(), WeeklyOffActivity.class);
            startActivity(i);
            return true;
        } else if (id == R.id.leave) {
            Intent i = new Intent(getApplicationContext(), IdentificationMarkLeaveActivity.class);
            i.putExtra("name", name.getText().toString());
            i.putExtra("empcode", employeeid.getText().toString());
            startActivity(i);
            return true;
        }*/ else if (id == R.id.sos) {
            Intent i = new Intent(getApplicationContext(), SosActivity.class);
            i.putExtra("name", name.getText().toString());
            i.putExtra("empcode", employeeid.getText().toString());
            startActivity(i);
            return true;
        } else if (id == R.id.clearCache) {
            sharedPrefHelper.clearAll();
            ((ActivityManager) getSystemService(ACTIVITY_SERVICE)).clearApplicationUserData();
            startActivity(new Intent(Employee_Details.this, SplashActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        isclick = prefs.getString("isclickEmployee_Details", "");
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {

            //  Bitmap photo = (Bitmap) data.getExtras().get("data");
            Bitmap photo = MainActivityPhotoBlink.bitmap;
            if (photo != null) {
                int resolution = 600;

                // Actual Height and Width of image taken
                int Height = photo.getHeight();
                int Width = photo.getWidth();

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
                Bitmap resizedBitmap = Bitmap.createBitmap(photo, 0, 0, Width, Height, matrix, true);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                resizedBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] b = baos.toByteArray();
                imageStr = Base64.encodeToString(b, Base64.DEFAULT);
                String currentDateAndTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
                String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                CompGroupID = prefs.getString("companyGroudId", "");
                UnitLongitude = prefs.getString("UnitLongitude", "");
                UnitLatitude = prefs.getString("UnitLatitude", "");
                Unitcode = prefs.getString("UnitCode", "");
                Empcode = employeeid.getText().toString();
                String id = Empcode + currentDateAndTime;

                if (type == "punchin") {
                    databaseConnection.insertPunchIn(id, CompGroupID, Compid, Unitcode, UnitLongitude, UnitLongitude, Empcode,
                            //Desicode,
                            Double.toString(currentLatitude), Double.toString(currentLongitude), currentDateAndTime, getDeviceIMEI(), imageStr, CreatedByUserID, "1", selectedRadio, "false", ml_id);
                    sharedPrefHelper.setPunchInStatus(1);
                    sharedPrefHelper.setPunchOutStatus(2);
                    sharedPrefHelper.setLastPunchInDate(currentDate);
                  //  sharedPrefHelper.setCurrentDesignationName(Desicode);
                } else {
                    databaseConnection.insertPunchOut(id, CompGroupID, Compid, Unitcode, UnitLongitude, UnitLongitude, Empcode,
                            //Desicode,
                            Double.toString(currentLatitude), Double.toString(currentLongitude), currentDateAndTime, getDeviceIMEI(), imageStr, CreatedByUserID, "1", selectedRadio, "false", ml_id);
                    sharedPrefHelper.setPunchInStatus(2);
                    sharedPrefHelper.setPunchOutStatus(1);
                    sharedPrefHelper.setLastPunchInDate("0.0");
                    //sharedPrefHelper.setCurrentDesignationName("null");
                }

            } else {

                imageStr = "Redmiphone6error";
                String currentDateAndTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
                String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                CompGroupID = prefs.getString("companyGroudId", "");
                UnitLongitude = prefs.getString("UnitLongitude", "");
                UnitLatitude = prefs.getString("UnitLatitude", "");
                Unitcode = prefs.getString("UnitCode", "");
                String id = Empcode + currentDateAndTime;
                if (type == "punchin") {
                    databaseConnection.insertPunchIn(id, CompGroupID, Compid, Unitcode, UnitLongitude, UnitLongitude, Empcode,
                            //Desicode,
                            Double.toString(currentLatitude), Double.toString(currentLongitude), currentDateAndTime, getDeviceIMEI(), imageStr, CreatedByUserID, "1", selectedRadio, "false", ml_id);
                    sharedPrefHelper.setPunchInStatus(1);
                    sharedPrefHelper.setPunchOutStatus(2);
                    //sharedPrefHelper.setCurrentDesignationName(Desicode);
                    sharedPrefHelper.setLastPunchInDate(currentDate);
                } else {
                    sharedPrefHelper.setPunchInStatus(2);
                    sharedPrefHelper.setPunchOutStatus(1);
                    sharedPrefHelper.setLastPunchInDate("0.0");
                  //  sharedPrefHelper.setCurrentDesignationName("null");
                    databaseConnection.insertPunchOut(id, CompGroupID, Compid, Unitcode, UnitLongitude, UnitLongitude, Empcode,
                            //Desicode,
                            Double.toString(currentLatitude), Double.toString(currentLongitude), currentDateAndTime, getDeviceIMEI(), imageStr, CreatedByUserID, "1", selectedRadio, "false", ml_id);

                }
            }
            handlePunchStatus();

            //TaskHelper.checkPunchStatus();
            //TaskHelper.checkOfflinePunchSync();
            //showInstructions();
        }
        if (requestCode != CUSTOMIZED_REQUEST_CODE && requestCode != IntentIntegrator.REQUEST_CODE) {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);

            return;
        }

        switch (requestCode) {
            case CUSTOMIZED_REQUEST_CODE: {
                Toast.makeText(this, "REQUEST_CODE = " + requestCode, Toast.LENGTH_LONG).show();
                break;
            }
            default:
                break;
        }

        IntentResult result = IntentIntegrator.parseActivityResult(resultCode, data);

        if (result.getContents() == null) {
            Log.d("MainActivity", "Cancelled scan");
            Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
        } else if (isclick.contains("scan unit code")) {
            JSONObject json = null;
            //databaseConnection.deleteUserDesignation();
            try {
                json = new JSONObject(result.getContents());
                Log.e("QR_SCAN_DETAILS", "onActivityResult: " + json.toString());
                String companyGroudId = json.getString("CompGroupID");
                final String UnitCode = json.getString("UnitCode");
                String companyId = json.getString("CompID");
                final String UnitName = json.getString("UnitName");
                final String UnitLatitude = json.getString("UnitLatitude");
                final String UnitLongitude = json.getString("UnitLongitude");
                SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                editor.putString("UnitName", UnitName);
                editor.putString("UnitCode", UnitCode);
                editor.putString("UnitLatitude", UnitLatitude);
                editor.putString("UnitLongitude", UnitLongitude);
                editor.putString("companyId", companyId);
                editor.putString("companyGroudId", companyGroudId);
                editor.apply();

                if (internetConnectionAvailable(2000)) {
                    ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);

//                    Call<List<UserDesignation>> call = apiService.GetUserDesignationList(UnitCode, companyGroudId, companyId);
//                    call.enqueue(new Callback<List<UserDesignation>>() {
//                                     @Override
//                                     public void onResponse(Call<List<UserDesignation>> call, Response<List<UserDesignation>> response) {
//
//
//                                         listData = (response.body());
//                                         DatabaseConnection databaseConnection = new DatabaseConnection(getApplicationContext());
//                                         databaseConnection.deleteUserDesignation();
//                                         Log.e("Eiraj", "Api working" + response.body());
//                                         if (listData != null && listData.size() > 0) {
//                                             for (int i = 0; i < listData.size(); i++) {
//
//                                                 databaseConnection.insertUserDesignatione(listData.get(i));
//                                             }
//                                             setUnitname();
//
//                                         } else {
//                                             Log.e("Eiraj", "No data coming employee details 374");
//                                         }
//
//                                         //sharedPrefHelper.setPunchLatLng(LATITUDE + "," + LONGITUDE);
//                                     }
//
//                                     @Override
//                                     public void onFailure(Call<List<UserDesignation>> call, Throwable t) {
//                                         Log.e("Eiraj", "Api not working");
//
//                                     }
//                                 }
//                    );


                } else {
                    //databaseConnection.deleteUserDesignation();
                    AlertDialog.Builder builder = new AlertDialog.Builder(Employee_Details.this, R.style.MyAlertDialogTheme);
                    builder.setTitle("Alert");
                    builder.setMessage("We are unable to get unit code your attendance will be marked in offline mode/\n" +
                            "हम यूनिट कोड प्राप्त करने में असमर्थ हैं आपकी उपस्थिति ऑफलाइन मोड में चिह्नित की जाएगी");
                    builder.setPositiveButton("Ok/अनुदान", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    builder.show();

                }
                setUnitname();

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(Employee_Details.this, "Please check your QR code/कृपया अपने क्यूआर कोड की जांच करें", Toast.LENGTH_SHORT).show();
            }


        } else if (isclick == "employee button") {
            JSONObject json = null;
            try {
                json = new JSONObject(result.getContents());
                Compid = json.getString("Compid");
                Empcode = json.getString("Empcode");
                Empname = json.getString("Empname");
                EmpFHName = json.getString("EmpFHName");
                String CreatedByUserID = json.getString("CreatedByUserID");
                CompName = json.getString("CompName");

                SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
                SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                editor.putString("companyId", Compid);
                editor.apply();
                CompGroupID = prefs.getString("companyGroudId", "");
                UnitLongitude = prefs.getString("UnitLongitude", "");
                UnitLatitude = prefs.getString("UnitLatitude", "");
                Unitcode = prefs.getString("UnitCode", "");
                final DatabaseConnection databaseConnection = new DatabaseConnection(Employee_Details.this);
                // new PostVersion(Employee_Details.this,Empcode).execute();
                if (Empcode.equals(employeeid.getText().toString())) {
                    databaseConnection.deleteAttendanceDetail();
                    databaseConnection.insertAttendanceDetail(CompGroupID, Compid, Unitcode, UnitLongitude, UnitLatitude, Empcode,
                            //Desicode,
                            CreatedByUserID);
                    Intent i = new Intent(getApplicationContext(), MarkAttendance.class);

                    startActivity(i);


                } else {
                    final String finalCompGroupID = CompGroupID;
                   // final String finalDesiCode = Desicode;
                    final String finalCreatedByUserID = CreatedByUserID;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder builder = new AlertDialog.Builder(Employee_Details.this, R.style.MyAlertDialogTheme);
                            builder.setTitle("Confirm");
                            builder.setMessage("Please check the Employee details" +

                                    "\n" +

                                    "Name/नाम: " + Empname + "\n" +
                                    "Father Name/पिता का नाम: " + EmpFHName + "\n" +
                                    "Employee Code/कर्मचारी कोड: " + Empcode + "\n" +
                                    "Company Name/कंपनी का नाम: " + CompName + "\n");
                            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    databaseConnection.deleteAttendanceDetail();
                                    if (internetConnectionAvailable(2000)) {


                                        databaseConnection.insertAttendanceDetail(finalCompGroupID, Compid, Unitcode, UnitLongitude, UnitLatitude, Empcode,
                                                //finalDesiCode,
                                                finalCreatedByUserID);
//                                        Intent i = new Intent(getApplicationContext(), MarkAttendance.class);
//
//                                        startActivity(i);
                                    } else {
                                        Toast.makeText(Employee_Details.this, R.string.msg_alert_no_internet, Toast.LENGTH_SHORT).show();
                                        databaseConnection.insertAttendanceDetail(finalCompGroupID, Compid, Unitcode, UnitLongitude, UnitLatitude, Empcode,
                                                //finalDesiCode,
                                                finalCreatedByUserID);
//                                        Intent i = new Intent(getApplicationContext(), MarkAttendance.class);
//
//                                        startActivity(i);
                                    }
                                }
                            });
                            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    finish();
                                    dialog.cancel();
                                }
                            });
                            Rect displayRectangle = new Rect();
                            Window window = getWindow();

                            window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
                            builder.show();
                        }
                    });

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {

        }
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
        LocationManager lm = (LocationManager) Employee_Details.this.getSystemService(Context.LOCATION_SERVICE);
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
            AlertDialog.Builder dialog = new AlertDialog.Builder(Employee_Details.this);
            dialog.setMessage("Gps is not enabled/जीपीएस सक्षम नहीं है");
            dialog.setPositiveButton("Open Setting/सेटिंग खोलें", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    Employee_Details.this.startActivity(myIntent);
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

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
        handlePunchStatus();
        if (onlineOfflineText != null) {
            checkInternet.viewChangesOn(onlineOfflineText);
        }
        //TaskHelper.checkPunchStatus();
        //TaskHelper.checkOfflinePunchSync();
        //showInstructions();
        //Log.e("XXX", "onCreate: " + sharedPrefHelper.getPunchInStatus() + sharedPrefHelper.getPunchOutStatus() );
    }

    private void handlePunchStatus() {

        if (sharedPrefHelper != null) {
            String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            if(sharedPrefHelper.getLastPunchInDate().contains(currentDate) && sharedPrefHelper.getPunchInStatus() == 1 && sharedPrefHelper.getPunchOutStatus() == 2 ){
                sharedPrefHelper.setPunchInStatus(1);
               sharedPrefHelper.setPunchOutStatus(2);
            }
            else if(sharedPrefHelper.getLastPunchInDate().contains("0.0") && sharedPrefHelper.getPunchInStatus() == 2 && sharedPrefHelper.getPunchOutStatus() == 1 ){
                sharedPrefHelper.setPunchInStatus(2);
                sharedPrefHelper.setPunchOutStatus(1);
            }
            else{
                sharedPrefHelper.setPunchInStatus(2);
                sharedPrefHelper.setPunchOutStatus(1);
                sharedPrefHelper.setLastPunchInDate(currentDate);
            }


            Log.e("PUNCH_STATUS", sharedPrefHelper.getPunchOutStatus() + " / " + sharedPrefHelper.getPunchInStatus());

            switch (sharedPrefHelper.getPunchInStatus()) {
                case 0:
                    employeecodebutton.setEnabled(true);
                    employeecodebutton.setBackgroundResource(R.drawable.curved_green_background);
                    employeecodebutton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.green)));
                    break;
                case 1:
                    employeecodebutton.setEnabled(false);
                    employeecodebutton.setBackground(ContextCompat.getDrawable(Employee_Details.this, R.drawable.curved_grey_background_solid));
                    employeecodebutton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.grey)));
                    break;
                case 2:
                    employeecodebutton.setEnabled(true);
                    employeecodebutton.setBackgroundResource(R.drawable.curved_green_background);
                    employeecodebutton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.green)));
                    break;
            }

            switch (sharedPrefHelper.getPunchOutStatus()) {
                case 0:
                    punchOut.setEnabled(true);
                    punchOut.setBackground(ContextCompat.getDrawable(Employee_Details.this, R.drawable.curved_primary_background));
                    break;
                case 1:
                    punchOut.setEnabled(false);
                    punchOut.setBackground(ContextCompat.getDrawable(Employee_Details.this, R.drawable.curved_grey_background_solid));
                    break;
                case 2:
                    punchOut.setEnabled(true);
                    punchOut.setBackground(ContextCompat.getDrawable(Employee_Details.this, R.drawable.curved_primary_background));
                    break;
            }

        } else {
            SharedPrefHelper sharedPrefHelper = new SharedPrefHelper();



            String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            if(sharedPrefHelper.getLastPunchInDate().contains(currentDate) && sharedPrefHelper.getPunchInStatus() == 1 && sharedPrefHelper.getPunchOutStatus() == 2 ){
                sharedPrefHelper.setPunchInStatus(1);
                sharedPrefHelper.setPunchOutStatus(2);
            }
            else if(sharedPrefHelper.getLastPunchInDate().contains("0.0") && sharedPrefHelper.getPunchInStatus() == 2 && sharedPrefHelper.getPunchOutStatus() == 1 ){
                sharedPrefHelper.setPunchInStatus(2);
                sharedPrefHelper.setPunchOutStatus(1);
            }
            else{
                sharedPrefHelper.setPunchInStatus(2);
                sharedPrefHelper.setPunchOutStatus(1);
                // sharedPrefHelper.setLastPunchInDate(currentDate);
            }

            Log.e("PUNCH_STATUS", sharedPrefHelper.getPunchOutStatus() + " / " + sharedPrefHelper.getPunchInStatus());
            switch (sharedPrefHelper.getPunchInStatus()) {
                case 0:
                    employeecodebutton.setEnabled(true);
                    employeecodebutton.setBackgroundResource(R.drawable.curved_green_background);
                    employeecodebutton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.green)));
                    break;
                case 1:
                    employeecodebutton.setEnabled(false);
                    employeecodebutton.setBackground(ContextCompat.getDrawable(Employee_Details.this, R.drawable.curved_grey_background_solid));
                    employeecodebutton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.grey)));
                    break;
                case 2:
                    employeecodebutton.setEnabled(true);
                    employeecodebutton.setBackgroundResource(R.drawable.curved_green_background);
                    employeecodebutton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.green)));
                    break;
            }

            switch (sharedPrefHelper.getPunchOutStatus()) {
                case 0:
                    punchOut.setEnabled(true);
                    punchOut.setBackground(ContextCompat.getDrawable(Employee_Details.this, R.drawable.curved_primary_background));
                    break;
                case 1:
                    punchOut.setEnabled(false);
                    punchOut.setBackground(ContextCompat.getDrawable(Employee_Details.this, R.drawable.curved_grey_background_solid));
                    break;
                case 2:
                    punchOut.setEnabled(true);
                    punchOut.setBackground(ContextCompat.getDrawable(Employee_Details.this, R.drawable.curved_primary_background));
                    break;
            }
        }
    }

    @Override
    protected void onPause() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }

        checkInternet.removeChanges();
        super.onPause();
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
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            currentLatitude = location.getLatitude();
            currentLongitude = location.getLongitude();

            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(this, Locale.getDefault());

            try {
                addresses = geocoder.getFromLocation(currentLatitude, currentLongitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                if (addresses.size() >= 1) {
                    address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                    String city = addresses.get(0).getLocality();
                    String state = addresses.get(0).getAdminArea();
                    String country = addresses.get(0).getCountryName();
                    String postalCode = addresses.get(0).getPostalCode();
                    String knownName = addresses.get(0).getFeatureName();
                } else {
                    address = String.valueOf(currentLatitude) + "," + String.valueOf(currentLongitude);
                }
                // addressTextView.setText(address);
            } catch (IOException e) {
                e.printStackTrace();
                // addressTextView.setText(currentLatitude+","+currentLongitude);
            }


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
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(currentLatitude, currentLongitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            if (addresses.size() >= 1) {
                address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();
                String postalCode = addresses.get(0).getPostalCode();
                String knownName = addresses.get(0).getFeatureName();
            } else {
                address = String.valueOf(currentLatitude) + "," + String.valueOf(currentLongitude);
            }
            //   addressTextView.setText(address);
        } catch (IOException e) {
            // addressTextView.setText(currentLatitude + "," + currentLongitude);
            e.printStackTrace();

        }

        //  Toast.makeText(this, currentLatitude + " WORKS " + currentLongitude + "", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(REQUEST_LOCATION_PERMISSION)
    public void requestLocationPermission() {
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};
        if (EasyPermissions.hasPermissions(this, perms)) {
            //Toast.makeText(this, "Permission already granted", Toast.LENGTH_SHORT).show();
        } else {
            EasyPermissions.requestPermissions(this, "Please grant the location permission", REQUEST_LOCATION_PERMISSION, perms);
        }
    }

    @SuppressLint("MissingPermission")
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

    private void getDesiredRadiusForLiveTrack() {
        Call<ResponseBody> getLiveTrackRadius = apiInterface.getLiveTrackRadius();
        getLiveTrackRadius.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        int radius = Integer.parseInt(response.body().string());
                        sharedPrefHelper.setLiveTrackRadius(radius);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    private void updateFCMToken() {
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String newToken = instanceIdResult.getToken();

                SaveFCMTokenModel model = new SaveFCMTokenModel();
                model.setFCMTokenId(newToken);
                model.setEmpCode(sharedPrefHelper.getEmployeeCode());
                model.setCompId(Integer.parseInt(sharedPrefHelper.getCompanyID()));

                Call<ResponseBody> saveFCMToken = apiInterface.saveFCMToken(model);
                saveFCMToken.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                    }
                });
            }
        });


    }
}

