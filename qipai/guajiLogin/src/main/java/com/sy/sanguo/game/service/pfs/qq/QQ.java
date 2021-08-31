package com.sy.sanguo.game.service.pfs.qq;

import com.sy.mainland.util.PropertiesCacheUtil;
import com.sy.sanguo.common.log.GameBackLogger;
import com.sy.sanguo.common.util.*;
import com.sy.sanguo.game.bean.PfSdkConfig;
import com.sy.sanguo.game.bean.RegInfo;
import com.sy.sanguo.game.pdkuai.util.LogUtil;
import com.sy.sanguo.game.service.BaseSdk;
import com.sy.sanguo.game.staticdata.PfCommonStaticData;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class QQ extends BaseSdk {
    @Override
    public String payExecute() {
        return null;
    }

    @Override
    public String loginExecute() {
        String result = "";
        String access_token = getString("access_token");
        String openid = getString("openid");
        String pf = getPf();

        PfSdkConfig pfSdkConfig = PfCommonStaticData.getConfig(pf);

        if (pfSdkConfig!=null){
            long time1=System.currentTimeMillis();

            JsonWrapper json = QqUtil.getUserinfo(pfSdkConfig.getAppId(),access_token,openid);

            if ("1".equals(PropertiesCacheUtil.getValueOrDefault("qq_login_time","1",Constants.GAME_FILE))) {
                LogUtil.i("qq login time(ms):" + (System.currentTimeMillis() - time1));
            }

            if (json != null) {
                if ("0".equals(json.getString("ret"))) {
                    setSdkId(openid);
                    json.putString("access_token",access_token);
                    result = json.toString();
                } else {
                    GameBackLogger.SYS_LOG.error(pf + " loginExecute err:" + json.toString());
                }
            }
        }

        return result;
    }

    @Override
    public void createRole(RegInfo regInfo, String info) throws Exception {
        JsonWrapper wrapper = new JsonWrapper(info);
        String openid = getString("openid");
        String nickname = wrapper.getString("nickname");
        nickname = StringUtil.filterEmoji(nickname);
        String headimgurl = wrapper.getString("figureurl_2");
        int sex = "男".equals(wrapper.getString("gender"))?1:2;
        if (headimgurl==null){
            headimgurl="";
        }
        regInfo.setFlatId(openid);
        regInfo.setName(nickname);
        String imageName = MD5Util.getStringMD5(headimgurl);
        regInfo.setHeadimgraw(imageName);
        regInfo.setHeadimgurl(headimgurl);
        regInfo.setSex(sex);
        regInfo.setIdentity(openid);
        String password = "xsg_" + regInfo.getPf() + "_pw_default_" + regInfo.getFlatId();
        regInfo.setPw(RegUtil.genPw(password));
        regInfo.setSessCode(RegUtil.genSessCode(regInfo.getFlatId()));

        JsonWrapper jsonWrapper = new JsonWrapper("");
        jsonWrapper.putString("qq",nickname);
        regInfo.setLoginExtend(jsonWrapper.toString());
    }

    @Override
    public Map<String, Object> refreshRole(RegInfo regInfo, String info) throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        JsonWrapper wrapper = new JsonWrapper(info);
        String nickname = wrapper.getString("nickname");
        String headimgurl = wrapper.getString("figureurl_2");
        String unionid = wrapper.getString("openid");
        int sex = "男".equals(wrapper.getString("gender"))?1:2;
        if (headimgurl==null){
            headimgurl="";
        }
        if (StringUtils.isBlank(regInfo.getIdentity())) {
            map.put("identity", unionid);
            regInfo.setIdentity(unionid);
        }

        nickname = StringUtil.filterEmoji(nickname);
        if (regInfo.getName()==null||!regInfo.getName().equals(nickname)) {
            map.put("name", nickname);
            regInfo.setName(nickname);
        }

        JsonWrapper jsonWrapper = new JsonWrapper(regInfo.getLoginExtend());
        if (!org.apache.commons.lang3.StringUtils.equals(nickname,jsonWrapper.getString("qq"))){
            jsonWrapper.putString("qq",nickname);
            regInfo.setLoginExtend(jsonWrapper.toString());
            map.put("loginExtend", regInfo.getLoginExtend());
        }

        if (regInfo.getSex() != sex) {
            map.put("sex", sex);
            regInfo.setSex(sex);
        }

        if (headimgurl!=null&&!headimgurl.equals(regInfo.getHeadimgurl())) {
            map.put("headimgurl", headimgurl);
            map.put("headimgraw", MD5Util.getStringMD5(headimgurl));
            regInfo.setHeadimgurl(headimgurl);
        }

        return map;
    }

}
