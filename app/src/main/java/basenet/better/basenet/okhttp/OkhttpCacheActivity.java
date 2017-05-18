package basenet.better.basenet.okhttp;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import basenet.better.basenet.R;
import lib.basenet.NetUtils;
import lib.basenet.okhttp.OkHttpRequest;
import lib.basenet.request.AbsRequest;
import lib.basenet.request.AbsRequestCallBack;
import lib.basenet.response.Response;

public class OkhttpCacheActivity extends AppCompatActivity {


	EditText url;
	TextView message;
	TextView header;
	EditText cache_time;
	TextView cache_info;
	boolean mIsGet = true;

	private void clear(String dir) {
		File file = new File(dir);
		if (file.exists()) {
			if (file.isDirectory()) {
				File[] files = file.listFiles();
				for (File f : files) {
					clear(f.getAbsolutePath());
				}
			} else {
				file.delete();
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_okhttp_cache);
		url = (EditText) findViewById(R.id.url);
		message = (TextView) findViewById(R.id.message);
		cache_time = (EditText) findViewById(R.id.cache_time);
		header = (TextView) findViewById(R.id.header);
		cache_info = (TextView) findViewById(R.id.cache_info);

		header.setMovementMethod(ScrollingMovementMethod.getInstance());
		message.setMovementMethod(ScrollingMovementMethod.getInstance());
		findViewById(R.id.clearCache).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				clear(NetUtils.getInstance().getCacheDir());
			}
		});

		findViewById(R.id.force).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				clear();
				getData(true);
				setTitle("当前：" + (mIsGet ? "GET " : "POST") + " 强制刷新");
			}
		});

		findViewById(R.id.get).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				clear();
				mIsGet = true;
				getData(false);
				setTitle("当前：" + (mIsGet ? "GET " : "POST"));
			}
		});

		findViewById(R.id.post).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				clear();
				mIsGet = false;
				getData(false);
				setTitle("当前：" + (mIsGet ? "GET " : "POST"));
			}
		});


		// 全局配置NetConfig
		NetUtils.init(new NetUtils.Builder().cacheDir(Environment.getExternalStorageDirectory().getAbsolutePath() + "/basenet")
				.debug(true).enablePostCache(true).timeout(10).app(getApplication())
		);
	}

	private void getData(boolean isforce) {
		int cacheTime = Integer.parseInt(cache_time.getText().toString());
		Map<String, String> params = new HashMap<>();
		params.put("hello", "I'm better");
		params.put("hello1", "I'm better");
		params.put("hello5", "I'm better -- ");

		new OkHttpRequest.Builder().url(url.getText().toString()).cacheTime(cacheTime).type(mIsGet ? AbsRequest.RequestType.GET : AbsRequest.RequestType.POST).body(params).forceRefresh(isforce)
				.callback(new AbsRequestCallBack<String>() {
					@Override
					public void onSuccess(Response<String> response) {
						super.onSuccess(response);
						showHeader(response);
						message.setText(response.responseBody);
						if (response.isFromCache) {
							cache_info.setText("来自缓存");
						} else {
							cache_info.setText("来自 -- 》 网络");
						}
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
		cache_info.setText("");
	}


}
