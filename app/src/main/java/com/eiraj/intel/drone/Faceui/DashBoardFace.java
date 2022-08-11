package com.eiraj.intel.drone.Faceui;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.eiraj.intel.drone.Database.DatabaseConnection;
import com.eiraj.intel.drone.R;
import com.eiraj.intel.drone.Utils.SharedPrefHelper;
import com.eiraj.intel.drone.Utils.Theme;
import com.eiraj.intel.drone.activities.EmployeeDetail.Employee_Details;
import com.eiraj.intel.drone.activities.supervisor.UnitReport;
import com.eiraj.intel.drone.helper.CheckInternet;
import com.eiraj.intel.drone.model.EmpDetail;
import com.eiraj.intel.drone.rest.ApiClient;
import com.eiraj.intel.drone.rest.ApiInterface;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.eiraj.intel.drone.Utils.Util.MY_PREFS_NAME;
import static com.eiraj.intel.drone.Utils.Util.getCurrentVersion;
import static com.eiraj.intel.drone.Utils.Util.internetConnectionAvailable;
import static com.eiraj.intel.drone.rest.ApiClient.POST_REGISTER_USER;
import static com.eiraj.intel.drone.rest.ApiClient.POST_UNIT_DATA;


public class DashBoardFace extends AppCompatActivity implements ConnectionCallbacks,
        OnConnectionFailedListener,
        LocationListener {
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    public final int CUSTOMIZED_REQUEST_CODE = 0x0000ffff;

    private Button dashboardButton;
    private EditText employeeCodeEditText;
    private TextView name, fathersname, doj;
    private Button scanUnit;
    private TextView addressTextView;
    private Button scanQrCode;
    private ImageView refresh;
    private ProgressBar bar;
    private TextView errorMessageTextView;
    private LinearLayout userDataLayout;

    private String isPersonGroupIdReg;
    private String isclick = "";
    private ProgressDialog progressDialog;
    private int numRows;
    private String language = "";
    private DatabaseConnection databaseConnection;
    private String nameStr, fathersnamestr, empCodeStr, compId, compGroupId, employeerecordid, companyShortKey = "", registrationId = "", isActive = "";
    private String responseBody = "";
    private SharedPreferences prefs;
    private String address = "";
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private double currentLatitude = 0.0;
    private double currentLongitude = 0.0;
    private boolean canProceed = false;
    private SharedPrefHelper sharedPrefHelper;
    private boolean isRegistered = false;
    private TextView onlineOfflineText;
    private CheckInternet checkInternet;

    // region override methods

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board_face);
        Theme.toWhiteStatusBar(DashBoardFace.this);
        try {
            getSupportActionBar().hide();
        } catch (Exception e) {
            e.printStackTrace();
        }

        initView();
        checkInternet.viewChangesOn(onlineOfflineText);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        mGoogleApiClient.connect();

        if (onlineOfflineText != null)
            checkInternet.viewChangesOn(onlineOfflineText);

        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        //Disconnect from API onPause()
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
        checkInternet.removeChanges();
    }

    @Override
    public void onBackPressed() {
        customBackPress();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        isclick = prefs.getString("isclickEmployee_Details", "");
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

            try {
                Log.w("eiraj", result.getContents());
                json = new JSONObject(result.getContents());

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

                editor.putString("companyGroudId", companyGroudId);
                editor.apply();

                if (internetConnectionAvailable(2000)) {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(DashBoardFace.this, R.style.MyAlertDialogTheme);

                    builder1.setMessage("Please validate details" +
                            "\nUnit name:" + UnitName +
                            "\nUnit Code: " + UnitCode);

                    builder1.setCancelable(true);

                    builder1.setPositiveButton(
                            "Yes",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    if (currentLatitude != 0.0) {
                                        new PostUnitData(DashBoardFace.this, UnitCode).execute();
                                    } else {
                                        Toast.makeText(DashBoardFace.this, "Please check your location is on or not or else use referesh button to get address", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                    builder1.setNegativeButton(
                            "No",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });


                    AlertDialog alertDialog = builder1.create();
                    alertDialog.getWindow().setLayout(600, 400); //Controlling width and height.
                    alertDialog.show();
//                    ApiInterface apiService =
//                            ApiClient.getClient().create(ApiInterface.class);
//
//                    //http://192.168.1.11/api/MobileApp/GetUserDesignationList?UnitID=303&CompanyGroupID=2&CompanyID=1D=2034
//                    //http://192.168.1.15/api/MobileApp/GetUserDesignationList?&UnitID=303&CompanyGroupID=2034&CompanyID=2}
//                    Call<List<UserDesignation>> call = apiService.GetUserDesignationList(UnitCode, companyGroudId, companyId);
//                    call.enqueue(new Callback<List<UserDesignation>>() {
//                                     @Override
//                                     public void onResponse(Call<List<UserDesignation>> call, Response<List<UserDesignation>> response) {
//
//
//
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


                } else {


                }


            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(DashBoardFace.this, "Please check your QR code/कृपया अपने क्यूआर कोड की जांच करें", Toast.LENGTH_SHORT).show();
            }


        }

    }

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
            List<Address> addresses = new ArrayList<>();
            geocoder = new Geocoder(this, Locale.getDefault());

            try {
                addresses = geocoder.getFromLocation(currentLatitude, currentLongitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                if (addresses.size() > 0) {
                    address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                    String city = addresses.get(0).getLocality();
                    String state = addresses.get(0).getAdminArea();
                    String country = addresses.get(0).getCountryName();
                    String postalCode = addresses.get(0).getPostalCode();
                    String knownName = addresses.get(0).getFeatureName();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            addressTextView.setText(address);

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
        List<Address> addresses = new ArrayList<>();
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(currentLatitude, currentLongitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            if (addresses.size() > 0) {
                address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();
                String postalCode = addresses.get(0).getPostalCode();
                String knownName = addresses.get(0).getFeatureName();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        addressTextView.setText(address);
        //  Toast.makeText(this, currentLatitude + " WORKS " + currentLongitude + "", Toast.LENGTH_LONG).show();
    }

    // endregion override methods

    // region helper methods

    private void initView() {
        sharedPrefHelper = new SharedPrefHelper();
        scanUnit = findViewById(R.id.scanUnit);
        addressTextView = findViewById(R.id.address);
        onlineOfflineText = findViewById(R.id.onlineOfflineText);
        refresh = findViewById(R.id.refresh);
        errorMessageTextView = findViewById(R.id.errorMessageTextView);
        userDataLayout = findViewById(R.id.userDataLayout);
        prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        getEmployeeCountForIntent();
        if (numRows > 0) {
            //startService(new Intent(getApplicationContext(), SyncService.class));
            SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
            String SupID = prefs.getString("SupID", "");

            if (SupID.equals("")) {
                Intent i = new Intent(getApplicationContext(), Employee_Details.class);
                i.putExtra("internet", "c");
                startActivity(i);
                finish();

            } else {
                Intent i = new Intent(getApplicationContext(), UnitReport.class);
                i.putExtra("internet", "c");
                startActivity(i);
                finish();
            }

        }
        scanUnit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isclick = "scan unit code";
                SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                editor.putString("isclickEmployee_Details", isclick);
                editor.apply();

                new IntentIntegrator(DashBoardFace.this).initiateScan();
            }
        });
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGoogleApiClient = new GoogleApiClient.Builder(DashBoardFace.this)
                        // The next two lines tell the new client that “this” current class will handle connection stuff
                        .addConnectionCallbacks(DashBoardFace.this)
                        .addOnConnectionFailedListener(DashBoardFace.this)
                        //fourth line adds the LocationServices API endpoint from GooglePlayServices
                        .addApi(LocationServices.API)
                        .build();

                // Create the LocationRequest object
                mLocationRequest = LocationRequest.create()
                        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                        .setInterval(1000)        // 10 seconds, in milliseconds
                        .setFastestInterval(1000);
            }
        });
        checkPlayServices();
        check();
        dashboardButton = findViewById(R.id.proceed);
        employeeCodeEditText = findViewById(R.id.employeecodeEditText);
        name = findViewById(R.id.name);
        fathersname = findViewById(R.id.fathersname);
        doj = findViewById(R.id.doj);
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

        setErrorMessage(getResources().getString(R.string.initial_info_dashboardFace), true);

       // checkInternet = SampleApp.getCheckInternet();
        checkInternet = new CheckInternet(DashBoardFace.this);
    }

    private void customBackPress() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(DashBoardFace.this);
        builder1.setMessage("Do you want to logout?");

        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.addCategory(Intent.CATEGORY_HOME);

                        startActivity(intent);
                    }
                });

        builder1.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        builder1.show();
    }

    private void getEmployeeCountForIntent() {
        databaseConnection = new DatabaseConnection(getApplicationContext());
        Cursor res = databaseConnection.getEmployeeInfoCount();
        numRows = res.getCount();
    }

    private void checkIfRegistrationRequired(String is_Registered, String employeerecordid) {
        if (is_Registered.equalsIgnoreCase("false")) {
            isRegistered = false;
            dashboardButton.setText("Register Face");
            dashboardButton.setBackgroundTintList(ContextCompat.getColorStateList(DashBoardFace.this, R.color.green));
            dashboardButton.setBackgroundTintMode(PorterDuff.Mode.SRC_ATOP);
        } else {
            isRegistered = true;
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
        LocationManager lm = (LocationManager) DashBoardFace.this.getSystemService(Context.LOCATION_SERVICE);
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
            AlertDialog.Builder dialog = new AlertDialog.Builder(DashBoardFace.this);
            dialog.setMessage("Gps is not enabled/जीपीएस सक्षम नहीं है");
            dialog.setPositiveButton("Open Setting/सेटिंग खोलें", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    DashBoardFace.this.startActivity(myIntent);
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

    public void setErrorMessage(String message, boolean visibility) {
        if (visibility) {
            canProceed = false;
            errorMessageTextView.setText(message);
            userDataLayout.setVisibility(View.GONE);
            errorMessageTextView.setVisibility(View.VISIBLE);
            dashboardButton.setBackgroundTintList(ContextCompat.getColorStateList(DashBoardFace.this, R.color.divider));
        } else {
            canProceed = true;
            errorMessageTextView.setVisibility(View.GONE);
            userDataLayout.setVisibility(View.VISIBLE);
            dashboardButton.setBackgroundTintList(ContextCompat.getColorStateList(DashBoardFace.this, R.color.green));
        }
        dashboardButton.setBackgroundTintMode(PorterDuff.Mode.SRC_ATOP);
        dashboardButton.setEnabled(canProceed);
    }

    // endregion helper methods

    // region onClick methods

    public void full(View view) {
        if (name.getText().toString() == "") {
            Toast.makeText(getApplicationContext(), "Enter valid Employee Code to proceed or press show button", Toast.LENGTH_LONG).show();
        } else if (name.getText().toString() != null) {

            if (isRegistered) {
                // Not needed after new ML based face recognition
                // was used when microsoft face recognition was used

                /*if (isPersonGroupIdReg.contains("Not Found")) {
                    SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                    editor.putString("SelectedNameForGroup", employeeCodeEditText.getText().toString());
                    editor.putString("Type", "0");
                    editor.apply();
                    Intent i = new Intent(getApplicationContext(), PersonActivity.class);// register activity
                    startActivity(i);
                } else {*/
                AlertDialog.Builder builder = new AlertDialog.Builder(DashBoardFace.this, R.style.MyAlertDialogTheme);

                builder.setTitle("Confirm");
                builder.setMessage("Please check your credentials\n" +
                        "कृपया अपने प्रमाण पत्र की जांच करें" +
                        "\n" +
                        "Name/नाम: " + nameStr + "\n" +
                        "Father Name/पिता का नाम: " + fathersnamestr + "\n" +
                        "Employee Code/कर्मचारी कोड: " + empCodeStr + "\n" +
                        "Company Name/कंपनी का नाम: " + getResources().getString(R.string.company_name) + "\n");
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        DatabaseConnection databaseConnection = new DatabaseConnection(DashBoardFace.this);
                        databaseConnection.deleteEmployeeInfo();
                        databaseConnection.insertEmployeeInfo(nameStr, fathersnamestr, getResources().getString(R.string.company_name), empCodeStr);

                        if (internetConnectionAvailable(2000)) {
                            //new PostRegisterUser(DashBoardFace.this).execute();
                            SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
                            Boolean supOrNot = prefs.getBoolean("isSup", false);
                            if (supOrNot.equals(true)) {
                                Intent i = new Intent(getApplicationContext(), Employee_Details.class);
                                i.putExtra("internet", "c");
                                startActivity(i);
                                finish();
                            } else {
                                Intent i = new Intent(getApplicationContext(), Employee_Details.class);
                                i.putExtra("internet", "c");
                                startActivity(i);
                                finish();
                            }
                        } else {
                            Toast.makeText(DashBoardFace.this, R.string.msg_alert_no_internet, Toast.LENGTH_SHORT).show();

                        }
                    }
                });
                builder.show();
                //}
            } else {
                Intent i = new Intent(getApplicationContext(), UserFaceRegistration.class);
                i.putExtra("userName", nameStr);
                i.putExtra("employeeCode", empCodeStr);
                i.putExtra("userID", employeerecordid);
                i.putExtra("compGroupId", compGroupId);
                i.putExtra("compId", compId);
                i.putExtra("fatherName", fathersnamestr);
                i.putExtra("companyShortKey", companyShortKey);
                startActivity(i);
                finish();
            }

        } else {
            Toast.makeText(getApplicationContext(), "Enter valid Employee Code to proceed", Toast.LENGTH_LONG).show();

        }
    }

    public void clear(View view) {
        setErrorMessage(getResources().getString(R.string.emptyCodeErrorMessage_dashboardFace), true);
        employeeCodeEditText.setText("");
        name.setText("");
        fathersname.setText("");
        doj.setText("");
        empCodeStr = "";
        fathersnamestr = "";
        empCodeStr = "";
        compId = "";
        compGroupId = "";
        employeerecordid = "";
    }

    public void show(View view) {
        if (employeeCodeEditText.getText() == null || employeeCodeEditText.getText().toString().isEmpty()) {
            Toast.makeText(getApplicationContext(), "Enter Employee Code to proceed", Toast.LENGTH_LONG).show();
            setErrorMessage(getResources().getString(R.string.emptyCodeErrorMessage_dashboardFace), true);
        } else {

            if (internetConnectionAvailable(2000)) {
                ApiInterface apiService =
                        ApiClient.getClient().create(ApiInterface.class);

                Call<List<EmpDetail>> call = apiService.GetEmpDetails(employeeCodeEditText.getText().toString());
                bar = findViewById(R.id.progressBar);

                try {
                    InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                } catch (Exception e) {
                    // Keyboard is not visible
                    e.printStackTrace();
                }

                bar.setVisibility(View.VISIBLE);
                call.enqueue(new Callback<List<EmpDetail>>() {
                                 @Override
                                 public void onResponse(Call<List<EmpDetail>> call, Response<List<EmpDetail>> response) {
                                     bar.setVisibility(View.GONE);
                                     if (response.body() != null && response.body().size() > 0) {
                                         EmpDetail empDetail = (response.body().get(0));

                                         // name.setText(response.body().indexOf(0));
                                         nameStr = empDetail.getName();
                                         fathersnamestr = empDetail.getFatherName();
                                         empCodeStr = employeeCodeEditText.getText().toString();
                                         compId = empDetail.getCompid();
                                         compGroupId = empDetail.getCompGroupID();
                                         employeerecordid = empDetail.getEmpRecordID();
                                         isActive = empDetail.getIsActive();

                                         name.setText("Name: " + empDetail.getName());
                                         fathersname.setText("Father Name: " + empDetail.getFatherName());
                                         doj.setText("Date of joining: " + empDetail.getDoj().substring(0, 10));
                                         isPersonGroupIdReg = empDetail.getIsPersonGroupIdReg();
                                         companyShortKey = empDetail.getCompanyShortKey();
                                         registrationId = empDetail.getRegistrationId();

                                         Log.e("XXX", "onResponse: " + nameStr);
                                         Log.e("XXX", "onResponse: " + fathersnamestr);
                                         Log.e("XXX", "onResponse: " + empCodeStr);
                                         Log.e("XXX", "onResponse: " + compId);
                                         Log.e("XXX", "onResponse: " + compGroupId);
                                         Log.e("XXX", "onResponse: " + employeerecordid);
                                         Log.e("XXX", "onResponse: " + isPersonGroupIdReg);
                                         Log.e("XXX", "onResponse: " + employeerecordid);
                                         Log.e("XXX", "onResponse: " + empDetail.getIsRegistered());
                                         Log.e("XXX", "onResponse: " + empDetail.getCompanyShortKey());
                                         Log.e("XXX", "onResponse: " + registrationId);

                                         sharedPrefHelper.setUserRegID(employeerecordid);
                                         sharedPrefHelper.setCompanyShortKey(companyShortKey);
                                         sharedPrefHelper.setML_registrationID(registrationId);
                                         sharedPrefHelper.setCompanyID(compId);
                                         sharedPrefHelper.setEmployeeCode(empCodeStr);
                                         checkIfRegistrationRequired(empDetail.getIsRegistered(), employeerecordid);

                                         if (isActive.equalsIgnoreCase("EMPLOYEE ACTIVE")){
                                             setErrorMessage("", false);
                                         } else {
                                             setErrorMessage("Employee not active.", true);
                                         }

                                     } else {
                                         Toast.makeText(getApplicationContext(), "Enter valid Employee Code to proceed", Toast.LENGTH_LONG).show();
                                         setErrorMessage(getResources().getString(R.string.invalidCodeErrorMessage_dashboardFace), true);
                                     }
                                 }

                                 @Override
                                 public void onFailure(Call<List<EmpDetail>> call, Throwable t) {
                                     bar.setVisibility(View.GONE);
                                     Log.e("Eiraj", "Api not working");

                                 }
                             }
                );


            } else {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.noInternetErrorMessage), Toast.LENGTH_LONG).show();
                setErrorMessage(getResources().getString(R.string.noInternetErrorMessage), true);
            }


        }
    }

    // endregion onClick methods

    // region API Async Tasks

    class PostUnitData extends AsyncTask<Void, Void, String> {

        private static final String TAG = "PostAttachments";
        String responseBody = "";
        Context ctx;
        String username, dataareaid;
        String unitCode;

        public PostUnitData(Context ctx, String unitCode) {
            this.unitCode = unitCode;
            this.ctx = ctx;

            progressDialog = new ProgressDialog(ctx);
            progressDialog.setTitle(getString(R.string.progress_dialog_title));
            progressDialog.show();

        }

        @Override
        protected String doInBackground(Void... arg0) {

            HttpClient httpClient = new DefaultHttpClient();
            HttpParams params = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(params, 3000);

            HttpPost httpPost = new HttpPost(POST_UNIT_DATA);

            Bundle bundle = new Bundle();

            SharedPreferences prefs;
            prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);


            List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();

            nameValuePair.add(new BasicNameValuePair("UnitCode", unitCode));
            nameValuePair.add(new BasicNameValuePair("Latitude", Double.toString(currentLatitude)));//login
            nameValuePair.add(new BasicNameValuePair("Longitude", Double.toString(currentLongitude)));
            nameValuePair.add(new BasicNameValuePair("Type", "1"));//for new


            try {
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
            } catch (UnsupportedEncodingException e) {
                // writing error to Log
                e.printStackTrace();
            }

            try {
                HttpResponse response = httpClient.execute(httpPost);

                //String dealername, String psrname, String beatname,String selecteddate,String itemname

                responseBody = EntityUtils.toString(response.getEntity());
                progressDialog.dismiss();

                //  databaseConnection.updateFeedback(cursor.getString(0));
                // writing response to log
                Log.d("Http Response:", response.toString());


                //  databaseConnection.insertlog(logid,mobno, GetLoginApi.userCode,currdates,version,manufacturer,model,osversion,logstat,GetLoginApi.DataAreaID);


            } catch (ClientProtocolException e) {
                // writing exception to log
                e.printStackTrace();
            } catch (IOException e) {
                // writing exception to log
                e.printStackTrace();

            }


            return responseBody;
        }


        @Override
        protected void onPostExecute(String result) {

            super.onPostExecute(result);

            Toast.makeText(getApplicationContext(), responseBody, Toast.LENGTH_SHORT).show();


        }

    }

    public class PostRegisterUser extends AsyncTask<Void, Void, Void> {

        private static final String TAG = "PostAttachments";
        Context ctx;
        String username, dataareaid;
        String img = "";
        private byte[] response;

        public PostRegisterUser(Context ctx) {
            this.ctx = ctx;
            bar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpClient httpClient = new DefaultHttpClient();
            HttpParams params = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(params, 3000);


            HttpPost httpPost = new HttpPost(POST_REGISTER_USER);


            List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();
            nameValuePair.add(new BasicNameValuePair("EmpRecordID", employeerecordid));
            nameValuePair.add(new BasicNameValuePair("Empcode", empCodeStr));
            nameValuePair.add(new BasicNameValuePair("CompGroupID", compGroupId));
            nameValuePair.add(new BasicNameValuePair("CompID", compId));
            nameValuePair.add(new BasicNameValuePair("MobileNo", ""));
            nameValuePair.add(new BasicNameValuePair("ImeiNo", getCurrentVersion(ctx)));
            if (isRegistered)
                nameValuePair.add(new BasicNameValuePair("RegistrationID", employeerecordid));
            else
                nameValuePair.add(new BasicNameValuePair("RegistrationID", ""));


            try {
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
            } catch (UnsupportedEncodingException e) {
                // writing error to Log
                e.printStackTrace();
            }

            try {
                HttpResponse response = httpClient.execute(httpPost);
                //String dealername, String psrname, String beatname,String selecteddate,String itemname
//Log.w("eiraj", getDeviceIMEI());
                responseBody = EntityUtils.toString(response.getEntity());
                Log.w("eiraj", responseBody);

                //  databaseConnection.updateFeedback(cursor.getString(0));
                // writing response to log
                Log.d("Http Response:", response.toString());


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
            bar.setVisibility(View.GONE);

            if (responseBody.contains("Employee Already Registered")) {

                Toast.makeText(ctx, "Employee Already Registered/कर्मचारी पहले ही पंजीकृत है", Toast.LENGTH_SHORT).show();

            } else if (responseBody.contains("Registration Successful") || responseBody.contains("Employee Duplicate Registration Successful.")) {
                DatabaseConnection databaseConnection = new DatabaseConnection(DashBoardFace.this);
                databaseConnection.deleteEmployeeInfo();
                databaseConnection.insertEmployeeInfo(nameStr, fathersnamestr, getResources().getString(R.string.company_name), empCodeStr);
                //Toast.makeText(ctx, responseBody, Toast.LENGTH_SHORT).show();
                SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
                Boolean supOrNot = prefs.getBoolean("isSup", false);
                if (supOrNot.equals(true)) {
                    Intent i = new Intent(getApplicationContext(), Employee_Details.class);
                    i.putExtra("internet", "c");
                    startActivity(i);
                    finish();
                } else {
                    Intent i = new Intent(getApplicationContext(), Employee_Details.class);
                    i.putExtra("internet", "c");
                    startActivity(i);
                    finish();
                }
            } else {
                Toast.makeText(ctx, responseBody, Toast.LENGTH_SHORT).show();
            }
        }

    }

    // endregion API Async Tasks
}
