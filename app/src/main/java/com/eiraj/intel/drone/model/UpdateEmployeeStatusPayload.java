package com.eiraj.intel.drone.model;

public class UpdateEmployeeStatusPayload {

    Integer battery;
    Double latitude;
    Double longitude;
    Boolean isLocationPermissionAvailable;
    String unitCode;
    String empCode;
    String location;

    public Integer getBattery() {
        return battery;
    }

    public void setBattery(Integer battery) {
        this.battery = battery;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Boolean getLocationPermissionAvailable() {
        return isLocationPermissionAvailable;
    }

    public void setLocationPermissionAvailable(Boolean locationPermissionAvailable) {
        isLocationPermissionAvailable = locationPermissionAvailable;
    }

    public String getUnitcode() {
        return unitCode;
    }

    public void setUnitcode(String unitcode) {
        unitCode = unitcode;
    }

    public String getEmpCode() {
        return empCode;
    }

    public void setEmpCode(String empCode) {
        this.empCode = empCode;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
