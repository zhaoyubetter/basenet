package basenet.better.basenet.mae.bundles.update;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;

import java.io.File;

/**
 * Created by liyu20 on 2018/4/11.
 *
 * 下载进度progressDialog显示器
 *
 */

public class ProgressDialogDisplay implements DownloadManager.DownloadListener {
    private ProgressDialog progressDialog;
    private String downloadFailMsg;
    private String stopDownloadMsg;
    private String downloadingMsg;

    public ProgressDialogDisplay(final Activity activity, int iconRes, String title, String downloadingMsg, final String downloadFailMsg, final String stopDownloadMsg) {
        this.downloadFailMsg = downloadFailMsg;
        this.stopDownloadMsg = stopDownloadMsg;
        this.downloadingMsg = downloadingMsg;
        progressDialog = new ProgressDialog(activity);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);// 设置水平进度条
        progressDialog.setCancelable(true);// 设置是否可以通过点击Back键取消
        progressDialog.setCanceledOnTouchOutside(false);// 设置在点击Dialog外是否取消Dialog进度条
        progressDialog.setIcon(iconRes);// 设置提示的title的图标，默认是没有的
        progressDialog.setTitle(title);
        progressDialog.setMax(100);
        progressDialog.setButton(DialogInterface.BUTTON_POSITIVE, activity.getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        progressDialog.setMessage(downloadingMsg);
    }

    @Override
    public void onStartDownload() {
        progressDialog.setMessage(downloadingMsg);
        progressDialog.show();
    }

    @Override
    public void onProgressUpdate(long total, long hasRead) {
        int current;
        if(hasRead == total){
            current = 100;
        } else {
            current = (int) (100 * hasRead / (total * 1.0));
        }
        progressDialog.setProgress(current);
    }

    @Override
    public void onSuccess(File file, UpdateInfo updateInfo) {
        progressDialog.dismiss();
    }

    @Override
    public void onFailure(Throwable e) {
        progressDialog.setMessage(downloadFailMsg);
    }

    @Override
    public void onNetStateChanged(UpdateManager.NetState netState, int downloadStatus, boolean isMobile) {

    }

    @Override
    public void onStopDownload() {
        progressDialog.setMessage(stopDownloadMsg);
    }

}
