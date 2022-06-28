package com.ma.mediascanner;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.alibaba.fastjson2.JSONObject;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.ma.mediascanner.databinding.ActivityScrollingBinding;
import com.ma.mediascanner.utils.GsonUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "SrcJson";
    private static BroadcastReceiver completeReceiver;
    private ActivityScrollingBinding binding;
    private final Context context = this;

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityScrollingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        FloatingActionButton fab = binding.fab;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id){
            case (int)R.id.action_settings:
                break;
            case (int)R.id.action_update:
                checkUpdate();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkUpdate() {
        Toast.makeText(this, "正在检查新版本...", Toast.LENGTH_SHORT).show();
        loadJson("https://sbmatch.github.io/mediascanner/app/release/output-metadata.json");
        loadJson("https://tenapi.cn/lanzou/?url=https://giaosha.lanzoul.com/i2nQ5072m8sd");
    }

    public void loadJson(String url) {

        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(url).build();
                Response response = client.newCall(request).execute();

                if (url.equals("https://tenapi.cn/lanzou/?url=https://wwn.lanzouj.com/i8TLi06zhs1i")){
                    JSONObject update_data_json = JSONObject.parseObject(Objects.requireNonNull(response.body()).string());
                    String down_url = update_data_json.getJSONObject("data").getString("url");
                    if (down_url != null){
                        saveInfo("url",down_url);
                    }
                }else {

                    Message msg = new Message();
                    Bundle data = new Bundle();
                    data.putString("value", Objects.requireNonNull(response.body()).string());
                    msg.setData(data);
                    handler.sendMessage(msg);
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG,e.getMessage(),e.fillInStackTrace());
            }
        }).start();

    }

   public Handler handler = new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            Bundle data = msg.getData();
            String val = data.getString("value");
            JSONObject update_data_json = JSONObject.parseObject(val);

            try {

                Object i = GsonUtils.toJson(update_data_json.get("elements")); // Gson ！使用反序列化
                int codeVer = JSONObject.parseObject(new org.json.JSONArray(i+"").getString(0)).getIntValue("versionCode");

                if (BuildConfig.VERSION_CODE == codeVer){
                    Toast.makeText(context, "您已经是最新版本了", Toast.LENGTH_SHORT).show();
                    saveInfo("vercode",codeVer+"");
                }else {
                    startDown(getInfo("vercode"),getInfo("url"));
                }

            }catch (Exception e){

                Log.e(TAG,e.getMessage(),e.fillInStackTrace());

            }
        }
    };



    public void saveInfo(String key ,String val) {
        SharedPreferences info = getSharedPreferences(key,0);
        SharedPreferences.Editor editor = info.edit();
        editor.putString(key,val);
        editor.apply();
        Log.i(key, "保存信息成功");
    }

    public String getInfo(String key) {
        return getSharedPreferences(key, 0).getString("conf","");
    }


    public void startDown(String verCode,String url) {
        mShowDialog(context,false,"发现新版本","版本号："+verCode,url,"更新","取消");
    }

    public static void mShowDialog(final Context context, boolean isCancel, String title, String msg, String srcUrl, String positive, String negative) {
        new MaterialAlertDialogBuilder(context).setTitle(title).setMessage(msg).setCancelable(isCancel).setPositiveButton(positive, (dialog, which) -> {

            if ( srcUrl != null) {
                DownloadManager downloadManager = (DownloadManager) context.getSystemService("download");
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(srcUrl)).setTitle(context.getString(R.string.app_name) + ".apk").setDestinationInExternalFilesDir(context, null, "apk/update.apk");
                Toast.makeText(context, "正在下载更新...", Toast.LENGTH_SHORT).show();
                long downloadId = downloadManager.enqueue(request);

                IntentFilter filter = new IntentFilter();
                filter.addAction("android.intent.action.DOWNLOAD_COMPLETE");
                context.registerReceiver(new completeReceiver(), filter);
            }


        }).setNegativeButton(negative, (dialog, which) -> {

        }).show();
    }

    public static void installApk(Context c, Uri uri) {
        try {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            intent.setData(uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            c.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}