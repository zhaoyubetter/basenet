package basenet.better.basenet.okhttp;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import basenet.better.basenet.R;
import basenet.better.basenet.utils.PermissionUtils;
import lib.basenet.NetUtils;
import lib.basenet.okhttp.upload_down.DownloadFileInfo;
import lib.basenet.okhttp.upload_down.DownloadFileManager;
import lib.basenet.okhttp.OkHttpRequest;
import lib.basenet.request.AbsDownloadRequestCallback;
import lib.basenet.request.AbsRequest;
import lib.basenet.request.AbsRequestCallBack;
import lib.basenet.response.Response;

public class OkhttpDownActivity extends AppCompatActivity implements View.OnClickListener {

    final String TAG = "down";

    EditText et_down_url;
    ProgressBar progress;
    TextView progressTV;
    TextView error;

    AbsRequest request;

    final String downUrl = "http://s1.xmcdn.com/apk/MainApp_v6.3.90.3_c159_release_proguard_180417_and-a1.apk";
//	final String downUrl = "http://111.231.206.52:8080/yu/hehe.gif";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_okhttp_down);

        et_down_url = (EditText) findViewById(R.id.et_down_url);
        progress = (ProgressBar) findViewById(R.id.progress);
        progressTV = (TextView) findViewById(R.id.progressTV);
        error = (TextView) findViewById(R.id.error);
        progress.setMax(100);

        et_down_url.setText(downUrl);

        findViewById(R.id.down).setOnClickListener(this);
        findViewById(R.id.cancel).setOnClickListener(this);
        findViewById(R.id.pause).setOnClickListener(this);
        findViewById(R.id.down_continue_).setOnClickListener(this);
        findViewById(R.id.re_startDown).setOnClickListener(this);
        findViewById(R.id.force_down).setOnClickListener(this);

        final IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        intentFilter.addAction(ACTION_CONTINUE_DOWN);
        registerReceiver(netStateReceiver, intentFilter);

    }

    final NetStateReceiver netStateReceiver = new NetStateReceiver();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(netStateReceiver);
    }

    private void down() {
        PermissionUtils.checkOnePermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, "权限申请", new Runnable() {
            @Override
            public void run() {
                realDown();
            }
        });
    }

    private void realDown() {
        error.setText("");
        Map<String, File> uploads = new HashMap<>();
        final String absolutePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        File file = new File(absolutePath + "/" + "headfirst.apk");
        request = new OkHttpRequest.Builder().url(et_down_url.getText().toString())
                .downFile(file)
                .callback(new AbsRequestCallBack() {
                    @Override
                    public void onSuccess(Response response) {
                        super.onSuccess(response);
                    }

                    @Override
                    public void onFailure(final Throwable e) {
                        super.onFailure(e);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                error.setText(e.toString());
                            }
                        });
                    }

                    @Override
                    public void onProgressUpdate(final long contentLength, final long bytesRead, final boolean done) {
                        super.onProgressUpdate(contentLength, bytesRead, done);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progress.setProgress((int) (bytesRead * 1.0f / contentLength * 100));
                                progressTV.setText(contentLength + "/" + bytesRead);
                            }
                        });
                    }
                }).tag(TAG)
                .build();
        request.request();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.force_down:
                PermissionUtils.checkOnePermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, "权限申请", new Runnable() {
                    @Override
                    public void run() {
                        downPart(true);
                    }
                });
                break;
            case R.id.down:
                PermissionUtils.checkOnePermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, "权限申请", new Runnable() {
                    @Override
                    public void run() {
//				down();
                        downPart(false);
                    }
                });
                break;
            case R.id.cancel:
                NetUtils.getInstance().cancel(TAG);
                break;
            case R.id.pause:
                if (downloadFileManager != null) {
                    downloadFileManager.stopDownload();
                }
                break;
            case R.id.down_continue_:
                if (downloadFileManager != null) {
                    downloadFileManager.startDownload();
                }
                break;
            case R.id.re_startDown:
                if (downloadFileManager != null) {
                    downloadFileManager.deleteDownload();
                    downloadFileManager.startDownload();

                }
                break;
        }
    }

    private DownloadFileManager downloadFileManager;

    private void downPart(boolean force) {
        if (downloadFileManager != null && downloadFileManager.getStatus() == DownloadFileInfo.DOWNLOADING) {
            return;
        }

        DownloadFileInfo downloadFileInfo = new DownloadFileInfo(et_down_url.getText().toString(),
                Environment.getExternalStorageDirectory().getAbsolutePath() + "/aaa.apk");
        downloadFileManager = new DownloadFileManager(downloadFileInfo, new AbsDownloadRequestCallback() {
            @Override
            public void onSuccess(final Response<String> response) {
                super.onSuccess(response);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(OkhttpDownActivity.this, "成功了", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onStart() {
                super.onStart();
            }

            @Override
            public void onFailure(final Throwable e) {
                super.onFailure(e);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        error.setText(e.toString());
                    }
                });
            }

            @Override
            public void onStop(long contentLength, long bytesRead, final DownloadFileInfo fileInfo) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progress.setProgress((int) (fileInfo.currentFinished * 1.0f / fileInfo.fileSize * 100));

                        progressTV.setText(fileInfo.fileSize + "/" + fileInfo.currentFinished);
                    }
                });
            }

            @Override
            public void onProgressUpdate(final long contentLength, final long bytesRead, final boolean done) {
                super.onProgressUpdate(contentLength, bytesRead, done);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        currentProgress = (int) (bytesRead * 1.0f / contentLength * 100);
                        progress.setProgress((int) (bytesRead * 1.0f / contentLength * 100));
                        progressTV.setText(contentLength + "/" + bytesRead);


                        if (notifyManager != null) {
                            notifyBuilder.setProgress(100, (int) (bytesRead * 1.0f / contentLength * 100), false)
                                    .setContentText(contentLength + "/" + bytesRead);
                            notifyManager.notify(1, notifyBuilder.build());
                        }

                    }
                });
            }
        });
        downloadFileManager.startDownload(force);


        // 通知栏

        notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notifyBuilder = new NotificationCompat.Builder(this);
        notifyBuilder.setOnlyAlertOnce(true);  // Android 8.0 总是发声，真是恶心，加上这个，第一次也会有，尴尬
        notifyBuilder.setContentTitle("下载啦")
                .setContentText("进度" + "0")
                .setSmallIcon(android.R.drawable.ic_btn_speak_now);
    }


    NotificationManager notifyManager;
    NotificationCompat.Builder notifyBuilder;
    int currentProgress = 0;


    boolean isAllowMobileNetDownload = false;

    /**
     * 网络监听与继续下载监听广播接收者
     */
    public class NetStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            SystemClock.sleep(2000);
            if (intent.getAction() != null && intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)
                    && downloadFileManager != null) {
                int netState = getNetWorkState(context);
                switch (netState) {
                    case MOBILE:
                        if (isAllowMobileNetDownload) {  // 失败继续下载
                            if (downloadFileManager.getStatus() == DownloadFileInfo.DOWNLOAD_PAUSE ||
                                    downloadFileManager.getStatus() == DownloadFileInfo.FAILURE) {
                                downloadFileManager.startDownload();
                            }
                        } else {
                            if (downloadFileManager.getStatus() == DownloadFileInfo.DOWNLOADING) {
                                downloadFileManager.stopDownload();
                                Log.e("better1244", "status: " + downloadFileManager.getStatus() + ", " +
                                        netState + ", 流量停止下载");
                            }
                        }
                        break;
                    case WIFI:  // 失败继续下载
                        if (downloadFileManager.getStatus() == DownloadFileInfo.DOWNLOAD_PAUSE ||
                                downloadFileManager.getStatus() == DownloadFileInfo.FAILURE) {
                            downloadFileManager.startDownload();
                            Log.e("better1244", "status: " + downloadFileManager.getStatus() + ", " +
                                    netState + ", wifi 继续下载");
                        }
                        break;
                }
                Log.e("better1244", "status: " + downloadFileManager.getStatus() + ", " +
                        netState + ", 全局");

                if(notifyManager == null) {
                    return;
                }
                // 通知栏
                if (netState == MOBILE && !isAllowMobileNetDownload) {
                    // 显示继续下载按钮
                    Intent snoozeIntent = new Intent(ACTION_CONTINUE_DOWN);
                    PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0,
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
                    notifyBuilder.setProgress(100, currentProgress, false);
                    notifyManager.notify(1, notifyBuilder.build());
                }
            }

            if (intent.getAction() != null && intent.getAction().equals(ACTION_CONTINUE_DOWN)
                    && downloadFileManager != null) {
                // 如果是来自，点击通知栏的继续下载
                if (intent.getAction() != null && intent.getAction().equals(ACTION_CONTINUE_DOWN)
                        && downloadFileManager != null) {
                    if (downloadFileManager.getStatus() == DownloadFileInfo.DOWNLOAD_PAUSE ||
                            downloadFileManager.getStatus() == DownloadFileInfo.FAILURE) {
                        downloadFileManager.startDownload();
                        Log.e("better123", "status: " + downloadFileManager.getStatus() + ", " +
                                "dd" + ", 流量下 继续下载");
                    }
                }
            }
        }
    }


    final static String ACTION_CONTINUE_DOWN = "com.mae.app_common_update_NotificationDisplay";

    public static int getNetWorkState(Context context) {
        // 得到连接管理器对象
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetworkInfo = connectivityManager
                .getActiveNetworkInfo();
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {

            if (activeNetworkInfo.getType() == (ConnectivityManager.TYPE_WIFI)) {
                return WIFI;
            } else if (activeNetworkInfo.getType() == (ConnectivityManager.TYPE_MOBILE)) {
                return MOBILE;
            }
        } else {
            return NONE;
        }
        return NONE;
    }

    static final int MOBILE = 1;
    static final int WIFI = 2;
    static final int NONE = 0;
}
