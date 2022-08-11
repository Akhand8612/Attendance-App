package com.eiraj.intel.drone.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UpdateEmpLocationModel {
    @SerializedName("unitName")
    @Expose
    private String unitName;
    @SerializedName("supFcmToken")
    @Expose
    private String supFcmToken;
    @SerializedName("empName")
    @Expose
    private String empName;
    @SerializedName("branchName")
    @Expose
    private String branchName;
    @SerializedName("branchCode")
    @Expose
    private int branchCode;

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public String getSupFcmToken() {
        return supFcmToken;
    }

    public void setSupFcmToken(String supFcmToken) {
        this.supFcmToken = supFcmToken;
    }

    public String getEmpName() {
        return empName;
    }

    public void setEmpName(String empName) {
        this.empName = empName;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public int getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(int branchCode) {
        this.branchCode = branchCode;
    }

}
