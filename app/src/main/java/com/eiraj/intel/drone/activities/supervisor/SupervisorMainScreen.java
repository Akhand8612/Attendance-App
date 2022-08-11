package com.eiraj.intel.drone.activities.supervisor;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.eiraj.intel.drone.Database.DatabaseConnection;
import com.eiraj.intel.drone.R;

public class SupervisorMainScreen extends AppCompatActivity {
    CardView unitReport, employeeWithDoubleAttendance, employeeWithNoAttendance, scanUnit;
    DatabaseConnection databaseConnection;
    String empName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supervisor_main_screen);
        unitReport = findViewById(R.id.unitReport);
        employeeWithNoAttendance = findViewById(R.id.employeeWithNoAttendance);
        databaseConnection = new DatabaseConnection(SupervisorMainScreen.this);
        employeeWithDoubleAttendance = findViewById(R.id.employeeWithDoubleAttendance);
        scanUnit = findViewById(R.id.scanUnit);

        employeeInfoCount();
        employeeWithDoubleAttendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), EmployeeWithDoubleAttendance.class);
                i.putExtra("internet", "c");
                startActivity(i);
                finish();
            }
        });
        scanUnit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), SupervisorDashBoard.class);
                i.putExtra("internet", "c");
                startActivity(i);
                finish();
            }
        });
        employeeWithNoAttendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), EmployeeWithNoAttendance.class);
                i.putExtra("internet", "c");
                startActivity(i);
                finish();
            }
        });
        unitReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), UnitReport.class);
                i.putExtra("internet", "c");
                startActivity(i);
                finish();
            }
        });
    }

    private void employeeInfoCount() {
        final Cursor res = databaseConnection.getEmployeeInfoCount();
        TextView welcome = findViewById(R.id.welcome);
        int numRows = res.getCount();
        if (numRows > 0) {
            res.moveToFirst();

            empName = res.getString(0);
            welcome.setText("Welcome," + empName);
        }
    }
}
