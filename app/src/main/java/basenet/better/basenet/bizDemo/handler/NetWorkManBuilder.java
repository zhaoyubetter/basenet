package basenet.better.basenet.bizDemo.handler;


import android.text.TextUtils;

import com.google.gson.Gson;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import basenet.better.basenet.bizDemo.handler.exception.CustomBizException;
import basenet.better.basenet.bizDemo.handler.request.DefaultRequestHandler;
import basenet.better.basenet.bizDemo.handler.request.RequestHandler;
import basenet.better.basenet.bizDemo.handler.response.DefaultResponseHandler;
import basenet.better.basenet.bizDemo.handler.response.ResponseHandler;
import lib.basenet.okhttp.OkHttpRequest;
import lib.basenet.request.BaseRequestBody;

/**
 * 网络请求入口，包装器
 *
 * @author zhaoyubetter
 * @since V2.0.0
 */
public final class NetWorkManBuilder {

    private OkHttpRequest.Builder builder;

    protected ReqCallback mCallBack;
    protected Map<String, Object> mParams;
    protected Map<String, String> mHeader;

    private boolean mUserJsonBody;
    private String mAction;

    private RequestHandler requestHandler;
    private ResponseHandler responseHandler;

    public NetWorkManBuilder() {
        builder = new OkHttpRequest.Builder();
    }

    /**
     * int GET = 0;
     * int POST = 1;
     * int PUT = 2;
     * int DELETE = 3;
     * int HEAD = 4;
     * int PATCH = 6;
     * int OPTIONS = 7;
     *
     * @param type
     * @return
     */
    public NetWorkManBuilder type(int type) {
        builder.type(type);
        return this;
    }

    public NetWorkManBuilder cacheTime(int time) {
        builder.cacheTime((int) (time / 1000));
        return this;
    }

    public NetWorkManBuilder timeout(long time) {
        builder.timeout(time);
        return this;
    }

    public NetWorkManBuilder tag(String tag) {
        builder.tag(tag);
        return this;
    }

    public NetWorkManBuilder downFile(File file) {
        builder.downFile(file);
        return this;
    }

    public NetWorkManBuilder uploadFiles(Map<String, File> files) {
        builder.uploadFiles(files);
        return this;
    }

    /* --------------------------------------------------------  other......... ------------------------------*/
    public NetWorkManBuilder requestHandler(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;
        return this;
    }

    public NetWorkManBuilder responseHandler(ResponseHandler responseHandler) {
        this.responseHandler = responseHandler;
        return this;
    }

    public NetWorkManBuilder action(String url) {
        this.mAction = url;
        return this;
    }

    public <T> NetWorkManBuilder callback(ReqCallback<T> callback) {
        this.mCallBack = callback;
        return this;
    }

    public NetWorkManBuilder params(Map<String, Object> params) {
        if (params != null) {
            this.mParams = new HashMap<>();
            // 过滤 null value
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                if (entry.getValue() == null) {
                    continue;
                }
                this.mParams.put(entry.getKey(), entry.getValue());
            }
        }
        return this;
    }

    /**
     * body 类型为 json
     */
    public NetWorkManBuilder useJsonBody(boolean useJsonBody) {
        this.mUserJsonBody = useJsonBody;
        return this;
    }

    /**
     * 请求方法
     */
    public final void go() {
        if (TextUtils.isEmpty(mAction)) {
            throw new RuntimeException("action is null,please checked!");
        }
        if (mParams == null)
            mParams = new HashMap<>();
        if (mHeader == null)
            mHeader = new HashMap<>();

        // ==== 1. 请求参数进行预处理的Handler
        final RequestHandler paramHandler = this.requestHandler != null ? this.requestHandler : new DefaultRequestHandler();
        // ==== 2.根据requestHandler设置信息 =====
        paramHandler.handleHeader(mHeader); // 设置头部信息
        // ==== 3.判断请求是否jsonObject
        handleJsonBody();       // 判断是否json body体
        // ==== 4. 请求的响应处理器
        ResponseHandler responseHandler = this.responseHandler != null ? this.responseHandler : new DefaultResponseHandler<String>();
        responseHandler.setCallback(mCallBack);

        // ==== 5.请求参数处理
        try {
            paramHandler.handleParams(mParams);
        } catch (CustomBizException e) {
            if (responseHandler != null) {
                responseHandler.onFailure(e);         // 直接执行错误回调
            }
        }

        // ==== 6.发起请求 (header、mParams、callback，需要重新设置)
        builder.url(mAction).headers(mHeader).body(mParams).callback(responseHandler).build().request();
    }


    private void handleJsonBody() {
        //对于json串，应该是不需要添加参数信息的
        if(mUserJsonBody) {
            final String json = new Gson().toJson(mParams);
            builder.requestBody(new BaseRequestBody() {
                @Override
                public String getBodyContentType() {
                    return "application/json";
                }
                @Override
                public byte[] getBody() {
                    return json.getBytes();
                }
            });
        }
    }
}
