package lib.basenet.okhttp.cache;

import java.io.File;
import java.io.IOException;

import lib.basenet.NetUtils;
import lib.basenet.utils.NetworkUtils;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.cache.CacheRequest;
import okhttp3.internal.cache.CacheStrategy;
import okhttp3.internal.http.HttpCodec;
import okhttp3.internal.http.HttpHeaders;
import okhttp3.internal.http.RealResponseBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import okio.Sink;
import okio.Source;
import okio.Timeout;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static okhttp3.internal.Util.closeQuietly;
import static okhttp3.internal.Util.discard;


/**
 * 缓存拦截
 * Created by zhaoyu on 2017/4/18.
 */
@Deprecated
public class NetCacheInterceptor_bak implements Interceptor {

	File cacheDir = new File(NetUtils.getInstance().getCacheDir() + "/post");
	NetPostCache cache = new NetPostCache(cacheDir, NetUtils.getInstance().getCacheSize());

	@Override
	public Response intercept(Chain chain) throws IOException {
		Request request = chain.request();

		// 能存储缓存
		if (null != request.cacheControl() && !request.cacheControl().noStore()) {
			boolean hasNet = NetworkUtils.hasNet(NetUtils.getInstance().getApplication());

			final Response originResponse;        // 原始响应

			if (!hasNet) {
				// 无网络，强制从缓存中获取
				request = request.newBuilder().cacheControl(CacheControl.FORCE_CACHE).build();
				originResponse = chain.proceed(request);
				return originResponse;
			} else {
				// 如果是post请求，则自己缓存
				if ("POST".equalsIgnoreCase(request.method())) {
					Response cacheCandidate = cache != null ? cache.get(request) : null;
					long now = System.currentTimeMillis();
					CacheStrategy strategy = new CacheStrategy.Factory(now, chain.request(), cacheCandidate).get();
					Request networkRequest = strategy.networkRequest;
					Response cacheResponse = strategy.cacheResponse;

					if (cache != null) {
						cache.trackResponse(strategy);
					}

					if (cacheCandidate != null && cacheResponse == null) {
						closeQuietly(cacheCandidate.body()); // The mPostCache candidate wasn't applicable. Close it.
					}

					// 有则从缓存中返回
					// If we don't need the network, we're done.
					if (networkRequest == null) {
						return cacheResponse.newBuilder()
								.cacheResponse(stripBody(cacheResponse))
								.build();
					}

					Response networkResponse = null;
					try {
						// 执行网络请求，并包装一下
						networkResponse = chain.proceed(networkRequest);
						networkResponse = getWrapperResponse(request, networkResponse);
					} finally {
						// If we're crashing on I/O or otherwise, don't leak the mPostCache body.
						if (networkResponse == null && cacheCandidate != null) {
							closeQuietly(cacheCandidate.body());
						}
					}

					Response response = networkResponse.newBuilder()
							.cacheResponse(stripBody(cacheResponse))
							.networkResponse(stripBody(networkResponse))
							.build();

					if (HttpHeaders.hasBody(response)) {
						CacheRequest cacheRequest = cache.put(response);
						response = cacheWritingResponse(cacheRequest, response);
					}

					// 替换response，当 onSuccess调用时，写入缓存
					return networkResponse.newBuilder().body(response.body()).build();
				}


				// 有网络，设置缓存时间 （通过下面一行，可拿到网络上的返回数据）
				originResponse = chain.proceed(request);
				// 再包装一下originResponse，方便后面的 CacheInterceptor 来做缓存处理
				return getWrapperResponse(request, originResponse);
			}
		}   // end 能存储缓存

		return chain.proceed(request);
	}

	/**
	 * 包装一下originResponse，方便后面的 CacheInterceptor 来做缓存处理
	 *
	 * @param request
	 * @param originResponse
	 * @return
	 */
	private Response getWrapperResponse(Request request, Response originResponse) {
		int maxAge = request.cacheControl().maxAgeSeconds();
		return originResponse.newBuilder()
				.removeHeader("Pragma")
				.removeHeader("Cache-control")
				.header("Cache-control", "public, max-age=" + maxAge)
				.build();
	}


	// 以下摘自 okhttp中的CacheInterceptor
	private static Response stripBody(Response response) {
		return response != null && response.body() != null
				? response.newBuilder().body(null).build()
				: response;
	}

	/**
	 * Returns a new source that writes bytes to {@code cacheRequest} as they are read by the source
	 * consumer. This is careful to discard bytes left over when the stream is closed; otherwise we
	 * may never exhaust the source stream and therefore not complete the cached response.
	 */
	private Response cacheWritingResponse(final CacheRequest cacheRequest, Response response)
			throws IOException {
		// Some apps return a null body; for compatibility we treat that like a null mPostCache request.
		if (cacheRequest == null) return response;
		Sink cacheBodyUnbuffered = cacheRequest.body();
		if (cacheBodyUnbuffered == null) return response;

		final BufferedSource source = response.body().source();
		final BufferedSink cacheBody = Okio.buffer(cacheBodyUnbuffered);

		Source cacheWritingSource = new Source() {
			boolean cacheRequestClosed;

			@Override
			public long read(Buffer sink, long byteCount) throws IOException {
				long bytesRead;
				try {
					bytesRead = source.read(sink, byteCount);
				} catch (IOException e) {
					if (!cacheRequestClosed) {
						cacheRequestClosed = true;
						cacheRequest.abort(); // Failed to write a complete mPostCache response.
					}
					throw e;
				}

				if (bytesRead == -1) {
					if (!cacheRequestClosed) {
						cacheRequestClosed = true;
						cacheBody.close(); // The mPostCache response is complete!
					}
					return -1;
				}

				sink.copyTo(cacheBody.buffer(), sink.size() - bytesRead, bytesRead);
				cacheBody.emitCompleteSegments();
				return bytesRead;
			}

			@Override
			public Timeout timeout() {
				return source.timeout();
			}

			@Override
			public void close() throws IOException {
				if (!cacheRequestClosed
						&& !discard(this, HttpCodec.DISCARD_STREAM_TIMEOUT_MILLIS, MILLISECONDS)) {
					cacheRequestClosed = true;
					cacheRequest.abort();
				}
				source.close();
			}
		};

		return response.newBuilder()
				.body(new RealResponseBody(response.headers(), Okio.buffer(cacheWritingSource)))
				.build();
	}

	/*
	if ("POST".equalsIgnoreCase(request.method())) {
					// 缓存post请求
					File cacheDir = new File(NetUtils.getCacheDir() + "/post");
					NetCache mPostCache = new NetCache(cacheDir, NetUtils.getCacheSize());

					Response cacheCandidate = mPostCache != null
							? mPostCache.get(request) : null;
					long now = System.currentTimeMillis();

					CacheStrategy strategy = new CacheStrategy.Factory(now, chain.request(), cacheCandidate).get();
					Request networkRequest = strategy.networkRequest;
					Response cacheResponse = strategy.cacheResponse;
					if (mPostCache != null) {
						mPostCache.trackResponse(strategy);
					}
					if (cacheCandidate != null && cacheResponse == null) {
						closeQuietly(cacheCandidate.body()); // The mPostCache candidate wasn't applicable. Close it.
					}

					int maxAge = request.cacheControl().maxAgeSeconds();
					final Response wrapperResponse = originResponse.newBuilder()
							.removeHeader("Pragma")
							.removeHeader("Cache-control")
							.header("Cache-control", "public, max-age=" + maxAge)
							.build();

					mPostCache.put(originResponse);

					return wrapperResponse;
				} else {
	 */
}
