package lib.basenet.okhttp;


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
    public static void remove(DownloadFileInfo downloadFileInfo) {
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
    public static DownloadFileInfo getCacheFileInfo(DownloadFileInfo downloadFileInfo) {
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
    public static void addOrUpdate(DownloadFileInfo downloadFileInfo) {
        try {
            // 1. 先删除，再新增
            final DiskLruCache cache = getLruCache();
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
}

