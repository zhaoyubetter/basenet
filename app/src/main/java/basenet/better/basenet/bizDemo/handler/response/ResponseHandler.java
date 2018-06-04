package basenet.better.basenet.bizDemo.handler.response;

import android.text.TextUtils;

import java.io.File;

import lib.basenet.exception.BaseNetException;
import lib.basenet.request.AbsRequestCallBack;
import lib.basenet.response.Response;

/**
 * responseHandler 处理器
 *
 * @param <T>
 */
public abstract class ResponseHandler<T> extends AbsRequestCallBack<String> {

    /**
     * 原始callback
     */
    private AbsRequestCallBack<T> originCallBack;

    /**
     * 具体业务数据
     */
    protected Class<T> clazz;

    public Class<T> getClazz() {
        return this.clazz;
    }

    public ResponseHandler(Class<T> clazz) {
        if(clazz == null) {
            throw new RuntimeException("clazz can't be null, default can use String.class");
        }
        this.clazz = clazz;
    }

    public final void setOiginalCallback(AbsRequestCallBack callback) {
        this.originCallBack = callback;
    }

    @Override
    public final void onSuccess(final Response response) {
        /* 在这里处理响应 */

        // ==== 1. 头处理
        try {
            headerParser(response);
        } catch (Exception e) {
            if (originCallBack != null) {
                MainThreadDelivery.post(new Runnable() {
                    @Override
                    public void run() {
                        originCallBack.onFailure(new BaseNetException("header parser error!"));
                    }
                });
            }

            return;     // 头部处理失败
        }

        // ==== 2. 请求是否成功判断
        if (!response.isSuccessful()) {
            if (originCallBack != null) {
                MainThreadDelivery.post(new Runnable() {
                    @Override
                    public void run() {
                        originCallBack.onFailure(new Exception(response.message));
                    }
                });
            }
            return;
        }

        // ==== 通过body判断是不是下载文件
        if (response.responseBody instanceof File) {
            if (originCallBack != null) {
                MainThreadDelivery.post(new Runnable() {
                    @Override
                    public void run() {
                        originCallBack.onFailure(new Exception(response.message));
                    }
                });
            }
            return;
        }

        //===== 响应体 body 不是String
        if (!(response.responseBody instanceof String)) {
            MainThreadDelivery.post(new Runnable() {
                @Override
                public void run() {
                    originCallBack.onFailure(new Exception(response.message));
                }
            });
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
        try {
            // ==== 获取解析后的body原始数据
            String body = getContentData(originData);       // 获取body数据
            final Response<T> resultResp = new Response<>(response.request, response.responseHeader, null);
            // ==== 外界解析数据
            contentParse(resultResp, body, clazz);

            resultResp.statusCode = response.statusCode;
            if (originCallBack != null) {
                MainThreadDelivery.post(new Runnable() {
                    @Override
                    public void run() {
                        originCallBack.onSuccess(resultResp);
                    }
                });
            }
        } catch (Exception e) {
            if (originCallBack != null) {
                originCallBack.onFailure(new BaseNetException(e));
            }
        }
    }


    /**
     * 1、处理头部内容
     */
    public void headerParser(Response response) throws Exception {
    }

    /**
     * 2. 处理body，可以是：
     * a.解压；
     * b.解密
     * c.解析；
     */
    public String getContentData(String originData) throws Exception {
        return originData;
    }

    /**
     * 3.数据处理，用户真正需要的数据
     *
     * @throws Exception
     */
    public void contentParse(Response response, String content, Class clazz) throws Exception {
        if (TextUtils.isEmpty(content)) {
            throw new BaseNetException("server exception: content is null!");
        }
    }
}
