package basenet.better.basenet;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import basenet.better.basenet.okhttp.OkhttpCacheActivity;
import basenet.better.basenet.okhttp.OkhttpCancellActivity;
import basenet.better.basenet.okhttp.OkhttpDownActivity;
import basenet.better.basenet.okhttp.OkhttpGetActivity;
import basenet.better.basenet.okhttp.OkhttpTimeoutActivity;
import basenet.better.basenet.okhttp.OkhttpUploadActivity;

public class OkHttpActivity extends AppCompatActivity implements View.OnClickListener {


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("Okhttp");
		setContentView(R.layout.activity_ok_http);
		findViewById(R.id.get).setOnClickListener(this);
		findViewById(R.id.timeout).setOnClickListener(this);
		findViewById(R.id.cancel).setOnClickListener(this);
		findViewById(R.id.upload).setOnClickListener(this);
		findViewById(R.id.download).setOnClickListener(this);
		findViewById(R.id.cache).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		Intent intent = null;
		switch (v.getId()) {
			case R.id.get:
				intent = new Intent(this, OkhttpGetActivity.class);
				break;
			case R.id.timeout:
				intent = new Intent(this, OkhttpTimeoutActivity.class);
				break;
			case R.id.cancel:
				intent = new Intent(this, OkhttpCancellActivity.class);
				break;
			case R.id.upload:
				intent = new Intent(this, OkhttpUploadActivity.class);
				break;
			case R.id.download:
				intent = new Intent(this, OkhttpDownActivity.class);
				break;
			case R.id.cache:
				intent = new Intent(this, OkhttpCacheActivity.class);
				break;
		}
		if (intent != null) {
			startActivity(intent);
		}
	}

}
