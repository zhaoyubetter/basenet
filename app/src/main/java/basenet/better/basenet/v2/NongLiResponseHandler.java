package basenet.better.basenet.v2;


import org.json.JSONObject;

import basenet.better.basenet.bizDemo.handler.response.ResponseHandler;
import basenet.better.basenet.bizDemo.handler.utils.JsonParser;
import lib.basenet.exception.BaseNetException;
import lib.basenet.response.Response;

/**
 * 具体服务器解析封装
 */
public class NongLiResponseHandler extends ResponseHandler {

    public NongLiResponseHandler(Class<NongliBean> clazz) {
        super(clazz);
    }

    @Override
    public void headerParser(Response response) throws Exception {
        super.headerParser(response);
    }

    /**
     * 获取业务数据
     * @param originData
     * @return
     * @throws Exception
     */
    @Override
    public String getContentData(String originData) throws Exception {
        // 预解析
        JSONObject object = new JSONObject(originData);
        int status = object.optInt("status");
        String message = object.optString("message");
        if (status != 200) {
            throw new BaseNetException(message);
        } else {
            return object.optString("data");
        }
    }

    @Override
    public void contentParse(Response response, String content, Class clazz) throws Exception {
        super.contentParse(response, content, clazz);
        new JsonParser<>(clazz).resultOperate(content, response);
    }
}
