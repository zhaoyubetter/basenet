package basenet.better.basenet.okhttp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Map;

import basenet.better.basenet.R;
import lib.basenet.okhttp.OkHttpRequest;
import lib.basenet.request.AbsRequest;
import lib.basenet.request.AbsRequestCallBack;
import lib.basenet.response.Response;

public class OkhttpGetActivity extends AppCompatActivity implements View.OnClickListener {


	EditText url;

	Button get;

	TextView header;

	TextView content;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ok_http_get);

		url = (EditText) findViewById(R.id.url);
		get = (Button) findViewById(R.id.get);
		header = (TextView) findViewById(R.id.header);
		content = (TextView) findViewById(R.id.content);
		header.setMovementMethod(ScrollingMovementMethod.getInstance());
		content.setMovementMethod(ScrollingMovementMethod.getInstance());
		get.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		header.setText("");
		content.setText("");

		new OkHttpRequest.Builder().url(url.getText().toString())
				.type(AbsRequest.RequestType.GET)
				.callback(new AbsRequestCallBack<String>() {
					@Override
					public void onSuccess(final Response<String> response) {
						super.onSuccess(response);
						showHeader(response);
						showBody(response);
					}

					@Override
					public void onFailure(final Throwable e) {
						super.onFailure(e);
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
							}
						});
					}
				}).build().request();
	}

	private void showHeader(final Response<String> response) {
		if (response.responseHeader != null) {
			final StringBuilder sb = new StringBuilder();
			for (Map.Entry<String, String> entry : response.responseHeader.entrySet()) {
				sb.append(entry.getKey() + ": " + entry.getValue()).append("\n");
			}

			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					header.setText(sb.toString());
				}
			});
		}
	}

	private void showBody(final Response<String> response) {
		if (response.responseBody != null) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					content.setText(response.responseBody);
				}
			});
		}
	}

}
