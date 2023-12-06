package com.ma.mediascanner.utils;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.UriPermission;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Environment;
import android.os.FileUtils;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.documentfile.provider.DocumentFile;

import com.tencent.mmkv.MMKV;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class DocumentFileUtils {
    private final static MMKV kv = MMKV.mmkvWithID(DocumentFileUtils.class.getSimpleName());
    public static String getPathFromUri(Context context, Uri uri) {
        String filePath = null;
        if (!DocumentFile.isDocumentUri(context, uri)) {
            if (uri.getPath().contains("tree")){
                DocumentFile documentTreeFile = DocumentFile.fromTreeUri(context, uri);
                String path = uri.getPath();
                if (path.startsWith("/tree/primary:")) {
                    String orgDirPath = path.replace("/tree/primary:", Environment.getExternalStorageDirectory() + "/");
                    int c = 0;
                    for (DocumentFile docu : documentTreeFile.listFiles()){
                        //filePath = orgDirPath+"/"+docu.getName();
                        if (docu.isDirectory()) {
                            c++;
                        }
                        kv.clearAll();
                        kv.encode("subFolderCount", c);
                    }
                    filePath = orgDirPath;
                }
            }
        }else {
            String singlePath = uri.getPath();
            if (singlePath.startsWith("/document/primary:")) {
                filePath = singlePath.replace("/document/primary:", Environment.getExternalStorageDirectory() + "/");
            } else if (singlePath.startsWith("content://")) {
                if (singlePath.contains("com.android.fileexplorer.myprovider/external_files"))
                    filePath = singlePath.replace("content://com.android.fileexplorer.myprovider/external_files", ":" + Environment.getExternalStorageDirectory() + "");
                if (singlePath.contains("com.android.externalstorage.documents/document/primary"))
                    filePath = singlePath.replace("content://com.android.externalstorage.documents/document/primary%3A", Environment.getExternalStorageDirectory()+"%2F");
            }
        }
        return filePath;
    }

    public static boolean isGrantDirPermissionFromUri(Context context, Uri uri) {
        List<UriPermission> uriPermissionList = context.getContentResolver().getPersistedUriPermissions();
        for (UriPermission persistedUriPermission : uriPermissionList) {
           return persistedUriPermission.getUri().equals(uri) && persistedUriPermission.isReadPermission();
        }
        return false;
    }

    /**
     * @param context
     * @param uri
     * @return FilePath
     */
    public static String getFileNameFromContentUri(Context context, Uri uri) {
        if (context == null || uri == null) {
            return null;
        }
        String filePath = null;
        ContentResolver contentResolver = context.getContentResolver();

        try (Cursor cursor = contentResolver.query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index != -1) {
                    String displayName = cursor.getString(index);
                    filePath = displayName;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return filePath;
    }

    public static MMKV getKv() {
        return kv;
    }
}
