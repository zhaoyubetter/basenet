# basenet简介
- 封装基本的网络请求，采用链式编程方案，底层实现使用的 Volley 和 Okhttp3.0；
- 上传下载文件，可使用Okhttp, volley 不支持大文件的上传下载；
- ~~暂没有考虑缓存实现；~~
- 支持 缓存（GET 与 POST, 由客户端，酌情去配置缓存时间）;
- https与gzip压缩有待测试（现可配置ssl）；

# 更新日志 -- 2017-03-26
- 新增tag标签 v0.0.1;
- 新增网络回调到主线程；
- response类新增 http 状态码字段；
- 去掉全部volley代码（可从 v0.0.1 tag中查看）

--- 2017-04-27
- 新增GET缓存策略；
- 支持POST缓存；
- 添加强制刷新功能；
- 添加全局配置类

--- 2017-05-22
- 新增 NetUtils 工具类，全局配置移入此；
- 新增同步请求功能，与示例代码；
- tag v0.0.4;

--- 2017-07-28
- 添加httpsUtils(拿了鸿洋的)；支持 https配置；
- 拦截器开放，外界可指定拦截器；
- tag 0.0.5

--- 2017-08-18
- 修正使用post请求，无文件上传时，basenet 使用 Multipartbody 问题；
- 由于没有添加 gson 支持，response的只有String泛型，其他泛型配置将失败，`大bug`

--- 2017-12-11
- 新增 `BaseRequestBody类`，支持直接post（但不能用于大文件）；
- 新增 自动识别 文件 mimetype功能；
- tag 0.0.7

--- 2017-01-10
- 新增 `onCancel` 方法，默认不实现；
- tag 0.0.8
--- 2018-03-01
- 新增 `断点下载`支持，用新类实现`DownloadFileManager`
--- 2018-03-05
- 修复断点下载问题；

--- 2018-04-04
- 新增`断点上传`，用新类实现 `UploadFileManager`

--- 2018-04-09
- 修正同步网络请求时，没有设置header问题；
- tag  1.0.1`

# 配置拦截器
```java
// 全局配置NetConfig
NetUtils.init(
	new NetUtils.Builder().cacheDir(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/basenet")
	.debug(true).enablePostCache(true).timeout(10).app(this)
		.addNetInterceptor(new StethoInterceptor()));
```

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

  ## NetConfig全局配置
  ```java
  // 全局配置NetConfig，设置缓存路径、允许使用post缓存等；
  NetUtils.init(new NetConfig.Builder().cacheDir(Environment.getExternalStorageDirectory().getAbsolutePath() + "/basenet")
  		.debug(true).enablePostCache(true).timeout(10).app(getApplication())
  );
  ```
  ## 缓存使用(客户端根据接口酌情配置)
  - POST缓存与此类似，只是将类型改为 POST即可；
  - 如果不需要缓存，不配置 cacheTime即可；
  - 如果想要强制刷新，配置builder.forceRefresh(true), 即可从网络加载，强制加载完毕后，将刷新本地缓存记录；
  - 注意：post 缓存时，因 唯一key，不好定位，只支持 请求参数为 键值对（key-value）form 表单形式，对于上传文件，下载文件形式的post，请求，一律无效；
  ```java
  // 缓存10s
  new OkHttpRequest.Builder().url("https://www.jd.com").cacheTime(10).type(AbsRequest.RequestType.GET).body(params).
  				.callback(new AbsRequestCallBack<String>() {
  					@Override
  					public void onSuccess(Response<String> response) {
  						super.onSuccess(response);
  						showHeader(response);
  						message.setText(response.responseBody);
  						if (response.isFromCache) {
  							//cache_info.setText("来自缓存");
  						} else {
  							//cache_info.setText("来自 -- 》 网络");
  						}
  					}

  					@Override
  					public void onFailure(Throwable e) {
  						super.onFailure(e);
  						//message.setText(e.toString());

  					}
  				}).build().request();
  ```

  # post 缓存，实现原理：
  - 利用okhttp cache.java 类，为参考，在存入 缓存时，去掉只支持GET形式，唯一key的生成如下
  ```java
	public static String key(Request request) {
    		String cUrl = request.url().toString();
    		if (request.body() != null) {
    			Buffer buffer = new Buffer();
    			try {
    				// 避免post重复，这里采用value来凭借，因key不好获取
    				// 如果有上传下载文件，此处为 ProgressRequestBody
    				if (request.body() instanceof MultipartBody) {
    					final List<MultipartBody.Part> parts = ((MultipartBody) request.body()).parts();
    					/**
    					 * 接受字符串格式的参数，其他忽略
    					 * @see lib.basenet.okhttp.OkHttpRequest#getRequestBody mParams
    					 */
    					for (MultipartBody.Part p : parts) {
    						if (null == p.body().contentType()) {
    							p.body().writeTo(buffer);
    						}
    					}
    				}
    				String params = buffer.readString(Charset.forName("UTF-8")); //获取请求参数
    				cUrl += params;
    			} catch (IOException e) {
    				e.printStackTrace();
    			} finally {
    				Util.closeQuietly(buffer);
    			}

    		}
    		return ByteString.encodeUtf8(cUrl).md5().hex();
    	}
  ```
  - 利用okhttp，应用层拦截器，如果是post时，询问缓存，有则取出，并终止执行其他拦截器，
  具体请参考：PostCacheInterceptor.java类；
  
   # 断点文件上传原理：
     断点上传，实际上不是真正的断点上传，而是将文件分块，分块上传；
     断点上传，需要服务器的支持（需要跟服务端协商好，接口与对应的接口字段）;
     断点上传，采用单线程的上传方式，一片接一片进行上传（完美）
      
     步骤：
     1. 初次上传时，将本地待上传的文件（文件名，大小，其md5值），拼接待上传文件信息json，格式为： {"fileName":"", "fileSize": , "md5": ""}
             传给服务器端，然后服务端会生成一个 id，客户端获取后，并设置当前上传的文件id，用来标识这个上传的文件；
           参考：{@link UploadDemoCode#obtainFileCode(FileSegmentInfo, String)}
     2. 将大文件，切割成多个分片，并将这些分片形成临时文件并，顺序上传这些分片，上传参数为：
          - 当前上传的分片文件： files;
          - 服务端返回的文件Id： fileId;
          - 开始位置          ： fileStartRange;
          - 结束位置          ： fileEndRange
     3. 如果上传中途失败，客户端会缓存上次成功的切片位置；
     4. 再次上传时，将从切片位置上传；


  # gradle构建依赖:
  	compile 'com.github.lib:basenet:1.0.0'

  # 其他(一些实例请参考 app 的例子代码)
 
