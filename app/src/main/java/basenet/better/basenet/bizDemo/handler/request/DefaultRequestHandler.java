package basenet.better.basenet.bizDemo.handler.request;

import java.util.Map;

import lib.basenet.exception.BaseNetException;

/**
 * Default 方式直接返回原文
 * Created by liyu20 on 2018/2/5.
 */
public final class DefaultRequestHandler implements RequestHandler {

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleHeader(Map<String, String> originHeaders) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleParams(Map<String, Object> originParams) throws BaseNetException {

    }

}
