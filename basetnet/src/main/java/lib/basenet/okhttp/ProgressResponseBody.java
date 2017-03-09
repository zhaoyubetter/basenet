package lib.basenet.okhttp;


import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * 监听文件下载进度的 ResponseBody
 * https://github.com/square/okhttp/blob/master/samples/guide/src/main/java/com/squareup/okhttp/recipes/Progress.java
 */
public class ProgressResponseBody extends ResponseBody {
    private final ResponseBody responseBody;
    private final ProgressCallback progressListener;
    private BufferedSource bufferedSource;
    private long totalLength;

    public ProgressResponseBody(ResponseBody responseBody, ProgressCallback progressListener) {
        this.responseBody = responseBody;
        this.progressListener = progressListener;
    }

    @Override
    public MediaType contentType() {
        return responseBody.contentType();
    }

    @Override
    public long contentLength() {
        totalLength = responseBody.contentLength();
        return totalLength;
    }

    @Override
    public BufferedSource source() {
        if (bufferedSource == null) {
            bufferedSource = Okio.buffer(source(responseBody.source()));
        }
        return bufferedSource;
    }

    private Source source(Source source) {
        return new ForwardingSource(source) {
            long totalBytesRead = 0L;

            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long bytesRead = super.read(sink, byteCount);
                // read() returns the number of bytes read, or -1 if this source is exhausted.
                totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                progressListener.update(responseBody.contentLength(), totalBytesRead, bytesRead == -1);
                return bytesRead;
            }
        };
    }
}
