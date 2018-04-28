package basenet.better.basenet.mae.bundles.update;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by liyu20 on 2018/4/11.
 */

public class UpdateManager {
    private DownloadManager downloadManager;
    private WeakReference<Activity> activityRef;
    //服务端返回的更新信息
    private UpdateInfo updateInfo;
    //更新监听器
    private UpdateListener updateListener;
    private List<DownloadManager.DownloadListener> downloadListeners;

    public UpdateManager(Activity activity, UpdateInfo updateInfo, UpdateListener updateListener) {
        this.updateListener = updateListener;
        this.activityRef = new WeakReference<>(activity);
        this.updateInfo = updateInfo;
        this.downloadListeners = new ArrayList<>();
        this.downloadManager = new DownloadManager(activity, updateInfo, downloadListener);
    }

    public void addDownloadListeners(DownloadManager.DownloadListener... downloadListeners) {
        this.downloadListeners.addAll(Arrays.asList(downloadListeners));
    }

    public boolean isNeedUpdate() {
        return updateInfo.isUpdate();
    }

    public void startDownload(){
        startDownload(false);
    }

    /**
     * 开始更新下载
     *
     * @param isForce 是否强制更新
     */
    public void startDownload(boolean isForce) {
        if (!updateInfo.isUpdate()) {
            return;
        }

        if (activityRef.get() == null) {
            return;
        }
        final Context ctx = activityRef.get().getApplicationContext();

        if (isWifi(ctx) || (downloadManager.isAllowMobileNetDownload() && getNetWorkState(ctx) == NetState.NETWORK_MOBILE)) {
            downloadManager.startDownload(ctx, isForce);
        } else {
            if (updateListener != null) {
                updateListener.notWifiState();
            }
        }
    }

    public void setAllowMobileNetDownload(boolean allowMobileNetDownload) {
        downloadManager.setAllowMobileNetDownload(allowMobileNetDownload);
    }

    public void stopDownload() {
        downloadManager.stopDownload();
    }

    public enum NetState {
        NETWORK_NONE, NETWORK_MOBILE, NETWORK_WIFI
    }

    /**
     * 添加不是wifi状态的回调， @see startDownload 方法中调用
     * {@link #startDownload()}
     */
    public interface UpdateListener extends DownloadManager.DownloadListener {
        void notWifiState();
    }

    static boolean isWifi(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkINfo = cm.getActiveNetworkInfo();
        return networkINfo != null && networkINfo.getType() == ConnectivityManager.TYPE_WIFI;
    }

    static UpdateManager.NetState getNetWorkState(Context context) {
        // 得到连接管理器对象
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetworkInfo = connectivityManager
                .getActiveNetworkInfo();
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {

            if (activeNetworkInfo.getType() == (ConnectivityManager.TYPE_WIFI)) {
                return UpdateManager.NetState.NETWORK_WIFI;
            } else if (activeNetworkInfo.getType() == (ConnectivityManager.TYPE_MOBILE)) {
                return UpdateManager.NetState.NETWORK_MOBILE;
            }
        } else {
            return UpdateManager.NetState.NETWORK_NONE;
        }
        return UpdateManager.NetState.NETWORK_NONE;
    }

    /**
     * 下载监听，回调到Android的 UI Thread
     */
    private DownloadManager.DownloadListener downloadListener = new DownloadManager.DownloadListener() {

        @Override
        public void onStopDownload() {
            final Activity activity = activityRef.get();
            if (activity != null && !activity.isFinishing()) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (DownloadManager.DownloadListener downloadListener : downloadListeners) {
                            downloadListener.onStopDownload();
                        }
                        if (updateListener != null) {
                            updateListener.onStopDownload();
                        }
                    }
                });
            }
        }

        @Override
        public void onProgressUpdate(final long total, final long current) {
            final Activity activity = activityRef.get();
            if (activity != null && !activity.isFinishing()) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (DownloadManager.DownloadListener downloadListener : downloadListeners) {
                            downloadListener.onProgressUpdate(total, current);
                        }
                        if (updateListener != null) {
                            updateListener.onProgressUpdate(total, current);
                        }
                    }
                });
            }
        }

        @Override
        public void onSuccess(final File file, final UpdateInfo updateInfo) {
            final Activity activity = activityRef.get();
            if (activity != null && !activity.isFinishing()) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (DownloadManager.DownloadListener downloadListener : downloadListeners) {
                            downloadListener.onSuccess(file, updateInfo);
                        }
                        if (updateListener != null) {
                            updateListener.onSuccess(file, updateInfo);
                        }
                    }
                });
            }
        }

        @Override
        public void onFailure(final Throwable e) {
            final Activity activity = activityRef.get();
            if (activity != null && !activity.isFinishing()) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        e.printStackTrace();
                        for (DownloadManager.DownloadListener downloadListener : downloadListeners) {
                            downloadListener.onFailure(e);
                        }
                        if (updateListener != null) {
                            updateListener.onFailure(e);
                        }
                    }
                });
            }
        }

        @Override
        public void onStartDownload() {
            final Activity activity = activityRef.get();
            if (activity != null && !activity.isFinishing()) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (DownloadManager.DownloadListener downloadListener : downloadListeners) {
                            downloadListener.onStartDownload();
                        }
                        if (updateListener != null) {
                            updateListener.onStartDownload();
                        }
                    }
                });
            }
        }

        @Override
        public void onNetStateChanged(final NetState netState, final int downloadStatus, final boolean isMobileAllowDown) {
            final Activity activity = activityRef.get();
            if (activity != null && !activity.isFinishing()) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (DownloadManager.DownloadListener downloadListener : downloadListeners) {
                            downloadListener.onNetStateChanged(netState, downloadStatus, isMobileAllowDown);
                        }
                        if (updateListener != null) {
                            updateListener.onNetStateChanged(netState, downloadStatus, isMobileAllowDown);
                        }
                    }
                });
            }
        }
    };

    public int getDownloadStatus() {
        return downloadManager.getDownloadStatus();
    }
}
