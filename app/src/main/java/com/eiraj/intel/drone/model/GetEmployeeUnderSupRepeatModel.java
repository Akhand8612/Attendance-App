package com.eiraj.intel.drone.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GetEmployeeUnderSupRepeatModel {
    @SerializedName("empcode")
    @Expose
    private String empcode;
    @SerializedName("empname")
    @Expose
    private String empname;

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

}