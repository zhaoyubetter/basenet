package lib.basenet.request;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * 网络请求封装接口,抽象类
 *
 * @author zhaoyu
 * @author hufeng
 * @version 1.0
 * @since 2017/3/6.
 * =========================== UPDATE LOG: =================================
 * Date: 2017/3/9 add start file and down file support
 * Date: 2017/4/17 add get request params, such as getUrl
 * Date: 2018/06/03 add level and update param Map<String,String> to Map<String, Object></>
 * =========================== UPDATE LOG: =================================
 */
public abstract class AbsRequest implements IRequest {

    public static final String CHAR_SET = "UTF-8";

    /**
     * url 地址
     */
    protected String mUrl;

    /**
     * 参数
     */
    protected Map<String, Object> mParams;

    /**
     * 请求头信息
     */
    protected Map<String, String> mHeader;

    /**
     * 本地请求超时时间
     */
    protected long mTimeOut;

    /**
     * 请求标记
     */
    protected Object mTag;

    /**
     * 回调
     */
    protected AbsRequestCallBack mCallBack;

    /**
     * 请求方式
     */
    protected int mReqType;

    /**
     * files need to start
     */
    protected Map<String, File> mUploadFiles;

    /**
     * down file
     */
    protected File mDownFile;

    /**
     * 缓存时间,秒为单位
     */
    protected int mCacheTime;
    /**
     * 是否强制刷新，不管有没有缓存，都从网络获取
     */
    protected boolean mIsForceRefresh = false;

    /**
     * 请求体
     */
    protected BaseRequestBody mRequestBody;

    public String getUrl() {
        return mUrl;
    }

    public Map<String, Object> getParams() {
        return mParams;
    }

    public Map<String, String> getHeader() {
        return mHeader;
    }

    public long getTimeOut() {
        return mTimeOut;
    }

    public Object getTag() {
        return mTag;
    }

    public AbsRequestCallBack getCallBack() {
        return mCallBack;
    }

    public int getReqType() {
        return mReqType;
    }

    public Map<String, File> getUploadFiles() {
        return mUploadFiles;
    }

    public File getDownFile() {
        return mDownFile;
    }

    public int getCacheTime() {
        return mCacheTime;
    }

    public boolean ismIsForceRefresh() {
        return mIsForceRefresh;
    }

    protected AbsRequest(Builder builder) {
        this.mUrl = builder.mUrl;
        this.mCallBack = builder.mCallBack;
        this.mTag = builder.mTag;
        this.mTimeOut = builder.mTimeOut;
        this.mReqType = builder.mReqType;
        this.mParams = builder.mParams;
        this.mHeader = builder.mHeader;
        this.mUploadFiles = builder.mUploadFiles;
        this.mDownFile = builder.mDownFile;
        this.mCacheTime = builder.mCacheTime;
        this.mIsForceRefresh = builder.mIsForceRefresh;
        this.mRequestBody = builder.mBody;
    }

    @Override
    public final void request() {
        switch (mReqType) {
            case RequestType.GET:
                get();
                break;
            case RequestType.POST:
                post();
                break;
        }
    }

    /**
     * 执行get方式
     */
    protected abstract void get();

    /**
     * 执行post方式
     */
    protected abstract void post();


    /**
     * 生成请求的url地址
     *
     * @param url
     * @param params
     * @return
     */
    protected String generateUrl(String url, Map<String, Object> params) {
        StringBuilder sb = new StringBuilder(url);
        if (params != null && params.size() > 0) {      // GET 请求，拼接url
            if (sb.charAt(sb.length() - 1) != '?') {            // get 请求 有 ?
                sb.append("?");
            }
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                try {
                    Object value = entry.getValue();
                    if (value == null) {
                        continue;
                    }
                    sb.append(URLEncoder.encode(entry.getKey(), CHAR_SET)).append("=").append(URLEncoder.encode(value.toString(), CHAR_SET)).append("&");
                } catch (UnsupportedEncodingException e) {
                    // NOT_HAPPEND
                }
            }
            sb = sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }


    /**
     * Let builder mode support inheritance
     *
     * @param <T>
     */
    public static abstract class Builder<T extends Builder> {

        /**
         * url 地址
         */
        protected String mUrl;

        /**
         * 参数
         */
        protected Map<String, Object> mParams;

        /**
         * 请求头信息
         */
        protected Map<String, String> mHeader;

        /**
         * 本地请求超时时间
         */
        protected long mTimeOut;

        /**
         * 请求标记
         */
        protected Object mTag;

        /**
         * 回调
         */
        protected AbsRequestCallBack mCallBack;

        /**
         * 请求方式
         */
        protected int mReqType;

        /**
         * 上传的文件
         */
        protected Map<String, File> mUploadFiles;

        /**
         * 下载的文件名
         */
        private File mDownFile;

        /**
         * 缓存时间,秒为单位
         */
        private int mCacheTime;
        /**
         * 是否强制刷新，不管有没有缓存，都从网络获取
         */
        private boolean mIsForceRefresh = false;

        /**
         * 请求的body
         */
        private BaseRequestBody mBody = null;


        public T url(String url) {
            this.mUrl = url;
            return (T) this;
        }

        public T body(Map<String, Object> params) {
            this.mParams = params;
            return (T) this;
        }

        public T addBody(String key, String value) {
            if (this.mParams == null) {
                this.mParams = new HashMap<>();
            }
            mParams.put(key, value);
            return (T) this;
        }

        public T headers(Map<String, String> headers) {
            this.mHeader = headers;
            return (T) this;
        }

        public T addHeader(String key, String value) {
            if (this.mHeader == null) {
                mHeader = new HashMap<>();
            }
            mHeader.put(key, value);
            return (T) this;
        }

        public T timeout(long time) {
            this.mTimeOut = time;
            return (T) this;
        }

        public T tag(Object tag) {
            this.mTag = tag;
            return (T) this;
        }

        public T callback(AbsRequestCallBack callBack) {
            this.mCallBack = callBack;
            return (T) this;
        }

        /**
         * @param reqType {@link IRequest.RequestType}中常量
         * @return
         */
        public T type(int reqType) {
            this.mReqType = reqType;
            return (T) this;
        }

        public T uploadFiles(Map<String, File> fileMaps) {
            this.mUploadFiles = fileMaps;
            return (T) this;
        }

        public T addUploadFile(String key, File file) {
            if (this.mUploadFiles == null) {
                mUploadFiles = new HashMap<>();
            }
            mUploadFiles.put(key, file);
            return (T) this;
        }


        public T downFile(File downFile) {
            this.mDownFile = downFile;
            return (T) this;
        }

        /**
         * 设置缓存时间，秒为单位
         *
         * @return
         */
        public T cacheTime(int cacheTime) {
            this.mCacheTime = cacheTime;
            return (T) this;
        }

        public T forceRefresh(boolean forceRefresh) {
            this.mIsForceRefresh = forceRefresh;
            return (T) this;
        }

        /**
         * 设置缓存时间，秒为单位
         *
         * @return
         */
        public T requestBody(BaseRequestBody body) {
            this.mBody = body;
            return (T) this;
        }

        public abstract <D extends AbsRequest> D build();
    }
}
