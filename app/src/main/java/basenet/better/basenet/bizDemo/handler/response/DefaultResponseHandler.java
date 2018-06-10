package basenet.better.basenet.bizDemo.handler.response;


import basenet.better.basenet.bizDemo.handler.exception.CustomBizException;
import lib.basenet.response.Response;

/**
 * response 处理
 *
 * @param <T>
 */
public class DefaultResponseHandler<T> extends ResponseHandler<T> {

    /**
     * 1、处理头部内容
     */
    public void headerParser(Response response) throws CustomBizException {
    }

    /**
     * 2. 处理body并获取需要解析的信息，这里可以是：
     * a.解压；
     * b.解密
     * c.获取content结果，如下 content
     * <pre>
     *     {
     *         'code' : 111
     *         'msg': 'ok'
     *         content: { XXX}
     *     }
     * </pre>
     */
    public String getContentData(String originData) throws CustomBizException {
        return originData;
    }

    /**
     * 解析
     *
     * @param response
     * @param content
     * @throws Exception
     */
    @Override
    public void contentParse(NetResponse<T> response, String content) throws CustomBizException {
        super.contentParse(response, content);
    }
}