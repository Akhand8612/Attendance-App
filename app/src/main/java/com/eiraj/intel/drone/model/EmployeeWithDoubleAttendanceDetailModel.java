package com.eiraj.intel.drone.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class EmployeeWithDoubleAttendanceDetailModel {
    @SerializedName("empcode")
    @Expose
    private String empcode;
    @SerializedName("dutyInDate")
    @Expose
    private String dutyInDate;
    @SerializedName("dutyOutDate")
    @Expose
    private String dutyOutDate;
    @SerializedName("unitCode")
    @Expose
    private String unitCode;

    public String getEmpcode() {
        return empcode;
    }

    public void setEmpcode(String empcode) {
        this.empcode = empcode;
    }

    public String getDutyInDate() {
        return dutyInDate;
    }

    public void setDutyInDate(String dutyInDate) {
        this.dutyInDate = dutyInDate;
    }

    public String getDutyOutDate() {
        return dutyOutDate;
    }

    public void setDutyOutDate(String dutyOutDate) {
        this.dutyOutDate = dutyOutDate;
    }

    public String getUnitCode() {
        return unitCode;
    }

    public void setUnitCode(String unitCode) {
        this.unitCode = unitCode;
    }

}