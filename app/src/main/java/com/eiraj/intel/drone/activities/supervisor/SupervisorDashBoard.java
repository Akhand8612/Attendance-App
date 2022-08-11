package com.eiraj.intel.drone.activities.supervisor;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.eiraj.intel.drone.R;
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

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static com.eiraj.intel.drone.Utils.Util.MY_PREFS_NAME;
import static com.eiraj.intel.drone.Utils.Util.internetConnectionAvailable;
import static com.eiraj.intel.drone.rest.ApiClient.POST_UNIT_DATA;


public class SupervisorDashBoard extends AppCompatActivity implements ConnectionCallbacks,
        OnConnectionFailedListener,
        LocationListener {
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    // lists for permissions
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    public final int CUSTOMIZED_REQUEST_CODE = 0x0000ffff;
    private final int REQUEST_READ_PHONE_STATE = 1;
    private final int REQUEST_LOCATION_PERMISSION = 1;
    Button dashboardButton;
    EditText employeeCodeEditText;
    TextView name, fathersname, doj;
    String isPersonGroupIdReg;
    Button scanUnit;
    String isclick = "";
    TextView addressTextView;
    ImageView refresh;
    ProgressDialog progressDialog;
    SharedPreferences prefs;
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
    private ProgressBar bar;

    @Override
    public void onBackPressed() {
        Intent i = new Intent(getApplicationContext(), SupervisorMainScreen.class);
        i.putExtra("internet", "c");
        startActivity(i);
        finish();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        check();
        checkPlayServices();
        requestLocationPermission();
        setContentView(R.layout.activity_supervisor_dash_board);
        scanUnit = findViewById(R.id.scanUnit);
        addressTextView = findViewById(R.id.address);
        refresh = findViewById(R.id.refresh);
        prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        scanUnit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String address = addressTextView.getText().toString();
                Log.w("eirajtext", address);
                if (!address.equals("Address:")) {

                    isclick = "scan unit code";
                    SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                    editor.putString("isclickEmployee_Details", isclick);
                    editor.apply();


                    new IntentIntegrator(SupervisorDashBoard.this).initiateScan();
                } else {
                    requestLocationPermission();
                    check();
                    checkPlayServices();
                    Toast.makeText(getApplicationContext(), "Please check your location permission once again", Toast.LENGTH_LONG).show();
                }
            }
        });
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGoogleApiClient = new GoogleApiClient.Builder(SupervisorDashBoard.this)
                        // The next two lines tell the new client that “this” current class will handle connection stuff
                        .addConnectionCallbacks(SupervisorDashBoard.this)
                        .addOnConnectionFailedListener(SupervisorDashBoard.this)
                        //fourth line adds the LocationServices API endpoint from GooglePlayServices
                        .addApi(LocationServices.API)
                        .build();

                // Create the LocationRequest object
                mLocationRequest = LocationRequest.create()
                        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                        .setInterval(1 * 1000)        // 10 seconds, in milliseconds
                        .setFastestInterval(1 * 1000);
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
                .setInterval(1 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000);

    }

    public void full(View view) {
        if (name.getText().toString() == "") {
            Toast.makeText(getApplicationContext(), "Enter vald Employee Code to proceed or press show button", Toast.LENGTH_LONG).show();
        } else if (name.getText().toString() != null) {

            if (isPersonGroupIdReg.contains("Not Found")) {

            } else {

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Alert");
                builder.setMessage("Employee Face Already scanned.Do you want to update details?");
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                        editor.putString("SelectedNameForGroup", employeeCodeEditText.getText().toString());
                        editor.putString("isPersonGroupIdReg", isPersonGroupIdReg);
                        editor.putString("Type", "1");
                        bar.setVisibility(View.VISIBLE);

                        editor.apply();


                        //   new DeletePersonTask("5f1c2074-1251-46b4-9857-22dd2c7d5633", isPersonGroupIdReg).execute(isPersonGroupIdReg);

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
            Toast.makeText(getApplicationContext(), "Enter valid Employee Code to proceed", Toast.LENGTH_LONG).show();

        }
    }

    public void show(View view) {
        if (employeeCodeEditText.getText().toString() == "") {
            Toast.makeText(getApplicationContext(), "Enter Employee Code to proceed", Toast.LENGTH_LONG).show();
        } else {


        }
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
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(SupervisorDashBoard.this, R.style.MyAlertDialogTheme);

                    builder1.setMessage("Please validate details" +
                            "\nUnit name:" + UnitName +
                            "\nUnit Code: " + UnitCode);

                    builder1.setCancelable(true);

                    builder1.setPositiveButton(
                            "Yes",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                    new PostUnitData(SupervisorDashBoard.this, UnitCode).execute();
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


                } else {


                }


            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(SupervisorDashBoard.this, "Please check your QR code/कृपया अपने क्यूआर कोड की जांच करें", Toast.LENGTH_SHORT).show();
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
        LocationManager lm = (LocationManager) SupervisorDashBoard.this.getSystemService(Context.LOCATION_SERVICE);
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
            final AlertDialog.Builder dialog = new AlertDialog.Builder(SupervisorDashBoard.this);
            dialog.setMessage("Gps is not enabled/जीपीएस सक्षम नहीं है");
            dialog.setPositiveButton("Open Setting/सेटिंग खोलें", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    SupervisorDashBoard.this.startActivity(myIntent);
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
                addressTextView.setText(address);
            } catch (IOException e) {
                e.printStackTrace();
                addressTextView.setText(currentLatitude + "," + currentLongitude);
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
            addressTextView.setText(address);
        } catch (IOException e) {
            addressTextView.setText(currentLatitude + "," + currentLongitude);
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
            progressDialog.setTitle("Please wait");
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
}
