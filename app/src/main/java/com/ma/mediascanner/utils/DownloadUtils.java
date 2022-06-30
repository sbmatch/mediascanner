package com.ma.mediascanner.utils;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.ma.mediascanner.BuildConfig;

import java.lang.reflect.Method;

public class DownloadUtils {

    public static long startDownload(Context context, String apkUrl){
        // 使用反射
        try {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(apkUrl));
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
            request.setTitle("更新包.apk");
            request.setDescription(BuildConfig.APPLICATION_ID +"更新版本");
            request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS,"update.apk");

            Object obj = context.getSystemService("download");
            Class<?> downloadMangerClazz = Class.forName("android.app.DownloadManager");
            Method enqueue = downloadMangerClazz.getDeclaredMethod("enqueue", DownloadManager.Request.class);
            enqueue.setAccessible(true);
            long downloadId = (long) enqueue.invoke(obj,request);
            Toast.makeText(context, "正在下载更新...", Toast.LENGTH_SHORT).show();
            return downloadId;
        } catch (Exception ignored) {}

        return 0;
    }
}
