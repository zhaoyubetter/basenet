package basenet.better.basenet;

import android.app.Application;
import android.os.Environment;

import com.facebook.stetho.Stetho;
import com.facebook.stetho.okhttp3.StethoInterceptor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import lib.basenet.NetUtils;

/**
 * Created by zhaoyu1 on 2017/7/28.
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Stetho.initializeWithDefaults(this);
        // 全局配置NetConfig
        NetUtils.init(new NetUtils.Builder().cacheDir(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/basenet")
                .debug(true)
                .enablePostCache(true)
                .timeout(10).app(this).addNetInterceptor(new StethoInterceptor())
        );
    }
}
