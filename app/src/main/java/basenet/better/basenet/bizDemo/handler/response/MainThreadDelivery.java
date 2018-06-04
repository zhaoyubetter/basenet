package basenet.better.basenet.bizDemo.handler.response;

import android.os.Handler;
import android.os.Looper;

/**
 * Created by zhaoyu1 on 2018/3/9.
 */
final class MainThreadDelivery {
    private static final Handler handler = new Handler(Looper.getMainLooper());

    public final static void post( Runnable runnable) {
        handler.post(runnable);
    }
}
