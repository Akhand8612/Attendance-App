package com.eiraj.intel.drone.activities.EmployeeDetail;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.eiraj.intel.drone.Database.DatabaseConnection;
import com.eiraj.intel.drone.R;
//import com.eiraj.intel.drone.model.UserDesignation;
import com.eiraj.intel.drone.rest.ApiClient;
import com.eiraj.intel.drone.rest.ApiInterface;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.zxing.integration.android.IntentIntegrator;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dmax.dialog.SpotsDialog;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.eiraj.intel.drone.activities.EmployeeDetail.MarkAttendance.isTimeAutomatic;
import static com.eiraj.intel.drone.Utils.Util.MY_PREFS_NAME;
import static com.eiraj.intel.drone.Utils.Util.internetConnectionAvailable;
import static com.eiraj.intel.drone.rest.ApiClient.POST_APPLY_LEAVE;

public class LeaveActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    // lists for permissions
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    public final int CUSTOMIZED_REQUEST_CODE = 0x0000ffff;
    private final int REQUEST_READ_PHONE_STATE = 1;
    private final int REQUEST_LOCATION_PERMISSION = 1;
    public android.app.AlertDialog waitingDialog;
    TextView name, fathersname, employeeid, companyname, siteunit, //designation
            fathersnameTitle, companynameTitle, employeeidTitle, siteunitTitle,
            //designationTitle,
            nameTitle;
   // Spinner spinner;
    Button unitcodebutton, employeecodebutton, weeklyOffButtton;
    Button employeeCodeButton;
    String isclick = "";
    SharedPreferences prefs;
    String Compid = "", Empcode = "", Empname = "", EmpFHName = "", CompName = "";
   //String Desicode = "";
    String CompGroupID = "";
    DatabaseConnection databaseConnection;
    String getEmpcode = "";
    //Button unitButton;
    EditText unitCodeEditText;
    //List<UserDesignation> listData;
    String unitname = "";
    String language = "";
    String selectedItem = "";
    String currentDateAndTime = "";
    String unitCode;
    LinearLayout line1;
    String Address, latti, longi;
    /**
     * If locationChanges change lat and long
     *
     * @param location
     */
    String address = "";
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private double currentLatitude = 0.0;
    private double currentLongitude = 0.0;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (android.os.Build.VERSION.SDK_INT >= 27) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (android.os.Build.VERSION.SDK_INT >= 27) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weekly_off);
        checkPlayServices();
        if (android.os.Build.VERSION.SDK_INT >= 27) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }
        check();
        requestLocationPermission();
        getId();
        setLanguage();
        setUnitname();
        //setUserDesignationSpinner();
        employeeInfoCount();
        employeeCodeButton = findViewById(R.id.employeecodebutton);
        //unitButton = findViewById(R.id.unitButton);
        unitCodeEditText = findViewById(R.id.unitCodeEditText);

        employeeCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isclick = "employee button weekly";
                SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                editor.putString("isclickEmployee_Details", isclick);
                editor.apply();
                new IntentIntegrator(LeaveActivity.this).initiateScan();
            }
        });
//        unitButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//
//              //  databaseConnection.deleteUserDesignation();
//
//
//                if (internetConnectionAvailable(2000)) {
//                    ApiInterface apiService =
//                            ApiClient.getClient().create(ApiInterface.class);

//                    Call<List<UserDesignation>> call = apiService.GetUserDesignationList(unitCodeEditText.getText().toString(), CompGroupID, Compid, "1");
//                    call.enqueue(new Callback<List<UserDesignation>>() {
//                                     @Override
//                                     public void onResponse(Call<List<UserDesignation>> call, Response<List<UserDesignation>> response) {
//
//
//                                         listData = (response.body());
//                                         DatabaseConnection databaseConnection = new DatabaseConnection(getApplicationContext());
//                                         databaseConnection.deleteUserDesignation();
//                                         Toast.makeText(LeaveActivity.this, "you can select your  designation now", Toast.LENGTH_LONG).show();
//                                         Log.e("Eiraj", "Api working" + response.body());
//                                         if (listData != null && listData.size() > 0) {
//                                             for (int i = 0; i < listData.size(); i++) {
//
//                                                 databaseConnection.insertUserDesignatione(listData.get(i));
//                                             }
//                                             setUnitname();
//                                             setUserDesignationSpinner();
//                                         } else {
//                                             Toast.makeText(LeaveActivity.this, "Weekly off not available on this unit", Toast.LENGTH_LONG).show();
//                                             Log.e("Eiraj", "No data coming employee details 374");
//                                         }
//
//
//                                     }
//
//                                     @Override
//                                     public void onFailure(Call<List<UserDesignation>> call, Throwable t) {
//                                         Log.e("Eiraj", "Api not working");
//
//                                     }
//                                 }
//                    );


//                } else {
//                  //  databaseConnection.deleteUserDesignation();
//                    AlertDialog.Builder builder = new AlertDialog.Builder(LeaveActivity.this);
//                    builder.setTitle("Alert");
//                    builder.setMessage("We are unable to get unit code your attendance will be marked in offline mode/\n" +
//                            "हम यूनिट कोड प्राप्त करने में असमर्थ हैं आपकी उपस्थिति ऑफलाइन मोड में चिह्नित की जाएगी");
//                    builder.setPositiveButton("Ok/अनुदान", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            dialog.cancel();
//                        }
//                    });
//
//                    builder.show();
//
//
//                }
//                setUnitname();
//                //setUserDesignationSpinner();
//
//
//            }
//        });
//        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                selectedItem = parent.getItemAtPosition(position).toString();
//
//                Cursor res3 = databaseConnection.getUserDesignation(selectedItem);
//
//
//                int numRows3 = res3.getCount();
//                if (numRows3 > 0) {
//                    res3.moveToFirst();
//
//                    CompGroupID = res3.getString(3);
//                    Desicode = res3.getString(2);
//
//                    unitCode = res3.getString(6);
//                }
//            } // to close the onItemSelected
//
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
        weeklyOffButtton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Empcode == "" && Compid == "") {
                    Toast.makeText(LeaveActivity.this, "Please select employee code and enter unit code", Toast.LENGTH_LONG).show();


                } else if (unitCodeEditText.getText().toString() == "") {
                    Toast.makeText(LeaveActivity.this, "Please enter unit code", Toast.LENGTH_LONG).show();

                }
                //else if (CompGroupID == "" && Desicode == "") {
                  //  Toast.makeText(LeaveActivity.this, "Please select designation", Toast.LENGTH_LONG).show();
                //}
                else {
                    if (isTimeAutomatic(getApplicationContext())) {
                        postAttendanceIn();
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(LeaveActivity.this);
                        builder.setTitle("Need AutoMatic Time");
                        builder.setMessage("This App needs Automatic Android time to punch in.");
                        builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivityForResult(new Intent(Settings.ACTION_DATE_SETTINGS), 0);
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
                .setInterval(1 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


    }

    private void employeeInfoCount() {
        final Cursor res = databaseConnection.getEmployeeInfoCount();
        int numRows = res.getCount();
        if (numRows > 0) {
            res.moveToFirst();

            getEmpcode = res.getString(3);

        }
    }

    private void setUnitname() {
        unitname = prefs.getString("UnitName", "");
        siteunit.setText(unitname);
    }

//    private void setUserDesignationSpinner() {
//        Cursor res1 = databaseConnection.getUserDesignationSpinner();
//        int numRows1 = res1.getCount();
//        List<String> categories = new ArrayList<String>();
//        if (numRows1 == 0) {
//            spinner.setVisibility(View.VISIBLE);
//
//            categories.add("");
//            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);
//            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//
//            // attaching data adapter to spinner
//            spinner.setAdapter(dataAdapter);
//        } else {
//            while (res1.moveToNext()) {
//
//
//                categories.add(res1.getString(0));
//                ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);
//                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//
//
//                spinner.setAdapter(dataAdapter);
//
//            }
//        }
//    }

    private void setLanguage() {
        prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);

        language = prefs.getString("lang", null);
    }

    private void getId() {
        waitingDialog = new SpotsDialog.Builder()
                .setContext(LeaveActivity.this)
                .setMessage("Please Wait")
                .setCancelable(false)
                .build();
        prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        databaseConnection = new DatabaseConnection(getApplicationContext());
      // databaseConnection.deleteUserDesignation();
        name = findViewById(R.id.name);
        fathersname = findViewById(R.id.fathersname);
        employeeid = findViewById(R.id.employeeid);
        Intent intent = getIntent();
        Empcode = intent.getStringExtra("employeecode");
        CompGroupID = intent.getStringExtra("CompGroupID");
        Compid = intent.getStringExtra("Compid");
        Address = intent.getStringExtra("Address");
        latti = intent.getStringExtra("latti");
        longi = intent.getStringExtra(longi);
        employeeid.setText(intent.getStringExtra("employeecode"));
        companyname = findViewById(R.id.companyname);
        siteunit = findViewById(R.id.siteunit);
        //spinner = findViewById(R.id.spinner);
        fathersnameTitle = findViewById(R.id.fathersnameTitle);
        employeeidTitle = findViewById(R.id.employeeidTitle);
        companynameTitle = findViewById(R.id.companynameTitle);
        siteunitTitle = findViewById(R.id.siteunitTitle);
        //designationTitle = findViewById(R.id.designationTitle);
        nameTitle = findViewById(R.id.nameTitle);
        unitcodebutton = findViewById(R.id.unitcodebutton);
        employeecodebutton = findViewById(R.id.employeecodebutton);
        weeklyOffButtton = findViewById(R.id.weeklyOffButtton);
        line1 = findViewById(R.id.line1);
    }

    public void postAttendanceIn() {


        currentDateAndTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
        String isOnline = "";
        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            isOnline = "0";
        } else {
            isOnline = "1";
        }
        String id = Empcode + currentDateAndTime;


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

                if (isOnline == "0") {

                    new PostAttendenceIn(LeaveActivity.this).execute();
                } else {
                    Toast.makeText(getBaseContext(), "Check your internet", Toast.LENGTH_SHORT).show();


                }
            }


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
        LocationManager lm = (LocationManager) LeaveActivity.this.getSystemService(Context.LOCATION_SERVICE);
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
            AlertDialog.Builder dialog = new AlertDialog.Builder(LeaveActivity.this);
            dialog.setMessage("Gps is not enabled/जीपीएस सक्षम नहीं है");
            dialog.setPositiveButton("Open Setting/सेटिंग खोलें", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    LeaveActivity.this.startActivity(myIntent);
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
        //Now lets connect to the API
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {

        super.onPause();
        Log.v(this.getClass().getSimpleName(), "onPause()");
        if (android.os.Build.VERSION.SDK_INT >= 27) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }
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
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            currentLatitude = location.getLatitude();
            currentLongitude = location.getLongitude();

            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(this, Locale.getDefault());

            try {
                addresses = geocoder.getFromLocation(currentLatitude, currentLongitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();
                String postalCode = addresses.get(0).getPostalCode();
                String knownName = addresses.get(0).getFeatureName();
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
            address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName();
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
            Toast.makeText(this, "Permission already granted", Toast.LENGTH_SHORT).show();
        } else {
            EasyPermissions.requestPermissions(this, "Please grant the location permission", REQUEST_LOCATION_PERMISSION, perms);
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
            HttpClient httpClient = new DefaultHttpClient();
            HttpParams params = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(params, 5000);

            HttpPost httpPost = new HttpPost(POST_APPLY_LEAVE);


            List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();


            nameValuePair.add(new BasicNameValuePair("CompGroupID", CompGroupID));
            nameValuePair.add(new BasicNameValuePair("CompID", Compid));
            nameValuePair.add(new BasicNameValuePair("Unitcode", unitCode));

            nameValuePair.add(new BasicNameValuePair("Empcode", Empcode));
          //  nameValuePair.add(new BasicNameValuePair("Desicode", Desicode));
            nameValuePair.add(new BasicNameValuePair("DutyInLongitude", latti));
            // nameValuePair.add(new BasicNameValuePair("Address", Address));

            nameValuePair.add(new BasicNameValuePair("DutyInLatitude", longi));
            nameValuePair.add(new BasicNameValuePair("DutyINDate", currentDateAndTime));
            nameValuePair.add(new BasicNameValuePair("AttType", "L"));


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

}

