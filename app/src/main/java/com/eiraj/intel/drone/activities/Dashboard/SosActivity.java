package com.eiraj.intel.drone.activities.Dashboard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eiraj.intel.drone.activities.EmployeeDetail.Employee_Details;
import com.eiraj.intel.drone.R;
import com.eiraj.intel.drone.Utils.SOSAdapter;
import com.eiraj.intel.drone.model.SOSModel;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.eiraj.intel.drone.Utils.Util.internetConnectionAvailable;
import static com.eiraj.intel.drone.rest.ApiClient.POST_SEND_EMAIL;

public class SosActivity extends AppCompatActivity {
    ProgressBar bar;
    List<SOSModel> listData;
    String empcode = "";
    SharedPreferences prefs;
    String name = "";
    public BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            String email = intent.getStringExtra("email");
            new SendMail(SosActivity.this, email, empcode, name).execute();
            Toast.makeText(SosActivity.this, email, Toast.LENGTH_SHORT).show();
        }
    };
    private RecyclerView recyclerView;
    private SOSAdapter SOSAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos);
        getId();
        getEmpCode();
        showRecycleOrNot();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("custom-message"));
    }

    private void getId() {
        bar = findViewById(R.id.progressBar);
        recyclerView = (RecyclerView) findViewById(R.id.recycle);
    }

    private void showRecycleOrNot() {
        if (internetConnectionAvailable(2000)) {
            bar.setVisibility(View.VISIBLE);
            ApiInterface apiService =
                    ApiClient.getClient().create(ApiInterface.class);


            Call<List<SOSModel>> call = apiService.GetSOSList();
            call.enqueue(new Callback<List<SOSModel>>() {
                             @Override
                             public void onResponse(Call<List<SOSModel>> call, Response<List<SOSModel>> response) {
                                 Log.e("SOS_API_DATA", response.toString());
                                 bar.setVisibility(View.INVISIBLE);


                                 listData = (response.body());


                                 if (response.body() != null) {
                                     SOSAdapter = new SOSAdapter(listData, SosActivity.this);
                                     RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                                     recyclerView.setLayoutManager(mLayoutManager);
                                     recyclerView.setItemAnimator(new DefaultItemAnimator());
                                     recyclerView.addItemDecoration(new DividerItemDecoration(SosActivity.this, LinearLayoutManager.VERTICAL));

                                     recyclerView.setAdapter(SOSAdapter);
                                 } else {
                                     Toast.makeText(SosActivity.this, "Some error occurred, please try again later.", Toast.LENGTH_SHORT).show();
                                     finish();
                                 }


                             }

                             @Override
                             public void onFailure(Call<List<SOSModel>> call, Throwable t) {
                                 Log.e("SOS_ACT", "Api not working");
                                 bar.setVisibility(View.INVISIBLE);
                             }
                         }
            );

        } else {
            Toast.makeText(SosActivity.this, R.string.msg_alert_no_internet, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void getEmpCode() {


        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        empcode = intent.getStringExtra("empcode");
    }

    class SendMail extends AsyncTask<Void, Void, Void> {


        private static final String TAG = "PostAttachments";
        Context ctx;
        String email, empId, name;

        public SendMail(Context ctx, String email, String empId, String name) {
            this.ctx = ctx;
            bar.setVisibility(View.VISIBLE);
            this.email = email;
            this.empId = empId;
            this.name = name;


        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpClient httpClient = new DefaultHttpClient();
            HttpParams params = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(params, 5000);

            HttpPost httpPost = new HttpPost(POST_SEND_EMAIL);


            List<NameValuePair> nameValuePair = new ArrayList<NameValuePair>();


            nameValuePair.add(new BasicNameValuePair("email", email));
            nameValuePair.add(new BasicNameValuePair("empid", empId));
            nameValuePair.add(new BasicNameValuePair("name", name));

            nameValuePair.add(new BasicNameValuePair("lat", "0"));
            nameValuePair.add(new BasicNameValuePair("long", "0"));


            try {
                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
            } catch (UnsupportedEncodingException e) {
                // writing error to Log
                e.printStackTrace();
            }

            try {
                HttpResponse response = httpClient.execute(httpPost);
                //String dealername, String psrname, String beatname,String selecteddate,String itemname


                String responseBody = EntityUtils.toString(response.getEntity());
                Log.w("eiraj", responseBody);


                responseBody = responseBody.substring(1, responseBody.length() - 2);

                //  databaseConnection.updateFeedback(cursor.getString(0));
                // writing response to log
                Log.d("Http Response:", response.toString());

                final String finalResponseBody = responseBody;
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        new AlertDialog.Builder(ctx)
                                .setTitle("Info")
                                .setMessage((finalResponseBody))
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        Intent i = new Intent(getApplicationContext(), Employee_Details.class);
                                        i.putExtra("internet", "delete post updation");
                                        startActivity(i);
                                    }
                                })

                                .show();

                        // Stuff that updates the UI

                    }
                });
                //  databaseConnection.insertlog(logid,mobno, GetLoginApi.userCode,currdates,version,manufacturer,model,osversion,logstat,GetLoginApi.DataAreaID);


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

            bar.setVisibility(View.INVISIBLE);
            // Its visible
        }

    }


}
