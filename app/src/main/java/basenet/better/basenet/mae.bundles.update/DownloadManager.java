package basenet.better.basenet.mae.bundles.update;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.util.Log;


import java.io.File;
import java.lang.ref.WeakReference;

import lib.basenet.okhttp.upload_down.DownloadFileInfo;
import lib.basenet.okhttp.upload_down.DownloadFileManager;
import lib.basenet.request.AbsDownloadRequestCallback;
import lib.basenet.response.Response;

/**
 * Created by liyu20 on 2018/4/10.
 * 网络切换时，okhttp 底层会进行 onStop操作，因为IO断了；
 */
class DownloadManager extends AbsDownloadRequestCallback {
    private DownloadFileManager downloadFileManager;

    private UpdateInfo updateInfo;
    private DownloadListener downloadListener;
    /**
     * 是否允许流量下载，外界设置
     */
    private boolean isAllowMobileNetDownload;
    //记录网络状态变化，因为每次应用启动都会有状态变化，所以记录值，过滤第一次
    private int stateChangeNum;
    private NetStateReceiver netStateReceiver;
    private WeakReference<Activity> activiyRef;

    DownloadManager(final Activity activity, UpdateInfo updateInfo, DownloadListener downloadListener) {
        // 前置检查
        if (downloadListener == null) {
            throw new RuntimeException("downloadListener can not be null!");
        }
        if (activity == null || activity.isFinishing()) {
            return;
        }

        activiyRef = new WeakReference<>(activity);

        this.updateInfo = updateInfo;
        this.downloadListener = downloadListener;
        downloadFileManager = new DownloadFileManager(updateInfo.getFileUrl(), updateInfo.getLocalFileFullPath(), this);

        netStateReceiver = new NetStateReceiver();
        final IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        intentFilter.addAction(NotificationDisplay.ACTION_CONTINUE_DOWN);   // 通知栏继续
        activity.registerReceiver(netStateReceiver, intentFilter);
    }

    public void setAllowMobileNetDownload(boolean allowMobileNetDownload) {
        isAllowMobileNetDownload = allowMobileNetDownload;
    }

    public boolean isAllowMobileNetDownload() {
        return isAllowMobileNetDownload;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (downloadListener != null) {
            downloadListener.onStartDownload();
        }
    }

    @Override
    public void onStop(long contentLength, long bytesRead, DownloadFileInfo fileInfo) {
        if (downloadListener != null) {
            downloadListener.onStopDownload();
        }
    }

    @Override
    public void onSuccess(Response<String> response) {
        if (downloadListener != null) {
            downloadListener.onSuccess(new File(updateInfo.getLocalFileFullPath()), updateInfo);
        }
        if (netStateReceiver != null && activiyRef.get() != null) {
            activiyRef.get().unregisterReceiver(netStateReceiver);
            netStateReceiver = null;
        }
    }

    @Override
    public void onFailure(Throwable e) {
        if (downloadListener != null) {
            downloadListener.onFailure(e);
        }
    }

    @Override
    public void onProgressUpdate(long contentLength, long bytesRead, boolean done) {
        if (downloadListener != null) {
            downloadListener.onProgressUpdate(contentLength, bytesRead);
        }
    }

    /**
     * 网络监听与继续下载监听广播接收者
     */
    public class NetStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                if (stateChangeNum == 0) {
                    stateChangeNum++;
                    return;
                }

                // 需要延迟一下，等待底层处理完毕后(okhttp断网，就会抛异常，中断下载)，这里再继续
                new android.os.Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        UpdateManager.NetState netState = UpdateManager.getNetWorkState(context);
                        if (downloadListener != null) {
                            downloadListener.onNetStateChanged(netState, downloadFileManager.getStatus(), isAllowMobileNetDownload);
                        }
                        switch (netState) {
                            case NETWORK_MOBILE:
                                if (isAllowMobileNetDownload) {  // 失败继续下载
                                    downloadFileManager.startDownload();
                                    downloadFileManager.startDownload();
                                    downloadFileManager.startDownload();
                                } else {
                                    downloadFileManager.stopDownload();
                                }
                                break;
                            case NETWORK_WIFI:  // 失败继续下载
                                downloadFileManager.startDownload();
                                downloadFileManager.startDownload();
                                downloadFileManager.startDownload();
                                downloadFileManager.startDownload();
                                Log.e("better123", "status: " + downloadFileManager.getStatus() + ", " +
                                        netState.name() + ", wifi 继续下载");
                                break;
                        }

                        Log.e("better123", "status: " + downloadFileManager.getStatus() + ", " +
                                netState.name() + ", 全局");
                    }
                },400);
            }


            // 如果是来自，点击通知栏的继续下载
            if (intent.getAction() != null && intent.getAction().equals(NotificationDisplay.ACTION_CONTINUE_DOWN)
                    && downloadFileManager != null) {
                downloadFileManager.startDownload();
                downloadFileManager.startDownload();
                downloadFileManager.startDownload();
                downloadFileManager.startDownload();
                Log.e("better123", "status: " + downloadFileManager.getStatus() + ", " +
                        "dd" + ", 流量下 继续下载");
            }
        }
    }

    void startDownload(Context context, boolean isForce) {
        if (downloadListener == null) {
            return;
        }
        File file = new File(updateInfo.getLocalFileFullPath());
        if (file.exists() && downloadFileManager.getStatus() == DownloadFileInfo.SUCCESS) {
            downloadListener.onSuccess(file, updateInfo);
        } else {
            if (UpdateManager.isWifi(context) || isAllowMobileNetDownload) {
                //需要下载
                downloadFileManager.startDownload(isForce);
            }
        }
    }

    void stopDownload() {
        downloadFileManager.stopDownload();
    }

    int getDownloadStatus() {
        return downloadFileManager.getStatus();
    }

    public interface DownloadListener {
        /**
         * 更新状态
         *
         * @param contentLength 长度
         * @param bytesRead     当前完成
         */
        void onProgressUpdate(long contentLength, long bytesRead);

        void onStopDownload();

        void onSuccess(File file, UpdateInfo updateInfo);

        void onFailure(Throwable e);

        /**
         * 网络状态改变回调
         *
         * @param netState                 当前网络状态
         * @param downloadStatus           下载状态
         * @param isAllowMobileNetDownload 是否允许流量下载
         */
        void onNetStateChanged(UpdateManager.NetState netState, int downloadStatus, boolean isAllowMobileNetDownload);

        void onStartDownload();
    }
}
