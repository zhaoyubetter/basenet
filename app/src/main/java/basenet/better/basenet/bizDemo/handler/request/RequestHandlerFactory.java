package basenet.better.basenet.bizDemo.handler.request;


import java.util.Map;

import basenet.better.basenet.bizDemo.handler.utils.Net2Utils;
import lib.basenet.request.AbsRequestCallBack;

/**
 * Created by liyu20 on 2018/2/5.
 */
public final class RequestHandlerFactory {

    public static <T> RequestHandler create(final String url, final AbsRequestCallBack<T> callback, final int level) {
        RequestHandler requestHandler = null;

        // 用户设置的
        final Map<Integer, Net2Utils.LevelHandler> levelHandlerMap = Net2Utils.getLevelHandlerMap();

        /*
         * 外部请求builder构建时，没有指定level，或者指定的level和全局处理器的level一致，
         * 此时如果设置了全局level请求处理器，则使用全局level请求处理器
         * */
        if (Net2Utils.getDefaultLevelHandle() != null
                && (level != 0 && !levelHandlerMap.containsKey(level))) {
            requestHandler = Net2Utils.getDefaultLevelHandle().getRequestHandler(url, callback, level);  //使用默认的
            return requestHandler;
        }

        // 使用默认的
        if (level == 0) {
            requestHandler = new DefaultRequestHandler();   // 使用默认的
            return requestHandler;
        }

        // 从用户注册的处理中取
        Net2Utils.LevelHandler levelHandler = levelHandlerMap.get(level);
        if (levelHandler != null) {
            // 获取请求参数处理Handler
            requestHandler = levelHandler.getRequestHandler(url, callback, level);
            if (requestHandler != null) {
                return requestHandler;
            }
        }

        throw new RuntimeException("You must register LevelHandel with level : " + level);
    }
}
