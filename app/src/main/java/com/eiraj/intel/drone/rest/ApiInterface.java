package com.eiraj.intel.drone.rest;


import com.eiraj.intel.drone.model.AttendanceRecordId;
import com.eiraj.intel.drone.model.EmpDetail;
import com.eiraj.intel.drone.model.EmpDetailPerson;
import com.eiraj.intel.drone.model.EmployeeWithDoubleAttendanceDetailModel;
import com.eiraj.intel.drone.model.FaceRecogParamModel;
import com.eiraj.intel.drone.model.FaceRecognizeModel;
import com.eiraj.intel.drone.model.GetEmployeeNoModel;
import com.eiraj.intel.drone.model.GetEmployeeUnderSupRepeatModel;
import com.eiraj.intel.drone.model.NotificationListModel;
import com.eiraj.intel.drone.model.SOSModel;
import com.eiraj.intel.drone.model.SaveFCMTokenModel;
import com.eiraj.intel.drone.model.SendNotificationModel;
import com.eiraj.intel.drone.model.UnitWiseDetailModel;
import com.eiraj.intel.drone.model.UnitWiseModel;
import com.eiraj.intel.drone.model.UpdateEmpLocationModel;
import com.eiraj.intel.drone.model.UpdateEmployeeStatusPayload;
//import com.eiraj.intel.drone.model.UserDesignation;
import com.eiraj.intel.drone.model.VersionModel;
import com.eiraj.intel.drone.model.registerFaceModel.RegisterFaceModel;
import com.google.gson.JsonObject;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;
import retrofit2.http.Url;


public interface ApiInterface {


    //@GET("MobileApp/GetUserDesignationList?")
//    Call<List<UserDesignation>> GetUserDesignationList(@Query("UnitID") String UnitID, @Query("CompanyGroupID") String CompanyGroupID,
//                                                       @Query("CompanyID") String CompanyID);

    @GET("MobileApp/GetMonthAttDetail?")
    Call<List<AttendanceRecordId>> GetAttDetails(@Query("empcode") String empcode, @Query("month") String month, @Query("CompanyID") String CompanyID);
//
//    @GET("MobileApp/GetUnitWiseLeave?")
//    Call<List<UserDesignation>> GetUserDesignationList(@Query("UnitID") String UnitID, @Query("CompanyGroupID") String CompanyGroupID,
//                                                       @Query("CompanyID") String CompanyID, @Query("Type") String type);

    @GET("MobileApp/GetSOSDetails?")
    Call<List<SOSModel>> GetSOSList();

    @GET("MobileApp/GetUnitWiseList?")
    Call<List<UnitWiseModel>> GetUnitWiseList(@Query("CompGroupID") String CompGroupID, @Query("SupID") String SupID, @Query("date") String date);

    @GET("MobileApp/GetUnitDetailList?")
    Call<List<UnitWiseDetailModel>> GetUnitWiseDetailList(@Query("UnitCode") String UnitCode, @Query("CompGroupID") String CompGroupID, @Query("CompID") String CompID, @Query("date") String date);

    @GET("MobileApp/GetEmployeeUnderSupRepeat?")
    Call<List<GetEmployeeUnderSupRepeatModel>> GetEmployeeUnderSupRepeat(@Query("SupId") String SupId, @Query("date") String date);

    @GET("MobileApp/GetEmployeeUnderSupRepeatDetail?")
    Call<List<EmployeeWithDoubleAttendanceDetailModel>> GetEmployeeUnderSupRepeatDetail(@Query("EmpId") String EmpId, @Query("date") String date);

    @GET("MobileApp/GetEmployeeNotMarked?")
    Call<List<GetEmployeeNoModel>> GetEmployeeNotMarked(@Query("SupId") String SupId, @Query("date") String date);

    @GET("MobileApp/CheckEmpData?")
    Call<List<EmpDetail>> GetEmpDetails(@Query("EmpCode") String empcode);

    @GET("MobileApp/EmpDataUsingFace?")
    Call<List<EmpDetailPerson>> GetEmpDetailsPersonGroupId(@Query("PersonGroupId") String PersonGroupId);

    @GET("MobileApp/GetAPIParameters")
    Call<FaceRecogParamModel> getFaceRecognitionParams();

    @Multipart
    @POST
    Call<FaceRecognizeModel> recognizeFace(@Url String url, @Part MultipartBody.Part user_image, @Part("ID") RequestBody ID);

    @Multipart
    @POST("register_user.php")
    Call<RegisterFaceModel> registerFace(@Part MultipartBody.Part user_image, @Part("ID") RequestBody ID, @Part("clientID") RequestBody clientID);

    @FormUrlEncoded
    @POST("MobileApp/RegisterUser")
    Call<String> registerUserInDB(@Field("EmpRecordID") String EmpRecordID, @Field("Empcode") String Empcode,
                                  @Field("CompGroupID") String CompGroupID, @Field("CompID") String CompID,
                                  @Field("MobileNo") String MobileNo, @Field("ImeiNo") String ImeiNo,
                                  @Field("RegistrationID") String RegistrationID, @Field("registrationId") String registrationId);

    @GET("MobileApp/DoLogin?")
    Call<ResponseBody> loginUser(@Query("uName") String uName, @Query("word") String word);

    @GET("MobileApp/GetVersionDetail")
    Call<List<VersionModel>> getVersion();

    @GET("MobileApp/UpdateEmpLastPostion?")
    Call<UpdateEmpLocationModel> updateEmployeeLocation(@Query("EmpCode") String EmpCode , @Query("LastLat") String LastLat,
                                                        @Query("LastLng") String LastLng, @Query("LastDistance") String LastDistance,
                                                        @Query("LocAddress") String LocAddress, @Query("UnitCode") String UnitCode,
                                                        @Query("CompId") String CompId, @Query("LastBattery") String LastBattery);

    // Send FCM Notifications
    @Headers({"Authorization:key=AAAA5w_60Xo:APA91bFQYHVV2WIZqfXtNGIvmTcB3X_XMRL1my3AFwSUi4mx7XoHmoZYWfgry_dtu_5vTPGDFlFmQ7gYzn2rCGiiviB3dyj99XuNVtOpJJ5jfbVIR1b_w_bgpsqErgQrJpNwgLoyXyXL"})
    @POST("send")
    Call<ResponseBody> sendNotification(@Body JsonObject data);



    // track module


    // save FCM Token
    @POST("EmployeeAppNotification/SaveFCMToken")
    Call<ResponseBody> saveFCMToken(@Body SaveFCMTokenModel saveFCMTokenModel);

    // Alert Supervisor
    @POST("EmployeeAppNotification/SendEmpAppNotification")
    Call<ResponseBody> alertSupervisor(@Body SendNotificationModel sendNotificationModel);

    // Live track radius
    @GET("EmployeeAppNotification/GetDistance")
    Call<ResponseBody> getLiveTrackRadius();

    // Notifications
    @GET("EmployeeAppNotification/GetSentNotificationDt?")
    Call<List<NotificationListModel>> getNotifications(@Query("EmpCode") String empCode, @Query("OutDate") String outDate);

    // Check If supervisor
    @GET("EmployeeAppNotification/GetEmpSupervisorDt?")
    Call<Boolean> checkIfSupervisor(@Query("EmpCode") String empCode);

    // update employee status
    @POST("EmployeeLiveTrack/UpdateEmployeeBatteryStatus?")
    Call<ResponseBody> updateEmployeeBatteryStatus(@Body UpdateEmployeeStatusPayload payload);

    @POST("EmployeeLiveTrack/UpdateEmployeeLocationStatus?")
    Call<ResponseBody> updateEmployeeLocationStatus(@Body UpdateEmployeeStatusPayload payload);

    @POST("EmployeeLiveTrack/UpdateEmployeeLocationStatusOnRequest")
    Call<ResponseBody> updateEmployeeLocationStatusForRequestedTracking(@Body UpdateEmployeeStatusPayload payload);
}





