package basenet.better.basenet.bizDemo.handler.request;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import basenet.better.basenet.bizDemo.handler.exception.CustomBizException;

/**
 * Default 方式直接返回原文
 */
public final class NetDiskRequestHandler implements RequestHandler {

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
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.fff");
        final String timeStamp = sdf.format(new Date());
        originParams.put("timeStamp", timeStamp);
    }
}
