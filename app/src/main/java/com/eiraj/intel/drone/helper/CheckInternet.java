package com.eiraj.intel.drone.helper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import com.eiraj.intel.drone.R;

import java.net.InetAddress;

import static androidx.core.net.ConnectivityManagerCompat.RESTRICT_BACKGROUND_STATUS_DISABLED;
import static androidx.core.net.ConnectivityManagerCompat.RESTRICT_BACKGROUND_STATUS_ENABLED;
import static androidx.core.net.ConnectivityManagerCompat.RESTRICT_BACKGROUND_STATUS_WHITELISTED;

public class CheckInternet {

    private Context context;
    private boolean isConnected = false, monitoringConnectivity = false;
    private TextView textView;

    public CheckInternet(Context context) {
        this.context = context;
    }

    public void viewChangesOn(TextView textView) {
        this.textView = textView;
        checkConnectivity();
    }

    public void removeChanges() {
        removeCallbacks();
    }

    private void checkConnectivity() {
        final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        final NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        isConnected = activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();

        if (isConnected) {
            /*if (textView != null) {
                textView.setText("You are online !");
                textView.setBackgroundColor(context.getResources().getColor(R.color.green));
            }*/
        } else {
            try {
                if (textView != null) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                textView.setText("You are offline !");
                                textView.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        connectivityManager.registerNetworkCallback(new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build(), connectivityCallback);
        monitoringConnectivity = true;
    }

    private ConnectivityManager.NetworkCallback connectivityCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(Network network) {
            if (CheckInternet.checkInternetConnection()) {
                isConnected = true;
                try {
                    if (textView != null) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    textView.setText("You are online !");
                                    textView.setBackgroundColor(context.getResources().getColor(R.color.green));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                isConnected = false;
                try {
                    if (textView != null) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    textView.setText("You are offline !");
                                    textView.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onLost(Network network) {
            isConnected = false;
            try {
                if (textView != null) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                textView.setText("You are offline !");
                                textView.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private static boolean checkInternetConnection() {
        boolean connection = false;
        try {
            if (VERSION.SDK_INT >= VERSION_CODES.O) {
                connection = InetAddress.getByName("www.google.com").isReachable(5000);
            } else {
                java.net.InetAddress[] x = java.net.InetAddress.getAllByName("www.google.com");
                if (!x[0].getHostAddress().isEmpty()) {
                    connection = true;
                } else {
                    connection = false;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return connection;
    }

    private void removeCallbacks() {
        if (monitoringConnectivity) {
            final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            connectivityManager.unregisterNetworkCallback(connectivityCallback);
            monitoringConnectivity = false;
        }
    }

    public static int checkStatus(Context context) {
        int status = 0;
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        // Checks if the device is on a metered network
        if (connMgr.isActiveNetworkMetered()) {
            // Checks user’s Data Saver settings.
            if (VERSION.SDK_INT >= VERSION_CODES.N) {
                switch (connMgr.getRestrictBackgroundStatus()) {
                    case RESTRICT_BACKGROUND_STATUS_ENABLED:
                        status = 2;
                        // Background data usage is blocked for this app. Wherever possible,
                        // the app should also use less data in the foreground.
                        break;
                    case RESTRICT_BACKGROUND_STATUS_WHITELISTED:
                        status = 3;
                        // The app is allowed to bypass Data Saver. Nevertheless, wherever possible,
                        // the app should use less data in the foreground and background.
                        break;
                    case RESTRICT_BACKGROUND_STATUS_DISABLED:
                        status = 4;
                        // Data Saver is disabled. Since the device is connected to a
                        // metered network, the app should use less data wherever possible.
                        break;
                }
            }
        } else {
            status = 1;
            // The device is not on a metered network.
            // Use data as required to perform syncs, downloads, and updates.
        }

        return status;
    }
}
