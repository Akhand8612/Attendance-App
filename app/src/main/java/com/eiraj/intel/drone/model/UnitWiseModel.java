package com.eiraj.intel.drone.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UnitWiseModel {

    @SerializedName("unitCode")
    @Expose
    private String unitCode;
    @SerializedName("unitName")
    @Expose
    private Object unitName;
    @SerializedName("noOfScans")
    @Expose
    private Object noOfScans;
    @SerializedName("compID")
    @Expose
    private String compID;


    public String getunitCode() {
        return unitCode;
    }

    public void setUnitCode(String empCode) {
        this.unitCode = unitCode;
    }

    public Object getunitName() {
        return unitName;
    }

    public void setunitName(Object empName) {
        this.unitName = unitName;
    }

    public Object getnoOfScans() {
        return noOfScans;
    }

    public void setnoOfScans(Object designation) {
        this.noOfScans = noOfScans;
    }

    public String getcompID() {
        return compID;
    }

    public void setcompID(String attendanceType) {
        this.compID = compID;
    }


}