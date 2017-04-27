package lib.basenet.okhttp.cache;

import java.io.IOException;

import lib.basenet.config.NetConfig;
import lib.basenet.utils.NetworkUtils;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;


/**
 * 缓存拦截
 * Created by zhaoyu on 2017/4/18.
 */
public class NetCacheInterceptor implements Interceptor {

	@Override
	public Response intercept(Chain chain) throws IOException {
		Request request = chain.request();

		// 从request获取是否能存储缓存
		if (null != request.cacheControl() && !request.cacheControl().noStore()) {
			// 是否有网络
			boolean hasNet = NetworkUtils.hasNet(NetConfig.application);
			// 原始响应
			final Response originResponse;

			if (!hasNet) {
				// 无网络，强制从缓存中获取
				request = request.newBuilder().cacheControl(CacheControl.FORCE_CACHE).build();
				originResponse = chain.proceed(request);
				return originResponse;
			} else {
				// 有网络，设置缓存时间 （通过下面一行，可拿到网络上的返回数据）
				originResponse = chain.proceed(request);
				// 再包装一下originResponse，方便后面的 CacheInterceptor、PostCacheInterceptor 来做缓存处理
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
}
