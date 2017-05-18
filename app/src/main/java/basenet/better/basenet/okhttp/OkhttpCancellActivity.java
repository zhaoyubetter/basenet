package basenet.better.basenet.okhttp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Map;

import basenet.better.basenet.R;
import lib.basenet.NetUtils;
import lib.basenet.okhttp.OkHttpRequest;
import lib.basenet.request.AbsRequest;
import lib.basenet.request.AbsRequestCallBack;
import lib.basenet.response.Response;

/**
 *
 */
public class OkhttpCancellActivity extends AppCompatActivity implements View.OnClickListener {

	private final String TAG = "canel";

	EditText url;

	Button get;

	TextView header;

	TextView content;

	TextView error;

	EditText timeout;

	private AbsRequest request;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("Okhttp 取消请求(执行回调)");
		setContentView(R.layout.activity_ok_http_cancel);


		url = (EditText) findViewById(R.id.url);
		get = (Button) findViewById(R.id.get);
		header = (TextView) findViewById(R.id.header);
		content = (TextView) findViewById(R.id.content);
		timeout = (EditText) findViewById(R.id.ed_time);
		error = (TextView) findViewById(R.id.error);
		header.setMovementMethod(ScrollingMovementMethod.getInstance());
		content.setMovementMethod(ScrollingMovementMethod.getInstance());
		get.setOnClickListener(this);
		findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//				if (null != request) {
//					request.cancel();
//				}

				NetUtils.getInstance().cancel(TAG);
			}
		});
	}

	@Override
	public void onClick(View v) {
		content.setText("");
		error.setText("");
		header.setText("");

		long time = Integer.parseInt(timeout.getText().toString()) * 1000;


		// 需要设置tag
		request = new OkHttpRequest.Builder().url(
				url.getText().toString()
		)
				.type(AbsRequest.RequestType.GET).timeout(time).tag(TAG)
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
						error.setText(e.toString());
					}
				}).build();
		request.request();
	}

	private void showHeader(final Response<String> response) {
		if (response.responseHeader != null) {
			final StringBuilder sb = new StringBuilder();
			for (Map.Entry<String, String> entry : response.responseHeader.entrySet()) {
				sb.append(entry.getKey() + ": " + entry.getValue()).append("\n");
			}

			header.setText(sb.toString());
		}
	}

	private void showBody(final Response<String> response) {
		if (response.responseBody != null) {
			content.setText(response.responseBody);
		}
	}

}
