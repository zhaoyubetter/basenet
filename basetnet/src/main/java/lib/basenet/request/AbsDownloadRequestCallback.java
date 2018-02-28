package lib.basenet.request;

import lib.basenet.okhttp.DownloadFileInfo;

/**
 * Created by zhaoyu1 on 2018/2/28.
 */
public abstract class AbsDownloadRequestCallback extends AbsRequestCallBack<String> {
    public void onStart(){

    }
    /**
     * 下载 or 上传暂停回调
     */
    public void onStop(DownloadFileInfo fileInfo) {
    }
}
