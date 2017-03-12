package basenet.better.basenet;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import lib.basenet.request.AbsRequest;
import lib.basenet.request.AbsRequestCallBack;
import lib.basenet.volley.VolleyRequest;

public class VolleyActivity extends AppCompatActivity implements View.OnClickListener {

	EditText url;
	EditText timeout;

	Button get;
	Button post;

	TextView result;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_volley);

		url = (EditText) findViewById(R.id.url);
		timeout = (EditText) findViewById(R.id.timeout);
		get = (Button) findViewById(R.id.get);
		post = (Button) findViewById(R.id.post);
		result = (TextView) findViewById(R.id.result);
		result.setMovementMethod(ScrollingMovementMethod.getInstance());

		get.setOnClickListener(this);
		post.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		result.setText("");
		long tTimeout = Integer.parseInt(timeout.getText().toString()) * 1000;
		String tUrl = url.getText().toString();

		final AbsRequest.Builder builder = new VolleyRequest.Builder(getApplicationContext())
				.timeout(tTimeout)
				.url(tUrl)
				.callback(new AbsRequestCallBack() {
					@Override
					public void onSuccess(final Object o) {
						super.onSuccess(o);
						result.post(new Runnable() {
							@Override
							public void run() {
								result.setText(o.toString());
							}
						});
					}

					@Override
					public void onFailure(final Throwable e) {
						super.onFailure(e);
						result.post(new Runnable() {
							@Override
							public void run() {
								result.setText(e.toString());
							}
						});
					}
				});

		if (R.id.get == v.getId()) {
			builder.type(AbsRequest.RequestType.GET).build().request();
		} else if (R.id.post == v.getId()) {
			upload();
		}
	}

	private void upload() {
		Map<String, String> params = new HashMap<>();
		params.put("key1", "value1");
		params.put("key2", "value2");

		final String absolutePath = Environment.getExternalStorageDirectory().getAbsolutePath();
		File file = new File(absolutePath + "/DCIM/Camera/11.jpg");

		Map<String, File> uploadFiles = new HashMap<>();
		uploadFiles.put("image", file);

		final AbsRequest.Builder builder = new VolleyRequest.Builder(getApplicationContext());
		builder.url("http://192.168.0.108:8080/myTestWeb/UploadServlet")
				.type(AbsRequest.RequestType.POST).callback(new AbsRequestCallBack() {
			@Override
			public void onSuccess(final Object o) {
				super.onSuccess(o);
				result.post(new Runnable() {
					@Override
					public void run() {
						result.setText(o.toString());
					}
				});
			}

			@Override
			public void onFailure(final Throwable e) {
				super.onFailure(e);
				result.post(new Runnable() {
					@Override
					public void run() {
						result.setText(e.toString());
					}
				});
			}

			@Override
			public void onProgressUpdate(long contentLength, long bytesRead, boolean done) {
				super.onProgressUpdate(contentLength, bytesRead, done);
			}
		})
				.body(params).uploadFiles(uploadFiles).build().request();
	}
}
