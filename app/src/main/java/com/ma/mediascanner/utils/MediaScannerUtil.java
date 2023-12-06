package com.ma.mediascanner.utils;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.util.Log;

import com.tencent.mmkv.MMKV;

import org.apache.tika.Tika;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class MediaScannerUtil {
    private final static MMKV kv = MMKV.mmkvWithID(MediaScannerUtil.class.getSimpleName());
    private final static MultiJarClassLoader jarClassLoader = MultiJarClassLoader.getInstance();
    private final static MediaScannerConnection.OnScanCompletedListener connectionCallback = new MediaScannerConnection.OnScanCompletedListener() {
        @Override
        public void onScanCompleted(String path, Uri uri) {
            System.out.println("扫描完成 --> "+path);
        }
    };
    public static void scanFolder(Context context, String folderPath){
        try {
            if (folderPath == null || !new File(folderPath).isDirectory()) {
                Log.e(MediaScannerUtil.class.getSimpleName(), "The folder path to scan is invalid: " + folderPath);
            } else {

                if (OsUtils.isMIUI()) {
                    ReflectUtil.callStaticObjectMethod(jarClassLoader.loadClass("miui.media.MiuiMediaScannerUtil"),
                            "scanFolder",
                            new Class[]{Context.class, String.class},
                            context, folderPath);
                } else {
                    MediaScannerConnection.scanFile(context, new String[]{folderPath}, null, connectionCallback);
                }
            }
        }catch (Throwable e){
            e.printStackTrace();
        }
    }

    public static void scanSingleFile(Context context, String filePath){
        try {
            if (filePath == null || new File(filePath).isDirectory()) {
                Log.e(MediaScannerUtil.class.getSimpleName(), "The path must be a file path: " + filePath);
            } else {
                if (OsUtils.isMIUI()) {
                    ReflectUtil.callStaticObjectMethod(Class.forName("miui.media.MiuiMediaScannerUtil"),
                            "scanSingleFile",
                            new Class[]{Context.class, String.class},
                            context, filePath);
                } else {
                    MediaScannerConnection.scanFile(context, new String[]{filePath}, null, connectionCallback);
                }
            }
        }catch (Throwable e){
            e.printStackTrace();
        }
    }

    public static MMKV getKv() {
        return kv;
    }
}
