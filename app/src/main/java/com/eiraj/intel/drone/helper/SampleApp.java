
package com.eiraj.intel.drone.helper;

import android.app.Application;
import android.content.Context;

import com.eiraj.intel.drone.Utils.service.PlaySound;


public class SampleApp extends Application {

    private static Context appContext;
    private static CheckInternet checkInternet = null;
    private static PlaySound playSound = null;

    public static Context getContext() {
        return appContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = this;
    }

    public static CheckInternet getCheckInternet(){
        if (checkInternet == null){
            checkInternet = new CheckInternet(appContext);
        }
        return checkInternet;
    }

    public static PlaySound getPlaySound() {
        if (playSound == null) {
            playSound = new PlaySound(appContext);
        }
        return playSound;
    }
}
