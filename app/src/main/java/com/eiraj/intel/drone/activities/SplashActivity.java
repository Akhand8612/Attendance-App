package com.eiraj.intel.drone.activities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.eiraj.intel.drone.R;
import com.eiraj.intel.drone.Utils.Constants;
import com.eiraj.intel.drone.Utils.Theme;
import com.eiraj.intel.drone.activities.Login.LoginActivity;
import com.eiraj.intel.drone.model.FaceRecogParamModel;
import com.eiraj.intel.drone.model.VersionModel;
import com.eiraj.intel.drone.rest.ApiClient;
import com.eiraj.intel.drone.rest.ApiInterface;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.eiraj.intel.drone.Utils.Util.getCurrentVersion;
import static com.eiraj.intel.drone.rest.ApiClient.BASE_URL;

public class SplashActivity extends Activity {
    private static final String TAG = SplashActivity.class.getSimpleName();
    private static final int SPLASH_TIME_OUT = 1500;
    private TextView appVersionText;
    private LinearLayout checkVersionLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Theme.toWhiteStatusBar(SplashActivity.this);

        setAppVersion();

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isNetworkAvailable()) {
                    checkAppVersion();
                } else {
                    Intent i = new Intent(SplashActivity.this, LoginActivity.class);
                    i.putExtra("No", "b");
                    startActivity(i);
                    finish();
                }
            }
        }, SPLASH_TIME_OUT);
    }

    private void setAppVersion() {

        appVersionText = findViewById(R.id.appVersionText);
        checkVersionLayout = findViewById(R.id.checkVersionLayout);
        appVersionText.setText("Drone v" + getCurrentVersion(SplashActivity.this));
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    private void checkAppVersion() {
        checkVersionLayout.setVisibility(View.VISIBLE);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiClient.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                //.client(Constants.okHttpClient_3minTimeout)
                .build();
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);

        Call<List<VersionModel>> getVersion = apiInterface.getVersion();

        getVersion.enqueue(new Callback<List<VersionModel>>() {
            @Override
            public void onResponse(Call<List<VersionModel>> call, Response<List<VersionModel>> response) {

                try {
                    String version = response.body().get(0).getVersionNumber();

                    if (getCurrentVersion(SplashActivity.this).equalsIgnoreCase(version)) {
                        Intent i = new Intent(SplashActivity.this, LoginActivity.class);
                        i.putExtra("No", "b");
                        startActivity(i);
                    } else {
                        appVersionText.setText("Your application " + getCurrentVersion(SplashActivity.this) + " is old please uninstall the old application and download the new application(" + version  +  ") from playstore" + ". आपका एप्लीकेशन पुराना है कृपया पुराना एप्लीकेशन हटाये और नया एप्लीकेशन डाउनलोड करे |");
                        try {
                            AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this, R.style.MyAlertDialogTheme);

                            builder.setTitle("Info")
                                    .setMessage("If you want to update the app then you can?\n" + "अगर आप ऐप को अपडेट करना चाहते हैं तो कर सकते हैं?")
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            try {
                                                final String appPackageName = getPackageName();
                                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                            } catch (android.content.ActivityNotFoundException anfe) {
                                                final String appPackageName = getPackageName();
                                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                            }
                                        }
                                    })
                                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                        }
                                    })
                                    .show();
                        } catch (Exception e) {
                            Intent i = new Intent(SplashActivity.this, LoginActivity.class);
                            i.putExtra("No", "b");
                            startActivity(i);
                            finish();
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(SplashActivity.this, response.code() + "\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(SplashActivity.this, LoginActivity.class);
                    i.putExtra("No", "b");
                    startActivity(i);
                    finish();
                }

                checkVersionLayout.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<List<VersionModel>> call, Throwable t) {
                Toast.makeText(SplashActivity.this, "Network failed.", Toast.LENGTH_SHORT).show();
                checkVersionLayout.setVisibility(View.GONE);
                Intent i = new Intent(SplashActivity.this, LoginActivity.class);
                i.putExtra("No", "b");
                startActivity(i);
                finish();
            }
        });
    }

    private void getFaceRecogParams() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);

        Call<FaceRecogParamModel> getFaceRecognitionParams = apiInterface.getFaceRecognitionParams();

        getFaceRecognitionParams.enqueue(new Callback<FaceRecogParamModel>() {
            @Override
            public void onResponse(Call<FaceRecogParamModel> call, Response<FaceRecogParamModel> response) {
                if (response != null && response.body() != null) {
                    try {

                        if (response.body().getFaceDetect() != null && !response.body().getFaceDetect().isEmpty())
                            Constants.faceDetect = response.body().getFaceDetect();

                        if (response.body().getModelRecog() != null && !response.body().getModelRecog().isEmpty())
                            Constants.model_recog = response.body().getModelRecog();

                        if (response.body().getModelReg() != null && !response.body().getModelReg().isEmpty())
                            Constants.model_reg = response.body().getModelReg();

                        if (response.body().getNumberUpSample() != null && !response.body().getNumberUpSample().isEmpty())
                            Constants.numberUpSample = response.body().getNumberUpSample();

                        if (response.body().getNumJitters() != null && !response.body().getNumJitters().isEmpty())
                            Constants.numJitters = response.body().getNumJitters();

                        if (response.body().getTolerance() != null && !response.body().getTolerance().isEmpty())
                            Constants.tolerance = response.body().getTolerance();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<FaceRecogParamModel> call, Throwable t) {

            }
        });
    }
}




