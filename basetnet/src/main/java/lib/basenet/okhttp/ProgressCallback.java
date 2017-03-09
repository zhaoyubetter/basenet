package lib.basenet.okhttp;

/**
 * 进度监听
 */
public interface ProgressCallback {
    /**
     * @param contentLength 总进度
     * @param bytesRead     当前进度
     * @param done          是否完成
     */
    void update(long contentLength, long bytesRead, boolean done);
}
