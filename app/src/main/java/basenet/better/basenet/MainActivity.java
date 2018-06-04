package basenet.better.basenet;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;

import basenet.better.basenet.bizDemo.handler.NetRequest_bak;
import basenet.better.basenet.mae.bundles.update.UpdateApkManager;
import basenet.better.basenet.v2.NongLiResponseHandler;
import basenet.better.basenet.v2.NongliBean;
import lib.basenet.NetUtils;
import lib.basenet.request.AbsRequestCallBack;
import lib.basenet.utils.FileUtils;
import okhttp3.MediaType;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("基础网络测试");

        findViewById(R.id.btn_volley).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), VolleyActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.btn_okhttp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), OkHttpActivity.class);
                startActivity(intent);
            }
        });

        findViewById(R.id.testMimeType).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String type = FileUtils.getMimeType("具体路径");
                MediaType.parse(type);
                Toast.makeText(getApplicationContext(), MediaType.parse(type).toString(), Toast.LENGTH_SHORT).show();
                test();
            }
        });

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateApkManager updateApkManager = new UpdateApkManager(MainActivity.this, NetUtils.getInstance().getOkHttpClient());
                updateApkManager.checkUpdate();
            }
        });

        findViewById(R.id.button_v2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v2();
            }
        });
    }

    private void test() {
    }

    public static String encodeBase64File(String path) throws Exception {
        File file = new File(path);
        FileInputStream inputFile = new FileInputStream(file);
        byte[] buffer = new byte[(int) file.length()];
        inputFile.read(buffer);
        inputFile.close();
        return Base64.encodeToString(buffer, Base64.NO_WRAP);
    }

    /**
     * 版本2.0
     * 注意，需要使用Gson
     */
    private void v2() {
        final String url = "https://www.sojson.com/open/api/lunar/json.shtml";
        new NetRequest_bak.Builder()
                .respHandler(new NongLiResponseHandler(NongliBean.class))
                .callback(new AbsRequestCallBack<NongliBean>() {
                    @Override
                    public void onSuccess(lib.basenet.response.Response<NongliBean> response) {
                        super.onSuccess(response);
                        Log.e("better", "" + response.responseBody.suit);
                    }

                    @Override
                    public void onFailure(Throwable e) {
                        super.onFailure(e);
                        Log.e("better", "error ： " + e.getMessage());
                    }
                }).url(url).build().request();

        // OkHttpRequest.Builder builder = new OkHttpRequest.Builder();
    }
}
