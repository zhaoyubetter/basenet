package basenet.better.basenet.bizDemo.handler;


import basenet.better.basenet.bizDemo.handler.request.RequestHandler;
import basenet.better.basenet.bizDemo.handler.request.RequestHandlerFactory;
import basenet.better.basenet.bizDemo.handler.response.ResponseHandler;
import basenet.better.basenet.bizDemo.handler.response.ResponseHandlerFactory;
import lib.basenet.okhttp.OkHttpRequest;
import lib.basenet.request.BaseRequestBody;

/**
 * 网络请求入口，包装器
 *
 * @author zhaoyubetter
 * @since V2.0.0
 */
public final class NetRequest_bak extends OkHttpRequest {

    protected NetRequest_bak(Builder builder) {
        super(builder);
    }

    /**
     * 使用OKHttpBuilder
     */
    public static class Builder extends OkHttpRequest.Builder{
        // 新增参数
        RequestHandler requestHandler;
        ResponseHandler responseHandler;
        int level = 0;
        String jsonBody;

        public Builder() {
        }

        /**
         * request 处理
         *
         * @param requestHandler
         * @return
         */
        public Builder reqHandler(RequestHandler requestHandler) {
            this.requestHandler = requestHandler;
            return this;
        }

        /**
         * reponse 处理
         *
         * @param responseHandler
         * @return
         */
        public Builder respHandler(ResponseHandler responseHandler) {
            this.responseHandler = responseHandler;
            return this;
        }

        public Builder jsonBody(String jsonBody) {
            this.jsonBody = jsonBody;
            return this;
        }

        @Override
        public NetRequest_bak build() {

            // ==== 1. 根据level，获取对请求参数进行预处理的Handler
            final RequestHandler paramHandler = this.requestHandler != null ? this.requestHandler : RequestHandlerFactory.create(mUrl, mCallBack, level);
            if (paramHandler == null) {
                return null;
            }

            // ==== 2.根据requestHandler设置信息 =====
            paramHandler.handleHeader(mHeader); // 设置头部信息
            // ==== 3.判断请求是否jsonObject
            handleJsonBody();       // 判断是否json body体
            // ==== 4. 根据level，获取对应请求的响应处理器
            ResponseHandler responseHandler = this.responseHandler != null ? this.responseHandler : ResponseHandlerFactory.create(mUrl, level);
            responseHandler.setOiginalCallback(mCallBack);  // 外界使用回调
            mCallBack = responseHandler;

            // ==== 5.请求参数处理
            try {
                paramHandler.handleParams(mParams);
            } catch (Exception e) {
                if (responseHandler != null) {
                    responseHandler.onFailure(e);         // 直接执行错误回调
                }
            }

            return new NetRequest_bak(this);
        }

        private void handleJsonBody() {
            if (jsonBody != null) {  //对于json串，应该是不需要添加参数信息的
                final String json = jsonBody;
                requestBody(new BaseRequestBody() {
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
}
