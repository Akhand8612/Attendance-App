package com.eiraj.intel.drone.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FaceRecogParamModel {

    @SerializedName("numberUpSample")
    @Expose
    private String numberUpSample;
    @SerializedName("model_recog")
    @Expose
    private String modelRecog;
    @SerializedName("model_reg")
    @Expose
    private String modelReg;
    @SerializedName("faceDetect")
    @Expose
    private String faceDetect;
    @SerializedName("tolerance")
    @Expose
    private String tolerance;
    @SerializedName("numJitters")
    @Expose
    private String numJitters;

    public String getNumberUpSample() {
        return numberUpSample;
    }

    public void setNumberUpSample(String numberUpSample) {
        this.numberUpSample = numberUpSample;
    }

    public String getModelRecog() {
        return modelRecog;
    }

    public void setModelRecog(String modelRecog) {
        this.modelRecog = modelRecog;
    }

    public String getModelReg() {
        return modelReg;
    }

    public void setModelReg(String modelReg) {
        this.modelReg = modelReg;
    }

    public String getFaceDetect() {
        return faceDetect;
    }

    public void setFaceDetect(String faceDetect) {
        this.faceDetect = faceDetect;
    }

    public String getTolerance() {
        return tolerance;
    }

    public void setTolerance(String tolerance) {
        this.tolerance = tolerance;
    }

    public String getNumJitters() {
        return numJitters;
    }

    public void setNumJitters(String numJitters) {
        this.numJitters = numJitters;
    }

}