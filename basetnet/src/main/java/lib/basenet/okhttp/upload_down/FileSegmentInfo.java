package lib.basenet.okhttp.upload_down;

import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * Created by liyu20 on 2018/3/8.
 */
public final class FileSegmentInfo implements Serializable {
    //切割文件大小，kb，文件大于该值，均分为100份，文件小于该值，按照12kb切分
    public static final int SEGMENT_FILE_SIZE = 12;
    public static final int NOT_BEGIN = 0;
    public static final int UPLOADING = 1;
    public static final int SUCCESS = 2; // 成功

    /**
     * 状态，缓存
     */
    private int status = NOT_BEGIN;
    /**
     * 源文件开始读取的位置，缓存
     **/
    private long srcFileStart;

    /**
     * 每一份切片文件大小
     */
    private transient long fileSegmentCutSize;
    private transient File fileSegment;
    private transient long fileSegmentSize;

    //所属文件的MD5值
    private transient String srcFileMd5;
    //所属文件的大小
    private transient long srcFileSize;
    //所属文件的名字
    private transient String srcFileName;
    /**
     * 所属文件的序列号（后台返给）
     **/
    private String serverFileID;
    private transient File srcFile;

    public FileSegmentInfo(File srcFile) {
        this.srcFileMd5 = getFileMD5(srcFile);
        this.srcFileSize = srcFile.length();
        this.srcFileName = srcFile.getName();
        this.srcFile = srcFile;
        if (srcFile.length() > 1024 * SEGMENT_FILE_SIZE * 100) {  // SEGMENT_FILE_SIZE = 12, 约1.1MB
            fileSegmentCutSize = srcFile.length() / 100;          //
        } else {
            fileSegmentCutSize = SEGMENT_FILE_SIZE * 1024;        // 12kb
        }
    }

    public void setFileSegment(File fileSegment) {
        this.fileSegment = fileSegment;
    }

    public void setFileSegmentSize(long fileSegmentSize) {
        this.fileSegmentSize = fileSegmentSize;
    }

    public void setSrcFileStart(long srcFileStart) {
        this.srcFileStart = srcFileStart;
    }

    public void setServerFileID(String serverFileID) {
        this.serverFileID = serverFileID;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public File getFileSegment() {
        return fileSegment;
    }

    public long getFileSegmentSize() {
        return fileSegmentSize;
    }

    public long getSrcFileStart() {
        return srcFileStart;
    }

    public String getSrcFileMd5() {
        return srcFileMd5;
    }

    public long getSrcFileSize() {
        return srcFileSize;
    }

    public String getSrcFileName() {
        return srcFileName;
    }

    public String getServerFileID() {
        return serverFileID;
    }

    public File getSrcFile() {
        return srcFile;
    }

    public long getFileSegmentCutSize() {
        return fileSegmentCutSize;
    }

    public int getStatus() {
        return status;
    }

    public void clear() {
        if (this.fileSegment != null && this.fileSegment.exists()) {
            fileSegment.delete();
        }
        srcFileStart = 0;
    }

    /**
     * 获取单个文件的MD5值！
     *
     * @param file
     * @return
     */

    public static String getFileMD5(File file) {
        if (!file.isFile()) {
            return null;
        }
        MessageDigest digest;
        FileInputStream in;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        BigInteger bigInt = new BigInteger(1, digest.digest());
        String md5 = bigInt.toString(16);
        // 当md5值首位为0时，bitInt会将其省略，因此需要手动补零
        String coverZero = "";
        for (int i = 0; i < 32 - md5.length(); i++) {
            coverZero += "0";
        }
        return coverZero + md5;
    }
}
