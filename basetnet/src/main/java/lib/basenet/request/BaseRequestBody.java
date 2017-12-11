package lib.basenet.request;

/**
 * Created by zhaoyu1 on 2017/12/11.
 */
public interface BaseRequestBody {
    /**
     * content-type
     *
     * @return
     */
    String getBodyContentType();

    /**
     * body内容
     *
     * @return
     */
    byte[] getBody();
}
