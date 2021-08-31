package com.sy.sanguo.game.service.pfs.xianliao;

import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.common.util.JsonWrapper;
import com.sy.sanguo.common.util.asyn.AsynUtil;
import com.sy.sanguo.game.pdkuai.util.LogUtil;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;

public final class XianliaoUtil {
    public static JsonWrapper getAccessToken(String appId, String secret, String code) {
        String url = "https://ssgw.updrips.com/oauth2/accessToken";
        String grant_type = "authorization_code";
        HashMap<String, String> params = new HashMap<String, String>();
        try {
            params.put("appid", appId);
            params.put("appsecret", secret);
            params.put("code", code);
            params.put("grant_type", grant_type);

            String resp = AsynUtil.submit(url,"UTF-8","GET",params,null,1);
            if (!StringUtils.isBlank(resp)) {
                JsonWrapper json=new JsonWrapper(resp);
                if ("0".equals(json.getString("err_code"))){
                    return new JsonWrapper(json.getString("data"));
                }
            }
            LogUtil.i("xianliao url="+url+",params="+params+",result="+resp);
        } catch (Exception e) {
            GameBackLogger.SYS_LOG.error("Xianliao getAccessToken error:"+e.getMessage(), e);
        }
        return null;
    }

    public static JsonWrapper refreshAccessToken(String appId, String secret, String refresh_token) {
        String url = "https://ssgw.updrips.com/oauth2/accessToken";
        String grant_type = "refresh_token";
        HashMap<String, String> params = new HashMap<String, String>();
        try {
            params.put("appid", appId);
            params.put("appsecret", secret);
            params.put("refresh_token", refresh_token);
            params.put("grant_type", grant_type);

            String resp = AsynUtil.submit(url,"UTF-8","GET",params,null,1);
            if (!StringUtils.isBlank(resp)) {
                JsonWrapper json=new JsonWrapper(resp);
                if ("0".equals(json.getString("err_code"))){
                    return new JsonWrapper(json.getString("data"));
                }
            }
            LogUtil.i("xianliao url="+url+",params="+params+",result="+resp);
        } catch (Exception e) {
            GameBackLogger.SYS_LOG.error("Xianliao refreshAccessToken error:"+e.getMessage(), e);
        }
        return null;
    }

    /**
     * 获取用户信息
     *
     * @param access_token
     * @return
     */
    public static JsonWrapper getUserinfo(String access_token) {
        String url = "https://ssgw.updrips.com/resource/user/getUserInfo";
        HashMap<String, String> params = new LinkedHashMap<>();
        try {
            params.put("access_token", access_token);

            String resp = AsynUtil.submit(url,"UTF-8","GET",params,null,1);
            if (!StringUtils.isBlank(resp)) {
                JsonWrapper json=new JsonWrapper(resp);
                if ("0".equals(json.getString("err_code"))){
                    return new JsonWrapper(json.getString("data"));
                }
            }
            LogUtil.i("xianliao url="+url+",params="+params+",result="+resp);
        } catch (Exception e) {
            GameBackLogger.SYS_LOG.error("Xianliao getUserinfo error:"+e.getMessage(), e);
        }
        return null;
    }

}
