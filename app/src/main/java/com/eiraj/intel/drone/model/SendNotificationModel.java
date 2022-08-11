package com.eiraj.intel.drone.model;

public class SendNotificationModel {

    Integer CompId;
    String UnitCode;
    String EmpCode;
    String title;
    String message;
    String notificationType;
    Boolean userInRange;

    public SendNotificationModel() {
    }

    public Integer getCompId() {
        return CompId;
    }

    public void setCompId(Integer compId) {
        CompId = compId;
    }

    public String getUnitCode() {
        return UnitCode;
    }

    public void setUnitCode(String unitCode) {
        UnitCode = unitCode;
    }

    public String getEmpCode() {
        return EmpCode;
    }

    public void setEmpCode(String empCode) {
        EmpCode = empCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public Boolean getUserInRange() {
        return userInRange;
    }

    public void setUserInRange(Boolean userInRange) {
        this.userInRange = userInRange;
    }
}
