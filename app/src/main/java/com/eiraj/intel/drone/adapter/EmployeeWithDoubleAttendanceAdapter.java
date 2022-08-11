package com.eiraj.intel.drone.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.eiraj.intel.drone.activities.supervisor.EmployeeWithDoubleAttendanceDetail;
import com.eiraj.intel.drone.R;
import com.eiraj.intel.drone.model.GetEmployeeUnderSupRepeatModel;

import java.util.List;


/**
 * Created by Eiraj on 11/29/2018.
 */

/**
 *  Modified by Akhand Pratap on 24/07/2022.
 */

public class EmployeeWithDoubleAttendanceAdapter extends RecyclerView.Adapter<EmployeeWithDoubleAttendanceAdapter.MyViewHolder> {

    Context ctx;
    String date = "";
    private List<GetEmployeeUnderSupRepeatModel> attendaceList;
    public EmployeeWithDoubleAttendanceAdapter(List<GetEmployeeUnderSupRepeatModel> attendaceList, Context ctx, String date) {
        this.attendaceList = attendaceList;
        this.ctx = ctx;
        this.date = date;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.employee_double_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    public void updateList(List<GetEmployeeUnderSupRepeatModel> list) {
        attendaceList = list;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final GetEmployeeUnderSupRepeatModel attendace = attendaceList.get(position);
        holder.unitCode.setText("Employee Code:" + attendace.getEmpcode().toString());
        holder.unitName.setText("Employee Name:" + attendace.getEmpname().toString());


        holder.forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ctx, EmployeeWithDoubleAttendanceDetail.class);
                i.putExtra("EmployeeCode", attendaceList.get(position).getEmpcode());
                i.putExtra("date", date);

                ctx.startActivity(i);
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