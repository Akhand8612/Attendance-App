package com.eiraj.intel.drone.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class EmpDetailPerson {

    @SerializedName("empCode")
    @Expose
    private String empCode;
    @SerializedName("personGrpId")
    @Expose
    private Object personGrpId;
    @SerializedName("empName")
    @Expose
    private String empName;
    @SerializedName("empFatherName")
    @Expose
    private String empFatherName;
    @SerializedName("empRecordId")
    @Expose
    private String empRecordId;
    @SerializedName("compName")
    @Expose
    private String compName;
    @SerializedName("compGroupId")
    @Expose
    private String compGroupId;
    @SerializedName("compId")
    @Expose
    private String compId;
    @SerializedName("type")
    @Expose
    private Object type;
    @SerializedName("supId")
    @Expose
    private Object supId;

    public String getEmpCode() {
        return empCode;
    }

    public void setEmpCode(String empCode) {
        this.empCode = empCode;
    }

    public Object getPersonGrpId() {
        return personGrpId;
    }

    public void setPersonGrpId(Object personGrpId) {
        this.personGrpId = personGrpId;
    }

    public String getEmpName() {
        return empName;
    }

    public void setEmpName(String empName) {
        this.empName = empName;
    }

    public String getEmpFatherName() {
        return empFatherName;
    }

    public void setEmpFatherName(String empFatherName) {
        this.empFatherName = empFatherName;
    }

    public String getEmpRecordId() {
        return empRecordId;
    }

    public void setEmpRecordId(String empRecordId) {
        this.empRecordId = empRecordId;
    }

    public String getCompName() {
        return compName;
    }

    public void setCompName(String compName) {
        this.compName = compName;
    }

    public String getCompGroupId() {
        return compGroupId;
    }

    public void setCompGroupId(String compGroupId) {
        this.compGroupId = compGroupId;
    }

    public String getCompId() {
        return compId;
    }

    public void setCompId(String compId) {
        this.compId = compId;
    }

    public Object getType() {
        return type;
    }

    public void setType(Object type) {
        this.type = type;
    }

    public Object getSupId() {
        return supId;
    }

    public void setSupId(Object supId) {
        this.supId = supId;
    }

}