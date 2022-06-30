package com.ma.mediascanner;

import android.Manifest;
import android.app.AppOpsManager;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;

import com.ma.mediascanner.utils.AppOpsUtils;

import java.io.File;

public class completeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
            try {
                Uri apkUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileProvider",new File("/storage/emulated/0/Android/data/"+BuildConfig.APPLICATION_ID+"/files/"+Environment.DIRECTORY_DOWNLOADS+"/update.apk"));
                MainActivity.installApk(context,apkUri);
                Toast.makeText(context, "下载完成，请点击安装", Toast.LENGTH_SHORT).show();
            } catch (RuntimeException e) {
                Log.e("error", e.getMessage());
            }
        }

    }

}
