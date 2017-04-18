package lib.basenet.config;

/**
 * 全局配置
 * Created by zhaoyu on 2017/4/18.
 */
public final class NetConfig {

	private static int DEFAULT_TIME_OUT = 10;
	private static int DEFAULT_CACHE_SIZE = 24;


	/**
	 * 统一设置超时时间, 秒为单位
	 *
	 * @param timeOut
	 */
	public static void setTimeOut(int timeOut) {
		if (timeOut > 0) {
			DEFAULT_TIME_OUT = timeOut;
		}
	}

	/**
	 * 设置缓存大小，MB 为单位
	 *
	 * @param size
	 */
	public static void setCacheSize(int size) {
		if (size > 0) {
			DEFAULT_CACHE_SIZE = size;
		}
	}

	public static int getTimeOut() {
		return DEFAULT_TIME_OUT;
	}

	public static int getCacheSize() {
		return DEFAULT_CACHE_SIZE;
	}


}
