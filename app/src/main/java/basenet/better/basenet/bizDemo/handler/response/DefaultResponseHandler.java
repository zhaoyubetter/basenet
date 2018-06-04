package basenet.better.basenet.bizDemo.handler.response;

import lib.basenet.response.Response;

/**
 * response 处理
 * @param <T>
 */
public class DefaultResponseHandler<T> extends ResponseHandler<T> {


    public DefaultResponseHandler(Class<T> clazz) {
        super(clazz);
    }

    /**
     * 1、处理头部内容
     */
    public void headerParser(Response response) throws Exception {
    }

    /**
     * 2. 处理body，可以是：
     * a.解压；
     * b.解密
     * c.解析；
     */
    public String getContentData(String originData) throws Exception{
        return originData;
    }
}