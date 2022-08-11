package com.eiraj.intel.drone.activities.EmployeeDetail;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eiraj.intel.drone.R;
import com.eiraj.intel.drone.adapter.AttendanceReportAdapter;
import com.eiraj.intel.drone.model.AttendanceRecordId;
import com.eiraj.intel.drone.rest.ApiClient;
import com.eiraj.intel.drone.rest.ApiInterface;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.eiraj.intel.drone.Utils.Util.MY_PREFS_NAME;
import static com.eiraj.intel.drone.Utils.Util.internetConnectionAvailable;

public class AttendanceReport extends AppCompatActivity {
    List<AttendanceRecordId> listData;
    String empcode = "";
    String companyId = "";
    ProgressBar bar;
    SharedPreferences prefs;
    private RecyclerView recyclerView;
    private AttendanceReportAdapter attendanceReportAdapter;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_report);
        getId();
        getEmpCode();
        showRecycleOrNot();


    }

    private void showRecycleOrNot() {
        if (internetConnectionAvailable(2000)) {
            bar.setVisibility(View.VISIBLE);
            ApiInterface apiService =
                    ApiClient.getClient().create(ApiInterface.class);
            java.util.Date date = new Date();
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            int month = cal.get(Calendar.MONTH) + 1;

            Call<List<AttendanceRecordId>> call = apiService.GetAttDetails(empcode, String.valueOf(month), companyId);
            call.enqueue(new Callback<List<AttendanceRecordId>>() {
                             @Override
                             public void onResponse(Call<List<AttendanceRecordId>> call, Response<List<AttendanceRecordId>> response) {
                                 Log.w("eiraj", response.toString());
                                 bar.setVisibility(View.INVISIBLE);
// set the adapter

                                 listData = (response.body());


                                 Log.e("XXX", "onResponse: " + listData );

                                 if (response.body() != null) {
                                     attendanceReportAdapter = new AttendanceReportAdapter(AttendanceReport.this, listData);
                                     RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                                     recyclerView.setLayoutManager(mLayoutManager);
                                     recyclerView.setItemAnimator(new DefaultItemAnimator());
                                     recyclerView.addItemDecoration(new DividerItemDecoration(AttendanceReport.this, LinearLayoutManager.VERTICAL));

                                     recyclerView.setAdapter(attendanceReportAdapter);
                                 } else {
                                     Toast.makeText(AttendanceReport.this, "No data coming", Toast.LENGTH_SHORT).show();
                                     Log.e("Eiraj", "No data coming");

                                 }
                                 if (progressDialog.isShowing())
                                     progressDialog.dismiss();

                             }

                             @Override
                             public void onFailure(Call<List<AttendanceRecordId>> call, Throwable t) {
                                 Log.e("Eiraj", "Api not working");
                                 bar.setVisibility(View.INVISIBLE);
                                 if (progressDialog.isShowing())
                                     progressDialog.dismiss();
                             }
                         }
            );

        } else {

            Toast.makeText(AttendanceReport.this, R.string.msg_alert_no_internet, Toast.LENGTH_SHORT).show();

            if (progressDialog.isShowing())
                progressDialog.dismiss();
        }
    }

    private void getEmpCode() {
        Intent intent = this.getIntent();
//
//        if (intent != null) {
//            empCode = intent.getExtras().getString("Empcode");
//        }
        prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
//        Compid=prefs.getString("Compid","");
        empcode = intent.getStringExtra("Empcode");
        companyId = intent.getStringExtra("Compid");
        //companyId=prefs.getString("companyId","");
    }

    private void getId() {
        bar = findViewById(R.id.progressBar);
        recyclerView = (RecyclerView) findViewById(R.id.recycle);

        progressDialog = new ProgressDialog(AttendanceReport.this);
        progressDialog.setMessage("Loading Attendance");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }


}
