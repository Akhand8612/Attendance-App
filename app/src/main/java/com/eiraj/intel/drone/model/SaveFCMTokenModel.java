package com.eiraj.intel.drone.model;

public class SaveFCMTokenModel {

    Integer CompId;
    String EmpCode;
    String FCMTokenId;

    public SaveFCMTokenModel() {
    }

    public Integer getCompId() {
        return CompId;
    }

    public void setCompId(Integer compId) {
        CompId = compId;
    }

    public String getEmpCode() {
        return EmpCode;
    }

    public void setEmpCode(String empCode) {
        EmpCode = empCode;
    }

    public String getFCMTokenId() {
        return FCMTokenId;
    }

    public void setFCMTokenId(String FCMTokenId) {
        this.FCMTokenId = FCMTokenId;
    }
}
