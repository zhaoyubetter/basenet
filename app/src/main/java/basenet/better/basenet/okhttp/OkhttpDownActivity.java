package basenet.better.basenet.okhttp;

import android.Manifest;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import basenet.better.basenet.R;
import basenet.better.basenet.utils.PermissionUtils;
import lib.basenet.NetUtils;
import lib.basenet.okhttp.DownloadFileInfo;
import lib.basenet.okhttp.DownloadFileManager;
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

    final String downUrl = "http://111.231.206.52:8080/yu/hehe.jpg";

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
            case R.id.down:
                PermissionUtils.checkOnePermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, "权限申请", new Runnable() {
                    @Override
                    public void run() {
//				down();
                        downPart();
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
                downloadFileManager.deleteDownload();
                if (downloadFileManager != null) {
                    downloadFileManager.startDownload();
                }
                break;
        }
    }

    private DownloadFileManager downloadFileManager;

    private void downPart() {
        if(downloadFileManager != null && downloadFileManager.getStatus() == DownloadFileInfo.DOWNLOADING ) {
            return;
        }

        DownloadFileInfo downloadFileInfo = new DownloadFileInfo(et_down_url.getText().toString(),
                Environment.getExternalStorageDirectory().getAbsolutePath() + "/aaa.jpq");
        downloadFileManager = new DownloadFileManager(downloadFileInfo, new AbsDownloadRequestCallback() {
            @Override
            public void onSuccess(final Response<String> response) {
                super.onSuccess(response);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
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
                        progress.setProgress((int) (bytesRead * 1.0f / contentLength * 100));
                        progressTV.setText(contentLength + "/" + bytesRead);
                    }
                });
            }
        });
        downloadFileManager.startDownload();
    }
}
