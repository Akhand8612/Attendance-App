package com.eiraj.intel.drone.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.eiraj.intel.drone.helper.SampleApp;

public class SharedPrefHelper {

    private SharedPreferences sharedPreferences;
    private static final String initSharedPref = "com.eiraj.intel.drone.session_init";
    private static final String SharedPref_punchLatLng = "com.eiraj.intel.drone.session_punchLatLng";
    private static final String SharedPref_punchAddress = "com.eiraj.intel.drone.session_punchAddress";
    private static final String SharedPref_userRegID = "com.eiraj.intel.drone.session_userRegID";
    private static final String SharedPref_companyShortKey = "com.eiraj.intel.drone.session_companyShortKey";
    private static final String SharedPref_ML_registrationID = "com.eiraj.intel.drone.session_ML_registrationID";
    private static final String SharedPref_punchInStatus = "com.eiraj.intel.drone.punchInStatus";
    private static final String SharedPref_punchOutStatus = "com.eiraj.intel.drone.punchOutStatus";
//       private static final String SharedPref_currentDesignationPosition = "com.eiraj.intel.drone.currentDesignationPosition";
//    private static final String SharedPref_currentDesignationName = "com.eiraj.intel.drone.currentDesignationName";
    private static final String SharedPref_employeeCode = "com.eiraj.intel.drone.employeeCode";
    private static final String SharedPref_companyId = "com.eiraj.intel.drone.companyId";
    private static final String SharedPref_unitCode = "com.eiraj.intel.drone.unitCode";
    private static final String SharedPref_pendingOfflineSync = "com.eiraj.intel.drone.pendingOfflineSync";
    private static final String SharedPref_lastPunchOutTime = "com.eiraj.intel.drone.lastPunchOutTime";
    private static final String SharedPref_lastPunchInDate = "com.eiraj.intel.drone.SharedPref_lastPunchInDate";
    private static final String SharedPref_isAttendanceActive = "com.eiraj.intel.drone.isAttendanceActive";
    private static final String SharedPref_unitLatitude = "com.eiraj.intel.drone.unitLatitude";
    private static final String SharedPref_unitLongitude = "com.eiraj.intel.drone.unitLongitude";
    private static final String SharedPref_previousLongitude = "com.eiraj.intel.drone.previousLongitude";
    private static final String SharedPref_previousLatitude = "com.eiraj.intel.drone.previousLatitude";


    private static final String SharedPref_liveTrackLastLatitude = ".liveTrackLastLatitude";
    private static final String SharedPref_liveTrackLastLongitude = ".liveTrackLastLongitude";
    private static final String SharedPref_isDutyActive = ".isDutyActive";
    private static final String SharedPref_liveTrackRadius = ".liveTrackRadius";
    private static final String SharedPref_isSupervisorNotifiedAboutOutOfRange = ".isSupervisorNotifiedAboutOutOfRange";
    private static final String SharedPref_isSupervisorNotifiedAboutInsideRange = ".isSupervisorNotifiedAboutInsideRange";
    private static final String SharedPref_isLocationServicesEnabled = ".isLocationServicesEnabled";
    private static final String SharedPref_employeeName = Constants.packageName + ".employeeName";
    
    public SharedPrefHelper(){
        sharedPreferences = SampleApp.getContext().getSharedPreferences(initSharedPref, Context.MODE_PRIVATE);
    }

    public void setPunchLatLng(String latLng) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPref_punchLatLng, latLng);
        editor.apply();
    }

    public String getPunchLatLng() {
        return sharedPreferences.getString(SharedPref_punchLatLng, "NA");
    }

    public void setPunchAddress(String address) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPref_punchAddress, address);
        editor.apply();
    }

    public String getPunchAddress() {
        return sharedPreferences.getString(SharedPref_punchAddress, "NA");
    }

    public void setUserRegID(String userRegID) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPref_userRegID, userRegID);
        editor.apply();
    }

    public String getUserRegID() {
        return sharedPreferences.getString(SharedPref_userRegID, "0");
    }

    public void setCompanyShortKey(String companyShortKey) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPref_companyShortKey, companyShortKey);
        editor.apply();
    }

    public String getCompanyShortKey() {
        return sharedPreferences.getString(SharedPref_companyShortKey, "0");
    }

    public void setML_registrationID(String userRegID) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPref_ML_registrationID, userRegID);
        editor.apply();
    }

    public String getML_registrationID() {
        return sharedPreferences.getString(SharedPref_ML_registrationID, "0");
    }

    public void setEmployeeCode(String employeeCode) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPref_employeeCode, employeeCode);
        editor.apply();
    }

    public String getEmployeeCode() {
        return sharedPreferences.getString(SharedPref_employeeCode, "0");
    }

    public void setCompanyID(String companyID) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPref_companyId, companyID);
        editor.apply();
    }

    public String getCompanyID() {
        return sharedPreferences.getString(SharedPref_companyId, "0");
    }

    public void setUnitCode(String unitCode) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPref_unitCode, unitCode);
        editor.apply();
    }

    public String getUnitCode() {
        return sharedPreferences.getString(SharedPref_unitCode, "0");
    }

    public void setPunchInStatus(int status){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(SharedPref_punchInStatus, status);
        editor.apply();
    }

    public int getPunchInStatus(){
        // 0 is for no data or default
        // 1 is for punched in
        // 2 is for not punched in
        return sharedPreferences.getInt(SharedPref_punchInStatus, 0);
    }

    public void setPunchOutStatus(int status){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(SharedPref_punchOutStatus, status);
        editor.apply();
    }

    public int getPunchOutStatus(){
        // 0 is for no data or default
        // 1 is for punched out
        // 2 is for not punched out
        return sharedPreferences.getInt(SharedPref_punchOutStatus, 0);
    }

//    public void setCurrentDesignationName(String name) {
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putString(SharedPref_currentDesignationName, name);
//        editor.apply();
//    }

//    public String getCurrentDesignationName() {
//        return sharedPreferences.getString(SharedPref_currentDesignationName, "null");
//    }

//    public void setCurrentDesignationPosition(int position){
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putInt(SharedPref_currentDesignationPosition, position);
//        editor.apply();
//    }

//    public int getCurrentDesignationPosition(){
//        // 0 is for no data or default
//        return sharedPreferences.getInt(SharedPref_currentDesignationPosition, 0);
//    }

    public void setPendingOfflineSync(boolean status) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SharedPref_pendingOfflineSync, status);
        editor.apply();
    }

    public boolean getPendingOfflineSync() {
        return sharedPreferences.getBoolean(SharedPref_pendingOfflineSync, false);
    }

    public void setLastPunchOutTime(long data){
        SharedPreferences.Editor editor =       sharedPreferences.edit();
        editor.putLong(SharedPref_lastPunchOutTime, data);
        editor.apply();
    }

    public long getLastPunchOutTime(){
        return sharedPreferences.getLong(SharedPref_lastPunchOutTime, 0);
    }

    public void setLastPunchInDate(String data){
        SharedPreferences.Editor editor =       sharedPreferences.edit();
        editor.putString(SharedPref_lastPunchInDate, data);
        editor.apply();
    }

    public String getLastPunchInDate(){
        return sharedPreferences.getString(SharedPref_lastPunchInDate, "0.0");
    }


    public void setAttendanceActive(boolean status) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SharedPref_isAttendanceActive, status);
        editor.apply();
    }

    public boolean isAttendanceActive() {
        return sharedPreferences.getBoolean(SharedPref_isAttendanceActive, false);
    }

    public void setUnitLatitude(String data) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPref_unitLatitude, data);
        editor.apply();
    }

    public String getUnitLatitude() {
        return sharedPreferences.getString(SharedPref_unitLatitude, "0.0");
    }

    public void setUnitLongitude(String data) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPref_unitLongitude, data);
        editor.apply();
    }

    public String getUnitLongitude() {
        return sharedPreferences.getString(SharedPref_unitLongitude, "0.0");
    }

    public void setPreviousLongitude(String data) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPref_previousLongitude, data);
        editor.apply();
    }

    public String getPreviousLongitude() {
        return sharedPreferences.getString(SharedPref_previousLongitude, "0.0");
    }

    public void setPreviousLatitude(String data) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPref_previousLatitude, data);
        editor.apply();
    }

    public String getPreviousLatitude() {
        return sharedPreferences.getString(SharedPref_previousLatitude, "0.0");
    }

    public void setLiveTrackLastLatitude(String data) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPref_liveTrackLastLatitude, data);
        editor.apply();
    }

    public String getLiveTrackLastLatitude() {
        return sharedPreferences.getString(SharedPref_liveTrackLastLatitude, "0.0");
    }

    public void setDutyActive(boolean data) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SharedPref_isDutyActive, data);
        editor.apply();
    }

    public boolean isDutyActive() {
        return sharedPreferences.getBoolean(SharedPref_isDutyActive, false);
    }

    public void setLiveTrackRadius(int data) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(SharedPref_liveTrackRadius, data);
        editor.apply();
    }

    public int getLiveTrackRadius() {
        return sharedPreferences.getInt(SharedPref_liveTrackRadius, 0);
    }

    public void setSupervisorNotifiedAboutOutOfRange(boolean data) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SharedPref_isSupervisorNotifiedAboutOutOfRange, data);
        editor.apply();
    }

    public boolean isSupervisorNotifiedAboutOutOfRange() {
        return sharedPreferences.getBoolean(SharedPref_isSupervisorNotifiedAboutOutOfRange, false);
    }

    public void setSupervisorNotifiedAboutInsideRange(boolean data) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SharedPref_isSupervisorNotifiedAboutInsideRange, data);
        editor.apply();
    }

    public boolean isSupervisorNotifiedAboutInsideRange() {
        return sharedPreferences.getBoolean(SharedPref_isSupervisorNotifiedAboutInsideRange, false);
    }

    public void setLocationServicesEnabled(boolean data) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SharedPref_isLocationServicesEnabled, data);
        editor.apply();
    }

    public boolean isLocationServicesEnabled() {
        return sharedPreferences.getBoolean(SharedPref_isLocationServicesEnabled, false);
    }

    public void setEmployeeName(String data) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPref_employeeName, data);
        editor.apply();
    }

    public void setLiveTrackLastLongitude(String data) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPref_liveTrackLastLongitude, data);
        editor.apply();
    }

    public String getLiveTrackLastLongitude() {
        return sharedPreferences.getString(SharedPref_liveTrackLastLongitude, "0.0");
    }

    public String getEmployeeName() {
        return sharedPreferences.getString(SharedPref_employeeName, "NA");
    }

    public void clearAll(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        SharedPreferences sharedPreferences1 = SampleApp.getContext().getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor1 = sharedPreferences1.edit();
        editor1.clear();
        editor1.apply();
    }
}
