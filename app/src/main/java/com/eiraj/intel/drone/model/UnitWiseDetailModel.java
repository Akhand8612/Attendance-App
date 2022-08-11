package com.eiraj.intel.drone.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UnitWiseDetailModel {
    @SerializedName("empCode")
    @Expose
    private String empCode;
    @SerializedName("empName")
    @Expose
    private String empName;
//    @SerializedName("designation")
//    @Expose
//    private String designation;
    @SerializedName("inTime")
    @Expose
    private String inTime;
    @SerializedName("attRecordID")
    @Expose
    private String attRecordID;

    public String getEmpCode() {
        return empCode;
    }

    public void setEmpCode(String empCode) {
        this.empCode = empCode;
    }

    public String getEmpName() {
        return empName;
    }

    public void setEmpName(String empName) {
        this.empName = empName;
    }

//    public String getDesignation() {
//        return designation;
//    }
//
//    public void setDesignation(String designation) {
//        this.designation = designation;
//    }

    public String getInTime() {
        return inTime;
    }

    public void setInTime(String inTime) {
        this.inTime = inTime;
    }

    public String getAttRecordID() {
        return attRecordID;
    }

    public void setAttRecordID(String attRecordID) {
        this.attRecordID = attRecordID;
    }

}