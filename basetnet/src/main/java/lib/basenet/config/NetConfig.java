package lib.basenet.config;

import android.app.Application;

import java.io.File;
import java.lang.reflect.Method;

/**
 * 全局配置
 * Created by zhaoyu on 2017/4/18.
 */
public final class NetConfig {

	/**
	 * 默认10s
	 */
	private static int DEFAULT_TIME_OUT = 10 * 1000;
	/**
	 * 默认10mb
	 */
	private static int cache_size = 25 * 1024 * 1024;

	/**
	 * 缓存目录
	 */
	private static String cacheDir;

	/**
	 * Application
	 */
	public static Application application;

	static {
		try {
			Class<?> activityThread = Class.forName("android.app.ActivityThread");
			Method method = activityThread.getMethod("currentActivityThread");
			final Object invoke = method.invoke(activityThread);
			Method method2 = invoke.getClass().getMethod("getApplication");
			application = (Application) method2.invoke(invoke);                       // 获取context
			File file = new File(application.getExternalCacheDir(), "responses");  // 可使用 getCacheDir()
			if (!file.exists()) {
				file.mkdirs();
			}
			cacheDir = file.getAbsolutePath();
		} catch (Exception e) {
			throw new RuntimeException("获取context 失败！");
		}
	}

	private NetConfig() {
	}

	/**
	 * 统一设置超时时间, 耗秒为单位
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
			cache_size = size * 1024 * 1024;
		}
	}

	public static int getTimeOut() {
		return DEFAULT_TIME_OUT;
	}

	public static int getCacheSize() {
		return cache_size;
	}

	/**
	 * 获取缓存目录
	 *
	 * @return
	 */
	public static String getCacheDir() {
		return cacheDir;
	}

	public static void setCacheDir(String dir) {
		cacheDir = dir;
	}
}
