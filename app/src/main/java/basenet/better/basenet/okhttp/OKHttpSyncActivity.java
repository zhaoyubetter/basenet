package basenet.better.basenet.okhttp;

import android.Manifest;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

import basenet.better.basenet.R;
import basenet.better.basenet.utils.PermissionUtils;
import lib.basenet.NetUtils;
import lib.basenet.okhttp.OkHttpRequest;
import lib.basenet.request.AbsRequest;
import lib.basenet.request.AbsRequestCallBack;
import lib.basenet.request.BaseRequestBody;
import lib.basenet.response.Response;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;


public class OKHttpSyncActivity extends AppCompatActivity {

	final String TAG = "Sync";
	final String TAG_NORMAL = "tag_normal";

	/**
	 * 文件下载
	 */
	final int FILE_DOWN_MSG = 1;
	/**
	 * 普通网络请求
	 */
	final int NORMAL_REQUEST = 2;


	private EditText et_down;
	private Button sync_down;
	private ProgressBar progress;

	private HandlerThread mHandlerThread;
	private Handler mWorkHandler;
	private AbsRequest mRequest;

	// 普通网络请求
	private EditText et_normal_url;
	private Button btn_normal;
	private TextView tv_content;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("同步请求");
		setContentView(R.layout.activity_okhttp_sync);

		et_down = (EditText) findViewById(R.id.et_down);
		sync_down = (Button) findViewById(R.id.sync_down);
		progress = (ProgressBar) findViewById(R.id.progress);
		et_normal_url = (EditText) findViewById(R.id.et_normal_url);
		btn_normal = (Button) findViewById(R.id.btn_normal);
		tv_content = (TextView) findViewById(R.id.tv_content);
		tv_content.setMovementMethod(ScrollingMovementMethod.getInstance());

		initWorkThread();

		findViewById(R.id.sync_cancel).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// 下载文件如要取消 用  request 来取消
//				if (null != mRequest) {
//					mRequest.cancel();
//				}

				NetUtils.getInstance().cancel(TAG_NORMAL);
			}
		});

		sync_down.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PermissionUtils.checkOnePermission(OKHttpSyncActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, "权限申请", new Runnable() {
					@Override
					public void run() {
						mWorkHandler.sendEmptyMessage(FILE_DOWN_MSG);
					}
				});
			}
		});

		// 普通网络请求
		btn_normal.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				tv_content.setText("");
				mWorkHandler.sendEmptyMessage(NORMAL_REQUEST);
			}
		});


		// 测试 BaseRequestBody
		findViewById(R.id.btn_test).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				NetUtils.getInstance().cancelAll();
			}
		});
	}


	private void initWorkThread() {
		mHandlerThread = new HandlerThread("work_thread");
		mHandlerThread.start();

		mWorkHandler = new Handler(mHandlerThread.getLooper()) {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if (msg.what == FILE_DOWN_MSG) {
					realDown();
				} else if (msg.what == NORMAL_REQUEST) {
					normalRequest();
				}
			}
		};
	}

	private void normalRequest() {
		final Response<String> response = (Response<String>) new OkHttpRequest.Builder().url(et_normal_url.getText().toString())
				.tag(TAG_NORMAL).callback(new AbsRequestCallBack() {
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
								Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
							}
						});
					}

					@Override
					public void onCancel() {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								Toast.makeText(getApplicationContext(),"网络取消。。。", Toast.LENGTH_SHORT).show();
							}
						});
					}
				})
				// .cacheTime(5000)  // 缓存测试OK
				.build()
				.requestSync();
		// 请求成功了
		if (response != null) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					tv_content.setText(response.responseBody);
				}
			});
		}
	}


	// 同步请求
	private void realDown() {
		final String absolutePath = Environment.getExternalStorageDirectory().getAbsolutePath();
		final File file = new File(absolutePath + "/" + "headfirst2.apk");

		mRequest = new OkHttpRequest.Builder().url(et_down.getText().toString())
				.downFile(file)
				.callback(new AbsRequestCallBack<File>() {
					@Override
					public void onSuccess(Response<File> response) {
						super.onSuccess(response);
					}

					@Override
					public void onFailure(final Throwable e) {
						super.onFailure(e);
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
							}
						});
					}

					@Override
					public void onProgressUpdate(final long contentLength, final long bytesRead, final boolean done) {
						super.onProgressUpdate(contentLength, bytesRead, done);
						Log.e(TAG, Thread.currentThread().getName() + "");
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								progress.setProgress((int) (bytesRead * 1.0f / contentLength * 100));
							}
						});
					}
				}).tag(TAG).build();
		mRequest.requestSync();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mHandlerThread.quit();
	}
}
