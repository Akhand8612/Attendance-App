package com.eiraj.intel.drone.Database;

/**
 * Created by nikhil on 2/3/2018.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

//import com.eiraj.intel.drone.model.UserDesignation;

/**
 * Modified by Akhand Pratap Singh on 21/07/2022.
 */

public class DatabaseConnection extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "XE.db";
 //private static final String CREATE_TABLE_UserDesignation = "CREATE TABLE IF NOT EXISTS UserDesignation(desName text,CreatedByUserID text,Desicode text,CompGroupID text,DesiRecordID text,IsActive text,getRemark text);";
   // private static final String CREATE_TABLE_UserDesignations = "CREATE TABLE IF NOT EXISTS User(desName text);";
    private static final String CREATE_TABLE_Store_EmployeeInfo = "CREATE TABLE IF NOT EXISTS EmployeeInfo(Empname text,EmpFHName text,CompName text,EmployeeInfo text);";
    private static final String CREATE_TABLE_Store_AttendancePunchIn = "CREATE TABLE IF NOT EXISTS PunchIn(id text, compGroupID  text,compid  text,unitcode  text,unitLongitude  text,unitLatitude  text,empcode  text,lat  text,longi  text,currentDateAndTime  text,deviceIMEI  text,imageStr  text,createdByUserID  text,isOnline  text,selectedRadio text,post text,type text, face_recog_status text, ml_id text);";
    private static final String CREATE_TABLE_Store_AttendancePunchOut = "CREATE TABLE IF NOT EXISTS Punchout(id text, compGroupID  text,compid  text,unitcode  text,unitLongitude  text,unitLatitude  text,empcode  text,lat  text,longi  text,currentDateAndTime  text,deviceIMEI  text,imageStr  text,createdByUserID  text,isOnline  text,selectedRadio text,post text,type text, face_recog_status text, ml_id text);";
    private static final String CREATE_TABLE_Store_MarkAttendanceInfo = "CREATE TABLE IF NOT EXISTS MarkAttendanceInfo(compGroupID  text,compid  text,unitcode  text,unitLongitude  text,unitLatitude  text,empcode  text,CreatedByUserID text);";
    private static final String CREATE_TABLE_LeaveWeeklyOff = "CREATE TABLE IF NOT EXISTS LWInfo(compGroupID  text,compid  text,unitcode  text,unitLongitude  text,unitLatitude  text,empcode  text,CreatedByUserID text);";
    SQLiteDatabase sq;


    public DatabaseConnection(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sq) {

      //  sq.execSQL(CREATE_TABLE_UserDesignation);
        sq.execSQL(CREATE_TABLE_Store_EmployeeInfo);
        sq.execSQL(CREATE_TABLE_Store_MarkAttendanceInfo);
        sq.execSQL(CREATE_TABLE_Store_AttendancePunchIn);
        sq.execSQL(CREATE_TABLE_Store_AttendancePunchOut);
        //sq.execSQL(CREATE_TABLE_UserDesignations);
        sq.execSQL(CREATE_TABLE_LeaveWeeklyOff);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        sqLiteDatabase.execSQL(CREATE_TABLE_LeaveWeeklyOff);
    }

//    public void insertUserDesignatione(UserDesignation UserDesignation) {
//        sq = this.getReadableDatabase();
//
//        //desName text,CreatedByUserID text,Desicode text,CompGroupID text,DesiRecordID text,IsActive text,getRemark text
//        ContentValues contentValues = new ContentValues();
//        contentValues.put("desName", UserDesignation.getDesiName());
//        contentValues.put("CompGroupID", UserDesignation.getCompGroupID());
//        contentValues.put("Desicode", UserDesignation.getDesicode());
//        contentValues.put("CreatedByUserID", UserDesignation.getCreatedByUserID());
//        contentValues.put("DesiRecordID", UserDesignation.getDesiRecordID());
//        contentValues.put("IsActive", UserDesignation.getIsActive());
//        contentValues.put("getRemark", UserDesignation.getRemark());
//        long i = sq.insert("UserDesignation", null, contentValues);
//
//    }

    public void insertEmployeeInfo(String Empname, String EmpFHName, String CompName, String Empcode) {

        sq = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("Empname", Empname);
        contentValues.put("EmpFHName", EmpFHName);
        contentValues.put("CompName", CompName);
        contentValues.put("EmployeeInfo", Empcode);


        long i = sq.insert("EmployeeInfo", null, contentValues);
        Log.w("Eiraj", String.valueOf(i));

    }

    public void deleteEmployeeInfo() {
        sq = this.getWritableDatabase();
        sq.execSQL("DELETE from EmployeeInfo");
    }

//    public Cursor getUserDesignationSpinner() {
//        sq = this.getReadableDatabase();
//        Cursor res = sq.rawQuery("select 'SELECT DESIGNATION' as desName union all select desName from UserDesignation", null);
//        return res;
//    }
//
//    public Cursor getUserDesignation(String desiName) {
//        sq = this.getReadableDatabase();
//        Cursor res = sq.rawQuery("select * from UserDesignation where desName ='" + desiName + "'", null);
//        return res;
//    }

    public Cursor getEmployeeInfoCount() {
        sq = this.getReadableDatabase();
        Cursor res = sq.rawQuery("select * from EmployeeInfo", null);
        return res;
    }


//    public void deleteUserDesignation() {
//        sq = this.getWritableDatabase();
//        sq.execSQL("DELETE from UserDesignation");
//    }

    public void insertAttendanceDetail(String compGroupID, String compid, String unitcode, String unitLongitude, String unitLatitude, String empcode,
                                       //String desiCode,
                                       String CreatedByUserID) {
        sq = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("compGroupID", compGroupID);
        contentValues.put("compid", compid);
        contentValues.put("unitcode", unitcode);
        contentValues.put("unitLongitude", unitLongitude);
        contentValues.put("unitLatitude", unitLatitude);
        contentValues.put("empcode", empcode);
        //contentValues.put("desiCode", desiCode);

        contentValues.put("CreatedByUserID", CreatedByUserID);
        long i = sq.insert("MarkAttendanceInfo", null, contentValues);

    }

    public Cursor getMarkAttendanceInfo() {
        sq = this.getReadableDatabase();
        Cursor res = sq.rawQuery("select * from MarkAttendanceInfo", null);
        return res;
    }

    public void insertPunchIn(String id, String compGroupID, String compid, String unitcode, String unitLongitude,
                              String unitLatitude, String empcode,
                              //String desiCode,
                              String lat, String longi, String currentDateAndTime,
                              String deviceIMEI, String imageStr, String createdByUserID, String isOnline, String selectedRadio, String faceRecogStatus, String ml_id) {
        sq = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", id); //1
        contentValues.put("compGroupID", compGroupID); //2
        contentValues.put("compid", compid); //3
        contentValues.put("unitcode", unitcode); //4
        contentValues.put("unitLongitude", unitLongitude); //5
        contentValues.put("unitLatitude", unitLatitude); //6
        contentValues.put("empcode", empcode); //7
       // contentValues.put("desiCode", desiCode); //8
        contentValues.put("lat", lat); //9
        contentValues.put("longi", longi); //10
        contentValues.put("currentDateAndTime", currentDateAndTime); //11
        contentValues.put("deviceIMEI", deviceIMEI); //12
        contentValues.put("imageStr", imageStr); //13
        contentValues.put("createdByUserID", createdByUserID); //14
        contentValues.put("isOnline", isOnline); //15
        contentValues.put("selectedRadio", selectedRadio); //16
        contentValues.put("post", "0"); //17
        contentValues.put("type", "Punch In"); //18
        contentValues.put("face_recog_status", faceRecogStatus); //19
        contentValues.put("ml_id", ml_id); //20


        long i = sq.insert("PunchIn", null, contentValues);
    }

    public Cursor getPunchInPost() {

        sq = this.getReadableDatabase();
        Cursor res = sq.rawQuery("select * from PunchIn where post=0 and type like '%Punch In%'", null);
        return res;
    }

    public Cursor getPunchInPost_unRecognizedFaces() {

        sq = this.getReadableDatabase();
        Cursor res = sq.rawQuery("select * from PunchIn where post=0 and type like '%Punch In%' and face_recog_status like 'false'" , null);
        return res;
    }

    public Cursor getPunchInPost_unRecognizedFacesAndRecognizedFaces() {

        sq = this.getReadableDatabase();
        Cursor res = sq.rawQuery("select * from PunchIn where post=0 and type like '%Punch In%' and face_recog_status != 'failed'" , null);
        return res;
    }

    public Cursor getPunchOutPost_unRecognizedFacesAndRecognizedFaces() {

        sq = this.getReadableDatabase();
        Cursor res = sq.rawQuery("select * from PunchIn where post=0 and type like '%Punch Out%' and face_recog_status != 'failed'" , null);
        return res;
    }

    public Cursor getPunchOutPost_unRecognizedFaces() {

        sq = this.getReadableDatabase();
        Cursor res = sq.rawQuery("select * from PunchIn where post=0 and type like '%Punch Out%' and face_recog_status like 'false'" , null);
        return res;
    }

    public Cursor getPunchInPost_RecognizedFaces() {

        sq = this.getReadableDatabase();
        Cursor res = sq.rawQuery("select * from PunchIn where post=0 and type like '%Punch In%' and face_recog_status like 'true'" , null);
        return res;
    }

    public Cursor getPunchOutPost_RecognizedFaces() {

        sq = this.getReadableDatabase();
        Cursor res = sq.rawQuery("select * from PunchIn where post=0 and type like '%Punch Out%' and face_recog_status like 'true'" , null);
        return res;
    }

    public Cursor getPunchOutPost_RecognizedOrUnRecognizedFaces() {

        sq = this.getReadableDatabase();
        Cursor res = sq.rawQuery("select * from PunchIn where post=0 and type like '%Punch Out%' and face_recog_status like 'true' or 'failed'" , null);
        return res;
    }

    public void updatePunchIn(String string) {

        sq = this.getWritableDatabase();
        String query = "Update  PunchIn set post='1' where id= '" + string + "'";
        sq.execSQL(query);

    }

    public void insertPunchOut(String id, String compGroupID, String compid, String unitcode,
                               String unitLongitude, String unitLatitude, String empcode,
                               //String desiCode,
                               String lat, String longi, String currentDateAndTime, String deviceIMEI, String imageStr,
                               String createdByUserID, String isOnline, String selectedRadio, String faceRecogStatus, String ml_id) {
        sq = this.getReadableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", id); //1
        contentValues.put("compGroupID", compGroupID); //2
        contentValues.put("compid", compid); //3
        contentValues.put("unitcode", unitcode); //4
        contentValues.put("unitLongitude", unitLongitude); //5
        contentValues.put("unitLatitude", unitLatitude); //6
        contentValues.put("empcode", empcode); //7
       // contentValues.put("desiCode", desiCode); //8
        contentValues.put("lat", lat); //9
        contentValues.put("longi", longi); //10
        contentValues.put("currentDateAndTime", currentDateAndTime); //11
        contentValues.put("deviceIMEI", deviceIMEI); //12
        contentValues.put("imageStr", imageStr); //13
        contentValues.put("createdByUserID", createdByUserID); //14
        contentValues.put("isOnline", isOnline); //15
        contentValues.put("selectedRadio", selectedRadio); //16
        contentValues.put("post", "0"); //17
        contentValues.put("type", "Punch Out"); //18
        contentValues.put("face_recog_status", faceRecogStatus); //19
        contentValues.put("ml_id", ml_id); //20


        long i = sq.insert("PunchIn", null, contentValues);
    }

    public void updatePunch_faceRecog(String id, String status){
        sq = this.getWritableDatabase();
        String query = "Update  PunchIn set face_recog_status='"+ status +"' where id= '" + id + "'";
        sq.execSQL(query);
    }


    public Cursor getPunchoutPost() {
        sq = this.getReadableDatabase();
        Cursor res = sq.rawQuery("select * from PunchIn where post='0' and type like '%Punch Out%'", null);
        return res;
    }

    public void updatePunchout(String string) {

        sq = this.getWritableDatabase();
        String query = "Update  PunchIn set post='1' where id= '" + string + "'";
        sq.execSQL(query);

    }


    public Cursor getSyncListReport() {
        sq = this.getReadableDatabase();
        Cursor res = sq.rawQuery("select 1 _id, currentDateAndTime as 'Punch in/out time',type as 'Type', face_recog_status from PunchIn where post=0 union select 1 _id, currentDateAndTime as 'Punch in/out time' ,type as 'Type', face_recog_status from PunchIn where post=0", null);
        return res;
    }


    public void deleteAttendanceDetail() {
        sq = this.getWritableDatabase();
        sq.execSQL("DELETE from MarkAttendanceInfo");
    }


    public Cursor checkPunchInThere(String currentTime) {
        sq = this.getReadableDatabase();

        //SQLiteDirectCursorDriver: SELECT ABS(JULIANDAY(currentDateAndTime) - JULIANDAY('2019-03-19 16:53:18.052' ))  < 0.167 from PunchIn where post=0 and type='Punch In'
        Cursor res = sq.rawQuery("SELECT ABS(JULIANDAY(currentDateAndTime) - JULIANDAY('" + currentTime + "' ))  < 0.167 from PunchIn where post=0 and type='Punch In'", null);
        return res;
    }

    public Cursor checkPunchOutThere(String currentTime) {
        sq = this.getReadableDatabase();
        Cursor res = sq.rawQuery("SELECT ABS(JULIANDAY(currentDateAndTime) - JULIANDAY('" + currentTime + "'))  < 0.167 from PunchIn where post=0 and type='Punch Out'", null);
        return res;
    }

    public Cursor checkPunchIn(String currentTime) {
        sq = this.getReadableDatabase();
        Cursor res = sq.rawQuery("SELECT ABS(JULIANDAY(currentDateAndTime) - JULIANDAY(currentTime))  < 0.167 from PunchIn where post=0 and type='Punch Out'", null);
        return res;
    }
}