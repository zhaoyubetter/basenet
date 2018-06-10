package basenet.better.basenet.bizDemo.handler;

import java.util.List;

/**
 * 业务回调
 *
 * @param <T>
 */
public abstract class ReqCallback<T> {
    private Class<T> clazz;

    public Class<T> getClazz() {
        return this.clazz;
    }

    public ReqCallback(Class<T> clazz) {
        this.clazz = clazz;
    }

    public abstract void onFailure(String message, String code, String rawData);

    public abstract void onSuccess(T var1, List<T> var2, String raw);

    public void onProgressUpdate(long contentLength, long bytesRead, boolean done) {
    }

}