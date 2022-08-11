package com.eiraj.intel.drone.Faceui;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.eiraj.intel.drone.Database.DatabaseConnection;
import com.eiraj.intel.drone.R;
import com.eiraj.intel.drone.Utils.AlertDialogs;
import com.eiraj.intel.drone.Utils.Constants;
import com.eiraj.intel.drone.Utils.ImageUtils;
import com.eiraj.intel.drone.Utils.SharedPrefHelper;
import com.eiraj.intel.drone.Utils.Util;
import com.eiraj.intel.drone.model.registerFaceModel.RegisterFaceModel;
import com.eiraj.intel.drone.rest.ApiInterface;
import com.google.android.material.button.MaterialButton;

import java.util.Objects;

import okhttp3.MultipartBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.eiraj.intel.drone.rest.ApiClient.BASE_URL;
import static com.eiraj.intel.drone.rest.ApiClient.BASE_URL_FACE_RECOGNITION;

public class UserFaceRegistration extends AppCompatActivity {

    private static final int REQUEST_SELECT_IMAGE = 0;

    private ImageView image;
    private Button select_image, register;
    private TextView employeeCodeTextView, employeeNameTextView, registrationStatus, info;

    private Bitmap userImage = null;
    private String userName = "", employeeCode = "", userID = "", compGroupID = "", compID = "", fatherName = "", companyShortKey = "", userMLID = "";
    private ApiInterface apiInterface;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_face_registration);

        initView();
        onClickListeners();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_SELECT_IMAGE:
                if (resultCode == RESULT_OK) {

                    // If image is selected successfully, set the image URI and bitmap.

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
                        float scaledWidth = (int)(Width / ratio);

                        // Ratio is calculated
                        scaleWidthRatio = ((float) scaledWidth) / Width;
                        scaleHeightRatio = ((float) scaledHeight) / Height;

                        Matrix matrix = new Matrix();
                        matrix.postScale(scaleWidthRatio, scaleHeightRatio);
                        userImage = Bitmap.createBitmap(mBitmap, 0, 0, Width, Height, matrix, true);

                        imageView.setImageBitmap(userImage);

                        //userImage = mBitmap;
                    }
                }
                break;
            default:
                break;


        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initView() {

        image = findViewById(R.id.image);
        select_image = findViewById(R.id.select_image);
        register = findViewById(R.id.register);
        employeeCodeTextView = findViewById(R.id.employeeCodeTextView);
        employeeNameTextView = findViewById(R.id.employeeNameTextView);
        registrationStatus = findViewById(R.id.registrationStatus);
        info = findViewById(R.id.info);

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.level(HttpLoggingInterceptor.Level.BODY);
        /*OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .connectTimeout(5, TimeUnit.MINUTES)
                .readTimeout(5, TimeUnit.MINUTES)
                .build();*/
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL_FACE_RECOGNITION)
                .addConverterFactory(GsonConverterFactory.create())
                .client(Constants.okHttpClient_3minTimeout)
                .build();
        /*Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL_FACE_RECOGNITION)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();*/
        apiInterface = retrofit.create(ApiInterface.class);

        if (getIntent().hasExtra("userName"))
            userName = getIntent().getStringExtra("userName");
        if (getIntent().hasExtra("employeeCode"))
            employeeCode = getIntent().getStringExtra("employeeCode");
        if (getIntent().hasExtra("userID"))
            userID = getIntent().getStringExtra("userID");
        if (getIntent().hasExtra("compGroupId"))
            compGroupID = getIntent().getStringExtra("compGroupId");
        if (getIntent().hasExtra("compId"))
            compID = getIntent().getStringExtra("compId");
        if (getIntent().hasExtra("fatherName"))
            fatherName = getIntent().getStringExtra("fatherName");
        if (getIntent().hasExtra("companyShortKey"))
            companyShortKey = getIntent().getStringExtra("companyShortKey");

        employeeCodeTextView.setText(employeeCode);
        employeeNameTextView.setText(userName);

        progressDialog = new ProgressDialog(UserFaceRegistration.this);
        progressDialog.setMessage("Registering Face...");
        progressDialog.setCanceledOnTouchOutside(false);
    }

    private void onClickListeners() {

        select_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showInformationDialog();
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (userImage != null) {
                    registerUser();
                } else {
                    Toast.makeText(UserFaceRegistration.this, "Please take your image first.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void showInformationDialog() {
        View view = LayoutInflater.from(UserFaceRegistration.this).inflate(R.layout.suggestion_dialog_face_recognition, null);
        Dialog dialog = new Dialog(UserFaceRegistration.this);

        MaterialButton proceed = view.findViewById(R.id.proceedButton);

        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserFaceRegistration.this, MainActivityPhotoBlink.class);
                startActivityForResult(intent, REQUEST_SELECT_IMAGE);
                dialog.dismiss();
            }
        });

        dialog.setContentView(view);
        dialog.setCanceledOnTouchOutside(false);
        Objects.requireNonNull(dialog.getWindow()).setLayout(-1, -2);
        dialog.show();
    }

    private void registerUser() {
        progressDialog.show();
        MultipartBody.Part imageFile = ImageUtils.handleAndConvertImage(
                UserFaceRegistration.this,
                userImage,
                "photo");

        Log.e("XXX", "registerUser: " + userID );
        Log.e("XXX", "registerUser: " + companyShortKey );

        Call<RegisterFaceModel> registerFace = apiInterface.registerFace(
                imageFile,
                Util.getRequestBody_textPlain(userID),
                Util.getRequestBody_textPlain(companyShortKey));

        registerFace.enqueue(new Callback<RegisterFaceModel>() {
            @Override
            public void onResponse(Call<RegisterFaceModel> call, Response<RegisterFaceModel> response) {
                Log.e("XXX", "onResponse: " + response.body().getCode());
                Log.e("XXX", "onResponse: " + response.body().getMessage());
                Log.e("XXX", "onResponse: " + response.body().getStatus());

                try {
                    Log.e("XXX", "onResponse: " + response.body().getData().getUserID());
                    Log.e("XXX", "onResponse: " + response.body().getData().getCallAPI());
                } catch (Exception e){
                    e.printStackTrace();
                }

                if (progressDialog.isShowing())
                    progressDialog.dismiss();

                if (response.body().getCode().equals("200")) {
                    userMLID = response.body().getData().getUserID();
                    new SharedPrefHelper().setML_registrationID(userMLID);
                    registerUserInDB();
                    progressDialog.setMessage("Updating records...");
                    progressDialog.show();
                } else {
                    AlertDialogs.show(UserFaceRegistration.this, "Face Registration failed, please take a picture again.");
                }
            }

            @Override
            public void onFailure(Call<RegisterFaceModel> call, Throwable t) {
                Log.e("XXX", "onFailure: " + t.getMessage());
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
            }
        });
    }

    private void registerUserInDB() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);

        Log.e("registerUserInDB", "registerUserInDB: " + userID);
        Log.e("registerUserInDB", "registerUserInDB: " + employeeCode);
        Log.e("registerUserInDB", "registerUserInDB: " + compGroupID);
        Log.e("registerUserInDB", "registerUserInDB: " + compID);

        Call<String> registerUserInDB = apiInterface.registerUserInDB(
                userID,
                employeeCode,
                compGroupID,
                compID,
                "",
                Util.getCurrentVersion(UserFaceRegistration.this),
                userMLID,
                userMLID);

        registerUserInDB.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                Log.e("XXX", "onResponse: " + response.body());

                if (progressDialog.isShowing())
                    progressDialog.dismiss();

                if (response.body() != null && !response.body().isEmpty() && response.body().contains("Registration Successful.")
                        || response.body().contains("Employee Duplicate Registration Successful.")) {

                    DatabaseConnection databaseConnection = new DatabaseConnection(UserFaceRegistration.this);
                    databaseConnection.deleteEmployeeInfo();
                    databaseConnection.insertEmployeeInfo(userName, fatherName, getResources().getString(R.string.company_name), employeeCode);

                    showSuccessDialog_Intent_EmployeeDetail();
                } else {
                    if (response.body() != null){
                        AlertDialogs.show(UserFaceRegistration.this, "Registration failed, please try again. Code - " + response.body());
                    } else {
                        AlertDialogs.show(UserFaceRegistration.this, "Registration failed, please try again. Code - BODY_NULL");
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }

    private void showSuccessDialog_Intent_EmployeeDetail() {
        View view = LayoutInflater.from(UserFaceRegistration.this).inflate(R.layout.face_registered_dialog, null);
        Dialog dialog = new Dialog(UserFaceRegistration.this);

        MaterialButton proceed = view.findViewById(R.id.proceedButton);

        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), DashBoardFace.class);
                i.putExtra("isalert", "no");
                startActivity(i);
                dialog.dismiss();
                finish();
            }
        });

        dialog.setContentView(view);
        dialog.setCanceledOnTouchOutside(false);
        Objects.requireNonNull(dialog.getWindow()).setLayout(-1, -2);
        dialog.show();
    }
}