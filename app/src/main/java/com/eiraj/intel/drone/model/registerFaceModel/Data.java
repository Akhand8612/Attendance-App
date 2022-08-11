package com.eiraj.intel.drone.model.registerFaceModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Data {

    @SerializedName("callAPI")
    @Expose
    private String callAPI;
    @SerializedName("imageName")
    @Expose
    private String imageName;
    @SerializedName("userID")
    @Expose
    private String userID;

    public String getCallAPI() {
        return callAPI;
    }

    public void setCallAPI(String callAPI) {
        this.callAPI = callAPI;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

}
