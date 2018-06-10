package basenet.better.basenet.bizDemo.handler;

import android.content.Context;

/**
 * 响应的错误编码，对应业务来封装
 */
public final class CustomBizErrCode {

    public static final String SUCCESS = "SUCCESS";
    public static final String FAILURE = "FAILURE";
    public static final String NO_AUTHENTICATION = "NO_AUTHENTICATION";
    public static final String INVALID_TIMESTAMP = "INVALID_TIMESTAMP";
    public static final String PARAMETE_EXCEPTION = "PARAMETE_EXCEPTION";
    public static final String INTERFACE_CALL_EXCEPTION = "INTERFACE_CALL_EXCEPTION";
    public static final String NO_PERMISSION = "NO_PERMISSION";
    public static final String INVALID_SESSION = "INVALID_SESSION";

    // 自己加的，用于解析数据
    public static final String PARSER_ERROR = "NET_DISK_PARSER_ERR";
    public static final String SERVER_ERROR = "NET_DISK_SERVER_ERR";
    /**
     * 客户端错误
     */
    public static final String CLIENT_ERROR = "CLIENT_ERR";


    public static String getCodeMsg(Context context, String code) {
        return "code";
    }
}
