package com.eiraj.intel.drone.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SOSModel {

    @SerializedName("branch")
    @Expose
    private String branch;
    @SerializedName("name")
    @Expose
    private Object name;
    @SerializedName("number")
    @Expose
    private Object number;
    @SerializedName("emailId")
    @Expose
    private String emailId;


    public String getBranch() {
        return branch;
    }

    public void setBranch(String empCode) {
        this.branch = empCode;
    }

    public Object getName() {
        return name;
    }

    public void setName(Object empName) {
        this.name = empName;
    }

    public Object getNumber() {
        return number;
    }

  //  public void setNumber(Object designation) {
       // this.number = designation;
 //   }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String attendanceType) {
        this.emailId = attendanceType;
    }


}