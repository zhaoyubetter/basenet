//package lib.basenet.volley;
//
//
//import android.content.Context;
//
//import com.android.volley.AuthFailureError;
//import com.android.volley.DefaultRetryPolicy;
//import com.android.volley.NetworkResponse;
//import com.android.volley.Request;
//import com.android.volley.RequestQueue;
//import com.android.volley.Response;
//import com.android.volley.VolleyError;
//import com.android.volley.toolbox.StringRequest;
//import com.android.volley.toolbox.Volley;
//
//import java.io.File;
//import java.util.Map;
//
//import lib.basenet.request.AbsRequest;
//import lib.basenet.utils.FileUtils;
//
//
///**
// * Volley请求封装类
// *
// * @author zhaoyu
// * @author hufeng
// * @version 1.0
// * @since 2017/3/6.
// */
//public class VolleyRequest extends AbsRequest {
//
//	private static RequestQueue requestQueue;
//	private Request mRequest;
//
//	private VolleyRequest(Builder builder) {
//		super(builder);
//		if (requestQueue == null) {
//			synchronized (VolleyRequest.class) {
//				if (requestQueue == null) {
//					requestQueue = Volley.newRequestQueue(builder.mCtx);
//				}
//			}
//		}
//	}
//
//	@Override
//	protected void get() {
//		realRequest(Request.Method.GET);
//	}
//
//	@Override
//	protected void post() {
//		realRequest(Request.Method.POST);
//	}
//
//	private void realRequest(final int reqType) {
//		int tReqType = Request.Method.GET;
//		String tUrl = mUrl;
//		switch (reqType) {
//			case Request.Method.GET:
//				tReqType = Request.Method.GET;
//				tUrl = generateUrl(mUrl, mParams);
//				break;
//			case Request.Method.POST:
//				tReqType = Request.Method.POST;
//				break;
//		}
//
//		// 如果是上传文件
//		if (mUploadFiles != null && mUploadFiles.size() > 0) {
//			upload();
//			return;
//		}
//
//		// 如果是下载文件
//		if (mDownFile != null) {
//			down();
//		}
//
//		mRequest = new StringRequest(tReqType, tUrl, null, new Response.ErrorListener() {
//			@Override
//			public void onErrorResponse(VolleyError error) {
//				if (mCallBack != null) {
//					mCallBack.onFailure(error);
//				}
//			}
//		}) {
//			@Override
//			public Map<String, String> handleHeader() throws AuthFailureError {
//				Map<String, String> superHeader = super.handleHeader();
//				if (mHeader != null && mHeader.size() > 0) {
//					superHeader = mHeader;
//				}
//				return superHeader;
//			}
//
//			// 设置Body参数
//			@Override
//			protected Map<String, String> getParams() throws AuthFailureError {
//				Map<String, String> tParams = super.getParams();
//				if (mParams != null && mParams.size() > 0 && reqType == Request.Method.POST) {
//					tParams = mParams;
//				}
//				return tParams;
//			}
//
//			@Override
//			protected Response<String> parseNetworkResponse(NetworkResponse response) {
//				Response<String> stringResponse = super.parseNetworkResponse(response);
//				if (mCallBack != null) {
//					lib.basenet.response.Response myResponse = new lib.basenet.response.Response(VolleyRequest.this, response.headers, stringResponse.result);
//					myResponse.statusCode = response.statusCode;
//					mCallBack.onSuccess(myResponse);
//				}
//				return stringResponse;
//			}
//		};
//
//		// 设置此次请求超时时间
//		if (mTimeOut >= 1000) {
//			mRequest.setRetryPolicy(new DefaultRetryPolicy((int) mTimeOut, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//		} else {
//			mRequest.setRetryPolicy(new DefaultRetryPolicy((int) DEFAULT_TIME_OUT, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
//		}
//		mRequest.setTag(mTag);
//		requestQueue.add(mRequest);
//	}
//
//	/**
//	 * 下载文件
//	 */
//	private void down() {
//		mRequest = new DownRequest(Request.Method.GET, mUrl, new Response.ErrorListener() {
//			@Override
//			public void onErrorResponse(VolleyError error) {
//				if (mCallBack != null) {
//					mCallBack.onFailure(error);
//				}
//			}
//		}, null) {
//			@Override
//			public Map<String, String> handleHeader() throws AuthFailureError {
//				Map<String, String> superHeader = super.handleHeader();
//				if (mHeader != null && mHeader.size() > 0) {
//					superHeader = mHeader;
//				}
//				return superHeader;
//			}
//
//			// 设置Body参数
//			@Override
//			protected Map<String, String> getParams() throws AuthFailureError {
//				Map<String, String> tParams = super.getParams();
//				if (mParams != null && mParams.size() > 0) {
//					tParams = mParams;
//				}
//				return tParams;
//			}
//
//			@Override
//			protected Response<byte[]> parseNetworkResponse(NetworkResponse response) {
//				Response<byte[]> resultResponse = super.parseNetworkResponse(response);
//				try {
//					FileUtils.saveFile(resultResponse.result, mDownFile);
//					if (null != mCallBack) {
//						mCallBack.onSuccess(new lib.basenet.response.Response(VolleyRequest.this, response.headers, resultResponse.result));
//
//					}
//				} catch (Exception e) {
//					if (mCallBack != null) {
//						mCallBack.onFailure(e);
//					}
//				}
//				return resultResponse;
//			}
//		};
//
//		mRequest.setTag(mTag);
//		requestQueue.add(mRequest);
//	}
//
//	/**
//	 * 文件上传
//	 */
//	private void upload() {
//		mRequest = new PostUploadRequest(Request.Method.POST, mUrl, new Response.ErrorListener() {
//			@Override
//			public void onErrorResponse(VolleyError error) {
//				if (mCallBack != null) {
//					mCallBack.onFailure(error);
//				}
//			}
//		}, null) {
//			@Override
//			public Map<String, String> handleHeader() throws AuthFailureError {
//				Map<String, String> superHeader = super.handleHeader();
//				if (mHeader != null && mHeader.size() > 0) {
//					superHeader = mHeader;
//				}
//				return superHeader;
//			}
//
//			// 设置Body参数
//			@Override
//			protected Map<String, String> getParams() throws AuthFailureError {
//				Map<String, String> tParams = super.getParams();
//				if (mParams != null && mParams.size() > 0) {
//					tParams = mParams;
//				}
//				return tParams;
//			}
//
//			@Override
//			public Map<String, File> getUploadFiles() {
//				return mUploadFiles;
//			}
//
//			@Override
//			protected Response<String> parseNetworkResponse(NetworkResponse response) {
//				Response<String> stringResponse = super.parseNetworkResponse(response);
//				if (null != mCallBack) {
//					mCallBack.onSuccess(new lib.basenet.response.Response(VolleyRequest.this, response.headers, stringResponse.result));
//
//				}
//				return stringResponse;
//			}
//		};
//
//		mRequest.setTag(mTag);
//		requestQueue.add(mRequest);
//	}
//
//	@Override
//	public void cancel() {
//		if (mRequest != null) {
//			mRequest.cancel();
//		} else if (mTag != null) {
//			requestQueue.cancelAll(mTag);
//		}
//	}
//
//
//	public static class Builder extends AbsRequest.Builder {
//
//		private Context mCtx;
//
//		public Builder(Context ctx) {
//			this.mCtx = ctx;
//		}
//
//		@Override
//		public AbsRequest build() {
//			return new VolleyRequest(this);
//		}
//	}
//}
