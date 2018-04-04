package lib.basenet.okhttp.upload_down;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import lib.basenet.NetUtils;
import lib.basenet.okhttp.OkHttpRequest;
import lib.basenet.request.AbsDownloadRequestCallback;
import lib.basenet.request.AbsRequestCallBack;
import lib.basenet.request.IRequest;

/**
 * 断点上传，实际上不是真正的断点上传，而是将文件分块，分块上传；
 * 断点上传，需要服务器的支持;
 * 断点上传，采用单线程的上传方式，一片接一片进行上传（完美）
 * <p>
 * <pre>
 *     1. 初次上传时，将本地待上传的文件，拼接待上传文件信息json，格式为： {"fileName":"", "fileSize": , "md5": ""}
 *        传给服务器端，然后服务端会生成一个 id，客户端获取后，并设置当前上传的文件id（再缓存），用来标识这个上传的文件；
 *        参考：link
 *     2. 如果上传中途失败，客户端会缓存上次成功的切片位置；
 *     3. 再次上传时，
 * </pre>
 * Created by liyu20 on 2018/3/8.
 */
public class UploadFileManager {
    /**
     * 文件片段
     */
    private final FileSegmentInfo fileSegmentInfo;
    /**
     * 文件片段切割器
     */
    private final FileScissors fileScissors;
    private final AbsDownloadRequestCallback uploadListener;

    private final String uploadUrl;

    /**
     * UploadFileManager 构造
     *
     * @param srcFile   需要上传的源文件
     * @param serverId  serverId
     * @param uploadUrl 上传url
     * @param listener  监听器
     */
    public UploadFileManager(File srcFile, final String serverId, final String uploadUrl, final AbsDownloadRequestCallback listener) {
        if (srcFile == null || !srcFile.exists()) {
            throw new NullPointerException("file is invalid");
        }
        if (serverId == null || serverId.trim().length() <= 0) {
            throw new NullPointerException("必须传递的ServerId, 此ID为方便服务器关联当前上传的文件");
        }

        this.uploadListener = listener;
        fileSegmentInfo = new FileSegmentInfo(srcFile);
        fileScissors = new FileScissors();
        this.uploadUrl = uploadUrl;

        // 与服务器进行绑定
        fileSegmentInfo.setSrcFileID(serverId);

        // ===== 是否断点信息（缓存的）
        FileSegmentInfo cache = DownFileUtil.getCacheUploadInfo(fileSegmentInfo);
        // 已完成，删除缓存信息
        if (cache != null) {
            if (cache.getStatus() == FileSegmentInfo.SUCCESS) {
                DownFileUtil.remove(cache);
            } else {
                // 设置切片开始信息
                fileSegmentInfo.setSrcFileStart(cache.getSrcFileStart());
            }
        }
    }

    public void start() {
        // 上传中
        if (fileSegmentInfo.getStatus() == FileSegmentInfo.UPLOADING) {
            return;
        }
        switch (fileSegmentInfo.getStatus()) {
            case FileSegmentInfo.NOT_BEGIN:
                fileSegmentInfo.setStatus(FileSegmentInfo.UPLOADING);
                break;
            case FileSegmentInfo.UPLOADING:
                fileSegmentInfo.setStatus(FileSegmentInfo.NOT_BEGIN);
                return;
        }
        fileScissors.cutFile(fileSegmentInfo);
        uploadSegmentFile(fileSegmentInfo);
    }


    /**
     * 移除上传任务
     */
    public void delete() {
        NetUtils.getInstance().cancel(this.toString()); //
        DownFileUtil.remove(fileSegmentInfo);
        fileSegmentInfo.clear();
    }

    public void stop() {
        NetUtils.getInstance().cancel(this.toString()); // 移除任務
        // 完成时，不操作
        if (fileSegmentInfo.getStatus() != fileSegmentInfo.SUCCESS) {
            fileSegmentInfo.setStatus(fileSegmentInfo.NOT_BEGIN);
            DownFileUtil.addOrUpdate(fileSegmentInfo);
        }
    }

    /**
     * 分片上传,这块需要分散下
     *
     * @param fileSegmentInfo
     */
    private void uploadSegmentFile(final FileSegmentInfo fileSegmentInfo) {
        if (!fileSegmentInfo.getFileSegment().exists()) {
            if (uploadListener != null) {
                uploadListener.onFailure(new Exception("the file segment not exits!"));
            }
            return;
        }

        Map<String, File> fileMap = new HashMap<>();
        fileMap.put("files", fileSegmentInfo.getFileSegment());     // 当前切片
        Map<String, String> map = new HashMap<>();
        map.put("fileId", fileSegmentInfo.getSrcFileID());          // 服务端返回的文件ID
        map.put("fileStartRange", "" + fileSegmentInfo.getSrcFileStart());
        map.put("fileEndRange", "" + (fileSegmentInfo.getSrcFileStart() + fileSegmentInfo.getFileSegmentSize()));

        new OkHttpRequest.Builder()
                .tag(this.toString())
                .url(uploadUrl)
                .type(IRequest.RequestType.POST)
                .uploadFiles(fileMap)
                .body(map).callback(new AbsRequestCallBack() {
            @Override
            public void onSuccess(lib.basenet.response.Response response) {
                super.onSuccess(response);
                // 1. 更新信息
                fileSegmentInfo.setSrcFileStart(fileSegmentInfo.getSrcFileStart() + fileSegmentInfo.getFileSegmentSize());
                DownFileUtil.addOrUpdate(fileSegmentInfo);

                if (uploadListener != null) {
                    uploadListener.onProgressUpdate(fileSegmentInfo.getSrcFileSize(), fileSegmentInfo.getSrcFileStart(), fileSegmentInfo.getSrcFileStart() == fileSegmentInfo.getSrcFileSize());
                    if (fileSegmentInfo.getSrcFileStart() < fileSegmentInfo.getSrcFileSize()) {
                        if (fileSegmentInfo.getStatus() == FileSegmentInfo.UPLOADING) {
                            fileScissors.cutFile(fileSegmentInfo);      // 继续切
                            uploadSegmentFile(fileSegmentInfo);
                        } else {
                            uploadListener.onStop(fileSegmentInfo.getSrcFileSize(), fileSegmentInfo.getSrcFileStart(), null);
                        }
                    } else {
                        uploadListener.onSuccess(response);
                        fileSegmentInfo.setStatus(FileSegmentInfo.SUCCESS);
                        fileSegmentInfo.clear();
                        DownFileUtil.remove(fileSegmentInfo);
                    }
                }
            }

            @Override
            public void onFailure(Throwable e) {
                super.onFailure(e);
                if (uploadListener != null) {
                    uploadListener.onFailure(e);
                }
            }
        }).build().request();
    }
}
