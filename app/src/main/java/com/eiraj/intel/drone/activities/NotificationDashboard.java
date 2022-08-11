package com.eiraj.intel.drone.activities;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.eiraj.intel.drone.R;
import com.eiraj.intel.drone.Utils.SharedPrefHelper;
import com.eiraj.intel.drone.adapter.NotificationListAdapter;
import com.eiraj.intel.drone.model.NotificationListModel;
import com.eiraj.intel.drone.rest.ApiClient;
import com.eiraj.intel.drone.rest.ApiInterface;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationDashboard extends AppCompatActivity {

    private RecyclerView notificationsRecyclerView;
    private NotificationListAdapter notificationListAdapter;
    private List<NotificationListModel> list = new ArrayList<>();
    private SharedPrefHelper sharedPrefHelper;
    private ApiInterface apiInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_dashboard);

        init();
    }

    private void init() {
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        sharedPrefHelper = new SharedPrefHelper();

        notificationsRecyclerView = findViewById(R.id.notificationsRecyclerView);

        notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(NotificationDashboard.this, LinearLayoutManager.VERTICAL, false));

        getListFromApi();
    }

    private void getListFromApi() {

        Call<List<NotificationListModel>> getNotifications = apiInterface.getNotifications(sharedPrefHelper.getEmployeeCode(), "null");
        getNotifications.enqueue(new Callback<List<NotificationListModel>>() {
            @Override
            public void onResponse(Call<List<NotificationListModel>> call, Response<List<NotificationListModel>> response) {
                if (response.isSuccessful() && response.body() != null)
                    notificationListAdapter = new NotificationListAdapter(NotificationDashboard.this, response.body());
                notificationsRecyclerView.setAdapter(notificationListAdapter);

            }

            @Override
            public void onFailure(Call<List<NotificationListModel>> call, Throwable t) {

            }
        });
    }
}