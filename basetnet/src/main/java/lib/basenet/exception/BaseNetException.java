package lib.basenet.exception;

/**
 * 异常
 */
public class BaseNetException extends Exception {

    public BaseNetException(String message) {
        super(message);
    }

    public BaseNetException(Throwable e) {
        super(e);
    }
}
