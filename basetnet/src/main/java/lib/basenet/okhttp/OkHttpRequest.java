package lib.basenet.okhttp;


import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import lib.basenet.NetUtils;
import lib.basenet.request.AbsRequest;
import lib.basenet.utils.FileUtils;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * d
 * okhttp 类
 * Created by zhaoyu1 on 2017/3/7.
 * <p>
 * addOrUpdate log: 2017/05/18
 * 1.下载文件时，更新监听下载文件进度方法，修复，下载无法cancel问题；
 * 2.将okhttpclient的创建方法移入 {@link NetUtils#getOkHttpClient()}
 * <p>
 * addOrUpdate log : 2017/05/19
 * 1.新增，同步请求，新增 {@link #mIsSync} 成员变量
 * 2.同步请求，请使用 Request.cancel()进行请求取消
 * 2018-04-09
 * 1. 修复同步请求时，没有设置Header问题
 */
public class OkHttpRequest extends AbsRequest {

    private static final OkHttpClient sOkHttpClient;

    /**
     * 记录Call，方便取消
     */
    private Call mCall;
    /**
     * deliverHandler
     */
    private Handler mDeliverHandler = new Handler(Looper.getMainLooper());

    /**
     * 是否同步
     */
    private boolean mIsSync = false;

    static {
        sOkHttpClient = NetUtils.getInstance().getOkHttpClient();
    }

    public OkHttpRequest(Builder builder) {
        super(builder);
    }

    /**
     * 主线程中回调
     *
     * @param runnable
     */
    private void deliverCallBack(final Runnable runnable) {
        mDeliverHandler.post(runnable);
    }

    /**
     * 下载文件
     *
     * @param tBuilder
     */
    private void downFile(Request.Builder tBuilder) {
        // 执行下载逻辑
        mCall = getClient().newCall(tBuilder.build());
        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(final Call call, final IOException e) {
                if (null != mCallBack) {  // not cancel,so it's real exception
                    if (call != null && call.isCanceled()) {
                        deliverCallBack(new Runnable() {
                            @Override
                            public void run() {
                                mCallBack.onCancel();
                            }
                        });
                    } else {
                        deliverCallBack(new Runnable() {
                            @Override
                            public void run() {
                                mCallBack.onFailure(e);
                            }
                        });
                    }
                }
            }

            @Override
            public void onResponse(final Call call, final Response response) {
                if (null != mCallBack) {
                    final lib.basenet.response.Response myResponse = new lib.basenet.response.Response(OkHttpRequest.this, getResponseHeaders(response), mDownFile);
                    try {
                        parseFileDownResponse(response);
                    } catch (final IOException e) {
                        deliverCallBack(new Runnable() {
                            @Override
                            public void run() {
                                mCallBack.onFailure(e);
                            }
                        });
                        return;
                    }

                    myResponse.statusCode = response.code();
                    myResponse.message = response.message();
                    deliverCallBack(new Runnable() {
                        @Override
                        public void run() {
                            mCallBack.onSuccess(myResponse);
                        }
                    });
                }
            }
        });
    }

    /**
     * 处理response
     */
    private void parseFileDownResponse(final Response response) throws IOException {
        final long total = response.body().contentLength();

        long lastRefreshTime = System.currentTimeMillis();

        FileOutputStream fos = null;
        InputStream ips = null;
        byte[] buf = new byte[4096];
        int len = 0;
        long sum = 0;

        try {
            ips = response.body().byteStream();
            fos = new FileOutputStream(mDownFile);
            while ((len = ips.read(buf)) != -1) {
                sum += len;
                fos.write(buf, 0, len);

                // 进度
                if (null != mCallBack) {
                    final long currentBytes = sum;
                    long curTime = System.currentTimeMillis();
                    if (curTime - lastRefreshTime >= 150 || currentBytes == total) {      // 每隔150毫米 or 下载完成 刷新一次
                        if (mIsSync) {
                            mCallBack.onProgressUpdate(total, currentBytes, currentBytes == total);
                        } else {
                            deliverCallBack(new Runnable() {
                                @Override
                                public void run() {
                                    mCallBack.onProgressUpdate(total, currentBytes, currentBytes == total);
                                }
                            });
                        }

                        lastRefreshTime = System.currentTimeMillis();
                    }
                }
            }

            fos.flush();
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                if (ips != null)
                    ips.close();
                if (fos != null)
                    fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private OkHttpClient getClient() {
        // 判断此次请求，超时时间是否不同，如果不同，copy Client
        OkHttpClient tClient = sOkHttpClient;
        if (mTimeOut >= 10 && mTimeOut != NetUtils.getInstance().getTimeOut()) {
            final OkHttpClient.Builder builder = sOkHttpClient.newBuilder().connectTimeout(mTimeOut, TimeUnit.MILLISECONDS).readTimeout(mTimeOut, TimeUnit.MILLISECONDS)
                    .writeTimeout(mTimeOut, TimeUnit.MILLISECONDS);
            tClient = builder.build();
        }

        return tClient;
    }

    private void setHeader(Request.Builder tBuilder) {
        // 设置Header
        if (mHeader != null && mHeader.size() > 0) {
            for (Map.Entry<String, String> entry : mHeader.entrySet()) {
                tBuilder.header(entry.getKey(), entry.getValue());
            }
        }
    }

    private void realRequest(Request.Builder tBuilder) {
        // 1.设置Header
        setHeader(tBuilder);

        // 如果是下载文件，不设置缓存
        if (mDownFile != null) {
            downFile(tBuilder);
            return;
        }

        // 2.获取Client
        OkHttpClient tClient = getClient();
        // 3.设置request 缓存
        setCache(tBuilder);

        final Request request = tBuilder.build();       // 创建request

        // 走异步
        mCall = tClient.newCall(request);
        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                if (null != mCallBack) {  // not cancel,so it's real exception
                    if (call != null && call.isCanceled()) {
                        deliverCallBack(new Runnable() {
                            @Override
                            public void run() {
                                mCallBack.onCancel();
                            }
                        });
                    } else {
                        deliverCallBack(new Runnable() {
                            @Override
                            public void run() {
                                mCallBack.onFailure(e);
                            }
                        });
                    }
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (null != mCallBack) {
                    final String returnBody = response.body().string();    // 字符串响应体
                    final lib.basenet.response.Response myResponse = new lib.basenet.response.Response(OkHttpRequest.this, getResponseHeaders(response), returnBody);
                    myResponse.statusCode = response.code();
                    myResponse.message = response.message();
                    //Log.e("okhttp cache", "" + response.cacheResponse());        // 缓存
                    //Log.e("okhttp net", "" + response.networkResponse());        // 服务器中
                    myResponse.isFromCache = response.networkResponse() == null;
                    deliverCallBack(new Runnable() {
                        @Override
                        public void run() {
                            mCallBack.onSuccess(myResponse);
                        }
                    });
                }
            }
        });
    }

    /**
     * 同步请求
     *
     * @param tBuilder
     * @return
     */
    private lib.basenet.response.Response realRequestSync(Request.Builder tBuilder) {
        // 设置Header
        setHeader(tBuilder);

        if (mDownFile != null) {
            return downFileSync(tBuilder);
        }

        // 获取Client
        OkHttpClient tClient = getClient();
        // 设置request 缓存
        setCache(tBuilder);
        final Request request = tBuilder.build();       // 创建request

        mCall = tClient.newCall(request);

        lib.basenet.response.Response myResponse = null;
        try {
            Response response = mCall.execute();
            final String returnBody = response.body().string();    // 字符串响应体
            myResponse = new lib.basenet.response.Response(OkHttpRequest.this, getResponseHeaders(response), returnBody);
            myResponse.statusCode = response.code();
            myResponse.message = response.message();
            myResponse.isFromCache = response.networkResponse() == null;
            if (null != mCallBack) {
                mCallBack.onSuccess(myResponse);
            }
        } catch (final IOException e) {
            if (null != mCallBack) {  // not cancel,so it's real exception
                if (mCall != null && mCall.isCanceled()) {
                    mCallBack.onCancel();
                } else {
                    mCallBack.onFailure(e);
                }
            }
        }

        return myResponse;
    }

    /**
     * 同步下载文件
     *
     * @param tBuilder
     */
    private lib.basenet.response.Response downFileSync(Request.Builder tBuilder) {
        // 执行下载逻辑
        mCall = getClient().newCall(tBuilder.build());
        lib.basenet.response.Response myResponse = null;

        try {
            Response response = mCall.execute();
            try {
                parseFileDownResponse(response);
            } catch (final IOException e) {
                if (null != mCallBack) {  // not cancel,so it's real exception
                    if (mCall != null && mCall.isCanceled()) {
                        mCallBack.onCancel();
                    } else {
                        mCallBack.onFailure(e);
                    }
                }
                return null;
            }

            myResponse = new lib.basenet.response.Response(OkHttpRequest.this, getResponseHeaders(response), mDownFile);
            myResponse.statusCode = response.code();
            myResponse.message = response.message();
            if (null != mCallBack) {
                mCallBack.onSuccess(myResponse);
            }
        } catch (IOException e) {
            if (null != mCallBack) {  // not cancel,so it's real exception
                if (mCall != null && mCall.isCanceled()) {
                    mCallBack.onCancel();
                } else {
                    mCallBack.onFailure(e);
                }
            }
        }

        return myResponse;
    }

    /**
     * 设置缓存
     */
    private void setCache(final Request.Builder tBuilder) {
        if (mIsForceRefresh) {
            // 1.情形一：强制刷新，从网络上获取(强制刷新时，如果有 mCacheTime，也马上缓存)
            tBuilder.cacheControl(new CacheControl.Builder().maxAge(mCacheTime, TimeUnit.SECONDS).noCache().build()).build();
        } else if (mCacheTime > 0) {
            // 2.情形二：设置缓存时间  注意：当重复请求时，缓存的起点时间是第一次请求成功的时间
            tBuilder.cacheControl(new CacheControl.Builder().maxAge(mCacheTime, TimeUnit.SECONDS).build()).build();
        } else {
            // 3.情形三（默认）：不缓存,也不存储 (如：用户登录接口、获取验证码等)
            tBuilder.cacheControl(new CacheControl.Builder().noCache().noStore().build()).build();
        }
    }

    @Override
    protected void get() {
        if (mUploadFiles != null || mRequestBody != null) {
            post();
        } else {
            Request.Builder tBuilder = new Request.Builder();
            tBuilder.get().url(generateUrl(mUrl, mParams)).tag(mTag);
            realRequest(tBuilder);
        }
    }

    @Override
    protected void post() {
        Request.Builder tBuilder = new Request.Builder();
        tBuilder.url(mUrl).tag(mTag).post(getRequestBody());
        realRequest(tBuilder);
    }

    @Override
    public void cancel() {
        if (mCall != null && !mCall.isCanceled()) {
            mCall.cancel();
        }
    }

    /**
     * 同步请求
     *
     * @return
     */
    @Override
    public lib.basenet.response.Response requestSync() {
        mIsSync = true;        // 新增成员变量

        int type = mReqType;

        lib.basenet.response.Response myResponse = null;
        Request.Builder tBuilder = new Request.Builder();

        // 避免出错，再次判断类型
        if (mUploadFiles != null) {
            type = RequestType.POST;
        }

        switch (type) {
            case RequestType.GET: {
                tBuilder.get().url(generateUrl(mUrl, mParams)).tag(mTag);
                myResponse = realRequestSync(tBuilder);
                break;
            }
            case RequestType.POST: {
                tBuilder.url(mUrl).tag(mTag).post(getRequestBody());
                myResponse = realRequestSync(tBuilder);
                break;
            }
        }

        return myResponse;

    }

    /**
     * post 请求体, 必须有一个请求体，否则报异常
     *
     * @return
     */
    private RequestBody getRequestBody() {
        RequestBody requestBody = null;

        // 2017-11-24 新增，支持外界设置RequestBody,并直接返回
        if (null != mRequestBody) {
            return getWrapperRequestBody(RequestBody.create(MediaType.parse(mRequestBody.getBodyContentType()), mRequestBody.getBody()));
        }

        // 上传文件部分 (参数使用 MultipartBody 来构建)
        if (null != mUploadFiles && mUploadFiles.size() > 0) {
            MultipartBody.Builder builder = new MultipartBody.Builder();
            builder.setType(MultipartBody.FORM);

            if (null != mParams && mParams.size() > 0) {
                for (Map.Entry<String, Object> entry : mParams.entrySet()) {
                    final Object value = entry.getValue();
                    if (value == null) {
                        continue;
                    }
                    builder.addFormDataPart(entry.getKey(), value.toString());
                }
            }

            for (Map.Entry<String, File> entry : mUploadFiles.entrySet()) {
                final MediaType mediaType = MediaType.parse(FileUtils.getMimeType(entry.getValue().getAbsolutePath()));
                builder.addFormDataPart(entry.getKey(), entry.getValue().getName(), RequestBody.create(mediaType, entry.getValue()));
            }

            // 包装一下，支持进度
            requestBody = getWrapperRequestBody(builder.build());
        } else {    // 普通表单
            // 2017-08-18 修正 bug，如果没有文件上传，使用默认FormBody方式
            FormBody.Builder formBuilder = new FormBody.Builder();
            if (null != mParams && mParams.size() > 0) {
                for (Map.Entry<String, Object> entry : mParams.entrySet()) {
                    final Object value = entry.getValue();
                    if (value == null) {
                        continue;
                    }
                    formBuilder.add(entry.getKey(), value.toString());
                }
            }
            requestBody = formBuilder.build();
        }

        try {
            if (requestBody == null || requestBody.contentLength() <= 0L) {
                FormBody.Builder formBuilder = new FormBody.Builder();
                formBuilder.add("basenet___", "basenet___");
                requestBody = formBuilder.build();
            }
        } catch (IOException e) {
        }

        return requestBody;
    }

    /**
     * 获取wrapper BaseRequestBody
     *
     * @return
     */
    private RequestBody getWrapperRequestBody(RequestBody body) {
        RequestBody requestBody = body;
        if (null != mCallBack) {
            final ProgressRequestBody progressRequestBody = new ProgressRequestBody(body, new ProgressCallback() {
                @Override
                public void update(final long contentLength, final long bytesRead, final boolean done) {
                    if (mIsSync) {
                        mCallBack.onProgressUpdate(contentLength, bytesRead, done);
                    } else {
                        deliverCallBack(new Runnable() {
                            @Override
                            public void run() {
                                mCallBack.onProgressUpdate(contentLength, bytesRead, done);
                            }
                        });
                    }
                }
            });
            requestBody = progressRequestBody;
        }
        return requestBody;
    }

    /**
     * 封装响应 header
     *
     * @param response
     * @return
     */
    private HashMap getResponseHeaders(Response response) {
        HashMap headerMap = null;
        if (null != response.headers() && response.headers().size() > 0) {
            headerMap = new HashMap<>();
            Headers responseHeaders = response.headers();
            for (int i = 0; i < responseHeaders.size(); i++) {
                headerMap.put(responseHeaders.name(i), responseHeaders.value(i));
            }
        }

        return headerMap;
    }

    public static class Builder extends AbsRequest.Builder {

        @Override
        public OkHttpRequest build() {
            return new OkHttpRequest(this);
        }
    }
}


