package lib.basenet.request;

import lib.basenet.okhttp.upload_down.DownloadFileInfo;

/**
 * 注意，会掉实在 okhttp 线程中执行的
 * Created by zhaoyu1 on 2018/2/28.
 */
public abstract class AbsDownloadRequestCallback extends AbsRequestCallBack<String> {
    public void onStart(){

    }
    /**
     * 下载 or 上传暂停回调
     */
    public void onStop(long contentLength, long bytesRead, DownloadFileInfo fileInfo) {
    }

}
