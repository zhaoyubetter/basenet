package lib.basenet.response;

import java.util.Map;

import lib.basenet.request.AbsRequest;

/**
 * 响应体封装
 * Created by zhaoyu on 2017/3/16.
 */
public class Response<T> {

	/**
	 * 此次的请求
	 */
	public AbsRequest request;

	/**
	 * 响应header
	 */
	public Map<String, String> responseHeader;

	/**
	 * 响应体
	 */
	public T responseBody;

	/**
	 * 状态码
	 */
	public int statusCode;

	public Response(AbsRequest request, Map<String, String> headerMap, T body) {
		this.request = request;
		this.responseHeader = headerMap;
		this.responseBody = body;
	}

	/**
	 * 获取某个header
	 *
	 * @param name
	 * @return
	 */
	public String header(String name) {
		String result = null;
		if (responseHeader != null) {
			result = responseHeader.get(name);
		}
		return result != null ? result : null;
	}
}
