package com.eiraj.intel.drone.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class VersionModel {

    @SerializedName("versionNo")
    @Expose
    private String versionNo;
    @SerializedName("versionNumber")
    @Expose
    private String versionNumber;
    @SerializedName("ver_Description")
    @Expose
    private String verDescription;

    public String getVersionNo() {
        return versionNo;
    }

    public void setVersionNo(String versionNo) {
        this.versionNo = versionNo;
    }

    public String getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
    }

    public String getVerDescription() {
        return verDescription;
    }

    public void setVerDescription(String verDescription) {
        this.verDescription = verDescription;
    }

}
