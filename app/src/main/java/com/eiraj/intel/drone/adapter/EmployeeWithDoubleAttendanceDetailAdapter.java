package com.eiraj.intel.drone.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.eiraj.intel.drone.R;
import com.eiraj.intel.drone.model.EmployeeWithDoubleAttendanceDetailModel;

import java.util.List;


/**
 * Created by Eiraj on 11/29/2018.
 */

/**
 *  Modified by Akhand Pratap on 24/07/2022.
 */

public class EmployeeWithDoubleAttendanceDetailAdapter extends RecyclerView.Adapter<EmployeeWithDoubleAttendanceDetailAdapter.MyViewHolder> {

    private List<EmployeeWithDoubleAttendanceDetailModel> attendaceList;

    public EmployeeWithDoubleAttendanceDetailAdapter(List<EmployeeWithDoubleAttendanceDetailModel> attendaceList) {
        this.attendaceList = attendaceList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.unit_detail_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    public void updateList(List<EmployeeWithDoubleAttendanceDetailModel> list) {
        attendaceList = list;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        EmployeeWithDoubleAttendanceDetailModel attendace = attendaceList.get(position);
        holder.empCode.setText("Employee Code: " + attendace.getEmpcode().toString());
        holder.empName.setText("Duty In: " + attendace.getDutyInDate().toString());

        //holder.designation.setText("Duty Out: " + attendace.getDutyOutDate().toString());
        holder.inTime.setText("Unit Code: " + attendace.getUnitCode().toString());

    }

    @Override
    public int getItemCount() {
        return attendaceList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView empCode, empName,
               // designation,
                inTime;

        public MyViewHolder(View view) {
            super(view);
            empCode = (TextView) view.findViewById(R.id.empCode);
            empName = (TextView) view.findViewById(R.id.empName);
            //designation = (TextView) view.findViewById(R.id.designation);
            inTime = (TextView) view.findViewById(R.id.inTime);

        }
    }
}