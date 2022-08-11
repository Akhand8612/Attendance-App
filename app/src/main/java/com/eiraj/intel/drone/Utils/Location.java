package com.eiraj.intel.drone.Utils;

import android.app.Dialog;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.provider.Settings;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class Location {

    private static android.location.Location currentLocation = null;
    private static LocationData currentLocationData = null;
    private static String currentLocationAddress = null;
    private static Dialog locationPermissionDialog = null;

    public static String getAddress(Context context, double LATITUDE, double LONGITUDE) {
        String address = "";
        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null && addresses.size() > 0) {
                address = addresses.get(0).getAddressLine(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return address;
    }

    public static int getLocationMode(Context context) {
        try {
            return Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
        } catch (Exception e) {
            return 0;
        }
    }

    public static class LocationData {
        double latitude, longitude;
        String address;

        public LocationData(double latitude, double longitude, String address) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.address = address;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public String getAddress() {
            return address;
        }
    }

    public interface LocationCallback {
        void onSuccess(LocationData locationData);
    }
}
