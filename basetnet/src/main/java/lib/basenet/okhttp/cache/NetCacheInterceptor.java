package lib.basenet.okhttp.cache;

import java.io.IOException;

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
		final Request request = chain.request();

		// 获取设置的缓存策略,不管 GET 还是 POST
		if (null != request.cacheControl() && !request.cacheControl().noCache()) {
			int maxAge = request.cacheControl().maxAgeSeconds();
			final Response response = chain.proceed(request);
			final Response wrapper = response.newBuilder()
					.removeHeader("Pragma")
					.removeHeader("Cache-control")
					.header("Cache-control", "max-age=" + maxAge)
					.build();
			return wrapper;
		}

		return chain.proceed(request);
	}
}
