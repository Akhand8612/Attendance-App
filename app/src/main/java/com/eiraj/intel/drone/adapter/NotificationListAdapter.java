package com.eiraj.intel.drone.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.eiraj.intel.drone.R;
import com.eiraj.intel.drone.model.NotificationListModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class NotificationListAdapter extends RecyclerView.Adapter<NotificationListAdapter.NotificationViewHolder> {

    private Context context;
    private List<NotificationListModel> list;

    public NotificationListAdapter(Context context, List<NotificationListModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_card, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationListModel data = list.get(position);

        holder.employeeCodeTextView.setText(data.getEmpCode());
        holder.employeeNameTextView.setText(data.getEmpname());
        holder.unitNameTextView.setText(data.getUnitName());
        holder.branchNameTextView.setText(data.getBranchName());
        if (data.getOutDate() != null && data.getOutTime() != null) {
            holder.dateTextView.setText(String.format("%s %s", data.getOutDate(), data.getOutTime()));
        }
        if (data.getInDate() != null && data.getInTime() != null) {
            holder.dateInTextView.setText(String.format("%s %s", data.getInDate(), data.getInTime()));
        }

        if (data.getUserBackInSite()) {
            holder.parentCardView.setStrokeColor(context.getResources().getColor(R.color.green));
        } else {
            holder.parentCardView.setStrokeColor(context.getResources().getColor(R.color.canticle_red));
        }

        //holder.errorTypeImageView.setVisibility(View.VISIBLE);
        //holder.errorTypeTextView.setVisibility(View.VISIBLE);

        if (data.getOutofSite().equalsIgnoreCase("Yes")) {
            holder.runningImageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.warning_icon));
            holder.errorTypeImageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.warning_icon));
            holder.errorTypeTextView.setText("Out Of Site");
        } else if (data.getOutofSite().equalsIgnoreCase("No")) {
            holder.runningImageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.mobile_cut_icon));
            holder.errorTypeImageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.mobile_cut_icon));
            holder.errorTypeTextView.setText("Network N.A");
        } else {
            holder.errorTypeImageView.setVisibility(View.GONE);
            holder.errorTypeTextView.setVisibility(View.GONE);
        }

        holder.callEmployeeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (data.getPhoneNumber() != null && !data.getPhoneNumber().isEmpty()) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + data.getPhoneNumber()));
                    context.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView employeeCodeTextView, employeeNameTextView, unitNameTextView, branchNameTextView, dateTextView, dateInTextView;
        MaterialButton callEmployeeButton;
        MaterialCardView parentCardView;
        ImageView errorTypeImageView, runningImageView;
        TextView errorTypeTextView;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);

            employeeCodeTextView = itemView.findViewById(R.id.employeeCodeTextView);
            employeeNameTextView = itemView.findViewById(R.id.employeeNameTextView);
            unitNameTextView = itemView.findViewById(R.id.unitNameTextView);
            branchNameTextView = itemView.findViewById(R.id.branchNameTextView);
            callEmployeeButton = itemView.findViewById(R.id.callEmployeeButton);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            dateInTextView = itemView.findViewById(R.id.dateInTextView);
            parentCardView = itemView.findViewById(R.id.parentCardView);
            errorTypeImageView = itemView.findViewById(R.id.errorTypeImageView);
            errorTypeTextView = itemView.findViewById(R.id.errorTypeTextView);
            runningImageView = itemView.findViewById(R.id.runningImageView);
        }
    }
}
