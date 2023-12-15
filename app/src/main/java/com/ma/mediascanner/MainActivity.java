package com.ma.mediascanner;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.UriPermission;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.documentfile.provider.DocumentFile;

import com.danielstone.materialaboutlibrary.MaterialAboutFragment;
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem;
import com.danielstone.materialaboutlibrary.items.MaterialAboutItemOnClickAction;
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard;
import com.danielstone.materialaboutlibrary.model.MaterialAboutList;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textview.MaterialTextView;
import com.ma.mediascanner.utils.DocumentFileUtils;
import com.ma.mediascanner.utils.FilesUtils;
import com.ma.mediascanner.utils.MediaScannerUtil;
import com.tencent.mmkv.MMKV;

public class MainActivity extends AppCompatActivity {
    static MaterialPageFragment materialPageFragment = new MaterialPageFragment();
    static Handler uiHandle = new Handler(Looper.getMainLooper());
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 跟随系统切换深色模式
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        // 深色模式适配
        View decorView = getWindow().getDecorView();
        int flags = decorView.getSystemUiVisibility();

        if ((getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
            // 如果是深色模式，则设置状态栏文字为白色
            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;

        } else {
            // 如果不是深色模式，则设置状态栏文字为黑色
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }

        decorView.setSystemUiVisibility(flags);

        getSupportActionBar().setBackgroundDrawable(null);
        getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment_content, materialPageFragment).commit();

        ((MaterialTextView) findViewById(R.id.main_description)).setText("刷新媒体存储是一款刷新媒体数据库的工具类应用\n" +
                "––––––––– 酷安 By sbmatch\n\n" +
                "以下是它可能的使用场景:\n" +
                "- 刷新通过【存储空间隔离】导出的的文件\n" +
                "- 刷新通过adb push推送到手机的文件\n" +
                "- 刷新通过ffmpeg、gifsicle等命令行工具生成的文件\n\n");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, Menu.FIRST+1, 0, "反馈");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case Menu.FIRST+1:
                openCustomTabs(this,"https://loooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo.ng/looooooooooooOooooOoooOoOoOOoooOOoOoooooooooOooooOooooOoooOooOOooooOOOoOooooOooOoOooooOOooooooOOOOoOoooOOoOOOoooooooOOoooOOoOOOoooooooOoooooOoOoooooooooOOoooOOoOOooooooOOOOoooOOOooooooOooOoOoooooOOoOoooOoOoOOooooOoOoOoooooooOoooooOOOoooooOOOOoOoooOOoOOoooooooooOoooOoOOoooooOOOOoOoooooOOOooooOoOoOOoooooOOoOoooOooOOOoooOoOOOOooooooooooooOooooOoooooooOOoooooOooooooOooOoOoooooOOooooooOOOOOooooOOooOoooooooOooooOooooOooooooOOooooooOooOng");
                break;
        }
        return true;
    }

    private void openCustomTabs(Context context, String url) {
        CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder();
        intentBuilder.setShowTitle(true);
        intentBuilder.setInstantAppsEnabled(true);
        intentBuilder.setUrlBarHidingEnabled(true);

        CustomTabsIntent customTabsIntent = intentBuilder.build();
        customTabsIntent.launchUrl(context, Uri.parse(url));
    }
    public static class MaterialPageFragment extends MaterialAboutFragment {
        LinearLayoutCompat.LayoutParams lp = getLayoutParams();
        final MMKV selectFolderPathMMKV = MMKV.mmkvWithID("selectedFolderPath");
        final ActivityResultLauncher<Uri> openTree = registerForActivityResult(new ActivityResultContracts.OpenDocumentTree(), o -> {
            if (o != null) {
               // 获取持久化读权限
                if (DocumentFileUtils.isGrantDirPermissionFromUri(requireContext(), o)){
                    requireContext().getContentResolver().takePersistableUriPermission(o, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
                DocumentFile documentF1 = DocumentFile.fromTreeUri(requireContext(), o);
                if (documentF1 != null){
                    LinearLayoutCompat layoutCompat = new LinearLayoutCompat(requireContext());
                    layoutCompat.setOrientation(LinearLayoutCompat.VERTICAL);

                    LinearProgressIndicator progressIndicator = new LinearProgressIndicator(requireContext());
                    progressIndicator.setMax(documentF1.listFiles().length);

                    layoutCompat.addView(progressIndicator, lp);
                    MaterialAlertDialogBuilder builder = showDialog(null, "正在刷新...", layoutCompat, null, null, null,null, null, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();

                    try {
                        for (int i = 0; i < documentF1.listFiles().length; i++){
                            progressIndicator.setProgressCompat(i, true);

                            if (progressIndicator.getProgress() == progressIndicator.getMax()-1) {
                                MediaScannerUtil.scanFolder(requireContext(), DocumentFileUtils.getPathFromUri(requireContext(), o));
                            }
                        }

                        uiHandle.postDelayed(() -> {
                            dialog.cancel();
                            Toast.makeText(requireContext(), "已刷新"+ FilesUtils.subFolderCountAndStartsWithString(documentF1, ".")+"个子文件夹", Toast.LENGTH_SHORT).show();
                        }, 1000);

                    } catch (Throwable e) {
                        throw new RuntimeException(e);
                    }

                }
            }
        });

       final ActivityResultLauncher<String> openSingleFile = registerForActivityResult(new ActivityResultContracts.GetContent(), o -> {
            if (o != null){
                MediaScannerUtil.scanSingleFile(requireContext(), DocumentFileUtils.getPathFromUri(requireContext(),o));
                Toast.makeText(requireContext(), "已刷新: "+DocumentFile.fromSingleUri(requireContext(),o).getName(), Toast.LENGTH_SHORT).show();
            }
        });

       final ActivityResultLauncher<Uri> grantFolderTreePermission = registerForActivityResult(new ActivityResultContracts.OpenDocumentTree(), o -> {
            if (o != null) {
                // 获取持久化读权限
                if (DocumentFileUtils.isGrantDirPermissionFromUri(requireContext(), o)) {
                    requireContext().getContentResolver().takePersistableUriPermission(o, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    Toast.makeText(requireContext(), "已授权此目录访问权限", Toast.LENGTH_SHORT).show();
                }
            }
        });

        private LinearLayoutCompat.LayoutParams getLayoutParams(){
            LinearLayoutCompat.LayoutParams lp = new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.topMargin = 5;
            lp.leftMargin = 60;
            lp.rightMargin = 60;
            return lp;
        }

        private MaterialAboutActionItem createDynamicItem(final Context context, String text, String subText, MaterialAboutItemOnClickAction onClickAction){
            return new MaterialAboutActionItem.Builder()
                    .text(text)
                    .subText(subText)
                    .icon(R.drawable.ic_refresh)
                    .setOnClickAction(onClickAction)
                    .build();
        }
        private MaterialAlertDialogBuilder showDialog(String title, String msg, View view, String neutralTitle, DialogInterface.OnClickListener neutralListener, String positiveTitle, DialogInterface.OnClickListener positiveListener, String negativeTitle, DialogInterface.OnClickListener negativeListener){
           return new MaterialAlertDialogBuilder(requireContext())
                   .setTitle(title)
                   .setMessage(msg)
                   .setView(view)
                   .setNeutralButton(neutralTitle, neutralListener)
                   .setPositiveButton(positiveTitle, positiveListener)
                   .setNegativeButton(negativeTitle, negativeListener);
        }

        @Override
        protected MaterialAboutList getMaterialAboutList(Context activityContext) {
            MaterialAboutCard.Builder selectFolderbuilder = new MaterialAboutCard.Builder();
            selectFolderbuilder.addItem(createDynamicItem(activityContext, "选择文件夹", null, () -> {
              openTree.launch(Uri.parse(Intent.ACTION_OPEN_DOCUMENT_TREE));
            }));

            MaterialAboutCard.Builder selectSingleFilebuilder = new MaterialAboutCard.Builder();
            selectSingleFilebuilder.addItem(createDynamicItem(activityContext, "选择单个文件", null, () -> {
                openSingleFile.launch("*/*");
            }));

            MaterialAboutCard.Builder setSpecifyTargetFolder = new MaterialAboutCard.Builder();
            setSpecifyTargetFolder.addItem(createDynamicItem(activityContext, "选择固定文件夹(可多选)", null, () -> {
                LinearLayoutCompat layoutCompat = new LinearLayoutCompat(requireContext());
                layoutCompat.setOrientation(LinearLayoutCompat.VERTICAL);

                for (UriPermission uriPermission : requireContext().getContentResolver().getPersistedUriPermissions()){
                    MaterialCheckBox checkBox = new MaterialCheckBox(requireContext());
                    checkBox.setTag(uriPermission.getUri());
                    checkBox.setId(uriPermission.getUri().hashCode());
                    checkBox.setUseMaterialThemeColors(true);
                    checkBox.setText(uriPermission.getUri().getPath());
                    checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        selectFolderPathMMKV.importFromSharedPreferences(requireContext().getSharedPreferences("selectedFolderPath",MODE_PRIVATE));
                        if (isChecked) {
                            selectFolderPathMMKV.putInt(DocumentFileUtils.getPathFromUri(requireContext(), (Uri) buttonView.getTag()), buttonView.getId());
                        }else {
                            selectFolderPathMMKV.putInt(DocumentFileUtils.getPathFromUri(requireContext(), (Uri) buttonView.getTag()), -1);
                        }
                    });
                    layoutCompat.addView(checkBox, lp);
                }

                MaterialAlertDialogBuilder builder = showDialog("请勾选需要操作的文件夹", "以下列出的是所有本应用有权访问的文件夹", layoutCompat,
                        "撤销授权", (dialog, which) -> {
                            for (UriPermission uriPermission : requireContext().getContentResolver().getPersistedUriPermissions()) {
                                if (selectFolderPathMMKV.getInt(DocumentFileUtils.getPathFromUri(requireContext(), uriPermission.getUri()), -1) == uriPermission.getUri().hashCode() && ((MaterialCheckBox)layoutCompat.findViewById(uriPermission.getUri().hashCode())).isChecked()) {
                                    requireContext().getContentResolver().releasePersistableUriPermission(uriPermission.getUri(), Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                }
                            }
                        }, "刷新目录", (dialog, which) -> {
                            for (UriPermission uriPermission : requireContext().getContentResolver().getPersistedUriPermissions()) {
                                if (selectFolderPathMMKV.getInt(DocumentFileUtils.getPathFromUri(requireContext(), uriPermission.getUri()), -1) == uriPermission.getUri().hashCode() && ((MaterialCheckBox)layoutCompat.findViewById(uriPermission.getUri().hashCode())).isChecked()) {
                                    MediaScannerUtil.scanFolder(requireContext(),DocumentFileUtils.getPathFromUri(requireContext(), uriPermission.getUri()));
                                    System.out.println(DocumentFileUtils.getPathFromUri(requireContext(), uriPermission.getUri()));
                                    Toast.makeText(requireContext(), "已刷新"+ FilesUtils.subFolderCountAndStartsWithString(DocumentFile.fromTreeUri(requireContext(), uriPermission.getUri()), ".")+"个文件夹", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }, "授权目录", (dialog, which) -> grantFolderTreePermission.launch(Uri.parse(Intent.ACTION_OPEN_DOCUMENT_TREE)));
                AlertDialog dialog = builder.create();
                dialog.show();
            }));
            return new MaterialAboutList(selectFolderbuilder.build(), selectSingleFilebuilder.build(), setSpecifyTargetFolder.build());
        }
    }
}