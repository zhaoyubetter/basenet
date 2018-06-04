package basenet.better.basenet.bizDemo.handler;//package lib.basenet;
//
//
//import java.io.File;
//import java.util.Map;
//
//import lib.basenet.handler.request.RequestHandler;
//import lib.basenet.handler.request.RequestHandlerFactory;
//import lib.basenet.handler.response.ResponseHandler;
//import lib.basenet.handler.response.ResponseHandlerFactory;
//import lib.basenet.okhttp.OkHttpRequest;
//import lib.basenet.request.AbsRequest;
//import lib.basenet.request.AbsRequestCallBack;
//import lib.basenet.request.BaseRequestBody;
//
///**
// * 网络请求入口，包装器
// *
// * @author zhaoyubetter
// * @since V2.0.0
// */
//public final class NetRequest extends OkHttpRequest {
//
//    protected NetRequest(Builder builder) {
//        super(builder);
//    }
//
//    /**
//     * 使用OKHttpBuilder
//     */
//    public static class Builder {
//        // 新增参数
//        RequestHandler requestHandler;
//        ResponseHandler responseHandler;
//        int level = 0;
//        String jsonBody;
//        protected String mUrl;
//        protected Map<String, Object> mParams;
//        protected Map<String, String> mHeader;
//        protected long mTimeOut;
//        protected Object mTag;
//        protected AbsRequestCallBack mCallBack;
//        protected int mReqType;
//        protected Map<String, File> mUploadFiles;
//        private File mDownFile;
//        private int mCacheTime;
//        private boolean mIsForceRefresh = false;
//
//        public Builder() {
//        }
//
//        /**
//         * request 处理
//         *
//         * @param requestHandler
//         * @return
//         */
//        public Builder reqHandler(RequestHandler requestHandler) {
//            this.requestHandler = requestHandler;
//            return this;
//        }
//
//        /**
//         * reponse 处理
//         *
//         * @param responseHandler
//         * @return
//         */
//        public Builder respHandler(ResponseHandler responseHandler) {
//            this.responseHandler = responseHandler;
//            return this;
//        }
//
//        public Builder jsonBody(String jsonBody) {
//            this.jsonBody = jsonBody;
//            return this;
//        }
//
//        @Override
//        public NetRequest build() {
//
//            // ==== 1. 根据level，获取对请求参数进行预处理的Handler
//            final RequestHandler paramHandler = this.requestHandler != null ? this.requestHandler : RequestHandlerFactory.create(mUrl, mCallBack, level);
//            if (paramHandler == null) {
//                return null;
//            }
//
//            // ==== 2.根据requestHandler设置信息 =====
//            paramHandler.handleHeader(mHeader); // 设置头部信息
//            // ==== 3.判断请求是否jsonObject
//            handleJsonBody();       // 判断是否json body体
//            // ==== 4.请求参数处理
//            try {
//                paramHandler.handleParams(mParams);
//            } catch (Exception e) {
//                if (mCallBack != null) {
//                    mCallBack.onFailure(e);         // 直接执行错误回调
//                }
//            }
//
//            // ==== 5. 根据level，获取对应请求的响应处理器
//            ResponseHandler responseHandler = this.responseHandler != null ? this.responseHandler : ResponseHandlerFactory.create(mUrl, level);
//            responseHandler.setOriginalCallback(mCallBack);
//
//            return new NetRequest(this);
//        }
//
//        private void handleJsonBody() {
//            if (jsonBody != null) {  //对于json串，应该是不需要添加参数信息的
//                final String json = jsonBody;
//                requestBody(new BaseRequestBody() {
//                    @Override
//                    public String getBodyContentType() {
//                        return "application/json";
//                    }
//
//                    @Override
//                    public byte[] getBody() {
//                        return json.getBytes();
//                    }
//                });
//            }
//        }
//    }
//}
