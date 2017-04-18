package lib.basenet.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by zhaoyu on 2017/4/18.
 */

public final class NetworkUtils {
	/**
	 * 判断wifi是否处于连接状态
	 *
	 * @return boolean :返回wifi是否连接
	 */
	private static boolean isWiFi(Context context) {
		ConnectivityManager manager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = manager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		boolean result = false;
		if (networkInfo != null) {
			result = networkInfo.isConnected();
		}
		return result;
	}

	/**
	 * 判断是否APN列表中某个渠道处于连接状态
	 *
	 * @return 返回是否连接
	 */
	private static boolean isMobile(Context context) {
		ConnectivityManager manager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = manager
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		boolean result = false;
		if (networkInfo != null) {
			result = networkInfo.isConnected();
		}
		return result;
	}

	/**
	 * 判断当前apn列表中哪个连接选中了
	 */
	public static boolean hasNet(Context context) {
		boolean wifi = isWiFi(context);
		boolean mobile = isMobile(context);
		return !(!wifi && !mobile);
	}
}
