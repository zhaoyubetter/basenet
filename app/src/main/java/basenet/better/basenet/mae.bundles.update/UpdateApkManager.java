package basenet.better.basenet.mae.bundles.update;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by liyu20 on 2018/4/12.
 */

public class UpdateApkManager implements UpdateManager.UpdateListener {
    private static final String TAG = "UpdateApkManager";

    private Activity activity;
    public UpdateManager updateManager;
    private OkHttpClient okHttpClient;

    public UpdateApkManager(Activity activity, OkHttpClient okHttpClient) {
        this.activity = activity;
        this.okHttpClient = okHttpClient;
    }

    public void checkUpdate() {
        update("" + 1, "4.6.0");
    }

    private void update(String code, String versionName) {
        // 直接测试代码
        String fileUrl = "http://s1.xmcdn.com/apk/MainApp_v6.3.90.3_c159_release_proguard_180417_and-a1.apk";
        UpdateInfo updateInfo = new UpdateInfo(fileUrl,
                false, true, "更新啦",
                "更新内容", Environment.getExternalStorageDirectory().getAbsolutePath());
        update(updateInfo);


    }

    private void update(UpdateInfo updateInfo) {
        updateManager = new UpdateManager(activity, updateInfo, this);
        //是否允许移动网络下载
        updateManager.setAllowMobileNetDownload(true);


//        updateManager.addDownloadListeners(new ProgressDialogDisplay(activity, R.mipmap.mae_demos_ic_launcher,
//                activity.getString(R.string.mae_demos_updating), "正在下载：", "下载失败", "暂停下载"));
        updateManager.addDownloadListeners(new NotificationDisplay(activity, "1", "app下载", "当前进度：",
                android.R.drawable.ic_btn_speak_now, "暂停下载", "下载成功", "下载失败"));
        updateManager.startDownload();
    }

    @Override
    public void notWifiState() {
        //此处可设置下载dialog
        /*new MAEEasyDialog.Builder(activity)
                .setTitle("流量更新")
                .setMessage("当前不在wifi状态下")
                .setPositiveButton("继续更新", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //允许下载
                        updateManager.setAllowMobileNetDownload(true);
                        updateManager.startDownload(false);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();*/
    }

    @Override
    public void onStartDownload() {

    }

    @Override
    public void onProgressUpdate(long total, long current) {
    }

    @Override
    public void onStopDownload() {

    }

    @Override
    public void onSuccess(final File file, UpdateInfo updateInfo) {

    }

    @Override
    public void onFailure(Throwable e) {

    }

    @Override
    public void onNetStateChanged(UpdateManager.NetState netState, final int downloadStatus, boolean isAllowMobileNetDownload) {
    }
}
