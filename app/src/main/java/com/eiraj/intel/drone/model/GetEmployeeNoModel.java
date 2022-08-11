package com.eiraj.intel.drone.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class GetEmployeeNoModel {
    @SerializedName("empcode")
    @Expose
    private String empcode;
    @SerializedName("empname")
    @Expose
    private String empname;
    @SerializedName("mobileNo")
    @Expose
    private String mobileNo;

    public String getEmpcode() {
        return empcode;
    }

    public void setEmpcode(String empcode) {
        this.empcode = empcode;
    }

    public String getEmpname() {
        return empname;
    }

    public void setEmpname(String empname) {
        this.empname = empname;
    }

    public String getmobileNo() {
        return mobileNo;
    }

    public void setmobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

}
