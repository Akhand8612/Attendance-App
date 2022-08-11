package com.eiraj.intel.drone.activities.Login;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.view.textclassifier.TextClassifierEvent;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ComponentActivity;

import com.eiraj.intel.drone.Database.DatabaseConnection;
import com.eiraj.intel.drone.Faceui.DashBoardFace;
import com.eiraj.intel.drone.R;
import com.eiraj.intel.drone.Utils.AlertDialogs;
import com.eiraj.intel.drone.Utils.Constants;
import com.eiraj.intel.drone.Utils.Theme;
import com.eiraj.intel.drone.Utils.Util;
import com.eiraj.intel.drone.activities.Dashboard.AdminDashboard;
import com.eiraj.intel.drone.activities.EmployeeDetail.Employee_Details;
import com.eiraj.intel.drone.activities.supervisor.SupervisorDashBoard;
import com.eiraj.intel.drone.helper.CheckInternet;
import com.eiraj.intel.drone.rest.ApiClient;
import com.eiraj.intel.drone.rest.ApiInterface;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.eiraj.intel.drone.Utils.Util.MY_PREFS_NAME;
import static com.eiraj.intel.drone.activities.EmployeeDetail.MarkAttendance.isTimeAutomatic;


public class LoginActivity extends AppCompatActivity {
    private static final int PERMISSION_CALLBACK_CONSTANT = 100;
    private static final int REQUEST_PERMISSION_SETTING = 101;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    List<String> categories;
    String results = "";
    String[] permissionsRequired = new String[]{Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE};
    String selectedItem = "";
    int numRows;
    DatabaseConnection databaseConnection;
    private EditText userName, userPassword;
    private Button submit;
    private TextInputLayout textInputLayout;
    private ProgressBar bar;
    private CoordinatorLayout coordinatorLayout;
    private TextInputLayout textInputLayoutPassword;
    private Spinner spinner;
    private SharedPreferences permissionStatus;
    private boolean sentToSettings = false;
    private LinearLayout innerParentLayout;
    private TextView onlineOfflineText;
    private CheckInternet checkInternet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        try {
            getSupportActionBar().hide();
        } catch (Exception e) {
            e.printStackTrace();
        }

        getEmployeeCountForIntent();
        Intent intent = this.getIntent();
        if (intent != null) {
            String isInternetMessageEmployeeDetail = intent.getExtras().getString("No");
            if (isInternetMessageEmployeeDetail.equals("a")) {

            } else {
                if (numRows > 0) {

                    //startService(new Intent(getApplicationContext(), SyncService.class));

                    SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
                    String SupID = prefs.getString("SupID", "");

                    Log.e("XXX", "onCreate: " + SupID);

                    if (SupID.equals("")) {
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

                }

            }
        }


        getId();
        isDateTimeAuto();
        launchActivity();
        setSpinnerData();
        setLanguage();
        //checkInternet = SampleApp.getCheckInternet();
        checkInternet = new CheckInternet(LoginActivity.this);
        checkInternet.viewChangesOn(onlineOfflineText);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                editor.putString("lang", selectedItem);

                editor.apply();

                SharedPreferences prefs;
                prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);

                selectedItem = prefs.getString("lang", "English");
                if (selectedItem.equals("English")) {

                    textInputLayout.setHint("User Name");
                    textInputLayoutPassword.setHint("Password");
                    submit.setText("Login");
                } else if (selectedItem.equals("Hindi")) {
                    textInputLayout.setHint("उपयोगकर्ता नाम");
                    textInputLayoutPassword.setHint("पारण शब्द");
                    submit.setText("लॉग इन करें");
                }

            } // to close the onItemSelected

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userName.getText().toString().equals("")) {
                    Snackbar snackbar = Snackbar
                            .make(coordinatorLayout, "Enter Your Valid User Name", Snackbar.LENGTH_SHORT);

                    snackbar.show();
                } else if (userPassword.getText().toString().equals("")) {
                    Snackbar snackbar = Snackbar
                            .make(coordinatorLayout, "Enter Your Valid Password", Snackbar.LENGTH_SHORT);

                    snackbar.show();
                } else if (userName.getText().toString().contains("demo1") && userPassword.getText().toString().contains("demo1")) {
                    Intent i = new Intent(getApplicationContext(), SupervisorDashBoard.class);
                    i.putExtra("isalert", "no");
                    startActivity(i);
                    finish();
                }
                /*else if (userName.getText().toString().contains("demo2") && userPassword.getText().toString().contains("demo2")) {
                    Intent i = new Intent(getApplicationContext(), DashBoardFaceAdmin.class);
                    i.putExtra("isalert", "no");
                    startActivity(i);
                    finish();
                }*/
                // TODO : UNCOMMENT FOR TESTING SUPER ADMIN
                else if (userName.getText().toString().contains("superadmin") && userPassword.getText().toString().contains("superadmin")) {
                    Intent i = new Intent(getApplicationContext(), AdminDashboard.class);
                    startActivity(i);
                    finish();
                } else {
                    if (Util.internetConnectionAvailable(200)) {
                        loginUser();
                    } else {
                        Snackbar.make(coordinatorLayout, R.string.msg_alert_no_internet, Snackbar.LENGTH_SHORT)
                                .setAction("Action", null).show();
                    }
                }
            }
        });
        innerParentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                } catch (Exception e) {
                    // Keyboard is not visible
                    e.printStackTrace();
                }
            }
        });

        Theme.toWhiteStatusBar(LoginActivity.this);

        /*Toast.makeText(LoginActivity.this, "" + CheckInternet.checkStatus(LoginActivity.this) , Toast.LENGTH_SHORT).show();

        switch (CheckInternet.checkStatus(LoginActivity.this)){
            case 2:
                AlertDialogs.show(LoginActivity.this, "Internet connection is restricted");
                break;
        }*/
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

    private void getEmployeeCountForIntent() {
        databaseConnection = new DatabaseConnection(getApplicationContext());
        Cursor res = databaseConnection.getEmployeeInfoCount();
        numRows = res.getCount();
    }

    private void setLanguage() {
        SharedPreferences prefs;
        prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        //"No name defined" is the default value.
        selectedItem = prefs.getString("lang", "English");
        if (selectedItem.equals("English")) {

            textInputLayout.setHint("User Name");
            // do your stuff
            textInputLayoutPassword.setHint("Password");
            submit.setText("Login");
        } else if (selectedItem.equals("Hindi")) {
            textInputLayout.setHint("उपयोगकर्ता नाम");
            textInputLayoutPassword.setHint("पारण शब्द");
            submit.setText("लॉग इन करें");
        }
    }

    private void isDateTimeAuto() {
        if (isTimeAutomatic(getApplicationContext())) {

        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
            builder.setTitle("Need Automatic Time");
            builder.setMessage("This app needs Automatic Android time.");
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
                    finish();
                    dialog.cancel();
                }
            });
            builder.show();
        }
    }


    private void setSpinnerData() {
        categories = new ArrayList<String>();
        categories.add("English");
        categories.add("Hindi");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
    }

    private void launchActivity() {
        if (ActivityCompat.checkSelfPermission(LoginActivity.this, permissionsRequired[0]) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(LoginActivity.this, permissionsRequired[1]) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(LoginActivity.this, permissionsRequired[2]) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this, permissionsRequired[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this, permissionsRequired[1])
                    || ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this, permissionsRequired[2])) {

                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setTitle("Need Multiple Permissions/एकाधिक अनुमतियों की आवश्यकता है");
                builder.setMessage("This app needs Camera and Location permissions./इस ऐप को कैमरा और स्थान अनुमतियों की आवश्यकता है।");
                builder.setPositiveButton("Grant/अनुदान", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(LoginActivity.this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
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
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
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
                ActivityCompat.requestPermissions(LoginActivity.this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
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
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this, permissionsRequired[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this, permissionsRequired[1])
                    || ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this, permissionsRequired[2])) {

                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setTitle("Need Multiple Permissions");
                builder.setMessage("This app needs Camera and Location permissions.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(LoginActivity.this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Snackbar snackbar = Snackbar
                                .make(coordinatorLayout, "Grant All Permissions To Use The Application", Snackbar.LENGTH_SHORT);
                        snackbar.show();
                        dialog.cancel();
                        finish();
                    }
                });
                builder.show();
            } else {
                //  Toast.makeText(getBaseContext(),"Unable to get Permission",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PERMISSION_SETTING) {
            if (ActivityCompat.checkSelfPermission(LoginActivity.this, permissionsRequired[0]) == PackageManager.PERMISSION_GRANTED) {
                //Got Permission
                proceedAfterPermission();
            }
        }
    }

    @Override
    public void onBackPressed() {

        new AlertDialog.Builder(this)
                .setTitle("Exit?")
                .setMessage("Are you sure you want to exit?/\n" +
                        "क्या आप वाकई ऐप से बाहर निकलना चाहते हैं?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                    public void onClick(DialogInterface arg0, int arg1) {
                        finishAffinity();
                    }
                }).create().show();
    }

    private void proceedAfterPermission() {

        //  Toast.makeText(getBaseContext(), "We got All Permissions", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (sentToSettings) {
            if (ActivityCompat.checkSelfPermission(LoginActivity.this, permissionsRequired[0]) == PackageManager.PERMISSION_GRANTED) {
                //Got Permission
                //  proceedAfterPermission();
            }
        }
    }

    @Override
    protected void onResume() {
        if (onlineOfflineText != null){
            checkInternet.viewChangesOn(onlineOfflineText);
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        checkInternet.removeChanges();
        super.onPause();
    }

    public void getId() {
        userName = findViewById(R.id.editText);
        onlineOfflineText = findViewById(R.id.onlineOfflineText);
        userPassword = findViewById(R.id.editText2);
        submit = findViewById(R.id.button);
        innerParentLayout = findViewById(R.id.innerParentLayout);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        bar = (ProgressBar) this.findViewById(R.id.progressBar);
        spinner = (Spinner) findViewById(R.id.lang);
        textInputLayout = (TextInputLayout) findViewById(R.id.text_input);

        textInputLayoutPassword = (TextInputLayout) findViewById(R.id.text_input_pass);
        permissionStatus = PreferenceManager
                .getDefaultSharedPreferences(this);
    }

    private void loginUser() {

        bar.setVisibility(View.VISIBLE);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiClient.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(Constants.okHttpClient_3minTimeout)
                .build();
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);

        Call<ResponseBody> loginUser = apiInterface.loginUser(userName.getText().toString(), userPassword.getText().toString());
        loginUser.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try {

                    if (response.body() != null) {
                        String apiResponse = response.body().string().toString();

                        Log.e("XXX", "onResponse: " + apiResponse);

                        if (apiResponse.contains("Invalid Credentials !!!")) {

                            Snackbar snackbar = Snackbar.make(coordinatorLayout, apiResponse, Snackbar.LENGTH_LONG);
                            snackbar.show();

                        } else if (apiResponse.contains("ZGVtbzpkZW1v")) {

                            Intent i = new Intent(getApplicationContext(), DashBoardFace.class);
                            i.putExtra("isalert", "no");
                            startActivity(i);
                            finish();

                        } else if (apiResponse.contains("SuperAdministrator")) {

                            Intent i = new Intent(getApplicationContext(), AdminDashboard.class);
                            startActivity(i);
                            finish();

                        } else {
                            try {
                                JSONObject jsonObject = new JSONObject(apiResponse);
                                if (jsonObject.has("name")) {
                                    Snackbar snackbar = Snackbar.make(coordinatorLayout, "Some error occurred. Code - " + jsonObject.getString("name"), Snackbar.LENGTH_LONG);
                                    snackbar.show();
                                } else {
                                    Snackbar snackbar = Snackbar.make(coordinatorLayout, "Some error occurred. Code - " + apiResponse, Snackbar.LENGTH_LONG);
                                    snackbar.show();
                                }
                            } catch (Exception e) {
                                Snackbar snackbar = Snackbar.make(coordinatorLayout, "Some error occurred. Code - " + apiResponse, Snackbar.LENGTH_LONG);
                                snackbar.show();
                            }
                        }
                    } else {
                        AlertDialogs.show(LoginActivity.this, "Unable to process request. Code - " + response.code());
                    }
                } catch (Exception e) {
                    AlertDialogs.show(LoginActivity.this, "Unable to process request. Code - " + response.code() + " Message-" + e.getMessage());
                    e.printStackTrace();
                }
                bar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                bar.setVisibility(View.GONE);
                AlertDialogs.show(LoginActivity.this, "Request Timed out. Please use an active internet connection. ERROR MESSAGE : " + t.getMessage());
            }
        });
    }
}
