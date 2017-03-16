package basenet.better.basenet;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import basenet.better.basenet.volley.VolleyCancelActivity;
import basenet.better.basenet.volley.VolleyGetActivity;
import basenet.better.basenet.volley.VolleyTimeoutActivity;

/**
 * http://httpbin.org/delay/0
 */
public class VolleyActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_volley);

		findViewById(R.id.get).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), VolleyGetActivity.class);
				startActivity(intent);
			}
		});

		findViewById(R.id.timeout).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), VolleyTimeoutActivity.class);
				startActivity(intent);
			}
		});

		findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), VolleyCancelActivity.class);
				startActivity(intent);
			}
		});
	}
}
