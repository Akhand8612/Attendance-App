package com.eiraj.intel.drone.Utils.service;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.widget.Toast;

import com.eiraj.intel.drone.activities.EmployeeDetail.Employee_Details;

public class AutoPunchOutService extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {
        Toast.makeText(this, "JOB WORKED", Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }

}

