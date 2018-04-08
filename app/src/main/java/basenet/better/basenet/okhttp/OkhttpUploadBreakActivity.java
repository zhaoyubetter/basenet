package basenet.better.basenet.okhttp;

import android.Manifest;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;

import basenet.better.basenet.R;
import basenet.better.basenet.utils.PermissionUtils;
import lib.basenet.okhttp.OkHttpRequest;
import lib.basenet.okhttp.upload_down.DownloadFileInfo;
import lib.basenet.okhttp.upload_down.FileSegmentInfo;
import lib.basenet.okhttp.upload_down.UploadDemoCode;
import lib.basenet.okhttp.upload_down.UploadFileManager;
import lib.basenet.request.AbsDownloadRequestCallback;
import lib.basenet.response.Response;

public class OkhttpUploadBreakActivity extends AppCompatActivity implements View.OnClickListener {

    UploadFileManager uploadFileManager;

    ProgressBar progress;
    TextView progressTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_okhttp_upload_break);

        findViewById(R.id.start).setOnClickListener(this);
        findViewById(R.id.upload).setOnClickListener(this);
        findViewById(R.id.cancel).setOnClickListener(this);
        findViewById(R.id.stop).setOnClickListener(this);

        progress = (ProgressBar) findViewById(R.id.progress);
        progressTV = (TextView) findViewById(R.id.progressTV);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.upload:
                PermissionUtils.checkOnePermission(this, Manifest.permission.READ_EXTERNAL_STORAGE, "权限申请", new Runnable() {
                    @Override
                    public void run() {
                        upload();
                    }
                });
                break;
            case R.id.start:
                if (uploadFileManager != null) {
                    uploadFileManager.start();
                }
                break;
            case R.id.cancel:
                if (uploadFileManager != null) {
                    uploadFileManager.delete();
                }
                uploadFileManager = null;
                break;
            case R.id.stop:
                if (uploadFileManager != null) {
                    uploadFileManager.stop();
                }
                break;
        }
    }

    private static final String OBTAIN_CODE_URL = "获取id的url";
    private static final String UPLOAD_FILE_URL = "上传的url";

    private void upload() {
        final String absolutePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        File file = new File(absolutePath + "/" + "Groovy in Action(EN).pdf");
        final FileSegmentInfo info = new FileSegmentInfo(file);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if(uploadFileManager != null) {
                        return;
                    }
                    // == 1.获取服务端id
                    UploadDemoCode.obtainFileCode(info, OBTAIN_CODE_URL);
                    // === 2.上传
                    uploadFileManager = new UploadFileManager(info.getSrcFile(), info.getServerFileID(), UPLOAD_FILE_URL, new AbsDownloadRequestCallback() {

                        @Override
                        public void onProgressUpdate(final long contentLength, final long bytesRead, boolean done) {
                            super.onProgressUpdate(contentLength, bytesRead, done);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progress.setProgress((int) (bytesRead * 1.0f / contentLength * 100));
                                    progressTV.setText(contentLength + "/" + bytesRead);
                                }
                            });
                        }


                        @Override
                        public void onStop(long currentUpload, long allCount, DownloadFileInfo info) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(OkhttpUploadBreakActivity.this, "Stop", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onSuccess(Response<String> response) {
                            super.onSuccess(response);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(OkhttpUploadBreakActivity.this, "上传OK", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onFailure(final Throwable e) {
                            super.onFailure(e);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(OkhttpUploadBreakActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });

                    uploadFileManager.start();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
