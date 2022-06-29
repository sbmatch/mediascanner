package com.ma.mediascanner;

import android.app.Application;
import android.os.Environment;

public class BaseApplication extends Application {
    public static final String APK_DOWN_PATH = "/storage/emulated/0/Android/data/"+BuildConfig.APPLICATION_ID+"/files/"+ Environment.DIRECTORY_DOWNLOADS +"/apk/";
    @Override
    public void onCreate() {
        super.onCreate();

    }
}
