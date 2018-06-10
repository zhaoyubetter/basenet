package basenet.better.basenet.bizDemo.handler.exception;

/**
 * 业务错误异常
 */
public class CustomBizException extends Exception {

    public String code;

    public CustomBizException() {
        super();
    }

    public CustomBizException(String message) {
        super(message);
    }

    public CustomBizException(String code, String message) {
        super(message);
        this.code = code;
    }
}
