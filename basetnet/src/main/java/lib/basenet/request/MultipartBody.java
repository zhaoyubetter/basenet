package lib.basenet.request;


import okhttp3.Headers;

/**
 * 让 volley 支持文件上传
 * Created by zhaoyu1 on 2017/3/10.
 */
public class MultipartBody {
    private final static char[] MULTIPART_CHARS = "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    /**
     * 文本参数和字符集
     */

    private final String TYPE_TEXT_CHARSET = "text/plain; charset=UTF-8";

    public static void createFormData(String name, String value) {

    }

    public static void createFormData(String name, String filename, RequestBody body) {
        if (name == null) {
            throw new NullPointerException("name == null");
        }

        StringBuilder disposition = new StringBuilder("form-data; name=");
        appendQuotedString(disposition, name);

        if (filename != null) {
            disposition.append("; filename=");
            appendQuotedString(disposition, filename);
        }

        return create(Headers.of("Content-Disposition", disposition.toString()), body);
    }

    static StringBuilder appendQuotedString(StringBuilder target, String key) {
        target.append('"');
        for (int i = 0, len = key.length(); i < len; i++) {
            char ch = key.charAt(i);
            switch (ch) {
                case '\n':
                    target.append("%0A");
                    break;
                case '\r':
                    target.append("%0D");
                    break;
                case '"':
                    target.append("%22");
                    break;
                default:
                    target.append(ch);
                    break;
            }
        }
        target.append('"');
        return target;
    }
}
