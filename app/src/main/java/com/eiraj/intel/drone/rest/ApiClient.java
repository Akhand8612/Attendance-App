package com.eiraj.intel.drone.rest;


import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class ApiClient {

    //public static final String BASE_URL = "http://119.81.99.46:85/api/MobileApp/"; // OLD URL
    public static final String BASE_URL = "http://103.20.212.78:94/api/"; // PROD URL
    //public static final String BASE_URL = "http://192.168.1.7:81/api/MobileApp/"; // DEV URL (AMAR)
    public static final String BASE_URL_FACE_RECOGNITION = "http://103.20.212.167/recog_bot/api/";
    public static final String BASE_URL_FCM = "https://fcm.googleapis.com/fcm/";
    private static Retrofit retrofit = null;


    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    // URLs used by Https Client Methods
    public static final String POST_UNIT_DATA = BASE_URL + "MobileApp/UpdateUnitCoordinate";
    public static final String POST_REGISTER_USER = BASE_URL +  "MobileApp/RegisterUser";
    public static final String POST_ATTENDANCE_IN = BASE_URL +  "MobileApp/EmpAttIn";
    public static final String POST_ATTENDANCE_OUT = BASE_URL +  "MobileApp/EmpAttOut";
    public static final String POST_UNBLOCK_USER = BASE_URL +  "MobileApp/UnblockUser";
    public static final String POST_APPLY_LEAVE = BASE_URL +  "MobileApp/LeaveApply";
    public static final String POST_LOGIN_USER = BASE_URL +  "MobileApp/DoLogin?";
    public static final String GET_VERSION = BASE_URL +  "MobileApp/GetVersionDetail";
    public static final String POST_SEND_EMAIL = BASE_URL +  "MobileApp/SendEmail";
    public static final String POST_UPDATE_GROUP_ID = BASE_URL +  "MobileApp/UpdatePersonGroupId";
}