package lib.basenet.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.internal.Util;

/**
 * Created by zhaoyu1 on 2017/3/9.
 */

public final class FileUtils {
    public static final void saveFile(InputStream is, File file) throws IOException {
        InputStream inputStream = is;
        if (null != inputStream) {
            FileOutputStream fos = null;
            int len = 0;
            try {
                byte[] bys = new byte[4 * 1024];
                fos = new FileOutputStream(file);
                while ((len = inputStream.read(bys)) != -1) {
                    fos.write(bys, 0, len);
                }
                fos.flush();
            } finally {
                Util.closeQuietly(fos);
            }
        }
    }
}
