package lib.basenet.config;

import android.app.Application;
import android.text.TextUtils;

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
	private static final int DEFAULT_TIME_OUT = 10 * 1000;

	/**
	 * 默认缓存大小
	 */
	private static final int DEFAULT_CACHE_SIZE = 25 * 1024 * 1024;

	/**
	 * 默认25mb
	 */
	private int cacheSize;

	/**
	 * 超时时间
	 */
	private int timeOut;

	/**
	 * 缓存目录
	 */
	private String cacheDir;

	/**
	 * Application
	 */
	private Application application;

	/**
	 * 启动Post缓存
	 */
	private boolean isPostCache;

	/**
	 * debug 模式
	 */
	private boolean isDebug;


	/**
	 * 单例
	 */
	private static NetConfig instance;

	/**
	 * 全局初始化,构建自己的 NetConfig 对象
	 *
	 * @param builder
	 */
	public static final void init(Builder builder) {
		if (instance == null) {
			instance = builder.build();
		}
	}

	/**
	 * 如果没有调用 init 方法，直接获取实例，将取得默认实例
	 *
	 * @return
	 */
	public static NetConfig getInstance() {
		if (instance == null) {
			instance = new Builder().debug(false).enablePostCache(true).build();
		}
		return instance;
	}


	public Application getApplication() {
		return application;
	}

	/**
	 * 是否debug，debug会进行一些日志输出
	 *
	 * @return
	 */
	public boolean isDebug() {
		return isDebug;
	}

	/**
	 * 超时时间
	 *
	 * @return
	 */
	public int getTimeOut() {
		return timeOut;
	}

	/**
	 * 缓存大小
	 *
	 * @return
	 */
	public int getCacheSize() {
		return cacheSize;
	}

	/**
	 * 获取缓存目录
	 */
	public String getCacheDir() {
		return cacheDir;
	}


	/**
	 * 是否启动post缓存
	 *
	 * @return
	 */
	public boolean isEnablePostCache() {
		return isPostCache;
	}

	/**
	 * 反射获取Applicaton
	 *
	 * @return
	 */
	private Application getApp() {
		try {
			Class<?> activityThread = Class.forName("android.app.ActivityThread");
			Method method = activityThread.getMethod("currentActivityThread");
			final Object invoke = method.invoke(activityThread);
			Method method2 = invoke.getClass().getMethod("getApplication");
			application = (Application) method2.invoke(invoke);                       // 获取context
			return application;
		} catch (Exception e) {
			throw new RuntimeException("获取context 失败！");
		}
	}

	/**
	 * 私有构造
	 *
	 * @param builder
	 */
	private NetConfig(Builder builder) {
		if (builder.app != null) {
			application = builder.app;
		} else {
			application = getApp();
		}

		// 缓存目录
		if (null != builder.cacheDir && !TextUtils.isEmpty(builder.cacheDir)) {
			cacheDir = builder.cacheDir;
		} else {
			File file = new File(application.getCacheDir(), "response");
			if (!file.exists()) {
				file.mkdirs();
			}
			cacheDir = file.getAbsolutePath() + "/response";
		}

		// 缓存大小
		if (builder.cacheSize > 0) {
			cacheSize = builder.cacheSize;
		} else {
			cacheSize = DEFAULT_CACHE_SIZE;
		}

		// 超时时间
		if (builder.timeout > 0)
			this.timeOut = builder.timeout;
		else
			this.timeOut = DEFAULT_TIME_OUT;

		// 是否debug模式
		this.isDebug = builder.debug;

		// 是否启用post缓存
		this.isPostCache = builder.postCache;
	}


	/**
	 * 使用Builder模式
	 */
	public static final class Builder {

		private NetConfig sConfig;      // 配置
		private Application app;
		private String cacheDir;
		private int cacheSize;
		private boolean debug;
		private int timeout;
		private boolean postCache;

		/**
		 * 确保只初始化一次
		 *
		 * @return
		 */
		public NetConfig build() {
			if (sConfig == null) {
				synchronized (Builder.this) {
					if (sConfig == null) {
						sConfig = new NetConfig(this);
					}
				}
			}
			return sConfig;
		}

		/**
		 * 设置Application
		 *
		 * @param application
		 * @return
		 */
		public Builder app(Application application) {
			this.app = application;
			return this;
		}

		/**
		 * 缓存大小，MB为单位
		 *
		 * @param cacheSize
		 * @return
		 */
		public Builder cacheSize(int cacheSize) {
			this.cacheSize = cacheSize * 1024 * 1024;
			return this;
		}

		/**
		 * 是否debug
		 *
		 * @param debug
		 * @return
		 */
		public Builder debug(boolean debug) {
			this.debug = debug;
			return this;
		}

		/**
		 * 缓存目录
		 *
		 * @param cacheDir
		 * @return
		 */
		public Builder cacheDir(String cacheDir) {
			this.cacheDir = cacheDir;
			return this;
		}

		/**
		 * 秒为单位
		 *
		 * @param timeout
		 * @return
		 */
		public Builder timeout(int timeout) {
			this.timeout = timeout * 1000;
			return this;
		}

		/**
		 * post 缓存
		 *
		 * @return
		 */
		public Builder enablePostCache(boolean postCache) {
			this.postCache = postCache;
			return this;
		}
	}
}
