package lib.basenet.okhttp;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;

import lib.basenet.NetUtils;
import lib.basenet.request.AbsDownloadRequestCallback;
import lib.basenet.utils.FileUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by liyu20 on 2018/2/27.
 */
public final class DownloadFileManager {
    private final OkHttpClient okHttpClient;
    private final AbsDownloadRequestCallback downloadListener;
    private final DownloadFileInfo downloadFileInfo;

    public DownloadFileManager(DownloadFileInfo fileInfo, AbsDownloadRequestCallback downloadListener) {
        if(fileInfo == null || fileInfo.fileUrl == null || fileInfo.localFilePath == null) {
            throw new RuntimeException("fileInfo or fileUrl or localFilePath  cannot be a null");
        }
        this.okHttpClient = NetUtils.getInstance().getOkHttpClient();
        this.downloadListener = downloadListener;
        this.downloadFileInfo = fileInfo;
    }

    public DownloadFileManager(String downUrl, String localFileFullPath, AbsDownloadRequestCallback downloadListener) {
        this(new DownloadFileInfo(downUrl, localFileFullPath), downloadListener);
    }

    public int getStatus() {
        return downloadFileInfo.status;
    }

    /**
     * 移除下载任务
     */
    public void deleteDownload() {
        NetUtils.getInstance().cancel(this.toString()); // 移除任務
        DownFileUtil.remove(downloadFileInfo);
        downloadFileInfo.reset();   // 清0
        FileUtils.deleteFile(downloadFileInfo.localFilePath);
    }

    /**
     * 停止下载任务
     *
     */
    public void stopDownload() {
        downloadFileInfo.status = DownloadFileInfo.DOWNLOAD_PAUSE;
        DownFileUtil.addOrUpdate(downloadFileInfo);
    }

    /**
     * 开始下载
     */
    public void startDownload() {
        DownloadFileInfo fileInfo = downloadFileInfo;
        // 0. 当前正在下载，return
        if (downloadFileInfo.status == DownloadFileInfo.DOWNLOADING) {
            return;
        }
        // 1. 是否有断点信息
        final DownloadFileInfo cacheFileInfo = DownFileUtil.getCacheFileInfo(fileInfo);

        if (cacheFileInfo != null) {  // 賦值進度
            fileInfo.currentFinished = cacheFileInfo.currentFinished;
            fileInfo.fileSize = cacheFileInfo.fileSize;
        }
        DownFileUtil.addOrUpdate(fileInfo);
        downloadFile(fileInfo);
    }

    private void downloadFile(final DownloadFileInfo fileInfo) {
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
                    builder.addHeader("Range", "bytes=" + fileInfo.currentFinished + "-" + fileInfo.fileSize);
                }
                break;      // break
            case DownloadFileInfo.NOT_BEGIN:
                if (file.exists()) {
                    file.delete();
                }
                break;      // break
        }
        Request request = builder.url(fileInfo.fileUrl).build();
        downloadRequest(request, fileInfo, file);
    }

    private void downloadRequest(Request request, final DownloadFileInfo fileInfo, final File file) {
        if (downloadListener != null) {
            downloadListener.onStart();
        }

        //发起请求
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                fileInfo.status = DownloadFileInfo.FAILURE;
                DownFileUtil.addOrUpdate(fileInfo);
                if (downloadListener != null) {
                    downloadListener.onFailure(e);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    //下载失败
                    fileInfo.status = DownloadFileInfo.FAILURE;
                    if (downloadListener != null) {
                        downloadListener.onFailure(new Exception(response.code() + " " + response.message()));
                    }
                    return;
                }

                InputStream is;
                byte[] buffer = new byte[4096];
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
                    randomFile.seek(fileInfo.currentFinished);
                    //http 206部分下载，感觉不需要
                    while ((len = is.read(buffer)) != -1) {
                        //写入文件`
                        randomFile.write(buffer, 0, len);
                        fileInfo.currentFinished += len;
                        if (downloadListener != null) {
                            downloadListener.onProgressUpdate(fileInfo.fileSize, fileInfo.currentFinished, fileInfo.fileSize == fileInfo.currentFinished);
                        }
                        //停止下载
                        if (fileInfo.status != DownloadFileInfo.DOWNLOADING) {
                            randomFile.close();
                            if (downloadListener != null) {
                                downloadListener.onStop(fileInfo.fileSize, fileInfo.currentFinished, fileInfo);
                            }
                            return;
                        }
                    }

                    //下载完成
                    randomFile.close();
                    fileInfo.status = DownloadFileInfo.SUCCESS;
                    if (downloadListener != null) {
                        final lib.basenet.response.Response myResponse = new lib.basenet.response.Response(null, new HashMap<String, String>(), fileInfo.localFilePath);
                        myResponse.statusCode = response.code();
                        downloadListener.onSuccess(myResponse);
                    }
                } catch (Exception e) {
                    DownFileUtil.addOrUpdate(fileInfo); // 更新信息
                    e.printStackTrace();
                    if (downloadListener != null) {
                        downloadListener.onFailure(new Exception(response.code() + " " + response.message()));
                    }
                }
            }
        });
    }
}
