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
import lib.basenet.okhttp.OkHttpRequest;
import lib.basenet.request.AbsRequest;
import lib.basenet.request.AbsRequestCallBack;
import lib.basenet.response.Response;

public class OkhttpDownActivity extends AppCompatActivity {

	final String TAG = "down";

	EditText et_down_url;
	ProgressBar progress;
	TextView progressTV;
	TextView error;

	AbsRequest request;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_okhttp_down);

		et_down_url = (EditText) findViewById(R.id.et_down_url);
		progress = (ProgressBar) findViewById(R.id.progress);
		progressTV = (TextView) findViewById(R.id.progressTV);
		error = (TextView) findViewById(R.id.error);
		progress.setMax(100);

		et_down_url.setText("http://storage.jd.com/jd.jme.production.client/JDME_3.3.0.apk");

		findViewById(R.id.upload).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				upload();
			}
		});

		findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				NetUtils.getInstance().cancel(TAG);
				//if(request != null) {
				//	request.cancel();
				//}
			}
		});
	}


	private void upload() {
		PermissionUtils.checkOnePermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, "权限申请", new Runnable() {
			@Override
			public void run() {
				realUpload();
			}
		});
	}

	private void realUpload() {
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
}
