package basenet.better.basenet.bizDemo.handler.response;

import java.util.Map;

import basenet.better.basenet.bizDemo.handler.utils.Net2Utils;


/**
 * Created by liyu20 on 2018/2/5.
 */

public class ResponseHandlerFactory {

    public static <T>ResponseHandler<T> create(final String url, final int level){
        ResponseHandler<T> responseHandler;
        // 用户设置的
        final Map<Integer, Net2Utils.LevelHandler> levelHandlerMap = Net2Utils.getLevelHandlerMap();

        /*
        * 外部请求builder构建时，没有指定level，或者指定的level和全局处理器的level一致，
        * 此时如果设置了全局level响应处理器，则使用全局level响应处理器
        * */
        if (Net2Utils.getDefaultLevelHandle() != null
                && (level != 0 && !levelHandlerMap.containsKey(level))) {
            responseHandler = Net2Utils.getDefaultLevelHandle().getResponseHandler(url, level);  //使用默认的
            return responseHandler;
        }

        // 使用默认的
        if (level == 0) {
            responseHandler = new DefaultResponseHandler(String.class);   // 使用默认的
            return responseHandler;
        }

        // 从用户注册的处理中取
        Net2Utils.LevelHandler levelHandler = levelHandlerMap.get(level);
        if (levelHandler != null) {
            // 获取请求参数处理Handler
            responseHandler = levelHandler.getResponseHandler(url, level);
            if (responseHandler != null) {
                return responseHandler;
            }
        }

        throw new RuntimeException("You must register LevelHandel with level : " + level);
    }
}
