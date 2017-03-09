package lib.basenet.request;

/**
 * Created by zhaoyu1 on 2017/3/6.
 */
public abstract class AbsRequestCallBack<T> {
    public void onSuccess(T t) {
    }

    public void onFailure(Throwable e) {
    }

    public void onProgressUpdate(long contentLength, long bytesRead, boolean done) {
    }
}
