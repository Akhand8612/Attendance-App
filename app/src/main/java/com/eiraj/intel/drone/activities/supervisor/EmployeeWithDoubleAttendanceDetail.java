package com.eiraj.intel.drone.activities.supervisor;

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
import com.eiraj.intel.drone.adapter.EmployeeWithDoubleAttendanceDetailAdapter;
import com.eiraj.intel.drone.model.EmployeeWithDoubleAttendanceDetailModel;
import com.eiraj.intel.drone.rest.ApiClient;
import com.eiraj.intel.drone.rest.ApiInterface;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.eiraj.intel.drone.Utils.Util.MY_PREFS_NAME;
import static com.eiraj.intel.drone.Utils.Util.internetConnectionAvailable;

public class EmployeeWithDoubleAttendanceDetail extends AppCompatActivity {
    List<EmployeeWithDoubleAttendanceDetailModel> listData;
    String EmployeeCode = "";
    String CompID = "";
    String date = "";
    ProgressBar bar;
    private RecyclerView recyclerView;
    private EmployeeWithDoubleAttendanceDetailAdapter employeeWithDoubleAttendanceDetailAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unit_report_detail);
        getId();
        showRecycleOrNot();
    }

    private void getId() {
        bar = findViewById(R.id.progressBar);
        recyclerView = (RecyclerView) findViewById(R.id.recycle);
        Intent intent = getIntent();
        EmployeeCode = intent.getStringExtra("EmployeeCode");

        date = intent.getStringExtra("date");
    }

    private void showRecycleOrNot() {
        if (internetConnectionAvailable(2000)) {
            bar.setVisibility(View.VISIBLE);
            ApiInterface apiService =
                    ApiClient.getClient().create(ApiInterface.class);

            SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
            String CompGroupId = prefs.getString("CompGroupID", "");

            Call<List<EmployeeWithDoubleAttendanceDetailModel>> call = apiService.GetEmployeeUnderSupRepeatDetail(EmployeeCode, date);
            call.enqueue(new Callback<List<EmployeeWithDoubleAttendanceDetailModel>>() {
                             @Override
                             public void onResponse(Call<List<EmployeeWithDoubleAttendanceDetailModel>> call, Response<List<EmployeeWithDoubleAttendanceDetailModel>> response) {
                                 Log.w("eiraj", response.toString());
                                 bar.setVisibility(View.INVISIBLE);


                                 listData = (response.body());


                                 if (response.body() != null) {
                                     employeeWithDoubleAttendanceDetailAdapter = new EmployeeWithDoubleAttendanceDetailAdapter(listData);
                                     RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                                     recyclerView.setLayoutManager(mLayoutManager);
                                     recyclerView.setItemAnimator(new DefaultItemAnimator());
                                     recyclerView.addItemDecoration(new DividerItemDecoration(EmployeeWithDoubleAttendanceDetail.this, LinearLayoutManager.VERTICAL));

                                     recyclerView.setAdapter(employeeWithDoubleAttendanceDetailAdapter);
                                 } else {
                                     Toast.makeText(EmployeeWithDoubleAttendanceDetail.this, "No data coming", Toast.LENGTH_SHORT).show();
                                     Log.e("Eiraj", "No data coming");

                                 }


                             }

                             @Override
                             public void onFailure(Call<List<EmployeeWithDoubleAttendanceDetailModel>> call, Throwable t) {
                                 Log.e("Eiraj", "Api not working");
                                 bar.setVisibility(View.INVISIBLE);
                             }
                         }
            );

        } else {

            Toast.makeText(EmployeeWithDoubleAttendanceDetail.this, R.string.msg_alert_no_internet, Toast.LENGTH_SHORT).show();
        }
    }

}
