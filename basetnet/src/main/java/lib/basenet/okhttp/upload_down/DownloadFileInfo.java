package lib.basenet.okhttp.upload_down;

import java.io.Serializable;

/**
 * 断点下载文件信息
 */
public final class DownloadFileInfo implements Serializable {
    //状态值
    //下载成功：完成
    public static final int SUCCESS = 4;
    //下载失败：网络错误
    public static final int FAILURE = 1;
    //下载中：下载中 此状态不缓存
    public static final int DOWNLOADING = 2;
    //暂停：暂停下载
    public static final int DOWNLOAD_PAUSE = 3;
    //下载未开始：触发下载任务，但是还未发起网络请求
    public static final int NOT_BEGIN = 0;

    //下载状态
    protected int status;
    //文件URL地址
    public String fileUrl;
    //文件本地存储地址
    public String localFilePath;
    //当前下载完成量
    public long currentFinished;
    //文件总大小
    public long fileSize;

    public DownloadFileInfo(String fileUrl, String localFilePath) {
        if (fileUrl == null || localFilePath == null) {
            throw new NullPointerException("DownloadFileInfo's param must not be null");
        }
        this.fileUrl = fileUrl;
        this.localFilePath = localFilePath;
        this.status = NOT_BEGIN;
    }

    protected void reset() {
        currentFinished = 0;
        this.status = NOT_BEGIN;
    }
}
