package lib.basenet.volley;


import android.text.TextUtils;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;


/**
 * 让 volley 支持文件上传
 * 参考：http://blog.csdn.net/bboyfeiyu/article/details/42266869
 * Created by zhaoyu1 on 2017/3/10.
 */
class MultipartBody {


	private final static char[] MULTIPART_CHARS = "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

	private final String CONTENT_TYPE = "Content-Type: ";
	private final String CONTENT_DISPOSITION = "Content-Disposition: ";
	/**
	 * 文本参数和字符集
	 */
	private final String TYPE_TEXT_CHARSET = "text/plain; charset=UTF-8";

	/**
	 * 字节流参数
	 */
	private final String TYPE_OCTET_STREAM = "application/octet-stream";
	/**
	 * 二进制参数
	 */
	private final byte[] BINARY_ENCODING = "Content-Transfer-Encoding: binary\r\n\r\n".getBytes();
	/**
	 * 文本参数
	 */
	private final byte[] BIT_ENCODING = "Content-Transfer-Encoding: 8bit\r\n\r\n".getBytes();

	/**
	 * 换行符
	 */
	private final String NEW_LINE_STR = "\r\n";

	/**
	 * 分隔符
	 */
	private String mBoundary = null;
	/**
	 * 输出流
	 */
	ByteArrayOutputStream mOutputStream = new ByteArrayOutputStream();


	private StringBuilder mSb = new StringBuilder();


	public MultipartBody() {
		this.mBoundary = generateBoundary();
	}

	/**
	 * 生成分隔符
	 *
	 * @return
	 */
	private String generateBoundary() {
		final StringBuffer buf = new StringBuffer();
		final Random rand = new Random();
		for (int i = 0; i < 30; i++) {
			buf.append(MULTIPART_CHARS[rand.nextInt(MULTIPART_CHARS.length)]);
		}
		return buf.toString();
	}

	/**
	 * 开头分隔符
	 * --WebKitFormBoundaryMcD0BA59mk3aFx4I
	 */
	private void writeFirstBoundary() throws IOException {
		mOutputStream.write(("--" + mBoundary + NEW_LINE_STR).getBytes());
		mSb.append(("--" + mBoundary + NEW_LINE_STR));
	}

	/**
	 * 添加文本参数
	 */
	public void addStringPart(String paramName, String paramValue) {
		// writeToOutputStream(paramName, paramValue.getBytes(), TYPE_TEXT_CHARSET, BIT_ENCODING, "");
		writeToOutputStream(paramName, paramValue.getBytes(), null, null, "");
	}

	/**
	 * 添加二进制参数, 例如Bitmap的字节流参数
	 *
	 * @param paramName
	 * @param rawData
	 */
	public void addBinaryPart(String paramName, final byte[] rawData) {
		writeToOutputStream(paramName, rawData, TYPE_OCTET_STREAM, BINARY_ENCODING, "no-file");
	}

	/**
	 * 添加文件参数,可以实现文件上传功能
	 *
	 * @param key
	 * @param file
	 */
	public void addFilePart(final String key, final File file) {
		InputStream fin = null;
		try {
			fin = new FileInputStream(file);
			writeFirstBoundary();
			mOutputStream.write(getContentDispositionBytes(key, file.getName()));
			final String type = CONTENT_TYPE + TYPE_OCTET_STREAM + NEW_LINE_STR;
			mOutputStream.write(type.getBytes());
			mOutputStream.write(NEW_LINE_STR.getBytes());

			final byte[] tmp = new byte[4096];
			int len = 0;
			while ((len = fin.read(tmp)) != -1) {
				mOutputStream.write(tmp, 0, len);
			}
			mOutputStream.write(NEW_LINE_STR.getBytes());
			mOutputStream.flush();
		} catch (final IOException e) {
			e.printStackTrace();
		} finally {
			closeSilently(fin);
		}
	}


	/**
	 * 将数据写入到输出流中
	 *
	 * @param paramName
	 * @param rawData
	 * @param type
	 * @param encodingBytes
	 * @param fileName
	 */
	private void writeToOutputStream(String paramName,
									 byte[] rawData,
									 String type,
									 byte[] encodingBytes,
									 String fileName) {
		try {
			writeFirstBoundary();
			if (type != null && type.length() > 0) {
				mOutputStream.write((CONTENT_TYPE + type + NEW_LINE_STR).getBytes());
			}
			mOutputStream.write(getContentDispositionBytes(paramName, fileName));
			mOutputStream.write(NEW_LINE_STR.getBytes());
			mSb.append(new String(rawData));

			if (encodingBytes != null) {
				mOutputStream.write(encodingBytes);
			}

			mOutputStream.write(rawData);
			mOutputStream.write(NEW_LINE_STR.getBytes());
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	private byte[] getContentDispositionBytes(String paramName, String fileName) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(CONTENT_DISPOSITION + "form-data; name=\"" + paramName + "\"");
		if (!TextUtils.isEmpty(fileName)) {
			stringBuilder.append("; filename=\"" + fileName + "\"");
		}
		return stringBuilder.append(NEW_LINE_STR).toString().getBytes();
	}


	public long getContentLength() {
		return mOutputStream.toByteArray().length;
	}

	// Content-Type: multipart/form-data; boundary=
	public String getContentType() {
		return "multipart/form-data; boundary=" + mBoundary;
	}

	public void writeTo(final OutputStream outstream) throws IOException {
		// 参数最末尾的结束符
		final String endString = "--" + mBoundary + "--" + NEW_LINE_STR;
		// 写入结束符
		mOutputStream.write(endString.getBytes());
		outstream.write(mOutputStream.toByteArray());
	}


	private void closeSilently(Closeable closeable) {
		try {
			if (closeable != null) {
				closeable.close();
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}
}
