package com.eiraj.intel.drone.Utils;

/**
 * Created by Eiraj on 1/24/2019.
 */

/**
 *  Modified by Akhand Pratap on 24/07/2022.
 */

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

public class MessageDialogFragment extends DialogFragment {
    private String mTitle;
    private String mMessage;
    private MessageDialogListener mListener;

    public static MessageDialogFragment newInstance(String title, String message, MessageDialogListener listener) {
        MessageDialogFragment fragment = new MessageDialogFragment();
        fragment.mTitle = title;
        fragment.mMessage = message;
        fragment.mListener = listener;
        return fragment;
    }

    public void onCreate(Bundle state) {
        super.onCreate(state);
        setRetainInstance(true);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(mMessage)
                .setTitle(mTitle);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (mListener != null) {
                    mListener.onDialogPositiveClick(MessageDialogFragment.this);
                }
            }
        });

        return builder.create();
    }

    public interface MessageDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
    }
}