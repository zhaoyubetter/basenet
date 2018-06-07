package lib.basenet.okhttp.upload_down;



import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

import lib.basenet.NetUtils;
import lib.basenet.request.AbsDownloadRequestCallback;
import lib.basenet.utils.FileUtils;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 注意：
 * okhttp3.6版本 通过get下载时，请设置缓存级别为：builder.cacheControl(new CacheControl.Builder().noCache().noStore().build());
 * 否则断点下载失效
 * Created by liyu20 on 2018/2/27.
 */
public final class DownloadFileManager {
    private final OkHttpClient okHttpClient;
    private final AbsDownloadRequestCallback downloadListener;
    private final DownloadFileInfo downloadFileInfo;
    /**
     * 控制下载状态
     */
    private volatile int currentStatus = DownloadFileInfo.NOT_BEGIN;

    public DownloadFileManager(DownloadFileInfo fileInfo, AbsDownloadRequestCallback downloadListener) {
        if (fileInfo == null || fileInfo.fileUrl == null || fileInfo.localFilePath == null) {
            throw new RuntimeException("fileInfo or fileUrl or localFilePath  cannot be a null");
        }
        this.okHttpClient = NetUtils.getInstance().getOkHttpClient();
        this.downloadListener = downloadListener;
        this.downloadFileInfo = fileInfo;

        // 是否有信息
        DownloadFileInfo cache = DownFileUtil.getCacheFileInfo(fileInfo);
        // 重新赋值status
        if (cache != null) {
            downloadFileInfo.status = cache.status;
            currentStatus = (cache.status == DownloadFileInfo.DOWNLOADING ? DownloadFileInfo.NOT_BEGIN : cache.status);
        }
    }

    public DownloadFileManager(String downUrl, String localFileFullPath, AbsDownloadRequestCallback downloadListener) {
        this(new DownloadFileInfo(downUrl, localFileFullPath), downloadListener);
    }

    public synchronized int getStatus() {
        return currentStatus;
    }

    /**
     * 移除下载任务
     */
    public synchronized void deleteDownload() {
        NetUtils.getInstance().cancel(this.toString()); // 移除任務
        DownFileUtil.remove(downloadFileInfo);
        downloadFileInfo.reset();   // 清0
        FileUtils.deleteFile(downloadFileInfo.localFilePath);
        currentStatus = DownloadFileInfo.NOT_BEGIN;
    }

    /**
     * 停止下载任务
     */
    public synchronized void stopDownload() {
        NetUtils.getInstance().cancel(this.toString()); // 移除任務
        // 完成时，不操作
        if (downloadFileInfo.status != DownloadFileInfo.SUCCESS) {
            downloadFileInfo.status = DownloadFileInfo.DOWNLOAD_PAUSE;
            DownFileUtil.addOrUpdate(downloadFileInfo);
            currentStatus = DownloadFileInfo.DOWNLOAD_PAUSE;
        }
    }

    public  void startDownload() {
        startDownload(false);
    }

    /**
     * 开始下载
     *
     * @param forceDown 强制重新下载
     */
    public synchronized void startDownload(boolean forceDown) {
        // 0. 当前正在下载，return
        if (currentStatus == DownloadFileInfo.DOWNLOADING) {
            return;
        }

        currentStatus = DownloadFileInfo.DOWNLOADING;

        // 如果是强制下载
        if(forceDown) {
            DownFileUtil.remove(downloadFileInfo);
            downloadFileInfo.status = DownloadFileInfo.NOT_BEGIN;
        } else {
            // 1. 是否有断点信息
            final DownloadFileInfo cacheFileInfo = DownFileUtil.getCacheFileInfo(downloadFileInfo);
            if (cacheFileInfo != null) {  // 賦值進度
                downloadFileInfo.currentFinished = cacheFileInfo.currentFinished;
                downloadFileInfo.fileSize = cacheFileInfo.fileSize;
                downloadFileInfo.status = cacheFileInfo.status;

                // 如果已完成，并且是非强制下载下，并且文件存在，直接返回成功
                if (cacheFileInfo.status == DownloadFileInfo.SUCCESS) {
                    try {
                        if (new File(cacheFileInfo.localFilePath).exists()) {
                            final lib.basenet.response.Response myResponse = new lib.basenet.response.Response(null, new HashMap<>(), cacheFileInfo.localFilePath);
                            myResponse.statusCode = 200;
                            currentStatus = DownloadFileInfo.SUCCESS;
                            downloadListener.onStart();
                            downloadListener.onSuccess(myResponse);
                            return;
                        } else {
                            downloadFileInfo.status = DownloadFileInfo.FAILURE; // 重新下载
                        }
                    } catch (Exception e) {
                        downloadFileInfo.status = DownloadFileInfo.DOWNLOAD_PAUSE;
                        DownFileUtil.remove(downloadFileInfo);
                    }
                }
            }
        }

        DownFileUtil.addOrUpdate(downloadFileInfo);
        downloadFile(downloadFileInfo);
    }

    private synchronized void downloadFile(final DownloadFileInfo fileInfo) {
        //已有下载信息，则添加Header，若无则正常请求
        Request.Builder builder = new Request.Builder();
        builder.tag(this.toString());
        final File file = new File(fileInfo.localFilePath);
        // 校验文件
        switch (fileInfo.status) {
            case DownloadFileInfo.SUCCESS:       //已下载完成
                return;
            case DownloadFileInfo.DOWNLOADING:   //正在下载
                return;
            case DownloadFileInfo.FAILURE:
            case DownloadFileInfo.DOWNLOAD_PAUSE:
                if (file.exists()) {    // 断点下载
                    builder.addHeader("Range", "bytes=" + fileInfo.currentFinished + "-");
                }
                break;      // break
            case DownloadFileInfo.NOT_BEGIN:
                if (file.exists()) {
                    file.delete();
                    fileInfo.reset();    // 复位
                }
                break;      // break
        }
        // 下载，必须要加cacheControl控制
        builder.cacheControl(new CacheControl.Builder().noCache().noStore().build());
        Request request = builder.url(fileInfo.fileUrl).build();
        downloadRequest(request, fileInfo, file);
    }

    private synchronized void downloadRequest(final Request request, final DownloadFileInfo fileInfo, final File file) {
        if (downloadListener != null) {
            downloadListener.onStart();
        }

        //发起请求
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                fileInfo.status = DownloadFileInfo.FAILURE;
                currentStatus = DownloadFileInfo.FAILURE;
                DownFileUtil.addOrUpdate(fileInfo);
                if (downloadListener != null) {
                    if (downloadListener != null && call.isCanceled()) {
                        downloadListener.onCancel();
                    } else {
                        downloadListener.onFailure(e);
                    }
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    //下载失败
                    fileInfo.status = DownloadFileInfo.FAILURE;
                    currentStatus = DownloadFileInfo.FAILURE;
                    if (downloadListener != null) {
                        lib.basenet.response.Response<Object> response1 = new lib.basenet.response.Response(
                                null, getResponseHeaders(response), response.body()
                        );
                        response1.statusCode = response.code();
                        response1.message = response.message();
                        downloadListener.onResponse(response1);
                    }
                    return;
                }

                // 影响Header
                Map<String, String> respHeader = getResponseHeaders(response);

                InputStream is;
                byte[] buffer = new byte[8192];
                int len;
                try {
                    is = response.body().byteStream();
                    long total = response.body().contentLength();
                    //不设置断点下载时，文件大小才是真实大小
                    if (fileInfo.status == DownloadFileInfo.NOT_BEGIN) {
                        fileInfo.fileSize = total;
                    }
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    //设置状态值
                    fileInfo.status = DownloadFileInfo.DOWNLOADING;
                    RandomAccessFile randomFile = new RandomAccessFile(file, "rwd");
                    // 断点下载，服务端返回206
                    if (response.code() != 206) {
                        fileInfo.currentFinished = 0;    // 重新下载
                    }
                    randomFile.seek(fileInfo.currentFinished);
                    while ((len = is.read(buffer)) != -1) {
                        //写入文件`
                        randomFile.write(buffer, 0, len);
                        fileInfo.currentFinished += len;
                        if (downloadListener != null) {
                            downloadListener.onProgressUpdate(fileInfo.fileSize, fileInfo.currentFinished, fileInfo.fileSize == fileInfo.currentFinished);
                        }
                        //Log.e("better1244", "currentFinish: " + fileInfo.currentFinished + "," + Thread.currentThread().toString());
                        //停止下载
                        if (fileInfo.status != DownloadFileInfo.DOWNLOADING) {
                            try {
                                randomFile.close();   // 避免关闭流时，异常造成 断点不正确
                            } catch (IOException e) {
                                if(fileInfo.currentFinished - len > 0) {
                                    fileInfo.currentFinished -= len;
                                }
                            }
                            DownFileUtil.addOrUpdate(fileInfo);        // 更新
                            if (downloadListener != null) {
                                downloadListener.onStop(fileInfo.fileSize, fileInfo.currentFinished, fileInfo);
                            }
                            return;
                        }
                    }

                    //下载完成
                    randomFile.close();
                    fileInfo.status = DownloadFileInfo.SUCCESS;
                    currentStatus = DownloadFileInfo.SUCCESS;
                    DownFileUtil.addOrUpdate(fileInfo);        // 更新
                    if (downloadListener != null) {
                        final lib.basenet.response.Response myResponse =
                                new lib.basenet.response.Response(null, respHeader, fileInfo.localFilePath);
                        myResponse.statusCode = response.code();
                        downloadListener.onSuccess(myResponse);
                    }
                } catch (Exception e) {
                    fileInfo.status = DownloadFileInfo.FAILURE;
                    currentStatus = DownloadFileInfo.FAILURE;
                    DownFileUtil.addOrUpdate(fileInfo); // 更新信息

                    if (downloadListener != null) {
                        if (downloadListener != null && call.isCanceled()) {
                            downloadListener.onCancel();
                        } else {
                            downloadListener.onFailure(e);
                        }
                    }
                }
            }
        });
    }

    /**
     * 封装响应 header
     *
     * @param response
     * @return
     */
    private HashMap getResponseHeaders(Response response) {
        HashMap headerMap = null;
        if (null != response.headers() && response.headers().size() > 0) {
            headerMap = new HashMap<>();
            Headers responseHeaders = response.headers();
            for (int i = 0; i < responseHeaders.size(); i++) {
                headerMap.put(responseHeaders.name(i), responseHeaders.value(i));
            }
        }

        return headerMap;
    }
}
