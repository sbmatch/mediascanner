package com.ma.mediascanner;

import android.app.Application;

public class BaseApplication extends Application {
    public static final String APK_DOWN_PATH = "/storage/emulated/0/Android/data/"+BuildConfig.APPLICATION_ID+"/files/apk/";
    @Override
    public void onCreate() {
        super.onCreate();

    }
}
