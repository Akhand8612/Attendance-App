package com.eiraj.intel.drone.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.eiraj.intel.drone.R;
import com.eiraj.intel.drone.model.GetEmployeeNoModel;

import java.util.List;


/**
 * Created by Eiraj on 11/29/2018.
 */

/**
 *  Modified by Akhand Pratap on 24/07/2022.
 */

public class EmployeeWithNoAttendanceAdapter extends RecyclerView.Adapter<EmployeeWithNoAttendanceAdapter.MyViewHolder> {

    Context ctx;
    String date = "";
    private List<GetEmployeeNoModel> attendaceList;
    public EmployeeWithNoAttendanceAdapter(List<GetEmployeeNoModel> attendaceList, Context ctx, String date) {
        this.attendaceList = attendaceList;
        this.ctx = ctx;
        this.date = date;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.employee_no_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    public void updateList(List<GetEmployeeNoModel> list) {
        attendaceList = list;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final GetEmployeeNoModel attendace = attendaceList.get(position);
        holder.unitCode.setText("Employee Code:" + attendace.getEmpcode().toString());
        holder.unitName.setText("Employee Name:" + attendace.getEmpname().toString());
        holder.forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (attendace.getmobileNo().equals("0")) {
                    Toast.makeText(ctx, "Number not valid", Toast.LENGTH_SHORT).show();
                } else {
                    ctx.startActivity(new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", attendace.getmobileNo(), null)));
                }
            }
        });


    }

    @Override
    public int getItemCount() {
        return attendaceList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView unitCode, noofscan, punchOut, unitName, attendanceType;
        public ImageView forward;

        public MyViewHolder(View view) {
            super(view);
            unitCode = (TextView) view.findViewById(R.id.unitCode);
            noofscan = (TextView) view.findViewById(R.id.noofscan);
            punchOut = (TextView) view.findViewById(R.id.punchOut);
            unitName = (TextView) view.findViewById(R.id.unitname);
            forward = (ImageView) view.findViewById(R.id.forward);
            attendanceType = (TextView) view.findViewById(R.id.attendanceType);
        }
    }
}