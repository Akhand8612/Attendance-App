package com.eiraj.intel.drone.Utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.eiraj.intel.drone.R;
import com.eiraj.intel.drone.model.SOSModel;

import java.util.List;


/**
 * Created by Eiraj on 11/29/2018.
 */

/**
 *  Modified by Akhand Pratap on 24/07/2022.
 */
public class SOSAdapter extends RecyclerView.Adapter<SOSAdapter.MyViewHolder> {

    Context ctx;
    private List<SOSModel> attendaceList;

    public SOSAdapter(List<SOSModel> attendaceList, Context ctx) {
        this.attendaceList = attendaceList;
        this.ctx = ctx;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.sos_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final SOSModel attendace = attendaceList.get(position);
        holder.name.setText("Contact Person: " + attendace.getName().toString());
        holder.branch.setText("Branch: " + attendace.getBranch().toString());
        holder.imageButtonSos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialContactPhone(attendace.getNumber().toString(), attendace.getEmailId());

            }
        });

    }

    @Override
    public int getItemCount() {
        return attendaceList.size();
    }

    private void dialContactPhone(final String phoneNumber, final String email) {


        ctx.startActivity(new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNumber, null)));
        Intent intent = new Intent("custom-message");
        //            intent.putExtra("quantity",Integer.parseInt(quantity.getText().toString()));
        intent.putExtra("email", email);

        LocalBroadcastManager.getInstance(ctx).sendBroadcast(intent);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name, branch;
        public ImageButton imageButtonSos;

        public MyViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.name);
            branch = (TextView) view.findViewById(R.id.branch);
            imageButtonSos = (ImageButton) view.findViewById(R.id.imageButtonSos);
        }
    }
}