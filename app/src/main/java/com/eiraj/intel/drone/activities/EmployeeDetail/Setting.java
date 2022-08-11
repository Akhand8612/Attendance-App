package com.eiraj.intel.drone.activities.EmployeeDetail;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.eiraj.intel.drone.BuildConfig;
import com.eiraj.intel.drone.R;

import java.util.ArrayList;
import java.util.List;

import static com.eiraj.intel.drone.Utils.Util.MY_PREFS_NAME;


public class Setting extends AppCompatActivity {
    Spinner spinner;
    List<String> categories;
    TextView versionNo;
    TextView identify;
    Button deletePerson;
    private ProgressBar bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        getId();
        deletePerson = findViewById(R.id.deletePerson);
        bar = findViewById(R.id.progressBar);
        deletePerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bar.setVisibility(View.VISIBLE);
            }
        });
        setSpinnerData();
        setVersionNo();
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                if (selectedItem.contains("Select")) {
                    selectedItem = "English";
                }
                SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                editor.putString("lang", selectedItem);

                editor.apply();


            } // to close the onItemSelected

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void setVersionNo() {
        int versionCode = BuildConfig.VERSION_CODE;
        String versionName = BuildConfig.VERSION_NAME;
        versionNo.setText("Version Name :" + versionName);
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        final String isclick = prefs.getString("iddelete", "");
        identify.setText(isclick);
    }

    public void getId() {

        spinner = (Spinner) findViewById(R.id.lang);
        versionNo = (TextView) findViewById(R.id.versionNo);
        identify = (EditText) findViewById(R.id.identify);
    }

    private void setSpinnerData() {
        categories = new ArrayList<String>();
        categories.add("Select");
        categories.add("English");
        categories.add("Hindi");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(getApplicationContext(), Employee_Details.class);
        i.putExtra("internet", "");
        startActivity(i);
        finish();
    }


}
