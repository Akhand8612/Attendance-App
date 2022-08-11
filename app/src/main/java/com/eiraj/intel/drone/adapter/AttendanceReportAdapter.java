package com.eiraj.intel.drone.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.eiraj.intel.drone.R;
import com.eiraj.intel.drone.model.AttendanceRecordId;

import java.util.List;


/**
 * Created by Eiraj on 11/29/2018.
 */

/**
 Modified by Akhand pratap on 24/07/2022.
*/

public class AttendanceReportAdapter extends RecyclerView.Adapter<AttendanceReportAdapter.MyViewHolder> {

    private Context context;
    private List<AttendanceRecordId> attendaceList;

    public AttendanceReportAdapter(Context context, List<AttendanceRecordId> attendaceList) {
        this.context = context;
        this.attendaceList = attendaceList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.attendance_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        AttendanceRecordId attendace = attendaceList.get(position);
        holder.code.setText(String.format("Employee Code : %s", attendace.getEmpCode().toString()));
        if (attendace.getAttendanceType().contains("WO")) {
            holder.punchIn.setText("Weekly off date : " + attendace.getActInTime().toString().replace("T", " "));
            if (attendace.getPunchTime().contains("1900")) {
                holder.punchOut.setText("");
                holder.punchOut.setVisibility(View.GONE);
            } else {
                holder.punchOut.setText("");
                holder.punchOut.setVisibility(View.GONE);

            }
        } else if (attendace.getAttendanceType().contains("WO")) {
            holder.punchIn.setText("Leave date : " + attendace.getActInTime().toString().replace("T", " "));
            if (attendace.getPunchTime().contains("1900")) {
                holder.punchOut.setText("");
                holder.punchOut.setVisibility(View.GONE);
            } else {
                holder.punchOut.setText("");
                holder.punchOut.setVisibility(View.GONE);


            }
        } else {
            try {
                holder.punchIn.setText(String.format("Punch In :    %s", attendace.getActInTime().toString().replace("T", " ").split("\\.")[0]));
                if (attendace.getPunchTime().contains("1900")) {
                    holder.punchOut.setText("Punch Out : ");

                } else {
                    holder.punchOut.setText(String.format("Punch Out : %s", attendace.getPunchTime().toString().replace("T", " ").split("\\.")[0]));
                }
            } catch (Exception e){
                holder.punchIn.setText(String.format("Punch In :   %s", attendace.getActInTime().toString().replace("T", " ")));
                if (attendace.getPunchTime().contains("1900")) {
                    holder.punchOut.setText("Punch Out : ");

                } else {
                    holder.punchOut.setText(String.format("Punch Out : %s", attendace.getPunchTime().toString().replace("T", " ")));
                }
            }
        }
        holder.unitName.setText(String.format("Unit Name : %s(%s)", attendace.getUnitName().toString(), attendace.getUnitCode()));
        holder.attendanceType.setText(attendace.getAttendanceType().toString());

        if (attendace.getAttendanceType().equalsIgnoreCase("Duty")){
            holder.attendanceType.setBackgroundResource(R.drawable.curved_green_background);
        } else {
            holder.attendanceType.setBackgroundResource(R.drawable.curved_blue_background);
        }

        if (attendace.getInOfflineStatus().equalsIgnoreCase("Online")){
            holder.punchIn.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(context, R.drawable.green_circle), null);
        } else {
            holder.punchIn.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(context, R.drawable.red_circle), null);
        }

        if (attendace.getOutOfflineStatus().equalsIgnoreCase("Online")){
            holder.punchOut.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(context, R.drawable.green_circle), null);
        } else {
            holder.punchOut.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(context, R.drawable.red_circle), null);
        }
    }

    @Override
    public int getItemCount() {
        return attendaceList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView code, punchIn, punchOut, unitName, attendanceType;

        public MyViewHolder(View view) {
            super(view);
            code = (TextView) view.findViewById(R.id.code);
            punchIn = (TextView) view.findViewById(R.id.punchIn);
            punchOut = (TextView) view.findViewById(R.id.punchOut);
            unitName = (TextView) view.findViewById(R.id.unitname);
            attendanceType = (TextView) view.findViewById(R.id.attendanceType);
        }
    }
}