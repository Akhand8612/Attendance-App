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
import com.eiraj.intel.drone.adapter.UnitReportDetailAdapter;
import com.eiraj.intel.drone.model.UnitWiseDetailModel;
import com.eiraj.intel.drone.rest.ApiClient;
import com.eiraj.intel.drone.rest.ApiInterface;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.eiraj.intel.drone.Utils.Util.MY_PREFS_NAME;
import static com.eiraj.intel.drone.Utils.Util.internetConnectionAvailable;

public class UnitReportDetail extends AppCompatActivity {
    List<UnitWiseDetailModel> listData;
    String unitCode = "";
    String CompID = "";
    String date = "";
    ProgressBar bar;
    private RecyclerView recyclerView;
    private UnitReportDetailAdapter unitReportDetailAdapter;

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
        unitCode = intent.getStringExtra("UnitCode");
        CompID = intent.getStringExtra("CompID");
        date = intent.getStringExtra("date");
    }

    private void showRecycleOrNot() {
        if (internetConnectionAvailable(2000)) {
            bar.setVisibility(View.VISIBLE);
            ApiInterface apiService =
                    ApiClient.getClient().create(ApiInterface.class);

            SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
            String CompGroupId = prefs.getString("CompGroupID", "");

            Call<List<UnitWiseDetailModel>> call = apiService.GetUnitWiseDetailList(unitCode, CompGroupId, CompID, date);
            call.enqueue(new Callback<List<UnitWiseDetailModel>>() {
                             @Override
                             public void onResponse(Call<List<UnitWiseDetailModel>> call, Response<List<UnitWiseDetailModel>> response) {
                                 Log.w("eiraj", response.toString());
                                 bar.setVisibility(View.INVISIBLE);


                                 listData = (response.body());


                                 if (response.body() != null) {
                                     unitReportDetailAdapter = new UnitReportDetailAdapter(listData);
                                     RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                                     recyclerView.setLayoutManager(mLayoutManager);
                                     recyclerView.setItemAnimator(new DefaultItemAnimator());
                                     recyclerView.addItemDecoration(new DividerItemDecoration(UnitReportDetail.this, LinearLayoutManager.VERTICAL));

                                     recyclerView.setAdapter(unitReportDetailAdapter);
                                 } else {
                                     Toast.makeText(UnitReportDetail.this, "No data coming", Toast.LENGTH_SHORT).show();
                                     Log.e("Eiraj", "No data coming");

                                 }


                             }

                             @Override
                             public void onFailure(Call<List<UnitWiseDetailModel>> call, Throwable t) {
                                 Log.e("Eiraj", "Api not working");
                                 bar.setVisibility(View.INVISIBLE);
                             }
                         }
            );

        } else {

            Toast.makeText(UnitReportDetail.this, R.string.msg_alert_no_internet, Toast.LENGTH_SHORT).show();
        }
    }

}
