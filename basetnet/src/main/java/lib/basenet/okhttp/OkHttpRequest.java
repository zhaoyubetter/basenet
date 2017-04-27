package lib.basenet.okhttp;


import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import lib.basenet.config.NetConfig;
import lib.basenet.okhttp.cache.NetCacheInterceptor;
import lib.basenet.okhttp.cache.PostCacheInterceptor;
import lib.basenet.okhttp.log.LoggerInterceptor;
import lib.basenet.request.AbsRequest;
import lib.basenet.utils.FileUtils;
import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * okhttp 类
 * Created by zhaoyu1 on 2017/3/7.
 */
public class OkHttpRequest extends AbsRequest {

	private static final OkHttpClient sOkHttpClient;
	private static final MediaType MEDIA_TYPE_MARKDOWN = MediaType.parse("media/type");

	/**
	 * 记录Call，方便取消
	 */
	private Call mCall;
	/**
	 * deliverHandler
	 */
	private Handler mDeliverHandler = new Handler(Looper.getMainLooper());

	static {
		final OkHttpClient.Builder builder = new OkHttpClient.Builder();
		builder.connectTimeout(NetConfig.getInstance().getTimeOut(), TimeUnit.MILLISECONDS);
		builder.readTimeout(NetConfig.getInstance().getTimeOut(), TimeUnit.MILLISECONDS);
		builder.writeTimeout(NetConfig.getInstance().getTimeOut(), TimeUnit.MILLISECONDS);

		/* ==设置拦截器== */
		// 设置缓存
		File cacheDir = new File(NetConfig.getInstance().getCacheDir());
		// GET 形式缓存设置
		Cache cache = new Cache(cacheDir, NetConfig.getInstance().getCacheSize());
		builder.cache(cache).addNetworkInterceptor(new NetCacheInterceptor());        // 设置缓存拦截器
		// 日志拦截
		if (NetConfig.getInstance().isDebug()) {
			builder.addInterceptor(new LoggerInterceptor());
		}
		// 是否允许POST 形式缓存设置
		if (NetConfig.getInstance().isEnablePostCache()) {
			builder.addInterceptor(new PostCacheInterceptor());
		}

		sOkHttpClient = builder.build();
	}

	protected OkHttpRequest(Builder builder) {
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
	 */
	private void downFile() {
		// 是否是下载
		final OkHttpClient tClient = sOkHttpClient.newBuilder().addInterceptor(new Interceptor() {
			@Override
			public Response intercept(Chain chain) throws IOException {
				Response originalResponse = chain.proceed(chain.request());
				return originalResponse.newBuilder()
						.body(new ProgressResponseBody(originalResponse.body(), new ProgressCallback() {
							@Override
							public void update(final long contentLength, final long bytesRead, final boolean done) {
								if (null != mCallBack) {
									deliverCallBack(new Runnable() {
										@Override
										public void run() {
											mCallBack.onProgressUpdate(contentLength, bytesRead, done);
										}
									});
								}
							}
						})).build();
			}
		}).build();

		// 执行下载逻辑
		tClient.newCall(new Request.Builder().url(mUrl).build()).enqueue(new Callback() {
			Map<String, String> headerMap = null;            // 响应头

			@Override
			public void onFailure(final Call call, final IOException e) {
				if (null != mCallBack) {
					deliverCallBack(new Runnable() {
						@Override
						public void run() {
							mCallBack.onFailure(e);
						}
					});
				}
			}

			@Override
			public void onResponse(final Call call, final Response response) {
				if (null != mCallBack) {
					headerMap = getResponseHeaders(response);
					if (response.isSuccessful()) {
						InputStream inputStream = response.body().byteStream();
						try {
							FileUtils.saveFile(inputStream, mDownFile);
						} catch (final IOException e) {
							deliverCallBack(new Runnable() {
								@Override
								public void run() {
									mCallBack.onFailure(e);
								}
							});
						}
						final lib.basenet.response.Response myResponse = new lib.basenet.response.Response(OkHttpRequest.this, headerMap, mDownFile);
						myResponse.statusCode = response.code();
						deliverCallBack(new Runnable() {
							@Override
							public void run() {
								mCallBack.onSuccess(myResponse);
							}
						});
					} else {
						deliverCallBack(new Runnable() {
							@Override
							public void run() {
								mCallBack.onFailure(new Exception(response.code() + " " + response.message()));
							}
						});
					}
				}
			}
		});
	}

	private void realRequest(Request.Builder tBuilder) {
		if (mDownFile != null) {
			downFile();
			return;
		}

		// 判断此次请求，超时时间是否不同，如果不同，copy Client
		OkHttpClient tClient = sOkHttpClient;
		if (mTimeOut >= 10 && mTimeOut != NetConfig.getInstance().getTimeOut()) {
			final OkHttpClient.Builder builder = sOkHttpClient.newBuilder().connectTimeout(mTimeOut, TimeUnit.MILLISECONDS).readTimeout(mTimeOut, TimeUnit.MILLISECONDS)
					.writeTimeout(mTimeOut, TimeUnit.MILLISECONDS);
			tClient = builder.build();
		} else {
			tClient = sOkHttpClient;
		}

		// 设置Header
		if (mHeader != null && mHeader.size() > 0) {
			for (Map.Entry<String, String> entry : mHeader.entrySet()) {
				tBuilder.header(entry.getKey(), entry.getValue());
			}
		}

		// request 缓存设置
		setCache(tBuilder);

		final Request request = tBuilder.build();       // 创建request

		// 走异步
		mCall = tClient.newCall(request);
		mCall.enqueue(new Callback() {
			boolean isSuccess = false;                       // 是否成功
			Map<String, String> headerMap = null;            // 响应头
			String returnBody = null;                        // 响应体

			@Override
			public void onFailure(Call call, final IOException e) {
				if (null != mCallBack) {
					deliverCallBack(new Runnable() {
						@Override
						public void run() {
							mCallBack.onFailure(e);
						}
					});
				}
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				if (null != mCallBack) {
					headerMap = getResponseHeaders(response);
					if (response.isSuccessful()) {
						isSuccess = true;
						returnBody = response.body().string();    // 字符串响应体
						final lib.basenet.response.Response myResponse = new lib.basenet.response.Response(OkHttpRequest.this, headerMap, returnBody);
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
					} else {
						isSuccess = false;
						returnBody = response.code() + " " + response.message();
						deliverCallBack(new Runnable() {
							@Override
							public void run() {
								mCallBack.onFailure(new Exception(returnBody));
							}
						});
					}
				}
			}
		});
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
		if (mUploadFiles != null) {
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
	 * post 请求体, 必须有一个请求体，否则报异常
	 *
	 * @return
	 */
	private RequestBody getRequestBody() {
		RequestBody requestBody = null;

		MultipartBody.Builder builder = new MultipartBody.Builder();
		builder.setType(MultipartBody.FORM);

		if (null != mParams && mParams.size() > 0) {
			for (Map.Entry<String, String> entry : mParams.entrySet()) {
				builder.addFormDataPart(entry.getKey(), entry.getValue());
			}
		}

		// 上传文件部分
		if (null != mUploadFiles && mUploadFiles.size() > 0) {
			for (Map.Entry<String, File> entry : mUploadFiles.entrySet()) {
				builder.addFormDataPart(entry.getKey(), entry.getValue().getName(), RequestBody.create(MEDIA_TYPE_MARKDOWN, entry.getValue()));
			}

			requestBody = builder.build();
			if (null != mCallBack) {
				final ProgressRequestBody progressRequestBody = new ProgressRequestBody(requestBody, new ProgressCallback() {
					@Override
					public void update(final long contentLength, final long bytesRead, final boolean done) {

						deliverCallBack(new Runnable() {
							@Override
							public void run() {
								mCallBack.onProgressUpdate(contentLength, bytesRead, done);
							}
						});
					}
				});
				requestBody = progressRequestBody;
			}
		}

		if (requestBody == null) {
			requestBody = builder.build();
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
		public AbsRequest build() {
			return new OkHttpRequest(this);
		}
	}
}


