package com.sy599.game.gcommand.login.base.pfs.qq;

import com.sy599.game.db.bean.RegInfo;
import com.sy599.game.gcommand.login.base.BaseSdk;
import com.sy599.game.gcommand.login.base.pfs.configs.PfSdkConfig;
import com.sy599.game.gcommand.login.base.pfs.configs.PfUtil;
import com.sy599.game.gcommand.login.util.LoginUtil;
import com.sy599.game.util.JsonWrapper;
import com.sy599.game.util.LogUtil;
import com.sy599.game.util.MD5Util;
import com.sy599.game.util.StringUtil;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class QQ extends BaseSdk {
    @Override
    public String loginExecute() {

        String result = "";
        String access_token = params.getString("access_token");
        String openid = params.getString("openid");
        String pf = getPf();

        PfSdkConfig pfSdkConfig = PfUtil.getConfig(pf);

        if (pfSdkConfig!=null){
            long time1=System.currentTimeMillis();

            JsonWrapper json = QqUtil.getUserinfo(pfSdkConfig.getAppId(),access_token,openid);

            LogUtil.msgLog.info("qq login time(ms):" + (System.currentTimeMillis() - time1));

            if (json != null) {
                if ("0".equals(json.getString("ret"))) {
                    setSdkId(openid);
                    json.putString("access_token",access_token);
                    result = json.toString();
                } else {
                    LogUtil.msgLog.error(pf + " loginExecute err:" + json.toString());
                }
            }
        }

        return result;
    }

    @Override
    public void createRole(RegInfo regInfo, String info) throws Exception {
        JsonWrapper wrapper = new JsonWrapper(info);
        String openid = params.getString("openid");
        String nickname = wrapper.getString("nickname");
        nickname = StringUtil.filterEmoji(nickname);
        String headimgurl = wrapper.getString("figureurl_2");
        int sex = "男".equals(wrapper.getString("gender"))?1:2;
        if (headimgurl==null){
            headimgurl="";
        }
        regInfo.setFlatId(openid);
        regInfo.setName(nickname);
        String imageName = MD5Util.getMD5String(headimgurl);
        regInfo.setHeadimgraw(imageName);
        regInfo.setHeadimgurl(headimgurl);
        regInfo.setSex(sex);
        regInfo.setIdentity(openid);
        String password = "xsg_" + regInfo.getPf() + "_pw_default_" + regInfo.getFlatId();
        regInfo.setPw(LoginUtil.genPw(password));
        regInfo.setSessCode(LoginUtil.genSessCode(regInfo.getFlatId()));

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
            map.put("headimgraw", MD5Util.getMD5String(headimgurl));
            regInfo.setHeadimgurl(headimgurl);
        }

        return map;
    }

}
