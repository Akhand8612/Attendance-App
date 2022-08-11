package com.eiraj.intel.drone.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class NotificationListModel {

    @SerializedName("empCode")
    @Expose
    private String empCode;
    @SerializedName("empname")
    @Expose
    private String empname;
    @SerializedName("unitBranchCode")
    @Expose
    private String unitBranchCode;
    @SerializedName("unitName")
    @Expose
    private String unitName;
    @SerializedName("branchName")
    @Expose
    private String branchName;
    @SerializedName("outDate")
    @Expose
    private String outDate;
    @SerializedName("outTime")
    @Expose
    private String outTime;
    @SerializedName("inDate")
    @Expose
    private String inDate;
    @SerializedName("inTime")
    @Expose
    private String inTime;
    @SerializedName("phone_number")
    @Expose
    private String phoneNumber;
    @SerializedName("isUserBackInSite")
    @Expose
    private Boolean isUserBackInSite = false;
    @SerializedName("outOfSite")
    @Expose
    private String OutofSite = "";

    public String getEmpCode() {
        return empCode;
    }

    public void setEmpCode(String empCode) {
        this.empCode = empCode;
    }

    public String getEmpname() {
        return empname;
    }

    public void setEmpname(String empname) {
        this.empname = empname;
    }

    public String getUnitBranchCode() {
        return unitBranchCode;
    }

    public void setUnitBranchCode(String unitBranchCode) {
        this.unitBranchCode = unitBranchCode;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getOutDate() {
        return outDate;
    }

    public void setOutDate(String outDate) {
        this.outDate = outDate;
    }

    public String getOutTime() {
        return outTime;
    }

    public void setOutTime(String outTime) {
        this.outTime = outTime;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getInDate() {
        return inDate;
    }

    public void setInDate(String inDate) {
        this.inDate = inDate;
    }

    public String getInTime() {
        return inTime;
    }

    public void setInTime(String inTime) {
        this.inTime = inTime;
    }

    public Boolean getUserBackInSite() {
        return isUserBackInSite;
    }

    public void setUserBackInSite(Boolean userBackInSite) {
        isUserBackInSite = userBackInSite;
    }

    public String getOutofSite() {
        return OutofSite;
    }

    public void setOutofSite(String outofSite) {
        OutofSite = outofSite;
    }
}
