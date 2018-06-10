package basenet.better.basenet.v2;


import org.json.JSONException;
import org.json.JSONObject;

import basenet.better.basenet.bizDemo.handler.ReqCallback;
import basenet.better.basenet.bizDemo.handler.exception.CustomBizException;
import basenet.better.basenet.bizDemo.handler.response.NetResponse;
import basenet.better.basenet.bizDemo.handler.response.ResponseHandler;
import basenet.better.basenet.bizDemo.handler.utils.JsonParser;
import lib.basenet.exception.BaseNetException;
import lib.basenet.response.Response;

/**
 * 具体服务器解析封装
 */
public class NongLiResponseHandler extends ResponseHandler {


    /**
     * 获取业务数据
     * @param originData
     * @return
     * @throws Exception
     */
    @Override
    public String getContentData(String originData) throws CustomBizException {
        // 预解析
        JSONObject object = null;
        try {
            object = new JSONObject(originData);
            int status = object.optInt("status");
            String message = object.optString("message");
            if (status != 200) {
                throw new CustomBizException(message);
            } else {
                return object.optString("data");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public void contentParse(NetResponse response, String content) throws CustomBizException {
        super.contentParse(response, content);
        new JsonParser<>(reqCallback.getClazz()).resultOperate(content, response);
    }
}
