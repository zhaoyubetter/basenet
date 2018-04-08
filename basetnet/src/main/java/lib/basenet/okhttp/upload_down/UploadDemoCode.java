package lib.basenet.okhttp.upload_down;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import lib.basenet.NetUtils;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 上传文件示例代码
 * Created by zhaoyu1 on 2018/4/4.
 */
public class UploadDemoCode {

    /**
     * 从服务端获取切片文件信息示例代码
     *
     * @param fileSegmentInfo
     */
    public static void obtainFileCode(final FileSegmentInfo fileSegmentInfo, final String serverUrl) throws IOException, JSONException {
        String json;

        // ==== 1，请求参数拼接，需要跟服务端进行协商
        // 格式可自定义，以下为我们的测试格式
        try {
            JSONObject object = new JSONObject();
            object.put("fileName", fileSegmentInfo.getSrcFileName());
            object.put("fileSize", fileSegmentInfo.getSrcFileSize());
            object.put("md5", fileSegmentInfo.getSrcFileMd5());
            json = object.toString();
        } catch (JSONException e) {
            return;
        }
        RequestBody requestBodyPost = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);
        final Request request = new Request.Builder()
                .url(serverUrl)
                .post(requestBodyPost)
                .build();

        final Response response = NetUtils.getInstance().getOkHttpClient().newCall(request).execute();
        if (response.isSuccessful()) {
            fileSegmentInfo.setServerFileID(new JSONObject(response.body().string()).getString("data")); // 获取serverID
        }
    }
}