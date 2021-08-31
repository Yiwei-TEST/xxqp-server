package com.sy.sanguo.common.util;

import com.alibaba.fastjson.JSONObject;
import com.sy.mainland.util.PropertiesCacheUtil;
import com.sy.sanguo.common.util.asyn.AsynUtil;
import com.sy.sanguo.common.util.request.HttpUtil;
import com.sy.sanguo.common.util.request.MD5Util;
import com.sy.sanguo.game.pdkuai.util.LogUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public final class CheckNetUtil {
    private static final String SECRET_KEY1 = "GD@H&J_%*_~69fGH;";
    private static final String SECRET_KEY2 = "sd!@$+:-*'90";
    private static final String SECRET_KEY3 = "/?\"|jk}5~gfh&";

    private static final ExecutorService executorService = Executors.newCachedThreadPool();
    private static final int TIME_OUT = 1;//超时时间s

    public static String[] loadGameUrl(int serverId, long totalCount) {
        final String url = PropertiesCacheUtil.getValue("check_net_url",Constants.GAME_FILE);
        if (StringUtils.isBlank(url)) {
            return null;
        } else {
            String type = "server" + serverId;
            String timestamp = String.valueOf(System.currentTimeMillis());
            final Map<String, String> params = new HashMap<>();
            params.put("totalCount", String.valueOf(totalCount));
            params.put("type", type);
            params.put("timestamp", timestamp);
            params.put("sign", MD5Util.getMD5String(new StringBuilder(80).append(timestamp).append(totalCount).append(type).append(SECRET_KEY2).append(SECRET_KEY1).append(SECRET_KEY3).toString()));

            String result = null;

            long time1 = System.currentTimeMillis();
            Future<String> mFuture = AsynUtil.EXECUTOR_SERVICE.submit(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    return HttpUtil.getUrlReturnValue(url + "/net/load", HttpUtil.DEFAULT_CHARSET, HttpUtil.POST, params, TIME_OUT);
                }
            });
            String extRet;
            try {
                result = mFuture.get(TIME_OUT, TimeUnit.SECONDS);
                extRet = "time(ms):" + (System.currentTimeMillis() - time1);
            } catch (Exception e) {
                extRet = "time(ms):" + (System.currentTimeMillis() - time1)+",timeout:" + TIME_OUT + "s,cancel=" + mFuture.cancel(true);
                LogUtil.e("Exception:" + e.getMessage(), e);
            }

            StringBuilder strBuilder = new StringBuilder().append("loadGameUrl:").append("time=").append(extRet).append(",url=").append(url).append("/net/load")
                    .append(",params=").append(params.toString()).append(",result=").append(result);

            String [] rets=new String[]{"","",""};
            if (StringUtils.isNotBlank(result)) {
                JSONObject jsonObject = JSONObject.parseObject(result);
                String ret = jsonObject.getString("datas");
                if (StringUtils.isNotBlank(ret)) {
                    rets[0] = DesUtil.strDec(ret, SECRET_KEY1, SECRET_KEY2, SECRET_KEY3);
                }
                ret = jsonObject.getString("datas1");
                if (StringUtils.isNotBlank(ret)) {
                    rets[1] = DesUtil.strDec(ret, SECRET_KEY1, SECRET_KEY2, SECRET_KEY3);
                }
                ret = jsonObject.getString("datas2");
                if (StringUtils.isNotBlank(ret)) {
                    rets[2] = DesUtil.strDec(ret, SECRET_KEY1, SECRET_KEY2, SECRET_KEY3);
                }
            }

            strBuilder.append(",returnUrl=").append(rets[0]).append(",").append(rets[1]).append(",").append(rets[2]);

            LogUtil.i(strBuilder.toString());

            return (StringUtils.isNotBlank(rets[0])||StringUtils.isNotBlank(rets[1])||StringUtils.isNotBlank(rets[2]))?rets:null;
        }
    }

}
