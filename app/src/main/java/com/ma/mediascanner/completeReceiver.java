package com.ma.mediascanner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;

import java.io.File;

public class completeReceiver extends BroadcastReceiver {
    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.hashCode() == 1248865515) {
            if (action.equals("android.intent.action.DOWNLOAD_COMPLETE")) {
                try {
                    Toast.makeText(context, "下载完成", Toast.LENGTH_SHORT).show();
                    Uri apkUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileProvider", new File(BaseApplication.APK_DOWN_PATH + "update.apk"));
                    MainActivity.installApk(context, apkUri);
                } catch (RuntimeException e) {
                    Log.e("error", e.getMessage());
                }
            }
        }
    }
}
