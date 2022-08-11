package com.eiraj.intel.drone.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class EmpDetail {

    @SerializedName("empId")
    @Expose
    private String empId;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("fatherName")
    @Expose
    private String fatherName;
    @SerializedName("doj")
    @Expose
    private String doj;
    @SerializedName("isActive")
    @Expose
    private String isActive;
    @SerializedName("isPersonGroupIdReg")
    @Expose
    private String isPersonGroupIdReg;
    @SerializedName("empRecordID")
    @Expose
    private String empRecordID;
    @SerializedName("compid")
    @Expose
    private String compid;
    @SerializedName("compGroupID")
    @Expose
    private String compGroupID;
    @SerializedName("isRegistered")
    @Expose
    private String isRegistered;
    @SerializedName("companyShortKey")
    @Expose
    private String companyShortKey;
    @SerializedName("registrationId")
    @Expose
    private String registrationId;

    public String getRegistrationId() {
        return registrationId;
    }

    public String getCompanyShortKey() {
        return companyShortKey;
    }

    public String getIsRegistered() {
        return isRegistered;
    }

    public String getEmpId() {
        return empId;
    }

    public void setEmpId(String empId) {
        this.empId = empId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFatherName() {
        return fatherName;
    }

    public void setFatherName(String fatherName) {
        this.fatherName = fatherName;
    }

    public String getDoj() {
        return doj;
    }

    public void setDoj(String doj) {
        this.doj = doj;
    }

    public String getIsActive() {
        return isActive;
    }

    public void setIsActive(String isActive) {
        this.isActive = isActive;
    }

    public String getIsPersonGroupIdReg() {
        return isPersonGroupIdReg;
    }

    public void setIsPersonGroupIdReg(String isPersonGroupIdReg) {
        this.isPersonGroupIdReg = isPersonGroupIdReg;
    }

    public String getEmpRecordID() {
        return empRecordID;
    }

    public void setEmpRecordID(String empRecordID) {
        this.empRecordID = empRecordID;
    }

    public String getCompid() {
        return compid;
    }

    public void setCompid(String compid) {
        this.compid = compid;
    }

    public String getCompGroupID() {
        return compGroupID;
    }

    public void setCompGroupID(String compGroupID) {
        this.compGroupID = compGroupID;
    }

}