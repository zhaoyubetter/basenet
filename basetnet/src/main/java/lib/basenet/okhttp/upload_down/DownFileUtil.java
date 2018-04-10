package lib.basenet.okhttp.upload_down;


import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import lib.basenet.NetUtils;
import okhttp3.internal.cache.DiskLruCache;
import okhttp3.internal.io.FileSystem;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.ByteString;
import okio.Okio;

/**
 * 下载帮助类
 * Created by zhaoyu1 on 2018/2/28.
 */

class DownFileUtil {

    private static final int VERSION = 1;
    private static final String SEPARATE = "==";
    private static DiskLruCache lruCache;

    private static DiskLruCache getLruCache() {
        if (lruCache == null) {
            synchronized (DownFileUtil.class) {
                if (lruCache == null) {
                    String cacheDir = NetUtils.getInstance().getCacheDir() + "/downOrUpload";
                    File file = new File(cacheDir);
                    if (!file.exists()) {
                        file.mkdirs();
                    }
                    lruCache = DiskLruCache.create(FileSystem.SYSTEM, file, VERSION, 1, 5 * 1024 * 1024);
                }
            }
        }
        return lruCache;
    }

    private static String getKey(String fileUrl, String filePath) {
        return ByteString.encodeUtf8(fileUrl + SEPARATE + filePath).md5().hex();
    }

    /**
     * 移除
     *
     * @param downloadFileInfo
     */
    public synchronized static void remove(DownloadFileInfo downloadFileInfo) {
        String key = getKey(downloadFileInfo.fileUrl, downloadFileInfo.localFilePath);
        final DiskLruCache cache = getLruCache();
        try {
            cache.remove(key);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取断点信息
     *
     * @param downloadFileInfo
     * @return
     */
    public synchronized static DownloadFileInfo getCacheFileInfo(DownloadFileInfo downloadFileInfo) {
        DownloadFileInfo cacheDownFileInfo = null;
        try {
            final DiskLruCache cache = getLruCache();
            final String key = getKey(downloadFileInfo.fileUrl, downloadFileInfo.localFilePath);
            final DiskLruCache.Snapshot snapshot = cache.get(key);
            if (snapshot != null) {
                final BufferedSource buffer = Okio.buffer(snapshot.getSource(0));
                ObjectInputStream ois = new ObjectInputStream(buffer.inputStream());
                cacheDownFileInfo = (DownloadFileInfo) ois.readObject();
                ois.close();
            }
        } catch (Exception e) {
            cacheDownFileInfo = null;
        }
        return cacheDownFileInfo;
    }


    /**
     * 更新，or 设置
     *
     * @param downloadFileInfo
     */
    public synchronized static void addOrUpdate(DownloadFileInfo downloadFileInfo) {
        try {
            // 1. 先删除，再新增
            final DiskLruCache cache = getLruCache();
            // 不缓存下载中，状态
            if (downloadFileInfo.status == DownloadFileInfo.DOWNLOADING) {
                downloadFileInfo.status = DownloadFileInfo.FAILURE;
            }
            final String key = getKey(downloadFileInfo.fileUrl, downloadFileInfo.localFilePath);
            cache.remove(key);

            DiskLruCache.Editor editor = cache.edit(key);
            if (editor != null) {
                BufferedSink sink = Okio.buffer(editor.newSink(0));
                ObjectOutputStream oos = new ObjectOutputStream(sink.outputStream());
                oos.writeObject(downloadFileInfo);
                editor.commit();
                oos.flush();
                oos.close();
            }
        } catch (Exception e) {

        }
    }


    /**
     * 获取上传文件的缓存信息
     *
     * @param segmentInfo
     * @return
     */
    public synchronized static FileSegmentInfo getCacheUploadInfo(FileSegmentInfo segmentInfo) {
        FileSegmentInfo cacheInfo = null;
        try {
            final DiskLruCache cache = getLruCache();
            final String key = getKey(segmentInfo.getSrcFile().getAbsolutePath(), "");
            final DiskLruCache.Snapshot snapshot = cache.get(key);
            if (snapshot != null) {
                final BufferedSource buffer = Okio.buffer(snapshot.getSource(0));
                ObjectInputStream ois = new ObjectInputStream(buffer.inputStream());
                cacheInfo = (FileSegmentInfo) ois.readObject();
                ois.close();
            }
        } catch (Exception e) {
            cacheInfo = null;
        }
        return cacheInfo;
    }

    /**
     * 更新，or 设置
     *
     * @param info
     */
    public synchronized static void addOrUpdate(FileSegmentInfo info) {
        try {
            // 1. 先删除，再新增
            final DiskLruCache cache = getLruCache();
            final String key = getKey(info.getSrcFile().getAbsolutePath(), "");
            cache.remove(key);

            DiskLruCache.Editor editor = cache.edit(key);
            if (editor != null) {
                BufferedSink sink = Okio.buffer(editor.newSink(0));
                ObjectOutputStream oos = new ObjectOutputStream(sink.outputStream());
                oos.writeObject(info);
                editor.commit();
                oos.flush();
                oos.close();
            }
        } catch (Exception e) {
        }
    }

    /**
     * 移除上传文件缓存信息
     */
    public static synchronized void remove(FileSegmentInfo info) {
        String key = getKey(info.getSrcFile().getAbsolutePath(), "");
        final DiskLruCache cache = getLruCache();
        try {
            cache.remove(key);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

