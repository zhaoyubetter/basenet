package basenet.better.basenet.bizDemo.handler.response;


import java.io.File;
import java.util.List;

import basenet.better.basenet.bizDemo.handler.ReqCallback;
import basenet.better.basenet.bizDemo.handler.exception.CustomBizException;
import lib.basenet.request.AbsRequestCallBack;
import lib.basenet.response.Response;

/**
 * responseHandler 处理器包装器
 *
 * @param <T>
 */
public abstract class ResponseHandler<T> extends AbsRequestCallBack<String> {

    private ResReqCallback innerCallback;
    protected ReqCallback<T> reqCallback;

    public final void setCallback(ReqCallback<T> reqCallback) {
        this.reqCallback = reqCallback;
        if (reqCallback != null) {
            this.innerCallback = new ResReqCallback(reqCallback.getClazz(), reqCallback);
        }
    }

    @Override
    public final void onFailure(Throwable e) {
        super.onFailure(e);
        if (innerCallback != null) {
            if (e instanceof CustomBizException) {
                innerCallback.onFailure(e.getMessage(), ((CustomBizException) e).code, null);
            } else {
                innerCallback.onFailure(e.getMessage(), "", null);
            }
        }
    }

    @Override
    public final void onProgressUpdate(long contentLength, long bytesRead, boolean done) {
        super.onProgressUpdate(contentLength, bytesRead, done);
        if (innerCallback != null) {
            innerCallback.onProgressUpdate(contentLength, bytesRead, done);
        }
    }

    @Override
    public final void onSuccess(final Response response) {
        /* 在这里处理响应 */

        // ==== 1. 头处理
        try {
            headerParser(response);
        } catch (Exception e) {
            if (innerCallback != null) {
                innerCallback.onFailure("header parser error!", "" + response.statusCode, null);
            }
            return;     // 头部处理失败
        }

        // ==== 2. 请求是否成功判断
        if (!response.isSuccessful()) {
            if (innerCallback != null) {
                innerCallback.onFailure(response.message, response.statusCode + "", null);
                if (response.responseBody instanceof File) {
                    innerCallback.onFailure(response.message, response.statusCode + "", null);
                } else if (response.responseBody instanceof String) {
                    innerCallback.onFailure(response.message, response.statusCode + "", (String) response.responseBody);
                } else {
                    innerCallback.onFailure(response.message, response.statusCode + "", null);
                }
            }
            return;
        }

        // ==== 通过body判断是不是下载文件
        if (response.responseBody instanceof File) {
            String name = ((File) response.responseBody).getAbsolutePath();
            innerCallback.onSuccess(null, null, name);
            return;
        }

        //===== 响应体 body 不是String
        if (!(response.responseBody instanceof String)) {
            innerCallback.onFailure(response.message, response.statusCode + "", null);
            return;
        }

        // ==== 3.获取原始数据，并赋值
        String originData = (String) response.responseBody;

        /** ==== 4. 处理解压原始数据，比如：
         * 1.解压；
         * 2.解密
         * 3.解析；
         */
        // ==== 4. 处理原始数据
        NetResponse<T> result = new NetResponse<>();
        result.isFromCache = response.isFromCache;
        result.httpCode = response.statusCode;
        result.httpMsg = response.message;

        try {
            // ==== 获取解析后的body原始数据
            String body = getContentData(originData);       // 获取body数据
            // ==== 外界解析数据
            contentParse(result, body);
            if (innerCallback != null) {
                innerCallback.onSuccess(result.t, result.tArray, result.rawData);
            }
        } catch (CustomBizException e) {
            if (innerCallback != null) {
                innerCallback.onFailure(e.getMessage(), e.code, originData);
            }
        }
    }


    /**
     * 1、处理头部内容
     */
    public void headerParser(Response response) throws CustomBizException {
    }

    /**
     * 2. 处理body，可以是：
     * a.解压；
     * b.解密
     * c.解析；
     */
    public abstract String getContentData(String originData) throws CustomBizException;

    /**
     * 3.数据处理，用户真正需要的数据
     *
     * @throws CustomBizException
     */
    public void contentParse(NetResponse<T> response, String content) throws CustomBizException {
        response.isSuccess = true;   // 业务成功，失败设定；
    }

    /*---------- 内部执行回调，包装，主线程中，回调 ---------------*/
    private class ResReqCallback extends ReqCallback<T> {
        private ReqCallback<T> reqCallback;

        private ResReqCallback(Class<T> clazz, ReqCallback<T> reqCallback) {
            super(clazz);
            this.reqCallback = reqCallback;
        }

        @Override
        public void onFailure(final String errorMsg, final String code, final String rawData) {
            MainThreadDelivery.post(new Runnable() {
                @Override
                public void run() {
                    reqCallback.onFailure(errorMsg, code, rawData);
                }
            });
        }

        @Override
        public void onSuccess(final T t, final List<T> tArray, final String rawData) {
            MainThreadDelivery.post(new Runnable() {
                @Override
                public void run() {
                    reqCallback.onSuccess(t, tArray, rawData);
                }
            });
        }

        @Override
        public void onProgressUpdate(final long contentLength, final long bytesRead, final boolean done) {
            MainThreadDelivery.post(new Runnable() {
                @Override
                public void run() {
                    reqCallback.onProgressUpdate(contentLength, bytesRead, done);
                }
            });
        }
    }
}
