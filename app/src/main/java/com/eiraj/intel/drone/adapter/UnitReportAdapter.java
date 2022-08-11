package com.eiraj.intel.drone.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.eiraj.intel.drone.activities.supervisor.UnitReportDetail;
import com.eiraj.intel.drone.R;
import com.eiraj.intel.drone.model.UnitWiseModel;

import java.util.List;


/**
 * Created by Eiraj on 11/29/2018.
 */

/**
 *  Modified by Akhand Pratap on 24/07/2022.
 */

public class UnitReportAdapter extends RecyclerView.Adapter<UnitReportAdapter.MyViewHolder> {

    Context ctx;
    String date = "";
    private List<UnitWiseModel> attendaceList;
    public UnitReportAdapter(List<UnitWiseModel> attendaceList, Context ctx, String date) {
        this.date = date;

        this.attendaceList = attendaceList;
        this.ctx = ctx;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.unit_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    public void updateList(List<UnitWiseModel> list) {
        attendaceList = list;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        final UnitWiseModel attendace = attendaceList.get(position);
        holder.unitCode.setText("Unit Code:" + attendace.getunitCode().toString());
        holder.unitName.setText("Unit Name:" + attendace.getunitName().toString());

        holder.noofscan.setText("No of scans:" + attendace.getnoOfScans().toString());


        holder.forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ctx, UnitReportDetail.class);
                i.putExtra("UnitCode", attendaceList.get(position).getunitCode());
                i.putExtra("CompID", attendaceList.get(position).getcompID());
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