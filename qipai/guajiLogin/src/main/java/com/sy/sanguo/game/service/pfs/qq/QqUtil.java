package com.sy.sanguo.game.service.pfs.qq;

import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.common.util.JsonWrapper;
import com.sy.sanguo.common.util.asyn.AsynUtil;
import com.sy.sanguo.game.pdkuai.util.LogUtil;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;

public final class QqUtil {
    /**
     * 获取用户信息
     *
     * @param access_token
     * @return
     */
    public static JsonWrapper getUserinfo(String appid,String access_token,String openid) {
        String url = "https://graph.qq.com/user/get_user_info";
        HashMap<String, String> params = new LinkedHashMap<>();
        try {
            params.put("access_token", access_token);
            params.put("oauth_consumer_key", appid);
            params.put("openid", openid);

            String resp = AsynUtil.submit(url,"UTF-8","GET",params,null,1);
            if (!StringUtils.isBlank(resp)) {
                JsonWrapper json=new JsonWrapper(resp);
                if ("0".equals(json.getString("ret"))){
                    return json;
                }
            }
            LogUtil.i("qq url="+url+",params="+params+",result="+resp);
        } catch (Exception e) {
            GameBackLogger.SYS_LOG.error("QQ getUserinfo error:"+e.getMessage(), e);
        }
        return null;
    }

}
