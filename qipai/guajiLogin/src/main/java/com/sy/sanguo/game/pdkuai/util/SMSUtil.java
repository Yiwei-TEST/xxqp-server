package com.sy.sanguo.game.pdkuai.util;

import com.alibaba.fastjson.JSON;
import com.sy.mainland.util.HttpsUtil;
import com.sy.mainland.util.SHAUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by pc on 2017/4/18.
 */
public class SMSUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(SMSUtil.class);
    private static final Logger LOGGER_SYS = LoggerFactory.getLogger("sys");

    public static final String sendSMS(String appId, String appKey, String msg, String strMobile) {
        StringBuilder stringBuilder = new StringBuilder();

        long strRand = System.currentTimeMillis(); //url中的random字段的值
        long strTime = strRand / 1000; //unix时间戳

        stringBuilder.append("appkey=").append(appKey);
        stringBuilder.append("&random=").append(strRand);
        stringBuilder.append("&time=").append(strTime);
        stringBuilder.append("&mobile=").append(strMobile);

        String sig = SHAUtil.sha256(stringBuilder.toString());

        String url = new StringBuilder("https://yun.tim.qq.com/v5/tlssmssvr/sendsms?sdkappid=")
                .append(appId).append("&random=").append(strRand).toString();

        Map<String, Object> dataMap = new LinkedHashMap<>();
        Map<String, Object> data1Map = new LinkedHashMap<>();
        data1Map.put("nationcode", "86");
        data1Map.put("mobile", strMobile);

        dataMap.put("tel", data1Map);
        dataMap.put("type", 0);
        dataMap.put("msg", msg);
        dataMap.put("sig", sig);
        dataMap.put("time", strTime);
        dataMap.put("extend", "");
        dataMap.put("ext", "");

        String temp = JSON.toJSONString(dataMap);

        Map<String, String> params = new HashMap<>();
        params.put("$", temp);
        String result = HttpsUtil.getUrlReturnValue(url, "UTF-8", "POST", params);
        LOGGER.info("url:{},data:{},result:{}", url, temp, result);
        return result;
    }

    public static final String sendSMSNew(String appId, String appKey, String strMobile,String sign,int verifyCode,int tplId) {
        StringBuilder stringBuilder = new StringBuilder();
        long strRand = System.currentTimeMillis(); //url中的random字段的值
        long strTime = strRand / 1000; //unix时间戳
        stringBuilder.append("appkey=").append(appKey);
        stringBuilder.append("&random=").append(strRand);
        stringBuilder.append("&time=").append(strTime);
        stringBuilder.append("&mobile=").append(strMobile);

        String sig = SHAUtil.sha256(stringBuilder.toString());

        String url = new StringBuilder("https://yun.tim.qq.com/v5/tlssmssvr/sendsms?sdkappid=")
                .append(appId).append("&random=").append(strRand).toString();

        Map<String, Object> dataMap = new LinkedHashMap<>();
        Map<String, Object> data1Map = new LinkedHashMap<>();
        data1Map.put("nationcode", "86");
        data1Map.put("mobile", strMobile);

        dataMap.put("tel", data1Map);
        dataMap.put("sig", sig);
        dataMap.put("time", strTime);
        dataMap.put("extend", "");
        dataMap.put("ext", "");
        dataMap.put("sign",sign);
        dataMap.put("tpl_id",tplId);
        dataMap.put("params",new int[]{verifyCode});

        String temp = JSON.toJSONString(dataMap);

        Map<String, String> params = new HashMap<>();
        params.put("$", temp);
        String result = HttpsUtil.getUrlReturnValue(url, "UTF-8", "POST", params);
        return result;
    }
}
