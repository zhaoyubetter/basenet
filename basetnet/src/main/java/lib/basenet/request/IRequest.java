package lib.basenet.request;


import lib.basenet.response.Response;

/**
 * 网络请求封装接口
 *
 * @author zhaoyu
 * @author hufeng
 * @version 1.0
 * @since 2017/3/6.
 */
public interface IRequest {

    interface RequestType {
        int GET = 0;
        int POST = 1;
    }

    /**
     * 执行请求，默认是 get 方式
     */
    void request();

    /**
     * 取消网络请求
     */
    void cancel();

    /**
     * 同步請求
     */
    Response requestSync();

}
