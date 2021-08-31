package com.sy599.game.common.asyn;

import com.sy.mainland.util.HttpUtil;
import com.sy599.game.util.LogUtil;

import java.util.Map;
import java.util.concurrent.*;

public class AsynUtil {

    public static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();
    public static final int TIME_OUT = 1;//超时时间s

    public static final String submit(final String urlAddress, final String charset, final String method,
                                      final Map<String, String> paramsMap, final Map<String, String> requestPropertiesMap, final int seconds) {
        String result = null;

        long time1 = System.currentTimeMillis();
        Future<String> mFuture = EXECUTOR_SERVICE.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return HttpUtil.getUrlReturnValue(urlAddress, charset == null ? HttpUtil.DEFAULT_CHARSET : charset, method == null ? HttpUtil.GET : method, paramsMap, requestPropertiesMap, seconds < 1 ? TIME_OUT : seconds);
            }
        });
        StringBuilder strBuilder = new StringBuilder(1024);
        strBuilder.append("url=").append(urlAddress)
                .append(",params=").append(paramsMap).append(",headers=").append(requestPropertiesMap);

        try {
            result = mFuture.get(seconds < 1 ? TIME_OUT : seconds, TimeUnit.SECONDS);
            strBuilder.append(",time(ms):").append(System.currentTimeMillis() - time1);
        } catch (Throwable e) {
            strBuilder.append(",time(ms):").append(System.currentTimeMillis() - time1).append(",timeout:").append(seconds < 1 ? TIME_OUT : seconds).append("s,cancel=").append(mFuture.cancel(true));
            LogUtil.msgLog.error("Exception:" + e.getMessage(), e);
        }

        strBuilder.append(",result=").append(result);

        LogUtil.msgLog.info("asyn submit:{}", strBuilder.toString());

        return result;
    }
}
