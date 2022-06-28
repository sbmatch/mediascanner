//
// Decompiled by Jadx - 500ms
//
package com.ma.mediascanner.utils;

import android.util.Log;

import java.io.IOException;
import java.util.Objects;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class HttpUtils {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String TAG = "HttpUtils";
    private static String reply = null;

    public static String sendGetRequest(String url){

        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(url).build();
                Response response = client.newCall(request).execute();
                reply = Objects.requireNonNull(response.body()).string();
                Log.i(TAG,reply);
            }catch (IOException e){
                Log.e(TAG,e.getMessage());
            }

        }).start();

        return reply;
    }

    public static String sendPostRequest(String url, String json) {
        try {
            OkHttpClient client = new OkHttpClient();
            RequestBody body = RequestBody.create(json, JSON);
            Request request = new Request.Builder().url(url).post(body).build();
            Response response = client.newCall(request).execute();
            return ((ResponseBody) Objects.requireNonNull(response.body())).string();
        }catch (IOException e){
            Log.e(TAG,e.getMessage());
        }
        return null;
    }
}
