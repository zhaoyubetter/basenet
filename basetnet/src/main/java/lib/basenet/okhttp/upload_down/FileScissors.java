package lib.basenet.okhttp.upload_down;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import lib.basenet.NetUtils;

/**
 * 文件切割器
 * Created by liyu20 on 2018/3/8.
 */
final class FileScissors {

    public void cutFile(FileSegmentInfo segmentInfo) {
        if (segmentInfo.getSrcFile() == null || !segmentInfo.getSrcFile().exists()) {
            throw new IllegalArgumentException("segmentInfo is invalid!");
        }
        try {
            String filePath = NetUtils.getInstance().getCacheDir() + "/fileScissorsTmp/" + segmentInfo.getSrcFile().getName();
            File tempFile = new File(filePath);
            if (tempFile.exists()) {
                tempFile.delete();
            }

            // 创建目录
            if(!tempFile.getParentFile().exists()) {
                tempFile.getParentFile().mkdirs();
            }

            if (!tempFile.createNewFile()) {
                throw new IOException("create new file fail");
            }
            FileOutputStream tempFileOut = new FileOutputStream(tempFile);

            int currentSize = 0;
            byte[] buffer = new byte[2048];
            RandomAccessFile randomFile = new RandomAccessFile(segmentInfo.getSrcFile(), "rwd");
            randomFile.seek(segmentInfo.getSrcFileStart());
            int len;
            while (currentSize < segmentInfo.getFileSegmentCutSize() && (len = randomFile.read(buffer)) != -1) {
                //写入文件
                tempFileOut.write(buffer, 0, len);
                currentSize += len;
            }
            randomFile.close();
            tempFileOut.flush();
            tempFileOut.close();

            segmentInfo.setFileSegment(tempFile);
            segmentInfo.setFileSegmentSize(currentSize);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
