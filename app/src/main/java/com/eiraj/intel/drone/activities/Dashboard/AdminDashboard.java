package com.eiraj.intel.drone.activities.Dashboard;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.eiraj.intel.drone.R;
import com.eiraj.intel.drone.Utils.AlertDialogs;
import com.eiraj.intel.drone.Utils.Theme;
import com.eiraj.intel.drone.model.EmpDetail;
import com.eiraj.intel.drone.rest.ApiClient;
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
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.eiraj.intel.drone.Utils.Util.getCurrentVersion;
import static com.eiraj.intel.drone.Utils.Util.internetConnectionAvailable;
import static com.eiraj.intel.drone.rest.ApiClient.POST_REGISTER_USER;

public class AdminDashboard extends AppCompatActivity {

    private Button dashboardButton;
    private EditText employeeCodeEditText;
    private TextView name, fathersname, doj, faceId;
    private ProgressBar bar;
    private TextView errorMessageTextView;
    private LinearLayout userDataLayout;

    private String isPersonGroupIdReg;
    private String nameStr = "", fathersnamestr = "", empCodeStr = "", compId = "", compGroupId = "", employeerecordid = "";
    private String responseBody = "";
    private boolean canProceed = false;

    // region override methods

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);
        Theme.toWhiteStatusBar(AdminDashboard.this);
        try {
            getSupportActionBar().hide();
        } catch (Exception e) {
            e.printStackTrace();
        }

        initView();

    }

    @Override
    public void onBackPressed() {
        customBackPress();
    }

    // endregion override methods

    // region helper methods

    private void initView() {
        errorMessageTextView = findViewById(R.id.errorMessageTextView);
        userDataLayout = findViewById(R.id.userDataLayout);
        dashboardButton = findViewById(R.id.proceed);
        employeeCodeEditText = findViewById(R.id.employeecodeEditText);
        name = findViewById(R.id.name);
        faceId = findViewById(R.id.faceId);
        fathersname = findViewById(R.id.fathersname);
        doj = findViewById(R.id.doj);

        setErrorMessage(getResources().getString(R.string.initial_info_dashboardFace), true);
    }

    private void customBackPress() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(AdminDashboard.this);
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

    public void setErrorMessage(String message, boolean visibility) {
        if (visibility) {
            canProceed = false;
            errorMessageTextView.setText(message);
            userDataLayout.setVisibility(View.GONE);
            errorMessageTextView.setVisibility(View.VISIBLE);
            dashboardButton.setBackgroundTintList(ContextCompat.getColorStateList(AdminDashboard.this, R.color.divider));
        } else {
            canProceed = true;
            errorMessageTextView.setVisibility(View.GONE);
            userDataLayout.setVisibility(View.VISIBLE);
            dashboardButton.setBackgroundTintList(ContextCompat.getColorStateList(AdminDashboard.this, R.color.green));
        }
        dashboardButton.setBackgroundTintMode(PorterDuff.Mode.SRC_ATOP);
        dashboardButton.setEnabled(canProceed);
    }

    // endregion helper methods

    // region onClick methods

    public void full(View view) {
        if (name.getText() != null && !name.getText().toString().isEmpty()) {
            new PostRegisterUser(AdminDashboard.this).execute();
        } else {
            Toast.makeText(getApplicationContext(), "Enter valid Employee Code to proceed or press show button", Toast.LENGTH_LONG).show();
        }
    }

    public void clear(View view) {
        setErrorMessage(getResources().getString(R.string.emptyCodeErrorMessage_dashboardFace), true);
        employeeCodeEditText.setText("");
        name.setText("");
        faceId.setText("");
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
                                         fathersnamestr = empDetail.getName();
                                         empCodeStr = employeeCodeEditText.getText().toString();
                                         compId = empDetail.getCompid();
                                         compGroupId = empDetail.getCompGroupID();
                                         employeerecordid = empDetail.getEmpRecordID();

                                         name.setText("Name: " + empDetail.getName());
                                         fathersname.setText("Father Name: " + empDetail.getFatherName());
                                         doj.setText("Date of joining: " + empDetail.getDoj().substring(0, 10));

                                         if (empDetail.getRegistrationId().isEmpty()){
                                             faceId.setText(String.format("Face ID: Not Registered (%s)", empDetail.getRegistrationId()));
                                             faceId.setTextColor(getResources().getColor(R.color.colorPrimary));
                                         } else {
                                             faceId.setText(String.format("Face ID: Registered (%s)", empDetail.getRegistrationId()));
                                             faceId.setTextColor(getResources().getColor(R.color.green));
                                         }

                                         isPersonGroupIdReg = empDetail.getIsPersonGroupIdReg();

                                         Log.e("XXX", "onResponse: " + nameStr);
                                         Log.e("XXX", "onResponse: " + fathersnamestr);
                                         Log.e("XXX", "onResponse: " + empCodeStr);
                                         Log.e("XXX", "onResponse: " + compId);
                                         Log.e("XXX", "onResponse: " + compGroupId);
                                         Log.e("XXX", "onResponse: " + employeerecordid);
                                         Log.e("XXX", "onResponse: " + isPersonGroupIdReg);
                                         Log.e("XXX", "onResponse: " + employeerecordid);
                                         Log.e("XXX", "onResponse: " + empDetail.getIsRegistered());

                                         setErrorMessage("", false);
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

    public class PostRegisterUser extends AsyncTask<Void, Void, Void> {

        private static final String TAG = "PostAttachments";
        Context ctx;
        ProgressDialog progressDialog;

        public PostRegisterUser(Context ctx) {
            this.ctx = ctx;
            bar.setVisibility(View.VISIBLE);
            progressDialog = new ProgressDialog(ctx);
            progressDialog.setMessage("Please wait...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

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
            if (progressDialog.isShowing())
                progressDialog.dismiss();
            AlertDialogs.show(ctx, "Face removed successfully.");

        }


    }

    // endregion API Async Tasks

}