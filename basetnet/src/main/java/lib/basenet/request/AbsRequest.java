package lib.basenet.request;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * 网络请求封装接口,抽象类
 *
 * @author zhaoyu
 * @author hufeng
 * @version 1.0
 * @since 2017/3/6.
 * =========================== UPDATE LOG: =================================
 * Date: 2017/3/9 add upload file and down file support
 * Date: 2017/4/17 add get request params, such as getUrl
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
	protected Map<String, String> mParams;

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
	 * files need to upload
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

	public String getUrl() {
		return mUrl;
	}

	public Map<String, String> getParams() {
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
	protected String generateUrl(String url, Map<String, String> params) {
		StringBuilder sb = new StringBuilder(url);
		if (params != null && params.size() > 0) {      // GET 请求，拼接url
			if (sb.charAt(sb.length() - 1) != '?') {            // get 请求 有 ?
				sb.append("?");
			}
			for (Map.Entry<String, String> entry : params.entrySet()) {
				try {
					sb.append(URLEncoder.encode(entry.getKey(), CHAR_SET)).append("=").append(URLEncoder.encode(entry.getValue(), CHAR_SET)).append("&");
				} catch (UnsupportedEncodingException e) {
					// NOT_HAPPEND
				}
			}
			sb = sb.deleteCharAt(sb.length() - 1);
		}
		return sb.toString();
	}


	public static abstract class Builder {

		/**
		 * url 地址
		 */
		private String mUrl;

		/**
		 * 参数
		 */
		private Map<String, String> mParams;

		/**
		 * 请求头信息
		 */
		private Map<String, String> mHeader;

		/**
		 * 本地请求超时时间
		 */
		private long mTimeOut;

		/**
		 * 请求标记
		 */
		private Object mTag;

		/**
		 * 回调
		 */
		private AbsRequestCallBack mCallBack;

		/**
		 * 请求方式
		 */
		private int mReqType;

		/**
		 * 上传的文件
		 */
		private Map<String, File> mUploadFiles;

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

		public Builder() {

		}

		public Builder url(String url) {
			this.mUrl = url;
			return this;
		}

		public Builder body(Map<String, String> params) {
			this.mParams = params;
			return this;
		}

		public Builder headers(Map<String, String> headers) {
			this.mHeader = headers;
			return this;
		}

		public Builder timeout(long time) {
			this.mTimeOut = time;
			return this;
		}

		public Builder tag(Object tag) {
			this.mTag = tag;
			return this;
		}

		public Builder callback(AbsRequestCallBack callBack) {
			this.mCallBack = callBack;
			return this;
		}

		/**
		 * @param reqType {@link IRequest.RequestType}中常量
		 * @return
		 */
		public Builder type(int reqType) {
			this.mReqType = reqType;
			return this;
		}

		public Builder uploadFiles(Map<String, File> fileMaps) {
			this.mUploadFiles = fileMaps;
			return this;
		}

		public Builder downFile(File downFile) {
			this.mDownFile = downFile;
			return this;
		}

		/**
		 * 设置缓存时间，秒为单位
		 *
		 * @return
		 */
		public Builder cacheTime(int cacheTime) {
			this.mCacheTime = cacheTime;
			return this;
		}

		public Builder forceRefresh(boolean forceRefresh) {
			this.mIsForceRefresh = forceRefresh;
			return this;
		}

		public abstract AbsRequest build();
	}
}
