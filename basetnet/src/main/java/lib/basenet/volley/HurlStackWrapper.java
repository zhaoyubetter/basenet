//package lib.basenet.volley;
//
//import com.android.volley.toolbox.HurlStack;
//
//import java.io.IOException;
//import java.net.HttpURLConnection;
//import java.net.URL;
//
///**
// * Created by zhaoyu on 2017/3/15.
// */
//
//public class HurlStackWrapper extends HurlStack {
//	@Override
//	protected HttpURLConnection createConnection(URL url) throws IOException {
//		HttpURLConnection connection = super.createConnection(url);
//		connection.setChunkedStreamingMode(128 * 1024);
//		return connection;
//	}
//}
