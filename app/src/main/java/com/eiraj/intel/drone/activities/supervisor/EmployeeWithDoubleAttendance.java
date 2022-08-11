package com.eiraj.intel.drone.activities.supervisor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eiraj.intel.drone.R;
import com.eiraj.intel.drone.adapter.EmployeeWithDoubleAttendanceAdapter;
import com.eiraj.intel.drone.model.GetEmployeeUnderSupRepeatModel;
import com.eiraj.intel.drone.rest.ApiClient;
import com.eiraj.intel.drone.rest.ApiInterface;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.eiraj.intel.drone.Utils.Util.MY_PREFS_NAME;
import static com.eiraj.intel.drone.Utils.Util.internetConnectionAvailable;

public class EmployeeWithDoubleAttendance extends AppCompatActivity {
    List<GetEmployeeUnderSupRepeatModel> listData;
    String empcode = "";
    String companyId = "";
    ProgressBar bar;
    EditText searchField, dateField;
    Calendar myCalendar;
    SharedPreferences prefs;
    private RecyclerView recyclerView;
    private EmployeeWithDoubleAttendanceAdapter employeeWithDoubleAttendanceAdapter;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(getApplicationContext(), SupervisorMainScreen.class);
        i.putExtra("internet", "c");
        startActivity(i);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_with_double_attendance);
        getId();
        getEmpCode();
        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = df.format(c);
        showRecycleOrNot(formattedDate);

        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                // filter your list from your input
                filter(s.toString());
                //you can use runnable postDelayed like 500 ms to delay search text
            }
        });


    }

    void filter(String text) {
        text = text.toUpperCase();
        List<GetEmployeeUnderSupRepeatModel> temp = new ArrayList();
        for (GetEmployeeUnderSupRepeatModel d : listData) {
            //or use .equal(text) with you want equal match
            //use .toLowerCase() for better matches
            if (d.getEmpname().toString().contains(text)) {
                temp.add(d);
            }
        }
        //update recyclerview
        employeeWithDoubleAttendanceAdapter.updateList(temp);
    }

    private void showRecycleOrNot(final String formattedDate) {
        if (internetConnectionAvailable(2000)) {
            bar.setVisibility(View.VISIBLE);
            ApiInterface apiService =
                    ApiClient.getClient().create(ApiInterface.class);

            SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
            String supId = prefs.getString("SupID", "");
            String CompGroupId = prefs.getString("CompGroupID", "");
            Call<List<GetEmployeeUnderSupRepeatModel>> call = apiService.GetEmployeeUnderSupRepeat(supId, formattedDate);
            call.enqueue(new Callback<List<GetEmployeeUnderSupRepeatModel>>() {
                             @Override
                             public void onResponse(Call<List<GetEmployeeUnderSupRepeatModel>> call, Response<List<GetEmployeeUnderSupRepeatModel>> response) {
                                 Log.w("eiraj", response.toString());
                                 bar.setVisibility(View.INVISIBLE);
// set the adapter

                                 listData = (response.body());


                                 if (response.body() != null) {
                                     employeeWithDoubleAttendanceAdapter = new EmployeeWithDoubleAttendanceAdapter(listData, getApplicationContext(), formattedDate);
                                     RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                                     recyclerView.setLayoutManager(mLayoutManager);
                                     recyclerView.setItemAnimator(new DefaultItemAnimator());
                                     recyclerView.addItemDecoration(new DividerItemDecoration(EmployeeWithDoubleAttendance.this, LinearLayoutManager.VERTICAL));

                                     recyclerView.setAdapter(employeeWithDoubleAttendanceAdapter);
                                 } else {
                                     Toast.makeText(EmployeeWithDoubleAttendance.this, "No data coming", Toast.LENGTH_SHORT).show();
                                     Log.e("Eiraj", "No data coming");

                                 }


                             }

                             @Override
                             public void onFailure(Call<List<GetEmployeeUnderSupRepeatModel>> call, Throwable t) {
                                 Log.e("Eiraj", "Api not working");
                                 bar.setVisibility(View.INVISIBLE);
                             }
                         }
            );

        } else {

            Toast.makeText(EmployeeWithDoubleAttendance.this, R.string.msg_alert_no_internet, Toast.LENGTH_SHORT).show();
        }


    }

    private void getEmpCode() {
//        Intent intent = this.getIntent();
//
//        if (intent != null) {
//            empCode = intent.getExtras().getString("Empcode");
//        }
        prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        empcode = prefs.getString("empcodeattendance", "");
        companyId = prefs.getString("companyId", "");
    }

    private void getId() {
        bar = findViewById(R.id.progressBar);
        recyclerView = (RecyclerView) findViewById(R.id.recycle);
        searchField = (EditText) findViewById(R.id.searchField);
        dateField = (EditText) findViewById(R.id.dateField);
    }
}
