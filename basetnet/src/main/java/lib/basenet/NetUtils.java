package lib.basenet;

import android.app.Application;
import android.text.TextUtils;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import lib.basenet.okhttp.cache.NetCacheInterceptor;
import lib.basenet.okhttp.cache.PostCacheInterceptor;
import lib.basenet.okhttp.log.LoggerInterceptor;
import lib.basenet.utils.HttpsUtils;
import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

/**
 * 全局配置
 * Created by zhaoyu on 2017/4/18.
 * <p>
 * <p>
 * 修改记录：2017/5/18
 * 1. 修改类名为：NetUtils
 * 2. Okhttpclient 的创建，移到这里
 * 3. 新增全局取消方法 cancel
 */
public final class NetUtils {

    /**
     * 默认10s
     */
    private static final int DEFAULT_TIME_OUT = 10 * 1000;

    /**
     * 默认缓存大小
     */
    private static final int DEFAULT_CACHE_SIZE = 25 * 1024 * 1024;

    /**
     * 默认25mb
     */
    private int cacheSize;

    /**
     * 超时时间
     */
    private int timeOut;

    /**
     * 缓存目录
     */
    private String cacheDir;

    /**
     * Application
     */
    private Application application;

    /**
     * 启动Post缓存
     */
    private boolean isPostCache;

    /**
     * debug 模式
     */
    private boolean isDebug;

    /**
     * 应用拦截器
     */
    private List<Interceptor> interceptors;

    /**
     * 网络拦截器
     */
    private List<Interceptor> netInterceptors;

    // ssl 支持
    private SSLSocketFactory sSLSocketFactory;
    private X509TrustManager trustManager;

    /**
     * 单例
     */
    private static NetUtils instance;

    /**
     * client 对象
     */
    private OkHttpClient sOkHttpClient;

    /**
     * 全局初始化,构建自己的 NetUtils 对象
     *
     * @param builder
     */
    public static final void init(Builder builder) {
        if (instance == null) {
            instance = builder.build();
        }
    }

    /**
     * 如果没有调用 init 方法，直接获取实例，将取得默认实例
     *
     * @return
     */
    public static NetUtils getInstance() {
        if (instance == null) {
            instance = new Builder().debug(false).enablePostCache(true).build();
        }
        return instance;
    }

    /**
     * 取消请求
     *
     * @param tag
     */
    public void cancel(Object tag) {
        if (null != tag) {
            for (Call call : getOkHttpClient().dispatcher().queuedCalls()) {
                if (tag.equals(call.request().tag()))
                    call.cancel();
            }

            for (Call call : getOkHttpClient().dispatcher().runningCalls()) {
                if (tag.equals(call.request().tag()))
                    call.cancel();
            }
        }
    }

    /**
     * 取消全部
     */
    public void cancelAll() {
        for (Call call : getOkHttpClient().dispatcher().queuedCalls()) {
            call.cancel();
        }

        for (Call call : getOkHttpClient().dispatcher().runningCalls()) {
            call.cancel();
        }
    }


    public Application getApplication() {
        return application;
    }

    /**
     * 是否debug，debug会进行一些日志输出
     *
     * @return
     */
    public boolean isDebug() {
        return isDebug;
    }

    /**
     * 超时时间
     *
     * @return
     */
    public int getTimeOut() {
        return timeOut;
    }

    /**
     * 缓存大小
     *
     * @return
     */
    public int getCacheSize() {
        return cacheSize;
    }

    /**
     * 获取缓存目录
     */
    public String getCacheDir() {
        return cacheDir;
    }


    /**
     * 是否启动post缓存
     *
     * @return
     */
    public boolean isEnablePostCache() {
        return isPostCache;
    }

    /**
     * 反射获取Applicaton
     *
     * @return
     */
    private Application getApp() {
        try {
            Class<?> activityThread = Class.forName("android.app.ActivityThread");
            Method method = activityThread.getMethod("currentActivityThread");
            final Object invoke = method.invoke(activityThread);
            Method method2 = invoke.getClass().getMethod("getApplication");
            application = (Application) method2.invoke(invoke);                       // 获取context
            return application;
        } catch (Exception e) {
            throw new RuntimeException("获取context 失败！");
        }
    }

    /**
     * 私有构造
     *
     * @param builder
     */
    private NetUtils(Builder builder) {
        if (builder.app != null) {
            application = builder.app;
        } else {
            application = getApp();
        }

        // 缓存目录
        if (null != builder.cacheDir && !TextUtils.isEmpty(builder.cacheDir)) {
            cacheDir = builder.cacheDir;
        } else {
            File file = new File(application.getCacheDir(), "response");
            if (!file.exists()) {
                file.mkdirs();
            }
            cacheDir = file.getAbsolutePath() + "/response";
        }

        // 缓存大小
        if (builder.cacheSize > 0) {
            cacheSize = builder.cacheSize;
        } else {
            cacheSize = DEFAULT_CACHE_SIZE;
        }

        // 超时时间
        if (builder.timeout > 0)
            this.timeOut = builder.timeout;
        else
            this.timeOut = DEFAULT_TIME_OUT;

        // 是否debug模式
        this.isDebug = builder.debug;

        // 是否启用post缓存
        this.isPostCache = builder.postCache;
        this.netInterceptors = builder.netInterceptors;
        this.interceptors = builder.interceptors;

        // ssl
        this.sSLSocketFactory = builder.sSLSocketFactory;
        this.trustManager = builder.trustManager;
    }

    public OkHttpClient getOkHttpClient() {
        if (sOkHttpClient == null) {
            createHttpClient();
        }
        return sOkHttpClient;
    }

    private void createHttpClient() {
        final OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(getTimeOut(), TimeUnit.MILLISECONDS);
        builder.readTimeout(getTimeOut(), TimeUnit.MILLISECONDS);
        builder.writeTimeout(getTimeOut(), TimeUnit.MILLISECONDS);

		/* ==设置拦截器== */
        // 设置缓存
        File cacheDir = new File(getCacheDir());
        // GET 形式缓存设置
        Cache cache = new Cache(cacheDir, getCacheSize());
        builder.cache(cache).addNetworkInterceptor(new NetCacheInterceptor());        // 设置缓存拦截器

        // 日志拦截
        if (isDebug()) {
            builder.addInterceptor(new LoggerInterceptor());
        }
        // 是否允许POST 形式缓存设置
        if (isEnablePostCache()) {
            builder.addInterceptor(new PostCacheInterceptor());
        }

        // 应用拦截
        if (interceptors != null) {
            for (Interceptor i : interceptors) {
                builder.addInterceptor(i);
            }
        }

        // net拦截
        if(netInterceptors != null) {
            for (Interceptor i : netInterceptors) {
                builder.addNetworkInterceptor(i);
            }
        }

        // ssl，默认设置可访问所有的https网站
        if(sSLSocketFactory == null && trustManager == null) {
            HttpsUtils.SSLParams sslParams = HttpsUtils.getSslSocketFactory(null, null, null);
            builder.sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager);
        } else {
            builder.sslSocketFactory(sSLSocketFactory, trustManager);
        }

        sOkHttpClient = builder.build();
    }

    // =====================================================================
    // =====================================================================

    /**
     * 使用Builder模式
     */
    public static final class Builder {

        private NetUtils sConfig;      // 配置
        private Application app;
        private String cacheDir;
        private int cacheSize;
        private boolean debug;
        private int timeout;
        private boolean postCache;
        private List<Interceptor> interceptors;        // 应用拦截器
        private List<Interceptor> netInterceptors;    // 网络拦截器

        // ssl 支持
        private SSLSocketFactory sSLSocketFactory;
        private X509TrustManager trustManager;

        /**
         * 确保只初始化一次
         *
         * @return
         */
        public NetUtils build() {
            if (sConfig == null) {
                synchronized (Builder.this) {
                    if (sConfig == null) {
                        sConfig = new NetUtils(this);
                    }
                }
            }
            return sConfig;
        }

        /**
         * 设置Application
         *
         * @param application
         * @return
         */
        public Builder app(Application application) {
            this.app = application;
            return this;
        }

        public Builder addInterceptor(Interceptor interce) {
            if(this.interceptors == null) {
                interceptors = new ArrayList<>();
            }
            interceptors.add(interce);
            return this;
        }

        public Builder addNetInterceptor(Interceptor interce) {
            if(this.netInterceptors == null) {
                netInterceptors = new ArrayList<>();
            }
            netInterceptors.add(interce);
            return this;
        }

        /**
         * 缓存大小，MB为单位
         *
         * @param cacheSize
         * @return
         */
        public Builder cacheSize(int cacheSize) {
            this.cacheSize = cacheSize * 1024 * 1024;
            return this;
        }

        /**
         * 是否debug
         *
         * @param debug
         * @return
         */
        public Builder debug(boolean debug) {
            this.debug = debug;
            return this;
        }

        /**
         * 缓存目录
         *
         * @param cacheDir
         * @return
         */
        public Builder cacheDir(String cacheDir) {
            this.cacheDir = cacheDir;
            return this;
        }

        /**
         * 秒为单位
         *
         * @param timeout
         * @return
         */
        public Builder timeout(int timeout) {
            this.timeout = timeout * 1000;
            return this;
        }

        /**
         * post 缓存
         *
         * @return
         */
        public Builder enablePostCache(boolean postCache) {
            this.postCache = postCache;
            return this;
        }

        public Builder ssl(SSLSocketFactory factory, X509TrustManager mgr) {
            this.sSLSocketFactory = factory;
            this.trustManager = mgr;
            return this;
        }
    }
}
