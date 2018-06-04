package basenet.better.basenet.bizDemo.handler.utils;

import java.util.Collections;
import java.util.Map;

import basenet.better.basenet.bizDemo.handler.request.RequestHandler;
import basenet.better.basenet.bizDemo.handler.response.ResponseHandler;
import lib.basenet.request.AbsRequestCallBack;

/**
 * 新增的工具
 */
public class Net2Utils {

    /*---- 版本V2.0.0新增 start -----------------------------------------------------------------------  */

    private static LevelHandler levelHandler;
    private static Map<Integer, LevelHandler> levelHandlerMap = Collections.emptyMap();

    /**
     * 全局请求和响应的统一处理器，可选项
     */
    public synchronized static void initLevelHandler(Map<Integer, LevelHandler> levelHandlerMap, LevelHandler defaultLevelHandler) {
        if (levelHandlerMap == null) {
            throw new RuntimeException("The param levelHandler should not be null !!");
        }

        for (Map.Entry<Integer, LevelHandler> entry : levelHandlerMap.entrySet()) {
            if (entry.getValue() == null) {
                throw new RuntimeException("LevelHandler's methods should not return null !");
            }
        }
        levelHandler = defaultLevelHandler;
    }

    public static Map<Integer, LevelHandler> getLevelHandlerMap() {
        return levelHandlerMap;
    }

    /**
     * 默认处理器
     *
     * @return
     */
    public static LevelHandler getDefaultLevelHandle() {
        return levelHandler;
    }

    /**
     * 请求级别处理器
     */
    public interface LevelHandler {
        /**
         * 请求参数处理器
         *
         * @param url
         * @param callBack
         * @param level
         */
        RequestHandler getRequestHandler(final String url, final AbsRequestCallBack callBack, final int level);

        /**
         * 响应数据处理器
         */
        <T> ResponseHandler<T> getResponseHandler(final String url, final int level);
    }
}
