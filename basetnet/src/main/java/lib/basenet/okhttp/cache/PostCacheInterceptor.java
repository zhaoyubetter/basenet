package lib.basenet.okhttp.cache;

import java.io.File;
import java.io.IOException;

import lib.basenet.config.NetConfig;
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
 * post缓存，从应用拦截器层，拦截
 * Created by zhaoyu on 2017/4/26.
 */
public class PostCacheInterceptor implements Interceptor {

	/**
	 * 缓存Post请求
	 */
	NetPostCache mPostCache;

	public PostCacheInterceptor() {
		File cacheDir = new File(NetConfig.getCacheDir() + "/posts");
		mPostCache = new NetPostCache(cacheDir, NetConfig.getCacheSize());
	}

	@Override
	public Response intercept(Chain chain) throws IOException {
		final Request request = chain.request();

		// 如果是post请求，并且设置了缓存，则在这里进行拦截，如果缓存有效，后续的拦截器将不执行
		if ("POST".equalsIgnoreCase(request.method()) && (null != request.cacheControl() && !request.cacheControl().noStore())) {
			// 强制刷新，删除旧有post缓存
			checkForceRefresh(request);

			// 以下代码逻辑来家：okhttp3 源码中的 CacheInterceptor.java，模拟其运行
			Response cacheCandidate = mPostCache != null ? mPostCache.get(request) : null;
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

			// 有则从缓存中返回，直接跳过后面的拦截器，不访问网络了
			// If we don't need the network, we're done.
			if (networkRequest == null) {
				return cacheResponse.newBuilder()
						.cacheResponse(stripBody(cacheResponse))
						.build();
			}

			Response networkResponse = null;
			try {
				// 执行网络请求，并包装一下
				networkResponse = chain.proceed(request);
			} finally {
				// If we're crashing on I/O or otherwise, don't leak the mPostCache body.
				if (networkResponse == null && cacheCandidate != null) {
					closeQuietly(cacheCandidate.body());
				}
			}

			if (HttpHeaders.hasBody(networkResponse)) {
				CacheRequest cacheRequest = mPostCache.put(networkResponse);
				networkResponse = cacheWritingResponse(cacheRequest, networkResponse);
			}

			// 当 onSuccess调用时，会写入缓存
			return networkResponse;
		}

		return chain.proceed(request);
	}

	/**
	 * 如果当前 post 是强制刷新 即：noCache @see，即：删除旧有缓存
	 *
	 * @see lib.basenet.okhttp.OkHttpRequest#setCache  mIsForceRefresh
	 */
	private void checkForceRefresh(Request request) {
		try {
			if (request.cacheControl().noCache()) {
				mPostCache.remove(request);
			}
		} catch (Exception e) {
		}
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
}
