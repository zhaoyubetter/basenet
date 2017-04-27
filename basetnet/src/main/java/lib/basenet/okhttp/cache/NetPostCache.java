/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package lib.basenet.okhttp.cache;

import java.io.Closeable;
import java.io.File;
import java.io.Flushable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.CipherSuite;
import okhttp3.FormBody;
import okhttp3.Handshake;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.TlsVersion;
import okhttp3.internal.Util;
import okhttp3.internal.cache.CacheRequest;
import okhttp3.internal.cache.CacheStrategy;
import okhttp3.internal.cache.DiskLruCache;
import okhttp3.internal.cache.InternalCache;
import okhttp3.internal.http.HttpHeaders;
import okhttp3.internal.http.StatusLine;
import okhttp3.internal.io.FileSystem;
import okhttp3.internal.platform.Platform;
import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.ByteString;
import okio.ForwardingSink;
import okio.ForwardingSource;
import okio.Okio;
import okio.Sink;
import okio.Source;

/**
 * Caches HTTP and HTTPS responses to the filesystem so they may be reused, saving time and
 * bandwidth.
 * <p>
 * <h3>Cache Optimization</h3>
 * <p>
 * <p>To measure mPostCache effectiveness, this class tracks three statistics:
 * <ul>
 * <li><strong>{@linkplain #requestCount() Request Count:}</strong> the number of HTTP
 * requests issued since this mPostCache was created.
 * <li><strong>{@linkplain #networkCount() Network Count:}</strong> the number of those
 * requests that required network use.
 * <li><strong>{@linkplain #hitCount() Hit Count:}</strong> the number of those requests
 * whose responses were served by the mPostCache.
 * </ul>
 * <p>
 * Sometimes a request will result in a conditional mPostCache hit. If the mPostCache contains a stale copy of
 * the response, the client will issue a conditional {@code GET}. The server will then send either
 * the updated response if it has changed, or a short 'not modified' response if the client's copy
 * is still valid. Such responses increment both the network count and hit count.
 * <p>
 * <p>The best way to improve the mPostCache hit rate is by configuring the web server to return
 * cacheable responses. Although this client honors all <a
 * href="http://tools.ietf.org/html/rfc7234">HTTP/1.1 (RFC 7234)</a> mPostCache headers, it doesn't mPostCache
 * partial responses.
 * <p>
 * <h3>Force a Network Response</h3>
 * <p>
 * <p>In some situations, such as after a user clicks a 'refresh' button, it may be necessary to
 * skip the mPostCache, and fetch data directly from the server. To force a full refresh, add the {@code
 * no-mPostCache} directive: <pre>   {@code
 * <p>
 *   Request request = new Request.Builder()
 *       .cacheControl(new CacheControl.Builder().noCache().build())
 *       .url("http://publicobject.com/helloworld.txt")
 *       .build();
 * }</pre>
 * <p>
 * If it is only necessary to force a cached response to be validated by the server, use the more
 * efficient {@code max-age=0} directive instead: <pre>   {@code
 * <p>
 *   Request request = new Request.Builder()
 *       .cacheControl(new CacheControl.Builder()
 *           .maxAge(0, TimeUnit.SECONDS)
 *           .build())
 *       .url("http://publicobject.com/helloworld.txt")
 *       .build();
 * }</pre>
 * <p>
 * <h3>Force a Cache Response</h3>
 * <p>
 * <p>Sometimes you'll want to show resources if they are available immediately, but not otherwise.
 * This can be used so your application can show <i>something</i> while waiting for the latest data
 * to be downloaded. To restrict a request to locally-cached resources, add the {@code
 * only-if-cached} directive: <pre>   {@code
 * <p>
 *     Request request = new Request.Builder()
 *         .cacheControl(new CacheControl.Builder()
 *             .onlyIfCached()
 *             .build())
 *         .url("http://publicobject.com/helloworld.txt")
 *         .build();
 *     Response forceCacheResponse = client.newCall(request).execute();
 *     if (forceCacheResponse.code() != 504) {
 *       // The resource was cached! Show it.
 *     } else {
 *       // The resource was not cached.
 *     }
 * }</pre>
 * This technique works even better in situations where a stale response is better than no response.
 * To permit stale cached responses, use the {@code max-stale} directive with the maximum staleness
 * in seconds: <pre>   {@code
 * <p>
 *   Request request = new Request.Builder()
 *       .cacheControl(new CacheControl.Builder()
 *           .maxStale(365, TimeUnit.DAYS)
 *           .build())
 *       .url("http://publicobject.com/helloworld.txt")
 *       .build();
 * }</pre>
 * <p>
 * <p>The {@link CacheControl} class can configure request caching directives and parse response
 * caching directives. It even offers convenient constants {@link CacheControl#FORCE_NETWORK} and
 * {@link CacheControl#FORCE_CACHE} that address the use cases above.
 * 为 POST 缓存提供服务
 * 1.修改了获取key的方法，去掉了 put 时，限制 GET 的约束
 * 2.其他保持不变
 */
public final class NetPostCache implements Closeable, Flushable {
	private static final int VERSION = 201105;
	private static final int ENTRY_METADATA = 0;
	private static final int ENTRY_BODY = 1;
	private static final int ENTRY_COUNT = 2;

	final InternalCache internalCache = new InternalCache() {
		@Override
		public Response get(Request request) throws IOException {
			return NetPostCache.this.get(request);
		}

		@Override
		public CacheRequest put(Response response) throws IOException {
			return NetPostCache.this.put(response);
		}

		@Override
		public void remove(Request request) throws IOException {
			NetPostCache.this.remove(request);
		}

		@Override
		public void update(Response cached, Response network) {
			NetPostCache.this.update(cached, network);
		}

		@Override
		public void trackConditionalCacheHit() {
			NetPostCache.this.trackConditionalCacheHit();
		}

		@Override
		public void trackResponse(CacheStrategy cacheStrategy) {
			NetPostCache.this.trackResponse(cacheStrategy);
		}
	};

	final DiskLruCache cache;

	/* read and write statistics, all guarded by 'this' */
	int writeSuccessCount;
	int writeAbortCount;
	private int networkCount;
	private int hitCount;
	private int requestCount;

	public NetPostCache(File directory, long maxSize) {
		this(directory, maxSize, FileSystem.SYSTEM);
	}

	NetPostCache(File directory, long maxSize, FileSystem fileSystem) {
		this.cache = DiskLruCache.create(fileSystem, directory, VERSION, ENTRY_COUNT, maxSize);
	}

	/*
	public static String key(HttpUrl url) {
		return ByteString.encodeUtf8(url.toString()).md5().hex();
	}*/

	public static String key(Request request) {
		String cUrl = request.url().toString();
		if (request.body() != null) {
			Buffer buffer = new Buffer();
			try {
				// 避免post重复，这里采用value来凭借，因key不好获取
				// 如果有上传下载文件，此处为 ProgressRequestBody
				if (request.body() instanceof MultipartBody) {
					final List<MultipartBody.Part> parts = ((MultipartBody) request.body()).parts();
					/**
					 * 接受字符串格式的参数，其他忽略
					 * @see lib.basenet.okhttp.OkHttpRequest#getRequestBody mParams
					 */
					for (MultipartBody.Part p : parts) {
						if (null == p.body().contentType()) {
							p.body().writeTo(buffer);
						}
					}
				}
				String params = buffer.readString(Charset.forName("UTF-8")); //获取请求参数
				cUrl += params;
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				Util.closeQuietly(buffer);
			}

		}
		return ByteString.encodeUtf8(cUrl).md5().hex();
	}

	Response get(Request request) {
		String key = key(request);
		DiskLruCache.Snapshot snapshot;
		Entry entry;
		try {
			snapshot = cache.get(key);
			if (snapshot == null) {
				return null;
			}
		} catch (IOException e) {
			// Give up because the mPostCache cannot be read.
			return null;
		}

		try {
			entry = new Entry(snapshot.getSource(ENTRY_METADATA));
		} catch (IOException e) {
			Util.closeQuietly(snapshot);
			return null;
		}

		Response response = entry.response(snapshot);

		if (!entry.matches(request, response)) {
			Util.closeQuietly(response.body());
			return null;
		}

		return response;
	}

	CacheRequest put(Response response) {
		String requestMethod = response.request().method();

		/*
		去掉只缓存 get
		if (HttpMethod.invalidatesCache(response.request().method())) {
			try {
				remove(response.request());
			} catch (IOException ignored) {
				// The mPostCache cannot be written.
			}
			return null;
		}


    if (!requestMethod.equals("GET")) {
      // Don't mPostCache non-GET responses. We're technically allowed to mPostCache
      // HEAD requests and some POST requests, but the complexity of doing
      // so is high and the benefit is low.
      return null;
    }*/

		if (HttpHeaders.hasVaryAll(response)) {
			return null;
		}

		Entry entry = new Entry(response);
		DiskLruCache.Editor editor = null;
		try {
			editor = cache.edit(key(response.request()));
			if (editor == null) {
				return null;
			}
			entry.writeTo(editor);
			return new CacheRequestImpl(editor);
		} catch (IOException e) {
			abortQuietly(editor);
			return null;
		}
	}

	void remove(Request request) throws IOException {
		cache.remove(key(request));
	}

	void update(Response cached, Response network) {
		Entry entry = new Entry(network);
		DiskLruCache.Snapshot snapshot = ((CacheResponseBody) cached.body()).snapshot;
		DiskLruCache.Editor editor = null;
		try {
			editor = snapshot.edit(); // Returns null if snapshot is not current.
			if (editor != null) {
				entry.writeTo(editor);
				editor.commit();
			}
		} catch (IOException e) {
			abortQuietly(editor);
		}
	}

	private void abortQuietly(DiskLruCache.Editor editor) {
		// Give up because the mPostCache cannot be written.
		try {
			if (editor != null) {
				editor.abort();
			}
		} catch (IOException ignored) {
		}
	}

	/**
	 * Initialize the mPostCache. This will include reading the journal files from the storage and building
	 * up the necessary in-memory mPostCache information.
	 * <p>
	 * <p>The initialization time may vary depending on the journal file size and the current actual
	 * mPostCache size. The application needs to be aware of calling this function during the
	 * initialization phase and preferably in a background worker thread.
	 * <p>
	 * <p>Note that if the application chooses to not call this method to initialize the mPostCache. By
	 * default, the okhttp will perform lazy initialization upon the first usage of the mPostCache.
	 */
	public void initialize() throws IOException {
		cache.initialize();
	}

	/**
	 * Closes the mPostCache and deletes all of its stored values. This will delete all files in the mPostCache
	 * directory including files that weren't created by the mPostCache.
	 */
	public void delete() throws IOException {
		cache.delete();
	}

	/**
	 * Deletes all values stored in the mPostCache. In-flight writes to the mPostCache will complete normally,
	 * but the corresponding responses will not be stored.
	 */
	public void evictAll() throws IOException {
		cache.evictAll();
	}


	public synchronized int writeAbortCount() {
		return writeAbortCount;
	}

	public synchronized int writeSuccessCount() {
		return writeSuccessCount;
	}

	public long size() throws IOException {
		return cache.size();
	}

	public long maxSize() {
		return cache.getMaxSize();
	}

	@Override
	public void flush() throws IOException {
		cache.flush();
	}

	@Override
	public void close() throws IOException {
		cache.close();
	}

	public File directory() {
		return cache.getDirectory();
	}

	public boolean isClosed() {
		return cache.isClosed();
	}

	synchronized void trackResponse(CacheStrategy cacheStrategy) {
		requestCount++;

		if (cacheStrategy.networkRequest != null) {
			// If this is a conditional request, we'll increment hitCount if/when it hits.
			networkCount++;
		} else if (cacheStrategy.cacheResponse != null) {
			// This response uses the mPostCache and not the network. That's a mPostCache hit.
			hitCount++;
		}
	}

	synchronized void trackConditionalCacheHit() {
		hitCount++;
	}

	public synchronized int networkCount() {
		return networkCount;
	}

	public synchronized int hitCount() {
		return hitCount;
	}

	public synchronized int requestCount() {
		return requestCount;
	}

	private final class CacheRequestImpl implements CacheRequest {
		private final DiskLruCache.Editor editor;
		private Sink cacheOut;
		private Sink body;
		boolean done;

		public CacheRequestImpl(final DiskLruCache.Editor editor) {
			this.editor = editor;
			this.cacheOut = editor.newSink(ENTRY_BODY);
			this.body = new ForwardingSink(cacheOut) {
				@Override
				public void close() throws IOException {
					synchronized (NetPostCache.this) {
						if (done) {
							return;
						}
						done = true;
						writeSuccessCount++;
					}
					super.close();
					editor.commit();
				}
			};
		}

		@Override
		public void abort() {
			synchronized (NetPostCache.this) {
				if (done) {
					return;
				}
				done = true;
				writeAbortCount++;
			}
			Util.closeQuietly(cacheOut);
			try {
				editor.abort();
			} catch (IOException ignored) {
			}
		}

		@Override
		public Sink body() {
			return body;
		}
	}

	private static final class Entry {
		/**
		 * Synthetic response header: the local time when the request was sent.
		 */
		private static final String SENT_MILLIS = Platform.get().getPrefix() + "-Sent-Millis";

		/**
		 * Synthetic response header: the local time when the response was received.
		 */
		private static final String RECEIVED_MILLIS = Platform.get().getPrefix() + "-Received-Millis";

		private final String url;
		private final Headers varyHeaders;
		private final String requestMethod;
		private final Protocol protocol;
		private final int code;
		private final String message;
		private final Headers responseHeaders;
		private final Handshake handshake;
		private final long sentRequestMillis;
		private final long receivedResponseMillis;

		/**
		 * Reads an entry from an input stream. A typical entry looks like this:
		 * <pre>{@code
		 *   http://google.com/foo
		 *   GET
		 *   2
		 *   Accept-Language: fr-CA
		 *   Accept-Charset: UTF-8
		 *   HTTP/1.1 200 OK
		 *   3
		 *   Content-Type: image/png
		 *   Content-Length: 100
		 *   Cache-Control: max-age=600
		 * }</pre>
		 * <p>
		 * <p>A typical HTTPS file looks like this:
		 * <pre>{@code
		 *   https://google.com/foo
		 *   GET
		 *   2
		 *   Accept-Language: fr-CA
		 *   Accept-Charset: UTF-8
		 *   HTTP/1.1 200 OK
		 *   3
		 *   Content-Type: image/png
		 *   Content-Length: 100
		 *   Cache-Control: max-age=600
		 *
		 *   AES_256_WITH_MD5
		 *   2
		 *   base64-encoded peerCertificate[0]
		 *   base64-encoded peerCertificate[1]
		 *   -1
		 *   TLSv1.2
		 * }</pre>
		 * The file is newline separated. The first two lines are the URL and the request method. Next
		 * is the number of HTTP Vary request header lines, followed by those lines.
		 * <p>
		 * <p>Next is the response status line, followed by the number of HTTP response header lines,
		 * followed by those lines.
		 * <p>
		 * <p>HTTPS responses also contain SSL session information. This begins with a blank line, and
		 * then a line containing the cipher suite. Next is the length of the peer certificate chain.
		 * These certificates are base64-encoded and appear each on their own line. The next line
		 * contains the length of the local certificate chain. These certificates are also
		 * base64-encoded and appear each on their own line. A length of -1 is used to encode a null
		 * array. The last line is optional. If present, it contains the TLS version.
		 */
		public Entry(Source in) throws IOException {
			try {
				BufferedSource source = Okio.buffer(in);
				url = source.readUtf8LineStrict();
				requestMethod = source.readUtf8LineStrict();
				Headers.Builder varyHeadersBuilder = new Headers.Builder();
				int varyRequestHeaderLineCount = readInt(source);
				for (int i = 0; i < varyRequestHeaderLineCount; i++) {
//					varyHeadersBuilder.addLenient(source.readUtf8LineStrict());
					addLenient(varyHeadersBuilder, source.readUtf8LineStrict());
				}
				varyHeaders = varyHeadersBuilder.build();

				StatusLine statusLine = StatusLine.parse(source.readUtf8LineStrict());
				protocol = statusLine.protocol;
				code = statusLine.code;
				message = statusLine.message;
				Headers.Builder responseHeadersBuilder = new Headers.Builder();
				int responseHeaderLineCount = readInt(source);
				for (int i = 0; i < responseHeaderLineCount; i++) {
					addLenient(responseHeadersBuilder, source.readUtf8LineStrict());
				}
				responseHeadersBuilder.build();
				String sendRequestMillisString = responseHeadersBuilder.get(SENT_MILLIS);
				String receivedResponseMillisString = responseHeadersBuilder.get(RECEIVED_MILLIS);
				responseHeadersBuilder.removeAll(SENT_MILLIS);
				responseHeadersBuilder.removeAll(RECEIVED_MILLIS);
				sentRequestMillis = sendRequestMillisString != null
						? Long.parseLong(sendRequestMillisString)
						: 0L;
				receivedResponseMillis = receivedResponseMillisString != null
						? Long.parseLong(receivedResponseMillisString)
						: 0L;
				responseHeaders = responseHeadersBuilder.build();

				if (isHttps()) {
					String blank = source.readUtf8LineStrict();
					if (blank.length() > 0) {
						throw new IOException("expected \"\" but was \"" + blank + "\"");
					}
					String cipherSuiteString = source.readUtf8LineStrict();
					CipherSuite cipherSuite = CipherSuite.forJavaName(cipherSuiteString);
					List<Certificate> peerCertificates = readCertificateList(source);
					List<Certificate> localCertificates = readCertificateList(source);
					TlsVersion tlsVersion = !source.exhausted()
							? TlsVersion.forJavaName(source.readUtf8LineStrict())
							: null;
					handshake = Handshake.get(tlsVersion, cipherSuite, peerCertificates, localCertificates);
				} else {
					handshake = null;
				}
			} finally {
				in.close();
			}
		}

		Headers.Builder addLenient(Headers.Builder builder, String line) {
			int index = line.indexOf(":", 1);
			if (index != -1) {
				return addLenient(builder, line.substring(0, index), line.substring(index + 1));
			} else if (line.startsWith(":")) {
				// Work around empty header names and header names that start with a
				// colon (created by old broken SPDY versions of the response mPostCache).
				return addLenient(builder, "", line.substring(1)); // Empty header name.
			} else {
				return addLenient(builder, "", line); // No header name.
			}
		}

		Headers.Builder addLenient(Headers.Builder builder, String name, String value) {
			builder.add(name, value);
			return builder;
		}

		public Entry(Response response) {
			this.url = response.request().url().toString();
			this.varyHeaders = HttpHeaders.varyHeaders(response);
			this.requestMethod = response.request().method();
			this.protocol = response.protocol();
			this.code = response.code();
			this.message = response.message();
			this.responseHeaders = response.headers();
			this.handshake = response.handshake();
			this.sentRequestMillis = response.sentRequestAtMillis();
			this.receivedResponseMillis = response.receivedResponseAtMillis();
		}

		public void writeTo(DiskLruCache.Editor editor) throws IOException {
			BufferedSink sink = Okio.buffer(editor.newSink(ENTRY_METADATA));

			sink.writeUtf8(url)
					.writeByte('\n');
			sink.writeUtf8(requestMethod)
					.writeByte('\n');
			sink.writeDecimalLong(varyHeaders.size())
					.writeByte('\n');
			for (int i = 0, size = varyHeaders.size(); i < size; i++) {
				sink.writeUtf8(varyHeaders.name(i))
						.writeUtf8(": ")
						.writeUtf8(varyHeaders.value(i))
						.writeByte('\n');
			}

			sink.writeUtf8(new StatusLine(protocol, code, message).toString())
					.writeByte('\n');
			sink.writeDecimalLong(responseHeaders.size() + 2)
					.writeByte('\n');
			for (int i = 0, size = responseHeaders.size(); i < size; i++) {
				sink.writeUtf8(responseHeaders.name(i))
						.writeUtf8(": ")
						.writeUtf8(responseHeaders.value(i))
						.writeByte('\n');
			}
			sink.writeUtf8(SENT_MILLIS)
					.writeUtf8(": ")
					.writeDecimalLong(sentRequestMillis)
					.writeByte('\n');
			sink.writeUtf8(RECEIVED_MILLIS)
					.writeUtf8(": ")
					.writeDecimalLong(receivedResponseMillis)
					.writeByte('\n');

			if (isHttps()) {
				sink.writeByte('\n');
				sink.writeUtf8(handshake.cipherSuite().javaName())
						.writeByte('\n');
				writeCertList(sink, handshake.peerCertificates());
				writeCertList(sink, handshake.localCertificates());
				// The handshake’s TLS version is null on HttpsURLConnection and on older cached responses.
				if (handshake.tlsVersion() != null) {
					sink.writeUtf8(handshake.tlsVersion().javaName())
							.writeByte('\n');
				}
			}
			sink.close();
		}

		private boolean isHttps() {
			return url.startsWith("https://");
		}

		private List<Certificate> readCertificateList(BufferedSource source) throws IOException {
			int length = readInt(source);
			if (length == -1)
				return Collections.emptyList(); // OkHttp v1.2 used -1 to indicate null.

			try {
				CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
				List<Certificate> result = new ArrayList<>(length);
				for (int i = 0; i < length; i++) {
					String line = source.readUtf8LineStrict();
					Buffer bytes = new Buffer();
					bytes.write(ByteString.decodeBase64(line));
					result.add(certificateFactory.generateCertificate(bytes.inputStream()));
				}
				return result;
			} catch (CertificateException e) {
				throw new IOException(e.getMessage());
			}
		}

		private void writeCertList(BufferedSink sink, List<Certificate> certificates)
				throws IOException {
			try {
				sink.writeDecimalLong(certificates.size())
						.writeByte('\n');
				for (int i = 0, size = certificates.size(); i < size; i++) {
					byte[] bytes = certificates.get(i).getEncoded();
					String line = ByteString.of(bytes).base64();
					sink.writeUtf8(line)
							.writeByte('\n');
				}
			} catch (CertificateEncodingException e) {
				throw new IOException(e.getMessage());
			}
		}

		public boolean matches(Request request, Response response) {
			return url.equals(request.url().toString())
					&& requestMethod.equals(request.method())
					&& HttpHeaders.varyMatches(response, varyHeaders, request);
		}

		public Response response(DiskLruCache.Snapshot snapshot) {

			List<String> name = new ArrayList<>(1);
			List<String> value = new ArrayList<>(1);
			RequestBody postParams = new FormBody.Builder().add("1", "1").build();


			String contentType = responseHeaders.get("Content-Type");
			String contentLength = responseHeaders.get("Content-Length");
			Request cacheRequest = new Request.Builder()
					.url(url)
					.method(requestMethod, postParams)
					.headers(varyHeaders)
					.build();
			return new Response.Builder()
					.request(cacheRequest)
					.protocol(protocol)
					.code(code)
					.message(message)
					.headers(responseHeaders)
					.body(new CacheResponseBody(snapshot, contentType, contentLength))
					.handshake(handshake)
					.sentRequestAtMillis(sentRequestMillis)
					.receivedResponseAtMillis(receivedResponseMillis)
					.build();
		}
	}

	static int readInt(BufferedSource source) throws IOException {
		try {
			long result = source.readDecimalLong();
			String line = source.readUtf8LineStrict();
			if (result < 0 || result > Integer.MAX_VALUE || !line.isEmpty()) {
				throw new IOException("expected an int but was \"" + result + line + "\"");
			}
			return (int) result;
		} catch (NumberFormatException e) {
			throw new IOException(e.getMessage());
		}
	}

	private static class CacheResponseBody extends ResponseBody {
		final DiskLruCache.Snapshot snapshot;
		private final BufferedSource bodySource;
		private final String contentType;
		private final String contentLength;

		public CacheResponseBody(final DiskLruCache.Snapshot snapshot,
								 String contentType, String contentLength) {
			this.snapshot = snapshot;
			this.contentType = contentType;
			this.contentLength = contentLength;

			Source source = snapshot.getSource(ENTRY_BODY);
			bodySource = Okio.buffer(new ForwardingSource(source) {
				@Override
				public void close() throws IOException {
					snapshot.close();
					super.close();
				}
			});
		}

		@Override
		public MediaType contentType() {
			return contentType != null ? MediaType.parse(contentType) : null;
		}

		@Override
		public long contentLength() {
			try {
				return contentLength != null ? Long.parseLong(contentLength) : -1;
			} catch (NumberFormatException e) {
				return -1;
			}
		}

		@Override
		public BufferedSource source() {
			return bodySource;
		}
	}
}
