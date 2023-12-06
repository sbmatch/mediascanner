package com.ma.mediascanner.app;

import android.app.Application;
import android.content.Context;
import android.os.Build;

import com.tencent.mmkv.MMKV;

import org.lsposed.hiddenapibypass.HiddenApiBypass;

public class BaseApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        String rootPath = MMKV.initialize(this);
        System.out.println("mmkv init path: "+rootPath);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            HiddenApiBypass.addHiddenApiExemptions("");
        }
    }
}
