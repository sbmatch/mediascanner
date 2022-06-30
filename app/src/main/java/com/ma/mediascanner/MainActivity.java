package com.ma.mediascanner;

import android.Manifest;
import android.app.AppOpsManager;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson2.JSONObject;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;
import com.ma.mediascanner.databinding.ActivityScrollingBinding;
import com.ma.mediascanner.utils.AppOpsUtils;
import com.ma.mediascanner.utils.DownloadUtils;
import com.ma.mediascanner.utils.GsonUtils;
import com.ma.mediascanner.utils.HttpUtils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "SrcJson";
    private static final String apkUrl = "https://giaosha.lanzoul.com/i2nQ5072m8sd";
    private ActivityScrollingBinding binding;
    private final Context context = this;
    private MaterialTextView info;
    private MaterialCardView cardView;
    private completeReceiver receiver;
    private MaterialButton materialButton;

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityScrollingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        cardView = binding.card;
        info = binding.textm;
        materialButton = binding.bt;

        loadJson("https://tenapi.cn/lanzou/?url="+apkUrl);

        String s = "context.getExternalFilesDir(null) --------------> "+ context.getExternalFilesDir(null)
                +"\nEnvironment.getExternalStoragePublicDirectoryEnvironment.DIRECTORY_DOWNLOADS) -------------> "+Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                +"\nEnvironment.getExternalStorageDirectory() --------------->"+ Environment.getExternalStorageDirectory();

        String sl = "1. 更新版本号\n2. 更新好多bug";
        printUpdateLog(BuildConfig.VERSION_CODE,sl);

        receiver= new completeReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        intentFilter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED);
        registerReceiver(receiver,intentFilter);

        materialButton.setOnClickListener(this);
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
            case (int)R.id.action_update:
                checkUpdate();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkUpdate() {
        Toast.makeText(this, "正在检查新版本...", Toast.LENGTH_SHORT).show();

        loadJson("https://privacy.mpcloud.top/appUpdataLog.json");
    }

    public void printUpdateLog(int ver,String log) {
        JSONObject upJson = new JSONObject();
        upJson.put("versionCode",ver);
        upJson.put("upLog",log);
        String json = GsonUtils.toJson(upJson);
        Log.e("log",json);
        info.setText(json);

    }

    public void loadJson(String url) {

        new Thread(() -> {
            Looper.prepare();
            try {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(url).build();
                Response response = client.newCall(request).execute();

                if (url.equals("https://tenapi.cn/lanzou/?url="+apkUrl)){
                    JSONObject update_data_json = JSONObject.parseObject(Objects.requireNonNull(response.body()).string());
                    String down_url = update_data_json.getJSONObject("data").getString("url");
                    if (!down_url.isEmpty()){
                        saveInfo("url", URLEncoder.encode(down_url,"utf-8"));
                    }else{
                        loadJson("https://api.dzzui.com/api/lanzoujx?url="+apkUrl);
                    }
                }

                if (url.equals("https://api.dzzui.com/api/lanzoujx?url="+apkUrl)){
                    JSONObject update_data_json = JSONObject.parseObject(Objects.requireNonNull(response.body()).string());
                    int status = update_data_json.getIntValue("status");
                    String down_url = update_data_json.getString("info");
                    if (status != 0){
                        saveInfo("url", URLEncoder.encode(down_url,"utf-8"));
                    }else{
                        // 可拓展接口
                    }
                }

                if (url.equals("https://privacy.mpcloud.top/appUpdataLog.json"))
                {
                    try {
                        JSONObject update_data_json = JSONObject.parseObject(Objects.requireNonNull(response.body()).string());
                        int codeVer = update_data_json.getIntValue("versionCode");
                        String upLog = update_data_json.getString("upLog");

                        saveInfo("vercode", codeVer + ""); //保存远程版本号

                        if (BuildConfig.VERSION_CODE == codeVer) {
                            Toast.makeText(context, "您已经是最新版本了", Toast.LENGTH_SHORT).show();
                        } else {
                            Message msg = new Message();
                            Bundle data = new Bundle();
                            data.putString("verCode", getInfo("vercode"));
                            data.putString("log",upLog);
                            msg.setData(data);
                            handler.sendMessage(msg);
                        }

                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage(), e.fillInStackTrace());
                    }
                }


             /*   if (url.equals("https://sbmatch.github.io/mediascanner/app/release/output-metadata.json"))
                {
                    try {
                            JSONObject update_data_json = JSONObject.parseObject(Objects.requireNonNull(response.body()).string());
                            Object i = GsonUtils.toJson(update_data_json.get("elements")); // Gson ！使用反序列化
                            int codeVer = JSONObject.parseObject(new org.json.JSONArray(i + "").getString(0)).getIntValue("versionCode");

                            saveInfo("vercode", codeVer + ""); //保存远程版本号
                            if (BuildConfig.VERSION_CODE == codeVer) {
                                Toast.makeText(context, "您已经是最新版本了", Toast.LENGTH_SHORT).show();
                            } else {
                                Message msg = new Message();
                                Bundle data = new Bundle();
                                data.putString("c", getInfo("vercode"));
                                msg.setData(data);
                                handler.sendMessage(msg);
                            }

                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage(), e.fillInStackTrace());
                        }
                }*/

            } catch (IOException e) {
                Log.e(TAG,e.getMessage(),e.fillInStackTrace());
            }
        }).start();

    }

   public Handler handler = new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            Bundle data = msg.getData();
            String val = data.getString("verCode");
            String log = data.getString("log");
            try {
                startDown("版本号：" + val +"\n更新日志：\n"+log+"\n", URLDecoder.decode(getInfo("url"),"utf-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    };


    public void saveInfo(String key ,String val) {
        SharedPreferences info = getSharedPreferences(key,MODE_PRIVATE);
        SharedPreferences.Editor editor = info.edit();
        editor.putString(key,val);
        editor.apply();
        Log.e(key,val);
    }

    public String getInfo(String key) {
        return getSharedPreferences(key, MODE_PRIVATE).getString(key,"");
    }


    public void startDown(String msg,String url) {
        if (!AppOpsUtils.checkOps(context, AppOpsManager.permissionToOp(Manifest.permission.READ_EXTERNAL_STORAGE))) {
           // request.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        mShowDialog(false, "发现新版本", msg, url, "更新", "取消");

    }

    public void mShowDialog(boolean isCancel, String title, String msg, String srcUrl, String positive, String negative) {

        new MaterialAlertDialogBuilder(context).setTitle(title).setMessage(msg).setCancelable(isCancel).setPositiveButton(positive, (dialog, which) -> {

            if ( srcUrl != null) {

                //HttpUtils.openCustomTabs(context,srcUrl);
                long downId = DownloadUtils.startDownload(context,srcUrl);
               Log.e("downId",downId+"");

            }


        }).setNegativeButton(negative, (dialog, which) -> {

        }).show();
    }

    public static void installApk(Context c, Uri uri) {
        try {
            Log.e("url",uri+"");
            Intent installAppIntent = getInstallAppIntent(uri);
            c.startActivity(installAppIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Intent getInstallAppIntent(final Uri uri) {
        if (uri == null) return null;
        Intent intent = new Intent(Intent.ACTION_VIEW);
        String type = "application/vnd.android.package-archive";
        intent.setDataAndType(uri, null);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return intent;
    }

    private final ActivityResultLauncher<String> request = registerForActivityResult(new ActivityResultContracts.RequestPermission(),
            result -> {
        if (result){
            Log.i("","已授权");
        }
    });

    private final ActivityResultLauncher<Intent> AvResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (!AppOpsUtils.checkOps(context, AppOpsManager.permissionToOp(Manifest.permission.READ_EXTERNAL_STORAGE))){
            request.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    });

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt:
                Log.i("","点到我了");
                break;
        }
    }


    /***
     *
     *
     *  File f = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)+"/update.apk");
     *
     *                 DownloadManager.Request request = new DownloadManager.Request(Uri.parse(srcUrl))
     *                         .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
     *                         .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
     *                         //.setDestinationUri(Uri.fromFile(new File(APK_DOWN_PATH,"update.apk")))
     *                         .setDestinationUri(Uri.fromFile(f))
     *                         .setTitle("update.apk")
     *                         .setDescription("正在下载更新包");
     *
     *                 Toast.makeText(context, "正在下载更新...", Toast.LENGTH_SHORT).show();
     *
     *                 long downloadId = downloadManager.enqueue(request);
     *
     *                 IntentFilter filter = new IntentFilter();
     *                 filter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
     *                 filter.addAction(DownloadManager.ACTION_VIEW_DOWNLOADS);
     *                 filter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED);
     *                 context.registerReceiver(completeReceiver, filter);
     *
     *
     *
     *
     *
     */

}