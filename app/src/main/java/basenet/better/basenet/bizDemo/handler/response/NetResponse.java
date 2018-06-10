package basenet.better.basenet.bizDemo.handler.response;

import java.util.List;

/**
 * 网络响应类
 */
public final class NetResponse<T> {
    public String errorCode;
    public String errorMsg;
    public boolean isFromCache;     // 是否来自缓存
    /**
     * 后端返回的明文
     */
    public String rawData;

    public boolean isSuccess;//是否成功
    public T t;
    public List<T> tArray;


    // http 状态
    public int httpCode;
    public String httpMsg;

    public NetResponse() {
    }
}
