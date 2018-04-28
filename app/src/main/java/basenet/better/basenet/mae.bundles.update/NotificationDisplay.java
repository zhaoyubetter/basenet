package basenet.better.basenet.mae.bundles.update;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.text.format.Formatter;


import java.io.File;
import java.lang.reflect.Method;


/**
 * Created by liyu20 on 2018/4/11.
 * <p>
 * 下载进度notification显示器
 */

public class NotificationDisplay implements DownloadManager.DownloadListener {

    /**
     * 继续下载广播_action
     */
    final static String ACTION_CONTINUE_DOWN = "com.mae.app_common_update_NotificationDisplay";

    private Activity activity;
    private NotificationManager notifyManager;
    private NotificationCompat.Builder notifyBuilder;
    private String contentTitle;
    private String contentText;
    private int smallIconRes;
    private int currentProgress;
    private String stopDownloadMsg;
    private String successMsg;
    private String failureMsg;
    private boolean hasInit;
    private String channelID;   // for android 8.0
    private String mProgressDesc; // 11MB/33MB

    public NotificationDisplay(Activity activity, String channelID, String contentTitle, String contentText, int smallIconRes,
                               String stopDownloadMsg, String successMsg, String failureMsg) {
        this.successMsg = successMsg;
        this.failureMsg = failureMsg;
        this.activity = activity;
        this.stopDownloadMsg = stopDownloadMsg;
        this.contentTitle = contentTitle;
        this.contentText = contentText;
        this.smallIconRes = smallIconRes;
        this.channelID = channelID;
        notifyManager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
        notifyBuilder = new NotificationCompat.Builder(activity);
        notifyBuilder.setOnlyAlertOnce(true);  // Android 8.0 总是发声，真是恶心，加上这个，第一次也会有，尴尬
        notifyBuilder.setContentTitle(contentTitle)
                .setContentText(contentText + "0")
                .setSmallIcon(smallIconRes);


    }

    private void downloadComplete(String msg) {
        notifyBuilder.setContentText(msg)//下载完成
                .setProgress(0, 0, false);    //移除进度条
        notifyManager.notify(1, notifyBuilder.build());
    }

    private void updateDownloadProgressNotify(int progress, String progressDesc) {
        notifyBuilder.setProgress(100, progress, false)
                .setContentText(contentText + progressDesc);
        notifyManager.notify(1, notifyBuilder.build());
    }

    private void setMessage(String msg) {
        notifyBuilder.setContentText(msg)//下载完成
                .setProgress(100, currentProgress, false);
        notifyManager.notify(1, notifyBuilder.build());
    }

    @Override
    public void onStartDownload() {
        hasInit = true;
    }

    @Override
    public void onProgressUpdate(long total, long current) {
        int tCurrent = 0;
        if (total == current) {
            tCurrent = 100;
        } else {
            tCurrent = (int) (100 * current / (total * 1.0));
        }
        currentProgress = tCurrent;
        String totalDesc = Formatter.formatFileSize(activity.getApplicationContext(), total);
        String currentDesc = Formatter.formatFileSize(activity.getApplicationContext(), current);
        mProgressDesc = currentDesc + "/" + totalDesc;
        updateDownloadProgressNotify(tCurrent, mProgressDesc);
    }

    @Override
    public void onStopDownload() {
        setMessage(stopDownloadMsg);
    }

    @Override
    public void onSuccess(File file, UpdateInfo updateInfo) {
        if (hasInit) {
            downloadComplete(successMsg);
            collapseStatusBar(activity);
            notifyManager.cancel(1);
        }

    }

    @Override
    public void onFailure(Throwable e) {
        downloadComplete(failureMsg);
    }

    @Override
    public void onNetStateChanged(UpdateManager.NetState netState, int downloadStatus, boolean isAllowMobileNetDownload) {
        // if in mobile_net and not allow mobile_net,then show action for user confirm downloading continue?
        if (netState == UpdateManager.NetState.NETWORK_MOBILE && !isAllowMobileNetDownload) {
            // 显示继续下载按钮
            Intent snoozeIntent = new Intent(ACTION_CONTINUE_DOWN);
            PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(activity.getApplicationContext(), 0,
                    snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            // 清空action
            notifyBuilder.mActions.clear();

            notifyBuilder.addAction(android.R.drawable.ic_btn_speak_now, "继续下载", snoozePendingIntent);
            notifyBuilder.setContentText("当前不在WiFi环境下，是否继续下载？");
            notifyBuilder.setProgress(100, currentProgress, false);
            notifyManager.notify(1, notifyBuilder.build());
        } else {
            // 清空action
            notifyBuilder.mActions.clear();
            notifyBuilder.setProgress(100, currentProgress, false)
                    .setContentText(contentText + (TextUtils.isEmpty(mProgressDesc) ? "" : mProgressDesc));
            notifyManager.notify(1, notifyBuilder.build());
        }
    }

    /**
     * 收起通知栏
     *
     * @param context
     */
    public static void collapseStatusBar(Context context) {
        try {
            Object statusBarManager = context.getSystemService("statusbar");
            Method collapse;
            if (Build.VERSION.SDK_INT <= 16) {
                collapse = statusBarManager.getClass().getMethod("collapse");
            } else {
                collapse = statusBarManager.getClass().getMethod("collapsePanels");
            }
            collapse.invoke(statusBarManager);
        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }

}