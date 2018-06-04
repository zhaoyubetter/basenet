package basenet.better.basenet.bizDemo.handler.request;

import java.util.Map;

/**
 * <pre>
 * 请求预处理操作接口
 * Created by liyu20 on 2018/2/5.
 */
public interface RequestHandler {

    /**
     * 设置头部信息
     * 设置头部信息，不会出异常的
     */
    void handleHeader(Map<String, String> originHeaders);

    /**
     * 处理请求参数，可以是：
     * 1.添加公共参数；
     * 2.加密
     * 3.压缩
     *
     * @param originParams
     */
    void handleParams(Map<String, Object> originParams) throws Exception;

}