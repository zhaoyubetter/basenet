package lib.basenet.volley;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Created by zhaoyu on 2017/3/11.
 */

class PostUploadRequest extends Request<String> {

	private Response.Listener mListener;

	private MultipartBody mMultiPartEntity = new MultipartBody();

	@Override
	protected Response<String> parseNetworkResponse(NetworkResponse response) {
		String parsed = "";
		try {
			parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
		} catch (UnsupportedEncodingException e) {
			parsed = new String(response.data);
		}
		return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));

	}

	@Override
	protected void deliverResponse(String response) {
		if (mListener != null) {
			mListener.onResponse(response);
		}
	}

	/**
	 * 获取要上传的文件
	 * @return
	 */
	public Map<String, File> getUploadFiles() {
		return null;
	}

	public PostUploadRequest(int method, String url, Response.ErrorListener listener, Response.Listener mListener) {
		super(method, url, listener);
		this.mListener = mListener;
		// 超时设置 10 分钟
		setRetryPolicy(new DefaultRetryPolicy(10 * 60 * 1000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
		setShouldCache(false);
	}


	/**
	 * 请求体
	 *
	 * @return
	 * @throws AuthFailureError
	 */
	@Override
	public byte[] getBody() throws AuthFailureError {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			// 参数部分
			Map<String, String> params = getParams();
			if (params != null && params.size() > 0) {
				for (Map.Entry<String, String> entry : params.entrySet()) {
					mMultiPartEntity.addStringPart(entry.getKey(), entry.getValue());
				}
			}

//			// 文件部分
			final Map<String, File> uploadFiles = getUploadFiles();
			if (uploadFiles != null && uploadFiles.size() > 0) {
				for (Map.Entry<String, File> entry : uploadFiles.entrySet()) {
					mMultiPartEntity.addFilePart(entry.getKey(), entry.getValue());
				}
			}

			// multipart body
			mMultiPartEntity.writeTo(bos);
		} catch (IOException e) {
			throw new AuthFailureError(e.getMessage());
		}
		return bos.toByteArray();
	}

	@Override
	public String getBodyContentType() {
		return mMultiPartEntity.getContentType();
	}
}
