# basenet简介
- 封装基本的网络请求，采用链式编程方案，底层实现使用的 Volley 和 Okhttp3.0；
- 上传下载文件，可使用Okhttp, volley 不支持大文件的上传下载；
- 暂没有考虑缓存实现；
- 暂没有考虑https与重定向；

# 更新日志 -- 2017-03-26
- 新增tag标签 v0.0.1;
- 新增网络回调到主线程；
- response类新增 http 状态码字段；
- 去掉全部volley代码（可从 v0.0.1 tag中查看）


# ~~使用volley请求网络（废弃）~~
```java
 new VolleyRequest.Builder(getApplication()).url("https://www.github.com")
				.type(AbsRequest.RequestType.GET)
				.callback(new AbsRequestCallBack<String>() {
					@Override
					public void onSuccess(final Response<String> response) {
						super.onSuccess(response);
						showHeader(response);
						showBody(response);
					}

					@Override
					public void onFailure(final Throwable e) {
						super.onFailure(e);
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
							}
						});
					}
				}).build().request();
```

# 使用okhttp请求网络
```java
new OkHttpRequest.Builder().url("https://www.github.com")
				.type(AbsRequest.RequestType.GET)
				.callback(new AbsRequestCallBack<String>() {
					@Override
					public void onSuccess(final Response<String> response) {
						super.onSuccess(response);
						showHeader(response);
						showBody(response);
					}

					@Override
					public void onFailure(final Throwable e) {
						super.onFailure(e);
								Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
						});
					}
				}).build().request();
	}
```

 ## okhttp下载文件
 ```java
 final String absolutePath = Environment.getExternalStorageDirectory().getAbsolutePath();
		File file = new File(absolutePath + "/" + "headfirst.pdf");

		new OkHttpRequest.Builder().url(et_down_url.getText().toString())
				.downFile(file)
				.callback(new AbsRequestCallBack() {
					@Override
					public void onSuccess(Response response) {
						super.onSuccess(response);
					}

					@Override
					public void onFailure(final Throwable e) {
						super.onFailure(e);
						error.setText(e.toString());
					}

					@Override
					public void onProgressUpdate(final long contentLength, final long bytesRead, final boolean done) {
						super.onProgressUpdate(contentLength, bytesRead, done);
						progress.setProgress((int) (bytesRead * 1.0f / contentLength * 100));
						progressTV.setText(contentLength + "/" + bytesRead);
					}
				})
				.build().request();
 ```
 
  ## okhttp上传文件
  ```java
  Map<String, File> uploads = new HashMap<>();
		final String absolutePath = Environment.getExternalStorageDirectory().getAbsolutePath();
		File file = new File(absolutePath + "/" + "11.txt");

		uploads.put("file", file);

		new OkHttpRequest.Builder().url(et_down_url.getText().toString())
				.uploadFiles(uploads)
				.callback(new AbsRequestCallBack() {
					@Override
					public void onSuccess(Response response) {
						super.onSuccess(response);
					}

					@Override
					public void onFailure(final Throwable e) {
						super.onFailure(e);
						error.setText(e.toString());
					}

					@Override
					public void onProgressUpdate(final long contentLength, final long bytesRead, final boolean done) {
						super.onProgressUpdate(contentLength, bytesRead, done);
						progress.setProgress((int) (bytesRead * 1.0f / contentLength * 100));
						progressTV.setText(contentLength + "/" + bytesRead);
					}
				})
				.build().request();
  ```

  # gradle构建依赖:
  	compile 'com.github.lib:basenet:0.0.1'

  # 其他(一些实例请参考 app 的例子代码)
 
