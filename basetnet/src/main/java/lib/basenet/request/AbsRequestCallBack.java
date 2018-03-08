package lib.basenet.request;


import lib.basenet.response.Response;

/**
 * Created by zhaoyu1 on 2017/3/6.
 */
public abstract class AbsRequestCallBack<T> {
	/**
	 * 这个方法不一定返回就是成功，需要判断 code
	 * @param response
	 */
	public void onSuccess(Response<T> response) {
	}

	public void onFailure(Throwable e) {
	}

	// 取消
	public void onCancel() {

	}

	public void onProgressUpdate(long contentLength, long bytesRead, boolean done) {
	}
}
