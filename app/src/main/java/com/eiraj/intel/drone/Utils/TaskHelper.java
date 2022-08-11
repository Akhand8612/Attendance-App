package com.eiraj.intel.drone.Utils;

import android.database.Cursor;

import com.eiraj.intel.drone.Database.DatabaseConnection;
import com.eiraj.intel.drone.helper.SampleApp;
import com.eiraj.intel.drone.model.AttendanceRecordId;
import com.eiraj.intel.drone.rest.ApiClient;
import com.eiraj.intel.drone.rest.ApiInterface;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.eiraj.intel.drone.Utils.Util.internetConnectionAvailable;

public class TaskHelper {

    public static void checkPunchStatus(){
        SharedPrefHelper sharedPrefHelper = new SharedPrefHelper();
       // sharedPrefHelper.setPunchOutStatus(false);
       // sharedPrefHelper.setPunchInStatus(false);
        checkOfflinePunches(sharedPrefHelper);
    }

    public static void checkOfflinePunchSync(){
        SharedPrefHelper sharedPrefHelper = new SharedPrefHelper();
        DatabaseConnection databaseConnection = new DatabaseConnection(SampleApp.getContext());
        Cursor punchIn = databaseConnection.getPunchInPost_unRecognizedFaces();
        Cursor punchOut = databaseConnection.getPunchOutPost_unRecognizedFaces();

        if (punchIn.getCount() > 0 || punchOut.getCount() > 0){
            sharedPrefHelper.setPendingOfflineSync(true);
        } else {
            sharedPrefHelper.setPendingOfflineSync(false);
        }

    }

    private static void checkOfflinePunches(SharedPrefHelper sharedPrefHelper){

        DatabaseConnection databaseConnection = new DatabaseConnection(SampleApp.getContext());
        Cursor punchIn = databaseConnection.getPunchInPost_unRecognizedFacesAndRecognizedFaces();
        if (punchIn.getCount() > 0){
          //  sharedPrefHelper.setPunchInStatus(true);
        }

        Cursor punchOut = databaseConnection.getPunchOutPost_unRecognizedFacesAndRecognizedFaces();
        if (punchOut.getCount() > 0){
           // sharedPrefHelper.setPunchOutStatus(true);
        }

        checkOnlinePunches(sharedPrefHelper);
    }

    private static void checkOnlinePunches(SharedPrefHelper sharedPrefHelper){

        if (internetConnectionAvailable(2000)) {
            String empcode = sharedPrefHelper.getEmployeeCode();
            String companyId = sharedPrefHelper.getCompanyID();
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            java.util.Date date = new Date();
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            int month = cal.get(Calendar.MONTH) + 1;
            int day = cal.get(Calendar.DATE);

            Call<List<AttendanceRecordId>> call = apiService.GetAttDetails(empcode, String.valueOf(month), companyId);
            call.enqueue(new Callback<List<AttendanceRecordId>>() {
                @Override
                public void onResponse(Call<List<AttendanceRecordId>> call, Response<List<AttendanceRecordId>> response) {
                    if (response != null){
                        if(response.body() != null){
                            if (response.body().size() > 0){
                                if (response.body().get(0).getActInTime().contains("-" + day)){
                                  //  sharedPrefHelper.setPunchInStatus(true);
                                }
                                if (response.body().get(0).getPunchTime().contains("-" + day)){
                                  //  sharedPrefHelper.setPunchOutStatus(true);
                                }
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<List<AttendanceRecordId>> call, Throwable t) {

                }
            });
        }
    }
}
