package basenet.better.basenet.okhttp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.util.Map;

import basenet.better.basenet.R;
import lib.basenet.config.NetConfig;
import lib.basenet.okhttp.OkHttpRequest;
import lib.basenet.request.AbsRequest;
import lib.basenet.request.AbsRequestCallBack;
import lib.basenet.response.Response;

public class OkhttpCacheActivity extends AppCompatActivity {


	EditText url;
	TextView message;
	TextView header;
	EditText cache_time;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_okhttp_cache);
		url = (EditText) findViewById(R.id.url);
		message = (TextView) findViewById(R.id.message);
		cache_time = (EditText) findViewById(R.id.cache_time);
		header = (TextView) findViewById(R.id.header);
		header.setMovementMethod(ScrollingMovementMethod.getInstance());
		message.setMovementMethod(ScrollingMovementMethod.getInstance());
		findViewById(R.id.clearCache).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				File file = new File(NetConfig.getCacheDir());
				if (file.exists()) {
					if (file.isDirectory()) {
						File[] files = file.listFiles();
						for (File f : files) {
							f.delete();
						}
					}
				}
			}
		});

		findViewById(R.id.force).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				clear();
				getData(true);
			}
		});

		findViewById(R.id.get).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				clear();
				getData(false);
			}
		});

		findViewById(R.id.post).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
			}
		});
	}

	private void getData(boolean isforce) {
		int cacheTime = Integer.parseInt(cache_time.getText().toString());
		new OkHttpRequest.Builder().url(url.getText().toString()).cacheTime(cacheTime).type(AbsRequest.RequestType.GET).forceRefresh(isforce)
				.callback(new AbsRequestCallBack<String>() {
					@Override
					public void onSuccess(Response<String> response) {
						super.onSuccess(response);
						showHeader(response);
						message.setText(response.responseBody);

					}

					@Override
					public void onFailure(Throwable e) {
						super.onFailure(e);
						message.setText(e.toString());

					}
				}).build().request();
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

	private void clear() {
		header.setText("");
		message.setText("");
	}


}
