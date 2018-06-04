package basenet.better.basenet.bizDemo.handler.utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import lib.basenet.response.Response;

/**
 * 需要创建一个与业务相关的response对象
 * Created by liyu20 on 2018/2/5.
 */
public class JsonParser<E> {

    private Class<E> clazz;

    public JsonParser(Class<E> clazz) {
        this.clazz = clazz;
    }

    public <T> Response<T> resultOperate(String data, Response<T> result) {
        if (data != null) {
            try {
                if (clazz == String.class) {
                    result.responseBody = (T) data;
                    return result;
                }
                com.google.gson.JsonParser jsonParser = new com.google.gson.JsonParser();
                JsonElement contentElem = jsonParser.parse(data);
                if (clazz == null) {
                    result.responseBody = null;
//                    result.responseArray = null;
                    return result;
                }
                if (contentElem.isJsonObject()) {
                    fromJson(clazz, data, result);
                } else if (contentElem.isJsonArray()) {
                    fromJsonList(clazz, data, result);
                } else {//content没返回数据，但整体格式是正确的，只不过数据都为null。
                    result.responseBody = null;
//                    result.responseArray = null;
                }
            } catch (Exception e) {
                // 服务端返回的json根本不对
//                result.bizCode = "5000";
//                result.bizMessage = "data parse error: " + data;
            }
        } else {
//            result.bizCode = "5000";
//            result.bizMessage = "server no data response";
        }
        return result;
    }

    private static <T> void fromJson(Class cls, String json, Response<T> result) {
        result.responseBody = (T) new Gson().fromJson(json, cls);
    }

    private static <T> void fromJsonList(Class cls, String json, Response<T> result) throws JSONException {
        JSONArray jsonArray = new JSONArray(json);
        if (jsonArray != null && jsonArray.length() > 0) {
            List<T> tArray = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                tArray.add((T) new Gson().fromJson(obj.toString(), cls));
            }
//            result.responseArray = tArray;
        } else {
//            result.responseArray = null;
        }
        result.responseBody = null;
    }
}
