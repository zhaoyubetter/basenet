package basenet.better.basenet;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import lib.basenet.NetUtils;
import lib.basenet.okhttp.OkHttpRequest;
import lib.basenet.request.AbsRequestCallBack;
import lib.basenet.request.IRequest;
import lib.basenet.utils.FileUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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
}
