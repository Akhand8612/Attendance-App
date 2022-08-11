package com.eiraj.intel.drone.Utils;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;

import com.eiraj.intel.drone.R;

public class AlertDialogs {

    public static void show(final Context context, String message) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.company_name));
        builder.setMessage(message);
        builder.setPositiveButton("OK", null);
        builder.setNegativeButton("Cancel", null);

        final AlertDialog alertDialog = builder.create();

        /*alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                try {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                            .getBackground().setColorFilter(ContextCompat.getColor(context, R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
                    alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                            .getBackground().setColorFilter(ContextCompat.getColor(context, R.color.white), PorterDuff.Mode.MULTIPLY);
                    alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(context.getResources().getColor(R.color.notUploadedGrey));
                    TextView textView = (TextView) alertDialog.findViewById(android.R.id.message);
                    Typeface face = ResourcesCompat.getFont(context, R.font.opensans_regular);
                    textView.setTypeface(face);
                } catch (Exception e) {
                }
            }
        });*/

        try {
            alertDialog.show();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
