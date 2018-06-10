package basenet.better.basenet.bizDemo.handler.request;


import java.util.Map;

import basenet.better.basenet.bizDemo.handler.exception.CustomBizException;

/**
 * Default 方式直接返回原文
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
    public void handleParams(Map<String, Object> originParams) throws CustomBizException {

    }
}
