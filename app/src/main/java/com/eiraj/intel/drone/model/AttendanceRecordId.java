package com.eiraj.intel.drone.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AttendanceRecordId {

    @SerializedName("empCode")
    @Expose
    private String empCode;
    @SerializedName("empName")
    @Expose
    private Object empName;
//    @SerializedName("designation")
//    @Expose
//    private Object designation;
    @SerializedName("attendanceType")
    @Expose
    private String attendanceType;
    @SerializedName("unitName")
    @Expose
    private String unitName;
    @SerializedName("actInTime")
    @Expose
    private String actInTime;
    @SerializedName("punchTime")
    @Expose
    private String punchTime;
    @SerializedName("inPic")
    @Expose
    private Object inPic;
    @SerializedName("inDis")
    @Expose
    private Integer inDis;
    @SerializedName("attRecordID")
    @Expose
    private Integer attRecordID;
    @SerializedName("inOfflineStatus")
    @Expose
    private String inOfflineStatus;
    @SerializedName("outOfflineStatus")
    @Expose
    private String outOfflineStatus;
    @SerializedName("unitCode")
    @Expose
    private String unitCode;

    public String getUnitCode() {
        return unitCode;
    }

    public String getInOfflineStatus() {
        return inOfflineStatus;
    }

    public String getOutOfflineStatus() {
        return outOfflineStatus;
    }

    public String getEmpCode() {
        return empCode;
    }

    public void setEmpCode(String empCode) {
        this.empCode = empCode;
    }

    public Object getEmpName() {
        return empName;
    }

    public void setEmpName(Object empName) {
        this.empName = empName;
    }

//    public Object getDesignation() {
//        return designation;
//    }
//
//    public void setDesignation(Object designation) {
//        this.designation = designation;
//    }

    public String getAttendanceType() {
        return attendanceType;
    }

    public void setAttendanceType(String attendanceType) {
        this.attendanceType = attendanceType;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public String getActInTime() {
        return actInTime;
    }

    public void setActInTime(String actInTime) {
        this.actInTime = actInTime;
    }

    public String getPunchTime() {
        return punchTime;
    }

    public void setPunchTime(String punchTime) {
        this.punchTime = punchTime;
    }

    public Object getInPic() {
        return inPic;
    }

    public void setInPic(Object inPic) {
        this.inPic = inPic;
    }

    public Integer getInDis() {
        return inDis;
    }

    public void setInDis(Integer inDis) {
        this.inDis = inDis;
    }

    public Integer getAttRecordID() {
        return attRecordID;
    }

    public void setAttRecordID(Integer attRecordID) {
        this.attRecordID = attRecordID;
    }

}